/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.devices.pidomeMQTTBrokerDevice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class PidomeMQTTBrokerDevice extends Device {
    
    static Logger LOG = LogManager.getLogger(PidomeMQTTBrokerDevice.class);
    
    public PidomeMQTTBrokerDevice(){}

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        dispatchToDriver(command.getGroupId(), command.getControlId(), command.getCommandValueData().toString());
    }

    @Override
    public void handleData(String data, Object object) {
        /// Not used handled in plugin.
    }   

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported"); //To change body of generated methods, choose Tools | Templates.
    }
    
}
