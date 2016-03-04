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
package org.pidome.server.services.clients.persons;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.HashUtils;
import org.pidome.misc.utils.MiscImpl;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.tools.MathImpl;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.system.db.DB;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 *
 * @author John
 */
public final class PersonsManagement {
    
    static Logger LOG = LogManager.getLogger(PersonsManagement.class);
    
    private static List<Person> persons = new ArrayList<>();
    
    private static PersonsManagement me;
    
    static SimpleDateFormat fullDateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    
    private PersonsManagement(){
        me = this;
    }
    
    /**
     * Returns the instance.
     * @return 
     */
    public static PersonsManagement getInstance() {
        if(me==null){
            me = new PersonsManagement();
            try {
                me.initialize();
            } catch (PersonsManagementException ex){
                LOG.error("Could not initialize persons, login not possible: {}", ex.getMessage());
            }
        }
        return me;
    }
    
    /**
     * Checks if all the persons are present.
     * @return 
     */
    public final boolean allPersonsPresenceCheck(){
        for(Person person:persons){
            if(!person.getLoginName().equals("admin")){
                if(person.getIfPresent()){
                    return true;
		}
            }
        }
        return false;
    }
    
    /**
     * Checks if all the persons are non present.
     * @return 
     */
    public final boolean allPersonsNonPresenceCheck(){
        for(Person person:persons){
            if(!person.getLoginName().equals("admin")){
                if(person.getIfPresent()){
                    return false;
		}
            }
        }
        return true;
    }
    
    /**
     * Checks if everyone is out of GPS bounds.
     * @return 
     */
    protected final boolean allPersonsOutOfBounds(){
        for(Person person:persons){
            if(!person.getLoginName().equals("admin")){
                if(!person.isOutSideGPSBoundary()){
                    return false;
		}
            }
        }
        return true;
    }
    
    /**
     * Initializes the living!
     * @throws PersonsManagementException 
     */
    private void initialize() throws PersonsManagementException {
        loadPersons();
    }
    
