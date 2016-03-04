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
public class TimedDiscoveryException extends Exception {

    /**
     * Creates a new instance of <code>TimedDiscoveryException</code> without
     * detail message.
     */
    public TimedDiscoveryException() {
    }

    /**
     * Constructs an instance of <code>TimedDiscoveryException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TimedDiscoveryException(String msg) {
        super(msg);
    }
}
