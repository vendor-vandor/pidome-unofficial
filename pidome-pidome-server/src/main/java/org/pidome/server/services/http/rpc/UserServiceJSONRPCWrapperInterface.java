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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.presence.PresenceException;

/**
 *
 * @author John
 */
public interface UserServiceJSONRPCWrapperInterface {
    
    /**
     * Web interface signon method.
     * @param username
     * @param password
     * @return 
     */
    public Object signOn(String username, String password);
    
    /**
     * Goodbye.
     * @return 
     */
    public Object signOff();
    
    /**
     * Returns a list of all users.
     * @return
     * @throws PersonsManagementException 
     */
    public Object getUsers() throws PersonsManagementException;
    
    /**
     * Returns a list of users capable of assignments and server interactions
     * @return
     * @throws PersonsManagementException 
     */
    public Object getNormalizedUsers() throws PersonsManagementException;
    
    /**
     * Return a single bound device.
     * @param clientId
     * @return
     * @throws PersonsManagementException 
     */
    public Object getUserBoundDevice(Long clientId) throws PersonsManagementException;
    
    /**
     * Returns a single user.
     * @param userId
     * @return
     * @throws PersonsManagementException 
     */
    public Object getUser(Long userId) throws PersonsManagementException;
    
    /**
     * Returns personalized data.
     * @return
     * @throws PersonsManagementException 
     */
    public Object getMyData() throws PersonsManagementException;
    
    /**
     * Returns devices by the caller.
     * @return
     * @throws PersonsManagementException 
     */
    public Object getMyDevices() throws PersonsManagementException;
    
    /**
     * Updates a clients location.
     * This function is restricted to the logged in user and checks will be made if it is supplied by the device registered.
     * @param latitude The clients latitude
     * @param longitude The clients Longitude
     * @param Accuracy The accuracy of the current set location.
     * @return 
     * @throws org.pidome.server.services.clients.persons.PersonsManagementException When not applicable.
     */
    public Object updateMyLocation(Number latitude, Number longitude, Number accuracy) throws PersonsManagementException;
    
    /**
     * Updates the connected authorized user's password.
     * @param oldPass
     * @param newPass
     * @param newPassAgain
     * @return
     * @throws PersonsManagementException 
     */
    public Object updatePassword(String oldPass, String newPass, String newPassAgain) throws PersonsManagementException;
    
    /**
     * Returns devices bound to an user.
     * @param userId
     * @return
     * @throws PersonsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getUserBoundDevices(Long userId) throws PersonsManagementException;
    
    /**
     * Removes an user bound device.
     * This will cause the device to be disconnected and to be reassigned again.
     * @param deviceId
     * @return
     * @throws PersonsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object removeUserBoundDevice(Long deviceId) throws PersonsManagementException;
    
    /**
     * Updates a user
     * @param clientId
     * @param password
     * @param firstname
     * @param lastname
     * @param cpwd
     * @param roleset
     * @param ext
     * @param gpsdevices
     * @return
     * @throws PersonsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updateUser(Long clientId, String password, String firstname,String lastname, boolean cpwd,HashMap<String,Object> roleset,boolean ext, List<Map<String,Object>> gpsdevices) throws PersonsManagementException;
    
    /**
     * Removes an user
     * @param userId
     * @return
     * @throws PersonsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object removeUser(Long userId) throws PersonsManagementException;
    
    /**
     * Adds an user to the system.
     * @param username
     * @param password
     * @param firstname
     * @param lastname
     * @param cpwd
     * @param roleset
     * @param ext
     * @return
     * @throws PersonsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object addUser(String username, String password, String firstname, String lastname, boolean cpwd, HashMap<String,Object> roleset, boolean ext) throws PersonsManagementException;
    
    /**
     * Sets the presence of a user.
     * @param id
     * @param presenceId
     * @return
     * @throws PresenceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object setUserPresence(Long id, Long presenceId) throws PresenceException;
}