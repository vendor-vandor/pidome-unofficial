/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import org.pidome.client.system.PCCConnectionInterface.PCCConnectionStatus;

/**
 * Listener class for connection events.
 * @author John
 */
public interface PCCConnectionListener {
    
    /**
     * Handle the connection status including event.
     * When remote host information is available it is included in the event.
     * @param status Connection status
     * @param event When remote host information is available this is included.
     */
    public void handlePCCConnectionEvent(PCCConnectionStatus status, PCCConnectionEvent event);
    
    
}
