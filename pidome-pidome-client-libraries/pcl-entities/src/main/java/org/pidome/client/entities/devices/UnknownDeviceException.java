/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 * Thrown when an unknown device is requested.
 * @author John
 */
public class UnknownDeviceException extends Exception {

    /**
     * Creates a new instance of <code>UnknownDeviceException</code> without
     * detail message.
     */
    public UnknownDeviceException() {
    }

    /**
     * Constructs an instance of <code>UnknownDeviceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownDeviceException(String msg) {
        super(msg);
    }
}
