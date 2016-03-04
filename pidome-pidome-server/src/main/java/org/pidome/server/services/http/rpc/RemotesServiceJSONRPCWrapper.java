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

import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException;
import org.pidome.server.connector.plugins.plugindata.PluginDataException;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.services.plugins.RemotesPluginService;

/**
 *
 * @author John
 */
public class RemotesServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements RemotesServiceJSONRPCWrapperInterface {

    public RemotesServiceJSONRPCWrapper(){
        super();
    }

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getRemotes", null);
                put("getRemote", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteRemote", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getRemoteButtons", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("updateRemoteVisual", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("data", new Object());}});
                    }
                });
                put("pressButton", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("button", "");}});
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
    public Object getRemotes() {
        Map<Integer,Map<String,Object>> pluginsList = RemotesPluginService.getInstance().getPlugins();
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
                item.put("active", RemotesPluginService.getInstance().getPlugin(key).getRunning());
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
    public Object getRemote(Long remoteId) throws PluginException {
        Map<Integer,Map<String,Object>> pluginsList = RemotesPluginService.getInstance().getPlugins();
        Map<String,Object> item = new HashMap<>();
        for(int key: pluginsList.keySet()){
            if(key == remoteId.intValue()){
                item.put("id", key);
                item.put("name", pluginsList.get(key).get("name"));
                item.put("description", pluginsList.get(key).get("description"));
                item.put("locationid", pluginsList.get(key).get("locationid"));
                item.put("locationname", pluginsList.get(key).get("location"));
                item.put("pluginname", pluginsList.get(key).get("pluginname"));
                try {
                    item.put("active", RemotesPluginService.getInstance().getPlugin(key).getRunning());
                } catch (Exception ex){
                    item.put("active", false);
                }
                item.put("recorddevice", RemotesPluginService.getInstance().getPlugin(key).getRecorderDevice());
                item.put("sendtestdevice", RemotesPluginService.getInstance().getPlugin(key).getSenderDevice());
                try {
                    PidomeJSONRPC jsonRpc = new PidomeJSONRPC(RemotesPluginService.getInstance().getPlugin(key).getCustomData(), false);
                    item.put("remotevisuals", jsonRpc.parsedObject);
                } catch (PluginDataException | NullPointerException ex) {
                    item.put("remotevisuals", new HashMap<String,Object>());
                } catch (PidomeJSONRPCException ex) {
                    LOG.error("Could not parse remote visuals: {}", ex.getMessage());
                    item.put("remotevisuals", "");
                }
            }
        }
        return item;
    }

    
    /**
     * @inheritDoc
     */
    @Override
    public Object getRemoteButtons(Long remoteId) throws PiDomeRemoteButtonException {
        Map<Integer,Map<String,Object>> pluginsList = RemotesPluginService.getInstance().getPlugins();
        Map<String,Object> item = new HashMap<>();
        for(int key: pluginsList.keySet()){
            if(key == remoteId.intValue()){
                try {
                    PidomeJSONRPC jsonRpc = new PidomeJSONRPC(RemotesPluginService.getInstance().getPlugin(key).getCustomData(), false);
                    item.put("remotevisuals", jsonRpc.parsedObject);
                } catch (PluginDataException | NullPointerException ex) {
                    item.put("remotevisuals", new HashMap<String,Object>());
                } catch (PidomeJSONRPCException ex) {
                    LOG.error("Could not parse remote visuals: {}", ex.getMessage());
                    item.put("remotevisuals", "");
                } catch (PluginException ex) {
                    throw new PiDomeRemoteButtonException(ex.getMessage());
                }
            }
        }
        return item;
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public Object deleteRemote(Long remoteId) throws PluginException, PluginServiceException {
        return RemotesPluginService.getInstance().deletePlugin(remoteId.intValue());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object updateRemoteVisual(Long remoteId, Object data) throws PluginException {
        return RemotesPluginService.getInstance().updateRemoteVisuals(remoteId.intValue(), data);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object pressButton(Long remoteId, String buttonId) throws PiDomeRemoteButtonException {
        RemotesPluginService.getInstance().handleRemoteButton(remoteId.intValue(), buttonId);
        return true;
    }
    
}
