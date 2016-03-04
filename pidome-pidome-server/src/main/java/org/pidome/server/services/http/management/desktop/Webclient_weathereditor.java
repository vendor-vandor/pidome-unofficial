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
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherPlugin;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.WeatherPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_weathereditor extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_weathereditor.class);
    
    boolean render = true;
    boolean saveResult = true;
    String errorMessage = "";
    
    @Override
    public void collect(){
        if(!postDataMap.isEmpty()){
            try {
                Map<String,String> pluginOptions = new HashMap<>();
                for(String key:postDataMap.keySet()){
                    if(key.startsWith("weatherOption_")){
                        String[] splitted = key.split("_");
                        pluginOptions.put(splitted[1], postDataMap.get(key));
                    }
                }
                if(!postDataMap.containsKey("pluginid") && postDataMap.containsKey("installedid")){
                    Map<String,Object> set = WeatherPluginService.getInstance().getPluginBase(Integer.parseInt(postDataMap.get("installedid")));
                    WeatherPluginService.getInstance().savePlugin((String)set.get("name"),
                                                                  (String)set.get("name"),
                                                                  1,
                                                                  false,
                                                                  pluginOptions,
                                                                  Integer.parseInt(postDataMap.get("installedid")));
                } else {
                    WeatherPlugin plugin = WeatherPluginService.getInstance().getPlugin(Integer.parseInt(postDataMap.get("pluginid")));
                    WeatherPluginService.getInstance().updatePlugin(Integer.parseInt(postDataMap.get("pluginid")),
                                                                    plugin.getPluginName(),
                                                                    plugin.getPluginName(),
                                                                    1,
                                                                    false,
                                                                    pluginOptions);
                }
            } catch (PluginServiceException ex) {
                LOG.error("Could not start Weather plugin: {}", ex.getMessage());
                errorMessage = "Could not start Weather plugin: " + ex.getMessage();
                saveResult = false;
            } catch (PluginException ex) {
                LOG.error("It seems like there is a race condition due to updating a non running Weather plugin: {}", ex.getMessage());
                errorMessage = "Could not update Weather plugin: " + ex.getMessage();
                saveResult = false;
            }
        }
    }
    
    @Override
    public final String render() throws Exception {
        if(saveResult == false){
            return "{ \"result\" : { \"exec\":false, \"reason\":\""+errorMessage+"\" } }";
        } else {
            return "{ \"result\" : { \"exec\":true } }";               
        }
    }
    
}
