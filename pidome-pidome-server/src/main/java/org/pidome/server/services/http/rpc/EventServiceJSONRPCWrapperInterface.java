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

import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.services.events.EventServiceException;

/**
 *
 * @author John
 */
public interface EventServiceJSONRPCWrapperInterface {
    
    /**
     * Returns available custom events.
     * @return 
     * @throws org.pidome.server.services.events.EventServiceException 
     */
    public Object getCustomEvents() throws EventServiceException;
    
    /**
     * Returns a custom event.
     * @param eventId the event id.
     * @return a declared custom event.
     * @throws org.pidome.server.services.events.EventServiceException 
     */
    public Object getCustomEvent(Number eventId) throws EventServiceException;
    
    /**
     * Occur an event by it's id.
     * @param id the id of the event.
     * @param reason the reason of the event.
     * @return 
     * @throws org.pidome.server.services.events.EventServiceException 
     */
    public boolean occurById(Long id, String reason) throws EventServiceException;
    
    /**
     * Occur an event by it's identifier.
     * @param identifier the identifier of the event.
     * @param reason the reason of the event.
     * @return 
     * @throws org.pidome.server.services.events.EventServiceException 
     */
    public boolean occurByIdentifier(String identifier, String reason) throws EventServiceException;
    
    /**
     * Updates an event.
     * @param id the id of the event.
     * @param name he name of the event.
     * @param description the description of the event.
     * @return true when saved.
     * @throws EventServiceException when saving fails
     */
    @PiDomeJSONRPCPrivileged
    public boolean updateCustomEvent(Long id, String name, String description) throws EventServiceException;
    
    /**
     * Saves a new event.
     * @param identifier the identifier of the event.
     * @param name he name of the event.
     * @param description the description of the event.
     * @return true when saved.
     * @throws EventServiceException when saving fails
     */
    @PiDomeJSONRPCPrivileged
    public boolean addCustomEvent(String identifier, String name, String description) throws EventServiceException;
    
    /**
     * Deletes a custom event.
     * @param id the id of the event.
     * @return true when deleted.
     * @throws EventServiceException when deletion fails.
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteCustomEvent(Long id) throws EventServiceException;
    
}