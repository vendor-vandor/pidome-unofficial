/*
 * Copyright 2015 John.
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
import org.pidome.server.services.events.CustomEvent;
import org.pidome.server.services.events.EventService;
import org.pidome.server.services.events.EventServiceException;

/**
 *
 * @author John
 */
public class EventServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements EventServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getCustomEvents", null);
                put("getCustomEvent", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("occurById", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("reason", "");}});
                    }
                });
                put("occurByIdentifier", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("identifier", "");}});
                        put(1,new HashMap<String,Object>(){{put("reason", "");}});
                    }
                });
                put("addCustomEvent", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("identifier", "");}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("updateCustomEvent", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("deleteCustomEvent", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    @Override
    public List<Map<String,Object>> getCustomEvents() throws EventServiceException {
        List<Map<String,Object>> events = new ArrayList<>();
        for(CustomEvent event:EventService.getInstance().getCustomEvents()){
            Map<String,Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("identifier", event.getIdentifier());
            eventMap.put("name", event.getName());
            eventMap.put("description", event.getDescription());
            eventMap.put("lastoccurrence", event.getLastOccurrence());
            eventMap.put("reason", event.getLastOccurrenceDescription());
            events.add(eventMap);
        }
        return events;
    }

    @Override
    public Map<String,Object> getCustomEvent(Number eventId) throws EventServiceException {
        Map<String,Object> eventMap = new HashMap<>();
        CustomEvent event = EventService.getInstance().getCustomEvent(eventId.intValue());
        eventMap.put("id", event.getId());
        eventMap.put("identifier", event.getIdentifier());
        eventMap.put("name", event.getName());
        eventMap.put("description", event.getDescription());
        eventMap.put("lastoccurrence", event.getLastOccurrence());
        eventMap.put("reason", event.getLastOccurrenceDescription());
        return eventMap;
    }

    @Override
    public boolean occurById(Long id, String reason) throws EventServiceException {
        return EventService.getInstance().occur(id.intValue(), reason);
    }

    @Override
    public boolean occurByIdentifier(String identifier, String reason) throws EventServiceException {
        return EventService.getInstance().occur(identifier, reason);
    }

    @Override
    public boolean updateCustomEvent(Long id, String name, String description) throws EventServiceException {
        return EventService.getInstance().updateCustomEvent(id.intValue(), name, description);
    }

    @Override
    public boolean addCustomEvent(String identifier, String name, String description) throws EventServiceException {
        return EventService.getInstance().addCustomEvent(identifier, name, description);
    }

    @Override
    public boolean deleteCustomEvent(Long id) throws EventServiceException {
        return EventService.getInstance().deleteCustomEvent(id.intValue());
    }

}