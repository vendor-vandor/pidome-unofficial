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

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataInterface;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.UtilityPluginService;


public class UtilityMeasurementServiceJSONWrapper extends AbstractRPCMethodExecutor implements UtilityMeasurementServiceJSONWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getCurrentUsage", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("filter", new ArrayList());}});
                    }
                });
                put("getCurrentTotalUsage", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("filter", new ArrayList());}});
                    }
                });
                put("getPlugins", null);
                put("removePlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getCurrentUsage(Long pluginId, ArrayList filter) throws PluginException {
        UtilityData plugin = UtilityPluginService.getInstance().getPlugin(pluginId.intValue());
        Map<UtilityDataInterface.Type, List<Map<String,Map<String,Object>>>> usageData = plugin.getCurrentUsages();
        if(filter == null || filter.isEmpty()){
            return usageData;
        } else {
            Map<UtilityDataInterface.Type,List<Map<String,Map<String,Object>>>> filteredData = new HashMap<>();
            for(int i=0;i<filter.size();i++){
                for(UtilityDataInterface.Type type:usageData.keySet()){
                    if(type.toString().equals(filter.get(i))){
                        filteredData.put(type, usageData.get(type));
                    }
                }
            }
            return filteredData;
        }
    }

        /**
     * @inheritDoc
     */
    @Override
    public Object getCurrentTotalUsage(Long pluginId, ArrayList filter) throws PluginException {
        UtilityData plugin = UtilityPluginService.getInstance().getPlugin(pluginId.intValue());
        Map<UtilityDataInterface.Type, Map<String,Map<String,Object>>> usageData = plugin.getCurrentTotalUsages();
        if(filter == null || filter.isEmpty()){
            return usageData;
        } else {
            Map<UtilityDataInterface.Type,Map<String,Map<String,Object>>> filteredData = new HashMap<>();
            for(int i=0;i<filter.size();i++){
                for(UtilityDataInterface.Type type:usageData.keySet()){
                    if(type.toString().equals(filter.get(i))){
                        filteredData.put(type, usageData.get(type));
                    }
                }
            }
            return filteredData;
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getPlugins() {
        Map<Integer,Map<String,Object>> pluginsList = UtilityPluginService.getInstance().getPlugins();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(int key: pluginsList.keySet()){
            Map<String,Object> item = new HashMap<>();
            item.put("id", key);
            item.put("name", pluginsList.get(key).get("name"));
            item.put("description", pluginsList.get(key).get("description"));
            item.put("locationid", pluginsList.get(key).get("locationid"));
            item.put("locationname", pluginsList.get(key).get("location"));
            item.put("pluginname", pluginsList.get(key).get("pluginname"));
            try {
                item.put("currentusage", UtilityPluginService.getInstance().getPlugin(key).getCurrentUsages());
            } catch (PluginException ex) {
                item.put("currentusage", new HashMap<>());
            }
            try {
                item.put("active", UtilityPluginService.getInstance().getPlugin(key).getRunning());
            } catch (Exception ex){
                item.put("active", false);
            }
            newList.add(item);
        }
        return newList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object removePlugin(Long pluginId) throws PluginException {
        try {
            return UtilityPluginService.getInstance().deletePlugin(pluginId.intValue());
        } catch (PluginServiceException ex) {
            LOG.error("Could not stop using plugin {}, {}", pluginId, ex.getMessage(), ex);
            throw new PluginException("Could not stop using plugin");
        }
    }
    
}
