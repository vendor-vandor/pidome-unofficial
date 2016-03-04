/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.client.data;

/**
 *
 * @author John
 */
public class ClientSessionException extends Exception {

    /**
     * Creates a new instance of <code>ClientSessionException</code> without
     * detail message.
     */
    public ClientSessionException() {
    }

    /**
     * Constructs an instance of <code>ClientSessionException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ClientSessionException(String msg) {
        super(msg);
    }
}
