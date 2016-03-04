/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entities;
import org.pidome.client.entities.EntityErrorBroadcaster;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPC;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPCException;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 * The client's main class.
 * Handles login and session.
 * @author John
 */
public final class PCCClient implements PCCClientInterface,PCCConnectionNameSpaceRPCListener {
    
    static {
        Logger.getLogger(PCCClient.class.getName()).setLevel(Level.ALL);
    }
    
    PCCClientInterface.ClientStatus status = PCCClientInterface.ClientStatus.LOGGED_OUT;
    
    /**
     * Set of listeners.
     */
    private final HashSet<PCCCLientStatusListener> _listeners = new HashSet<>();
    
    /**
     * Settings.
     */
    private final LocalSettingsStorageInterface   settings;
    
    /**
     * Connection proxy.
     */
    private final PCCConnectionInterface connection;
    
    /**
     * Client preferences.
     */
    private final LocalPreferenceStorageInterface preferences;
    
    /**
     * Main object containing all the entities.
     */
    private final Entities entities;
    
    /**
     * Client key.
     */
    private String clientKey = "";
    
    /**
     * Latest known logincode.
     */
    private int loginCode = 0;
    
    /**
     * Holding boolean if a login attempt is made.
     * This also holds true while in authorization progress or logging out.
     */
    private boolean inProgress = false;
    
    /**
     * Client capabilities.
     * By default has the default capabilites object. 
     * Refer to the object for these defaults.
     */
    private ClientCapabilities capabilities = new ClientCapabilities();
    
    /**
     * Constructor.
     * @param settings The PCCSettings object
     * @param preferences The PCCPreferences holding the user preferences.
     * @param connection The PCCConnection server connection object.
     */
    public PCCClient(LocalSettingsStorageInterface settings, LocalPreferenceStorageInterface preferences, PCCConnectionInterface connection){
        this(settings, preferences, connection, true);
    }
    
    /**
     * Constructor.
     * @param settings The PCCSettings object
     * @param preferences The PCCPreferences holding the user preferences.
     * @param connection The PCCConnection server connection object.
     * @param includeEntities Set to false to disable entities initialization.
     */
    public PCCClient(LocalSettingsStorageInterface settings, LocalPreferenceStorageInterface preferences, PCCConnectionInterface connection, boolean includeEntities){
        this.settings    = settings;
        this.connection  = connection;
        this.preferences = preferences;
        this.entities    = new Entities(this.connection);
        if(includeEntities) {
            this.entities.initialize();
        }
        this.connection.setClientListener(this);
        this.connection.addPCCConnectionNameSpaceListener("SystemService", this);
    }
    
    /**
     * Initialize all entities.
     */
    public final void initializeEntitities(){
        if(!this.entities.initialized()){
            this.entities.initialize();
        }
    }
    
    /**
     * Returns the entities object.
     * This object by default is uninitialized. When the user is logged in objects
     * contained by the entities will be available. When the user is logged out
     * for whatever reason all the objects are released.
     * @return all entities registered in the system.
     */
    @Override
    public final Entities getEntities(){
        return entities;
    }
    
    /**
     * Returns the client settings.
     * Returns client preferences.
     * @return The preferences for user preference data.
     */
    @Override
    public final LocalPreferenceStorageInterface getPreferences(){
        return this.preferences;
    }
    
    /**
     * Adds a listener.
     * @param listener Listener for client statuses.
     */
    @Override
    public final void addListener(PCCCLientStatusListener listener){
        _listeners.add(listener);
    }
    
    /**
     * Removes a listener.
     * @param listener Listener for client statuses.
     */
    @Override
    public final void removeListener(PCCCLientStatusListener listener){
        _listeners.remove(listener);
    }
    
    /**
     * Used for automated login.
     */
    @Override
    public final void login(){
        if(this.connection.getConnectionProfile().equals(Profile.FIXED)){
            if(this.settings.getStringSetting("user.login", "").equals("") || this.settings.getStringSetting("user.pass", "").equals("")){
                broadcastLoginEvent(ClientStatus.NO_DATA, 0, "No user data known");
            } else {
                loginFixed(this.settings.getStringSetting("user.login", ""), this.settings.getStringSetting("user.pass", ""));
            }
        } else {
            if(this.settings.getStringSetting("user.login", "").equals("") || this.settings.getStringSetting("user.userinfo", "").equals("")){
                broadcastLoginEvent(ClientStatus.NO_DATA, 0, "No user data known");
            } else {
                loginMobile(this.settings.getStringSetting("user.login", ""), this.settings.getStringSetting("user.userinfo", ""));
            }
        }
    }
    
