/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 * Thrown when a device group is not found.
 * @author John
 */
public class DeviceGroupNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>DeviceGroupNotFoundException</code>
     * without detail message.
     */
    public DeviceGroupNotFoundException() {
    }

    /**
     * Constructs an instance of <code>DeviceGroupNotFoundException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceGroupNotFoundException(String msg) {
        super(msg);
    }
}
