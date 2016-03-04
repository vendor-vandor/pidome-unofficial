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
import org.pidome.server.services.plugins.MessengerPluginService;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.UtilityPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_messengerplugineditor extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_messengerplugineditor.class);
    
    boolean render = true;
    boolean saveResult = true;
    String errorMessage = "";
    
    @Override
    public void collect(){
        if(!postDataMap.isEmpty()){
            try {
                Map<String,String> pluginOptions = new HashMap<>();
                for(String key:postDataMap.keySet()){
                    pluginOptions.put(key, postDataMap.get(key));
                }
                if(!postDataMap.containsKey("installed")){
                    MessengerPluginService.getInstance().savePlugin("PiDome messenger service",
                                                                  "The default messenger service",
                                                                  1,
                                                                  false,
                                                                  pluginOptions);
                } else {
                    MessengerPluginService.getInstance().updatePlugin(MessengerPluginService.getInstance().getPlugin(0).getPluginId(),
                                                                    "PiDome messenger service",
                                                                    "The default messenger service",
                                                                    1,
                                                                    false,
                                                                    pluginOptions);
                }
            } catch (PluginServiceException ex) {
                LOG.error("Could not start Messenger plugin: {}", ex.getMessage());
                errorMessage = "Could not start Messenger plugin: " + ex.getMessage();
                saveResult = false;
            } catch (PluginException ex) {
                LOG.error("It seems like there is a race condition due to updating a non running Messenger plugin: {}", ex.getMessage());
                errorMessage = "Could not update Messenger plugin: " + ex.getMessage();
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
