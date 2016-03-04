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

import java.util.List;
import java.util.Map;
import org.pidome.server.system.location.LocationServiceException;

/**
 *
 * @author John
 */
interface LocationServiceJSONRPCWrapperInterface {
    
    /**
     * Returns a map of locations
     * @return
     * @throws LocationServiceException 
     */
    public List<Map<String,Object>> getLocations() throws LocationServiceException;
    
    /**
     * Returns a list of rooms/locations identified by the given floor id.
     * @return
     * @throws LocationServiceException 
     */
    public List<Map<String,Object>> getLocationsByFloor(Long floorId)  throws LocationServiceException;
    
    /**
     * Returns a single location
     * @param locationId
     * @return
     * @throws LocationServiceException 
     */
    public Map<String,Object> getLocation(Long locationId) throws LocationServiceException;
    
    /**
     * Adds a location to the database
     * @param name
     * @param floor
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean addLocation(String name, Long floor) throws LocationServiceException;
    
    /**
     * Edits a location
     * @param locationId
     * @param name
     * @param floor
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean editLocation(Long locationId, String name, Long floor) throws LocationServiceException;
    
    /**
     * Deletes a location from the database
     * @param locationId
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteLocation(Long locationId) throws LocationServiceException;
    
    
    /**
     * Returns a map of floors
     * @return
     * @throws LocationServiceException 
     */
    public List<Map<String,Object>> getFloors() throws LocationServiceException;
    
    /**
     * Returns a single floor
     * @param locationId
     * @return
     * @throws LocationServiceException 
     */
    public Map<String,Object> getFloor(Long floorId) throws LocationServiceException;
    
    /**
     * Adds a floor to the database
     * @param name
     * @param description
     * @param floor
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean addFloor(String name, Long level) throws LocationServiceException;
    
    /**
     * Edits a floor
     * @param locationId
     * @param name
     * @param description
     * @param floor
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean editFloor(Long floorId, String name, Long level) throws LocationServiceException;
    
    /**
     * Deletes a floor from the database
     * @param locationId
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteFloor(Long floorId) throws LocationServiceException;
    
    
    /////////////// These are "private" exposed functions for use with the visual room editor
    
    /**
     * Sets an image path to the server's http path.
     * By setting an image path the "floor" turns on it's visibility.
     * @param floorId
     * @param imagePath
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean setFloorVisual(Long floorId, String imagePath) throws LocationServiceException;
    
    /**
     * Unsets the visibility of a floor.
     * This function does <b>not</b> remove the floor's room bounds.
     * @param floorId
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean removeFloorVisual(Long floorId) throws LocationServiceException;
    
    /**
     * This function is used in conjunction with the visual floor editor.
     * The reason the old parameter is used is that this one is none zero when a room name has been changed.
     * When the room name has been changed the old room id is used to detach and the normal room id used to attach.
     * @param floorId
     * @param roomId
     * @param x
     * @param y
     * @param w
     * @param h
     * @param old
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public boolean setRoomVisual(Long roomId, Long x, Long y, Long w, Long h, Long old) throws LocationServiceException;
    
    /**
     * Updates a room with new bounds
     * @param roomId
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean updateRoomVisual(Long roomId, Long x, Long y, Long w, Long h) throws LocationServiceException;
    
    /**
     * Removes a room from visibility.
     * @param roomId
     * @return
     * @throws LocationServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean removeRoomVisual(Long roomId) throws LocationServiceException;
    
}
