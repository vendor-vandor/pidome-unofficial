/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.visuals.controls.devices;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.pidome.client.entities.devices.DeviceButtonControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class VisualDeviceButtonControl extends Button implements VisualDeviceControlInterface {

    public VisualDeviceButtonControl (PCCConnectionInterface connection, final DeviceButtonControl control){
        this.focusTraversableProperty().setValue(Boolean.FALSE);
        this.setText(control.getLabel());
        this.setOnAction((ActionEvent e) -> {
            try {
                DeviceControl.DeviceCommandStructure command = control.createSendCommand(new DeviceButtonControl.ButtonCommand());
                connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
            } catch (DeviceControlCommandException | PCCEntityDataHandlerException ex) {
                Logger.getLogger(VisualDeviceButtonControl.class.getName()).log(Level.SEVERE, "Could not send command: " + ex.getMessage(), ex);
            }
        });
        
    }
    
    
    @Override
    public void destroy() {
        /// Not used
    }
    
}
