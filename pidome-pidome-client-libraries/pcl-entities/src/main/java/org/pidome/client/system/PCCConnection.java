/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.pidome.client.system.PCCConnection.PCCConnectionStatus;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionDataInterface;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.pidome.pcl.data.connection.Connection;
import org.pidome.pcl.data.connection.Connection.ConnectionStatus;
import org.pidome.pcl.data.connection.ConnectionException;
import org.pidome.pcl.data.connection.ConnectionListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.networking.connections.server.ServerConnection;

/**
 * Connection proxy class for front-end
 * @author John
 */
public final class PCCConnection implements PCCConnectionInterface,ConnectionListener,SystemServiceInitDataListener {

    static {
        Logger.getLogger(PCCConnection.class.getName()).setLevel(Level.ALL);
    }
    
    private final Connection connection;
    private final LocalSettingsStorageInterface settings;
    
    /**
     * A collection of listeners.
     */
    private List<PCCConnectionListener> _listeners = new CopyOnWriteArrayList<>();
    
    /**
     * For listening to raw RPC commands.
     */
    private Map<String,Collection<PCCConnectionNameSpaceRPCListener>> _RPCListeners = new HashMap<>();

    PCCConnectionInterface.PCCConnectionStatus currentStatus = PCCConnectionInterface.PCCConnectionStatus.DISCONNECTED;
    
    /**
     * PCC Client.
     */
    PCCClientInterface _PCCClient;
    
    /**
     * The profile used to connect with.
     */
    ServerConnection.Profile profile;
    
    /**
     * A boolean telling if a connection effort is in progress now.
     * This includes the searching progress.
     */
    private boolean inProgress = false;
    
    /**
     * Constructor.
     * @param settings The Settings oject.
     * @param profile The connection profile to be used.
     * @param netProvider Provider for network availability signaling.
     * @throws ConnectionException When connection construction fails.
     */
    public PCCConnection(LocalSettingsStorageInterface settings, ServerConnection.Profile profile, NetworkAvailabilityProvider netProvider) throws ConnectionException {
        this.profile = profile;
        this.settings = settings;
        this.connection = new Connection(settings, profile, netProvider);
        this.connection.setListener(this);
    }
    
    /**
     * Returns the network provider.
     * @return Provider for network availability signaling.
     */
    @Override
    public final NetworkAvailabilityProvider getNetProvider(){
        return this.connection.getNetProvider();
    }
    
    /**
     * Closing the connection.
     */
    @Override
    public final void disconnect(){
        if(currentStatus.equals(PCCConnectionStatus.CONNECTED)){
            this.connection.disconnect();
        }
    }
    
    /**
     * Returns the connection profile.
     * @return The ServerConnection profile in use.
     */
    @Override
    public final ServerConnection.Profile getConnectionProfile(){
        return this.profile;
    }
    
    /**
     * Returns the current connection status.
     * @return The current connection status.
     */
    @Override
    public final PCCConnectionInterface.PCCConnectionStatus getCurrentStatus(){
        return currentStatus;
    }
    
