/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.events;

/**
 * Listener for events.
 * Currently only custom events are supported.
 * @author John
 */
public interface EventServiceListener {

    /**
     * Handle a custom event.
     * @param event
     */
    public void handleCustomEvent(CustomEvent event);
    
}