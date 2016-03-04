/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.peripherals.hardware;

/**
 * Proxy data between software and hardware driver.
 * @author John
 */
public class PeripheralHardwareDriverDataProxy {
    
    private PeripheralHardwareDriver driver;
    
    protected void setHardwareProxy(PeripheralHardwareDriver driver){
        this.driver = driver;
    }
    
}