    /**
     * Adds a PCC Connection listener.
     * @param listener Listener for server connection statuses
     */
    @Override
    public final void addPCCConnectionListener(PCCConnectionListener listener){
        if(!_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    /**
     * Adds a RPC namespace listener.
     * @param RPCNameSpace The namespace to listen to.
     * @param listener The receiver.
     */
    @Override
    public final void addPCCConnectionNameSpaceListener(String RPCNameSpace, PCCConnectionNameSpaceRPCListener listener){
        if(_RPCListeners.containsKey(RPCNameSpace)){
            _RPCListeners.get(RPCNameSpace).add(listener);
        } else {
            _RPCListeners.put(RPCNameSpace, new HashSet<PCCConnectionNameSpaceRPCListener>(){{
                add(listener);
            }});
        }
    }
    
    /**
     * Adds a RPC namespace universal listener.
     * Listeners added here are not bound to a namespace.
     * @param listener The receiver.
     */
    @Override
    public final void addPCCConnectionNameSpaceListener(PCCConnectionNameSpaceRPCListener listener){
        if(_RPCListeners.containsKey("non-rpc")){
            _RPCListeners.get("non-rpc").add(listener);
        } else {
            _RPCListeners.put("non-rpc", new HashSet<PCCConnectionNameSpaceRPCListener>(){{
                add(listener);
            }});
        }
    }
    
    /**
     * Removes a RPC namespace universal listener.
     * Listeners removed here are not bound to a namespace.
     * @param listener The receiver.
     */
    @Override
    public final void removePCCConnectionNameSpaceListener(PCCConnectionNameSpaceRPCListener listener){
        if(_RPCListeners.containsKey("non-rpc")){
            _RPCListeners.get("non-rpc").remove(listener);
        }
    }
    
    /**
     * Returns the server's connection data.
     * @return Server connection information data.
     */
    @Override
    public final ServerConnectionDataInterface getConnectionData(){
        return this.connection.getConnectionData();
    }
    
    /**
     * Removes a RPC namespace listener.
     * @param RPCNameSpace The namespace where currently is listening to, refer to RPC spec.
     * @param listener The listener.
     */
    @Override
    public final void removePCCConnectionNameSpaceListener(String RPCNameSpace, PCCConnectionNameSpaceRPCListener listener){
        if(_RPCListeners.containsKey(RPCNameSpace)){
            _RPCListeners.get(RPCNameSpace).remove(listener);
        }
    }
    
    /**
     * Removes a PCC Connection listener.
     * @param listener Server connection status listener.
     */
    @Override
    public final void removePCCConnectionListener(PCCConnectionListener listener){
        _listeners.remove(listener);
    }
    
    /**
     * Provides listeners the current connection status.
     * @param status 
     */
    private void provideListeners(PCCConnectionStatus status){
        currentStatus = status;
        Iterator<PCCConnectionListener> iterate = _listeners.iterator();
        while (iterate.hasNext()){
           Runnable run = () -> {
               iterate.next().handlePCCConnectionEvent(status, new PCCConnectionEvent());
           };
           run.run();
        }
    }
    
    /**
     * Provides listeners the current connection status.
     * @param status 
     */
    private void provideListeners(PCCConnectionStatus status, PCCConnectionEvent event){
        currentStatus = status;
        Iterator<PCCConnectionListener> iterate = _listeners.iterator();
        while (iterate.hasNext()){
           Runnable run = () -> {
               iterate.next().handlePCCConnectionEvent(status, event);
           };
           run.run();
        }
    }
    
    /**
     * Starts a manual connection to the server.
     * @param ip The server's ip address.
     * @param port The server's remote socket port.
     * @param secure Set to true if the ports are secure ports.
     */
    @Override
    public final void startManualConnection(String ip, int port, boolean secure){
        this.connection.connect(ip, port, secure);
    }
    
    /**
     * Connects if manual data is present.
     */
    @Override
    public final void startInitialConnection(){
        if (hasInitialManualConnectData()){
            boolean secure = false;
            int socketPort;
            String remoteHost = this.settings.getStringSetting("server.address", "");
            socketPort = this.settings.getIntSetting("server.socket.port", 0);
            if(this.settings.getBoolSetting("server.socket.ssl", false)!=false){
                secure = true;
            }
            startManualConnection(remoteHost, socketPort,secure);
        }
    }
    
    /**
     * Returns true if manual connect data is available to do a manual connection attempt so search is not needed.
     * @return true if there is initial connection data present.
     */
    @Override
    public final boolean hasInitialManualConnectData(){
        return (!this.settings.getStringSetting("server.address","").equals("") && this.settings.getIntSetting("server.socket.port", 0)!=0);
    }
    
    /**
     * Sets a custom broadcast receiver for receiving network broadcasts.
     * @param listener The custom broadcast receiver.
     */
    @Override
    public final void setCustomBroadcastListener(NetworkBroadcastReceiverInterface listener){
        this.connection.setBroadcastReceiver(listener);
    }
    
    /**
     * Starts searching for a server.
     */
    @Override
    public final void startSearch(){
        Thread search = new Thread(() -> {
            this.connection.search();
        });
        search.start();
    }
    
    /**
     * Handles internal connection statuses.
     * @param status current ConnectionStatus
     */
    @Override
    public void handleInternalConnectionStatus(ConnectionStatus status) {
        Runnable run = () -> {
            switch(status){
                case SEARCHING:
                    inProgress = true;
                    provideListeners(PCCConnectionStatus.SEARCHING);
                break;
                case FOUND:
                    inProgress = true;
                    provideListeners(PCCConnectionStatus.FOUND, getPCCConnectionEventRemoteData());
                break;
                case NOT_FOUND:
                    inProgress = false;
                    provideListeners(PCCConnectionStatus.NOT_FOUND);
                break;
                case CONNECTING:
                    inProgress = true;
                    provideListeners(PCCConnectionStatus.CONNECTING, getPCCConnectionEventRemoteData());
                break;
                case CONNECTED:
                    inProgress = false;
                    try {
                        this.settings.setStringSetting("server.address",    this.connection.getConnectionData().getHost());
                        this.settings.setIntSetting   ("server.socket.port",this.connection.getConnectionData().getSocketPort());
                        this.settings.setBoolSetting  ("server.socket.ssl", this.connection.getConnectionData().isSSL());
                        try {
                            this.settings.storeSettings();
                        } catch (IOException ex) {
                            Logger.getLogger(PCCConnection.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } catch (Exception ex){
                        Logger.getLogger(PCCConnection.class.getName()).log(Level.WARNING, "Not possible to save connection data (" +ex.getCause().getMessage()+ "): " + ex.getMessage(),ex);
                    }
                    provideListeners(PCCConnectionStatus.CONNECTED, getPCCConnectionEventRemoteData());
                break;
                case DISCONNECTED:
                    inProgress = false;
                    provideListeners(PCCConnectionStatus.DISCONNECTED, getPCCConnectionEventRemoteData());
                break;
                case CONNECT_FAILED:
                    inProgress = false;
                    provideListeners(PCCConnectionStatus.CONNECT_FAILED, getPCCConnectionEventRemoteData());
                break;
                case UNAVAILABLE:
                    inProgress = false;
                    provideListeners(PCCConnectionStatus.UNAVAILABLE);
                break;
            }
        };
        run.run();
    }
    
    /**
     * Returns if a connection progress attempt is made.
     * This function also returns true for searching.
     * @return True if a connection attempt is in progress.
     */
    @Override
    public final boolean inConnectionProgress(){
        return this.inProgress;
    }
    
    /**
     * Handles data from a connection.
     * @param handler PCCEntityDataHandler containing auth data.
     * @todo Handle empty id's and error codes from server.
     */
    @Override
    public void handleInternalConnectionData(PCCEntityDataHandler handler) {
        try {
            if(handler.getId().equals("ClientService.signOn")){
                _PCCClient.authClient(handler.getResult(), false);
            } else if(handler.getId().equals("ClientService.signOff")){
                _PCCClient.authClient(handler.getResult(), false);
            } else if(handler.getNamespace().equals("ClientService") && handler.getMethod().equals("approveClient")){
                _PCCClient.authClient(handler.getParameters(), true);
            } else {
                provideRPCListeners(handler);
            }
        } catch (NullPointerException ex){
            //// Unsupported operation done.
        }
    }
    
    /**
     * Supplies the correct namespace listener with the received RPC container.
     * @param handler the RPC data container.
     */
    void provideRPCListeners(PCCEntityDataHandler handler){
        if(!handler.getNamespace().isEmpty()){
            if(_RPCListeners.containsKey("non-rpc")){
                Iterator<PCCConnectionNameSpaceRPCListener> iterate = _RPCListeners.get("non-rpc").iterator();
                while (iterate.hasNext()){
                   Runnable run = () -> {
                       iterate.next().handleRPCCommandByBroadcast(handler);
                   };
                   run.run();
                }
            }
            if(_RPCListeners.containsKey(handler.getNamespace())){
                Iterator<PCCConnectionNameSpaceRPCListener> iterate = _RPCListeners.get(handler.getNamespace()).iterator();
                while (iterate.hasNext()){
                   Runnable run = () -> {
                       iterate.next().handleRPCCommandByBroadcast(handler);
                   };
                   run.run();
                }
            }
        } else if(!((String)handler.getId()).isEmpty()){
            try {
                String rpcNameSpace = ((String)handler.getId()).split("\\.")[0];
                if(_RPCListeners.containsKey("non-rpc")){
                    Iterator<PCCConnectionNameSpaceRPCListener> iterate = _RPCListeners.get("non-rpc").iterator();
                    while (iterate.hasNext()){
                       Runnable run = () -> {
                           iterate.next().handleRPCCommandByResult(handler);
                       };
                       run.run();
                    }
                }
                if(_RPCListeners.containsKey(rpcNameSpace)){
                    Iterator<PCCConnectionNameSpaceRPCListener> iterate = _RPCListeners.get(rpcNameSpace).iterator();
                    while (iterate.hasNext()){
                       Runnable run = () -> {
                           iterate.next().handleRPCCommandByResult(handler);
                       };
                       run.run();
                    }
                }
            } catch (Exception ex){
                Logger.getLogger(PCCConnection.class.getName()).log(Level.SEVERE, "Illegal RPC id: {0}", Arrays.asList(handler.getId(),ex));
            }
        }
    }
    
    /**
     * Internal client listener used for auth data. 
     * @param _PCCClient PCCClient for listening to data.
     */
    @Override
    public void setClientListener(PCCClientInterface _PCCClient){
        this._PCCClient = _PCCClient;
    }
    
    /**
     * Creates the event data for passing to listeners.
     * @return 
     */
    private PCCConnectionEvent getPCCConnectionEventRemoteData(){
        PCCConnectionEvent event = new PCCConnectionEvent();
        event.setRemoteSocketAddress(this.connection.getConnectionData().getHost());
        event.isSocketSSLConnection(this.connection.getConnectionData().isSSL());
        event.setRemoteSocketPort(this.connection.getConnectionData().getSocketPort());
        return event;
    }
    
    /**
     * Returns JSON data requested using an http(s) request.
     * @param method The method to be remotely executed.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @param requestId The request id set for this method (usually the method name).
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    @Override
    public final PCCEntityDataHandler getJsonHTTPRPC(String method, Map<String,Object> params, String requestId) throws PCCEntityDataHandlerException{
        try {
            Logger.getLogger(PCCConnection.class.getName()).log(Level.FINE, "Executing getJsonHTTPRPC request: {0}, params: {1}", new Object[]{method, params});
            return this.connection.getJsonHTTPRPC(method, params, requestId);
        } catch (ConnectionException ex) {
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    /**
     * Returns JSON data as string requested using an http(s) request.
     * @param method The method to be remotely executed.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @param requestId The request id set for this method (usually the method name).
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    @Override
    public String getJsonHTTPRPCAsString(String method, Map<String, Object> params, String requestId) throws PCCEntityDataHandlerException {
        try {
            Logger.getLogger(PCCConnection.class.getName()).log(Level.FINE, "Executing getJsonHTTPRPC request: {0}, params: {1}", new Object[]{method, params});
            return this.connection.getJsonHTTPRPCAsString(method, params, requestId);
        } catch (ConnectionException ex) {
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    /**
     * Returns binar data requested using an http(s) request.
     * @param url Url to get the info from.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    @Override
    public final byte[] getBinaryHttp(String url, Map<String,String> params) throws PCCEntityDataHandlerException{
        try {
            return this.connection.getBinaryHTTPData(url, params);
        } catch (ConnectionException ex) {
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    /**
     * Sends data to the server using raw socket.
     * @param data The data to be send over the socket.
     * @throws org.pidome.client.system.PCCConnectionException  When sending data fails.
     */
    @Override
    public final void sendData(String data) throws PCCConnectionException {
        try {
            this.connection.sendData(data);
        } catch (ConnectionException ex) {
            throw new PCCConnectionException(ex);
        }
    }
 
    /**
     * Handle initialization paths from the server.
     * @param provider SystemServiceInitPathsProvider containing initialization data.
     */
    @Override
    public void handleClientInitPaths(SystemServiceInitPathsProvider provider) {
        if(provider.getSuccess()){
            this.connection.setHttpData(provider.getHttpAddress(), 
                                        provider.getHttpPort(), 
                                        provider.getIsSecure(), 
                                        provider.getRPCUrl());
            this.settings.setStringSetting("server.http.port", String.valueOf(provider.getHttpPort()));
            this.settings.setBoolSetting("server.http.port.ssl",  provider.getIsSecure());
            _PCCClient.handleConnectionResourceInit(PCCConnectionStatus.RESOURCES_SUCCESS);
        } else {
            _PCCClient.handleConnectionResourceInit(PCCConnectionStatus.RESOURCES_FAILED);
        }
    }

}
