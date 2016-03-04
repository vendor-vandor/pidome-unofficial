/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.weatherplugin;

/**
 *
 * @author John
 */
public class ForecastWeatherDataException extends Exception {

    /**
     * Creates a new instance of <code>ForecastWeatherDataException</code>
     * without detail message.
     */
    public ForecastWeatherDataException() {
    }

    /**
     * Constructs an instance of <code>ForecastWeatherDataException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ForecastWeatherDataException(String msg) {
        super(msg);
    }
}
