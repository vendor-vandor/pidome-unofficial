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
public class PeripheralSoftwareException extends Exception {

    /**
     * Creates a new instance of <code>PeripheralSoftwareException</code>
     * without detail message.
     */
    public PeripheralSoftwareException() {
    }

    /**
     * Constructs an instance of <code>PeripheralSoftwareException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PeripheralSoftwareException(String msg) {
        super(msg);
    }
}
