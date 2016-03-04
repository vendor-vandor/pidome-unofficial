/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;

/**
 *
 * @author John
 */
public interface DeviceHookListener {
    
    /**
     * Handles device data. To be used for a plugin to implement. 
     * @param device
     * @param group
     * @param control
     * @param deviceControl
     * @param deviceValue
     */
    public void handleDeviceData(final Device device, final String group, final String control, final DeviceControl deviceControl, final Object deviceValue);
    
}
