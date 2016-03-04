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

import org.pidome.server.system.userstatus.UserStatusException;

/**
 *
 * @author John
 */
public interface UserStatusServiceJSONRPCWrapperInterface {
    
    /**
     * Sets the current user status.
     * @param id
     * @return
     * @throws UserStatusException 
     */
    public Object setUserStatus(Long id) throws UserStatusException;

    /**
     * Returns a current user status.
     * @param id
     * @return
     * @throws UserStatusException 
     */
    public Object getUserStatus(Long id) throws UserStatusException;
    
    /**
     * Adds a user status.
     * @param name
     * @param description
     * @return
     * @throws UserStatusException 
     */
    @PiDomeJSONRPCPrivileged
    public Object addUserStatus(String name, String description) throws UserStatusException;
    
    /**
     * Updates a user status.
     * @param id
     * @param name
     * @param description
     * @return
     * @throws UserStatusException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updateUserStatus(Long id, String name, String description) throws UserStatusException;
    
    /**
     * Deletes a user status.
     * @param id
     * @return
     * @throws UserStatusException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deleteUserStatus(Long id) throws UserStatusException;
    
    /**
     * Returns a list of user statuses.
     * @return
     * @throws UserStatusException 
     */
    public Object getUserStatuses() throws UserStatusException;
    
}
