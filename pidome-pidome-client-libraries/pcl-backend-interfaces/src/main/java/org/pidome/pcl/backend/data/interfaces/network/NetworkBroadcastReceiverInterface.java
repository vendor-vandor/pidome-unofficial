/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.network;

/**
 *
 * @author John
 */
public interface NetworkBroadcastReceiverInterface {
    
    /**
     * Event types that can be broadcast.
     */
    public enum BroadcastStatus {
        /**
         * Starts searching
         */
        START,
        /**
         * Server found.
         */
        FOUND,
        /**
         * Server not found.
         */
        NOT_FOUND
    }
    
    public void run();
    
    /**
     * The result of the runnable.
     * @return The result of the broadcast event.
     */
    public NetworkBroadcastReceiverInterface.BroadcastStatus getResult();
    
    /**
     * Returns the message received.
     * @return Returns the message received so it can be handed over to the parser. This is called when the listen() function returns true.
     */
    public String getMessage();
    
}
