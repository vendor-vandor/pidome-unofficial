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

package org.pidome.server.services.clients.remoteclient;

import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.services.clients.persons.PersonsManagement;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.HashUtils;
import org.pidome.misc.utils.MiscImpl;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.system.network.http.HttpClientLoggedInOnOtherLocationException;
import org.pidome.server.services.clients.socketservice.SocketServiceClient;
import org.pidome.server.services.http.rpc.PiDomeJSONRPCAuthentificationParameters;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.db.DB;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 *
 * @author John
 */
public class RemoteClientsAuthentication {
    
    public static enum EndPoint {
      RAW,
      RPC,
      WEB,
      WSOCKET,
      MQTT
    }
    
    public static enum AuthResult {
        OK,
        FAILED,
        WAIT
    }
    
    static Logger LOG = LogManager.getLogger(RemoteClientsAuthentication.class);
    
    static Map<String, RemoteClient> clients = new HashMap<>();
    
    /**
     * Authenticates a client.
     * @param client
     * @param authTokens
     * @return
     * @throws AuthenticationException 
     */
    protected static AuthResult authenticate(RemoteClient client, PiDomeJSONRPCAuthentificationParameters authTokens) throws AuthenticationException {
        switch(client.getType()){
            case WEB:
                try {
                    return authenticateWebClient(client, authTokens.getUsername(), authTokens.getPassword(), false);
                } catch (HttpClientLoggedInOnOtherLocationException | RemoteClientException ex) {
                    java.util.logging.Logger.getLogger(RemoteClientsAuthentication.class.getName()).log(Level.SEVERE, null, ex);
                }
            case SOCKET:
                return authenticateSocketClient((SocketServiceClient)client, authTokens);
            case MQTT:
                return authenticateMQTTClient(client, authTokens);
            case WEBSOCKET:
                return authenticateWebsocketClient(client, authTokens);
            default:
                return AuthResult.FAILED;
        }
    }
    
    /**
     * Returns if a client is authorized
     * @param client
     * @return 
     */
    public static boolean isAuthorized(RemoteClient client){
        return clients.containsKey(client.getKey());
    }
    
    /**
     * Returns if a client has an endpoint assigned.
     * @param client
     * @param endPoint
     * @return 
     */
    protected static boolean endPointAllowed(RemoteClient client, EndPoint endPoint){
        return client.hasEndpoint(endPoint);
    }
    
