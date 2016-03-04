/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.plugindata;

/**
 *
 * @author John
 */
public class PluginDataException extends Exception {

    /**
     * Creates a new instance of <code>PluginSettingException</code> without
     * detail message.
     */
    public PluginDataException() {
    }

    /**
     * Constructs an instance of <code>PluginSettingException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PluginDataException(String msg) {
        super(msg);
    }
}
