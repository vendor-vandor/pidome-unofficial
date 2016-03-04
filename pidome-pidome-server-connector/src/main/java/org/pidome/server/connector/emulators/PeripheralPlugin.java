/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.emulators;

import java.util.Map;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationInterface;

/**
 *
 * @author John
 */
public interface PeripheralPlugin {
    
    /**
     * Handles data from a device.
     * @param device
     * @param group
     * @param set
     * @param data 
     * @param userIntent 
     */
    public void handleDeviceData(Device device, String group, String set, byte[] data, boolean userIntent);

    /**
     * Handles data from a device.
     * @param device
     * @param request
     */
    public void handleDeviceData(Device device, DeviceCommandRequest request);
    
    /**
     * Notification that a device has been removed and which one.
     * @param device 
     */
    public void deviceRemoved(Device device);
    
    /**
     * Notification that a device has been added and which one.
     * @param device 
     */
    public void deviceAdded(Device device);
    
    /**
     * Assignes a set of installed device id's for the plugin.
     * With this function a set of installed id's is given to the plugin. These
     * installed id's are a mappaing from driver name to id.
     * @param installedDevices 
     */
    public void assignDeviceInstalledIds(Map<String,Integer> installedDevices);
    /**
     * Removes the installed id assignments.
     */
    public void removeAssignDeviceInstalledIds();
    /**
     * Constructs a link between the device service and the plugin.
     * @param deviceServiceLink 
     */
    public void setDeviceServiceLink(PluginDeviceMutationInterface deviceServiceLink);
    /**
     * Removes the device service link.
     */
    public void removeDeviceServiceLink();
    
    /**
     * Indicator a device is loaded and which device.
     * @param device 
     */
    public void deviceLoaded(Device device);
    
}
