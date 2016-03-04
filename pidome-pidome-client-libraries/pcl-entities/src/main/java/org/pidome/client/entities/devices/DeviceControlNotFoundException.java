/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 * Thrown when a control is requested which does not exist.
 * @author John
 */
public class DeviceControlNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>DeviceControlNotFoundException</code>
     * without detail message.
     */
    public DeviceControlNotFoundException() {
    }

    /**
     * Constructs an instance of <code>DeviceControlNotFoundException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceControlNotFoundException(String msg) {
        super(msg);
    }
}
