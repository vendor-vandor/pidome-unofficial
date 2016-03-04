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
import org.pidome.server.services.triggerservice.TriggerException;

/**
 *
 * @author John
 */
public interface TriggerServiceJSONRPCWrapperInterface {
    
    /**
     * Retrieves a list of triggers.
     * @return
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getTriggers() throws TriggerException;

    /**
     * Returns a list of possible trigger subject types as device, daytime etc...
     * This returns a list for a trigger where these types represent possible matching subjects for triggers to return positive for if matched.
     * @param filter To be able to filter out the types to return. USe an empty array to return all
     * @return All possible items unless filtered.
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getAllTriggerTypes(ArrayList<Object> filter) throws TriggerException;
    
    /**
     * Returns a list of possible trigger subject types as device, daytime etc...
     * This returns a list for a trigger where these types represent possible matching subjects for triggers to return positive for if matched.
     * @param filter To be able to filter out the types to return. USe an empty array to return all
     * @return Only items which are able to match against.
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getTriggerMatchTypes(ArrayList<Object> filter) throws TriggerException;
    
    /**
     * Returns a list of possible trigger result actions.
     * This is a list of possible types which are capable to be executed when trigger rules return positive to be executed.
     * @param filter To be able to filter out the types to return. USe an empty array to return all
     * @return Only items available for action types
     * @throws TriggerException 
     */
    public Object getTriggerActionTypes(ArrayList<Object> filter) throws TriggerException;
    
    /**
     * Returns a single trigger.
     * @param id
     * @return
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getTrigger(Long id) throws TriggerException;
    
    /**
     * Deletes a trigger
     * @param id
     * @return
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deleteTrigger(Long id) throws TriggerException;
    
    /**
     * Saves a trigger on the system.
     * @param name
     * @param description
     * @param reccurrence
     * @param ruleMatch
     * @param ruleset
     * @param exec
     * @return
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object saveTrigger(String name, String description, String reccurrence, String ruleMatch, ArrayList ruleset, ArrayList exec) throws TriggerException;
    
    /**
     * Updates a trigger in the system.
     * @param triggerId
     * @param name
     * @param description
     * @param reccurrence
     * @param ruleMatch
     * @param ruleset
     * @param exec
     * @return
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updateTrigger(Long triggerId, String name, String description, String reccurrence, String ruleMatch, ArrayList ruleset, ArrayList exec) throws TriggerException;
    
    /**
     * Runs a trigger as it if where true.
     * @param id
     * @return
     * @throws TriggerException 
     */
    @PiDomeJSONRPCPrivileged
    public Object runTrigger(Long id) throws TriggerException;
    
}
