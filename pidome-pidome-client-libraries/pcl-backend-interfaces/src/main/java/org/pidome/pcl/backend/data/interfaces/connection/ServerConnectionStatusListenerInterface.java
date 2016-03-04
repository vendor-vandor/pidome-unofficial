/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.connection;

import org.pidome.pcl.backend.data.interfaces.connection.ServerConnectionInterface.Status;

/**
 * Interface for internal connection listeners.
 * @author John
 */
public interface ServerConnectionStatusListenerInterface {
    
    /**
     * Handles a connection status
     * @param status The connection status.
     */
    public void handleConnectionStatus(Status status);
    
}
