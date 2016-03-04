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

import org.pidome.server.system.dayparts.DayPartException;

/**
 *
 * @author John
 */
public interface DayPartServiceJSONRPCWrapperInterface {
    
    /**
     * Sets the current presence.
     * @param id
     * @return
     * @throws DayPartException 
     */
    public Object setDayPart(Long id) throws DayPartException;
    
    /**
     * Returns a current presence.
     * @param id
     * @return
     * @throws DayPartException 
     */
    public Object getDayPart(Long id) throws DayPartException;
    
    /**
     * Adds a presence.
     * @param name
     * @param description
     * @return
     * @throws DayPartException 
     */
    @PiDomeJSONRPCPrivileged
    public Object addDayPart(String name, String description) throws DayPartException;
    
    /**
     * Updates a presence.
     * @param id
     * @param name
     * @param description
     * @return
     * @throws DayPartException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updateDayPart(Long id, String name, String description) throws DayPartException;
    
    /**
     * Deletes a presence.
     * @param id
     * @return
     * @throws DayPartException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deleteDayPart(Long id) throws DayPartException;
    
    /**
     * Returns a list of presences.
     * @return
     * @throws DayPartException 
     */
    public Object getDayParts() throws DayPartException;
    
}
