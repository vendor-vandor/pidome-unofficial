/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services.aidl.client;

import android.os.RemoteException;
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
import org.pidome.client.services.aidl.service.SystemServiceAidlInterface;
import org.pidome.client.system.PCCClientInterface;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.client.system.SystemServiceInitPathsProvider;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionDataInterface;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;
import org.pidome.pcl.data.connection.Connection;
import org.pidome.pcl.data.connection.ConnectionException;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.networking.connections.server.ServerConnection;
import org.pidome.pcl.networking.connections.server.ServerConnectionData;

/**
 *
 * @author John
 */
public class PCCConnectionInterfaceAidlProxy implements PCCConnectionInterface {

    static {
        Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * A collection of listeners.
     */
    private final List<PCCConnectionListener> _listeners = new CopyOnWriteArrayList<>();
    
    /**
     * For listening to raw RPC commands.
     */
    private final Map<String,Collection<PCCConnectionNameSpaceRPCListener>> _RPCListeners = new HashMap<>();

    PCCConnectionInterface.PCCConnectionStatus currentStatus = PCCConnectionInterface.PCCConnectionStatus.DISCONNECTED;
    
    private boolean inProgress = false;
    
    SystemServiceAidlInterface connection;
    
    /**
     * Constructor.
     * @param connection The aidl proxy interface.
     * @throws ConnectionException When connection construction fails.
     */
    public PCCConnectionInterfaceAidlProxy(SystemServiceAidlInterface connection) throws ConnectionException {
        this.connection = connection;
    }
    
    /**
     * Closing the connection.
     */
    @Override
    public final void disconnect(){
        if(currentStatus.equals(PCCConnectionStatus.CONNECTED)){
            try {
                this.connection.disconnect();
            } catch (RemoteException ex) {
                Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Returns the connection profile.
     * @return The ServerConnection profile in use.
     */
    @Override
    public final ServerConnection.Profile getConnectionProfile(){
        return ServerConnection.Profile.MOBILE;
    }
    
    /**
     * Returns the current connection status.
     * @return The current connection status.
     */
    @Override
    public final PCCConnectionInterface.PCCConnectionStatus getCurrentStatus(){
        try {
            String statRemote = this.connection.getCurrentConnectionStatusAsString();
            for(PCCConnectionInterface.PCCConnectionStatus stat: PCCConnectionInterface.PCCConnectionStatus.values()){
                if(stat.toString().equals(statRemote)){
                    currentStatus = stat;
                    break;
                }
            }
            return currentStatus;
        } catch (RemoteException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
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
     * Returns the server's connection data.
     * @return Server connection information data.
     */
    @Override
    public final ServerConnectionDataInterface getConnectionData(){
        ServerConnectionData data = new ServerConnectionData();
        try {
            data.setHost(this.connection.getHost());
            data.setHttpPort(this.connection.getHttpPort());
            data.setHttpSSLPort(this.connection.getHttpSSLPort());
            data.setSocketPort(this.connection.getSocketPort());
            data.setSocketSSLPort(this.connection.getSocketSSLPort());
            return data;
        } catch (RemoteException | NullPointerException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
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
        try {
            this.connection.manualConnect(ip, port, secure);
        } catch (RemoteException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Connects if manual data is present.
     */
    @Override
    public final void startInitialConnection(){
        try {
            this.connection.startInitialConnection();
        } catch (RemoteException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns true if manual connect data is available to do a manual connection attempt so search is not needed.
     * @return true if there is initial connection data present.
     */
    @Override
    public final boolean hasInitialManualConnectData(){
        try {
            return this.connection.hasInitialManualConnectData();
        } catch (RemoteException ex) {
            return false;
        }
    }
    
    /**
     * Starts searching for a server.
     */
    @Override
    public final void startSearch(){
        try {
            this.connection.startSearch();
        } catch (RemoteException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Updates the client connection status from supplied string from service.
     * @param status 
     */
    protected final void updateCurrentConnectionStatusFromString(String status){
        for(Connection.ConnectionStatus stat: Connection.ConnectionStatus.values()){
            if(stat.toString().equals(status)){
                handleInternalConnectionStatus(stat);
                break;
            }
        }
    }
    
    protected void broadcastConnectionEventByString(String status, int errCode, String message){
        for(Connection.ConnectionStatus stat: Connection.ConnectionStatus.values()){
            if(stat.toString().equals(status)){
                handleInternalConnectionStatus(stat);
                break;
            }
        }
    }
    
    /**
     * Handles internal connection statuses.
     * @param status current ConnectionStatus
     */
    @Override
    public void handleInternalConnectionStatus(Connection.ConnectionStatus status) {
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
        /// Not used in proxy
    }
    
    protected void broadcastRPCData(String RPCData){
        try {
            PCCEntityDataHandler handler = new PCCEntityDataHandler(RPCData);
            provideRPCListeners(handler);
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Supplies the correct namespace listener with the received RPC container.
     * @param handler the RPC data container.
     */
    void provideRPCListeners(PCCEntityDataHandler handler){
        if(!handler.getNamespace().isEmpty()){
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
        ///Not used.
    }
    
    /**
     * Creates the event data for passing to listeners.
     * @return 
     */
    private PCCConnectionEvent getPCCConnectionEventRemoteData(){
        PCCConnectionEvent event = new PCCConnectionEvent();
        try {
            event.setRemoteSocketAddress(this.connection.getHost());
            event.isSocketSSLConnection(this.connection.getSocketHasSSL());
            if(this.connection.getSocketHasSSL()){
                event.setRemoteSocketPort(this.connection.getSocketSSLPort());
            } else {
                event.setRemoteSocketPort(this.connection.getSocketPort());
            }
            return event;
        } catch (RemoteException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public final PCCEntityDataHandler getJsonHTTPRPC(String method, Map<String,Object> params, String requestId) throws PCCEntityDataHandlerException {
        try {
            Logger.getLogger(PCCConnection.class.getName()).log(Level.FINE, "Executing getJsonHTTPRPC request: {0}, params: {1}", new Object[]{method, params});
            return new PCCEntityDataHandler(this.connection.getJsonHTTPRPCAsString(method, params, requestId));
        } catch (RemoteException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    /**
     * Returns JSON data requested using an http(s) request.
     * @param url The url to get the data from.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    @Override
    public final String getSimpleXmlHttp(String url, Map<String,String> params) throws PCCEntityDataHandlerException{
        try {
            return this.connection.getSimpleXmlHttp(url,params);
        } catch (RemoteException ex) {
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    @Override
    public byte[] getBinaryHttp(String url, Map<String, String> params) throws PCCEntityDataHandlerException {
        try {
            return this.connection.getBinaryHttp(url,params);
        } catch (RemoteException ex) {
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
        } catch (RemoteException ex) {
            throw new PCCConnectionException(ex);
        }
    }
 
    //// We are passing some values as proxy service for the client interface
    protected String getCurrentClientStatusAsString(){
        try {
            return this.connection.getCurrentClientStatusAsString();
        } catch (RemoteException| NullPointerException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
            return "DISCONNECTED";
        }
    }

    protected boolean isClientLoggedIn(){
        try {
            return this.connection.isLoggedIn();
        } catch (RemoteException | NullPointerException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    protected void clientLogin(){
        try {
            this.connection.clientLogin();
        } catch (RemoteException | NullPointerException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected boolean inLoginProgress(){
        try {
            return this.connection.inLoginProgress();
        } catch (RemoteException | NullPointerException ex) {
            Logger.getLogger(PCCConnectionInterfaceAidlProxy.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    @Override
    public void handleClientInitPaths(SystemServiceInitPathsProvider ssipp) {
        /// not used.
    }

    @Override
    public NetworkAvailabilityProvider getNetProvider() {
        throw new UnsupportedOperationException("Not supported in proxy."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCustomBroadcastListener(NetworkBroadcastReceiverInterface nbri) {
        throw new UnsupportedOperationException("Not supported in proxy."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getJsonHTTPRPCAsString(String string, Map<String, Object> map, String string1) throws PCCEntityDataHandlerException {
        throw new UnsupportedOperationException("Not supported in proxy."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPCCConnectionNameSpaceListener(PCCConnectionNameSpaceRPCListener pl) {
        throw new UnsupportedOperationException("Not supported in proxy."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePCCConnectionNameSpaceListener(PCCConnectionNameSpaceRPCListener pl) {
        throw new UnsupportedOperationException("Not supported in proxy."); //To change body of generated methods, choose Tools | Templates.
    }

}