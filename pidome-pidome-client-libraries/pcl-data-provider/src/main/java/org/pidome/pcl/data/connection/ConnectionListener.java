/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.connection;

import org.pidome.pcl.data.connection.Connection.ConnectionStatus;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;

/**
 * Listener interface for connection state and data.
 * @author John
 */
public interface ConnectionListener {
    
    /**
     * Handles the connection status.
     * @param status The connection status to be handled.
     */
    public void handleInternalConnectionStatus(ConnectionStatus status);
    
    /**
     * Handles data from a connection.
     * @param handler JSON-RPC data handler.
     */
    public void handleInternalConnectionData(PCCEntityDataHandler handler);
    
}
