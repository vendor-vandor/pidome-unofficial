/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.network.sockets;

/**
 *
 * @author John
 */
public class SocketBaseException extends Exception {

    /**
     * Creates a new instance of <code>SocketBaseException</code> without detail
     * message.
     */
    public SocketBaseException() {
    }

    /**
     * Constructs an instance of <code>SocketBaseException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SocketBaseException(String msg) {
        super(msg);
    }
}
