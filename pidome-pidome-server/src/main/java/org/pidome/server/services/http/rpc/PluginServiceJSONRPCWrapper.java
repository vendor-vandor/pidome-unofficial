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
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.plugins.MessengerPluginService;
import org.pidome.server.services.plugins.PluginService;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.RemotesPluginService;
import org.pidome.server.services.plugins.UtilityPluginService;

/**
 *
 * @author John
 */
public class PluginServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements PluginServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getPlugins", null);
                put("getInstalledPlugins", null);
                put("restartPlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("setFavorite", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("favorite", new Boolean(true));}});
                    }
                });
                put("setInstalledActive", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("active", new Boolean(false));}});
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
    public Object getInstalledPlugins() {
        return PluginService.getFullInstalledPluginCollection();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getPlugins() {
        Map<Integer,Map<String,Object>> pluginsList = PluginService.getPluginsCollection();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(int key: pluginsList.keySet()){
            Map<String,Object> item = new HashMap<>();
            item.put("id", key);
            item.put("name", pluginsList.get(key).get("name"));
            item.put("description", pluginsList.get(key).get("description"));
            item.put("locationid", pluginsList.get(key).get("locationid"));
            item.put("locationname", pluginsList.get(key).get("location"));
            item.put("pluginname", pluginsList.get(key).get("pluginname"));
            item.put("activated", pluginsList.get(key).get("activated"));
            try {
                switch((int)pluginsList.get(key).get("typeid")){
                    case 1:
                        item.put("active", MediaPluginService.getInstance().getPlugin(key).getRunning());
                    break;
                    case 2:
                        item.put("active", UtilityPluginService.getInstance().getPlugin(key).getRunning());
                    break;
                    case 3:
                        item.put("active", MessengerPluginService.getInstance().getPlugin(key).getRunning());
                    break;
                    case 4:
                        item.put("active", RemotesPluginService.getInstance().getPlugin(key).getRunning());
                    break;
                }
            } catch (Exception ex){
                item.put("active", false);
            }
            item.put("type", pluginsList.get(key).get("typeid"));
            newList.add(item);
        }
        return newList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object restartPlugin(Long pluginId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object setFavorite(Long pluginId, Boolean favorite) throws PluginServiceException {
        return PluginService.setFavorite(pluginId.intValue(), favorite);
    }    
    
    /**
     * @inheritDoc
     */
    @Override
    public Object setInstalledActive(Long installedId, Boolean active) throws PluginServiceException {
        return PluginService.setActive(installedId.intValue(), active);
    }   

    /**
     * @inheritDoc
     */
    @Override
    public Object deletePlugin(Long pluginId) throws PluginServiceException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
