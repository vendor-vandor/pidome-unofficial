/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.connection;

import java.io.IOException;

/**
 * Interface used to provide the base connection.
 * @author John
 */
public interface ServerConnectionInterface {
    
    /**
     * Possible connection statusses.
     */
    public enum Status {
        /**
         * Connecting to a server resource.
         */
        CONNECTING,
        /**
         * Disconnected from the server.
         */
        DISCONNECTED,
        /**
         * Connected to the server.
         */
        CONNECTED,
        /**
         * Failed to connect.
         */
        CONNECT_FAILED;
    }

    /**
     * Sets the connection data. 
     * @param connectData ServerConnectionDataInterface implementing object
     */
    public void setConnectionData(ServerConnectionDataInterface connectData);
    
    /**
     * Returns the known connection data.
     * @return ServerConnectionDataInterface implementing object.
     */
    public ServerConnectionDataInterface getConnectionData();
    
    /**
     * Starts a connection to the server.
     * @throws java.io.IOException When connection fails.
     */
    public void connect() throws IOException;
    
    /**
     * Stops a connection.
     */
    public void disconnect();
    
}
