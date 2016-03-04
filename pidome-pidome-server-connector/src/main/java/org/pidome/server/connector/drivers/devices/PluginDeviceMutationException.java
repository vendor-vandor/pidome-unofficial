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
public class PluginDeviceMutationException extends Exception {

    /**
     * Creates a new instance of <code>PluginDeviceMutationException</code>
     * without detail message.
     */
    public PluginDeviceMutationException() {
    }

    /**
     * Constructs an instance of <code>PluginDeviceMutationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PluginDeviceMutationException(String msg) {
        super(msg);
    }
}
