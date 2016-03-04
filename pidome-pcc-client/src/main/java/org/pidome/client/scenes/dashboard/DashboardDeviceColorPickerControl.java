/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import  javafx.scene.paint.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.dashboard.DashboardDeviceItem;
import org.pidome.client.entities.devices.DeviceColorPickerControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlCommandException;
import org.pidome.client.visuals.controls.devices.VisualDeviceColorPickerHueControl;
import org.pidome.client.scenes.dashboard.svg.SVGBase;
import org.pidome.client.scenes.dashboard.svg.devices.DeviceColorPickerBG;
import org.pidome.client.scenes.panes.popups.ColorPickerPopUp;
import org.pidome.client.scenes.panes.popups.PopUp;

/**
 *
 * @author John
 */
public class DashboardDeviceColorPickerControl extends DashboardDeviceControlBase {

    SVGBase bg = new DeviceColorPickerBG();
    
    private final PropertyChangeListener deviceDataListener = this::dataListener;
    
    private final VisualDeviceColorPickerHueControl pickerControl;
    
    private void dataListener(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            bg.updateFill(Paint.valueOf(this.getColorControl().getHex()));
            pickerControl.setCurrentColor(Color.web(this.getColorControl().getHex()));
        });
    }
    
    public DashboardDeviceColorPickerControl(VisualDashboardDeviceItem parent, DeviceControl control) {
        super(parent, control);
        pickerControl = new VisualDeviceColorPickerHueControl(getColorControl().getFullButtonsList());
        pickerControl.setCurrentColor(Color.web(this.getColorControl().getHex()));
        this.getPane().getStyleClass().add("colorpicker-control");
        this.getPane().setBackGround(bg);
    }

    private DeviceColorPickerControl getColorControl(){
        return (DeviceColorPickerControl)this.getControl();
    }
    
    private DashboardDeviceItem getDashboardDeviceItem(){
        return (DashboardDeviceItem)this.getPane().getDashboardItem();
    }
    
    @Override
    public void build() {
        Platform.runLater(() -> {
            bg.updateOpacity(0.4);
            bg.updateFill(Paint.valueOf(this.getColorControl().getHex()));
        });
        Text displayName;
        if(getDashboardDeviceItem().getShowDeviceName()){
            displayName = new Text(this.getControl().getControlGroup().getDevice().getDeviceName());
        } else {
            displayName = new Text(this.getControl().getName());
        }
        displayName.getStyleClass().add("control-text");
        displayName.setStyle("-fx-font-size: " + this.getPane().calcFontSize(16, false));
        displayName.setWrappingWidth(this.getPane().getPaneWidth()-10);
        displayName.setTextAlignment(TextAlignment.CENTER);
        this.getPane().getChildren().add(displayName);
        
        ColorPickerPopUp colorPickerPopup = new ColorPickerPopUp(this.getControl().getName());
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
                            this.getControl().getControlGroup().getDevice().sendCommand(command);
                        } catch (DeviceControlCommandException ex) {
                            Logger.getLogger(DashboardDeviceColorPickerControl.class.getName()).log(Level.SEVERE, "Could not send command", ex);
                        }
                    }
                break;
            }
        });
        colorPickerPopup.build();
        this.getPane().setOnMouseClicked((MouseEvent me) -> {
            colorPickerPopup.show();
        });
        getColorControl().getValueProperty().addPropertyChangeListener(deviceDataListener);
    }

    @Override
    public void destruct() {
        getColorControl().getValueProperty().removePropertyChangeListener(deviceDataListener);
    }
    
}