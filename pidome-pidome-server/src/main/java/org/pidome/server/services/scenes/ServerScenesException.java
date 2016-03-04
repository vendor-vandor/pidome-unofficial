/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.scenes;

/**
 *
 * @author John
 */
public class ServerScenesException extends Exception {

    /**
     * Creates a new instance of <code>ServerScenesException</code> without
     * detail message.
     */
    public ServerScenesException() {
    }

    /**
     * Constructs an instance of <code>ServerScenesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ServerScenesException(String msg) {
        super(msg);
    }
}
