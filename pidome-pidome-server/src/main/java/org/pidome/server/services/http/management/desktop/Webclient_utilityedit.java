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
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.plugins.UtilityPluginService;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_utilityedit  extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_utilityedit.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Set utility measurements configuration");
        pageData.put("pluginsalive", false);
        try {
            try {
                
                Map<Integer, PluginBase> plugins = UtilityPluginService.getInstance().getRunningPlugins();
                if(plugins.isEmpty()) throw new PluginException("Plugin not loaded");
                
                pageData.put("pluginsalive", true);
                
                UtilityData plugin = UtilityPluginService.getInstance().getPlugin(plugins.keySet().iterator().next());
                pageData.put("pluginid", plugin.getPluginId());
                pageData.put("plugin", plugin);
                Map<String,String> deviceSet = new HashMap<>();
                try {
                    for(WebConfigurationOptionSet set: plugin.getConfiguration().getOptions()){
                        for(WebOption option: set.getOptions()){
                            if(option.getId().equals("POWERDEVICE") || option.getId().equals("WATERDEVICE") || option.getId().equals("GASDEVICE")){
                                try {
                                    String[] deviceStringSplitted = option.getValue().split(";");
                                    DeviceInterface device = DeviceService.getDevice(Integer.valueOf(deviceStringSplitted[0]));
                                    String deviceName = device.getDeviceName();
                                    String deviceCommandName = "";
                                    Map<String,DeviceControlsGroup> fullSet = device.getFullCommandSet().getControlsGroups();
                                    for(DeviceControlsGroup values: fullSet.values()){
                                        for(DeviceControl control: values.getGroupControls().values()){
                                            if(control.getControlId().equals(deviceStringSplitted[2])){
                                                deviceCommandName = ", " + control.getDescription();
                                                String hiddenId = "<input type=\"hidden\" id=\"utilityOption_"+option.getId()+"\" name=\"utilityOption_"+option.getId()+"\" value=\""+deviceStringSplitted[0]+";" + deviceStringSplitted[1] + ";" + deviceStringSplitted[2] + "\" >";
                                                deviceSet.put(option.getId(), deviceName + deviceCommandName + hiddenId);
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    deviceSet.put(option.getId(), "Could not determine current device");
                                    LOG.error("Could not get device for utility setting: {}", option.getId(), ex);
                                }
                            }
                        }
                    }
                } catch (WebConfigurationException ex) {
                    LOG.error("Could not load options", ex);
                }
                pageData.put("deviceSet", deviceSet);
            } catch (PluginException ex){
                LOG.debug("No plugin running, error (if in error): {}", ex.getMessage(), ex);
                if(getDataMap.containsKey("installed_id")){
                    pageData.put("plugin", UtilityPluginService.getInstance().getBareboneInstance(Integer.parseInt(getDataMap.get("installed_id"))));
                    pageData.put("installed_id", getDataMap.get("installed_id"));
                } else {
                    Map<String,Map<String,Object>> newSet = new HashMap<>();
                    for(Map.Entry<Integer,Map<String,Object>> walkSet:UtilityPluginService.getInstance().getInstalledPluginsList().entrySet()){
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