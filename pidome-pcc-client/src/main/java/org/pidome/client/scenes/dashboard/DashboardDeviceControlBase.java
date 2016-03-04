/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import org.pidome.client.entities.devices.DeviceControl;

/**
 *
 * @author John
 */
public abstract class DashboardDeviceControlBase {
    
    private final VisualDashboardDeviceItem parent;
    private final DeviceControl control;
    
    protected DashboardDeviceControlBase(VisualDashboardDeviceItem parent, DeviceControl control){
        this.parent     = parent;
        this.control    = control;
    }
    
    protected final DeviceControl getControl(){
        return control;
    }
    
    protected final VisualDashboardDeviceItem getPane(){
        return this.parent;
    }
    
    public abstract void build();

    public abstract void destruct();
    
}