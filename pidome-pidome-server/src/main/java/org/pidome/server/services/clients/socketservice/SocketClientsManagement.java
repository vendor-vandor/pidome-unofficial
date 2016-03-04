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
package org.pidome.server.services.clients.socketservice;

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
import org.pidome.misc.utils.HashUtils;
import org.pidome.misc.utils.MiscImpl;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.db.DB;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 *
 * @author John
 */
public class SocketClientsManagement {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(SocketClientsManagement.class);
    
    static SimpleDateFormat fullDateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    
    /**
     * Updates last login time
     * @param client 
     * @return  
     * @throws org.pidome.server.services.clients.persons.PersonsManagementException 
     */
    protected static boolean updateLoginDateTime(SocketServiceClient client) throws PersonsManagementException {
        boolean result = false;
        String date = fullDateTimeFormatter.format(new Date());
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE fixedclients SET 'lastlogin'=? where id=?")) {
                prep.setString(1, date);
                prep.setInt(2, client.getId());
                result = true;
            } catch (Exception ex){
                LOG.error("Problem updating client {}", ex.getMessage());
                throw new PersonsManagementException("Could not update client: " + ex.getMessage());
            }
            client.setLastLogin(date);
        } catch (SQLException ex) {
            LOG.error("Problem updating client {}", ex.getMessage());
            throw new PersonsManagementException("Could not update client: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Updates a remote client.
     * @param clientId
     * @param password
     * @param roleset
     * @param ext
     * @return
     * @throws org.pidome.server.services.clients.socketservice.SocketClientsManagementException
     */
    public static boolean updateRemoteClient(int clientId, String password, HashMap<String,Object> roleset,boolean ext) throws SocketClientsManagementException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            if(password.equals("")){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE fixedclients SET 'roleset'=?,'ext'=?,'modified'=datetime('now') where id=?")) {
                    prep.setString(1, PidomeJSONRPCUtils.getParamCollection(roleset));
                    prep.setBoolean(2, ext);
                    prep.setInt(3, clientId);
                    prep.executeUpdate();
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client {}", ex.getMessage());
                    throw new SocketClientsManagementException("Could not update client: " + ex.getMessage());
                }
            } else {
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE fixedclients SET 'clientpass'=?,'roleset'=?,'ext'=?,'modified'=datetime('now') where id=?")) {
                    prep.setString(1, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(password)));
                    prep.setString(2, PidomeJSONRPCUtils.getParamCollection(roleset));
                    prep.setBoolean(3, ext);
                    prep.setInt(4, clientId);
                    prep.executeUpdate();
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client {}", ex.getMessage());
                    throw new SocketClientsManagementException("Could not update client: " + ex.getMessage());
                }
            }
            SocketServiceClient disconnect = null;
            for(SocketServiceClient client:RemoteClientsConnectionPool.getConnectedDisplayClients()){
                if(client.getId()==clientId){
                    disconnect = client;
                }
            }
            if(disconnect!=null){
                disconnect.finish();
                RemoteClientsConnectionPool.removeClient(disconnect);
            }
        } catch (SQLException ex) {
            LOG.error("Problem updating client {}", ex.getMessage());
            throw new SocketClientsManagementException("Could not update client: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Deletes a remote client.
     * @param clientId
     * @return
     * @throws SocketClientsManagementException 
     */
    public static boolean deleteRemoteClient(int clientId) throws SocketClientsManagementException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM fixedclients WHERE id=?")) {
                prep.setInt(1, clientId);
                prep.executeUpdate();
                result = true;
            } catch (Exception ex){
                LOG.error("Problem deleting client {}", ex.getMessage());
                throw new SocketClientsManagementException("Could not delete client: " + ex.getMessage());
            }
            SocketServiceClient disconnect = null;
            for(SocketServiceClient client:RemoteClientsConnectionPool.getConnectedDisplayClients()){
                if(client.getId()==clientId){
                    disconnect = client;
                }
            }
            if(disconnect!=null){
                disconnect.finish();
                RemoteClientsConnectionPool.removeClient(disconnect);
            }
        } catch (SQLException ex) {
            LOG.error("Problem deleting client {}", ex.getMessage());
            throw new SocketClientsManagementException("Could not delete client: " + ex.getMessage());
        }
        return result;
    }
    
    /**
     * Adds a remote client.
     * @param username
     * @param password
     * @param roleset
     * @param ext
     * @return
     * @throws org.pidome.server.services.clients.socketservice.SocketClientsManagementException
     */
    public static boolean addRemoteClient(String username, String password, HashMap<String,Object> roleset,boolean ext) throws SocketClientsManagementException {
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            if((!password.equals("") && password.length()>7) && (!username.equals("") && username.length()>5)){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO fixedclients ('clientname', 'clientpass', 'roleset','ext') values (?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    prep.setString(1, username);
                    prep.setString(2, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(password)));
                    prep.setString(3, PidomeJSONRPCUtils.getParamCollection(roleset));
                    prep.setBoolean(4, ext);
                    prep.executeUpdate();
                    result = true;
                } catch (Exception ex){
                    LOG.error("Problem updating client {}", ex.getMessage());
                    throw new SocketClientsManagementException("Could not update client: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            LOG.error("Problem updating client {}", ex.getMessage());
            throw new SocketClientsManagementException("Could not update client: " + ex.getMessage());
        }
        return result;
    }
 
    /**
     * Loads a single person.
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketClientsManagementException 
     */
    public static List<Map<String,Object>> getClients() throws SocketClientsManagementException{
        List<Map<String,Object>> clients = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT * FROM fixedclients")) {
                try (ResultSet rsEvents = prep.executeQuery()) {
                    while (rsEvents.next()) {
                        Map<String,Object> client = new HashMap<>();
                        client.put("id", rsEvents.getInt("id"));
                        client.put("lastlogin", rsEvents.getString("lastlogin"));
                        client.put("clientname", rsEvents.getString("clientname"));
                        client.put("ext", rsEvents.getBoolean("ext"));
                        clients.add(client);
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading clients {}", ex.getMessage());
                    throw new SocketClientsManagementException("Problem loading clients: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading clients {}", ex.getMessage());
                throw new SocketClientsManagementException("Problem loading clients: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading clients {}", ex.getMessage());
            throw new SocketClientsManagementException("Problem loading clients: " + ex.getMessage());
        }
        return clients;
    }
    
    /**
     * Returns a single client.
     * @param clientId
     * @return
     * @throws SocketClientsManagementException 
     */
    public static Map<String,Object> getClient(int clientId) throws SocketClientsManagementException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("SELECT * FROM fixedclients WHERE id=? LIMIT 1")) {
            prep.setInt(1, clientId);
            try (ResultSet rsEvents = prep.executeQuery()) {
                if (rsEvents.next()) {
                    Map<String,Object> client = new HashMap<>();
                    client.put("id", rsEvents.getInt("id"));
                    client.put("lastlogin", rsEvents.getString("lastlogin"));
                    client.put("clientname", rsEvents.getString("clientname"));
                    client.put("roleset", new PidomeJSONRPC(rsEvents.getString("roleset"), false).getParsedObject());
                    try {
                        client.put("capabilities", new PidomeJSONRPC(rsEvents.getString("clientsettings"), false).getParsedObject());
                    } catch (NullPointerException | PidomeJSONRPCException ex) {
                        LOG.warn("No capabilities set for {}", client.get("clientname"));
                        client.put("capabilities", new HashMap<>());
                    }
                    client.put("ext", rsEvents.getBoolean("ext"));
                    return client;
                }
            } catch (SQLException ex){
                LOG.error("Problem loading client {}", ex.getMessage());
                throw new SocketClientsManagementException("Problem loading client: " + ex.getMessage());
            }
        } catch (SQLException | PidomeJSONRPCException ex) {
            LOG.error("Problem loading client {}", ex.getMessage());
            throw new SocketClientsManagementException("Problem loading client: " + ex.getMessage());
        }
        throw new SocketClientsManagementException("Client not found");
    }
    
    /**
     * Stores client's settings.
     * @param clientId
     * @param settings
     * @return 
     */
    public static boolean storeClientSettings(int clientId, Map<String,Object> settings){
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE fixedclients SET 'clientsettings'=? WHERE id=?")) {
                prep.setString(1, PidomeJSONRPCUtils.getParamCollection(settings));
                prep.setInt(2, clientId);
                prep.executeUpdate();
                result = true;
            } catch (Exception ex){
                LOG.error("Problem updating client settings {}", ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem updating client settings {}", ex.getMessage());
        }
        return result;
    }
    
    /**
     * Returns the client's settings
     * @param clientId
     * @return 
     */
    public static Map<String,Object> getClientSettings(int clientId) {
        Map<String,Object> settings = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
            try (PreparedStatement prep = fileDBConnection.prepareStatement("SELECT clientsettings FROM fixedclients WHERE id=? LIMIT 1")) {
                prep.setInt(1, clientId);
                try (ResultSet rsEvents = prep.executeQuery()) {
                    if (rsEvents.next()) {
                        settings = new PidomeJSONRPC(rsEvents.getString("clientsettings"), false).getParsedObject();
                    }
                } catch (Exception ex){
                    LOG.error("Problem loading client {}", ex.getMessage());
                    throw new SocketClientsManagementException("Problem loading client: " + ex.getMessage());
                }
            } catch (Exception ex){
                LOG.error("Problem loading client {}", ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading client {}", ex.getMessage());
        }
        return settings;
    }
    
}