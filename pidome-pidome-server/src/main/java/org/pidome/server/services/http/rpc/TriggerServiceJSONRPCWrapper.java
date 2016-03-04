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
import org.pidome.server.services.triggerservice.TriggerEvent;
import org.pidome.server.services.triggerservice.TriggerException;
import org.pidome.server.services.triggerservice.TriggerService;

/**
 *
 * @author John
 */
public class TriggerServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements TriggerServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getTriggers", null);
                put("getAllTriggerTypes", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("filter", new Object());}});
                    }
                });
                put("getTriggerMatchTypes", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("filter", new Object());}});
                    }
                });
                put("getTriggerActionTypes", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("filter", new Object());}});
                    }
                });
                put("getTrigger", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("runTrigger", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteTrigger", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("saveTrigger", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                        put(2,new HashMap<String,Object>(){{put("reccurrence", "");}});
                        put(3,new HashMap<String,Object>(){{put("rulesmatch", "");}});
                        put(4,new HashMap<String,Object>(){{put("rules", new ArrayList());}});
                        put(5,new HashMap<String,Object>(){{put("executions", new ArrayList());}});
                    }
                });
                put("updateTrigger", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("reccurrence", "");}});
                        put(4,new HashMap<String,Object>(){{put("rulesmatch", "");}});
                        put(5,new HashMap<String,Object>(){{put("rules", new ArrayList());}});
                        put(6,new HashMap<String,Object>(){{put("executions", new ArrayList());}});
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
    public Object getTriggers() throws TriggerException {
        ArrayList<Map<String,Object>> triggerSet = new ArrayList();
        for (TriggerEvent trigger : TriggerService.getTriggers().values()) {
            Map<String,Object>triggerDetails = new HashMap<>();
            triggerDetails.put("id", trigger.getTriggerId());
            triggerDetails.put("name", trigger.getTriggerName());
            triggerDetails.put("description", trigger.getTriggerDescription());
            triggerDetails.put("occurrence", trigger.getOccurrence().toString());
            triggerDetails.put("rulesmatch", trigger.getRulesMatch());
            triggerDetails.put("rules", trigger.getRulesCount());
            triggerDetails.put("subjects", trigger.getSubjectsCount());
            triggerDetails.put("lastoccurrence", trigger.getLastOccurrence());
            triggerSet.add(triggerDetails);
        }
        return triggerSet;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getAllTriggerTypes(ArrayList<Object> filter) throws TriggerException {
        ArrayList<Map<String,String>> types = new ArrayList<>();
        if(filter.contains("devices") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "device");
                        put("name", "Device");
                    }
                });
        }
        if(filter.contains("daytimes") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "daytime");
                        put("name", "Time of day");
                    }
                });
        }
        if (filter.contains("presences") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "presence");
                    put("name", "Presence");
                }
            });
        }
        if (filter.contains("macros") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "macros");
                    put("name", "Macro's");
                }
            });
        }   
        if (filter.contains("scenes") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "scenes");
                    put("name", "Scenes");
                }
            });
        }   
        if(filter.contains("dayparts") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "daypart");
                        put("name", "Part of day");
                    }
                });
        }
        if(filter.contains("currenttime") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "currenttime");
                        put("name", "Current time");
                    }
                });
        }
        if(filter.contains("weatherplugin") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "weatherplugin");
                        put("name", "Weather");
                    }
                });
        }
        if(filter.contains("utilityusages") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "utilityusages");
                        put("name", "Utility usages");
                    }
                });
        }
        if(filter.contains("userstatuses") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "userstatus");
                        put("name", "User status");
                    }
                });
        }
        if(filter.contains("mediaplugins") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "mediaplugin");
                        put("name", "Media plugin");
                    }
                });
        }
        if (filter.contains("messengerplugins") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "messengerplugin");
                    put("name", "Messenger plugin");
                }
            });
        }
        if (filter.contains("remotes") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "remotesplugin");
                    put("name", "Remotes plugin");
                }
            });
        }
        return types;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getTriggerMatchTypes(ArrayList<Object> filter) throws TriggerException {
        ArrayList<Map<String,String>> types = new ArrayList<>();
        if(filter.contains("devices") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "device");
                        put("name", "Device");
                    }
                });
        }
        if(filter.contains("daytimes") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "daytime");
                        put("name", "Time of day");
                    }
                });
        }
        if(filter.contains("presences") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "presence");
                        put("name", "Presence");
                    }
                });
        }        
        if(filter.contains("dayparts") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "daypart");
                        put("name", "Part of day");
                    }
                });
        }
        if(filter.contains("userstatuses") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "userstatus");
                        put("name", "User status");
                    }
                });
        }
        if(filter.contains("mediaplugins") || filter.isEmpty()){
            types.add(new HashMap<String,String>(){
                    {
                        put("id",  "mediaplugin");
                        put("name", "Media plugin");
                    }
                });
        }
        return types;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getTriggerActionTypes(ArrayList<Object> filter) throws TriggerException {
        ArrayList<Map<String, String>> types = new ArrayList<>();
        if (filter.contains("devices") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "device");
                    put("name", "Device");
                }
            });
        }
        if (filter.contains("presences") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "presence");
                    put("name", "Presence");
                }
            });
        }
        if (filter.contains("macros") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "macros");
                    put("name", "Macro's");
                }
            });
        }
        if (filter.contains("userstatuses") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "userstatus");
                    put("name", "User status");
                }
            });
        }
        if (filter.contains("mediaplugins") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "mediaplugin");
                    put("name", "Media plugin");
                }
            });
        }
        if (filter.contains("messengerplugins") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "messengerplugin");
                    put("name", "Messenger plugin");
                }
            });
        }
        if (filter.contains("remotes") || filter.isEmpty()) {
            types.add(new HashMap<String, String>() {
                {
                    put("id", "remotesplugin");
                    put("name", "Remotes plugin");
                }
            });
        }
        return types;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object saveTrigger(String name, String description, String reccurrence, String ruleMatch, ArrayList ruleset, ArrayList exec) throws TriggerException {
        LOG.debug("Save trigger ruleset: {}", ruleset);
        TriggerEvent.Occurrence triggerReccurrence = TriggerEvent.Occurrence.TOGGLE;
        switch(reccurrence){
            case "CONTINUOUS":
                triggerReccurrence = TriggerEvent.Occurrence.CONTINUOUS;
            break;
            case "ONCE":
                triggerReccurrence = TriggerEvent.Occurrence.ONCE;
            break;
        }
        TriggerService.saveTrigger(name, description, triggerReccurrence, ruleMatch, ruleset, exec);
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object updateTrigger(Long triggerId, String name, String description, String reccurrence, String ruleMatch, ArrayList ruleset, ArrayList exec) throws TriggerException {
        TriggerEvent.Occurrence triggerReccurrence = TriggerEvent.Occurrence.TOGGLE;
        switch(reccurrence){
            case "CONTINUOUS":
                triggerReccurrence = TriggerEvent.Occurrence.CONTINUOUS;
            break;
            case "ONCE":
                triggerReccurrence = TriggerEvent.Occurrence.ONCE;
            break;
        }
        return TriggerService.saveTrigger(triggerId.intValue(), name, description, triggerReccurrence, ruleMatch, ruleset, exec);
    }

    @Override
    public Object getTrigger(Long id) throws TriggerException {
        TriggerEvent trigger = TriggerService.getTrigger(id.intValue());
        Map<String,Object> triggerDetails = new HashMap<>();
        triggerDetails.put("id", trigger.getTriggerId());
        triggerDetails.put("name", trigger.getTriggerName());
        triggerDetails.put("description", trigger.getTriggerDescription());
        triggerDetails.put("reccurrence", trigger.getOccurrence().toString());
        triggerDetails.put("rulesmatch", trigger.getRulesMatch());
        triggerDetails.put("rules", trigger.getTriggerRulesetSetup());
        triggerDetails.put("subjects", trigger.getSubjectsCount());
        triggerDetails.put("executions", trigger.getActionsList());
        return triggerDetails;
    }

    @Override
    public Object deleteTrigger(Long id) throws TriggerException {
        return TriggerService.deleteTrigger(id.intValue());
    }
    
    @Override
    public Object runTrigger(Long id) throws TriggerException {
        TriggerService.runTrigger(id.intValue());
        return true;
    }

}