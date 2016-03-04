/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import javafx.application.Platform;
import org.pidome.client.entities.dashboard.DashboardDeviceItem;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceDataControl;

/**
 *
 * @author John
 */
public class DashboardDeviceDataControl extends DashboardDeviceControlBase {

    private DashboardDeviceDataBaseControl dataControl;
    
    public DashboardDeviceDataControl(VisualDashboardDeviceItem parent, DeviceControl control) {
        super(parent, control);
        this.getPane().getStyleClass().add("data-control");
    }
    
    protected DeviceDataControl getDataControl(){
        return (DeviceDataControl)this.getControl();
    }
    
    protected DashboardDeviceItem getDeviceItem(){
        return (DashboardDeviceItem)this.getPane().getDashboardItem();
    }
    
    @Override
    public void build() {
        switch(getDeviceItem().getVisualType()){
            case GRAPH:
                dataControl = new DashboardDeviceDataGraphControl(this);
            break;
            case GAUGE:
                dataControl = new DashboardDeviceDataGaugeControl(this);
            break;
            default:
                dataControl = new DashboardDeviceDataTextControl(this);
            break;
        }
        if(dataControl!=null){
            dataControl.build();
            this.getPane().setContent(dataControl.getContainer());
        }
        
    }
    
    @Override
    public void destruct() {
        if(dataControl!=null){
            dataControl.destruct();
            Platform.runLater(() -> {
                this.getPane().removeContent(dataControl.getContainer());
            });
        }
    }
    
}