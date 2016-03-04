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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.misc.utils.MinuteListener;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.services.messengers.ClientMessenger;
import static org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication.AuthResult.OK;
import static org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication.AuthResult.WAIT;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.clients.remoteclient.AuthenticationException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClient.Type;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.http.rpc.PiDomeJSONRPCAuthentificationParameters;

/**
 *
 * @author John
 */
public final class SocketServiceClient extends RemoteClient implements RemoteClientInterface,Runnable,MinuteListener {
    
    Socket clientSocket;
    PrintWriter out;
    BufferedReader in;
    
    Thread t = new Thread(this);
    
    static Logger LOG = LogManager.getLogger(SocketServiceClient.class);
    
    private int clientId = 0;
    private String lastLogin = "Unknown";
    
    SocketServiceBaseRole role;
    
    public SocketServiceClient(Socket socket){
        super(Type.SOCKET);
        clientSocket = socket;
        setRemoteHost(socket.getInetAddress().getHostAddress());
    }

    /**
     * Creates a role.
     * @param roleSet 
     */
    public final void createRole(String roleSet){
        try {
            this.role = new SocketServiceBaseRole(new PidomeJSONRPC(roleSet, false).getParsedObject());
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not create role for this client");
        }
    }
    
    /**
     * Creates a role. 
     * @return 
     * @throws org.pidome.server.services.clients.socketservice.SocketServiceException 
     */
    @Override
    public final SocketServiceBaseRole getRole() throws SocketServiceException {
        if(this.role==null){
            throw new SocketServiceException("No client role present");
        } else {
            return this.role;
        }
    }
    
    /**
     * Sets the last login date.
     * @param date 
     */
    public final void setLastLogin(String date){
        this.lastLogin = date;
    }
    
    /**
     * Sets the last login date. 
     * @return  
     */
    public final String getLastLogin(){
        return this.lastLogin;
    }
    
    /**
     * Sets the socket client id.
     * @param id 
     */
    public final void setClientId(int id){
        this.clientId = id;
        this.setResourceId(clientId);
    }
    
