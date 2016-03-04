/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.nativeZWaveDriver.nativeZWaveDevice;

import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class NativeZWaveDevice extends Device {

    public NativeZWaveDevice(){}
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        /// All is handled by the driver to keep consistency.
        this.dispatchToDriver(command);
    }

    @Override
    public void handleData(String data, Object object) {
        /// First the actions
    }

    @Override
    public void shutdownDevice() {
        /// bye bye
    }

    @Override
    public void startupDevice() {
        // Hey hey
    }
    
}