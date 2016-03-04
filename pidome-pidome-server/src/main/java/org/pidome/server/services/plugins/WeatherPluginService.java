/*
 * Copyright 2015 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pidome.server.services.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.weatherplugin.CurrentWeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherData.Icon;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPlugin;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPluginListener;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.plugins.PluginsDB;

/**
 *
 * @author John
 */
public class WeatherPluginService extends PluginService implements WeatherPluginListener {

    private final int definedPluginId = 6;
    private final int definedPluginTypeId = 6;
    
    private List<WeatherStatusListener> _listeners = new ArrayList<>();
    
    private static WeatherPluginService me;
    
    /**
     * Constructor.
     */
    protected WeatherPluginService(){
        if(me!=null){
            me = this;
        }
    }
    
    /**
     * Returns instance.
     * @return 
     */
    public static WeatherPluginService getInstance(){
        if(me==null){
            me = new WeatherPluginService();
        }
        return me;
    }
    
    @Override
    public int getInstalledId() {
        return definedPluginId;
    }

    @Override
    public int getPluginTypeId() {
        return definedPluginTypeId;
    }

    /**
     * Adds a weather status listener.
     * @param listener 
     */
    public final void addListener(WeatherStatusListener listener){
        if(!this._listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener.
     * @param listener 
     */
    public final void removeListener(WeatherStatusListener listener){
        _listeners.remove(listener);
    }
    
    /**
     * Returns the device plugins known and if active including object.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getPlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            pluginCollection.get(key).put("id", key);
            if(pluginsList.containsKey(key)){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (WeatherPlugin)pluginsList.get(key));
            } else {
                pluginCollection.get(key).put("active", false);
                pluginCollection.get(key).put("pluginObject", null);
            }
        }
        return pluginCollection;
    }
    
    /**
     * Return a list of base value states.
     * The returned list is depending on the used plugin.
     * @return 
     */
    public final List<String> getWeatherBaseStates(){
        List<String> baseValues = new ArrayList<>();
        for(Icon icon:WeatherData.Icon.values()){
            baseValues.add(icon.getBaseValue());
        }
        return baseValues;
    }
    
    /**
     * Returns only the active weather plugins known.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getActivePlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key) && pluginsList.get(key).getRunning()){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (WeatherPlugin)pluginsList.get(key));
            } else {
                pluginCollection.remove(key);
            }
        }
        return pluginCollection;
    }
    
    @Override
    public WeatherPlugin getPlugin(int pluginId) throws PluginException {
        if(pluginsList.containsKey(pluginId)) return (WeatherPlugin)pluginsList.get(pluginId);
        throw new PluginException("Plugin not found/active");
    }

    @Override
    void startPluginHandlers(int pluginId) {
        ((WeatherPlugin)pluginsList.get(pluginId)).addListener(me);
    }

    @Override
    void stopHandlers(int pluginId) {
        ((WeatherPlugin)pluginsList.get(pluginId)).removeListener(me);
    }


    @Override
    public String getServiceName() {
        return "Weather plugins service";
    }

    @Override
    public void handleNewCurrentWeather(WeatherPlugin plugin, CurrentWeatherData data) {
        ClientMessenger.send("WeatherService","getCurrentWeather", plugin.getPluginLocationId(),new HashMap<String,Object>(){{
                put("id", plugin.getPluginId());
                put("weatherdate", data.getWeatherDate());
                put("location", plugin.getLocationName());
                put("text", data.getStateName());
                put("icon", data.getStateIcon());
                put("iconimage", new StringBuilder().append(data.getStateIcon().getValue()).append(".png").toString());
                put("temperature", data.getTemperature());
                put("humidity", data.getHumidity());
                put("pressure", data.getPressure());
                put("windspeed", data.getWindSpeed());
                put("winddirection", data.getWindDirection());
                put("winddirectiondegrees", data.getWindDirectionDegrees());
            }}
        );
    }
    
    /**
     * Weather status listener for internal listeners.
     * Used by the automation rules.
     */
    public static interface WeatherStatusListener {
        public void handleNewWeatherStatus(CurrentWeatherData data);
    }
    
}