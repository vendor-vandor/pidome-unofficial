/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

import org.pidome.server.connector.plugins.PluginBase;

/**
 *
 * @author John
 */
public interface DeviceDeliverHookListener {
    
    /**
     * Handles device data. To be used for a plugin to implement.
     * @param plugin
     * @param deviceId
     * @param group
     * @param set
     * @param deviceData 
     */
    public void handleHookDeviceDeliverData(PluginBase plugin, int deviceId, String group, String set, Object deviceData);
    
}
