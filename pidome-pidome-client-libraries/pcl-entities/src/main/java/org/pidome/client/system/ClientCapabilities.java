/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Set client capabilities and let them be send to the server.
 * @author John
 */
public final class ClientCapabilities {
    
    /**
     * The display width.
     * By default is current width.
     */
    private double displayWidth = 0;
    /**
     * Display height.
     * By default is current height.
     */
    private double displayHeight= 0;
    
    /**
     * Constructor.
     */
    public ClientCapabilities(){}
    
    /**
     * Use this to overwrite the defaults.
     * @param displayWidth the width of the client's viewport.
     * @param displayHeight The height of the client's viewport.
     */
    public final void setDisplayDimensions(double displayWidth, double displayHeight){
        this.displayWidth = displayWidth;
        this.displayHeight= displayHeight;
    }
    
    public final double getDisplayWidth(){
        return this.displayWidth;
    }
    
    public final double getDisplayHeight(){
        return this.displayHeight;
    }
    
}