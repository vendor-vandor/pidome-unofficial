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
public class NotAFloatDataTypeException extends Exception {

    /**
     * Creates a new instance of <code>NotAFloatDataTypeException</code> without
     * detail message.
     */
    public NotAFloatDataTypeException() {
    }

    /**
     * Constructs an instance of <code>NotAFloatDataTypeException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public NotAFloatDataTypeException(String msg) {
        super(msg);
    }
}
