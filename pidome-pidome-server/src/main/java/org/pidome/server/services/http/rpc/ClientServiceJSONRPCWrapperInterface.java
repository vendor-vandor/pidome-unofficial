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
import org.pidome.server.services.clients.remoteclient.AuthenticationException;
import org.pidome.server.services.clients.socketservice.SocketClientsManagementException;
import org.pidome.server.services.clients.socketservice.SocketServiceException;

/**
 *
 * @author John
 */
public interface ClientServiceJSONRPCWrapperInterface {
    
    /**
     * Sign on a client.
     * @param clientName
     * @param type
     * @param key
     * @param clientInfo
     * @return 
     */
    public Object signOn(String clientName, String type, String key, String clientInfo);
    
    /**
     * Put a client in sleep mode so it can resume later
     * @param clientName
     * @param type
     * @param key
     * @param clientInfo
     * @deprecated 
     * @return 
     */
    public Object sleep(String clientName, String type, String key, String clientInfo);
    
    /**
     * Resume from a sleep or continue authorization.
     * Sleep has been deprecated. This is an authorization method to be used in conjunction with web authorization.
     * When an user is authorized via web this should be used on a socket so it will be linked together.
     * @param key
     * @return 
     */
    public Object resume(String key);
    
    /**
     * Web interface signon method.
     * @param username
     * @param password
     * @return 
     */
    public Object signOn(String username, String password);
    
    /**
     * Approve a socket client's connection.
     * @param clientName
     * @param clientId
     * @return
     * @throws SocketServiceException
     * @throws AuthenticationException 
     */
    @PiDomeJSONRPCPrivileged
    public Object approveClient(String clientName, Long clientId) throws SocketServiceException, AuthenticationException;
    
    /**
     * Disapprove a client's connection.
     * @param clientName
     * @return
     * @throws SocketServiceException
     * @throws AuthenticationException 
     */
    @PiDomeJSONRPCPrivileged
    public Object disApproveClient(String clientName) throws SocketServiceException, AuthenticationException;
    
    /**
     * Goodbye.
     * @return 
     */
    public Object signOff();
    
    /**
     * Sends a message to a client.
     * @param clientName
     * @param message
     * @return 
     */
    public Object sendDisplayClientMessage(String clientName, String message);
    
    /**
     * Disconnects a client.
     * When used on a socket client it has to be re-approved again.
     * @param clientName
     * @param message
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object disconnectClient(String clientName, String message);
    
    /**
     * Returns a list of connected clients.
     * These clients are having interaction with the server.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public List<Map<String, Object>> getConnectedClients();
    
    /**
     * Returns a list of connected fixed display clients.
     * @return 
     */
    public List<Map<String, Object>> getDisplayClients();
    
    /**
     * Returns a single display client.
     * @param clientId
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketClientsManagementException 
     */
    public Map<String, Object> getDisplayClient(Long clientId) throws SocketClientsManagementException;
    
    /**
     * These clients are waiting for approval.
     * These clients are waiting to be approved. They will receive time values form the server.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public List<Map<String, Object>> getWaitingClients();
    
    /**
     * These clients are waiting for approval.
     * These clients are waiting to be approved. They will receive time values form the server.
     * @return A full list of all connected and waiting clients.
     */
    @PiDomeJSONRPCPrivileged
    public List<Map<String, Object>> getAllClients();
    
    /**
     * Updates a display client
     * @param clientId
     * @param password
     * @param roleset
     * @param ext
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketClientsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updateDisplayClient(Long clientId, String password,HashMap<String,Object> roleset,boolean ext) throws SocketClientsManagementException;
    
    /**
     * Deletes a display client so it can not connect anymore.
     * @param clientId
     * @return
     * @throws SocketClientsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deleteDisplayClient(Long clientId) throws SocketClientsManagementException;
    
    /**
     * Adds an user to the system.
     * @param username
     * @param password
     * @param roleset
     * @param ext
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketClientsManagementException 
     */
    @PiDomeJSONRPCPrivileged
    public Object addDisplayClient(String username, String password, HashMap<String,Object> roleset, boolean ext) throws SocketClientsManagementException;
    
    /**
     * Stores client settings of the connected fixed client.
     * @param settings
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketServiceException 
     */
    public Object storeClientSettings(Map<String,Object> settings) throws SocketServiceException;
    
    /**
     * Returns the client settings of the current connected fixed client.
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketServiceException 
     */
    public Object getClientSettings() throws SocketServiceException;
 
    /**
     * Sets client capabilities.
     * @param capabilities
     * @return 
     */
    public boolean setCapabilities(HashMap<String,Object> capabilities);
    
}
