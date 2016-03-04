/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftPwm;
import javafx.scene.input.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.pidome.pcl.storage.preferences.LocalPreferenceStorage;
import org.pidome.pcl.utilities.math.MathUtilities;

/**
 *
 * @author John
 */
public class PhysicalDisplay extends PhysicalDisplayInterface {

    private GpioController controller;

    GpioPinPwmOutput brightnessPin;

    IntegerProperty brightness = new SimpleIntegerProperty(0);
    private boolean brightnessInitialized = false;
    int minBrightness = 10;
    int maxBrightness = 000;
    
    long brightnessCheck = 60000;
    
    LocalPreferenceStorage prefs;
            
    SimpleBooleanProperty displayOn = new SimpleBooleanProperty(true);
    Timer powerOffTimer;
    long powerOffInterval = 30l;
    
    private StackPane touchCaptureOverlay;
    
    private static final Logger LOG = Logger.getLogger(PhysicalDisplay.class.getName());        
    
    private static List<Support> supportsList = new ArrayList<PhysicalDisplayInterface.Support>() {
            {
                add(PhysicalDisplayInterface.Support.BRIGHTNESS);
                add(PhysicalDisplayInterface.Support.EMULATE_ON_OFF);
            }
        };
    
    public PhysicalDisplay(ServiceConnector connector) {
        super(connector);
        if(System.getProperty("com.sun.javafx.isEmbedded")!=null && System.getProperty("com.sun.javafx.isEmbedded").equals("true")){
            try {
                controller = GpioFactory.getInstance();
            } catch (Exception ex){
                LOG.warning("Could not load Pi4J library. If not running on raspberry pi you can discard this message.");
                /// legal miss when not running on the pi.
            }
        }
    }

    @Override
    public List<Support> getAvailableSupportTypes() {
        return supportsList;
    }

    @Override
    public final boolean brightnessInitialized(){
        return brightnessInitialized;
    }
    
    @Override
    public void setBrightness(int value) {
        brightness.setValue(value);
    }

    @Override
    public int getBrightness() {
        return brightness.getValue();
    }

    @Override
    public void setDisplayOn(boolean value) {

    }

    @Override
    public boolean getDisplayOn() {
        return true;
    }

    @Override
    public void init() {
        prefs = this.getServiceConnector().getPreferences();
        
        minBrightness = prefs.getIntPreference("org.pidome.client.fixed.settings.backlight.min", 10);
        maxBrightness = prefs.getIntPreference("org.pidome.client.fixed.settings.backlight.max", 100);
        
        brightnessCheck = prefs.getLongPreference("org.pidome.client.fixed.settings.backlight.in.interval", 60) * 1000l;
        
        if(prefs.getBoolPreference("org.pidome.client.fixed.settings.backlight.emulatepower", false) == true){
            LOG.info("Emulate power off by using brightness");
            powerOffInterval = prefs.getLongPreference("org.pidome.client.fixed.settings.backlight.emulatepower.off.timeout", 30);
            LOG.info("Power off interval set at: " + powerOffInterval + " seconds.");
            setupPowerOffEmulationByBrightness();
        } else {
            LOG.info("Emulate power off by using brightness not available");
        }
        
        if(prefs.getStringPreference("org.pidome.client.fixed.settings.backlight.out.type", "gpio").equals("gpio")){
            LOG.info("Brightness conrol using GPIO");
            setupBrightnessGPIOControl();
        } else {
            LOG.info("No brightness conrol");
        }
        if(prefs.getStringPreference("org.pidome.client.fixed.settings.backlight.in.type", "gpio").equals("gpio")){
            LOG.info("Ambient light sensing using GPIO");
            setupLDRGPIOControl();
        } else {
            LOG.info("No ambient light sensing");
        }
    }

