/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionDataInterface;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionListenerException;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import static org.pidome.pcl.data.connection.Connection.ConnectionStatus;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.networking.CertHandler;
import org.pidome.pcl.networking.CertHandlerException;
import org.pidome.pcl.networking.connections.broadcasts.BroadcastParser;
import org.pidome.pcl.networking.connections.broadcasts.BroadcastParserException;
import org.pidome.pcl.networking.connections.broadcasts.BroadcastReceiver;
import org.pidome.pcl.networking.connections.broadcasts.BroadcastReceiverEvent;
import org.pidome.pcl.networking.connections.broadcasts.BroadcastReceiverListener;
import org.pidome.pcl.networking.connections.server.ServerConnection;
import org.pidome.pcl.networking.connections.server.ServerConnection.ServerConnectionStatus;
import org.pidome.pcl.networking.connections.server.ServerConnectionData;
import org.pidome.pcl.networking.connections.server.ServerConnectionDataListener;
import org.pidome.pcl.networking.connections.server.ServerConnectionListener;
import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;

/**
 * Main class for handling all the connection based actions.
 * This class handles broadcast and connection handles. It also provides connection
 * resources.
 * @author John
 */
public final class Connection implements ServerConnectionListener,BroadcastReceiverListener,ServerConnectionDataListener {

    static {
        Logger.getLogger(Connection.class.getName()).setLevel(Level.ALL);
    }
    
    private String httpHost = "";
    private int httpPort    = 0;
    private boolean httpSSL = false;
    private String RPCUrl   = "";
    
    /**
     * Possible server connection and search statuses.
     */
    public enum ConnectionStatus {
        /**
         * Searching for server.
         */
        SEARCHING,
        /**
         * Server found.
         */
        FOUND,
        /**
         * Server not found.
         */
        NOT_FOUND,
        /**
         * Connecting.
         */
        CONNECTING,
        /**
         * Failed to connect.
         */
        CONNECT_FAILED,
        /**
         * Connected.
         */
        CONNECTED,
        /**
         * Disconnected.
         */
        DISCONNECTED,
        /**
         * Not possible to create a connection.
         */
        UNAVAILABLE;
    }
    
    /**
     * Connection listener.
     */
    private ConnectionListener _listener;
    
    /**
     * System connection.
     */
    private final ServerConnection connection;
    
    /**
     * System settings.
     */
    private LocalSettingsStorageInterface settings;
    
    /**
     * Provider for network availability.
     */
    private NetworkAvailabilityProvider netProvider;
    
    /**
     * Replacement network broadcast receiver.
     */
    private NetworkBroadcastReceiverInterface broadcastReceiverReplacement;
    
