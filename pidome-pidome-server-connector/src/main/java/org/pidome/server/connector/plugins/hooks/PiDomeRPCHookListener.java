/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.hooks;

/**
 *
 * @author John
 */
public interface PiDomeRPCHookListener {
    
    /**
     * Handles an RPC String.
     * @param RPCString 
     */
    public void handleRPCString(String RPCString);
    
    /**
     * Returns the plugin name.
     * @return 
     */
    public String getFriendlyName();
    
    /**
     * Returns the plugin id.
     */
    public int getPluginId();
}
