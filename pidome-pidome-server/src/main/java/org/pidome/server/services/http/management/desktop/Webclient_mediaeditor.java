/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.system.location.BaseLocations;
import org.pidome.server.system.location.LocationServiceException;
import org.pidome.server.services.http.Webservice_renderer;

/**
 * Editor for 
 * @author John Sirach
 */
public class Webclient_mediaeditor extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_mediaeditor.class);
    
    boolean render = true;
    boolean saveResult = true;
    String errorMessage = "";
    
    @Override
    public void collect(){
        try {
            Map<String,Object> pageData = new HashMap<>();
            List<Map<String,Object>>locations = BaseLocations.getLocations();
            if(!getDataMap.isEmpty() && getDataMap.containsKey("remove_id")){
                render = false;
                try {
                    MediaPluginService.getInstance().deletePlugin(Integer.parseInt(getDataMap.get("remove_id")));
                } catch (PluginServiceException ex) {
                    LOG.error("Could not delete plugin: {}", ex.getMessage());
                    errorMessage = "Could not delete plugin: " + ex.getMessage();
                    saveResult = false;
                }
            } else if(!getDataMap.isEmpty() && getDataMap.containsKey("media_id") && !postDataMap.isEmpty() && postDataMap.containsKey("media_id")){
                render = false;
                try {
                    Map<String,String> pluginOptions = new HashMap<>();
                    for(String key:postDataMap.keySet()){
                        if(key.startsWith("mediaOption_")){
                            String[] splitted = key.split("_");
                            pluginOptions.put(splitted[1], postDataMap.get(key));
                        }
                    }
                    MediaPluginService.getInstance().updatePlugin(Integer.parseInt(postDataMap.get("media_id")),
                            postDataMap.get("plugin_name"),
                            postDataMap.get("plugin_desc"),
                            Integer.parseInt(postDataMap.get("plugin_location")), 
                            postDataMap.get("plugin_fav").equals("true"),
                            pluginOptions);
                } catch (PluginServiceException ex) {
                    LOG.error("Could not update plugin: {}", ex.getMessage());
                    errorMessage = "Could not update plugin: " + ex.getMessage();
                    saveResult = false;
                }
            } else if(!getDataMap.isEmpty() && getDataMap.containsKey("installed_id") && !postDataMap.isEmpty() && postDataMap.containsKey("installed_id")){
                render = false;
                try {
                    Map<String,String> pluginOptions = new HashMap<>();
                    for(String key:postDataMap.keySet()){
                        if(key.startsWith("mediaOption_")){
                            String[] splitted = key.split("_");
                            pluginOptions.put(splitted[1], postDataMap.get(key));
                        }
                    }
                    MediaPluginService.getInstance().savePlugin(postDataMap.get("plugin_name"),
                                                                postDataMap.get("plugin_desc"),
                                                                Integer.parseInt(postDataMap.get("plugin_location")), 
                                                                postDataMap.get("plugin_fav").equals("true"),
                                                                pluginOptions,
                                                                Integer.parseInt(getDataMap.get("installed_id")));
                } catch (PluginServiceException ex) {
                    LOG.error("Could not save new plugin: {}", ex.getMessage());
                    errorMessage = "Could not save new plugin: " + ex.getMessage();
                    saveResult = false;
                }
            } else {
                if(!getDataMap.isEmpty() && getDataMap.containsKey("installed_id")){
                    try {
                        pageData.put("plugin", MediaPluginService.getInstance().getBareboneInstance(Integer.parseInt(getDataMap.get("installed_id"))));
                        pageData.put("installed_id", getDataMap.get("installed_id"));
                    } catch (PluginException ex) {
                        LOG.error("Could not load barebone instance: {}", getDataMap.get("installed_id"));
                    }
                } else if(!getDataMap.isEmpty() && getDataMap.containsKey("media_id")){
                    pageData.put("plugin", MediaPluginService.getInstance().getPlugin(Integer.parseInt(getDataMap.get("media_id"))));
                    pageData.put("media_id", getDataMap.get("media_id"));
                } else {
                    Map<Integer,Map<String,Object>> origData = MediaPluginService.getInstance().getInstalledPlugins();
                    Map<String,Map<String,Object>> mediaData = new HashMap<>();
                    for(int key:origData.keySet()){
                        mediaData.put(String.valueOf(key), origData.get(key));
                    }
                    pageData.put("pluginlist", mediaData);
                }
                pageData.put("locations", locations);
                setData(pageData);
            }
        } catch (LocationServiceException ex) {
            LOG.error("Locations not evailable: {}", ex.getMessage());
            errorMessage = "Locations not evailable: " + ex.getMessage();
            saveResult = false;
        }
    }

    @Override
    public final String render() throws Exception {
        if (render==true){
            return super.render();
        } else {
            if(saveResult == false){
                return "{ \"result\" : { \"exec\":false, \"reason\":\""+errorMessage+"\" } }";
            } else {
                return "{ \"result\" : { \"exec\":true } }";               
            }
        }
    }
    
}