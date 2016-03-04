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
import org.pidome.server.system.presence.Presence;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;

/**
 *
 * @author John
 */
public class PresenceServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements PresenceServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer,Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getPresences", null);
                put("setPresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("setGlobalPresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("activateGlobalPresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getPresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("addPresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                        put(1,new HashMap<String,Object>(){{put("macro", 0L);}});
                    }
                });
                put("updatePresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("macro", 0L);}});
                    }
                });
                put("deletePresence", new TreeMap<Integer,Map<String, Object>>(){
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
    public Object getPresences() throws PresenceException {
        List<Presence> presenceList = PresenceService.getPresences();
        List<Map<String, Object>> presences = new ArrayList();
        for(int i=0; i < presenceList.size(); i++){
            Map<String,Object>presence = new HashMap<>();
            presence.put("id", presenceList.get(i).getId());
            presence.put("name", presenceList.get(i).getName());
            presence.put("description", presenceList.get(i).getDescription());
            try {
                presence.put("macroid", presenceList.get(i).getMacroId());
            } catch (PresenceException ex){
                presence.put("macroid", null);
            }
            presence.put("lastactivated", presenceList.get(i).getLastActivated());
            presence.put("active", presenceList.get(i).getId()==PresenceService.current().getId());
            presence.put("fixed", presenceList.get(i).getIsFixed());
            presences.add(presence);
        }
        return presences;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object setGlobalPresence(Long id) throws PresenceException {
        return PresenceService.setGlobalPresence(id.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object activateGlobalPresence(Long id) throws PresenceException {
        return PresenceService.activateGlobalPresence(id.intValue());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getPresence(Long id) throws PresenceException {
        Presence presence = PresenceService.getPresence(id.intValue());
        Map<String,Object>presenceMap = new HashMap<>();
        presenceMap.put("id", presence.getId());
        presenceMap.put("name", presence.getName());
        presenceMap.put("description", presence.getDescription());
        try {
            presenceMap.put("macroid", presence.getMacroId());
        } catch (PresenceException ex){
            presenceMap.put("macroid", null);
        }
        presenceMap.put("active", presence.getId()==PresenceService.current().getId());
        presenceMap.put("lastactivated", presence.getLastActivated());
        presenceMap.put("fixed", presence.getIsFixed());
        return presenceMap;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object addPresence(String name, String description, Long macroid) throws PresenceException {
        return PresenceService.addPresence(name, description, macroid.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object updatePresence(Long id, String name, String description, Long macroid) throws PresenceException {
        return PresenceService.updatePresence(id.intValue(), name, description, macroid.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deletePresence(Long id) throws PresenceException {
        return PresenceService.deletePresence(id.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object setPresence(Long id) throws PresenceException {
        return PresenceService.setPresence(this.getCaller().getId(), id.intValue());
    }
    
}
