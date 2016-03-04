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

package org.pidome.client.system.domotics.components.dayparts;

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

/**
 *
 * @author John
 */
public class DayParts implements DomComponent,ClientDataConnectionListener {

    static Map<Integer,String> dayparts = new HashMap<>();
    ObservableMap<Integer,String> observableDayParts = FXCollections.observableMap(dayparts);
    
    static Logger LOG = LogManager.getLogger(DayParts.class);
    
    static List _daypartChangedListeners = new ArrayList();
    
    static int current = 0;
    
    public DayParts(ArrayList<Map<String,Object>> locationData){
        startDayPartChangeListener();
        locationData.stream().forEach((location) -> {
            createDayPart(location);
        });
        ClientData.addClientDataConnectionListener(this);
    }
    
    public final void createDayPart(Map<String,Object> info){
        LOG.trace("Create day part: {}", info);
        observableDayParts.put(((Long)info.get("id")).intValue(), (String)info.get("name"));
        if((boolean)info.get("active")==true){
            current = ((Long)info.get("id")).intValue();
            _fireDayPartChangeEvent(DayPartsEvent.DAYPARTCHANGED, current, (String)info.get("name"));
        }
    }

    public final void updateDayPart(Map<String,Object> info){
        LOG.debug("Updating: {}", info);
        if(dayparts.containsKey(((Long)info.get("id")).intValue())){
            observableDayParts.put(((Long)info.get("id")).intValue(), (String)info.get("name"));
            if((boolean)info.get("active")==true){
                current = ((Long)info.get("id")).intValue();
                _fireDayPartChangeEvent(DayPartsEvent.DAYPARTCHANGED, current, (String)info.get("name"));
            }
        }
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.DAYPARTRECEIVED)){
            Map<String,Object> dataSet = (Map<String,Object>)event.getData();
            LOG.debug("Got data: {}", dataSet);
            switch(event.getMethod()){
                case "setDayPart":
                    try {
                        int dayPartId = ((Long)dataSet.get("id")).intValue();
                        if(dayparts.containsKey(dayPartId) && current!=dayPartId){
                            current = dayPartId;
                            _fireDayPartChangeEvent(DayPartsEvent.DAYPARTCHANGED, current, dayparts.get(current));
                        }
                    } catch (NullPointerException e){
                        LOG.error("Faulty day part data: {}", event.getData());
                    }
                break;
                case "addDayPart":
                    createDayPart(dataSet);
                break;
                case "updateDayPart":
                    updateDayPart(dataSet);
                break;
                case "deleteDayPart":
                    int dayPartId = ((Long)dataSet.get("id")).intValue();
                    if(observableDayParts.containsKey(dayPartId)){
                        observableDayParts.remove(dayPartId);
                    }
                break;
            }
        }
    }
    
    final void startDayPartChangeListener(){
        observableDayParts.addListener((MapChangeListener.Change<? extends Integer, ? extends String> change) -> {
            if (change.wasRemoved() && change.wasAdded()){
                _fireDayPartChangeEvent(DayPartsEvent.DAYPARTUPDATED, change.getKey(), change.getValueAdded());
            } else if (change.wasRemoved()){
                _fireDayPartChangeEvent(DayPartsEvent.DAYPARTREMOVED, change.getKey(), change.getValueRemoved());
            } else if(change.wasAdded()){
                _fireDayPartChangeEvent(DayPartsEvent.DAYPARTADDED, change.getKey(), change.getValueAdded());
            }
        });
    }
    
    public static Map<Integer,String> getDayParts(){
        return dayparts;
    }
    
    public static int getCurrent(){
        return current;
    }
    
    public static String getDayPart(int id) throws DomComponentsException {
        if(dayparts.containsKey(id)){
            return dayparts.get(id);
        } else {
            LOG.error("Day part id {} does not exist", id);
            throw new DomComponentsException("Day part id " + id + " does not exist");
        }
    }
    
    public static synchronized void addDayPartsEventListener(DayPartsEventListener l){
        LOG.debug("Added day part event listener {}", l.getClass().getName());
        _daypartChangedListeners.add(l);
    }

    public static synchronized void removeDayPartsEventListener(DayPartsEventListener l){
        LOG.debug("Removed day part event listener {}", l.getClass().getName());
        _daypartChangedListeners.remove(l);
    }
    
    private synchronized void _fireDayPartChangeEvent(String EVENTTYPE, int dayPartId, String dayPartName) {
        LOG.debug("Event: {}", EVENTTYPE);
        DayPartsEvent event = new DayPartsEvent(this, EVENTTYPE);
        event.setDayPartData(dayPartId, dayPartName);
        Iterator listeners = _daypartChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((DayPartsEventListener) listeners.next()).handleDayPartsEvent(event);
        }
    }

}

