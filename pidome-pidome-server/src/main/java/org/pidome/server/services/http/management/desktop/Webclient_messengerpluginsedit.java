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
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.plugins.messengers.sms.SMSMessengerBase;
import org.pidome.server.services.plugins.MessengerPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_messengerpluginsedit extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_messengerpluginsedit.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Set utility measurments configuration");
        try {
            try {
                Map<Integer,Map<String,Object>> plugins = MessengerPluginService.getInstance().getActivePlugins();
                if(plugins.isEmpty()) throw new PluginException("Plugin not loaded");
                
                SMSMessengerBase plugin = MessengerPluginService.getInstance().getPlugin(plugins.keySet().iterator().next());
                pageData.put("installed", plugin.getPluginId());
                pageData.put("plugin", plugin);
                Map<String,String> pluginSet = new HashMap<>();
                
                
                try {
                    for(WebConfigurationOptionSet set: plugin.getConfiguration().getOptions()){
                        set.getOptions().stream().forEach((option) -> {
                            pluginSet.put(option.getId(), option.getValue());
                        });
                    }
                } catch (WebConfigurationException ex) {
                    LOG.error("Could not load options", ex);
                }
                pageData.put("deviceSet", pluginSet);
                
                
            } catch (PluginException ex){
                LOG.debug("No plugin running, error (if in error): {}", ex.getMessage(), ex);
                pageData.put("plugin", MessengerPluginService.getInstance().getBareboneInstance());
            }
        } catch (PluginException ex) {
            LOG.error("Coult not load utility plugin: {}", ex.getMessage());
        }
        setData(pageData);
    }
}
