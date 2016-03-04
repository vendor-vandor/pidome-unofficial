/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.media;

/**
 *
 * @author John
 */
public class MediaPluginException extends Exception {

    /**
     * Creates a new instance of <code>MediaPluginException</code> without
     * detail message.
     */
    public MediaPluginException() {
    }

    /**
     * Constructs an instance of <code>MediaPluginException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MediaPluginException(String msg) {
        super(msg);
    }
}
