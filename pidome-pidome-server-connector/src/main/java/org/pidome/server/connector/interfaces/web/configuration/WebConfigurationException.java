/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.configuration;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class WebConfigurationException extends Exception {

    /**
     * Creates a new instance of <code>PluginConfigurationException</code>
     * without detail message.
     */
    public WebConfigurationException() {
    }

    /**
     * Constructs an instance of <code>PluginConfigurationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public WebConfigurationException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>PluginConfigurationException</code> with
     * the specified detail throwable.
     *
     * @param ex the throwable.
     */
    public WebConfigurationException(Throwable ex) {
        super(ex);
    }
    
}
