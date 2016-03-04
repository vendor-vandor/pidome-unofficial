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
import java.util.Map;
import org.pidome.server.services.automations.AutomationRulesException;

/**
 *
 * @author John
 */
public interface AutomationRulesServiceJSONRPCWrapperInterface {
    
    /**
     * Returns a list of rules.
     * @return
     * @throws AutomationRulesException 
     */
    @PiDomeJSONRPCPrivileged
    public ArrayList<Map<String,Object>> getRules() throws AutomationRulesException;
    
    /**
     * Returns a single rule.
     * @param ruleId
     * @return
     * @throws AutomationRulesException 
     */
    @PiDomeJSONRPCPrivileged
    public Map<String,Object> getRule(Long ruleId) throws AutomationRulesException;
    
    /**
     * Saves a rule.
     * @param name
     * @param description
     * @param active
     * @param rule
     * @return
     * @throws AutomationRulesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean saveRule(String name, String description, boolean active, String rule) throws AutomationRulesException;
    
    /**
     * Updates a rule.
     * @param ruleId
     * @param name
     * @param description
     * @param active
     * @param rule
     * @return
     * @throws AutomationRulesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean updateRule(Long ruleId, String name, String description, boolean active, String rule) throws AutomationRulesException;
    
    /**
     * Deletes a rule.
     * @param triggerId
     * @return
     * @throws AutomationRulesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteRule(Long triggerId) throws AutomationRulesException;

    /**
     * enables or disables a rule.
     * @param ruleId
     * @return
     * @throws AutomationRulesException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean enableRule(Number ruleId, boolean active) throws AutomationRulesException;
}
