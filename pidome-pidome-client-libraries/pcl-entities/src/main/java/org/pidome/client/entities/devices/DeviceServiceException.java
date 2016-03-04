/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

/**
 * Thrown when errors occur in the device service.
 * @author John
 */
public class DeviceServiceException extends Exception {

    /**
     * Creates a new instance of <code>DeviceServiceException</code> without
     * detail message.
     */
    public DeviceServiceException() {
    }

    /**
     * Constructs an instance of <code>DeviceServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceServiceException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>DeviceServiceException</code> with
     * the specified detail message.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public DeviceServiceException(String msg, Exception exc) {
        super(msg, exc);
    }
    
}
