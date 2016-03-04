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

package org.pidome.client.system.domotics.components.userpresence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;

/**
 *
 * @author John
 */
public class UserPresences implements DomComponent,ClientDataConnectionListener {

    static Map<Integer,String> presences = new HashMap<>();
    ObservableMap<Integer,String> observablePresences = FXCollections.observableMap(presences);
    
    static Logger LOG = LogManager.getLogger(UserPresences.class);
    
    static List _presenceChangedListeners = new ArrayList();
    
    static int current = 0;
    
    public UserPresences(ArrayList<Map<String,Object>> locationData){
        startPresenceChangeListener();
        locationData.stream().forEach((location) -> {
            createPresence(location);
        });
        ClientData.addClientDataConnectionListener(this);
    }
    
    public final void createPresence(Map<String,Object> info){
        LOG.trace("Create presence: {}", info);
        observablePresences.put(((Long)info.get("id")).intValue(), (String)info.get("name"));
        if((boolean)info.get("active")==true){
            current = ((Long)info.get("id")).intValue();
            _firePresenceChangeEvent(UserPresenceEvent.PRESENCECHANGED, current, (String)info.get("name"));
        }
    }

    public final void updatePresence(Map<String,Object> info){
        LOG.debug("Updating: {}", info);
        if(presences.containsKey(((Long)info.get("id")).intValue())){
            observablePresences.put(((Long)info.get("id")).intValue(), (String)info.get("name"));
            if((boolean)info.get("active")==true){
                current = ((Long)info.get("id")).intValue();
                _firePresenceChangeEvent(UserPresenceEvent.PRESENCECHANGED, current, (String)info.get("name"));
            }
        }
    }
    
    public static int getCurrent(){
        return current;
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.USERPRESENCERECEIVED)){
            Map<String,Object> dataSet = (Map<String,Object>)event.getData();
            LOG.debug("Got data: {}", dataSet);
            switch(event.getMethod()){
                case "activateGlobalPresence":
                    try {
                        int presenceId = ((Long)dataSet.get("id")).intValue();
                        if(presences.containsKey(presenceId) && current!=presenceId){
                            current = presenceId;
                            _firePresenceChangeEvent(UserPresenceEvent.PRESENCECHANGED, current, presences.get(current));
                        }
                    } catch (NullPointerException e){
                        LOG.error("Faulty presence data: {}", event.getData());
                    }
                break;
                case "addPresence":
                    createPresence(dataSet);
                break;
                case "updatePresence":
                    updatePresence(dataSet);
                break;
                case "deletePresence":
                    int presenceId = ((Long)dataSet.get("id")).intValue();
                    if(observablePresences.containsKey(presenceId)){
                        observablePresences.remove(presenceId);
                    }
                break;
            }
        }
    }
    
    final void startPresenceChangeListener(){
        observablePresences.addListener((MapChangeListener.Change<? extends Integer, ? extends String> change) -> {
            if (change.wasRemoved() && change.wasAdded()){
                _firePresenceChangeEvent(UserPresenceEvent.PRESENCEUPDATED, change.getKey(), change.getValueAdded());
            } else if (change.wasRemoved()){
                _firePresenceChangeEvent(UserPresenceEvent.PRESENCEREMOVED, change.getKey(), change.getValueRemoved());
            } else if(change.wasAdded()){
                _firePresenceChangeEvent(UserPresenceEvent.PRESENCEADDED, change.getKey(), change.getValueAdded());
            }
        });
    }
    
    public static Map<Integer,String> getPresences(){
        return presences;
    }
    
    public static String getSetActiveCommand(int presenceId) throws DomComponentsException{
        Map<String, Object> params = new HashMap<>();
        params.put("id", presenceId);
        try {
            return PidomeJSONRPC.createExecMethod("PresenceService.activateGlobalPresence", "PresenceService.activatePresence", params);
        } catch (PidomeJSONRPCException ex) {
            throw new DomComponentsException(ex.getMessage());
        }
    }
    
    public static String getPresence(int id) throws DomComponentsException {
        if(presences.containsKey(id)){
            return presences.get(id);
        } else {
            LOG.error("Presence id {} does not exist", id);
            throw new DomComponentsException("Presence id " + id + " does not exist");
        }
    }
    
    public static synchronized void addPresencesEventListener(UserPresencesEventListener l){
        LOG.debug("Added presence event listener {}", l.getClass().getName());
        _presenceChangedListeners.add(l);
    }

    public static synchronized void removePresencesEventListener(UserPresencesEventListener l){
        LOG.debug("Removed presence event listener {}", l.getClass().getName());
        _presenceChangedListeners.remove(l);
    }
    
    private synchronized void _firePresenceChangeEvent(String EVENTTYPE, int locationId, String locationName) {
        LOG.debug("Event: {}", EVENTTYPE);
        UserPresenceEvent event = new UserPresenceEvent(this, EVENTTYPE);
        event.setPresenceData(locationId, locationName);
        Iterator listeners = _presenceChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((UserPresencesEventListener) listeners.next()).handleUserPresencesEvent(event);
        }
    }
    
}

