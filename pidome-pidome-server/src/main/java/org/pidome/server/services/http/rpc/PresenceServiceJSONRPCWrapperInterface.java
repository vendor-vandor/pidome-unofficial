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

import org.pidome.server.system.presence.PresenceException;

/**
 *
 * @author John
 */
public interface PresenceServiceJSONRPCWrapperInterface {
    
    /**
     * Sets the current presence.
     * @param id
     * @return
     * @throws PresenceException 
     */
    public Object setGlobalPresence(Long id) throws PresenceException;

    /**
     * Activates and sets the current presence.
     * The difference between activate and set is that activatePresence has the ability to run a macro if set.
     * @param id
     * @return
     * @throws PresenceException 
     */
    public Object activateGlobalPresence(Long id) throws PresenceException;
    
    /**
     * Returns a current presence.
     * @param id
     * @return
     * @throws PresenceException 
     */
    public Object getPresence(Long id) throws PresenceException;
    
    /**
     * Adds a presence.
     * @param name
     * @param description
     * @param macroid
     * @return
     * @throws PresenceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object addPresence(String name, String description, Long macroid) throws PresenceException;
    
    /**
     * Updates a presence.
     * @param id
     * @param name
     * @param description
     * @param macroid
     * @return
     * @throws PresenceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updatePresence(Long id, String name, String description, Long macroid) throws PresenceException;
    
    /**
     * Deletes a presence.
     * @param id
     * @return
     * @throws PresenceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deletePresence(Long id) throws PresenceException;
    
    /**
     * Returns a list of presences.
     * @return
     * @throws PresenceException 
     */
    public Object getPresences() throws PresenceException;
    
    /**
     * Sets the presence of an endpoint user (person) calling this.
     * @param id
     * @return
     * @throws PresenceException 
     */
    public Object setPresence(Long id) throws PresenceException;
    
}