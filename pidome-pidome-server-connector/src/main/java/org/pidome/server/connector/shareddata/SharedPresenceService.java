/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.shareddata;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author John
 */
public class SharedPresenceService {
    
    private static int currentStatusId;
    private static String currentStatusName;
    
    private static List<SharedPresenceServiceListener> _listeners = new ArrayList<>();
    
    /**
     * Returns current status id.
     * @return 
     */
    public static int getCurrentStatusId(){
        return currentStatusId;
    }
    
    /**
     * Returns current status name.
     * @return 
     */
    public static String getCurrentStatusName(){
        return currentStatusName;
    }
    
    /**
     * Adds a listener.
     * @param listener 
     */
    public static void addListener(SharedPresenceServiceListener listener){
        if(!_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener.
     * @param listener 
     */
    public static void removeListener(SharedPresenceServiceListener listener){
        if(_listeners.contains(listener)){
            _listeners.remove(listener);
        }
    }
    
    /**
     * Sets the new status.
     * @param id
     * @param name 
     */
    protected static void setNewStatus(int id, String name){
        currentStatusId = id;
        currentStatusName = name;
        for(SharedPresenceServiceListener listener:_listeners){
            listener.setNewPresence(currentStatusId,currentStatusName);
        }
    }
    
}
