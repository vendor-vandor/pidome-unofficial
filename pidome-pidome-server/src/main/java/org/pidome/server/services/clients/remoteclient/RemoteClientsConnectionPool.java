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
package org.pidome.server.services.clients.remoteclient;

import org.pidome.server.services.clients.persons.PersonBase;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.services.clients.persons.PersonsManagement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.clients.socketservice.SocketServiceClient;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.clients.socketservice.SocketServiceException;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

public final class RemoteClientsConnectionPool {
    
    private static final Map<String, RemoteClient> waitingClients = new HashMap<>();

    private static final Map<String, SocketServiceClient> displayClients = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(RemoteClientsConnectionPool.class);
    
    private static List<String> nonThrottled = new ArrayList<String>(){{
        add("ClientService");
        add("DayPartService");
        add("EventService");
        add("LocationService");
        add("PresenceService");
        add("SystemService");
        add("UserService");
        add("ClientService");
        add("UserStatusService");
        add("NotificationService");
    }};
    
    /**
     * Returns a list of non throttled services.
     * @return 
     */
    public static List<String> getNonThrottledServices(){
        return nonThrottled;
    }
    
    /**
     * Returns a list of connected socket clients.
     * @return 
     */
    public static List<RemoteClient> getConnectedClients(){
        List<PersonBase> list = new ArrayList<>();
        List<RemoteClient> returnList = new ArrayList<>();
        try {
            list.addAll(PersonsManagement.getInstance().getPersons());
        } catch (PersonsManagementException ex) {
            ///No persons logged in.
        }
        for(PersonBase client:list){
            returnList.addAll(client.getRemoteClients());
        }
        for(SocketServiceClient client: displayClients.values()){
            returnList.add(client);
        }
        return returnList;
    }
    
    /**
     * Returns a list of connected displayClients.
     * @return 
     */
    public static List<SocketServiceClient> getConnectedDisplayClients(){
        List<SocketServiceClient> list = new ArrayList();
        for(SocketServiceClient client:displayClients.values()){
            list.add(client);
        }
        return list;
    }
    
    /**
     * Removes a socket client.
     * Extended to remove people, and socket clients.
     * @param client 
     */
    public static void removeClient(RemoteClient client){
        boolean removeExist = false;
        boolean removeWaiting = false;
        try {
            for(PersonBase clientBase:PersonsManagement.getInstance().getPersons()){
                if(clientBase.containsClient(client)){
                    clientBase.removeAllRemoteClients();
                    break;
                }
            }
        } catch (PersonsManagementException ex) {
            /// No persons.
        }
        for(RemoteClient remoteClient:displayClients.values()){
            if(remoteClient==client){
                removeExist = true;
                break;
            }
        }
        if(removeExist){
            displayClients.remove(client.getClientName());
        }
        for(RemoteClient remoteClient:waitingClients.values()){
            if(remoteClient==client){
                removeWaiting = true;
                break;
            }
        }
        if(removeWaiting){
            waitingClients.remove(client.getClientName());
        }
    }
    
    /**
     * Returns a client by it's name.
     * @param loginName
     * @return
     * @throws RemoteClientException 
     */
    public static PersonBase getClientBaseByLogin(String loginName) throws RemoteClientException{
        try {
            for(PersonBase clientBase:PersonsManagement.getInstance().getPersons()){
                if(clientBase.getLoginName().equals(loginName)){
                    return clientBase;
                }
            }
        } catch (PersonsManagementException ex) {
            /// No persons.
        }
        throw new RemoteClientException("Client "+loginName+" not found");
    }
    
    /**
     * Returns a Remote Client base.
     * @param client
     * @return 
     */
    public static PersonBase getClientBaseByConnection(RemoteClient client) throws RemoteClientException {
        try {
            for(PersonBase clientBase:PersonsManagement.getInstance().getPersons()){
                if(clientBase.containsClient(client)){
                    return clientBase;
                }
            }
        } catch (PersonsManagementException ex) {
            throw new RemoteClientException("Client could not be retrieived: " + ex.getMessage());
        }
        throw new RemoteClientException("Client not present");
    }
    
    /**
     * Returns a Remote Client base.
     * @param remoteClient
     * @return 
     * @throws org.pidome.server.services.clients.remoteclient.RemoteClientException 
     */
    public static SocketServiceClient getIfSocketClientAuthorized(SocketServiceClient remoteClient) throws RemoteClientException {
        for(SocketServiceClient client:displayClients.values()){
            if(client == remoteClient){
                return remoteClient;
            }
        }
        throw new RemoteClientException("Client not present");
    }
    
