/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.entities.devices.DeviceToggleControl;
import org.pidome.client.entities.devices.DeviceToggleControl.ToggleCommand;
import org.pidome.client.scenes.dashboard.svg.devices.DeviceToggleButtonBG0;
import org.pidome.client.scenes.dashboard.svg.devices.DeviceToggleButtonBG1;

/**
 *
 * @author John
 */
public class DashboardDeviceToggleControl extends DashboardDeviceControlBase {

    private final PropertyChangeListener deviceToggleListener = this::toggleListener;
    
    Label controlName;
    
    public DashboardDeviceToggleControl(VisualDashboardDeviceItem parent, DeviceControl control) {
        super(parent, control);
        this.getPane().getStyleClass().add("toggle-control");
        this.getPane().setPadding(new Insets(2));
    }
    
    @Override
    public void build() {
        DeviceToggleButtonBG0 bg = new DeviceToggleButtonBG0();
        bg.getClipSVG().getStyleClass().add("outer");
        DeviceToggleButtonBG1 bg1 = new DeviceToggleButtonBG1();
        bg1.getClipSVG().getStyleClass().add("inner");
        bg.stack(bg1);
        this.getPane().setBackGround(bg);
        
        controlName = new Label(this.getControl().getName() + "\n" + this.getControl().getControlGroup().getDevice().getDeviceName());
        //controlName = new Label(this.getControl().getControlGroup().getDevice().getDeviceName());
        controlName.setWrapText(true);
        controlName.setTextAlignment(TextAlignment.CENTER);
        controlName.getStyleClass().add("control-text");
        this.getPane().getChildren().add(controlName);
        
        this.getControl().getValueProperty().addPropertyChangeListener(deviceToggleListener);
        
        setToggleActive((boolean)this.getControl().getValueProperty().getValue());
        
        this.getPane().setOnMouseClicked((MouseEvent me) -> {
            try {
                if(this.getPane().getStyleClass().contains("active")){
                    this.getControl().getControlGroup().getDevice().sendCommand(
                        ((DeviceToggleControl)this.getControl()).createSendCommand(new ToggleCommand(false))
                    );
                } else {
                    this.getControl().getControlGroup().getDevice().sendCommand(
                        ((DeviceToggleControl)this.getControl()).createSendCommand(new ToggleCommand(true))
                    );
                }
            } catch (DeviceControlCommandException ex) {
                Logger.getLogger(DashboardDeviceToggleControl.class.getName()).log(Level.SEVERE, "Could not switch", ex);
            }
        });
        
    }

    private void toggleListener(PropertyChangeEvent evt){
        setToggleActive((boolean)evt.getNewValue());
    }
    
    private void setToggleActive(boolean active){
        if(active == true && !controlName.getStyleClass().contains("active")){
            Platform.runLater(() -> { 
                this.getPane().getStyleClass().add("active");
            });
        } else if (active == false){
            Platform.runLater(() -> { 
                this.getPane().getStyleClass().remove("active");
            });
        }
    }
    
    @Override
    public void destruct() {
        this.getControl().getValueProperty().removePropertyChangeListener(deviceToggleListener);
    }
    
}