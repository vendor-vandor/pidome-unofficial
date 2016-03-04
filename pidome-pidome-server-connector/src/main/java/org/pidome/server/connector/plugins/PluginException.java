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
public class PluginException extends Exception {

    /**
     * Creates a new instance of <code>PluginException</code> without detail
     * message.
     */
    public PluginException() {
    }

    /**
     * Constructs an instance of <code>PluginException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public PluginException(String msg) {
        super(msg);
    }
}
