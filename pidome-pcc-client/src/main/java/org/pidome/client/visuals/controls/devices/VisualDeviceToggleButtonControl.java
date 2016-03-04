/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.visuals.controls.devices;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.entities.devices.DeviceToggleControl;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class VisualDeviceToggleButtonControl extends Button implements VisualDeviceControlInterface {

    DeviceToggleControl control;
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceToggleButtonControl (PCCConnectionInterface connection, DeviceToggleControl control){
        this.focusTraversableProperty().setValue(Boolean.FALSE);
        this.control = control;
        if((boolean)this.control.getValueData()==true){
            if(!this.getStyleClass().contains("active")){
                this.getStyleClass().add("active");
            }
            this.setText(this.control.getOnLabel());
        } else {
            this.getStyleClass().remove("active");
            this.setText(this.control.getOffLabel());
        }
        getStyleClass().add("device-control-toggle");
        
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
        
        this.setOnAction((ActionEvent e) -> {
            try {
                DeviceControl.DeviceCommandStructure command = this.control.createSendCommand(new DeviceToggleControl.ToggleCommand(!this.getStyleClass().contains("active")));
                connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
            } catch (DeviceControlCommandException | PCCEntityDataHandlerException ex) {
                Logger.getLogger(VisualDeviceToggleButtonControl.class.getName()).log(Level.SEVERE, "Could not send command: " + ex.getMessage(), ex);
            }
        });
        
    }

    
    private void dataChange(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            if((boolean)evt.getNewValue()==true){
                if(!this.getStyleClass().contains("active")){
                    this.getStyleClass().add("active");
                }
                this.setText(this.control.getOnLabel());
            } else {
                this.getStyleClass().remove("active");
                this.setText(this.control.getOffLabel());
            }
        });
    }
    
    @Override
    public void destroy() {
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
    
}