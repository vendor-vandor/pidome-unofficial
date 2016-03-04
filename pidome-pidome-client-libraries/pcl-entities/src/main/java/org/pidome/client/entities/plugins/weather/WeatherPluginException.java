/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.weather;

/**
 * Exception used when there is an issue with the weather plugin.
 * @author John
 */
public class WeatherPluginException extends Exception {

    /**
     * Creates a new instance of <code>WeatherPluginException</code> without
     * detail message.
     */
    public WeatherPluginException() {
    }

    /**
     * Constructs an instance of <code>WeatherPluginException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public WeatherPluginException(String msg) {
        super(msg);
    }
}