    /**
     * Returns web authentication.
     * @param client The remote client.
     * @param userName The username used
     * @param password The password used
     * @param override if an already logged in exception should be overridden.
     * @return The authentication result.
     * @throws AuthenticationException on system failure.
     * @throws org.pidome.server.system.network.http.HttpClientLoggedInOnOtherLocationException When an user is already logged in on other location.
     * @throws org.pidome.server.services.clients.remoteclient.RemoteClientException When there is no remote client instance.
     */
    public static AuthResult authenticateWebClient(final RemoteClient client, String userName, String password, boolean override) throws AuthenticationException, HttpClientLoggedInOnOtherLocationException, RemoteClientException {
        String canExt = "";
        try {
            if(!InetAddress.getByName( client.getRemoteHost()).isSiteLocalAddress()){
                canExt = " AND ext=1 ";
                try {
                    if(userName.equals("admin") && SystemConfig.getProperty("system", "system.adminremotelogin").equals("true")){
                        canExt = "";
                    }
                } catch (ConfigPropertiesException ex){
                    LOG.error("Could not check if admin can remote login: {}", ex.getMessage());
                }
            }
        } catch (UnknownHostException ex) {
            LOG.error("Could not check if address '{}' is external or not. Defaulting to local only", client.getRemoteHost());
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT id,clientname "
                                                                          + "FROM persons WHERE clientname=? AND clientpass=? AND (clienttype='USER' OR clienttype='ADMIN') "+canExt+" LIMIT 1")) {
            prep.setString(1, userName);
            try {
                prep.setString(2, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(password)));
            } catch (Exception ex) {
                throw new AuthenticationException("Could not authenticate: " + ex.getMessage());
            }
            try (ResultSet rs = prep.executeQuery()) {
                if(rs.next()){
                    int uid = rs.getInt("id");
                    RemoteClient toKill = null;
                    for(RemoteClient currentClient:RemoteClientsConnectionPool.getClientBaseByLogin(userName).getRemoteClients()){
                        if(client.getClientName().equals(userName)){
                            if(override){
                                toKill = currentClient;
                            } else {
                                throw new HttpClientLoggedInOnOtherLocationException("Already logged in at " + currentClient.getRemoteHost());
                            }
                        }
                    }
                    if(toKill!=null){
                        try {
                            final String oldLoc = toKill.getRemoteHost();
                            PersonsManagement.getInstance().getPerson(uid).sendMessage("NotificationService",PidomeJSONRPC.constructBroadcast("NotificationService","sendNotification", 
                                    new HashMap<String,String>(){{
                                        put("originates","system");
                                        put("type", "WARN");
                                        put("subject", "Login override");
                                        put("message","Logged in at new location: " + client.getRemoteHost() +", logged out at: " + oldLoc);
                                    }}
                            ));
                        } catch (PersonsManagementException | PidomeJSONRPCException ex) {
                            LOG.warn("Could not send new login notification");
                        }
                        RemoteClientsConnectionPool.disconnectClient(userName, "Logged out by new login at another location");
                        RemoteClientsAuthentication.deAuthorize(toKill);
                    }
                    client.setEndPoints(EndPoint.WSOCKET,EndPoint.WEB);
                    try {
                        PersonsManagement.getInstance().getPerson(rs.getInt("id")).addRemoteClient(client);
                    } catch (PersonsManagementException ex) {
                        LOG.error("Could not bind user to remote client: {}", ex.getMessage(), ex);
                        return AuthResult.FAILED;
                    }
                    return AuthResult.OK;
                } else {
                    return AuthResult.FAILED;
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get user info: {}", ex.getMessage());
            throw new AuthenticationException(ex.getMessage());
        }
    }
    
    /**
     * Returns websocket authentication
     * @param client
     * @param authTokens
     * @return
     * @throws AuthenticationException 
     */
    public static AuthResult authenticateWebsocketClient(RemoteClient client, PiDomeJSONRPCAuthentificationParameters authTokens) throws AuthenticationException {
        try {
            if(!clients.containsKey(client.getKey())){
                client.setClientName(authTokens.getLoginname());
                client.setClientInfo(authTokens.getClientInfo());
                switch(authTokens.getAuthType()){
                    case MOBILE:
                        client.setDeviceType(RemoteClient.DeviceType.MOBILE);
                    break;
                    default:
                        throw new AuthenticationException("Websocket clients need a MOBILE profile");
                }
                try {
                    PersonsManagement.LinkedClientInfo info = PersonsManagement.getInstance().getClientLink(client);
                    PersonsManagement.getInstance().getPerson(info.getBindId()).addRemoteClient(client);
                    client.setHasGPSEnabled(info.getHasGPS());
                    client.setSettingThrottled(info.getIsThrottled());
                    client.setResourceId(info.getResourceId());
                    client.setEndPoints(EndPoint.WSOCKET,EndPoint.RPC);
                    clients.put(client.getKey(), client);
                    LOG.info("Connected MOBILE client '{}' settings: GPS: {}, Throttled: {} ({}, {})", client.getRemoteHost(), client.getHasGPSEnabled(), client.throttled(), info.getBindId(), info.getResourceId());
                    return AuthResult.OK;
                } catch (PersonsManagementException ex){
                    LOG.warn("Client not allowed to interact (yet): {}", ex.getMessage());
                    client.setEndPoints(EndPoint.WSOCKET,EndPoint.RPC);
                    clients.put(client.getKey(), client);
                    return AuthResult.WAIT;
                }
            }
        } catch (Exception ex){
            LOG.error("Problem during authorization, {}", ex.getMessage(), ex);
        }
        return AuthResult.FAILED;
    }
    
