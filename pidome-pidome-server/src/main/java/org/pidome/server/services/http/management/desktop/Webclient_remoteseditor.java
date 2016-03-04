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
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.RemotesPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_remoteseditor extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_remoteseditor.class);
    
    boolean render = true;
    boolean saveResult = true;
    String errorMessage = "";
    
    @Override
    public void collect(){
        if(!postDataMap.isEmpty()){
            render = false;
            try {
                Map<String,String> pluginOptions = new HashMap<>();
                for(String key:postDataMap.keySet()){
                    if(key.startsWith("remoteOption_")){
                        String[] splitted = key.split("_");
                        pluginOptions.put(splitted[1], postDataMap.get(key));
                    }
                }
                if(!postDataMap.containsKey("remote_id")){
                    RemotesPluginService.getInstance().savePlugin(postDataMap.get("plugin_name"),
                                                                  postDataMap.get("plugin_desc"),
                                                                  1,
                                                                  Boolean.parseBoolean(postDataMap.get("plugin_fav")),
                                                                  pluginOptions);
                } else {
                    RemotesPluginService.getInstance().updatePlugin(Integer.parseInt(postDataMap.get("remote_id")),
                                                                    postDataMap.get("plugin_name"),
                                                                    postDataMap.get("plugin_desc"),
                                                                    1,
                                                                    Boolean.parseBoolean(postDataMap.get("plugin_fav")),
                                                                    pluginOptions);
                }
            } catch (PluginServiceException ex) {
                LOG.error("Could not (re)start remote plugin: {}", ex.getMessage());
                errorMessage = "Could not (re)start remote plugin: " + ex.getMessage();
                saveResult = false;
            }
        } else {
            Map<String,Object> pageData = new HashMap<>();
            if(!getDataMap.isEmpty() && getDataMap.containsKey("installed_id")){
                try {
                    pageData.put("plugin", RemotesPluginService.getInstance().getBareboneInstance());
                    pageData.put("installed_id", getDataMap.get("installed_id"));
                } catch (PluginException ex) {
                    LOG.error("Could not load barebone instance: {}", getDataMap.get("installed_id"));
                }
            } else if(!getDataMap.isEmpty() && getDataMap.containsKey("remote_id")){
                try {
                    pageData.put("plugin", RemotesPluginService.getInstance().getPlugin(Integer.parseInt(getDataMap.get("remote_id"))));
                    pageData.put("remote_id", getDataMap.get("remote_id"));
                } catch (PluginException ex) {
                    LOG.error("Could not get remote id '{}': {}", getDataMap.get("remote_id"), ex.getMessage());
                }
            } else {
                Map<Integer,Map<String,Object>> origData = RemotesPluginService.getInstance().getInstalledPlugins();
                Map<String,Map<String,Object>> remotesData = new HashMap<>();
                origData.keySet().stream().forEach((key) -> {
                    remotesData.put(String.valueOf(key), origData.get(key));
                });
                pageData.put("pluginlist", remotesData);
            }
            setData(pageData);
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
