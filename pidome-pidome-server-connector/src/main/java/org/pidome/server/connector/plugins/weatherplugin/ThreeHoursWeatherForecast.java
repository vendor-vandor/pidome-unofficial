/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.weatherplugin;

import java.util.ArrayList;

/**
 *
 * @author John
 */
public class ThreeHoursWeatherForecast extends ForecastWeatherData {

    public ThreeHoursWeatherForecast() {
        super(3);
    }

    @Override
    public ArrayList<WeatherData> getForecastCollection() {
        return this.list;
    }
    
}
