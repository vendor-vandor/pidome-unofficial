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

import java.util.List;
import org.pidome.server.connector.plugins.PluginException;

/**
 *
 * @author John
 */
public interface WeatherServiceJSONRPCWrapperInterface {
    
    /**
     * Returns available plugins.
     * @return 
     */
    public Object getPlugins();
    
    /**
     * Returns the current weather data for the current plugin.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    public Object getCurrentWeather(Number pluginId) throws PluginException;
    
    /**
     * Returns a non logical upcoming forecast where dates can be unexpected.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    public Object getUpcomingForecast(Number pluginId) throws PluginException;
    
    /**
     * Returns a three hours forecast.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    public Object getThreeHoursForecast(Number pluginId) throws PluginException;
    
    /**
     * Get three days forecast
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    public Object getThreeDaysForecast(Number pluginId) throws PluginException;
    
    /**
     * Removes a weather plugin from being used.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    @PiDomeJSONRPCPrivileged
    public Object removePlugin(Long pluginId) throws PluginException;
 
    /**
     * Returns a list of known weather states possible.
     * This list returns the full list, it depends on the plugin if a particular state is supported.
     * @return 
     */
    public List<String>getKnownWeatherStates();
    
}
