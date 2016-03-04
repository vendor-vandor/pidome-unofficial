/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.presencedevices;

/**
 *
 * @author John
 */
public class TokenLoadException extends Exception {

    /**
     * Creates a new instance of <code>TokenLoadException</code> without detail
     * message.
     */
    public TokenLoadException() {
    }

    /**
     * Constructs an instance of <code>TokenLoadException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TokenLoadException(String msg) {
        super(msg);
    }
}
