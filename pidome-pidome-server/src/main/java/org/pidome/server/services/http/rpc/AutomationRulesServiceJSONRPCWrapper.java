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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.automations.AutomationRules;
import org.pidome.server.services.automations.AutomationRulesException;

/**
 *
 * @author John
 */
public class AutomationRulesServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements AutomationRulesServiceJSONRPCWrapperInterface {
    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getRules", null);
                put("getRule", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteRule", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("enableRule", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("active", true);}});
                    }
                });
                put("saveRule", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                        put(2,new HashMap<String,Object>(){{put("active", true);}});
                        put(3,new HashMap<String,Object>(){{put("rule", "");}});
                    }
                });
                put("updateRule", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("active", true);}});
                        put(4,new HashMap<String,Object>(){{put("rule", "");}});
                    }
                });
            }
        };
        return mapping;
    }

    @Override
    public final ArrayList<Map<String, Object>> getRules() throws AutomationRulesException {
        return AutomationRules.getRules();
    }

    @Override
    public final Map<String, Object> getRule(Long ruleId) throws AutomationRulesException {
        return AutomationRules.getRule(ruleId.intValue());
    }

    @Override
    public final boolean saveRule(String name, String description, boolean active, String rule) throws AutomationRulesException {
        AutomationRules.saveRule(0, name, description, active, rule);
        return true;
    }

    @Override
    public final boolean updateRule(Long ruleId, String name, String description, boolean active, String rule) throws AutomationRulesException {
        AutomationRules.saveRule(ruleId.intValue(), name, description, active, rule);
        return true;
    }

    @Override
    public final boolean deleteRule(Long ruleId) throws AutomationRulesException {
        try {
            AutomationRules.deleteRule(ruleId.intValue());
        } catch (SQLException ex) {
            throw new AutomationRulesException("Could not delete rule: " + ex.getMessage());
        }
        return true;
    }
    
    @Override
    public final boolean enableRule(Number ruleId, boolean active) throws AutomationRulesException {
        return AutomationRules.setRuleActive(ruleId.intValue(), active);
    }
    
}
