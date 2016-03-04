/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.devices.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.devices.DeviceDataControl;

/**
 *
 * @author John
 */
public class VisualDeviceDataControl extends HBox implements VisualDeviceControlInterface {
    
    DeviceDataControl control;
    
    Text prefix;
    Text contentText;
    Text suffix;
    
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceDataControl(DeviceDataControl control){
        this.control = control;
        getStyleClass().add("device-control-data");
        
        prefix = new Text(this.control.getPrefix());
        prefix.getStyleClass().add("control-prefix");
        
        contentText = new Text(String.valueOf(this.control.getValueData()));
        contentText.getStyleClass().add("control-data");
        
        suffix = new Text(this.control.getSuffix());
        suffix.getStyleClass().add("control-suffix");
        
        getChildren().addAll(prefix, contentText, suffix);
        
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
        
    }
    
    private void dataChange(PropertyChangeEvent evt){
        contentText.setText(String.valueOf(evt.getNewValue()));
    }
    
    @Override
    public final void destroy(){
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
    
}
