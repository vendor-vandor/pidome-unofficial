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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 *
 * @author John
 */
public abstract class PersonBase implements RemoteClientInterface {
    
    private PersonBaseRole role;
    Map<String,Object> roleset = new HashMap<>();
    
    List<RemoteClient> connections = new ArrayList<>();
    
    String clientName= "";
    String lastLogin = "";
    
    boolean cpwd     = true;
    boolean ext      = false;
    boolean readOnly = false;
    
    int id           = 0;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(PersonBase.class);
    
    protected PersonBase(int id, String username, String lastLogin){
        this.clientName = username;
        this.lastLogin  = lastLogin;
        this.id         = id;
    }
    
    public abstract void roleCreated();
    
    /**
     * Returns the client's id.
     * @return 
     */
    @Override
    public final int getId(){
        return this.id;
    }
    
    /**
     * Sets if a password need to be changed.
     * @param set 
     */
    protected final void setIfCpwd(boolean set){
        this.cpwd = set;
    }
    
    /**
     * Sets if a client may connect externally
     * @param set 
     */
    protected final void setIfExternal(boolean set){
        this.ext = set;
    }
    
    /**
     * Returns if a password must be changed.
     * @return 
     */
    @Override
    public final boolean getIfCpwd(){
        return this.cpwd;
    }
    
    /**
     * Returns if this person can connect from external.
     * @return 
     */
    public final boolean getIfExternal(){
        return this.ext;
    }
    
    /**
     * Returns login name.
     * @return 
     */
    @Override
    public final String getLoginName(){
        return this.clientName;
    }
    
    /**
     * Returns last login datetime.
     * @return 
     */
    @Override
    public final String getLastLogin(){
        if(this.lastLogin==null){
            return "Unknown";
        } else {
            return this.lastLogin;
        }
    }
    
    /**
     * Sets the last login time.
     * @param datetime 
     */
    protected final void setLastLogin(String datetime){
        if(datetime!=null && !datetime.isEmpty()){
            this.lastLogin = datetime;
        }
    }
    
    /**
     * Returns this persons roleset.
     * @return 
     */
    @Override
    public final PersonBaseRole getRole(){
        return role;
    }
    
    /**
     * Creates the roles for this user.
     * @param roleset 
     */
    public final void createRoleSet(Map<String,Object> roleset){
        this.roleset = roleset;
        role = new PersonBaseRole(roleset);
        roleCreated();
    }
    
    /**
     * Returns true of this person has this client connection.
     * @param client
     * @return 
     */
    public final boolean containsClient(RemoteClient client){
        return connections.contains(client);
    }
    
    /**
     * Adds a client connection.
     * @param client 
     */
    public final void addRemoteClient(RemoteClient client){
        client.setSuperName(clientName);
        connections.add(client);
    }
    
    /**
     * Removes a client connection.
     * @param client 
     */
    public final void removeRemoteClient(RemoteClient client){
        connections.remove(client);
        client.finish();
    }
    
    /**
     * Removes and finishes all remote clients.
     */
    public final void removeAllRemoteClients(){
        for(RemoteClient client:connections){
            try {
                client.finish();
            } catch (Exception ex){}
        }
        connections.clear();
    }
    
    /**
     * Returns a list of connected clients.
     * @return 
     */
    public final List<RemoteClient> getRemoteClients(){
        return this.connections;
    }
    
    /**
     * Send a message to all sockets for this RemoteClient.
     * @param nameSpace
     * @param message 
     */
    public final void sendMessage(String nameSpace, String message){
        try {
            for(RemoteClient client:connections){
                client.sendMsg(nameSpace, message.getBytes());
            }
        } catch (Exception ex){
            //// Sometimes a peer disconnects very fast. which means a message is not sended.
        }
    }
    
    /**
     * Sends a personal message.
     * @param from
     * @param message
     * @return 
     */
    public final Map<String,Object> sendNonLegitMessage(final int from, final String message){
        LOG.debug("Message handling from {}, for {}, contents: {}",from,clientName,message);
        Map<String,Object> returnObject = new HashMap<>();
        returnObject.put("message", "");
        returnObject.put("success", false);
        try {
            byte[] msg = PidomeJSONRPC.constructBroadcast("ClientService", "sendDisplayClientMessage", new HashMap<String,Object>(){{
                put("from",from);
                put("message",message);
            }}).getBytes();
            for(RemoteClient client:connections){
                client.sendMsg("ClientService", msg);
                LOG.info("Send message to client: {} of type: {}", client.getClientName(), client.getType());
            }
            returnObject.put("success", true);
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send Message to: {}, {}", clientName, message);
            returnObject.put("message", "Could not send message: " + ex.getMessage());
        }
        return returnObject;
    }
 
    /**
     * Removes a persons connection.
     * @param client 
     */
    public final void removeClient(RemoteClient client){
        client.finish();
        connections.remove(client);
    }
    
    /**
     * Full signoff with all clients.
     */
    public final void signOff(){
        for(RemoteClient client:connections){
            client.finish();
            RemoteClientsAuthentication.deAuthorize(client);
        }
        connections.clear();
    }
    
}