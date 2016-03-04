/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 * Exception used with device control errors.
 * @author John
 */
public class DeviceControlException extends Exception {

    /**
     * Creates a new instance of <code>DeviceControlException</code> without
     * detail message.
     */
    public DeviceControlException() {
    }

    /**
     * Constructs an instance of <code>DeviceControlException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceControlException(String msg) {
        super(msg);
    }
}
