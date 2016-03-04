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
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.services.plugins.DevicePluginService;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public class Webclient_pluginDeviceSettingsRenderer extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_pluginDeviceSettingsRenderer.class);

    boolean render = true;
    boolean saveResult = true;
    String errorMessage = "";

    @Override
    public void collect() {
        Map<String, Object> pageData = new HashMap<>();
        if (!getDataMap.isEmpty() && getDataMap.containsKey("remove_id")) {
            render = false;
            try {
                DevicePluginService.getInstance().deletePlugin(Integer.parseInt(getDataMap.get("remove_id")));
            } catch (PluginServiceException ex) {
                LOG.error("Could not delete plugin: {}", ex.getMessage());
                errorMessage = "Could not delete plugin: " + ex.getMessage();
                saveResult = false;
            }
        } else if (!getDataMap.isEmpty() && getDataMap.containsKey("plugin_id") && !postDataMap.isEmpty() && postDataMap.containsKey("plugin_id")) {
            render = false;
            try {
                Map<String, String> pluginOptions = new HashMap<>();
                for (String key : postDataMap.keySet()) {
                    if (key.startsWith("pluginOption_")) {
                        String[] splitted = key.split("_");
                        pluginOptions.put(splitted[1], postDataMap.get(key));
                    }
                }
                DevicePluginService.getInstance().updatePlugin(Integer.parseInt(postDataMap.get("plugin_id")),
                        postDataMap.get("plugin_name"),
                        postDataMap.get("plugin_desc"),
                        1,
                        false,
                        pluginOptions);
            } catch (PluginServiceException ex) {
                LOG.error("Could not update plugin: {}", ex.getMessage());
                errorMessage = "Could not update plugin: " + ex.getMessage();
                saveResult = false;
            }
        } else if (!getDataMap.isEmpty() && getDataMap.containsKey("installed_id") && !postDataMap.isEmpty() && postDataMap.containsKey("installed_id")) {
            render = false;
            try {
                Map<String, String> pluginOptions = new HashMap<>();
                for (String key : postDataMap.keySet()) {
                    if (key.startsWith("pluginOption_")) {
                        String[] splitted = key.split("_");
                        pluginOptions.put(splitted[1], postDataMap.get(key));
                    }
                }
                DevicePluginService.getInstance().savePlugin(postDataMap.get("plugin_name"),
                        postDataMap.get("plugin_desc"),
                        1,
                        false,
                        pluginOptions,
                        Integer.parseInt(postDataMap.get("installed_id")));
            } catch (PluginServiceException ex) {
                LOG.error("Could not save new plugin: {}", ex.getMessage());
                errorMessage = "Could not save new plugin: " + ex.getMessage();
                saveResult = false;
            }
        } else {
            if (!getDataMap.isEmpty() && getDataMap.containsKey("installed_id")) {
                try {
                    pageData.put("plugin", DevicePluginService.getInstance().getBareboneDevicePluginInstance(Integer.parseInt(getDataMap.get("installed_id"))));
                    pageData.put("installed_id", getDataMap.get("installed_id"));
                } catch (PluginException ex) {
                    LOG.error("Could not load barebone instance: {}", getDataMap.get("installed_id"));
                }
            } else if (!getDataMap.isEmpty() && getDataMap.containsKey("plugin_id")) {
                try {
                    pageData.put("plugin", DevicePluginService.getInstance().getPlugin(Integer.parseInt(getDataMap.get("plugin_id"))));
                    pageData.put("plugin_id", getDataMap.get("plugin_id"));
                } catch (PluginException ex) {
                    LOG.error("Could not fetch plugin instance from plugin id: {}", getDataMap.get("plugin_id"), ex);
                }
            } else {
                Map<Integer, Map<String, Object>> origData = DevicePluginService.getInstance().getInstalledPlugins();
                Map<String, Map<String, Object>> pluginData = new HashMap<>();
                for (int key : origData.keySet()) {
                    pluginData.put(String.valueOf(key), origData.get(key));
                }
                pageData.put("pluginlist", pluginData);
            }
            setData(pageData);
        }

    }

    @Override
    public final String render() throws Exception {
        if (render == true) {
            return super.render();
        } else {
            if (saveResult == false) {
                return "{ \"result\" : { \"exec\":false, \"reason\":\"" + errorMessage + "\" } }";
            } else {
                return "{ \"result\" : { \"exec\":true } }";
            }
        }
    }

}
