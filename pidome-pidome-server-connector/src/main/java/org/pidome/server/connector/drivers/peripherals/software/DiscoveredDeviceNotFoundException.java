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
public class DiscoveredDeviceNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>DiscoveredDeviceNotFoundException</code>
     * without detail message.
     */
    public DiscoveredDeviceNotFoundException() {
    }

    /**
     * Constructs an instance of <code>DiscoveredDeviceNotFoundException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DiscoveredDeviceNotFoundException(String msg) {
        super(msg);
    }
}
