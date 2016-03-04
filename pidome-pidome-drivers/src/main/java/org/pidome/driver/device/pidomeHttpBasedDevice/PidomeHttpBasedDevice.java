/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeHttpBasedDevice;

import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 * Devices using http requests.
 * @author John
 */
public class PidomeHttpBasedDevice extends Device {

    /**
     * Constructor.
     * Does not set listening option.
     */
    public PidomeHttpBasedDevice(){
        super();
    }
    
    /**
     * Handles the device command
     * Currently only BUTTON and TOGGLE are supported.
     * @param command
     * @throws UnsupportedDeviceCommandException 
     */
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getControlType()){
            case BUTTON:
            case TOGGLE:
                dispatchToDriver(command.getGroupId(), command.getControlId(), (String)command.getCommandValueData());
            break;
            default:
                throw new UnsupportedDeviceCommandException("Control type " + command.getControlType() + " is unsupported in this device");
        }
    }
    
    /**
     * Handles data.
     * Not used.
     * @param data
     * @param object 
     */
    @Override
    public void handleData(String data, Object object) {
        /////
    }

    /**
     * Not used.
     */
    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Not used.
     */
    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}