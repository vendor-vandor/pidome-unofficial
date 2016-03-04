/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.hooks;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * RPC hooks class.
 * @author John
 */
public class PiDomeRPCHook {
    
    static Logger LOG = LogManager.getLogger(PiDomeRPCHook.class);
    
    /**
     * RPC strings listener.
     */
    private final static List<PiDomeRPCHookListener> hookList = new ArrayList<>();
    private static PiDomeRPCHookInterpretor interpreter;
    
    /**
     * Adds an RPC broadcast listener
     * @param listener 
     */
    public static void setRPCInterpreter(PiDomeRPCHookInterpretor listener){
        if(interpreter==null){
            interpreter = listener;
        }
    }
    
    /**
     * Handles delivery to RPC listeners.
     * @param listener
     * @param message 
     * @return  
     */
    public static String interpretRPCMessage(PiDomeRPCHookListener listener, String message){
        LOG.info("Passing to interpreter hook connector: {}", interpreter.getClass().getName());
        return interpreter.interpretExternal(listener, message);
    }
    
    /**
     * Adds an RPC broadcast listener
     * @param listener 
     */
    public final void addRPCListener(PiDomeRPCHookListener listener){
        if(!hookList.contains(listener)){
            hookList.add(listener);
        }
    }
    
    /**
     * Removes an RPC broadcast listener.
     * @param listener 
     */
    public final void removeRPCListener(PiDomeRPCHookListener listener){
        if(hookList.contains(listener)){
            hookList.remove(listener);
        }
    }
    
    /**
     * Handles delivery to RPC listeners.
     * @param message 
     */
    public static void handleRPCMessage(String message){
        for(PiDomeRPCHookListener plugin:hookList){
            Runnable runValue = () -> {
                plugin.handleRPCString(message);
            };
            runValue.run();
        }
    }
    
    
    
}