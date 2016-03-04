/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.weatherplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.pidome.server.connector.plugins.freeform.FreeformPlugin;

/**
 * Base for weather plugins.
 * @author John
 */
public abstract class WeatherPlugin extends FreeformPlugin {
    
    /**
     * Weather plugin listeners.
     */
    ArrayList<WeatherPluginListener> _listeners = new ArrayList<>();
    
    /**
     * Data owner url.
     */
    String ownerUrl = "";
    /**
     * Data owner name.
     */
    String ownerName = "";
    
    /**
     * Plugin's capabilities.
     */
    public enum Capabilities {
        SEARCH_LOCATION_LATLON,
        SEARCH_LOCATION_NAME,
        CURRENT_WEATHER,
        UPCOMING_FORECAST,
        THREEHOURS_FORECAST,
        THREEDAY_FORECAST,
        FIVEDAY_FORECAST
    }
    
    /**
     * Constructor.
     * Sets the capabilities.
     * @param capabilities 
     */
    public WeatherPlugin(Capabilities[] capabilities){
        if(this.capabilities.isEmpty()){
            this.capabilities.addAll(Arrays.asList(capabilities));
        }
    }
    
    /**
     * Current data.
     */
    private CurrentWeatherData currentData = new CurrentWeatherData();
    /**
     * 
     */
    private UpcomingWeatherForecast upcomingData = new UpcomingWeatherForecast();
    /**
     * Three hours data.
     */
    private ThreeHoursWeatherForecast threeHoursData = new ThreeHoursWeatherForecast();
    /**
     * Three days data.
     */
    private ThreeDayWeatherForecast threeDayData = new ThreeDayWeatherForecast();
    /**
     * Five days data.
     */
    private FiveDayWeatherForecast fiveDayData = new FiveDayWeatherForecast();
    
    /**
     * Location name.
     */
    private String locationName = "";
    
    /**
     * Holds a list of capabilities.
     */
    private final ArrayList<Capabilities> capabilities = new ArrayList<>();
    
    /**
     * Gets the capabilities. 
     * @return  
     */
    public final ArrayList<Capabilities> getCapabilities(){
        return this.capabilities;
    }
    
    /**
     * Sets current weather data.
     * @param data 
     */
    public final void setCurrentWeatherData(CurrentWeatherData data){
        currentData = data;
        broadcastNewCurrent(data);
    }
    
    /**
     * Returns the current weather data.
     * @return 
     */
    public abstract CurrentWeatherData getCurrentWeatherData();
    /**
     * When not using a private implementation return this current data.
     * @return 
     */
    public final CurrentWeatherData getCurrentWeatherDataInternal(){
        return this.currentData;
    }
    
    /**
     * Sets the upcoming forecast data.
     * Use this if there is no logical date possible
     * @param forecast 
     */
    public final void setUpcomingForecast(UpcomingWeatherForecast forecast){
        this.upcomingData = forecast;
    }
    /**
     * Returns an upcoming forecast.
     * @return 
     */
    public abstract UpcomingWeatherForecast getUpcomingForecast();
    /**
     * When not using a private implementation return this upcoming forecast.
     * @return 
     */
    public final UpcomingWeatherForecast getUpcomingForecastInternal(){
        return this.upcomingData;
    }
    
    /**
     * Sets the three hours forecast data.
     * @param forecast 
     */
    public final void setThreeHoursForecast(ThreeHoursWeatherForecast forecast){
        this.threeHoursData = forecast;
    }
    /**
     * Returns a three hours forecast.
     * @return 
     */
    public abstract ThreeHoursWeatherForecast getThreeHoursForecast();
    /**
     * When not using a private implementation return this three hours forecast.
     * @return 
     */
    public final ThreeHoursWeatherForecast getThreeHoursForecastInternal(){
        return this.threeHoursData;
    }
    
    /**
     * Sets the three days forecast data.
     * @param forecast 
     */
    public final void setThreeDaysForecast(ThreeDayWeatherForecast forecast){
        this.threeDayData = forecast;
    }
    /**
     * Returns a three day forecast.
     * @return 
     */
    public abstract ThreeDayWeatherForecast getThreeDayWeatherForecast();
    /**
     * When not using a private implementation return this three days forecast.
     * @return 
     */
    public final ThreeDayWeatherForecast getThreeDayWeatherForecastInternal(){
        return this.threeDayData;
    }
    
    /**
     * Returns a five day forecast.
     * @return 
     */
    public abstract FiveDayWeatherForecast getFiveDayWeatherForecast();
    /**
     * When not using a private implementation return this five days forecast.
     * @return 
     */
    public final FiveDayWeatherForecast getFiveDayWeatherForecastInternal(){
        return this.fiveDayData;
    }
    
    /**
     * Sets the current location name.
     * @param name 
     */
    public void setLocationName(String name){
        this.locationName = name;
    }
    
    /**
     * Returns the location name.
     * @return 
     */
    public abstract String getLocationName();
    /**
     * When not using a private implementation return this location name.
     * @return 
     */
    public final String getLocationNameInternal(){
        return this.locationName;
    }
    
    /**
     * Broadcasts current value change to listeners. 
     * @param data
     */
    public final void broadcastNewCurrent(CurrentWeatherData data){
        Runnable broadcast = () -> {
            Iterator<WeatherPluginListener> listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                listeners.next().handleNewCurrentWeather(this, data);
            }
        };
        broadcast.run();
    }
    
    /**
     * Sets the owner of the data.
     * Use this function to set the data copyright reference if required by the remote party.
     * P.S. Just always use it to be nice.
     * @param url
     * @param name 
     */
    public final void setDataOwner(String url, String name){
        this.ownerUrl = url;
        this.ownerName = name;
    }
    
    public final String getDataOwnerName(){
        return this.ownerName;
    }
    
    /**
     * Returns the data owner's url.
     * @return 
     */
    public final String getDataOwnerUrl(){
        return this.ownerUrl;
    }
    
    /**
     * Adds a listener
     * @param listener 
     */
    public final void addListener(WeatherPluginListener listener){
        if(!_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener
     * @param listener 
     */
    public final void removeListener(WeatherPluginListener listener){
        _listeners.remove(listener);
    }
    
}
