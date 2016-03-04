/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.connection;

/**
 * Exception used when there is a connection listener exception.
 * @author John
 */
public class ServerConnectionListenerException extends Exception {

    /**
     * Creates a new instance of <code>ConnectionListenerException</code>
     * without detail message.
     */
    public ServerConnectionListenerException() {
    }

    /**
     * Constructs an instance of <code>ConnectionListenerException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ServerConnectionListenerException(String msg) {
        super(msg);
    }
}
