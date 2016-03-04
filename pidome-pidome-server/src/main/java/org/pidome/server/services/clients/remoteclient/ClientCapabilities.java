/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.clients.remoteclient;

import java.util.HashMap;
import java.util.Map;

/**
 * The clients capabilities
 * @author John
 */
public final class ClientCapabilities {
    
    /**
     * The display width.
     * By default is current width.
     */
    private double displayWidth = 0.0;
    /**
     * Display height.
     * By default is current height.
     */
    private double displayHeight = 0.0;
    
    /**
     * Constructor.
     */
    public ClientCapabilities(){}
    
    /**
     * Set display width and heights.
     * @param displayWidth the remote client's width (fixed/mobile)
     * @param displayHeight  the remote client's height (fixed/mobile)
     */
    public final void setDisplayDimensions(double displayWidth, double displayHeight){
        this.displayWidth = displayWidth;
        this.displayHeight= displayHeight;
    }
    
    /**
     * Returns the display width.
     * @return double width.
     */
    protected final double getDisplayWidth(){
        return this.displayWidth;
    }
    
    /**
     * Returns the display height.
     * @return souble height.
     */
    protected final double getDisplayHeight(){
        return this.displayHeight;
    }
    
    public final Map<String,Object> asMap(){
        Map<String,Object> caps = new HashMap<>();
        caps.put("displaywidth", displayWidth);
        caps.put("displayheight", displayHeight);
        return caps;
    }
    
}