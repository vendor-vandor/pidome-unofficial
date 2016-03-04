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
public class DeviceControlsGroupException extends Exception {

    /**
     * Creates a new instance of <code>DeviceControlsGroupException</code>
     * without detail message.
     */
    public DeviceControlsGroupException() {
    }

    /**
     * Constructs an instance of <code>DeviceControlsGroupException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceControlsGroupException(String msg) {
        super(msg);
    }
}
