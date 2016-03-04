/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 * Exception used with failing control commands.
 * @author John
 */
public class DeviceControlCommandException extends Exception {

    /**
     * Creates a new instance of <code>DeviceControlCommandException</code>
     * without detail message.
     */
    public DeviceControlCommandException() {
    }

    /**
     * Constructs an instance of <code>DeviceControlCommandException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceControlCommandException(String msg) {
        super(msg);
    }
}
