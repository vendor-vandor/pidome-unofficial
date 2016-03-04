/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.server;

/**
 * Handles data from a socket data connection.
 * @author John
 */
public interface ServerConnectionDataListener {
    
    /**
     * Handle connection data.
     * @param data Data to be handled from a connection.
     */
    public void handleConnectionData(String data);
    
}
