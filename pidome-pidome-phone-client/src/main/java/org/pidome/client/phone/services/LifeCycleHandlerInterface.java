/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

/**
 *
 * @author John
 */
public interface LifeCycleHandlerInterface {
    
    /**
     * Check if the app is in foreground or not.
     * When the app does not have focus do not switch scenes.
     * @return 
     */
    public boolean inForeground();
    
    /**
     * Listener for life cycle changes.
     * @param listener 
     */
    public void addLifecycleListener(LifeCycleHandlerStatusListener listener);
 
    /**
     * Listener for life cycle changes.
     * @param listener 
     */
    public void removeLifecycleListener(LifeCycleHandlerStatusListener listener);
    
}