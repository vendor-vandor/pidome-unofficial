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

import java.util.ArrayList;
import java.util.Map;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemote;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteEventListener;
import org.pidome.server.connector.plugins.plugindata.PluginDataException;
import org.pidome.server.system.plugins.PluginsDB;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

/**
 *
 * @author John
 */
public class RemotesPluginService extends PluginService implements PiDomeRemoteEventListener {

    private static RemotesPluginService me;
    
    int definedPluginId = 5;
    int definedPluginTypeId = 4;
    
    /**
     * Constructor.
     */
    protected RemotesPluginService(){
        if(me!=null){
            me = this;
        }
    }
    
    /**
     * Returns instance.
     * @return 
     */
    public static RemotesPluginService getInstance(){
        if(me==null){
            me = new RemotesPluginService();
        }
        return me;
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
    public int getPluginTypeId() {
        return definedPluginTypeId;
    }

    /**
     * @inheritDoc
     */
    @Override
    public PiDomeRemote getPlugin(int pluginId) throws PluginException {
        if(pluginsList.containsKey(pluginId)) return (PiDomeRemote)pluginsList.get(pluginId);
        throw new PluginException("Remote id '"+pluginId+"' not found");
    }

    /**
     * Returns a list of remotes.
     * @return 
     */
    public Map<Integer,Map<String,Object>> getPlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        ArrayList<Integer> toRemove = new ArrayList();
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key)){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (PiDomeRemote)pluginsList.get(key));
            } else {
                toRemove.add(key);
            }
        };
        for (int key : toRemove){
            pluginCollection.remove(key);
        }
        return pluginCollection;
    }
    
    public final void handleRemoteButton(int pluginId, String buttonId) throws PiDomeRemoteButtonException {
        ((PiDomeRemote)pluginsList.get(pluginId)).handleButton(buttonId);
    }
    
    /**
     * Sets the remote visual data.
     * @param pluginId
     * @param data
     * @return 
     * @throws PluginException 
     */
    public boolean updateRemoteVisuals(int pluginId, Object data) throws PluginException {
        String saveData;
        try {
            saveData = PidomeJSONRPCUtils.getParamCollection(data);
        } catch (PidomeJSONRPCException ex) {
            throw new PluginException("Could not map json data");
        }
        if(saveCustomData(pluginId, saveData)){
            ((PiDomeRemote)pluginsList.get(pluginId)).createButtonsSet((Map<String,Object>)data);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    void startPluginHandlers(int pluginId) {
        ((PiDomeRemote)pluginsList.get(pluginId)).addListener(me);
        PidomeJSONRPC jsonRpc;
        try {
            jsonRpc = new PidomeJSONRPC(((PiDomeRemote)pluginsList.get(pluginId)).getCustomData(), false);
            ((PiDomeRemote)pluginsList.get(pluginId)).createButtonsSet(
                    jsonRpc.getParsedObject()
            );
        } catch (NullPointerException | PluginDataException | PidomeJSONRPCException ex) {
            LOG.error("Could not create buttons: {}", ex.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    void stopHandlers(int pluginId) {
        ((PiDomeRemote)pluginsList.get(pluginId)).removeListener(me);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getServiceName() {
        return "Remotes plugin service";
    }

    @Override
    public void handleRemoteDataEvent(int pluginId, String remoteString) {
        /// The remote string received.
    }

    @Override
    public void handleRemoteRecordingEvent(int pluginId, boolean enabled) {
        /// if recording or not.
    }
    
}
