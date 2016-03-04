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
package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.weatherplugin.CurrentWeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPlugin;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.WeatherPluginService;
import static org.pidome.server.services.http.rpc.AbstractRPCMethodExecutor.LOG;

/**
 *
 * @author John
 */
public class WeatherServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements WeatherServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getPlugins", null);
                put("getCurrentWeather", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getUpcomingForecast", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getThreeHoursForecast", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getThreeDaysForecast", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("removePlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getKnownWeatherStates", null);
            }
        };
        return mapping;
    }

    /**
     * Returns plugin list.
     * @return 
     */
    @Override
    public Object getPlugins() {
        Map<Integer,Map<String,Object>> pluginsList = WeatherPluginService.getInstance().getPlugins();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(int key: pluginsList.keySet()){
            Map<String,Object> item = new HashMap<>();
            item.put("id", key);
            item.put("name", pluginsList.get(key).get("name"));
            item.put("description", pluginsList.get(key).get("description"));
            item.put("locationid", pluginsList.get(key).get("locationid"));
            item.put("locationname", pluginsList.get(key).get("location"));
            item.put("pluginname", pluginsList.get(key).get("pluginname"));
            try {
                item.put("active", WeatherPluginService.getInstance().getPlugin(key).getRunning());
                item.put("capabilities", WeatherPluginService.getInstance().getPlugin(key).getCapabilities());
            } catch (Exception ex){
                item.put("active", false);
            }
            newList.add(item);
        }
        return newList;
    }
    
    /**
     * Returns current weather.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    @Override
    public Object getCurrentWeather(Number pluginId) throws PluginException {
        WeatherPlugin plugin = WeatherPluginService.getInstance().getPlugin(pluginId.intValue());
        CurrentWeatherData currentWeatherData = plugin.getCurrentWeatherData();
        return createWeatherDataObject(plugin, currentWeatherData);
    }

    /**
     * Returns three hours forecast.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    @Override
    public Object getUpcomingForecast(Number pluginId) throws PluginException {
        WeatherPlugin plugin = WeatherPluginService.getInstance().getPlugin(pluginId.intValue());
        List<Map<String,Object>> returnData = new ArrayList<>();
        plugin.getUpcomingForecast().getForecastCollection().stream().forEach((data) -> {
            returnData.add(createWeatherDataObject(plugin, data));
        });
        return returnData;
    }
    
    /**
     * Returns three hours forecast.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    @Override
    public Object getThreeHoursForecast(Number pluginId) throws PluginException {
        WeatherPlugin plugin = WeatherPluginService.getInstance().getPlugin(pluginId.intValue());
        List<Map<String,Object>> returnData = new ArrayList<>();
        plugin.getThreeHoursForecast().getForecastCollection().stream().forEach((data) -> {
            returnData.add(createWeatherDataObject(plugin, data));
        });
        return returnData;
    }
    
    /**
     * Get three days forecast
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    @Override
    public Object getThreeDaysForecast(Number pluginId) throws PluginException {
        WeatherPlugin plugin = WeatherPluginService.getInstance().getPlugin(pluginId.intValue());
        List<Map<String,Object>> returnData = new ArrayList<>();
        plugin.getThreeDayWeatherForecast().getForecastCollection().stream().forEach((data) -> {
            returnData.add(createWeatherDataObject(plugin, data));
        });
        return returnData;
    }
    
    /**
     * Creates a single weather object.
     * @param plugin
     * @param providedWeatherData
     * @return 
     */
    private Map<String,Object> createWeatherDataObject(WeatherPlugin plugin, WeatherData providedWeatherData){
        Map<String,Object> weatherData = new HashMap<>();
        weatherData.put("location", plugin.getLocationName());
        weatherData.put("weatherdate", providedWeatherData.getWeatherDate());
        weatherData.put("text", providedWeatherData.getStateName());
        weatherData.put("icon", providedWeatherData.getStateIcon());
        weatherData.put("iconimage", new StringBuilder().append(providedWeatherData.getStateIcon().getValue()).append(".png").toString());
        weatherData.put("temperature", providedWeatherData.getTemperature());
        weatherData.put("humidity", providedWeatherData.getHumidity());
        weatherData.put("pressure", providedWeatherData.getPressure());
        weatherData.put("windspeed", providedWeatherData.getWindSpeed());
        weatherData.put("winddirection", providedWeatherData.getWindDirection());
        weatherData.put("winddirectiondegrees", providedWeatherData.getWindDirectionDegrees());
        weatherData.put("extremes", providedWeatherData.getExtremesDescription());
        weatherData.put("ownername", plugin.getDataOwnerName());
        weatherData.put("ownerurl", plugin.getDataOwnerUrl());
        return weatherData;
    }
    
    /**
     * Removes a weather plugin from being used.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    @Override
    public Object removePlugin(Long pluginId) throws PluginException {
        try {
            return WeatherPluginService.getInstance().deletePlugin(pluginId.intValue());
        } catch (PluginServiceException ex) {
            LOG.error("Could not stop using plugin {}, {}", pluginId, ex.getMessage(), ex);
            throw new PluginException("Could not stop using plugin");
        }
    }

    /**
     * Returns a list of known weather states possible.
     * This list returns the full list, it depends on the plugin if a particular state is supported.
     * @return 
     */
    @Override
    public List<String> getKnownWeatherStates() {
        return WeatherPluginService.getInstance().getWeatherBaseStates();
    }
    
}
