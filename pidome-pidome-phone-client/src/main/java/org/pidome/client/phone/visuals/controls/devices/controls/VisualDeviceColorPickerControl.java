/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.devices.controls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.pidome.client.entities.devices.DeviceColorPickerControl;
import org.pidome.client.entities.devices.DeviceColorPickerControl.ColorCommand;
import org.pidome.client.entities.devices.DeviceColorPickerControl.ColorPickerCommand;
import org.pidome.client.entities.devices.DeviceColorPickerControl.CommandButton;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.scenes.visuals.DialogBox;
import org.pidome.client.phone.visuals.controls.devices.controls.colorpicker.VisualDeviceColorPickerHueControl;
import org.pidome.client.system.PCCConnection;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class VisualDeviceColorPickerControl extends StackPane implements VisualDeviceControlInterface {
    
    private final BaseScene scene;
    private final PCCConnection connection;
    
    DeviceColorPickerControl control;
    PropertyChangeListener dataChangeEvent = this::dataChange;
    
    public VisualDeviceColorPickerControl (BaseScene scene, PCCConnection connection, DeviceColorPickerControl control){
        this.control = control;
        this.scene = scene;
        this.connection = connection;
        
        getStyleClass().add("device-control-colorpicker");
        this.setStyle("-fx-background-color: " + this.control.getHex() + ";");
        
        this.control.getValueProperty().addPropertyChangeListener(dataChangeEvent);
        
        VisualDeviceColorPickerHueControl pickerControl = new VisualDeviceColorPickerHueControl(this.control.getFullButtonsList());
        
        DialogBox colorPickerPopup = new DialogBox(this.control.getName());
        colorPickerPopup.setContent(pickerControl);
        colorPickerPopup.setButtons(new DialogBox.PopUpButton[]{new DialogBox.PopUpButton("CANCEL", "Cancel"), new DialogBox.PopUpButton("OK", "Set")});
        colorPickerPopup.addListener((String buttonId) -> {
            scene.closePopup(colorPickerPopup);
            switch(buttonId){
                case "CANCEL":
                    /// Well, do nothing i guess :)
                break;
                case "OK":
                    ColorCommand colorCommand = new ColorCommand(pickerControl.getCustomColor().getHue()/360, 
                                                                 pickerControl.getCustomColor().getSaturation(), 
                                                                 pickerControl.getCustomColor().getBrightness());
                    CommandButton button = null;
                    for(CommandButton but:this.control.getFullButtonsList()){
                        if(but.getLabel().equals(pickerControl.getSelectedCommand())){
                            button = but;
                        }
                    }
                    if(button!=null){
                        ColorPickerCommand toSend = new ColorPickerCommand(button, colorCommand);
                        try {
                            DeviceControl.DeviceCommandStructure command = this.control.createSendCommand(toSend);
                            this.connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
                        } catch (DeviceControlCommandException | PCCEntityDataHandlerException ex) {
                            Logger.getLogger(VisualDeviceColorPickerControl.class.getName()).log(Level.SEVERE, "Could not send command", ex);
                        }
                    }
                break;
            }
        });
        colorPickerPopup.build();
        
        addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(!scene.hasPopup(colorPickerPopup)){
                Map<String,Double> colors = this.control.getHSB();
                pickerControl.setCurrentColor(Color.hsb(colors.get("h")*360, colors.get("s"), colors.get("b")));
                scene.showPopup(colorPickerPopup);
            }
        });
        
    }
    
    private void dataChange(PropertyChangeEvent evt){
        System.out.println("Having new color: " + evt.getNewValue());
        Platform.runLater(() -> { 
            this.setStyle("-fx-background-color: " + this.control.getHex() + ";");
        });
    }
    
    @Override
    public void destroy() {
        this.control.getValueProperty().removePropertyChangeListener(dataChangeEvent);
    }
    
}