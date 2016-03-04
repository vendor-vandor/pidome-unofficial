/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Listener interface for system initialization.
 * @author John
 */
public interface SystemServiceInitDataListener {
    
    /**
     * Handles a client init paths request.
     * @param provider Holds init data content.
     */
    public void handleClientInitPaths(SystemServiceInitPathsProvider provider);
    
}
