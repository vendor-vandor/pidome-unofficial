/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.peripherals.software;

import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;

/**
 *
 * @author John
 */
public interface DeviceDiscoveryScanInterface extends WebPresentAddExistingDeviceInterface {
    
    /**
     * Scan for new devices.
     * @return 
     */
    public boolean scanForNewDevices();
    
    /**
     * Scanning has been requested to be stopped.
     * @return 
     */
    public boolean stopScanForNewDevices();
    
}
