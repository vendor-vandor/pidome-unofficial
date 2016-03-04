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
public class DeviceDiscoveryServiceException extends Exception {

    /**
     * Creates a new instance of <code>DeviceDiscoveryServiceException</code>
     * without detail message.
     */
    public DeviceDiscoveryServiceException() {
    }

    /**
     * Constructs an instance of <code>DeviceDiscoveryServiceException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceDiscoveryServiceException(String msg) {
        super(msg);
    }
}
