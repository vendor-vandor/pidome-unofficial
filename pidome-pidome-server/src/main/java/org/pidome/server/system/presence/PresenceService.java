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

package org.pidome.server.system.presence;

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
import org.pidome.server.services.macros.MacroService;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.clients.persons.Person;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public final class PresenceService implements ServiceInterface,SharedDataStatusSetterInterface {
    
    static ArrayList<Presence> precenseList = new ArrayList();
    
    static Presence currentPresence;
    
    static ObjectPropertyBindingBean userPresence = new ObjectPropertyBindingBean();
    static ObjectPropertyBindingBean userPresenceId = new ObjectPropertyBindingBean();
    
    static Logger LOG = LogManager.getLogger(PresenceService.class);
    
    boolean started = false;
    
    public PresenceService() throws PresenceException{
        reloadPresences();
        setGlobalPresence(1);
    }
    
    static void reloadPresences() throws PresenceException{
        precenseList.clear();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (Statement statementEvents = fileDBConnection.createStatement()) {
                try (ResultSet rsEvents = statementEvents.executeQuery("SELECT p.id, "
                        + "p.name, "
                        + "p.description, "
                        + "p.fixed, "
                        + "p.macroid "
                        + "FROM presences p")) {
                    while (rsEvents.next()) {
                        LOG.debug("Loading presence: " + rsEvents.getInt("id") + ", name " + rsEvents.getString("name") + ", description: " + rsEvents.getString("description"));
                        precenseList.add(new Presence(rsEvents.getInt("id"),rsEvents.getString("name"),rsEvents.getString("description"),rsEvents.getInt("macroid"),rsEvents.getBoolean("fixed")));
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading precenses {}", ex.getMessage());
            throw new PresenceException("Could not load precenses: " + ex.getMessage());
        }
    }
    
    /**
     * Sets the presence for a particular person.
     * @param personId
     * @param presenceId
     * @return 
     * @throws org.pidome.server.system.presence.PresenceException 
     */
    public static boolean setPresence(int personId, int presenceId) throws PresenceException {
        try {
            Person person = PersonsManagement.getInstance().getPerson(personId);
            person.setPresence(presenceId);
            if(person.canSetGlobalPresence()){
                if(presenceId!=1 && PersonsManagement.getInstance().allPersonsNonPresenceCheck()){
                    activateGlobalPresence(2);
                } else if (presenceId==1 && PersonsManagement.getInstance().allPersonsPresenceCheck()){
                    activateGlobalPresence(1);
                }
            }
        } catch (PersonsManagementException ex) {
            LOG.error("Person unknown, can not send presence");
            throw new PresenceException("Person not found");
        }
        return true;
    }
    
    /**
     * Returns a list of presences.
     * @return 
     */
    public static ArrayList<Presence> getPresences(){
        return precenseList;
    }
    
    /**
     * Current user status text presentation property
     * @return 
     */
    public static ObjectPropertyBindingBean getCurrentPresenceTextProperty(){
        return userPresence;
    }
    
    /**
     * Current user status text presentation property
     * @return 
     */
    public static ObjectPropertyBindingBean getCurrentPresenceIdProperty(){
        return userPresenceId;
    }
    
    /**
     * Returns the current presence.
     * @return
     * @throws PresenceException 
     */
    public static Presence current() throws PresenceException{
        if(currentPresence==null){
            throw new PresenceException("There is no presence known");
        }
        return currentPresence;
    }
    
    /**
     * Returns a particular presence.
     * @param id
     * @return 
     * @throws org.pidome.server.system.presence.PresenceException 
     */
    public static Presence getPresence(int id) throws PresenceException{
        for (Presence precenseList1 : precenseList) {
            if (precenseList1.getId() == id) {
                return precenseList1;
            }
        }
        throw new PresenceException("Unknown presence id: " + id);
    }
    
    /**
     * Sets the current presence.
     * @param id
     * @return 
     * @throws PresenceException 
     */
    public static boolean setGlobalPresence(final int id) throws PresenceException{
        currentPresence = getPresence(id);
        userPresenceId.setValue(id);
        currentPresence.setLastActivated(TimeUtils.getCurrentTimeDate());
        LOG.info("Set presence to {} at {}", currentPresence.getName(),currentPresence.getLastActivated());
        SharedDataStatusSetter.setNewPresenceStatus(id, currentPresence.getName());
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", id);
                put("name", currentPresence.getName());
            }
        };
        ClientMessenger.send("PresenceService","activateGlobalPresence", 0, sendObject);
        TriggerService.handleEvent("PRESENCE", id);
        userPresence.setValue(currentPresence.getName());
        return true;
    }
    
    /**
     * Sets and activates a presence.
     * The difference between activate and set is that while the new presence is set, also a macro is executed if attached to it.
     * @param id
     * @return
     * @throws PresenceException 
     */
    public static boolean activateGlobalPresence(final int id) throws PresenceException{
        if(setGlobalPresence(id)){
            if(currentPresence.hasMacro()){
                MacroService.runMacro(currentPresence.getMacroId());
            }
        }
        return true;
    }
    
    /**
     * Adds a presence to the database and loads it.
     * @param name
     * @param description 
     * @param macroId 
     * @return  
     * @throws org.pidome.server.system.presence.PresenceException 
     */
    public static int addPresence(String name, String description, Integer macroId) throws PresenceException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            /// Save the macro
            try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into presences ('name', 'description', 'macroid') values (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, name);
                prep.setString(2, description);
                if(macroId==0){
                    prep.setNull(3, java.sql.Types.NULL);
                } else {
                    prep.setInt(3, macroId);
                }
                prep.execute();
                try (ResultSet generatedKeys = prep.getGeneratedKeys()){
                    if (generatedKeys.next()) {
                        final int presenceId = generatedKeys.getInt(1);
                        precenseList.add(new Presence(presenceId,name,description,macroId,false));
                        Map<String, Object> sendObject = new HashMap<String, Object>() {
                            {
                                put("id", presenceId);
                                put("name", presenceId);
                                put("description", presenceId);
                                put("lastactivated", "00-00-0000 00:00:00");
                            }
                        };
                        ClientMessenger.send("PresenceService","addPresence", 0, sendObject);
                        return presenceId;
                    } else {
                        reloadPresences();
                        throw new SQLException("Creating new presence succeeded, but no id");
                    }
                }
            }
        } catch (SQLException ex){
            LOG.error("Error adding new presence: {}, {}, {}", name, description, ex.getMessage());
            throw new PresenceException("Error adding new presence: " + name);
        }
    }
    
    /**
     * Updates a presence.
     * @param id
     * @param name
     * @param macroId
     * @param description 
     * @return  
     * @throws org.pidome.server.system.presence.PresenceException 
     */
    public static boolean updatePresence(final Integer id, final String name, final String description, final Integer macroId) throws PresenceException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            /// Save the macro
            try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE presences set 'name'=?, 'description'=?, 'macroid'=? ,'modified'=datetime('now') WHERE id=?", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, name);
                prep.setString(2, description);
                if(macroId==0){
                    prep.setNull(3, java.sql.Types.NULL);
                } else {
                    prep.setInt(3, macroId);
                }
                prep.setInt(4, id);
                prep.executeUpdate();
                try {
                    if(precenseList.contains(getPresence(id))){
                        precenseList.get(precenseList.indexOf(getPresence(id))).updateName(name);
                        precenseList.get(precenseList.indexOf(getPresence(id))).updateDescription(description);
                        precenseList.get(precenseList.indexOf(getPresence(id))).updateMacro(macroId);
                    }
                } catch (PresenceException ex){
                    /// not found, reload list
                    reloadPresences();
                }
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                        put("name", name);
                        put("description", name);
                    }
                };
                ClientMessenger.send("PresenceService","updatePresence", 0, sendObject);
                if(currentPresence!=null && currentPresence.getId()==id){
                    setGlobalPresence(id);
                }
                return true;
            }
        } catch (SQLException ex){
            LOG.error("Could not update presence: {}, {}, {}, {}", id, name, description, ex.getMessage());
            throw new PresenceException("Could not update presence: " + id);
        }
    }
    
    /**
     * Deletes a presence which is not fixed in the database.
     * @param id 
     * @return  
     * @throws org.pidome.server.system.presence.PresenceException 
     */
    public static boolean deletePresence(final int id) throws PresenceException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            /// Save the macro
            try (PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM presences WHERE id=? and fixed=0", Statement.RETURN_GENERATED_KEYS)) {
                prep.setInt(1, id);
                prep.executeUpdate();
                try {
                    precenseList.remove(getPresence(id));
                } catch (PresenceException x){
                    /// already removed
                }
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                    }
                };
                ClientMessenger.send("PresenceService","deletePresence", 0, sendObject);
                if(currentPresence!=null && currentPresence.getId()==id){
                    setGlobalPresence(1);
                }
                return true;
            }
        } catch (SQLException ex){
            LOG.error("Could not delete presence: {}, {}", id, ex.getMessage());
            throw new PresenceException("Could not delete presence: " + id);
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
        return "Presence service";
    }
    
}
