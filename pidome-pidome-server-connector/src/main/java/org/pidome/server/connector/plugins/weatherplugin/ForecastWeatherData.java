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
public abstract class ForecastWeatherData {
    
    /**
     * List holding the data.
     */
    protected ArrayList<WeatherData> list;
    
    /**
     * Amount of max numbers in the list.
     */
    private final int dataSize;
    
    /**
     * Constructor.
     * Sets forecast size.
     * @param dataSize 
     */
    protected ForecastWeatherData(int dataSize){
        list = new ArrayList(dataSize);
        this.dataSize = dataSize;
    }
    
    /**
     * Returns the forecast collection.
     * @return 
     */
    public abstract ArrayList<WeatherData> getForecastCollection();
    
    /**
     * Sets the forecast collection.
     * @param data
     * @throws ForecastWeatherDataException 
     */
    public final void setForecastCollection(ArrayList<WeatherData> data) throws ForecastWeatherDataException {
        if (data.size()>dataSize){
            throw new ForecastWeatherDataException("Too much items in list");
        } else {
            list.clear();
            list.addAll(data);
        }
    }
    
    /**
     * Adds forecast data.
     * @param data
     * @throws ForecastWeatherDataException 
     */
    public final void addToForecastCollection(WeatherData data) throws ForecastWeatherDataException {
        if(this.list.size()==dataSize){
            throw new ForecastWeatherDataException("Already at maximum size");
        } else {
            this.list.add(data);
        }
    }
    
}
