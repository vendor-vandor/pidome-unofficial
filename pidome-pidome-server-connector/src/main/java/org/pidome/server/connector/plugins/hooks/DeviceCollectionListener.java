/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

/**
 *
 * @author John
 */
public interface DeviceCollectionListener {
    
    /**
     * Handles device data. To be used for a plugin to implement.
     * @param deviceId
     * @param deviceCommand
     * @param deviceData 
     * @param isUserAction 
     */
    public void handleDeviceCollectionValue(int deviceId, String deviceCommand, Object deviceData, final boolean isUserAction);
    
}