    /**
     * Returns a modifiable display clients waiting list.
     * @return 
     */
    public static Map<String, RemoteClient> getWaitingList(){
        return waitingClients;
    }
    
    /**
     * Returns an unmodifiable display clients waiting list.
     * @return 
     */
    public static Map<String, RemoteClient> getUnmodifiableWaitingList(){
        return Collections.unmodifiableMap(waitingClients);
    }
    
    /**
     * Adds a display device awaiting approval.
     * @param deviceName
     * @param c 
     */
    public static void addWaitingDevice(String deviceName, RemoteClient c){
        waitingClients.put(deviceName, c);
    }
    
    /**
     * Adds a display device.
     * @param deviceName
     * @param c 
     */
    public static void addDisplayDevice(String deviceName, SocketServiceClient c){
        displayClients.put(deviceName, c);
    }
    
    /**
     * Removes a device from the waiting list.
     * @param deviceName 
     */
    private static void removeWaiting(String deviceName){
        if(waitingClients.containsKey(deviceName)){
            waitingClients.remove(deviceName);
        }
    }
    
    /**
     * Sends a message to all the connected devices
     * @param nameSpace
     * @param locationId
     * @param msg 
     */
    public static void sendAll(String nameSpace, int locationId, String msg) {
        LOG.trace("BROADCAST_ALL: {}", msg);
        PersonsManagement.getInstance().sendAll(nameSpace, locationId, msg);
        displayClients.values().stream().forEach((client) -> {
            client.sendMessage(nameSpace, locationId, msg);
        });
    }

    /**
     * Sens a message to all the devices except doNotSend.
     * @param nameSpace
     * @param locationId
     * @param msg
     * @param doNotSend 
     */
    public static void sendAll(String nameSpace, int locationId, String msg, int doNotSend) {
        LOG.trace("BROADCAST_ALL_EXCLUDE_{}: {}", doNotSend, msg);
        PersonsManagement.getInstance().sendAll(nameSpace, locationId, msg, doNotSend);
        displayClients.values().stream().forEach((client) -> {
            if(client.getId()!=doNotSend){
                client.sendMessage(nameSpace, locationId, msg);
            }
        });
    }

    /**
     * Approves a device to be added.
     * @param client 
     * @param userId 
     * @return  
     * @throws org.pidome.server.services.clients.socketservice.SocketServiceException 
     * @throws org.pidome.server.services.clients.remoteclient.AuthenticationException 
     */
    public static boolean approveUserDevice(String client, int userId) throws SocketServiceException, AuthenticationException {
        try {
            if(RemoteClientsConnectionPool.getUnmodifiableWaitingList().containsKey(client)){
                final RemoteClient displayClient = RemoteClientsConnectionPool.getUnmodifiableWaitingList().get(client);
                if(displayClient.getType().equals(RemoteClient.Type.WEBSOCKET)){
                    displayClient.setEndPoints(RemoteClientsAuthentication.EndPoint.WSOCKET,RemoteClientsAuthentication.EndPoint.RPC);
                    PersonsManagement.getInstance().storeClientLink(displayClient, userId);
                    removeWaiting(displayClient.getClientName());
                    LOG.info("Client '{}' has been approved connection and is bound to user: {}", client, userId);
                }
                try {
                    displayClient.sendMsg("ClientService",PidomeJSONRPC.constructBroadcast("ClientService","approveClient", new HashMap<String,Object>(){{put("approved",true);}}).getBytes());
                    ClientMessenger.send("ClientService","signOn", 0, new HashMap<String,Object>(){{put("name",displayClient.getClientName());}});
                } catch (PidomeJSONRPCException ex) {
                    LOG.error("Could not send out aproval broadast for {}", displayClient.getClientName(), ex);
                    throw new SocketServiceException("Could not send out aproval broadast for " + displayClient.getClientName());
                }
                return true;
            }
        } catch (PersonsManagementException ex){
            LOG.debug("Client '{}' has been approved but connection could not be bound to user: {}", client, userId);
            throw new SocketServiceException(ex);
        }
        throw new SocketServiceException("Invalid client " + client);
    }
    
