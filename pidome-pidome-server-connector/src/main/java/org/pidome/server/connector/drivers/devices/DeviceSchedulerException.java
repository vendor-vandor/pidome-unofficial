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
public class DeviceSchedulerException extends Exception {

    /**
     * Creates a new instance of <code>DeviceSchedulerException</code> without
     * detail message.
     */
    public DeviceSchedulerException() {
    }

    /**
     * Constructs an instance of <code>DeviceSchedulerException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceSchedulerException(String msg) {
        super(msg);
    }
}
