/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.broadcasts;

/**
 * Listener interface for broadcast events.
 * @author John
 */
public interface BroadcastReceiverListener {
    
    public void handleBroadcastReceiverEvent(BroadcastReceiverEvent event);
    
}
