/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.media;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class MediaException extends Exception {

    /**
     * Creates a new instance of <code>MediaException</code> without detail
     * message.
     */
    public MediaException() {
    }

    /**
     * Constructs an instance of <code>MediaException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public MediaException(String msg) {
        super(msg);
    }
}
