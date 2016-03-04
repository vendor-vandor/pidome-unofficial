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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public abstract class RemoteClient {
    
    public static enum Type {
        SOCKET,
        WEB,
        MQTT,
        WEBSOCKET,
        INTERNAL_RPC
    }
    
    public enum DeviceType {
        DISPLAY,MOBILE,WEB
    }
    
    private boolean throttled = false;
    private boolean settingThrottled = false;
    
    static Logger LOG = LogManager.getLogger(RemoteClient.class);
    
    private UUID clientKey = UUID.randomUUID();
    
    private String clientName = "";
    private String remoteHost = "";
    
    private Type type;
    private DeviceType deviceType = DeviceType.WEB;
    private String clientInfo = "";
    
    private long connectedSince;
    
    private ArrayList<RemoteClientsAuthentication.EndPoint> endPoints = new ArrayList();
    
    private boolean connected = false;
    private boolean isSocketClient = false;
    
    private String superLoginName = "";
    
    private boolean hasGPSEnabled = false;
    
    private ClientCapabilities capabilities = new ClientCapabilities();
    
    private int resourceId;
    
    /**
     * Constructs a client.
     * @param type 
     */
    public RemoteClient(Type type){
        this.type = type;
        switch(this.type){
            case SOCKET:
            case WEBSOCKET:
                isSocketClient = true;
            break;
        }
        connectedSince = DateTime.now().getMillis();
    }
    
    /**
     * Set's this client's internal resource id.
     * The resource id is the id from or the fixed or linked clients.
     * @param resourceId 
     */
    public final void setResourceId(int resourceId){
        this.resourceId = resourceId;
    }
    
    /**
     * Returns the linked or fixed client.
     * @return 
     */
    public final int getResourceId(){
        return this.resourceId;
    }
    
    /**
     * Sets client capabilities from json vars.
     * @param capabs 
     */
    public final void setCapabilities(Map<String,Object> capabs){
        if(capabs != null){
            if((capabs.containsKey("displaywidth") && capabs.get("displaywidth") instanceof Number) && 
               (capabs.containsKey("displayheight") && capabs.get("displayheight") instanceof Number)){
                capabilities.setDisplayDimensions(((Number)capabs.get("displaywidth")).doubleValue(), ((Number)capabs.get("displayheight")).doubleValue());
            }
            updateCapabilities();
        }
    }
    
    /**
     * Updates the linked client's capabilities.
     * @param client
     * @param capabs
     * @return
     * @throws PersonsManagementException 
     */
    private void updateCapabilities() {
        try {
            String capabString = PidomeJSONRPCUtils.createNonRPCMethods(capabilities.asMap());
            if(this.deviceType==DeviceType.MOBILE){
                try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                     PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE clients_linked SET `clientsettings`=? WHERE `id`=?");) {
                    prep.setString(1, capabString);
                    prep.setInt(2, resourceId);
                    prep.executeUpdate();
                    LOG.info("Updated capabilities for MOBILE: '{}' with: {}", this.clientName, capabString);
                } catch (SQLException ex) {
                    LOG.error("Problem setting new capabilities {}", ex.getMessage());
                }
            } else if (this.deviceType == DeviceType.DISPLAY){
                try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                     PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE fixedclients SET `clientsettings`=? WHERE `clientname`=?");) {
                    prep.setString(1, capabString);
                    prep.setString(2, this.getClientName());
                    prep.executeUpdate();
                    LOG.info("Updated capabilities for FIXED: '{}' with: {}", this.clientName, capabString);
                } catch (SQLException ex) {
                    LOG.error("Problem setting new capabilities {}", ex.getMessage());
                }
            }
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Problem setting new capabilities {}. Faulty set: {}", ex.getMessage(), capabilities.asMap());
        }
    }
    
    /**
     * Returns capabilities set.
     * @return 
     */
    public final ClientCapabilities getCapabilities(){
        return this.capabilities;
    }
    
    /**
     * Returns true if this client is throttled or not.
     * @return 
     */
    public final boolean throttled(){
        return throttled;
    }
    
    /**
     * Set's an user throttled or not when connecting.
     * @param throttled 
     */
    private void setThrottled(boolean throttled){
        this.throttled = throttled;
    }
    
    /**
     * Set's an user throttled or not as a setting.
     * @param throttled 
     */
    public final void setSettingThrottled(boolean throttled){
        this.settingThrottled = throttled;
        try {
            InetAddress address = InetAddress.getByName(remoteHost);
            if(!address.isSiteLocalAddress()){
                this.setThrottled(settingThrottled);
            } else {
                this.setThrottled(false);
            }
        } catch (UnknownHostException ex) {
            this.setThrottled(false);
        }
    }
    
    /**
     * Sets GPS enabled or not.
     * @param gps 
     */
    public final void setHasGPSEnabled(boolean gps){
        hasGPSEnabled = gps;
    }
    
    /**
     * Gets GPS enabled or not.
     * @return 
     */
    public final boolean getHasGPSEnabled(){
        return hasGPSEnabled;
    }
    
    /**
     * Sets the Person/Deisplay client name
     * @param name 
     */
    public final void setSuperName(String name){
        this.superLoginName = name;
    }
    
    /**
     * Returns the Person/Display name.
     * @return 
     */
    public final String getSuperName(){
        return this.superLoginName;
    }
    
    /**
     * Retuns the client used key.
     * @return 
     */
    public final String getKey(){
        return clientKey.toString();
    }
    
    /**
     * Sets the endpoints to be used for this client.
     * @param endpoints 
     */
    public final void setEndPoints(RemoteClientsAuthentication.EndPoint... endpoints){
        endPoints.clear();
        endPoints.addAll(Arrays.asList(endpoints));
    }
    
    /**
     * Checks if a client may use a specific endpoint.
     * @param endPoint
     * @return 
     */
    public final boolean hasEndpoint(RemoteClientsAuthentication.EndPoint endPoint){
        return endPoints.contains(endPoint);
    }
    
    /**
     * Returns the client type.
     * @return 
     */
    public final Type getType(){
        return type;
    }
    
    /**
     * Checks if a client is authorized.
     * @return 
     */
    public final boolean isAuthorized(){
        return RemoteClientsAuthentication.isAuthorized(this);
    }
    
    /**
     * Sets the client's ip address.
     * @param remoteHost 
     */
    public final void setRemoteHost(String remoteHost){
        this.remoteHost = remoteHost;
    }
    
    /**
     * Returns the client's ip address.
     * @return 
     */
    public final String getRemoteHost(){
        return remoteHost;
    }
    
    /**
     * Sets the client name.
     * @param name 
     */
    public final void setClientName(String name){
        clientName = name;
    }
    
    /**
     * Sets some describing client info
     * @param info 
     */
    public final void setClientInfo(String info){
        clientInfo = info;
    }
    
    /**
     * Returns a simple client info string
     * @return 
     */
    public final String getClientInfo(){
        return clientInfo;
    }
    
    /**
     * Sets the device type.
     * @param type 
     */
    public final void setDeviceType(DeviceType type){
        deviceType = type;
    }
    
    /**
     * Returns the device type.
     * @return 
     */
    public final DeviceType getDeviceType(){
        return deviceType;
    }
    
    /**
     * Returns the client name.
     * @return 
     */
    public final String getClientName(){
        return clientName;
    }
    
    /**
     * Returns the date since this client connected (From successful login).
     * @return 
     */
    public final String getConnectedSinceDateTime(){
        DateTime dt = new DateTime(connectedSince);
        return TimeUtils.composeDDMMYYYYDate(dt.getDayOfMonth(), dt.getMonthOfYear(), dt.getYear()) + " " + TimeUtils.compose24Hours(dt.getHourOfDay(), dt.getMinuteOfHour());
    }
    
    /**
     * Returns the connection duration in seconds.
     * @return 
     */
    public final int getConnectionDuration(){
        return (int)((DateTime.now().getMillis() - connectedSince)/1000);
    }

    /**
     * Routine for extending class.
     * @param message
     * @return 
     */
    public abstract boolean sendSocket(String nameSpace, byte[] message);
    
    /**
     * Returns the remote client's ip address.
     * @return 
     */
    public abstract String getRemoteSocketAddress();
    
    /**
     * Should be implemented to clean up when disconnecting/disconnected
     */
    public abstract void finish();
    
    /**
     * Sends data to a socket connected client.
     * Non socket clients (EQ MQTT which is a loose instance) will always return true.
     * @param message 
     * @return  
     */
    public final boolean sendMsg(String nameSpace, byte[] message){
        return (isSocketClient==true)?sendSocket(nameSpace,message):true;
    }

    /**
     * Set this remote client connected.
     */
    public final void setConnected(){
        connected = true;
    }
    
    /**
     * Returns if the clients socket is connected.
     * @return 
     */
    public final boolean isConnected(){
        return connected;
    }
    
}