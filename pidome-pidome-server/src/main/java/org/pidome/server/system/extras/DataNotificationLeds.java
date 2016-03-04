/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.system.extras;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.software.DataNotificationListener;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;

/**
 *
 * @author John
 */
public class DataNotificationLeds implements DataNotificationListener {
    
    static GpioController gpio;  
    static GpioPinDigitalOutput notifRcvPin;  
    static GpioPinDigitalOutput notifSndPin;  
    
    static boolean supportsPins = false;
    static boolean supportsPinsTested = false;
    
    ExecutorService service;
    
    static Logger LOG = LogManager.getLogger(DataNotificationLeds.class);
    
    protected final void setDataNotificationLeds(boolean support){
        if(support==true && supportsPinsTested==false && System.getProperty("user.name").equals("root")){
            supportsPinsTested = true;
            try {
                gpio = GpioFactory.getInstance();
                notifRcvPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "rcvLED", PinState.LOW);
                notifSndPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "sndED", PinState.LOW);
                supportsPins = true;
                service = Executors.newSingleThreadExecutor();
                PeripheralSoftwareDriver.addDataNotificationListener(this);
                LOG.info("Led notification pins are enabled.");
            } catch (Throwable ex) {
                LOG.info("Led notification pins are unsupported because of an error: {}", ex.getMessage(), ex);
            }
        } else if (supportsPinsTested==false) {
            LOG.info("Led notification pins are disabled. If you want to enable this got to http://pidome.org on how to enable this.");
            supportsPinsTested = true;
        }
    }
    
    /**
     * Blink send led.
     */
    @Override
    public final synchronized void notifyLedSnd(){
        if (supportsPins) service.submit(() -> { notifSndPin.pulse(5); });
    }
    
    /**
     * Blink receive led.
     */
    @Override
    public final synchronized void notifyLedRcv(){
        if (supportsPins) service.submit(() -> { notifRcvPin.pulse(5); });
    }
    
}