    /**
     * Constructor.
     * @param settings Settings instance.
     * @param profile The connection profile used, fixed for raw socket, mobile for websocket.
     * @param netProvider Network availability provider.
     * @throws org.pidome.pcl.data.connection.ConnectionException When connection can not be made.
     */
    public Connection(LocalSettingsStorageInterface settings, ServerConnection.Profile profile, NetworkAvailabilityProvider netProvider) throws ConnectionException {
        this.settings = settings;
        this.netProvider = netProvider;
        connection = new ServerConnection(profile);
        try {
            CertHandler.init();
        } catch (CertHandlerException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.WARNING, "No SSL", ex);
        }
        try {
            connection.addConnectionListener(this);
        } catch (ServerConnectionListenerException ex) {
            throw new ConnectionException("Problem listening for connection status: " + ex.getMessage());
        }
    }
    
    /**
     * Returns the network provider.
     * @return Provider for network availability signaling.
     */
    public final NetworkAvailabilityProvider getNetProvider(){
        return this.netProvider;
    }
    
    /**
     * Set a replacement broadcast receiver.
     * When the default receiver (desktop profile based) is not sufficient replace
     * it with a platform specific replacement. For example Android requires a
     * specific lock with the wifi manager which a desktop would not need.
     * @param newBroadcastReceiver The broadcast receiver to be used instead of the default one.
     */
    public final void setBroadcastReceiver(NetworkBroadcastReceiverInterface newBroadcastReceiver){
        this.broadcastReceiverReplacement = newBroadcastReceiver;
    }
    
    /**
     * Disconnect the stuff.
     */
    public final void disconnect(){
        this.connection.disconnect();
    }
    
    /**
     * Sets the main connection listener.
     * @param listener Connection listener.
     */
    public final void setListener(ConnectionListener listener){
        this._listener = listener;
    }
    
    /**
     * Starts searching for the PiDome server.
     * @throws UnsupportedOperationException When searching is not possible.
     */
    public final void search() throws UnsupportedOperationException {
        Runnable run = () -> {
            BroadcastReceiver broadcast;
            if(this.broadcastReceiverReplacement!=null){
                broadcast = new BroadcastReceiver(this.netProvider, this.broadcastReceiverReplacement);
            } else {
                broadcast = new BroadcastReceiver(this.netProvider);
            }
            int port = this.settings.getIntSetting("broadcast.port", 0);
            if(port==0){
                port = 10000;
            }
            broadcast.listen(this, port);
        };
        run.run();
    }
    
    /**
     * Sends data over this connection's socket.
     * @param sendData Data to be send.
     * @throws org.pidome.pcl.data.connection.ConnectionException  When there is no connection available.
     */
    public final void sendData(String sendData) throws ConnectionException {
        try {
            Logger.getLogger(Connection.class.getName()).log(Level.FINE, "Sending data to server: {0}", sendData);
            this.connection.sendData(sendData);
        } catch (IOException ex) {
            throw new ConnectionException(ex);
        }
    }
    
    /**
     * Handles updates in the broadcast status.
     * @param event Broadcast event.
     */
    @Override
    public void handleBroadcastReceiverEvent(BroadcastReceiverEvent event) {
        switch(event.getEventType()){
            case START:
                this._listener.handleInternalConnectionStatus(ConnectionStatus.SEARCHING);
            break;
            case NOT_FOUND:
                event.getSource().stopListener(this);
                this._listener.handleInternalConnectionStatus(ConnectionStatus.NOT_FOUND);
            break;
            case FOUND:
                event.getSource().stopListener(this);
                this.settings.setIntSetting("broadcast.port", event.getSource().getPort());
                BroadcastParser broadcast;
                try {
                    broadcast = new BroadcastParser(event.getSource().getBroadcastMessage(), connection.getConnectionProfile());
                    setConnectionData(broadcast.getHost(), broadcast.getPort(), broadcast.isSSL());
                    this._listener.handleInternalConnectionStatus(ConnectionStatus.FOUND);
                    connect();
                } catch (BroadcastParserException ex) {
                    Logger.getLogger(BroadcastReceiver.class.getName()).log(Level.SEVERE, "Problem parsing broadcast", ex);
                    this._listener.handleInternalConnectionStatus(ConnectionStatus.NOT_FOUND);
                }
            break;
        }
    }
    
    /**
     * Sets new connection data
     */
    private void setConnectionData(String host, int port, boolean isSSL){
        ServerConnectionData connectData = new ServerConnectionData();
        connectData.setHost(host);
        connectData.setSocketPort(port);
        connectData.setIsSSL(isSSL);
        this.connection.setConnectionData((ServerConnectionDataInterface)connectData);
    }
    
    /**
     * Sets the http connction data.
     * @param host The http host.
     * @param port The http port.
     * @param secure If the port is a secure one (SSL).
     * @param RPCUrl The location of the RPC.
     */
    public final void setHttpData(String host, int port, boolean secure, String RPCUrl){
        if (this.httpHost.equals("")) this.httpHost = host;
        if (this.httpPort==0) this.httpPort = port;
        this.httpSSL  = secure;
        this.RPCUrl   = RPCUrl;
        Logger.getLogger(Connection.class.getName()).log(Level.FINE, "Setting http data: {0}", Arrays.asList(this.httpHost,this.httpPort,this.httpSSL,this.RPCUrl));
    }
    
    /**
     * Checks if all minimal http data is there.
     * @return 
     */
    private boolean hasHttpPrerequisites(){
        return !httpHost.isEmpty() && this.httpPort!=0 && !RPCUrl.isEmpty();
    }
    
    /**
     * Returns a PCCEntityDataHandler result from the given RPC method.
     * @param method Combination of namespace.method
     * @param params The parameters to be send as string,object map.
     * @param requestId The id of this request.
     * @return The request result string as JSON-RPC.
     * @throws org.pidome.pcl.data.parser.PCCEntityDataHandlerException When RPC data fails
     * @throws org.pidome.pcl.data.connection.ConnectionException When there is no connection possible.
     */
    public final PCCEntityDataHandler getJsonHTTPRPC(String method, Map<String, Object> params, String requestId) throws PCCEntityDataHandlerException,ConnectionException {
        if(hasHttpPrerequisites()){
            try {
                JSONConnector connector = new JSONConnector(httpHost, httpPort, RPCUrl, httpSSL);
                return connector.getJSON(method, params, requestId);
            } catch (MalformedURLException ex){
                throw new ConnectionException("Incorrect URL setup used for RPC: " + ex.getMessage());
            }
        } else {
            throw new ConnectionException("Not all http pre-requisites have been met");
        }
    }
    
    /**
     * Returns a String result from the given RPC method.
     * @param method Combination of namespace.method
     * @param params The parameters to be send as string,object map.
     * @param requestId The id of this request.
     * @return The request result string as JSON-RPC.
     * @throws org.pidome.pcl.data.parser.PCCEntityDataHandlerException When RPC data fails
     * @throws org.pidome.pcl.data.connection.ConnectionException When there is no connection possible.
     */
    public final String getJsonHTTPRPCAsString(String method, Map<String, Object> params, String requestId) throws PCCEntityDataHandlerException,ConnectionException {
        if(hasHttpPrerequisites()){
            try {
                JSONConnector connector = new JSONConnector(httpHost, httpPort, RPCUrl, httpSSL);
                return connector.getJSONAsString(method, params, requestId);
            } catch (MalformedURLException ex){
                throw new ConnectionException("Incorrect URL setup used for RPC: " + ex.getMessage());
            }
        } else {
            throw new ConnectionException("Not all http pre-requisites have been met");
        }
    }
    
    /**
     * Returns binary data from the server.
     * @param url The url absolute from the server path.
     * @param params The parameters to be send as string,object map.
     * @return byte[] Binary data.
     * @throws org.pidome.pcl.data.connection.ConnectionException When data can not be fetched or prerequisites have not been met.
     */
    public final byte[] getBinaryHTTPData(String url, Map<String, String> params) throws ConnectionException {
        if(hasHttpPrerequisites()){
            try {
                BinaryHttpDataConnector connector = new BinaryHttpDataConnector(httpHost, httpPort, RPCUrl, httpSSL);
                return connector.getHttpBinaryData(url, params);
            } catch (IOException ex){
                throw new ConnectionException("Incorrect URL setup used for binary data: " + ex.getMessage());
            }
        } else {
            throw new ConnectionException("Not all http pre-requisites have been met");
        }
    }
    
    
    /**
     * Used when settings are already known.
     */
    private void connect(){
        try {
            this.connection.connect();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Connect to a server resource.
     * @param host The remote host.
     * @param port The remote socket port
     * @param secure Set to true for secure connections.
     */
    public final void connect(String host, int port, boolean secure){
        setConnectionData(host, port, secure);
        this.httpHost = host;
        this.httpSSL  = secure;
        try {
            this.connection.connect();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns the current known connection data.
     * @return Server connection data.
     */
    public final ServerConnectionData getConnectionData(){
        return (ServerConnectionData)this.connection.getConnectionData();
    }
    
    /**
     * Handles the server's connection status.
     * @param status The server connection status.
     */
    @Override
    public void handleConnectionStatus(ServerConnectionStatus status) {
        switch(status){
            case CONNECTING:
                this._listener.handleInternalConnectionStatus(ConnectionStatus.CONNECTING);
            break;
            case CONNECTED:
                this._listener.handleInternalConnectionStatus(ConnectionStatus.CONNECTED);
                this.connection.addDataConnectionListener(this);
            break;
            case DISCONNECTED:
                this._listener.handleInternalConnectionStatus(ConnectionStatus.DISCONNECTED);
                this.connection.removeDataConnectionListener(this);
            break;
            case CONNECT_FAILED:
                this._listener.handleInternalConnectionStatus(ConnectionStatus.CONNECT_FAILED);
            break;
        }
    }
    
    /**
     * Handles data from a connection.
     * @param data Data received from the server socket.
     */
    @Override
    public void handleConnectionData(String data) {
        Logger.getLogger(Connection.class.getName()).log(Level.FINE, "Got data from server: {0}", data);
        Runnable run = () -> {
            try {
                PCCEntityDataHandler handler = new PCCEntityDataHandler(data);
                this._listener.handleInternalConnectionData(handler);
            } catch (PCCEntityDataHandlerException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        };
        run.run();
    }
    
}