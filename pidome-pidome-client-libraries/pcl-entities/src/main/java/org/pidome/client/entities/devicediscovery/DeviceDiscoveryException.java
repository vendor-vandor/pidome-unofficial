/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devicediscovery;

/**
 *
 * @author John
 */
public class DeviceDiscoveryException extends Exception {

    /**
     * Creates a new instance of <code>DeviceDiscoveryException</code> without
     * detail message.
     */
    public DeviceDiscoveryException() {
    }

    /**
     * Constructs an instance of <code>DeviceDiscoveryException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceDiscoveryException(String msg) {
        super(msg);
    }
    /**
     * Constructs an instance of <code>DeviceDiscoveryException</code> with the
     * specified throwable.
     *
     * @param ex a throwable.
     */
    public DeviceDiscoveryException(Throwable ex) {
        super(ex);
    }
    
}
