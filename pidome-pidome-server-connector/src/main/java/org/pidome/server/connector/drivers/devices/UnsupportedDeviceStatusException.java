/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices;

/**
 *
 * @author John
 */
public class UnsupportedDeviceStatusException extends Exception {

    /**
     * Creates a new instance of <code>UnsupportedDeviceStatusException</code>
     * without detail message.
     */
    public UnsupportedDeviceStatusException() {
    }

    /**
     * Constructs an instance of <code>UnsupportedDeviceStatusException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnsupportedDeviceStatusException(String msg) {
        super(msg);
    }
}