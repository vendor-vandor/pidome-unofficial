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
public interface DeviceDiscoveryBaseInterface {
    /**
     * Returns the instance name.
     * @return 
     */
    public String getName();
    /**
     * Returns the instance name.
     * @return 
     */
    public String getFriendlyName();
    /**
     * Enables discovery for a period of time, one shot, scan or indefinitely.
     * @param period 
     * @throws org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException 
     */
    public void enableDiscovery(int period) throws TimedDiscoveryException;
    /**
     * Disables discovery/scanning.
     * @param period 
     * @throws org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException 
     */
    public void disableDiscovery() throws TimedDiscoveryException;
    /**
     * Returns the amount of discovery time set.
     * @return
     * @throws TimedDiscoveryException 
     */
    public int getDiscoveryTime() throws TimedDiscoveryException;
    
    /**
     * Called when discovery is enabled so drivers can act on it.
     */
    public void discoveryEnabled();
    
    /**
     * Called when discovery is disabled so drivers can act on it.
     */
    public void discoveryDisabled();
    
    /**
     * Returns the amount of discovery time set.
     * @return
     */
    public int getDiscoveredAmount();
    
    /**
     * Returns if discovery is currently running.
     * @return 
     */
    public boolean discoveryIsEnabled();
    
}
