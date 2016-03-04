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
public class IllegalDeviceActionException extends Exception {

    /**
     * Creates a new instance of <code>IllegalDeviceActionException</code>
     * without detail message.
     */
    public IllegalDeviceActionException() {
    }

    /**
     * Constructs an instance of <code>IllegalDeviceActionException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalDeviceActionException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>IllegalDeviceActionException</code> with the
     * specified throwable.
     *
     * @param ex the rethrown trhowable.
     */
    public IllegalDeviceActionException(Throwable ex) {
        super(ex);
    }
}
