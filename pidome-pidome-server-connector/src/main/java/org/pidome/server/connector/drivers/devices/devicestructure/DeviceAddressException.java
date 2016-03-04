/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure;

/**
 *
 * @author John
 */
public class DeviceAddressException extends Exception {

    /**
     * Creates a new instance of <code>DeviceAddressException</code> without
     * detail message.
     */
    public DeviceAddressException() {
    }

    /**
     * Constructs an instance of <code>DeviceAddressException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceAddressException(String msg) {
        super(msg);
    }
    /**
     * Constructs an instance of <code>DeviceAddressException</code> with the
     * specified throwable.
     *
     * @param ex the rethrown trhowable.
     */
    public DeviceAddressException(Throwable ex) {
        super(ex);
    }
    
}