    /**
     * Disapproves a device to be added.
     * @param client 
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketServiceException 
     * @throws org.pidome.server.services.clients.remoteclient.AuthenticationException 
     */
    public static boolean disApproveUserDevice(String client) throws SocketServiceException, AuthenticationException {
        if(waitingClients.containsKey(client)){
            LOG.debug("Closing connection with client (401 Not allowed): {}", RemoteClientsConnectionPool.getUnmodifiableWaitingList().get(client).getRemoteSocketAddress());
            final RemoteClient displayClient = waitingClients.get(client);
            try {
                displayClient.sendMsg("ClientService",PidomeJSONRPC.constructBroadcast("ClientService","approveClient", new HashMap<String,Object>(){{put("approved",false);}}).getBytes());
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Could not send out disApprove broadast for {}", displayClient.getClientName());
                throw new SocketServiceException("Could not send out disApprove broadast for " + displayClient.getClientName());
            }
            LOG.debug("Client '{}' has been denied connection", client);
            displayClient.finish();
            return true;
        }
        throw new SocketServiceException("Invalid client " + client);
    }
    
    /**
     * Disconnects a client by it's name and sends a disconnect reason.
     * @param client
     * @param disconnectmessage 
     * @return  
     */
    public static boolean disconnectClient(final String client, final String disconnectmessage){
        RemoteClient toKill = null;
        for(RemoteClient remoteClient:getConnectedClients()){
            if(remoteClient.getClientName().equals(client)){
                toKill = remoteClient;
            }
        }
        if(toKill!=null){
            LOG.debug("Closing connection with client: {} ({})", toKill.getRemoteSocketAddress(), client);
            try {
                toKill.sendMsg("ClientService",PidomeJSONRPC.constructBroadcast("ClientService","disconnectClient", new HashMap<String,String>(){{put("message",disconnectmessage);}}).getBytes());
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Could not send disconnect message to {}", toKill.getClientName());
            }
            removeClient(toKill);
            toKill.finish();
        }
        if(displayClients.containsKey(client)){
            RemoteClient displayClient = displayClients.get(client);
            LOG.debug("Closing connection with waiting client: {} ({})", displayClient.getRemoteSocketAddress(), client);
            try {
                displayClient.sendMsg("ClientService",PidomeJSONRPC.constructBroadcast("ClientService","disconnectClient", new HashMap<String,String>(){{put("message",disconnectmessage);}}).getBytes());
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Could not send waiting clients disconnect message to: {}", displayClient.getClientName());
            }
            displayClients.get(client).finish();
            LOG.debug("Removed client from known waiting clients: {}", displayClients.remove(client));
        }
        if(waitingClients.containsKey(client)){
            RemoteClient displayClient = waitingClients.get(client);
            LOG.debug("Closing connection with waiting client: {} ({})", displayClient.getRemoteSocketAddress(), client);
            try {
                displayClient.sendMsg("ClientService",PidomeJSONRPC.constructBroadcast("ClientService","disconnectClient", new HashMap<String,String>(){{put("message",disconnectmessage);}}).getBytes());
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Could not send waiting clients disconnect message to: {}", displayClient.getClientName());
            }
            waitingClients.get(client).finish();
            LOG.debug("Removed client from known waiting clients: {}", waitingClients.remove(client));
        }
        ClientMessenger.send("ClientService","signOff", 0, new HashMap<String,Object>(){{put("name",client);}});
        return true;
    }
    
    /**
     * Return a list of waiting clients.
     * @return 
     */
    public static Map<String, RemoteClient> getWaitingClients(){
        return RemoteClientsConnectionPool.getUnmodifiableWaitingList();
    }
    
    /**
     * Sends a message to ALL the clients.
     * @param nameSpace
     * @param msg 
     */
    public static void sendClientMessage(String nameSpace, int locationId, String msg){
        RemoteClientsConnectionPool.sendAll(nameSpace, locationId, msg);
    }
    
    /**
     * Sends a message to ALL the clients, except.
     * @param nameSpace
     * @param msg 
     * @param butNot 
     */
    public static void sendClientMessage(String nameSpace, int locationId, String msg, int butNot){
        RemoteClientsConnectionPool.sendAll(nameSpace, locationId, msg, butNot);
    }
    
    /**
     * Sends a message from a non legit source.
     * A non legit source is a textual representation of a source which is not a signed in client.
     * @param from
     * @param client
     * @param message 
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketServiceException 
     */
    static Map<String,Object> sendSingleClientMessageMessage(int from, int client, String message) throws SocketServiceException {
        try {
            return PersonsManagement.getInstance().getPerson(client).sendNonLegitMessage(from, message);
        } catch (PersonsManagementException ex) {
            throw new SocketServiceException("Illegal sender or receiver");
        }
    }
}