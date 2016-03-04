/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.i2cLtsLc;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.DeviceScheduler;
import org.pidome.server.connector.drivers.devices.DeviceSchedulerException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControl;
import org.pidome.server.connector.tools.ColorImpl;
import org.pidome.server.connector.tools.MiscImpl;

/**
 *
 * @author John Sirach
 */
public class I2cLtsLc extends Device implements DeviceScheduler {

    static Logger LOG = LogManager.getLogger(I2cLtsLc.class);
    
    Map<String,Map<String,String>>cmdList = new HashMap<>();
    
    float temp = -10000;
    
    boolean tmpToLight = false;
    
    boolean devicePower = false;
    
    int r = 0;
    int g = 0;
    int b = 0;
    
    public I2cLtsLc(){}

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getControlType()){
            case COLORPICKER:
                if(command.hasExtra()){
                    String buttonPressed = command.getExtraValue();
                    Map<String,Object> colorMap = ((DeviceColorPickerControl)command.getControl()).getFullColorMap().get("rgb");
                    r = (int)colorMap.get("r");
                    g = (int)colorMap.get("g");
                    b = (int)colorMap.get("b");
                    switch(buttonPressed){
                        case "n":  // Set the color directly for whole strip
                            if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x16:"+r+","+g+","+b);
                        break;
                        case "c":  // Set the color using the fade function
                            if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x20:"+r+","+g+","+b);
                        break;
                        case "l":  // Set the color from left to right
                            if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x24:"+r+","+g+","+b);
                        break;
                        case "r":  // Set the color from right to left
                            if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x28:"+r+","+g+","+b);
                        break;
                        case "x":  // Set the color using random fading
                            if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x32:"+r+","+g+","+b);
                        break;
                    }
                } else {
                    throw new UnsupportedDeviceCommandException("Color picker command net setup correctly");
                }
            break;
            case SLIDER:
                if(command.getControlId().equals("t")) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x45:" + command.getCommandValue());
            break;
            case TOGGLE:
                switch(command.getControlId()){
                    case "switch":
                        if(((String)command.getCommandValue()).equals("y")){
                            dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x41:1");
                            devicePower=true;
                        } else {
                            devicePower=false;
                            dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x43:1");
                        }
                    break;
                    case "flTemp":
                        if(((String)command.getCommandValue()).equals("ttlOn")){
                            if(temp>-9999){
                                double[] rgb = ColorImpl.tempToRgb(String.valueOf(temp));
                                if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x20:"+(int)rgb[0]+","+(int)rgb[1]+","+(int)rgb[2]);
                            }
                            tmpToLight=true;
                        } else {
                            if (devicePower==true) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x20:"+r+","+g+","+b);
                            tmpToLight=false;
                        }
                    break;
                }
            break;
            case BUTTON:
                if(((String)command.getCommandValue()).equals("s")) dispatchToDriver(command.getGroupId(), command.getControlId(), ":WRITE:0x49:1");
            break;
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleData(String input, Object object) {
        /// We are using the object for the data. This object is a byte array with "Command/address" specific data
        /// without any standardisation in it.
        byte[] byteArray = (byte[])object;
        NumberFormat formatter;
        try {
            String[] data = input.split(":");
            DeviceNotification notification = new DeviceNotification();
            switch(data[1]){
                case "0x01": /// temp reading
                    temp = MiscImpl.byteArrayToFloat(byteArray);
                    formatter = new DecimalFormat("##0.00;-#0.00"); /// range for the tmp36 temp sensor is -40 to +125
                    if(tmpToLight==true && devicePower==true){
                        double[] rgb = ColorImpl.tempToRgb(String.valueOf(temp));
                        dispatchToDriver("moodcolor", "rgbselect", ":WRITE:0x20:"+(int)rgb[0]+","+(int)rgb[1]+","+(int)rgb[2]+"");
                    }
                    notification.addData("values", data[1], Float.valueOf(formatter.format(temp)));
                break;
                case "0x06": ///Lux reading
                    float lux = MiscImpl.byteArrayToFloat(byteArray);
                    formatter = new DecimalFormat("##0.####"); 
                    notification.addData("values",data[1], Float.valueOf(formatter.format(lux)));
                break;
                case "0x11": /// retrieve the current device state
                    Integer rC = (byteArray[0] & 0xff);
                    Integer gC = (byteArray[1] & 0xff);
                    Integer bC = (byteArray[2] & 0xff);
                    Integer speed = (byteArray[3] & 0xff);
                    if(rC!=0 | gC!=0 | bC!=0){
                        devicePower=true;
                        notification.addData("moodcolor", "rgbselect", ColorImpl.RGBToHex(rC+","+gC+","+bC));
                        notification.addData("actions", "switch", "y");
                        notification.addData("actions", "flTemp", "ttlOff");
                    }
                    notification.addData("actions", "t", speed);
                break;
                default:
                    throw new Exception();
            }
            dispatchToHost(notification);
        } catch (IndexOutOfBoundsException ex){
            LOG.error("Illegal return type");
        } catch (Exception ex){
            LOG.warn("Unsupported return set: {}", ex.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setScheduledItems() {
        LOG.debug("Scheduling sensor readings");
        try {
            this.scheduleItem(() -> {
                dispatchToDriver("values", "0x06", ":READ:0x06:4");/// LDR reading
                dispatchToDriver("values", "0x01", ":READ:0x01:4");/// Temp reading
            }, 1, TimeUnit.MINUTES);
        } catch (DeviceSchedulerException ex) {
            LOG.error("Server updater not scheduled: {}", ex.getMessage());
        }
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }
    
}