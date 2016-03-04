/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John
 */
public final class EntityErrorBroadcaster {
    
    static {
        Logger.getLogger(EntityErrorBroadcaster.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * List of error broadcast receivers.
     */
    private static final HashSet<EntityErrorBroadcastListener> _listeners = new HashSet<>();
    
    /**
     * Adds an error listener.
     * @param listener Listener for entity errors.
     */
    public static void addBroadcastListener(EntityErrorBroadcastListener listener){
        _listeners.add(listener);
    }
    
    /**
     * Removes a broadcast listener.
     * @param listener Listener for entity errors.
     */
    public static void removeBroadcastListener(EntityErrorBroadcastListener listener){
        _listeners.remove(listener);
    }
    
    /**
     * Broadcasts an error message.
     * @param title Error title.
     * @param message Error message.
     * @param ex Original exception raised.
     */
    public static void broadcastMessage(String title, String message, Exception ex){
        Runnable run = () -> {
            Iterator<EntityErrorBroadcastListener> listeners = _listeners.iterator();
            while(listeners.hasNext()){
                listeners.next().handleEntityError(title, message, ex);
            }
        };
        run.run();
    }    
    
}
