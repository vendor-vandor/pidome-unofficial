/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.devices.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ToggleButton;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.entities.devices.DeviceToggleControl;
import org.pidome.client.system.PCCConnection;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class VisualDeviceToggleButtonControl extends ToggleButton implements VisualDeviceControlInterface {

    DeviceToggleControl control;
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceToggleButtonControl (PCCConnection connection, DeviceToggleControl control){
        this.control = control;
        if((boolean)this.control.getValueData()==true){
            this.setSelected(true);
            this.setText(this.control.getOnLabel());
        } else {
            this.setSelected(false);
            this.setText(this.control.getOffLabel());
        }
        getStyleClass().add("device-control-toggle");
        
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
        
        this.selectedProperty().addListener((ChangeListener<Boolean>)(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            try {
                DeviceControl.DeviceCommandStructure command = this.control.createSendCommand(new DeviceToggleControl.ToggleCommand(newValue));
                connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
            } catch (DeviceControlCommandException | PCCEntityDataHandlerException ex) {
                Logger.getLogger(VisualDeviceToggleButtonControl.class.getName()).log(Level.SEVERE, "Could not send command: " + ex.getMessage(), ex);
            }
        });
        
    }

    
    private void dataChange(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            if((boolean)evt.getNewValue()==true){
                this.setSelected(true);
                this.setText(this.control.getOnLabel());
            } else {
                this.setSelected(false);
                this.setText(this.control.getOffLabel());
            }
        });
    }
    
    @Override
    public void destroy() {
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
    
}