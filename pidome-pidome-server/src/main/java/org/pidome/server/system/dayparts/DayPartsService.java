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

package org.pidome.server.system.dayparts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.shareddata.SharedDataStatusSetter;
import org.pidome.server.connector.shareddata.SharedDataStatusSetterInterface;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public final class DayPartsService implements ServiceInterface,SharedDataStatusSetterInterface {
    
    static ArrayList<DayPart> daypartsList = new ArrayList();
    
    static DayPart currentDayPart;
    
    static Logger LOG = LogManager.getLogger(DayPartsService.class);
    
    static ObjectPropertyBindingBean daypart =   new ObjectPropertyBindingBean();
    static ObjectPropertyBindingBean daypartId = new ObjectPropertyBindingBean();
    
    boolean started = false;
    
    public DayPartsService() throws DayPartException{
        reloadDayParts();
    }
    
    static void reloadDayParts() throws DayPartException{
        daypartsList.clear();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (Statement statementEvents = fileDBConnection.createStatement()) {
                try (ResultSet rsEvents = statementEvents.executeQuery("SELECT d.id, "
                        + "d.name, "
                        + "d.description, "
                        + "d.fixed "
                        + "FROM dayparts d")) {
                    while (rsEvents.next()) {
                        LOG.debug("Loading day part: " + rsEvents.getInt("id") + ", name " + rsEvents.getString("name") + ", description: " + rsEvents.getString("description"));
                        daypartsList.add(new DayPart(rsEvents.getInt("id"),rsEvents.getString("name"),rsEvents.getString("description"), rsEvents.getBoolean("fixed")));
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading dayparts {}", ex.getMessage());
            throw new DayPartException("Could not load dayparts " + ex.getMessage());
        }
    }
    
    /**
     * Returns a list of presences.
     * @return 
     */
    public static ArrayList<DayPart> getDayParts(){
        if(daypartsList.isEmpty()){
            try {
                reloadDayParts();
            } catch (DayPartException ex) {
                /// Wait for system load instead of early bird client load.
            }
        }
        return daypartsList;
    }
    
    /**
     * Returns the current presence.
     * @return
     * @throws DayPartException 
     */
    public static DayPart current() throws DayPartException{
        if(currentDayPart==null){
            throw new DayPartException("There is no daypart known, please wait for initial system update.");
        }
        return currentDayPart;
    }
    
    /**
     * Current user status text presentation property
     * @return 
     */
    public static ObjectPropertyBindingBean getCurrentDaypartTextProperty(){
        return daypart;
    }
    
    /**
     * Current user status text presentation property
     * @return 
     */
    public static ObjectPropertyBindingBean getCurrentDaypartIdProperty(){
        return daypartId;
    }
    
    /**
     * Returns a particular presence.
     * @param id
     * @return 
     * @throws org.pidome.server.system.dayparts.DayPartException 
     */
    public static DayPart getDayPart(int id) throws DayPartException{
        for(int i = 0; i < daypartsList.size(); i++){
            if (daypartsList.get(i).getId()==id){
                return daypartsList.get(i);
            }
        }
        throw new DayPartException("Unknown daypart id: " + id);
    }
    
    /**
     * Sets the current presence.
     * @param id
     * @return 
     * @throws DayPartException 
     */
    public static boolean setDayPart(final int id) throws DayPartException{
        currentDayPart = getDayPart(id);
        daypartId.setValue(id);
        currentDayPart.setLastActivated(TimeUtils.getCurrentTimeDate());
        LOG.info("Set daypart to {} at {}", currentDayPart.getName(),currentDayPart.getLastActivated());
        SharedDataStatusSetter.setNewDayPartStatus(id, currentDayPart.getName());
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", id);
                put("name", currentDayPart.getName());
            }
        };
        daypart.setValue(currentDayPart.getName());
        ClientMessenger.send("DayPartService","setDayPart", 0, sendObject);
        TriggerService.handleEvent("DAYPART", id);
        return true;
    }
    
    /**
     * Adds a presence to the database and loads it.
     * @param name
     * @param description 
     * @return 
     * @throws org.pidome.server.system.dayparts.DayPartException 
     */
    public static int addDayPart(String name, String description) throws DayPartException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            /// Save the macro
            try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into dayparts ('name', 'description') values (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, name);
                prep.setString(2, description);
                prep.execute();
                try (ResultSet generatedKeys = prep.getGeneratedKeys()){
                    if (generatedKeys.next()) {
                        final int presenceId = generatedKeys.getInt(1);
                        daypartsList.add(new DayPart(presenceId,name,description, false));
                        Map<String, Object> sendObject = new HashMap<String, Object>() {
                            {
                                put("id", presenceId);
                            }
                        };
                        ClientMessenger.send("DayPartService","addDayPart", 0, sendObject);
                        return presenceId;
                    } else {
                        reloadDayParts();
                        throw new SQLException("Creating new daypart succeeded, but no id");
                    }
                }
            }
        } catch (SQLException ex){
            LOG.error("Error adding new daypart: {}, {}, {}", name, description, ex.getMessage());
            throw new DayPartException("Error adding new daypart: " + name);
        }
    }
    
    /**
     * Updates a presence.
     * @param id
     * @param name
     * @param description 
     * @return  
     * @throws org.pidome.server.system.dayparts.DayPartException 
     */
    public static boolean updateDayPart(final Integer id, final String name, final String description) throws DayPartException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            /// Save the macro
            try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE dayparts set 'name'=?, 'description'=?,'modified'=datetime('now') WHERE id=?", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, name);
                prep.setString(2, description);
                prep.setInt(3, id);
                prep.executeUpdate();
                try {
                    if(daypartsList.contains(getDayPart(id))){
                        daypartsList.get(daypartsList.indexOf(getDayPart(id))).updateName(name);
                        daypartsList.get(daypartsList.indexOf(getDayPart(id))).updateDescription(description);
                    }
                } catch (DayPartException ex){
                    /// not found, reload list
                    reloadDayParts();
                }
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                        put("name", name);
                    }
                };
                ClientMessenger.send("DayPartService","updateDayPart", 0, sendObject);
                return true;
            }
        } catch (SQLException ex){
            LOG.error("Could not update daypart: {}, {}, {}, {}", id, name, description, ex.getMessage());
            throw new DayPartException("Could not update daypart: " + id);
        }
    }
    
    /**
     * Deletes a presence which is not fixed in the database.
     * @param id 
     * @return 
     * @throws org.pidome.server.system.dayparts.DayPartException 
     */
    public static boolean deleteDayPart(final int id) throws DayPartException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            /// Save the macro
            try (PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM dayparts WHERE id=? and fixed=0", Statement.RETURN_GENERATED_KEYS)) {
                prep.setInt(1, id);
                prep.executeUpdate();
                try {
                    daypartsList.remove(getDayPart(id));
                } catch (DayPartException x){
                    /// already removed
                }
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                    }
                };
                ClientMessenger.send("DayPartService","deleteDayPart", 0, sendObject);
                return true;
            }
        } catch (SQLException ex){
            LOG.error("Could not delete daypart: {}, {}", id, ex.getMessage());
            throw new DayPartException("Could not delete daypart: " + id);
        }
    }

    @Override
    public void interrupt() {
        /// not needed
        started = false;
    }

    @Override
    public void start() {
        /// initializtion is enough
        started = true;
    }

    @Override
    public boolean isAlive() {
        return started;
    }
    
    @Override
    public String getServiceName() {
        return "Day part service";
    }
    
}
