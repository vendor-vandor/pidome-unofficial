/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import javafx.scene.layout.Pane;

/**
 *
 * @author John
 */
public abstract class DashboardDeviceDataBaseControl {
    
    private final DashboardDeviceDataControl control;
    
    protected DashboardDeviceDataBaseControl(DashboardDeviceDataControl control){
        this.control = control;
    }
    
    protected final DashboardDeviceDataControl getDashboardDeviceDataControl(){
        return this.control;
    }
    
    protected abstract void build();
    
    protected abstract void destruct();
    
    protected abstract Pane getContainer();
    
}