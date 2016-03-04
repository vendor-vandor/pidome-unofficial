/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.plugins;

/**
 *
 * @author John
 */
public class PidomeMediaPluginException extends Exception {

    /**
     * Creates a new instance of <code>PidomeMediaPluginException</code> without
     * detail message.
     */
    public PidomeMediaPluginException() {
    }

    /**
     * Constructs an instance of <code>PidomeMediaPluginException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PidomeMediaPluginException(String msg) {
        super(msg);
    }
}
