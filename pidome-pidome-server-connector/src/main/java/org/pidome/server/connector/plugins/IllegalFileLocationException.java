/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins;

/**
 *
 * @author John
 */
public class IllegalFileLocationException extends Exception {

    /**
     * Creates a new instance of <code>IllegalFileLocationException</code>
     * without detail message.
     */
    public IllegalFileLocationException() {
    }

    /**
     * Constructs an instance of <code>IllegalFileLocationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public IllegalFileLocationException(String msg) {
        super(msg);
    }
}
