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
package org.pidome.server.services.automations.rule;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class AutomationRulesVarProxy {
    
    private static Map<String,ObjectPropertyBindingBean> deviceList = Collections.synchronizedMap(new HashMap<>());
    private static Map<String,ObjectPropertyBindingBean> mediaList = Collections.synchronizedMap(new HashMap<>());
    
    static Logger LOG = LogManager.getLogger(AutomationRulesVarProxy.class);
    
    public static void addDeviceVarBinding(String deviceId, ObjectPropertyBindingBean prop){
        if(!deviceList.containsKey(deviceId)){
            deviceList.put(deviceId, new ObjectPropertyBindingBean());
        }
        deviceList.get(deviceId).bind(prop);
    }

    public static void removeDeviceVarBinding(String deviceId){
        if(deviceList.containsKey(deviceId)){
            deviceList.get(deviceId).unbind();
        }
    }
    
    public static ObjectPropertyBindingBean getDeviceBindListener(String deviceId){
        if(!deviceList.containsKey(deviceId)){
            deviceList.put(deviceId, new ObjectPropertyBindingBean());
        }
        return deviceList.get(deviceId);
    }
    
    public static void addPluginVarBinding(String pluginId, ObjectPropertyBindingBean prop){
        if(!deviceList.containsKey(pluginId)){
            deviceList.put(pluginId, new ObjectPropertyBindingBean());
        }
        deviceList.get(pluginId).bind(prop);
    }

    public static void removePluginVarBinding(String pluginId){
        if(deviceList.containsKey(pluginId)){
            deviceList.get(pluginId).unbind();
        }
    }
    
    public static ObjectPropertyBindingBean getPluginBindListener(String pluginId){
        if(!deviceList.containsKey(pluginId)){
            deviceList.put(pluginId, new ObjectPropertyBindingBean());
        }
        return deviceList.get(pluginId);
    }
    
}