    @Override
    public final StackPane getTouchOverlay(){
        if(touchCaptureOverlay==null){
            touchCaptureOverlay = new StackPane();
            touchCaptureOverlay.setMinSize(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
            touchCaptureOverlay.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent event) -> {
                if(!displayOn.getValue()){
                    Platform.runLater(() -> { 
                        touchCaptureOverlay.toBack();
                    });
                    event.consume();
                    createPowerOffTimer();
                    displayOn.setValue(true);
                } else {
                    
                }
            });
        }
        return touchCaptureOverlay;
    }
    
    private void createPowerOffTimer(){
        purgeTimerOffTimer();
        powerOffTimer = new Timer(); //Instantiate again, as we Cancel the Timer
        powerOffTimer.schedule(new PowerOffTimerTask(), powerOffInterval * 1000);
    }
    
    private void purgeTimerOffTimer(){
        if(powerOffTimer!=null){
            powerOffTimer.cancel();
            powerOffTimer.purge();
        }
    }

    @Override
    public void updateBlankTimer() {
        createPowerOffTimer();
    }
    
    private class PowerOffTimerTask extends TimerTask {

        @Override
        public void run() {
            displayOn.setValue(false);
            Platform.runLater(() -> { 
                touchCaptureOverlay.toFront();
            });
        }
        
    }
    
    private void setupPowerOffEmulationByBrightness(){
        createPowerOffTimer();
    }
    
    private void setupLDRGPIOControl(){
        if(controller!=null){
            int ldrPinNumber = prefs.getIntPreference("org.pidome.client.fixed.settings.backlight.gpio.in", -1);
            Thread ldrRead = new Thread(() -> {
                if(ldrPinNumber!=-1){
                    GpioPinDigitalOutput ldrPin = controller.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + String.valueOf(ldrPinNumber)));
                    if(ldrPin!=null){
                        int lightThreshold = prefs.getIntPreference("org.pidome.client.fixed.settings.backlight.gpio.in.min", 10);
                        int darkThreshold = prefs.getIntPreference("org.pidome.client.fixed.settings.backlight.gpio.in.max", 100);
                        LOG.info("LDR read enabled using wiringpi numbered pin: " + ldrPin.getName());
                        while(true){
                            int value = 0;
                            ldrPin.setMode(PinMode.DIGITAL_OUTPUT);
                            ldrPin.setState(PinState.LOW);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                LOG.warning("Cold not wait for slow update");
                                break;
                            }
                            Date startMeasure = new Date();
                            ldrPin.setMode(PinMode.DIGITAL_INPUT);
                            while (ldrPin.getState() == PinState.LOW){
                                value++;
                                if ((value % 3000) == 0) {
                                    break;
                                }
                            }
                            Date endMeasure = new Date();
                            if(brightnessInitialized){
                                long amountOfTime = (endMeasure.getTime() - startMeasure.getTime());
                                LOG.info("It took "+amountOfTime+" miliseconds to check brightness");
                                setBrightness((int)MathUtilities.map(amountOfTime, darkThreshold, lightThreshold, minBrightness, maxBrightness));
                            }
                            try {
                                Thread.sleep(brightnessCheck);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, null, ex);
                                break;
                            }
                        }
                    } else {
                        Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, "The selected pin {0} is not used for light level readings", ldrPinNumber);
                    }
                } else {
                    Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, "Invalid pin number or no light level readings enabled: {0}", ldrPinNumber);
                }
            });
            ldrRead.start();
        }
    }
    
    private void setupBrightnessGPIOControl(){
        if(controller!=null){
            int pin = prefs.getIntPreference("org.pidome.client.fixed.settings.backlight.gpio.out.pin", -1);

            if (pin != -1) {
                try {
                    if (!prefs.getStringPreference("org.pidome.client.fixed.settings.backlight.gpio.out.method", "pi4j").equals("BCM")) {
                        LOG.info("Brightness control using wiringpi numbered pin: " + pin);
                        /// We need to check for hardware or software pwm
                        switch (pin) {
                            case 0:
                            case 1:
                            case 23:
                            case 24:
                            case 26:
                                brightnessPin = controller.provisionPwmOutputPin(RaspiPin.getPinByName("GPIO " + String.valueOf(pin)), brightness.getValue());
                            break;
                            default:
                                Gpio.wiringPiSetup();
                                if (SoftPwm.softPwmCreate(pin, 100, 100) == 0) {
                                    LOG.info("Soft pwm setup success");
                                } else {
                                    LOG.severe("Soft pwm setup failed");
                                }
                            break;
                        }
                        brightness.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                            if(displayOn.getValue()){
                                if (brightnessPin != null) {
                                    brightnessPin.setPwm((int)(((newValue.doubleValue()>=(double)minBrightness)?newValue.doubleValue():(double)minBrightness)*10.23));
                                } else {
                                    SoftPwm.softPwmWrite(pin, (newValue.intValue()>=minBrightness)?newValue.intValue():minBrightness);
                                }
                            }
                        });
                        displayOn.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                            if(!newValue){
                                if (brightnessPin != null) {
                                    brightnessPin.setPwm(0);
                                } else {
                                    SoftPwm.softPwmWrite(pin, 0);
                                }
                            } else {
                                if (brightnessPin != null) {
                                    brightnessPin.setPwm((int)(((brightness.doubleValue()>=(double)minBrightness)?brightness.doubleValue():(double)minBrightness)*10.23));
                                } else {
                                    SoftPwm.softPwmWrite(pin, (brightness.intValue()>=minBrightness)?brightness.intValue():minBrightness);
                                }
                            }
                        });
                    } else {
                        LOG.info("Using alternative method with official Broadcom pin number scheme to write PWM to pin: " + pin);
                        switch (pin) {
                            case 1:
                            case 2:
                            case 4:
                            case 6:
                            case 9:
                            case 14:
                            case 17:
                            case 20:
                            case 25:
                            case 27:
                            case 28:
                            case 30:
                            case 34:
                            case 39:
                                Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, "The selected pin {0} is not used for settings brightness.", pin);
                            break;
                            default:
                                final Runtime rt = Runtime.getRuntime();
                                RaspiPin.getPinByName("GPIO " + String.valueOf(pin)).getAddress();
                                Process prRun = rt.exec("/usr/bin/gpio -g mode " + String.valueOf(pin) + " pwm");
                                brightness.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                                    if(displayOn.getValue()){
                                        try {
                                            Process pr = rt.exec("/usr/bin/gpio -g pwm " + String.valueOf(pin) + " " + String.valueOf((int)(((newValue.doubleValue()>=(double)minBrightness)?newValue.doubleValue():(double)minBrightness) * 10.23)));
                                        } catch (IOException ex) {
                                            Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });
                                displayOn.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                                    if(!newValue){
                                        try {
                                            Process pr = rt.exec("/usr/bin/gpio -g pwm " + String.valueOf(pin) + " 0");
                                        } catch (IOException ex) {
                                            Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    } else {
                                        try {
                                            Process pr = rt.exec("/usr/bin/gpio -g pwm " + String.valueOf(pin) + " " + String.valueOf((int)(((brightness.doubleValue()>=(double)minBrightness)?brightness.doubleValue():(double)minBrightness) * 10.23)));
                                        } catch (IOException ex) {
                                            Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                });
                            break;
                        }
                    }
                    Thread wait = new Thread(() -> {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, "No time to wait to set?", ex);
                        }
                        setBrightness(maxBrightness);
                        brightnessInitialized = true;
                    });
                    wait.start();
                } catch (IOException ex) {
                    Logger.getLogger(PhysicalDisplay.class.getName()).log(Level.SEVERE, "Initial GPIO pwm set failed", ex);
                }
            } else {
                LOG.info("No brightness control");
            }
        }
    }
    
}