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
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.pidome.client.entities.devices.DeviceColorPickerControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.scenes.dashboard.DashboardDeviceColorPickerControl;
import org.pidome.client.scenes.panes.popups.ColorPickerPopUp;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.system.PCCConnectionInterface;

/**
 *
 * @author John
 */
public class VisualDeviceColorPickerControl extends StackPane implements VisualDeviceControlInterface {
    
    private final PCCConnectionInterface connection;
    
    DeviceColorPickerControl control;
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceColorPickerControl (PCCConnectionInterface connection, DeviceColorPickerControl control){
        this.control = control;
        this.connection = connection;
        
        getStyleClass().add("device-control-colorpicker");
        this.setStyle("-fx-background-color: " + this.control.getHex() + ";");
        
        addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);

            VisualDeviceColorPickerHueControl pickerControl = new VisualDeviceColorPickerHueControl(getColorControl().getFullButtonsList());
            pickerControl.setCurrentColor(Color.web(this.getColorControl().getHex()));

            ColorPickerPopUp colorPickerPopup = new ColorPickerPopUp(this.control.getName());
            colorPickerPopup.setContent(pickerControl);

            pickerControl.setPadding(new Insets(10,10,10,10));

            ColorPickerPopUp.setMargin(pickerControl, new Insets(10,10,10,10));
            colorPickerPopup.setButtons(new PopUp.PopUpButton[]{new PopUp.PopUpButton("CANCEL", "Cancel"), new PopUp.PopUpButton("OK", "Set")});
            colorPickerPopup.addListener((String buttonId) -> {
                switch(buttonId){
                    case "CANCEL":
                        /// Well, do nothing i guess :)
                    break;
                    case "OK":
                        DeviceColorPickerControl.ColorCommand colorCommand = new DeviceColorPickerControl.ColorCommand(pickerControl.getCustomColor().getHue()/360, 
                                                                     pickerControl.getCustomColor().getSaturation(), 
                                                                     pickerControl.getCustomColor().getBrightness());
                        DeviceColorPickerControl.CommandButton button = null;
                        for(DeviceColorPickerControl.CommandButton but:getColorControl().getFullButtonsList()){
                            if(but.getLabel().equals(pickerControl.getSelectedCommand())){
                                button = but;
                            }
                        }
                        if(button!=null){
                            DeviceColorPickerControl.ColorPickerCommand toSend = new DeviceColorPickerControl.ColorPickerCommand(button, colorCommand);
                            try {
                                DeviceControl.DeviceCommandStructure command = getColorControl().createSendCommand(toSend);
                                this.control.getControlGroup().getDevice().sendCommand(command);
                            } catch (DeviceControlCommandException ex) {
                                Logger.getLogger(DashboardDeviceColorPickerControl.class.getName()).log(Level.SEVERE, "Could not send command", ex);
                            }
                        }
                    break;
                }
            });
            colorPickerPopup.build();
        
            colorPickerPopup.show();
        });
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
    }
    
    private DeviceColorPickerControl getColorControl(){
        return (DeviceColorPickerControl)this.control;
    }
    
    private void dataChange(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            this.setStyle("-fx-background-color: " + this.control.getHex() + ";");
        });
    }
    
    @Override
    public void destroy() {
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
    
}