    /**
     * Returns web authentication.
     * @param client
     * @param authTokens
     * @return
     * @throws AuthenticationException 
     */
    public static AuthResult authenticateMQTTClient(RemoteClient client, PiDomeJSONRPCAuthentificationParameters authTokens) throws AuthenticationException {
        return AuthResult.OK;
    }
    
    /**
     * Returns a display clients authentication.
     * @param client
     * @param authTokens
     * @return
     * @throws AuthenticationException 
     */
    public static AuthResult authenticateSocketClient(SocketServiceClient client, PiDomeJSONRPCAuthentificationParameters authTokens) throws AuthenticationException {
        AuthResult authResult;
        String canExt = "";
        try {
            if(!InetAddress.getByName( client.getRemoteHost()).isSiteLocalAddress()){
                canExt = " AND ext=1 ";
            }
        } catch (UnknownHostException ex) {
            LOG.error("Could not check if address {} is external or not. Defaulting to local only", client.getRemoteHost());
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT id,clientname,roleset,clientsettings "
                                                                          + "FROM fixedclients WHERE clientname=? AND clientpass=? "+canExt+" LIMIT 1")) {
            prep.setString(1, authTokens.getLoginname());
            try {
                prep.setString(2, MiscImpl.byteArrayToHexString(HashUtils.createSha256Hash(authTokens.getPassword())));
            } catch (Exception ex) {
                throw new AuthenticationException("Could not authenticate: " + ex.getMessage());
            }
            try (ResultSet rs = prep.executeQuery()) {
                if(rs.next()){
                    if(!clients.containsKey(client.getKey())){
                        for(String key:clients.keySet()){
                            if(clients.get(key).getClientName()!=null && clients.get(key).getClientName().equals(authTokens.getLoginname())) throw new AuthenticationException("Display name already taken, choose another.");
                        }
                        switch(authTokens.getAuthType()){
                            case DISPLAY:
                                client.setDeviceType(RemoteClient.DeviceType.DISPLAY);
                            break;
                            default:
                                throw new AuthenticationException("Raw socket clients need a DISPLAY profile");
                        }
                        client.setClientName(authTokens.getLoginname());
                        client.setClientInfo(authTokens.getClientInfo());
                        client.setClientId(rs.getInt("id"));
                        client.createRole(rs.getString("roleset"));
                        client.setSuperName(authTokens.getLoginname());
                        try {
                            client.setCapabilities(new PidomeJSONRPC(rs.getString("clientsettings"), false).getParsedObject());
                        } catch (NullPointerException | PidomeJSONRPCException ex) {
                            LOG.warn("No capabilities set for {}", authTokens.getLoginname(), ex);
                        }
                        client.setEndPoints(EndPoint.RAW,EndPoint.RPC);
                        clients.put(client.getKey(), client);
                        authResult = AuthResult.OK;
                    } else {
                        throw new AuthenticationException("Display already present logged in.");
                    }
                } else {
                    authResult = AuthResult.FAILED;
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get user info: {}", ex.getMessage());
            throw new AuthenticationException(ex.getMessage());
        }
        return authResult;
    }
    
    /**
     * Returns a list of waiting clients
     * @return 
     */
    public final static Map<String, RemoteClient> getWaitingDisplayClients(){
        return RemoteClientsConnectionPool.getUnmodifiableWaitingList();
    }
    
    /**
     * Removes authorization for the given device.
     * @param client 
     */
    public static void deAuthorize(RemoteClient client){
        if(clients.containsKey(client.getKey())) clients.remove(client.getKey());
    }
    
}
