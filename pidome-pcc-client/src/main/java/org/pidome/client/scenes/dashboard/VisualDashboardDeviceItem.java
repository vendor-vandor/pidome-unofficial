/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.dashboard.DashboardDeviceItem;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlNotFoundException;
import org.pidome.client.entities.devices.DeviceGroupNotFoundException;
import org.pidome.client.entities.devices.UnknownDeviceException;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public final class VisualDashboardDeviceItem extends VisualDashboardItem {

    DashboardDeviceControlBase controlBase;
    
    protected VisualDashboardDeviceItem(PCCSystem system, DashboardItem item) {
        super(system, item);
        this.getStyleClass().add("dashboard-device");
        DashboardDeviceItem deviceItem = (DashboardDeviceItem)this.getDashboardItem();
        try {
            DeviceControl control = getSystem()
                                    .getClient()
                                    .getEntities()
                                    .getDeviceService()
                                    .getDevice(deviceItem.getDeviceId())
                                    .getControlGroup(deviceItem.getGroupId())
                                    .getControl(deviceItem.getControlId());
            switch(control.getControlType()){
                case TOGGLE:
                    controlBase = new DashboardDeviceToggleControl(this, control);
                break;
                case DATA:
                    controlBase = new DashboardDeviceDataControl(this, control);
                break;
                case BUTTON:
                    controlBase = new DashboardDeviceButtonControl(this, control);
                break;
                case SLIDER:
                    controlBase = new DashboardDeviceSliderControl(this, control);
                break;
                case COLORPICKER:
                    controlBase = new DashboardDeviceColorPickerControl(this, control);
                break;
            }
        } catch (UnknownDeviceException | EntityNotAvailableException | DeviceGroupNotFoundException | DeviceControlNotFoundException ex) {
            Logger.getLogger(VisualDashboardDeviceItem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    @Override
    protected void build() {
        if(controlBase!=null) controlBase.build();
    }

    @Override
    protected void destruct() {
        if(controlBase!=null) controlBase.destruct();
    }
    
}
