/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.peripherals.software;

/**
 *
 * @author John
 */
public interface DiscoveredDeviceListener {
    
    /**
     * Handle a newly discovered device.
     * Do not try to remove a device using this handle.
     * @param driver
     * @param device 
     */
    public void handleNewDiscoveredDevice(DeviceDiscoveryBaseInterface driver, DiscoveredDevice device);
    
    /**
     * Broadcasting enabled discovery.
     * @param driver 
     * @param period 
     */
    public void deviceDiscoveryEnabled(DeviceDiscoveryBaseInterface driver, int period);
    
    /**
     * Broadcasting disabled discovery.
     * @param driver 
     */
    public void deviceDiscoveryDisabled(DeviceDiscoveryBaseInterface driver);
    
}
