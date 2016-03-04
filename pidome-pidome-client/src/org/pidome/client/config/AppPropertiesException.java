/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.config;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class AppPropertiesException extends Exception {

    /**
     * Creates a new instance of <code>AppPropertiesException</code> without
     * detail message.
     */
    public AppPropertiesException() {
    }

    /**
     * Constructs an instance of <code>AppPropertiesException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public AppPropertiesException(String msg) {
        super(msg);
    }
}
