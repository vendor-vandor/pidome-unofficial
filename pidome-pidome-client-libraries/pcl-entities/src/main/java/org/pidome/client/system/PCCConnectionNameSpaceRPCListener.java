/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import org.pidome.pcl.data.parser.PCCEntityDataHandler;


/**
 * Listener interface for namespace registered listeners.
 * @author John
 */
public interface PCCConnectionNameSpaceRPCListener {
    
    /**
     * Call back used when there is a broadcast done.
     * This function is ONLY compatible with broadcasts.
     * @param rpcDataHandler The PCCEntityDataHandler containing broadcast data.
     */
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler);
    
    /**
     * Callback when there is an action to be taken on request.
     * This function is compatible for both function result broadcasts as data received from an json http request.
     * @param rpcDataHandler PCCEntityDataHandler containing request results.
     */
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler);
    
}
