/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.pidomeremote;

/**
 *
 * @author John
 */
public class PiDomeRemoteButtonException extends Exception {

    /**
     * Creates a new instance of <code>PiDomeRemoteButtonException</code>
     * without detail message.
     */
    public PiDomeRemoteButtonException() {
    }

    /**
     * Constructs an instance of <code>PiDomeRemoteButtonException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PiDomeRemoteButtonException(String msg) {
        super(msg);
    }
}
