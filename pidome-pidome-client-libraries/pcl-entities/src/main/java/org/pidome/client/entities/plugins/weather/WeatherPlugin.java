/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.weather;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 * The main weather plugin entry.
 * @author John
 */
public class WeatherPlugin {

    static {
        Logger.getLogger(WeatherPlugin.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * City name of weather.
     */
    private StringPropertyBindingBean cityName = new StringPropertyBindingBean();
    /**
     * Supplier name of the weather data.
     */
    private StringPropertyBindingBean supplierName = new StringPropertyBindingBean();
    
    /**
     * Server connection.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Forecast capabilities.
     */
    public enum Capabilities {
        /**
         * Current weather
         */
        CURRENT_WEATHER,
        /*
        * Three hours forecast
        */
        THREEHOURS_FORECAST,
        /**
         * A three day forecast.
         */
        THREEDAY_FORECAST,
        /**
         * Well, there is a forecast, but how far and which?
         */
        UPCOMING_FORECAST
    }
    
    /**
     * List of plugin capabilities.
     */
    private final ArrayList<Capabilities> capabilities = new ArrayList<>();
    
    /**
     * Id of the plugin.
     */
    private int pluginId;
    
    /**
     * Bindable current weather data bean
     */
    ObjectPropertyBindingBean<WeatherData> currentData = new ObjectPropertyBindingBean();
    
    /**
     * Observable list of upcoming weather data.
     */
    ObservableArrayListBean<WeatherData> nearWeatherData = new ObservableArrayListBean();
    /**
     * REad only observable list of upcoming weather data.
     */
    ReadOnlyObservableArrayListBean<WeatherData> readOnlyNearWeatherData = new ReadOnlyObservableArrayListBean<>(nearWeatherData);
    
    /**
     * Observable list of upcoming weather data in days.
     */
    ObservableArrayListBean<WeatherData> distancedWeatherData = new ObservableArrayListBean();
    /**
     * Read only observable list of upcoming weather data in days.
     */
    ReadOnlyObservableArrayListBean<WeatherData> readOnlyDistancedWeatherData = new ReadOnlyObservableArrayListBean<>(distancedWeatherData);
    
    /**
     * Unset all data.
     */
    protected final void unset(){
        currentData.setValue(new WeatherData());
        nearWeatherData.clear();
        distancedWeatherData.clear();
    }
    
    /**
     * Constructor.
     * Set's id and capabilities.
     * @param connection The server connection
     * @throws org.pidome.client.entities.plugins.weather.WeatherPluginException When the plugin can not be constructed.
     */
    protected WeatherPlugin(PCCConnectionInterface connection) throws WeatherPluginException {
        this.connection = connection;
    }
    
    /**
     * Sets plugin details
     * @param pluginId The plugin id as known on the server.
     * @param capabilities List of possible plugin capabilities, refer to RPC spec.
     * @throws org.pidome.client.entities.plugins.weather.WeatherPluginException When data is incomplete.
     */
    protected final void setPluginData(int pluginId, ArrayList<String> capabilities) throws WeatherPluginException {
        this.pluginId = pluginId;
        if(capabilities.isEmpty()){
            throw new WeatherPluginException("No capabilities present");
        }
        for(String cap:capabilities){
            switch(cap){
                case "CURRENT_WEATHER":
                    this.capabilities.add(Capabilities.CURRENT_WEATHER);
                break;
                case "THREEHOURS_FORECAST":
                    this.capabilities.add(Capabilities.THREEHOURS_FORECAST);
                break;
                case "THREEDAY_FORECAST":
                    this.capabilities.add(Capabilities.THREEDAY_FORECAST);
                break;
                case "UPCOMING_FORECAST":
                    this.capabilities.add(Capabilities.UPCOMING_FORECAST);
                break;
            }
        }
        update();
    }
    
    /**
     * Returns city name.
     * @return The weather city name.
     */
    public final ReadOnlyStringPropertyBindingBean getCityName(){
        return this.cityName.getReadOnlyBooleanPropertyBindingBean();
    }

    /**
     * Returns supplier name.
     * Some suppliers require this.
     * @return The weather suppliers name.
     */
    public final ReadOnlyStringPropertyBindingBean getSupplierName(){
        return this.supplierName.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the current weather object property.
     * @return a bindable object containing current weather data.
     */
    public final ReadOnlyObjectPropertyBindingBean<WeatherData> getCurrentWeatherDataProperty(){
        return currentData.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns a list of the upcoming weather.
     * @return Read only observable list with weather data
     */
    public final ReadOnlyObservableArrayListBean<WeatherData> getUpcomingWeatherData(){
        return this.readOnlyNearWeatherData;
    }
    
    /**
     * Returns a list of the upcoming weather in days.
     * @return Read only observable list with daily weather data
     */
    public final ReadOnlyObservableArrayListBean<WeatherData> getUpcomingDaysWeatherData(){
        return this.readOnlyDistancedWeatherData;
    }
    
    /**
     * Sets the current weather data.
     * @throws WeatherPluginException When new weather data can not be set.
     */
    public final void update() throws WeatherPluginException {
        if(!capabilities.isEmpty()){
            try {
                if(capabilities.contains(Capabilities.CURRENT_WEATHER)) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", pluginId);
                    Map<String,Object> data = (Map<String,Object>)this.connection.getJsonHTTPRPC("WeatherService.getCurrentWeather", params, "WeatherService.getCurrentWeather").getResult().get("data");
                    cityName.setValue((String)data.get("location"));
                    supplierName.setValue((String)data.get("ownername"));
                    updateWeatherData(data);
                } else {
                    throw new WeatherPluginException("Plugin does not support current weather data");
                }
            } catch (PCCEntityDataHandlerException ex) {
                Logger.getLogger(WeatherPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(this.capabilities.contains(Capabilities.UPCOMING_FORECAST)){
                try {
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", pluginId);
                    ArrayList<Map<String,Object>> dataSet = (ArrayList<Map<String,Object>>)this.connection.getJsonHTTPRPC("WeatherService.getUpcomingForecast", params, "WeatherService.getUpcomingForecast").getResult().get("data");
                    updateNearForecastWeatherData(dataSet);
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(WeatherPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(this.capabilities.contains(Capabilities.THREEDAY_FORECAST)){
                try {
                    Map<String, Object> params = new HashMap<>();
                    params.put("id", pluginId);
                    ArrayList<Map<String,Object>> dataSet = (ArrayList<Map<String,Object>>)this.connection.getJsonHTTPRPC("WeatherService.getThreeDaysForecast", params, "WeatherService.getThreeDaysForecast").getResult().get("data");
                    updateDistancedForecastWeatherData(dataSet);
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(WeatherPlugin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    /**
     * Updates the upcoming weather data.
     * @param dataSet refer to RPC spec for full data map.
     */
    protected void updateNearForecastWeatherData(ArrayList<Map<String,Object>> dataSet){
        nearWeatherData.clear();
        List<WeatherData> arrData = new ArrayList<>();
        for(Map<String,Object> data:dataSet){
            arrData.add(composeWeatherData(data));
        }
        nearWeatherData.addAll(arrData);
    }
    
    /**
     * Updates the daily weather data.
     * @param dataSet refer to RPC spec for full data map.
     */
    protected void updateDistancedForecastWeatherData(ArrayList<Map<String,Object>> dataSet){
        distancedWeatherData.clear();
        List<WeatherData> arrData = new ArrayList<>();
        for(Map<String,Object> data:dataSet){
            arrData.add(composeWeatherData(data));
        }
        distancedWeatherData.addAll(arrData);
    }
    
    /**
     * Updates the current weather data.
     * @param data refer to RPC spec for full data map.
     */
    protected void updateWeatherData(Map<String,Object> data){
        currentData.setValue(composeWeatherData(data));
    }
    
    /**
     * Creates a weatherdata object.
     * @param data JSON RPC promised weather data map
     * @return WeatherData object
     */
    private WeatherData composeWeatherData(Map<String,Object> data){
        WeatherData current = new WeatherData();
        current.setTemperature(((Number)data.get("temperature")).floatValue());
        current.setHumidity(((Number)data.get("humidity")).floatValue());
        current.setWindSpeed(((Number)data.get("windspeed")).floatValue());
        current.setIcon((String)data.get("icon"));
        current.setIconImage((String)data.get("iconimage"));
        current.setDescription((String)data.get("text"));
        current.setPressure(((Number)data.get("pressure")).floatValue());
        current.setWindDirection((String)data.get("winddirection"));
        current.setWindDirectionDegrees(((Number)data.get("winddirectiondegrees")).floatValue());
        current.setWeatherExtremes((String)data.get("extremes"));
        current.setWeatherDate(((Number)data.get("weatherdate")).intValue());
        return current;
    }
    
    /**
     * Returns plugin id.
     * @return the plugin id.
     */
    public final int getPluginId(){
        return pluginId;
    }
    
    /**
     * Retursn the plugin capabilities.
     * These are used to show forecast models.
     * @return the capabilities the plugin has. It helps with constructing forecast tables.
     */
    public final ArrayList<Capabilities> getCapabilities(){
        return this.capabilities;
    }
    
}