    /**
     * Used to log off.
     */
    @Override
    public final void logout(){
        new Thread(() -> {
            try {
                broadcastLoginEvent(ClientStatus.BUSY_LOGOUT, 0, "");
                this.connection.sendData(PidomeJSONRPC.createExecMethod("ClientService.signOff", "ClientService.signOff", null));
            } catch (PidomeJSONRPCException | PCCConnectionException ex) {
                Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "No connection", ex);
            }
            clientKey = "";
            loginCode = 0;
            broadcastLoginEvent(ClientStatus.LOGGED_OUT, 0, "");
            this.connection.disconnect();
            this.entities.EmptyServices();
        }).start();
    }
    
    /**
     * Checks if someone is logged in.
     * @return returns true if logged in.
     */
    @Override
    public final boolean isloggedIn(){
        return !clientKey.isEmpty() && loginCode==200;
    }
    
    /**
     * Returns true if authorization is needed.
     * @return truw when authorization is needed.
     */
    @Override
    public final boolean needsAuth(){
        return loginCode == 202;
    }
    
    /**
     * Used for manual connection to the server.
     * @param ip The ip of the server as string.
     * @param port The socket port to be used to connect to
     * @param secure true if the port data is for a secure port.
     */
    @Override
    public final void manualConnect(String ip, int port, boolean secure){
        this.connection.startManualConnection(ip, port, secure);
    }
    
    /**
     * Handles the client login as a fixed display.
     * @param username The fixed client login name to login with.
     * @param password The fixed client password to login with.
     */
    @Override
    public final void loginFixed(String username, String password){
        new Thread(() -> {
            this.settings.setStringSetting("user.login",username);
            this.settings.setStringSetting("user.pass",password);
            Map<String,Object> params = new HashMap<>();
            params.put("loginname", username);
            params.put("password", password);
            params.put("type", "DISPLAY");
            params.put("key", "");
            params.put("clientinfo", "PiDome Client");
            try {
                this.connection.sendData(PidomeJSONRPC.createExecMethod("ClientService.signOn", "ClientService.signOn", params));
            } catch (PCCConnectionException ex) {
                Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Problem connecting", ex);
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 0, "Connection issue: " + ex.getMessage());
            } catch (PidomeJSONRPCException ex) {
                Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Could not create RPC message", ex);
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 0, "System error: " + ex.getMessage());
            }
        }).start();
    }
    
    /**
     * Sends default client capabilities.
     */
    @Override
    public final void sendCapabilities(){
        sendCapabilities(this.capabilities);
    }
    
    /**
     * Sets the new capabilities and sends them.
     * @param capabilities The capabilities to send to the server.
     */
    @Override
    public final void sendCapabilities(ClientCapabilities capabilities){
        this.capabilities = capabilities;
        Map<String,Object> capabs = new HashMap<>();
        capabs.put("displaywidth", capabilities.getDisplayWidth());
        capabs.put("displayheight", capabilities.getDisplayHeight());
        Map<String,Object> params = new HashMap<>();
        params.put("capabilities", capabs);
        try {
            this.connection.sendData(PidomeJSONRPC.createExecMethod("ClientService.setCapabilities", "ClientService.setCapabilities", params));
        } catch (PCCConnectionException | PidomeJSONRPCException ex) {
            Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Could not send capabilities", ex);
        }
    }
    
    /**
     * Handles the client login as a mobile device.
     * @param username The mobile client login name to login with. Mostly a unique device id.
     * @param userinfo The information about the client.
     */
    @Override
    public final void loginMobile(String username, String userinfo){
        new Thread(() -> {
            this.settings.setStringSetting("user.login",username);
            this.settings.setStringSetting("user.userinfo",userinfo);
            Map<String,Object> params = new HashMap<>();
            params.put("loginname", username);
            params.put("type", "MOBILE");
            params.put("key", this.settings.getStringSetting("user.key", ""));
            params.put("clientinfo", userinfo);
            try {
                this.connection.sendData(PidomeJSONRPC.createExecMethod("ClientService.signOn", "ClientService.signOn", params));
            } catch (PCCConnectionException ex) {
                Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Problem connecting", ex);
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 0, "Connection issue: " + ex.getMessage());
            } catch (PidomeJSONRPCException ex) {
                Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Could not create RPC message", ex);
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 0, "System error: " + ex.getMessage());
            }
        }).start();
    }
    
    /**
     * Auth result data handling.
     * @param authData Map containing authentication data.
     * @param isApproval Approval result.
     */
    @Override
    public final void authClient(Map<String,Object> authData, boolean isApproval){
        if(isApproval){
            if(authData.containsKey("approved") && (boolean)authData.get("approved") == true){
                login();
            } else if(authData.containsKey("aproved") && (boolean)authData.get("aproved") == false){
                loginCode = 401;
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 401, "Approval has been denied, not logged in.");
            } else {
                loginCode = 0;
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 0, "Client error: Could not interpret approval data.");
            }
        } else {
            if(authData.containsKey("data")){
                Map<String,Object> data = (Map<String,Object>)authData.get("data");
                if(data.containsKey("auth") && (boolean)data.get("auth")==true){
                    if(data.containsKey("key")){
                        clientKey = (String)data.get("key");
                    }
                    if(data.containsKey("code") && ((Number)data.get("code")).intValue()==202){
                        loginCode = 202;
                        broadcastLoginEvent(ClientStatus.AUTHORIZATION_NEEDED, 202, "Server side authorization required first");
                    } else {
                        loginCode = 200;
                        broadcastLoginEvent(ClientStatus.LOGGED_IN, 200, "Logged in.");
                        sendInitPathsRequest();
                    }
                } else if(data.containsKey("auth") && (boolean)data.get("auth")==false){
                    clientKey = "";
                    loginCode = ((Number)data.get("code")).intValue();
                    broadcastLoginEvent(ClientStatus.FAILED_LOGIN, ((Number)data.get("code")).intValue(), (String)data.get("message"));
                } else {
                    clientKey = "";
                    loginCode = 500;
                    try {
                        broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 500, "Server error: " + (String)authData.get("message"));
                    } catch (Exception ex){
                        broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 500, "Unknown error: " + ex.getMessage());
                    }
                }
            } else {
                broadcastLoginEvent(ClientStatus.FAILED_LOGIN, 0, "Client error: Could not interpret login/logout data.");
            }
        }
    }
    
    /**
     * Sends a client init paths request.
     */
    private void sendInitPathsRequest(){
        try {
            this.connection.sendData(PidomeJSONRPC.createExecMethod("SystemService.getClientInitPaths", "SystemService.getClientInitPaths", null));
        } catch (PCCConnectionException ex) {
            Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Problem sending client init paths request", ex);
        } catch (PidomeJSONRPCException ex) {
            Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Could not create RPC message", ex);
        }
    }
    
    /**
     * Returns the current client status.
     * @return the current client status. Use in combination with the connection status for full information.
     */
    @Override
    public final PCCClientInterface.ClientStatus getCurrentClientStatus(){
        return this.status;
    }
    
    /**
     * Returns if a login progress is busy.
     * @return true when loggin in, auth needed or busy logging out.
     */
    @Override
    public final boolean inLoginProgress(){
        return this.inProgress;
    }
    
    /**
     * Broadcasts client events to listeners.
     * @param status
     * @param message 
     */
    private void broadcastLoginEvent(ClientStatus status, int errCode, String message){
        this.status = status;
        switch(status){
            case AUTHORIZATION_NEEDED:
                inProgress = true;
            break;
            case LOGGED_IN:
                inProgress = false;
            break;
            case LOGGED_OUT:
                inProgress = false;
            break;
            case BUSY_LOGIN:
                inProgress = true;
            break;
            case BUSY_LOGOUT:
                inProgress = true;
            break;
            case FAILED_LOGIN:
                inProgress = false;
            break;
            case NO_DATA:
                inProgress = false;
            break;
            case INIT_ERROR:
                inProgress = false;
            break;
            case INIT_DONE:
                inProgress = false;
            break;
        }
        Iterator<PCCCLientStatusListener> listeners = _listeners.iterator();
        PCCClientEvent event = new PCCClientEvent(status, errCode, message);
        while(listeners.hasNext()){
            listeners.next().handlePCCClientEvent(event);
        }
    }
    
    /**
     * Handles the final resource connection initialization.
     * @param status the PCCConnectionStatus with resource success or fail.
     */
    @Override
    public void handleConnectionResourceInit(PCCConnection.PCCConnectionStatus status){
        switch(status){
            case RESOURCES_SUCCESS:
                this.entities.pulseReady();
                broadcastLoginEvent(ClientStatus.INIT_DONE, 0, "Ready to go");
            break;
            case RESOURCES_FAILED:
                broadcastLoginEvent(ClientStatus.INIT_ERROR, 500, "Server initialization error");
            break;
        }
    }
    
    /**
     * Starts preloading all the data.
     */
    @Override
    public final void startPreloaders(){
        Runnable run = () -> {
            this.entities.runPreloaders();
        };
        run.run();
    }

    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        ////
    }

    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        try{
            switch((String)rpcDataHandler.getId()){
                case "SystemService.getClientInitPaths":
                    SystemServiceInitPathsProvider provider = new SystemServiceInitPathsProvider();
                    try {
                        Map<String,Object> result = (Map<String,Object>)rpcDataHandler.getResult().get("data");
                        provider.setHttpAddress(this.connection.getConnectionData().getHost());
                        provider.setHttpPort(this.connection.getConnectionData().getHttpPort());
                        provider.setRPCLocation((String)result.get("jsonurl"));
                        provider.setIsSecure(this.connection.getConnectionData().isSSL());
                        provider.setSuccess(true);
                        ((SystemServiceInitDataListener)this.connection).handleClientInitPaths(provider);
                    } catch (IllegalArgumentException ex){
                        Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Invalid init paths received (IllegalArgumentException): " + rpcDataHandler.getResult(), ex);
                        EntityErrorBroadcaster.broadcastMessage("Initialization error", "Invalid init paths received: " + ex.getMessage(), ex);
                        provider.setSuccess(false);
                    } catch (Exception ex){
                        Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Invalid init paths received (Other): " + rpcDataHandler.getResult(), ex);
                        EntityErrorBroadcaster.broadcastMessage("Initialization error", "Invalid init paths received: " + ex.getMessage(), ex);
                        provider.setSuccess(false);
                    }
                break;
            }
        } catch (Exception ex){
            Logger.getLogger(PCCClient.class.getName()).log(Level.SEVERE, "Got problem", ex);
        }
    }
    
}