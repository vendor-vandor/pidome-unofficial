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

package org.pidome.server.system.userstatus;

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
public class UserStatusService implements ServiceInterface,SharedDataStatusSetterInterface {
    
    static ArrayList<UserStatus> userStatusList = new ArrayList();
    
    static UserStatus currentUserStatus;
    
    static ObjectPropertyBindingBean userStatusText = new ObjectPropertyBindingBean();
    static ObjectPropertyBindingBean userStatusId = new ObjectPropertyBindingBean();
    
    static Logger LOG = LogManager.getLogger(UserStatusService.class);
    
    boolean started = false;
    
    public UserStatusService() throws UserStatusException{
        reloadUserStatuses();
        setUserStatus(1);
    }
    
    /**
     * Reloads user statuses
     * @throws UserStatusException 
     */
    static void reloadUserStatuses() throws UserStatusException{
        userStatusList.clear();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (Statement statementEvents = fileDBConnection.createStatement()) {
                try (ResultSet rsEvents = statementEvents.executeQuery("SELECT u.id, "
                        + "u.name, "
                        + "u.description, "
                        + "u.fixed "
                        + "FROM userstatuses u")) {
                    while (rsEvents.next()) {
                        LOG.debug("Loading user status: " + rsEvents.getInt("id") + ", name " + rsEvents.getString("name") + ", description: " + rsEvents.getString("description"));
                        userStatusList.add(new UserStatus(rsEvents.getInt("id"),rsEvents.getString("name"),rsEvents.getString("description"),rsEvents.getBoolean("fixed")));
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading statusses {}", ex.getMessage());
            throw new UserStatusException("Could not load statusses: " + ex.getMessage());
        }
    }
    
    /**
     * Current user status text presentation property
     * @return 
     */
    public static ObjectPropertyBindingBean getCurrentUserStatusTextProperty(){
        return userStatusText;
    }
    
    /**
     * Current user status text presentation property
     * @return 
     */
    public static ObjectPropertyBindingBean getCurrentUserStatusIdProperty(){
        return userStatusId;
    }
    
    /**
     * Returns a list of users statuses.
     * @return 
     */
    public static ArrayList<UserStatus> getUserStatuses(){
        return userStatusList;
    }
    
    /**
     * Returns the current user status.
     * @return
     * @throws UserStatusException 
     */
    public static UserStatus current() throws UserStatusException{
        if(currentUserStatus==null){
            throw new UserStatusException("There is no user status known");
        }
        return currentUserStatus;
    }
    
    /**
     * Returns a particular user status.
     * @param id
     * @return 
     * @throws org.pidome.server.system.userstatus.UserStatusException 
     */
    public static UserStatus getUserStatus(int id) throws UserStatusException{
        for (UserStatus userstatusList : userStatusList) {
            if (userstatusList.getId() == id) {
                return userstatusList;
            }
        }
        throw new UserStatusException("Unknown user status id: " + id);
    }
    
    /**
     * Sets the current user status.
     * @param id
     * @return 
     * @throws UserStatusException 
     */
    public static boolean setUserStatus(final int id) throws UserStatusException{
        currentUserStatus = getUserStatus(id);
        userStatusId.setValue(id);
        currentUserStatus.setLastActivated(TimeUtils.getCurrentTimeDate());
        LOG.info("Set user status to {} at {}", currentUserStatus.getName(),currentUserStatus.getLastActivated());
        SharedDataStatusSetter.setNewUserStatus(id, currentUserStatus.getName());
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", id);
                put("name", currentUserStatus.getName());
            }
        };
        ClientMessenger.send("UserStatusService","setUserStatus", 0, sendObject);
        TriggerService.handleEvent("USERSTATUS", id);
        userStatusText.setValue(currentUserStatus.getName());
        return true;
    }
    
    /**
     * Adds a user status to the database and loads it.
     * @param name
     * @param description 
     * @return  
     * @throws org.pidome.server.system.userstatus.UserStatusException  
     */
    public static int addUserStatus(String name, String description) throws UserStatusException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into userstatuses ('name', 'description') values (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, name);
                prep.setString(2, description);
                prep.execute();
                try (ResultSet generatedKeys = prep.getGeneratedKeys()){
                    if (generatedKeys.next()) {
                        final int userStatusId = generatedKeys.getInt(1);
                        userStatusList.add(new UserStatus(userStatusId,name,description,false));
                        Map<String, Object> sendObject = new HashMap<String, Object>() {
                            {
                                put("id", userStatusId);
                            }
                        };
                        ClientMessenger.send("UserStatusService","addUserStatus", 0, sendObject);
                        return userStatusId;
                    } else {
                        reloadUserStatuses();
                        throw new SQLException("Creating new user status succeeded, but no id");
                    }
                }
            }
        } catch (SQLException ex){
            LOG.error("Error adding new user status: {}, {}, {}", name, description, ex.getMessage());
            throw new UserStatusException("Error adding new user status: " + name);
        }
    }
    
    /**
     * Updates a user status.
     * @param id
     * @param name
     * @param description 
     * @return 
     * @throws org.pidome.server.system.userstatus.UserStatusException 
     */
    public static boolean updateUserStatus(final Integer id, final String name, final String description) throws UserStatusException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE userstatuses set 'name'=?, 'description'=?, 'modified'=datetime('now') WHERE id=?", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, name);
                prep.setString(2, description);
                prep.setInt(3, id);
                prep.executeUpdate();
                try {
                    if(userStatusList.contains(getUserStatus(id))){
                        userStatusList.get(userStatusList.indexOf(getUserStatus(id))).updateName(name);
                        userStatusList.get(userStatusList.indexOf(getUserStatus(id))).updateDescription(description);
                    }
                } catch (UserStatusException ex){
                    /// not found, reload list
                    reloadUserStatuses();
                }
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                        put("name", name);
                    }
                };
                ClientMessenger.send("UserStatusService","updateUserStatus", 0, sendObject);
                if(currentUserStatus!=null && currentUserStatus.getId()==id){
                    setUserStatus(id);
                }
                return true;
            }
        } catch (SQLException ex){
            LOG.error("Could not update user status: {}, {}, {}, {}", id, name, description, ex.getMessage());
            throw new UserStatusException("Could not update user status: " + id);
        }
    }
    
    /**
     * Deletes a user status which is not fixed in the database.
     * @param id 
     * @return  
     * @throws org.pidome.server.system.userstatus.UserStatusException  
     */
    public static boolean deleteUserStatus(final int id) throws UserStatusException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM userstatuses WHERE id=? and fixed=0", Statement.RETURN_GENERATED_KEYS)) {
                prep.setInt(1, id);
                prep.executeUpdate();
                try {
                    userStatusList.remove(getUserStatus(id));
                } catch (UserStatusException x){
                    /// already removed
                }
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", id);
                    }
                };
                ClientMessenger.send("UserStatusService","deleteUserStatus", 0, sendObject);
                if(currentUserStatus!=null && currentUserStatus.getId()==id){
                    setUserStatus(1);
                }
                return true;
            }
        } catch (SQLException ex){
            LOG.error("Could not delete user status: {}, {}", id, ex.getMessage());
            throw new UserStatusException("Could not delete user status: " + id);
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
        return "User status service";
    }
    
}

