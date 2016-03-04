/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.peripherals.software;

/**
 *
 * @author John
 */
public class PeripheralDriverDeviceMutationException extends Exception {

    /**
     * Creates a new instance of
     * <code>PeripheralDriverDeviceMutationException</code> without detail
     * message.
     */
    public PeripheralDriverDeviceMutationException() {
    }

    /**
     * Constructs an instance of
     * <code>PeripheralDriverDeviceMutationException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public PeripheralDriverDeviceMutationException(String msg) {
        super(msg);
    }
}
