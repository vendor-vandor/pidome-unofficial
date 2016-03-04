/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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

package org.pidome.server.system.location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.shareddata.SharedDataStatusSetter;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.db.DB;

/**
 * Basic locations
 * @author John Sirach
 */
public class BaseLocations implements ServiceInterface {
    
    final static List<Map<String,Object>> locations = new ArrayList();
    static Logger LOG = LogManager.getLogger(BaseLocations.class);
    
    @Override
    public void interrupt() {
        locations.clear();
    }

    @Override
    public void start() {
        try {
            reloadLocations();
        } catch (LocationServiceException ex) {
            LOG.error("Could not load locations: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isAlive() {
        return !locations.isEmpty();
    }

    @Override
    public String getServiceName() {
        return "Location service";
    }
    
    /**
     * Reloads known locations
     * @throws LocationServiceException 
     */
    private static synchronized void reloadLocations() throws LocationServiceException {
        LOG.debug("Reload");
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            locations.clear();
            try (Statement statementLocations = fileDBConnection.createStatement(); 
                    ResultSet rsLocations = statementLocations.executeQuery("SELECT l.id,l.name,l.floor,l.fixed,lf.name as floorname,l.screenX,l.screenY,l.screenW,l.screenH "
                                                                            + "FROM locations l "
                                                                           + "INNER JOIN locations_floors lf ON lf.id=l.floor")) {
                while (rsLocations.next()) {
                    Map<String, Object> setLocation = new HashMap<>();
                    setLocation.put("id", rsLocations.getInt("id"));
                    setLocation.put("name", rsLocations.getString("name"));
                    setLocation.put("screenX", rsLocations.getInt("screenX"));
                    setLocation.put("screenY", rsLocations.getInt("screenY"));
                    setLocation.put("screenW", rsLocations.getInt("screenW"));
                    setLocation.put("screenH", rsLocations.getInt("screenH"));
                    setLocation.put("floor", rsLocations.getInt("floor"));
                    setLocation.put("floorname", rsLocations.getString("floorname"));
                    setLocation.put("fixed", rsLocations.getBoolean("fixed"));
                    locations.add(setLocation);
                    LOG.debug("Loaded location: {}", setLocation);
                }
                SharedDataStatusSetter.setNewLocationSet(locations);
            }
        } catch (SQLException ex) {
            LOG.error("Locations sql error: {}", ex.getMessage());
            throw new LocationServiceException("Could not (re)load locations: " + ex.getMessage());
        }
    }
    
    /**
     * Returns a list of locations
     * @return
     * @throws LocationServiceException 
     */
    public static List<Map<String,Object>> getLocations() throws LocationServiceException {
        return locations;
    }
    
    /**
     * Get a single location
     * @param locationId
     * @return
     * @throws LocationServiceException 
     */
    public static Map<String,Object> getLocation(int locationId) throws LocationServiceException {
        if(locations.isEmpty()){
            reloadLocations();
        }
        for (int j = 0, k = locations.size(); j < k; j++) {
            if (locations.get(j).get("id").equals(locationId)) {
                return locations.get(j);
            }
        }
        throw new LocationServiceException("Location not found: " + locationId);
    }
    
    /**
     * Adds a location to the database
     * @param name
     * @param floor
     * @return
     * @throws LocationServiceException 
     */
    public static boolean saveLocation(String name, int floor) throws LocationServiceException {
        LOG.debug("Adding location {}, {}", name, floor);
        final int auto_id;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into 'locations' ('name','floor','created','modified') values (?,?,datetime('now'),datetime('now'))",Statement.RETURN_GENERATED_KEYS)){
                prep.setString(1, name);
                prep.setInt(2, floor);
                prep.execute();
                try (ResultSet rs = prep.getGeneratedKeys()) {
                    if (rs.next()) {
                        auto_id = rs.getInt(1);
                    } else {
                        auto_id = 0;
                    }
                }
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("could not add location: {}", ex.getMessage());
            throw new LocationServiceException("Could not add location: " + ex.getMessage());
        }
        if(auto_id>0){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", auto_id);
                }
            };
            ClientMessenger.send("LocationService","addLocation", 0, sendObject);
            reloadLocations();
            return true;
        } else {
            LOG.error("No new location added, returned new id=0");
            return false;
        }
    }
    
    /**
     * Updates a location in the database.
     * @param locationId
     * @param name
     * @param floor
     * @return
     * @throws LocationServiceException 
     */
    public static boolean editLocation(final int locationId, final String name, final int floor) throws LocationServiceException {
        LOG.debug("Updating location {}, {}", locationId,name);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("update 'locations' set 'name'=?, 'floor'=?,'modified'=datetime('now') where id=? and fixed=0")){
                prep.setString(1, name);
                prep.setInt(2, floor);
                prep.setInt(3, locationId);
                prep.executeUpdate();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("could not update location: {}", ex.getMessage());
            throw new LocationServiceException("Could not update location: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", locationId);
            }
        };
        ClientMessenger.send("LocationService","editLocation", 0, sendObject);
        return true;
    }
    
    /**
     * Removes a location from the database.
     * @param locationId
     * @return 
     * @throws org.pidome.server.system.location.LocationServiceException 
     */
    public static boolean deleteLocation(final int locationId) throws LocationServiceException {
        LOG.debug("Deleting location: {}", locationId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("delete from 'locations' where id=? and fixed=0")){
                prep.setInt(1, locationId);
                prep.executeUpdate();
            }
        } catch (SQLException ex) {
            LOG.error("Could not delete location: {}", ex.getMessage());
            throw new LocationServiceException("Could not delete location: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", locationId);
            }
        };
        ClientMessenger.send("LocationService","deleteLocation", 0, sendObject);
        return true;
    }

    /**
     * Returns a list of floors.
     * @return
     * @throws LocationServiceException 
     */
    public static List<Map<String,Object>> getFloors() throws LocationServiceException{
        List<Map<String,Object>> floors = new ArrayList();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            locations.clear();
            try {
                try (Statement statementLocations = fileDBConnection.createStatement(); 
                        ResultSet rsLocations = statementLocations.executeQuery("SELECT lf.id,lf.name,lf.zindex,lf.fixed,lf.image "
                                                                                + "FROM locations_floors lf WHERE fixed=0")) {
                    while (rsLocations.next()) {
                        Map<String, Object> setLocation = new HashMap<>();
                        setLocation.put("id", rsLocations.getInt("id"));
                        setLocation.put("name", rsLocations.getString("name"));
                        setLocation.put("level", rsLocations.getInt("zindex"));
                        setLocation.put("image", rsLocations.getString("image"));
                        setLocation.put("fixed", rsLocations.getBoolean("fixed"));
                        floors.add(setLocation);
                    }
                    return floors;
                }
            } catch (SQLException ex) {
                LOG.error("Locations sql error: {}", ex.getMessage());
                throw new LocationServiceException("Could not load floors: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("could not open locations: {}", ex.getMessage());
            throw new LocationServiceException("Could not load floors: " + ex.getMessage());
        }
    }

    /**
     * Returns a single floor.
     * @param floorId
     * @return
     * @throws LocationServiceException 
     */
    public static Map<String,Object> getFloor(int floorId) throws LocationServiceException{
        Map<String,Object> floor = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            locations.clear();
            try {
                try(PreparedStatement prep = fileDBConnection.prepareStatement("select id,name,zindex,fixed,image from locations_floors where id=? and fixed=0")){
                    prep.setInt(1, floorId);
                    ResultSet rsLocations = prep.executeQuery();
                    while (rsLocations.next()) {
                        floor.put("id", rsLocations.getInt("id"));
                        floor.put("name", rsLocations.getString("name"));
                        floor.put("image", rsLocations.getString("image"));
                        floor.put("level", rsLocations.getInt("zindex"));
                        floor.put("fixed", rsLocations.getBoolean("fixed"));
                    }
                    return floor;
                }
            } catch (SQLException ex) {
                LOG.error("Locations sql error: {}", ex.getMessage());
                throw new LocationServiceException("Could not load floor: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("could not open locations: {}", ex.getMessage());
            throw new LocationServiceException("Could not load floor: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a floor.
     * @param name
     * @param level
     * @return
     * @throws LocationServiceException 
     */
    public static boolean addFloor(String name, int level) throws LocationServiceException{
        LOG.debug("Adding floor {}, {}", name, level);
        final int auto_id;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into 'locations_floors' ('name','zindex','created','modified') values (?,?,datetime('now'),datetime('now'))",Statement.RETURN_GENERATED_KEYS)){
                prep.setString(1, name);
                prep.setInt(2, level);
                prep.execute();
                try (ResultSet rs = prep.getGeneratedKeys()) {
                    if (rs.next()) {
                        auto_id = rs.getInt(1);
                    } else {
                        auto_id = 0;
                    }
                }
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("could not add location: {}", ex.getMessage());
            throw new LocationServiceException("Could not add location: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", auto_id);
            }
        };
        ClientMessenger.send("LocationService","addFloor", 0, sendObject);
        return true;
    }

    /**
     * Edits a floor.
     * @param floorId
     * @param name
     * @param level
     * @return
     * @throws LocationServiceException 
     */
    public static boolean editFloor(final int floorId, final String name, final int level) throws LocationServiceException {
        LOG.debug("Updating location {}, {}, {}", floorId,name,level);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("update 'locations_floors' set 'name'=?, 'zindex'=?,'modified'=datetime('now') where id=? and fixed=0")){
                prep.setString(1, name);
                prep.setInt(2, level);
                prep.setInt(3, floorId);
                prep.executeUpdate();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("could not update floor: {}", ex.getMessage());
            throw new LocationServiceException("Could not update floor: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", floorId);
            }
        };
        ClientMessenger.send("LocationService","editFloor", 0, sendObject);
        return true;
    }
    
    /**
     * Deletes a floor from the db.
     * @param floorId
     * @return
     * @throws LocationServiceException 
     */
    public static boolean deleteFloor(final int floorId) throws LocationServiceException {
        LOG.debug("Deleting floor: {}", floorId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("delete from 'locations_floors' where id=? and fixed=0")){
                prep.setInt(1, floorId);
                prep.executeUpdate();
            }
        } catch (SQLException ex) {
            LOG.error("Could not delete floor: {}", ex.getMessage());
            throw new LocationServiceException("Could not delete floor: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", floorId);
            }
        };
        ClientMessenger.send("LocationService","deleteFloor", 0, sendObject);
        return true;
    }
    
    /**
     * Removes any links which make a floor visible.
     * @param floorId
     * @return
     * @throws LocationServiceException 
     */
    public static boolean detachFloorVisual(final int floorId) throws LocationServiceException {
        LOG.debug("Detach visual representation of floor {}, {}, {}", floorId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("update 'locations_floors' set 'image'=null where id=?")){
                prep.setInt(1, floorId);
                prep.executeUpdate();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not detach floor visuals: {}", ex.getMessage());
            throw new LocationServiceException("Could not detach floor visuals: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", floorId);
            }
        };
        ClientMessenger.send("LocationService","detachFloorVisual", 0, sendObject);
        return true;
    }
    
    public static boolean attachFloorVisual(final int floorId, final String floorImage) throws LocationServiceException {
        LOG.debug("Detach visual representation of floor {}, {}, {}", floorId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("update 'locations_floors' set 'image'=? where id=?")){
                prep.setString(1, floorImage);
                prep.setInt(2, floorId);
                prep.executeUpdate();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not attach floor visuals: {}", ex.getMessage());
            throw new LocationServiceException("Could not attach floor visuals: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", floorId);
                put("image", floorImage);
            }
        };
        ClientMessenger.send("LocationService","attachFloorVisual", 0, sendObject);
        return true;
    }
    
    /**
     * Removes any coordinates from the room used to visualize it.
     * @param roomId
     * @return
     * @throws LocationServiceException 
     */
    public static boolean detachRoomVisual(final int roomId) throws LocationServiceException {
        LOG.debug("Detach visual representation of location/room {}", roomId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("update 'locations' set 'screenX'=null,'screenY'=null,'screenW'=null,'screenH'=null where id=?")){
                prep.setInt(1, roomId);
                prep.executeUpdate();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not detach room visuals: {}", ex.getMessage());
            throw new LocationServiceException("Could not detach room visuals: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", roomId);
            }
        };
        ClientMessenger.send("LocationService","detachRoomVisual", 0, sendObject);
        return true;
    }
    
    public static boolean attachRoomVisual(final int roomId, final int x, final int y, final int w, final int h) throws LocationServiceException {
        LOG.debug("Attach visual representation of location/room {}, {}, {}, {}, {}", roomId,x,y,w,h);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try(PreparedStatement prep = fileDBConnection.prepareStatement("update 'locations' set 'screenX'=?,'screenY'=?,'screenW'=?,'screenH'=? where id=?")){
                prep.setInt(1, x);
                prep.setInt(2, y);
                prep.setInt(3, w);
                prep.setInt(4, h);
                prep.setInt(5, roomId);
                prep.executeUpdate();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not attach room visuals: {}", ex.getMessage());
            throw new LocationServiceException("Could not attach room visuals: " + ex.getMessage());
        }
        reloadLocations();
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", roomId);
                put("x", roomId);
                put("y", roomId);
                put("w", roomId);
                put("h", roomId);
            }
        };
        ClientMessenger.send("LocationService","attachRoomVisual", 0, sendObject);
        return true;
    }

}