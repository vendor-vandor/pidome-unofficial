/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.messengers;

/**
 *
 * @author John
 */
public class MessengerException extends Exception {

    /**
     * Creates a new instance of <code>MessengerException</code> without detail
     * message.
     */
    public MessengerException() {
    }

    /**
     * Constructs an instance of <code>MessengerException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MessengerException(String msg) {
        super(msg);
    }
}
