/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.devices.DeviceButtonControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.scenes.dashboard.svg.devices.DevicePushButtonBG;

/**
 *
 * @author John
 */
public class DashboardDeviceButtonControl extends DashboardDeviceControlBase {

    private final PropertyChangeListener deviceButtonListener = this::buttonListener;
    
    Text controlName;
    
    public DashboardDeviceButtonControl(VisualDashboardDeviceItem parent, DeviceControl control) {
        super(parent, control);
        this.getPane().getStyleClass().add("button-control");
        this.getPane().setBackGround(new DevicePushButtonBG());
    }
    
    @Override
    public void build() {
        
        controlName = new Text(this.getControl().getControlGroup().getDevice().getDeviceName());
        controlName.setWrappingWidth(this.getPane().getPaneWidth()-10);
        controlName.setTextAlignment(TextAlignment.CENTER);
        controlName.getStyleClass().add("control-text");
        this.getPane().getChildren().add(controlName);
        
        this.getControl().getValueProperty().addPropertyChangeListener(deviceButtonListener);
        
        this.getPane().setOnMouseClicked((MouseEvent me) -> {
            try {
                this.getControl().getControlGroup().getDevice().sendCommand(
                    ((DeviceButtonControl)this.getControl()).createSendCommand(new DeviceButtonControl.ButtonCommand())
                );
                setButtonActive(true);
            } catch (DeviceControlCommandException ex) {
                Logger.getLogger(DashboardDeviceToggleControl.class.getName()).log(Level.SEVERE, "Could not switch", ex);
            }
        });
        
    }

    private void buttonListener(PropertyChangeEvent evt){
        setButtonActive((boolean)evt.getNewValue());
    }
    
    private void setButtonActive(boolean active){
        if(active==true){
            Platform.runLater(() -> {
                this.getPane().getStyleClass().add("active");
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                     Platform.runLater(() -> {
                         DashboardDeviceButtonControl.this.getPane().getStyleClass().remove("active");
                     });
                }
            }, 500);
        }
    }
    
    @Override
    public void destruct() {
        this.getControl().getValueProperty().removePropertyChangeListener(deviceButtonListener);
    }
    
}