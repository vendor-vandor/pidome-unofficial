/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.emulators;

import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 *
 * @author John
 */
public interface HardwareInterfaceEmulator {
    
    /**
     * All drivers have an unique driver id string. This should return the expected id.
     * @return 
     */
    public String getExpectedDriverId();
    
    /**
     * Returns the drivers expected version number to use.
     * @return 
     */
    public String getExpectedDriverVersion();
    
    /**
     * Returns the bound hardware device.
     * @return 
     */
    public Peripheral getHardwareDevice();
    
    /**
     * Returns the software driver link.
     * This is needed to fulfill emulation tasks for example device discovery etc..
     * @return 
     */
    public PeripheralSoftwareDriverInterface getSoftwareDriverLink();
    
    /**
     * Sets the hardware device to be bound.
     * @param device
     */
    public void setHardwareDevice(Peripheral device);
    
}
