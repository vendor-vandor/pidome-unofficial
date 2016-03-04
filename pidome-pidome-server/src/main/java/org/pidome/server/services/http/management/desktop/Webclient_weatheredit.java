/*
 * Copyright 2014 John.
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

package org.pidome.server.services.http.management.desktop;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPlugin;
import org.pidome.server.services.plugins.WeatherPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_weatheredit  extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_weatheredit.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Set weather plugin configuration");
        pageData.put("pluginsalive", false);
        try {
            try {
                
                Map<Integer, PluginBase> plugins = WeatherPluginService.getInstance().getRunningPlugins();
                if(plugins.isEmpty()) throw new PluginException("Plugin not loaded");
                
                pageData.put("pluginsalive", true);
                
                WeatherPlugin plugin = WeatherPluginService.getInstance().getPlugin(plugins.keySet().iterator().next());
                pageData.put("pluginid", plugin.getPluginId());
                pageData.put("plugin", plugin);
            } catch (PluginException ex){
                LOG.debug("No plugin running, error (if in error): {}", ex.getMessage(), ex);
                if(getDataMap.containsKey("installed_id")){
                    pageData.put("plugin", WeatherPluginService.getInstance().getBareboneInstance(Integer.parseInt(getDataMap.get("installed_id"))));
                    pageData.put("installed_id", getDataMap.get("installed_id"));
                } else {
                    Map<String,Map<String,Object>> newSet = new HashMap<>();
                    for(Map.Entry<Integer,Map<String,Object>> walkSet:WeatherPluginService.getInstance().getInstalledPluginsList().entrySet()){
                        newSet.put(String.valueOf(walkSet.getKey()), walkSet.getValue());
                    }
                    LOG.info("Got set: {}", newSet);
                    pageData.put("plugindataset", newSet);
                }
            }
        } catch (PluginException ex) {
            LOG.error("Coult not load utility plugin: {}", ex.getMessage());
        }
        setData(pageData);
    }
}