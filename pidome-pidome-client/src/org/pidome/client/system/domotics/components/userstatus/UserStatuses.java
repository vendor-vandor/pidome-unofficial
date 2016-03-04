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

package org.pidome.client.system.domotics.components.userstatus;

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
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 *
 * @author John
 */
public class UserStatuses implements DomComponent {

    static Map<Integer,String> presences = new HashMap<>();
    ObservableMap<Integer,String> observablePresences = FXCollections.observableMap(presences);
    
    static Logger LOG = LogManager.getLogger(UserStatuses.class);
    
    static List _locationChangedListeners = new ArrayList();
    
    public UserStatuses(ArrayList<Map<String,Object>> locationData){
        startPresenceChangeListener();
        locationData.stream().forEach((location) -> {
            createPresence(location);
        });
    }
    
    public final void createPresence(Map<String,Object> info){
        LOG.trace("Create presence: {}", info);
        observablePresences.put(((Long)info.get("id")).intValue(), (String)info.get("name"));
    }

    public final void updatePresence(Map<String,Object> info){
        LOG.debug("Updating: {}", info);
        if(presences.containsKey(((Long)info.get("id")).intValue())){
            observablePresences.put(((Long)info.get("id")).intValue(), (String)info.get("name"));
        }
    }
    
    final void startPresenceChangeListener(){
        observablePresences.addListener((MapChangeListener.Change<? extends Integer, ? extends String> change) -> {
            if (change.wasRemoved() && change.wasAdded()){
                _firePresenceChangeEvent(UserStatusEvent.PRESENCEUPDATED, change.getKey(), change.getValueAdded());
            } else if (change.wasRemoved()){
                _firePresenceChangeEvent(UserStatusEvent.PRESENCEREMOVED, change.getKey(), change.getValueRemoved());
            } else if(change.wasAdded()){
                _firePresenceChangeEvent(UserStatusEvent.PRESENCEADDED, change.getKey(), change.getValueAdded());
            }
        });
    }
    
    public static Map<Integer,String> getPresences(){
        return presences;
    }
    
    public static String getPresence(int id) throws DomComponentsException {
        if(presences.containsKey(id)){
            return presences.get(id);
        } else {
            LOG.error("Presence id {} does not exist", id);
            throw new DomComponentsException("Presence id " + id + " does not exist");
        }
    }
    
    public static synchronized void addPresencesEventListener(UserStatusEventListener l){
        LOG.debug("Added presence event listener {}", l.getClass().getName());
        _locationChangedListeners.add(l);
    }

    public static synchronized void removePresencesEventListener(UserStatusEventListener l){
        LOG.debug("Removed presence event listener {}", l.getClass().getName());
        _locationChangedListeners.remove(l);
    }
    
    private synchronized void _firePresenceChangeEvent(String EVENTTYPE, int locationId, String locationName) {
        LOG.debug("Event: {}", EVENTTYPE);
        UserStatusEvent event = new UserStatusEvent(this, EVENTTYPE);
        event.setPresenceData(locationId, locationName);
        Iterator listeners = _locationChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((UserStatusEventListener) listeners.next()).handleUserPresencesEvent(event);
        }
    }
    
}

