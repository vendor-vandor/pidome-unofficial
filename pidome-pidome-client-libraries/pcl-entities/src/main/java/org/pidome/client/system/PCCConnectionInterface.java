/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import java.util.Map;
import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionDataInterface;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;
import org.pidome.pcl.data.connection.Connection;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.networking.connections.server.ServerConnection;

/**
 *
 * @author John
 */
public interface PCCConnectionInterface {
    
    public enum PCCConnectionStatus {
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
        UNAVAILABLE,
        /**
         * Connect failed.
         */
        CONNECT_FAILED,
        /**
         * We correctly got all resource data.
         */
        RESOURCES_SUCCESS,
        /**
         * We did not got valid resource data
         */
        RESOURCES_FAILED;
    }
    
    /**
     * Returns the network provider.
     * @return Provider for network availability signaling.
     */
    public NetworkAvailabilityProvider getNetProvider();
    
    /**
     * Closing the connection.
     */
    public void disconnect();
    
    /**
     * Returns the connection profile.
     * @return The ServerConnection profile in use.
     */
    public ServerConnection.Profile getConnectionProfile();
    
    /**
     * Returns the current connection status.
     * @return The current connection status.
     */
    public PCCConnectionStatus getCurrentStatus();
    
    /**
     * Adds a PCC Connection listener.
     * @param listener Listener for server connection statuses
     */
    public void addPCCConnectionListener(PCCConnectionListener listener);
    
    /**
     * Adds a RPC namespace listener.
     * @param RPCNameSpace The namespace to listen to.
     * @param listener The receiver.
     */
    public void addPCCConnectionNameSpaceListener(String RPCNameSpace, PCCConnectionNameSpaceRPCListener listener);
    
    /**
     * Adds a RPC namespace universal listener.
     * Listeners added here are not bound to a namespace.
     * @param listener The receiver.
     */
    public void addPCCConnectionNameSpaceListener(PCCConnectionNameSpaceRPCListener listener);
    
    /**
     * Removes a RPC namespace universal listener.
     * Listeners removed here are not bound to a namespace.
     * @param listener The receiver.
     */
    public void removePCCConnectionNameSpaceListener(PCCConnectionNameSpaceRPCListener listener);
    
    /**
     * Returns the server's connection data.
     * @return Server connection information data.
     */
    public ServerConnectionDataInterface getConnectionData();
    
    /**
     * Removes a RPC namespace listener.
     * @param RPCNameSpace The namespace where currently is listening to, refer to RPC spec.
     * @param listener The listener.
     */
    public void removePCCConnectionNameSpaceListener(String RPCNameSpace, PCCConnectionNameSpaceRPCListener listener);
    
    /**
     * Removes a PCC Connection listener.
     * @param listener Server connection status listener.
     */
    public void removePCCConnectionListener(PCCConnectionListener listener);
    
    /**
     * Starts a manual connection to the server.
     * @param ip The server's ip address.
     * @param port The server's remote socket port.
     * @param secure Set to true if the ports are secure ports.
     */
    public void startManualConnection(String ip, int port, boolean secure);
    
    /**
     * Connects if manual data is present.
     */
    public void startInitialConnection();
    
    /**
     * Returns true if manual connect data is available to do a manual connection attempt so search is not needed.
     * @return true if there is initial connection data present.
     */
    public boolean hasInitialManualConnectData();
    
    /**
     * Sets a custom broadcast receiver for receiving network broadcasts.
     * @param listener The custom broadcast receiver.
     */
    public void setCustomBroadcastListener(NetworkBroadcastReceiverInterface listener);
    
    /**
     * Starts searching for a server.
     */
    public void startSearch();
    
    /**
     * Handles internal connection statuses.
     * @param status current ConnectionStatus
     */
    public void handleInternalConnectionStatus(Connection.ConnectionStatus status);
    
    /**
     * Returns if a connection progress attempt is made.
     * This function also returns true for searching.
     * @return True if a connection attempt is in progress.
     */
    public boolean inConnectionProgress();
    
    /**
     * Handles data from a connection.
     * @param handler PCCEntityDataHandler containing auth data.
     * @todo Handle empty id's and error codes from server.
     */
    public void handleInternalConnectionData(PCCEntityDataHandler handler);
    
    /**
     * Internal client listener used for auth data. 
     * @param _PCCClient PCCClient for listening to data.
     */
    void setClientListener(PCCClientInterface _PCCClient);

    /**
     * Returns JSON data requested using an http(s) request.
     * @param method The method to be remotely executed.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @param requestId The request id set for this method (usually the method name).
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    public PCCEntityDataHandler getJsonHTTPRPC(String method, Map<String,Object> params, String requestId) throws PCCEntityDataHandlerException;
    
    /**
     * Returns JSON data requested using an http(s) request.
     * @param method The method to be remotely executed.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @param requestId The request id set for this method (usually the method name).
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    public String getJsonHTTPRPCAsString(String method, Map<String,Object> params, String requestId) throws PCCEntityDataHandlerException;
    
    /**
     * Returns binary data requested using an http(s) request.
     * @param url The full url from server root.
     * @param params The parameters for the method. Set this parameter to null of no parameters are used.
     * @return PCCEntityDataHandler containing the result of the request made.
     * @throws PCCEntityDataHandlerException When the request fails.
     */
    public byte[] getBinaryHttp(String url, Map<String,String> params) throws PCCEntityDataHandlerException;
    
    /**
     * Sends data to the server using raw socket.
     * @param data The data to be send over the socket.
     * @throws org.pidome.client.system.PCCConnectionException  When sending data fails.
     */
    public void sendData(String data) throws PCCConnectionException;
 
    /**
     * Handle initialization paths from the server.
     * @param provider SystemServiceInitPathsProvider containing initialization data.
     */
    public void handleClientInitPaths(SystemServiceInitPathsProvider provider);
    
}