    /**
     * Updates last login time
     * @param person 
     * @return  
     * @throws org.pidome.server.services.clients.persons.PersonsManagementException 
     */
    protected static boolean updateLoginDateTime(Person person) throws PersonsManagementException {
        boolean result = false;
        String date = fullDateTimeFormatter.format(new Date());
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE persons SET 'lastlogin'=? where id=?")) {
                prep.setString(1, date);
                prep.setInt(2, person.getId());
                result = true;
            } catch (Exception ex){
                LOG.error("Problem updating client {}", ex.getMessage());
                throw new PersonsManagementException("Could not update client: " + ex.getMessage());
            }
            person.setLastLogin(date);
        } catch (SQLException ex) {
            LOG.error("Problem updating client {}", ex.getMessage());
            throw new PersonsManagementException("Could not update client: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Returns a list of persons.
     * @return
     * @throws PersonsManagementException 
     */
    private List<Map<String,Object>> loadPersons() throws PersonsManagementException{
        List<Map<String,Object>> clientsList = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (Statement statementEvents = fileDBConnection.createStatement()) {
                try (ResultSet rsEvents = statementEvents.executeQuery("SELECT * FROM persons WHERE (clienttype='USER' OR clienttype='ADMIN')")) {
                    while (rsEvents.next()) {
                        Person person = new Person(rsEvents.getInt("id"), 
                                                   rsEvents.getString("lastlogin"), 
                                                   rsEvents.getString("clientname"),
                                                   rsEvents.getString("firstname"),
                                                   rsEvents.getString("lastname"));
                        
                        person.setIfCpwd(rsEvents.getBoolean("cpwd"));
                        person.setIfExternal(rsEvents.getBoolean("ext"));
                        person.setReadOnly(rsEvents.getBoolean("fixed"));
                        person.setPresence(rsEvents.getInt("presence"));
                        try {
                            person.createRoleSet(new PidomeJSONRPC(rsEvents.getString("roleset"), false).getParsedObject());
                        } catch (SQLException | PidomeJSONRPCException ex){
                            LOG.error("Could not create role for {}", person.getLoginName());
                        }
                        persons.add(person);
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading persons list {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load persons: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading persons list {}", ex.getMessage());
                throw new PersonsManagementException("Could not load persons: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading persons list {}", ex.getMessage());
            throw new PersonsManagementException("Could not load persons: " + ex.getMessage());
        }
        return clientsList;
    }
    
    /**
     * Loads a single person.
     * @param personId
     * @throws PersonsManagementException 
     */
    private void loadSinglePerson(int personId) throws PersonsManagementException{
        for(Person person: persons){
            if(person.getId()==personId){
                throw new PersonsManagementException("Person already loaded: " + personId);
            }
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT * FROM persons WHERE (clienttype='USER' OR clienttype='ADMIN') AND id=? LIMIT 1")) {
                prep.setInt(1, personId);
                try (ResultSet rsEvents = prep.executeQuery()) {
                    if (rsEvents.next()) {
                        Person person = new Person(rsEvents.getInt("id"), 
                                                   rsEvents.getString("lastlogin"), 
                                                   rsEvents.getString("clientname"),
                                                   rsEvents.getString("firstname"),
                                                   rsEvents.getString("lastname"));
                        
                        person.setIfCpwd(rsEvents.getBoolean("cpwd"));
                        person.setIfExternal(rsEvents.getBoolean("ext"));
                        person.setReadOnly(rsEvents.getBoolean("fixed"));
                        try {
                            person.createRoleSet(new PidomeJSONRPC(rsEvents.getString("roleset"), false).getParsedObject());
                        } catch (SQLException | PidomeJSONRPCException ex){
                            LOG.error("Could not create role for {}", person.getLoginName());
                        }
                        persons.add(person);
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading bound devices for client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem bound devices for  client {}", ex.getMessage());
                throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem bound devices for  client {}", ex.getMessage());
            throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
        }
    }
    
    /**
     * Returns a list of persons.
     * @return
     * @throws PersonsManagementException 
     */
    public final List<Person> getPersons() throws PersonsManagementException {
        return persons;
    }
    
    /**
     * Returns a client.
     * @param personId
     * @return
     * @throws PersonsManagementException 
     */
    public final Person getPerson(int personId) throws PersonsManagementException {
        for(Person person: persons){
            if(person.getId()==personId){
                return person;
            }
        }
        throw new PersonsManagementException("Person not found: " + personId);
    }
    
    /**
     * Returns a client by connection link.
     * @param client
     * @return
     * @throws PersonsManagementException 
     */
    public final Person getPersonByRemoteClient(RemoteClient client) throws PersonsManagementException {
        for(Person person: persons){
            if(person.containsClient(client)){
                return person;
            }
        }
        throw new PersonsManagementException("Person not found for link: " + client.getClientName());
    }
    
    /**
     * Removes a person.
     * @param personId
     * @return
     * @throws PersonsManagementException 
     */
    public final boolean removePerson(int personId) throws PersonsManagementException {
        boolean result = false;
        persons.remove(getPerson(personId));
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM persons WHERE id=? AND clientname!='admin'")) {
                prep.setInt(1, personId);
                prep.executeUpdate();
            } catch (Exception ex){
                LOG.error("Problem removing person {}", ex.getMessage());
                throw new PersonsManagementException("Problem removing person: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem removing person {}", ex.getMessage());
            throw new PersonsManagementException("Problem removing person: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Updates a remote client.
     * @param personId The id of the person to update.
     * @param password The new password if given.
     * @param firstname The first name.
     * @param lastname The last name
     * @param cpwd If a password must be changed or not.
     * @param roleset The known roles
     * @param ext If someone may connect from external.
     * @param gpsdevices Available GPS enabled devices.
     * @return
     * @throws PersonsManagementException 
     */
    public final boolean updatePerson(int personId, String password, String firstname,String lastname, boolean cpwd,HashMap<String,Object> roleset,boolean ext,List<Map<String,Object>> gpsdevices) throws PersonsManagementException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            if(password.equals("")){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE persons SET 'firstname'=?,'lastname'=?,'cpwd'=?,'roleset'=?,'ext'=?,'modified'=datetime('now') where id=?")) {
                    prep.setString(1, firstname);
                    prep.setString(2, lastname);
                    prep.setBoolean(3, cpwd);
                    prep.setString(4, PidomeJSONRPCUtils.getParamCollection(roleset));
                    prep.setBoolean(5, ext);
                    prep.setInt(6, personId);
                    prep.executeUpdate();
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not update client: " + ex.getMessage());
                }
            } else {
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE persons SET 'clientpass'=?,'firstname'=?,'lastname'=?,'cpwd'=?,'roleset'=?,'ext'=?,'modified'=datetime('now') where id=?")) {
                    prep.setString(1, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(password)));
                    prep.setString(2, firstname);
                    prep.setString(3, lastname);
                    prep.setBoolean(4, cpwd);
                    prep.setString(5, PidomeJSONRPCUtils.getParamCollection(roleset));
                    prep.setBoolean(6, ext);
                    prep.setInt(7, personId);
                    prep.executeUpdate();
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not update client: " + ex.getMessage());
                }
            }
            updateGPSEnabledDevices(personId, gpsdevices,fileDBConnection);
            Person person = getPerson(personId);
            person.setFirstName(firstname);
            person.setLastName(lastname);
            person.setIfCpwd(cpwd);
            person.setIfExternal(ext);
            person.createRoleSet(roleset);
        } catch (SQLException ex) {
            LOG.error("Problem updating client {}", ex.getMessage());
            throw new PersonsManagementException("Could not update client: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Enable or disable GPS for a set of devices.
     * @param clientId
     * @param gpsdevices
     * @param fileDBConnection 
     */
    private void updateGPSEnabledDevices(int clientId, List<Map<String,Object>> gpsdevices, Connection fileDBConnection){
        if(clientId!=1 && gpsdevices!=null){
            for(Map<String,Object> deviceInfo:gpsdevices){
                if(deviceInfo.containsKey("gpsenabled") && deviceInfo.containsKey("device")){
                    boolean throttled = false;
                    if(deviceInfo.containsKey("throttled")){
                        throttled = (boolean)deviceInfo.get("throttled");
                    }
                    try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE clients_linked SET gps=?,throttled=? WHERE id=? AND binding=?")) {
                        prep.setBoolean(1, (boolean)deviceInfo.get("gpsenabled"));
                        prep.setBoolean(2, throttled);
                        prep.setInt(3, ((Number)deviceInfo.get("device")).intValue());
                        prep.setInt(4, clientId);
                        prep.executeUpdate();
                    } catch (Exception ex){
                        LOG.error("Problem updating linked device {}", ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Updates a password.
     * @param clientId
     * @param oldPass
     * @param newPass
     * @param newPassAgain
     * @return
     * @throws PersonsManagementException 
     */
    public final boolean updatePersonPassword(int clientId, String oldPass, String newPass, String newPassAgain) throws PersonsManagementException {
        boolean result = false;
        if(!newPass.equals(newPassAgain)){
            throw new PersonsManagementException("New passwords doe not match");
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            if(!newPass.equals("")){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE persons SET 'clientpass'=?,'modified'=datetime('now'),'cpwd'=0 where (id=? and clientpass=?)")) {
                    prep.setString(1, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(newPass)));
                    prep.setInt(2, clientId);
                    prep.setString(3, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(oldPass)));
                    prep.executeUpdate();
                    getPerson(clientId).setIfCpwd(false);
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client's password: {}", ex.getMessage());
                    throw new PersonsManagementException("Could not update client's password: " + ex.getMessage());
                }
            } else {
                throw new PersonsManagementException("New password can not be empty");
            }
        } catch (Exception ex){
            LOG.error("Problem updating client's password: {}", ex.getMessage());
            throw new PersonsManagementException("Could not update user password.");
        }
        return result;
    }
    
    /**
     * Adds a person.
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
    public final boolean addPerson(String username, String password, String firstname,String lastname, boolean cpwd,HashMap<String,Object> roleset,boolean ext) throws PersonsManagementException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            if((!password.equals("") && password.length()>7) && (!username.equals("") && username.length()>5)){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO persons ('clientname', 'clientpass', 'firstname','lastname','cpwd','roleset','ext','clienttype','fixed') values (?,?,?,?,?,?,?,?,0)",Statement.RETURN_GENERATED_KEYS)) {
                    prep.setString(1, username);
                    prep.setString(2, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(password)));
                    prep.setString(3, firstname);
                    prep.setString(4, lastname);
                    prep.setBoolean(5, cpwd);
                    prep.setString(6, PidomeJSONRPCUtils.getParamCollection(roleset));
                    prep.setBoolean(7, ext);
                    prep.setString(8, "USER");
                    prep.executeUpdate();
                    try (ResultSet rs = prep.getGeneratedKeys()) {
                        if (rs.next()) {
                            loadSinglePerson(rs.getInt(1));
                        }
                    }
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not update client: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            LOG.error("Problem updating client {}", ex.getMessage());
            throw new PersonsManagementException("Could not update client: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Updates a clients location.
     * @param remoteClient
     * @param reportingDevice
     * @param latitude
     * @param longitude 
     * @param accuracy 
     * @return  
     */
    public final float updateLocation(RemoteClientInterface remoteClient, RemoteClient reportingDevice, float latitude, float longitude, float accuracy){
        float result = 0.0f;
        if(remoteClient instanceof Person){
            result = Math.round(MathImpl.GeoDistance(TimeUtils.getCurrentLatitude(), TimeUtils.getCurrentLongitude(), latitude, longitude));
            ((Person)remoteClient).setDistance(reportingDevice, result, latitude, longitude, accuracy);
        }
        return result;
    }
    
    /**
     * Returns a list of client bindings.
     * @param clientId
     * @return
     * @throws PersonsManagementException 
     */
    public final List<Map<String,Object>> getRemoteClientBindings(int clientId) throws PersonsManagementException {
        List<Map<String,Object>> clientsList = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT * FROM clients_linked WHERE binding=?")) {
                prep.setInt(1, clientId);
                try (ResultSet rsEvents = prep.executeQuery()) {
                    while (rsEvents.next()) {
                        Map<String,Object> clientInfo = new HashMap<>();
                        clientInfo.put("id", rsEvents.getInt("id"));
                        clientInfo.put("devicelogin", rsEvents.getString("devicelogin"));
                        clientInfo.put("deviceinfo", rsEvents.getString("deviceinfo"));
                        clientInfo.put("created", rsEvents.getString("created"));
                        clientInfo.put("gpsenabled", rsEvents.getBoolean("gps"));
                        clientInfo.put("throttled", rsEvents.getBoolean("throttled"));
                        try {
                            clientInfo.put("capabilities", new PidomeJSONRPC(rsEvents.getString("clientsettings"), false).getParsedObject());
                        } catch (NullPointerException | PidomeJSONRPCException ex) {
                            LOG.warn("No capabilities set for {}", clientInfo.get("deviceinfo"));
                            clientInfo.put("capabilities", new HashMap<>());
                        }
                        clientsList.add(clientInfo);
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading bound devices for client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem bound devices for  client {}", ex.getMessage());
                throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem bound devices for  client {}", ex.getMessage());
            throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
        }
        return clientsList;
    }
    
    
    /**
     * Returns a list of client bindings.
     * @param clientId
     * @return
     * @throws PersonsManagementException 
     */
    public final Map<String,Object> getRemoteClientBinding(int clientId) throws PersonsManagementException {
        Map<String,Object> clientInfo = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("SELECT * FROM clients_linked WHERE id=?")) {
            prep.setInt(1, clientId);
            try (ResultSet rsEvents = prep.executeQuery()) {
                if (rsEvents.next()) {
                    clientInfo.put("id", rsEvents.getInt("id"));
                    clientInfo.put("devicelogin", rsEvents.getString("devicelogin"));
                    clientInfo.put("deviceinfo", rsEvents.getString("deviceinfo"));
                    clientInfo.put("created", rsEvents.getString("created"));
                    clientInfo.put("gpsenabled", rsEvents.getBoolean("gps"));
                    clientInfo.put("throttled", rsEvents.getBoolean("throttled"));
                    try {
                        clientInfo.put("capabilities", new PidomeJSONRPC(rsEvents.getString("clientsettings"), false).getParsedObject());
                    } catch (NullPointerException | PidomeJSONRPCException ex) {
                        LOG.warn("No capabilities set for {}", clientInfo.get("deviceinfo"));
                        clientInfo.put("capabilities", new HashMap<>());
                    }
                } else {
                    throw new PersonsManagementException("No bound device found");
                }
            } catch (Exception ex){
                LOG.error("Problem bound devices for client {}", ex.getMessage());
                throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem bound devices for  client {}", ex.getMessage());
            throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
        }
        return clientInfo;
    }
    
    
    /**
     * Returns a list of client bindings.
     * @param clientDeviceId
     * @return
     * @throws PersonsManagementException 
     */
    public final String removeRemoteClientBinding(int clientDeviceId) throws PersonsManagementException {
        String clientName = null;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT devicelogin FROM clients_linked WHERE id=?")) {
                prep.setInt(1, clientDeviceId);
                try (ResultSet rsEvents = prep.executeQuery()) {
                    if (rsEvents.next()) {
                        clientName = rsEvents.getString("devicelogin");
                    } else {
                        LOG.error("No bound devices found for client: {}", clientDeviceId);
                        throw new PersonsManagementException("No bound devices found for client: " + clientDeviceId);
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading bound devices for client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load bound devices for client: " + ex.getMessage());
                }
            }
            try (PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM clients_linked WHERE id=?")) {
                prep.setInt(1, clientDeviceId);
                prep.executeUpdate();
            } catch (Exception ex){
                LOG.error("Problem removing bound device for client {}", ex.getMessage());
                throw new PersonsManagementException("Could not remove bound device for client: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem removing bound device for client {}", ex.getMessage());
            throw new PersonsManagementException("Could not remove bound device for client: " + ex.getMessage());
        }
        if(clientName!=null){
            return clientName;
        } else {
            LOG.error("No bound devices found for client: {}", clientDeviceId);
            throw new PersonsManagementException("No bound devices found for client: " + clientDeviceId);   
        }
    }
    
    /**
     * Returns the client roleset from the DB.
     * @param clientName
     * @return
     * @throws PersonsManagementException 
     */
    public final Map<String,Object> getRemoteClientRoleByName(String clientName) throws PersonsManagementException {
        Map<String,Object> clientInfo = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT roleset FROM persons WHERE clientname=? AND (clienttype='USER' OR clienttype='ADMIN') LIMIT 1")) {
                prep.setString(1, clientName);
                try (ResultSet rsEvents = prep.executeQuery()) {
                    if (rsEvents.next()) {
                        clientInfo = new PidomeJSONRPC(rsEvents.getString("roleset"), false).getParsedObject();
                        LOG.debug("Loaded roleset for {}: {}", clientName, clientInfo);
                    } else {
                        LOG.error("Problem loading role for client {}, client not found", clientName);
                        throw new PersonsManagementException("Could not load role for client " + clientName + ", client not found");
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading role for client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load role for client: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading role for client {}", ex.getMessage());
                throw new PersonsManagementException("Could not load role for client: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading role for client {}", ex.getMessage());
            throw new PersonsManagementException("Could not load role for client: " + ex.getMessage());
        }
        return clientInfo;
    }
    
    /**
     * Binds a remote client to an user.
     * @param client
     * @param userId
     * @return
     * @throws PersonsManagementException 
     */
    public final boolean storeClientLink(RemoteClient client, int userId) throws PersonsManagementException {
        boolean found = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT devicelogin FROM clients_linked WHERE devicelogin=? LIMIT 1")) {
                prep.setString(1, client.getClientName());
                try (ResultSet rsEvents = prep.executeQuery()) {
                    while (rsEvents.next()) {
                        found = true;
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading device check {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load device to check existence: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading device check {}", ex.getMessage());
                throw new PersonsManagementException("Could not load device to check existence: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading device check {}", ex.getMessage());
            throw new PersonsManagementException("Could not load device to check existence: " + ex.getMessage());
        }
        if(found){
            throw new PersonsManagementException("Device already exists, remove first");
        } else {
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO clients_linked ('devicelogin','deviceinfo','binding') values (?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    prep.setString(1, client.getClientName());
                    prep.setString(2, client.getClientInfo());
                    prep.setInt(3, userId);
                    prep.execute();
                    try (ResultSet rs = prep.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new PersonsManagementException("Error storing link in database.");
                        }
                    }
                } catch (Exception ex){
                    LOG.error("Problem linking {} to user {}: {}", client.getClientName(), userId, ex.getMessage());
                    throw new PersonsManagementException("Could not link device to user " + ex.getMessage());
                }
            } catch (SQLException ex) {
                LOG.error("Problem linking {} to user {}: {}", client.getClientName(), userId, ex.getMessage());
                throw new PersonsManagementException("Could not link device to user " + ex.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Checks if a remote client exists and returns to which user it is bound to.
     * @param client
     * @return
     * @throws PersonsManagementException 
     */
    public final LinkedClientInfo getClientLink(RemoteClient client) throws PersonsManagementException {
        final LinkedClientInfo info = new LinkedClientInfo();
        String canExt = "";
        try {
            if(!InetAddress.getByName( client.getRemoteHost()).isSiteLocalAddress()){
                canExt = " AND c.ext=1 ";
            }
        } catch (UnknownHostException ex) {
            LOG.error("Could not check if address {} is external or not. Defaulting to local only", client.getRemoteHost());
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT cl.id,cl.binding,cl.gps,cl.throttled FROM clients_linked cl "
                                                                          + "INNER JOIN persons c ON cl.binding=c.id "
                                                                          + "WHERE cl.devicelogin=? "+canExt+" LIMIT 1")) {
                prep.setString(1, client.getClientName());
                try (ResultSet rs = prep.executeQuery()) {
                    if (rs.next()) {
                        info.setHasGPSEnabled(rs.getBoolean("gps"));
                        info.setIsThrottled(rs.getBoolean("throttled"));
                        info.setResourceId(rs.getInt("id"));
                        info.setBindId(rs.getInt("binding"));
                        return info;
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading device check {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load device to check existence: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading device check {}", ex.getMessage());
                throw new PersonsManagementException("Could not load device to check existence: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading device check {}", ex.getMessage());
            throw new PersonsManagementException("Could not load device to check existence: " + ex.getMessage());
        }
        throw new PersonsManagementException("Device '"+client.getClientName()+"' not found");
    }
    
    /**
     * Returns role based on the RemoteClient that logged in.
     * @param client
     * @return
     * @throws PersonsManagementException 
     */
    public final Map<String,Object> getRemoteClientRoleByRemoteClient(RemoteClient client) throws PersonsManagementException {
        String canExt = "";
        try {
            if(!InetAddress.getByName( client.getRemoteHost()).isSiteLocalAddress()){
                canExt = " AND c.ext=1 ";
            }
        } catch (UnknownHostException ex) {
            LOG.error("Could not check if address {} is external or not. Defaulting to local only", client.getRemoteHost());
        }
        Map<String,Object> clientInfo = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT c.roleset FROM persons c "
                                                                          + "INNER JOIN clients_linked cs ON cs.binding=c.id "
                                                                          + "WHERE cs.devicelogin=? AND (c.clienttype='USER' OR c.clienttype='ADMIN') "+canExt+" LIMIT 1")) {
                prep.setString(1, client.getClientName());
                try (ResultSet rsEvents = prep.executeQuery()) {
                    if (rsEvents.next()) {
                        clientInfo = new PidomeJSONRPC(rsEvents.getString("roleset"), false).getParsedObject();
                        LOG.debug("Loaded roleset for {}: {}", client.getClientName(), clientInfo);
                    } else {
                        LOG.error("Problem loading role for client {}, client not found", client.getClientName());
                        throw new PersonsManagementException("Could not load role for client " + client.getClientName() + ", client not found");
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading client {}", ex.getMessage());
                    throw new PersonsManagementException("Could not load client: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading client {}", ex.getMessage());
                throw new PersonsManagementException("Could not load client: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading client {}", ex.getMessage());
            throw new PersonsManagementException("Could not load client: " + ex.getMessage());
        }
        return clientInfo;
    }
    
    public final void sendAll(String nameSpace, int locationId, String msg){
        Runnable run = () -> {
            persons.stream().filter((person) -> (person.getRole().hasLocationAccess(locationId) && person.getRole().hasNameSpaceAccess(nameSpace))).forEach((person) -> {
                person.sendMessage(nameSpace, msg);
            });
        };
        run.run();
    }
    
    public final void sendAll(String nameSpace, int locationId, String msg, int except){
        Runnable run = () -> {
            for(Person person:persons){
                if(person.getId()!= except && person.getRole().hasLocationAccess(locationId) && person.getRole().hasNameSpaceAccess(nameSpace)){
                    person.sendMessage(nameSpace, msg);
                }
            }
        };
        run.run();
    }
    
    public static class LinkedClientInfo {
        
        private boolean gps;
        private boolean throttled;
        private int boundId;
        private int resourceId;
        
        private LinkedClientInfo(){}
        
        private void setHasGPSEnabled(boolean gps){
            this.gps = gps;
        }
        
        private void setIsThrottled(boolean throttled){
            this.throttled = throttled;
        }
        
        private void setBindId(int boundId){
            this.boundId = boundId;
        }
        
        private void setResourceId(int resourceId){
            this.resourceId = resourceId;
        }
        
        public final boolean getHasGPS(){
            return this.gps;
        }
        
        public final boolean getIsThrottled(){
            return this.throttled;
        }
        
        public final int getBindId(){
            return this.boundId;
        }
        
        public final int getResourceId(){
            return this.resourceId;
        }
        
    }
    
    
}