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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.clients.remoteclient.AuthenticationException;
import org.pidome.server.services.clients.socketservice.SocketServiceException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClient.DeviceType;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.clients.remoteclient.RemoteClientException;
import org.pidome.server.services.clients.socketservice.SocketClientsManagement;
import org.pidome.server.services.clients.socketservice.SocketClientsManagementException;
import org.pidome.server.services.clients.socketservice.SocketServiceClient;

/**
 *
 * @author John
 */
public class ClientServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements ClientServiceJSONRPCWrapperInterface {

    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("signOn", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("loginname"   , "");}});
                        put(1,new HashMap<String,Object>(){{put("type"        , "");}});
                        put(2,new HashMap<String,Object>(){{put("key"         , "");}});
                        put(3,new HashMap<String,Object>(){{put("clientinfo"  , "");}});
                    }
                });
                put("resume", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("key"       , "");}});
                    }
                });
                put("sleep", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("loginname" , "");}});
                        put(1,new HashMap<String,Object>(){{put("type"      , "");}});
                        put(2,new HashMap<String,Object>(){{put("key"       , "");}});
                        put(3,new HashMap<String,Object>(){{put("clientinfo", "");}});
                    }
                });
                put("signOn", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("username", "");}});
                        put(1,new HashMap<String,Object>(){{put("password", "");}});
                    }
                });
                put("sendDisplayClientMessage", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("displayname", "");}});
                        put(1,new HashMap<String,Object>(){{put("message", "");}});
                    }
                });
                put("storeClientSettings", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("settings", new HashMap<>());}});
                    }
                });
                put("getClientSettings", null);
                put("approveClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("displayname", "");}});
                        put(1,new HashMap<String,Object>(){{put("userid", 0);}});
                    }
                });
                put("disApproveClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("displayname", "");}});
                    }
                });
                put("disconnectClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("displayname", "");}});
                        put(1,new HashMap<String,Object>(){{put("message", "");}});
                    }
                });
                put("getConnectedClients", null);
                put("getDisplayClients", null);
                put("getDisplayClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", "");}});
                    }
                });
                put("getWaitingClients", null);
                put("getAllClients", null);
                put("signOff", null);
                put("addDisplayClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("username", "");}});
                        put(1,new HashMap<String,Object>(){{put("password", "");}});
                        put(2,new HashMap<String,Object>(){{put("roleset", new HashMap<>());}});
                        put(3,new HashMap<String,Object>(){{put("extconnect", true);}});
                    }
                });
                put("updateDisplayClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                        put(1,new HashMap<String,Object>(){{put("password", "");}});
                        put(2,new HashMap<String,Object>(){{put("roleset", new HashMap<>());}});
                        put(3,new HashMap<String,Object>(){{put("extconnect", true);}});
                    }
                });
                put("deleteDisplayClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                    }
                });
                put("setCapabilities", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("capabilities", new HashMap<>());}});
                    }
                });
            }
        };
        return mapping;
    }

    @Override
    public Object signOn(String clientName, String type, String key, String clientInfo) {
        return false;
    }

    @Override
    public Object sleep(String clientName, String type, String key, String clientInfo) {
        return false;
    }

    @Override
    public Object resume(String key) {
        return true;
    }
    
    @Override
    public Object signOn(String username, String password) {
        return false;
    }
    
    @Override
    public Object approveClient(String clientName, Long clientId) throws SocketServiceException, AuthenticationException {
        return RemoteClientsConnectionPool.approveUserDevice(clientName, clientId.intValue());
    }

    @Override
    public Object disApproveClient(String clientName) throws SocketServiceException, AuthenticationException {
        return RemoteClientsConnectionPool.disApproveUserDevice(clientName);
    }

    @Override
    public Object signOff() {
        return RemoteClientsConnectionPool.disconnectClient(this.getCaller().getLoginName(), "Web interface logoff");
    }

    @Override
    public Object sendDisplayClientMessage(String clientName, String message) {
        try {
            RemoteClientsConnectionPool.getClientBaseByLogin(clientName).sendNonLegitMessage(getCaller().getId(), message);
            return true;
        } catch (RemoteClientException ex) {
            return false;
        }
    }

    @Override
    public Object disconnectClient(String clientName, String message) {
        return RemoteClientsConnectionPool.disconnectClient(clientName, message);
    }

    @Override
    public List<Map<String, Object>> getDisplayClients() {
        List<Map<String, Object>> returnList = new ArrayList();
        try {
            for(Map<String,Object> clientMap:SocketClientsManagement.getClients()){
                clientMap.put("since", "");
                clientMap.put("duration", "");
                clientMap.put("address", "");
                clientMap.put("lastlogin", "");
                clientMap.put("connected", false);
                clientMap.put("name", clientMap.get("clientname"));
                for(SocketServiceClient client:RemoteClientsConnectionPool.getConnectedDisplayClients()){
                    if((int)clientMap.get("id")==client.getId()){
                        clientMap.put("since", client.getConnectedSinceDateTime());
                        clientMap.put("duration", client.getConnectionDuration());
                        clientMap.put("address", client.getRemoteSocketAddress().replace("/", ""));
                        clientMap.put("connected", client.isConnected());
                        clientMap.put("lastlogin", client.getLastLogin());
                    }
                }
                returnList.add(clientMap);
            }
        } catch (SocketClientsManagementException ex) {
            LOG.error("Could not retreive clients list: {}", ex.getMessage());
        }
        return returnList;
    }
    
    @Override
    public Map<String, Object> getDisplayClient(Long clientId) throws SocketClientsManagementException {
        Map<String,Object> clientMap = SocketClientsManagement.getClient(clientId.intValue());
        clientMap.put("since", "");
        clientMap.put("duration", "");
        clientMap.put("address", "");
        clientMap.put("lastlogin", "");
        clientMap.put("lastlogin", "");
        clientMap.put("connected", false);
        clientMap.put("name", clientMap.get("clientname"));
        for(SocketServiceClient client:RemoteClientsConnectionPool.getConnectedDisplayClients()){
            if((int)clientMap.get("id")==client.getId()){
                clientMap.put("since", client.getConnectedSinceDateTime());
                clientMap.put("duration", client.getConnectionDuration());
                clientMap.put("address", client.getRemoteSocketAddress().replace("/", ""));
                clientMap.put("connected", client.isConnected());
                clientMap.put("lastlogin", client.getLastLogin());
                clientMap.put("connected", true);
            }
        }
        return clientMap;
    }
    
    @Override
    public List<Map<String, Object>> getConnectedClients() {
        List<Map<String, Object>> returnList = new ArrayList();
        for(RemoteClient client:RemoteClientsConnectionPool.getConnectedClients()){
            Map<String, Object> tmp = new HashMap<>();
            tmp.put("waiting", false);
            tmp.put("devicetype", client.getDeviceType());
            tmp.put("clienttype", client.getType());
            tmp.put("clientinfo", client.getClientInfo());
            tmp.put("since", client.getConnectedSinceDateTime());
            tmp.put("duration", client.getConnectionDuration());
            tmp.put("address", client.getRemoteSocketAddress().replace("/", ""));
            tmp.put("connected", true);
            tmp.put("name", client.getClientName());
            tmp.put("supername", client.getSuperName());
            returnList.add(tmp);
        }
        return returnList;
    }
    
    @Override
    public List<Map<String, Object>> getWaitingClients() {
        List<Map<String, Object>> returnList = new ArrayList();
        Map<String, RemoteClient> clientsList = RemoteClientsConnectionPool.getWaitingClients();
        for(String key:clientsList.keySet()){
            Map<String, Object> tmp = new HashMap<>();
            tmp.put("waiting", true);
            tmp.put("devicetype", clientsList.get(key).getDeviceType());
            tmp.put("clienttype", clientsList.get(key).getType());
            tmp.put("clientinfo", clientsList.get(key).getClientInfo());
            tmp.put("since", clientsList.get(key).getConnectedSinceDateTime());
            tmp.put("duration", clientsList.get(key).getConnectionDuration());
            tmp.put("address", clientsList.get(key).getRemoteSocketAddress().replace("/", ""));
            tmp.put("connected", false);
            tmp.put("name", key);
            tmp.put("supername", key);
            returnList.add(tmp);
        }
        return returnList;
    }

    @Override
    public List<Map<String, Object>> getAllClients(){
        List<Map<String, Object>> returnList = new ArrayList();
        returnList.addAll(getWaitingClients());
        returnList.addAll(getConnectedClients());
        return returnList;
    }
    
    @Override
    public Object updateDisplayClient(Long clientId, String password, HashMap<String, Object> roleset, boolean ext) throws SocketClientsManagementException {
        return SocketClientsManagement.updateRemoteClient(clientId.intValue(), password, roleset, ext);
    }
    
    @Override 
    public Object deleteDisplayClient(Long clientId) throws SocketClientsManagementException {
        return SocketClientsManagement.deleteRemoteClient(clientId.intValue());
    }

    @Override
    public Object addDisplayClient(String username, String password, HashMap<String, Object> roleset, boolean ext) throws SocketClientsManagementException {
        return SocketClientsManagement.addRemoteClient(username, password, roleset, ext);
    }

    @Override
    public Object storeClientSettings(Map<String, Object> settings) throws SocketServiceException {
        if(this.getCaller().getType()==RemoteClient.Type.SOCKET){
            return SocketClientsManagement.storeClientSettings(this.getCaller().getId(), settings);
        } else {
            throw new SocketServiceException("Requested by wrong client type");
        }
    }

    @Override
    public Object getClientSettings() throws SocketServiceException {
        if(this.getCaller().getType()==RemoteClient.Type.SOCKET){
            return SocketClientsManagement.getClientSettings(this.getCaller().getId());
        } else {
            throw new SocketServiceException("Requested by wrong client type");
        }
    }

    @Override
    public boolean setCapabilities(HashMap<String, Object> capabilities) {
        LOG.debug("Got capabilities: " + capabilities);
        if(this.getCallerResource().getDeviceType()==DeviceType.DISPLAY || this.getCallerResource().getDeviceType()==DeviceType.MOBILE){
            this.getCallerResource().setCapabilities(capabilities);
        }
        return true;
    }
    
}
