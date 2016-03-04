/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.server;

/**
 * Server connection listener interface.
 * @author John
 */
public interface ServerConnectionListener {
    /**
     * Handle connection status.
     * @param status The connection status to be handled.
     */
    public void handleConnectionStatus(ServerConnection.ServerConnectionStatus status);
}