    /**
     * Client mains.
     */
    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    PidomeJSONRPC pidomeJSONRPC = new PidomeJSONRPC(inputLine.trim());
                    Map<String, Object> result = new HashMap<>();
                    Map<String, Object> data = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "");
                    data.put("key", "");
                    data.put("auth", false);
                    if (!isAuthorized()) {
                        try {
                            if(pidomeJSONRPC.getMethod().equals("ClientService.signOff")){
                                finish();
                            } else if(!pidomeJSONRPC.getMethod().equals("ClientService.signOn")){
                                throw new AuthenticationException("Not authorized");
                            }
                            PiDomeJSONRPCAuthentificationParameters authObjects = pidomeJSONRPC.getAuthenticationParameters();
                            RemoteClientsAuthentication.AuthResult authResult = RemoteClientsAuthentication.authenticateSocketClient(this, authObjects);
                            switch(authResult){
                                case OK:
                                    data.put("auth", true);
                                    data.put("code", 200);
                                    data.put("key", getKey());
                                    data.put("message", "Authentication ok");
                                    TimeUtils.addMinuteListener(this);
                                    broadcastSignon();
                                    RemoteClientsConnectionPool.addDisplayDevice(getClientName(), this);
                                    this.setConnected();
                                break;
                                case WAIT:
                                    data.put("auth", true);
                                    data.put("code", 202);
                                    data.put("key", getKey());
                                    data.put("message", "Authentication needs to be verified");
                                    RemoteClientsConnectionPool.addWaitingDevice(getClientName(), this);
                                break;
                                default:
                                    data.put("code", 401);
                                    data.put("message", "Authentication failed");
                                break;
                            }
                        } catch (AuthenticationException ex) {
                            data.put("code", 401);
                            data.put("message", ex.getMessage());
                        }
                        result.put("data", data);
                        sendMsg("ClientService",pidomeJSONRPC.constructResponse(result).getBytes());
                    } else {
                        String method = pidomeJSONRPC.getMethod();
                        String returnMessage;
                        switch(method){
                            case "ClientService.signOff":
                                finish();
                                returnMessage = "";
                            break;
                            default:
                                pidomeJSONRPC.handle(RemoteClientsConnectionPool.getIfSocketClientAuthorized(this), this);
                                returnMessage = pidomeJSONRPC.getResult();
                            break;
                        }
                        sendMsg("ClientService",returnMessage.getBytes());
                    }
                } catch (PidomeJSONRPCException ex) {
                    sendMsg("ClientService",ex.getJsonReadyMessage().getBytes());
                } catch (Exception ex) {
                    LOG.error("An uncatched exception has occured: {}", ex.getMessage(), ex);
                    sendMsg("ClientService",new StringBuilder("{\"jsonrpc\": \"2.0\", \"error\": {\"code\": ").append(PidomeJSONRPCException.JSONError.SERVER_ERROR.toLong()).append(", \"message\": \"an internal server error occurred: ").append(ex.getMessage()).append("\"}, \"id\": null}").toString().getBytes());
                }
            }
        } catch (IOException e) {
            LOG.error("Client/Server caused disconnect",e);
            LOG.info("DISCONNECT: Client/Server caused disconnect: " + e.getMessage());
            finish();
        }
    }

    /**
     * Sends messages to the socket.
     * This method checks if the client is allowed to receive first.
     * @param nameSpace
     * @param locationId
     * @param msg 
     */
    public final void sendMessage(String nameSpace, int locationId, String msg){
        Runnable run = () -> {
            try {
                if(getRole().hasLocationAccess(locationId) && getRole().hasNameSpaceAccess(nameSpace)){
                    this.sendSocket(nameSpace, msg.getBytes());
                }
            } catch (SocketServiceException ex) {
                LOG.error("Could not send message to {}: {}", this.clientId, ex.getMessage());
            }
        };
        run.run();
    }
    
    /**
     * Sends time updates.
     * @param timeutils 
     */
    @Override
    public void handleMinuteUpdate(final TimeUtils timeutils) {
        try {
            sendMsg("SystemService", PidomeJSONRPC.constructBroadcast("SystemService","time", new HashMap<String,Object>(){{
                put("time",timeutils.get24HoursTime());
                put("date",timeutils.getDDMMYYYYdate());
                put("shorttext", timeutils.getShortDateTextRepresentation());
                put("day",timeutils.getDayOfMonth());
                put("dayname",timeutils.getDayName());
                put("month",timeutils.getMonth());
                put("monthname",timeutils.getMonthName());
                put("year",timeutils.getYear());
                put("sunrise",TimeUtils.getSunrise());
                put("sunset",TimeUtils.getSunset());
            }}).getBytes());
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send time update");
        }
    }
    
    /**
     * Broadcast the clients signon to other clients.
     */
    protected final void broadcastSignon(){
        ClientMessenger.send("ClientService","signOn", 0, new HashMap<String,String>(){{put("name",getClientName());}});
    }
    
    /**
     * Broadcasts the client signing off.
     */
    protected final void broadcastSignOff(){
        ClientMessenger.send("ClientService","signOff", 0, new HashMap<String,String>(){{put("name",getClientName());}});
    }
    
    
    /**
     * Start the client.
     */
    public final void start(){
        t.start();
    }
    
    /**
     * Returns the clients remote address.
     * @return 
     */
    @Override
    public final String getRemoteSocketAddress(){
        return clientSocket.getInetAddress().getHostAddress();
    }
    
    /**
     * Finishes the client.
     */
    @Override
    public final void finish(){
        finish(true);
    }
    
    /**
     * Stops the client thread,disconnects and removes from the message pool.
     * @param away
     */
    public final void finish(boolean away) {
        if(away) {
            broadcastSignOff();
            RemoteClientsAuthentication.deAuthorize(this);
            RemoteClientsConnectionPool.removeClient(this);
        }
        TimeUtils.removeMinuteListener(this);
        out.flush();
        out.close();
        try {
            in.close();
            clientSocket.close();
            LOG.info("Client disconnected: " + getClientName() + " (" + getRemoteHost() + ")");
        } catch (IOException e) {
            LOG.error("Disconnected problem: {}", e.getMessage(),e);
        }
    }

    /**
     * Sends data over the socket.
     * @param message
     * @return 
     */
    @Override
    public boolean sendSocket(String nameSpace,byte[] message) {
        if(message!=null){
            LOG.debug("Socket out:" + getClientName() + " (" + getRemoteHost() + ") -> " + new String(message));
            out.println(new String(message));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the client name.
     * @return 
     */
    @Override
    public String getLoginName() {
        return this.getClientName();
    }

    @Override
    public int getId() {
        return this.clientId;
    }

    @Override
    public boolean getIfCpwd() {
        return false;
    }
    
}
