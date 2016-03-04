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
public class NonFatalPluginException extends PluginException {

    /**
     * Creates a new instance of <code>NonFatalPluginException</code> without
     * detail message.
     */
    public NonFatalPluginException() {
        super();
    }

    /**
     * Constructs an instance of <code>NonFatalPluginException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public NonFatalPluginException(String msg) {
        super(msg);
    }
}
