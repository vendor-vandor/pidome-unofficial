/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.emulators;

import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;

/**
 *
 * @author John
 */
public interface PluginPeripheral {
    
    /**
     * Used when data is received from the plugin.
     * @param data 
     */
    public void handlePluginData(byte[] data);
    
    /**
     * Creates a link with the plugin.
     * @param plugin 
     */
    public void setPluginLink(PeripheralPlugin plugin);
    
    /**
     * Clears the plugin link.
     */
    public void removePluginLink();
    
    /**
     * Used for dispatching data to a plugin.
     * @param device
     * @param group
     * @param set
     * @param data 
     */
    public void dispatchPluginData(Device device, String group, String set, byte[] data, boolean userIntent);
    
    /**
     * Enable absolute device commands to passed to plugins.
     * @param device
     * @param request 
     */
    public void dispatchPluginData(Device device, DeviceCommandRequest request);
    
    /**
     * Returns the link to the plugin bound to the emulator.
     * @return 
     */
    public PeripheralPlugin getPluginLink();
    
    /**
     * Device loaded indicator
     * @param device 
     */
    public void deviceLoaded(Device device);
    
}