/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.bareboneArduinoDevice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;

/**
 *
 * @author John
 */
public class BareboneArduinoDevice extends Device {
    
    static Logger LOG = LogManager.getLogger(BareboneArduinoDevice.class);
    
    public BareboneArduinoDevice(){}


    /**
     * @inheritDoc
     */
    @Override
    public void handleData(String data, Object object) {
        LOG.trace("Got data: {}", data);
        DeviceNotification notification = new DeviceNotification();
        notification.addData("JkuDgMaUwX", "OUkhJWy7s1", data);
        dispatchToHost(notification);
    }

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) {
        /// Device almost removed.
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}