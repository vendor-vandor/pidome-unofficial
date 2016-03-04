/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Listener interface for client events.
 * @author John
 */
public interface PCCCLientStatusListener {
    
    /**
     * Handles a client event.
     * @param event The PCCClientEvent object.
     */
    public void handlePCCClientEvent(PCCClientEvent event);
}
