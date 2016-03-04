/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.util.List;
import javafx.scene.layout.StackPane;

/**
 *
 * @author John
 */
public abstract class PhysicalDisplayInterface {
    
    public enum Support {
        NONE,BRIGHTNESS,ON_OFF,EMULATE_ON_OFF;
    }
    
    private ServiceConnector connector;
    
    public PhysicalDisplayInterface(ServiceConnector connector){
        this.connector = connector;
    }
    
    public ServiceConnector getServiceConnector(){
        return this.connector;
    }
    
    public abstract void init();
    
    public abstract List<Support> getAvailableSupportTypes();
    
    public abstract boolean brightnessInitialized();

    public abstract void updateBlankTimer();
    public abstract void setBrightness(int value);
    public abstract int getBrightness();
    public abstract StackPane getTouchOverlay();
    
    public abstract void setDisplayOn(boolean value);
    
    public abstract boolean getDisplayOn();
    
}