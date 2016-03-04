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

package org.pidome.server.services.plugins;

import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataInterface;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataListener;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.plugins.PluginsDB;

/**
 *
 * @author John
 */
public class UtilityPluginService extends PluginService implements UtilityDataListener {
    
    static UtilityPluginService me;
    
    int definedPluginId = 2;
    int definedPluginTypeId = 2;
    
    /**
     * Constructor.
     */
    protected UtilityPluginService(){
        if(me!=null){
            me = this;
        }
    }
    
    /**
     * Returns instance.
     * @return 
     */
    public static UtilityPluginService getInstance(){
        if(me==null){
            me = new UtilityPluginService();
        }
        return me;
    }
    
    @Override
    public int getPluginTypeId() {
        return definedPluginTypeId;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final UtilityData getPlugin(int pluginId) throws PluginException {
        try {
            return (UtilityData)pluginsList.entrySet().iterator().next().getValue();
        } catch (Exception ex){
            throw new PluginException("Plugin not loaded");
        }
    }

    /**
     * Returns the media plugins known and if active including object.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getPlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key)){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (UtilityData)pluginsList.get(key));
            } else {
                pluginCollection.remove(key);
            }
        }
        return pluginCollection;
    }
    
    /**
     * Returns only the active utility data plugins known.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getActivePlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key) && pluginsList.get(key).getRunning()){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (UtilityData)pluginsList.get(key));
            } else {
                pluginCollection.remove(key);
            }
        }
        return pluginCollection;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    final void startPluginHandlers(int pluginId) {
        ((UtilityData)pluginsList.get(pluginId)).addListener(me);
    }

    /**
     * @inheritDoc
     */
    @Override
    final void stopHandlers(int pluginId) {
        ((UtilityData)pluginsList.get(pluginId)).removeListener(me);
    }

    /**
     * @inheritDoc
     */
    @Override
    public final int getInstalledId() {
        return definedPluginId;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleUtilityData(final UtilityData plugin, final UtilityDataInterface.Type type, final Map<String,Map<String,Object>> value) {
        ClientMessenger.send("UtilityMeasurementService","getCurrentUsage", plugin.getPluginLocationId(),new HashMap<String,Object>(){{
                    put("id", plugin.getPluginId());
                    put("type", type);
                    put("values", value);
                }}
        );
    }

    @Override
    public String getServiceName() {
        return "Utility usage plugin service";
    }
    
}
