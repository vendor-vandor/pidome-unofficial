/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.scenes.ComponentDimensions;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.displays.DevicesDisplay;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowManager;
import org.pidome.client.utils.ColorImpl;

/**
 *
 * @author John Sirach
 */
public class CommandColorPicker extends DeviceCmd implements DeviceValueChangeListener {

    static Logger LOG = LogManager.getLogger(CommandColorPicker.class);
    
    ComponentDimensions dimensions = new ComponentDimensions();
    double width;
    double height;
    
    TitledWindow parent;
    
    Device device;
    
    boolean serverData;
    
    String colorType  = "";
    String colorValue = "";
    String lastCmd    = "";
    
    String newColorValue = "";
    
    double[] hsb;
    double[] hsbNew;
    
    Circle curColorRectangle = new Circle();
    Double curColorRectangleSizeRatio = 0.8;
    Double buttonPartWidth;
    
    HBox interfaceButton;
    
    VBox pickerContainer;
    
    public CommandColorPicker(DevicesDisplay parent, Device device){
        this.parent = parent;
        setConfig(device);
    }

    public CommandColorPicker(TitledWindow parent, Device device){
        this.parent = parent;
        setConfig(device);
    }
    
    final void setConfig(Device device){
        this.device = device;
    }
    
    public void setSize(double width, double height){
        this.width = width;
        this.height= height;
        buttonPartWidth = this.width/2;
        
        curColorRectangle.setCenterX(12*DisplayConfig.getWidthRatio());
        curColorRectangle.setCenterY(12*DisplayConfig.getHeightRatio());
        curColorRectangle.setRadius(6*DisplayConfig.getHeightRatio());
        
    }
    
    @Override
    final public Pane getInterface(){
        return pickerContainer;
    }
    
    TitledWindow createPopup(){
        return new CommandColorPickerPopup(parent,device,groupName,setName, cmdSet);
    }
    
    void hexToHsb(){
        hsb = new double[3];
        Color c = Color.web(colorValue);
        hsb[0] = c.getHue();
        hsb[1] = c.getSaturation();
        hsb[2] = c.getBrightness();
    }
    
    void hexToHsbNew(){
        hsbNew = new double[3];
        Color c = Color.web(newColorValue);
        hsbNew[0] = c.getHue();
        hsbNew[1] = c.getSaturation();
        hsbNew[2] = c.getBrightness();
    }
    
    void hsbToHex(){
        colorValue = ColorImpl.hsbToHex((float)hsb[0], (float)hsb[1], (float)hsb[2]);
    }
    
    void hsbToHexNew(){
        newColorValue = ColorImpl.hsbToHex((float)hsbNew[0], (float)hsbNew[1], (float)hsbNew[2]);
    }

    @Override
    void build() {
        buildButtonSet();
    }
    
    void buildButtonSet(){
        interfaceButton = new HBox();
        StackPane interfacebuttonPane = new StackPane();
        interfaceButton.getStyleClass().add("ColorPicker");
        interfaceButton.setPrefSize(width, height);
        interfaceButton.setMaxHeight(Region.USE_PREF_SIZE);
        interfaceButton.setMinHeight(Region.USE_PREF_SIZE);
        interfaceButton.setAlignment(Pos.CENTER);
        parseCmdSet((String)device.getLastCmd(groupName, setName));
        try {
            curColorRectangle.setFill(Color.web(colorValue));
        } catch (IllegalArgumentException ex){
            colorValue = "#000000";
            curColorRectangle.setFill(Color.web(colorValue));
        }
        newColorValue = colorValue;
        hexToHsb();
        hexToHsbNew();
        interfaceButton.addEventHandler(MouseEvent.MOUSE_CLICKED, this::openPopupHelper);
        ImageView ColorPickerIcon = new ImageView(new ImageLoader("colorwheel.png", 32,32).getImage());
        ColorPickerIcon.setPreserveRatio(true);
        ColorPickerIcon.setSmooth(true);
        ColorPickerIcon.setFitWidth(buttonPartWidth * curColorRectangleSizeRatio);
        ColorPickerIcon.setFitHeight(height * curColorRectangleSizeRatio);
        interfacebuttonPane.getChildren().addAll(curColorRectangle,ColorPickerIcon);
        interfaceButton.getChildren().add(interfacebuttonPane);
        device.addDeviceValueEventListener(this, groupName, setName);
    }
    
    final void openPopupHelper(MouseEvent mouseEvent){
        WindowManager.openWindow(createPopup());
    }
    
    void parseCmdSet(String cmd){
        colorValue = cmd;
    }
    
    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                if(eventSet.equals(setName)){
                    Platform.runLater(() -> {
                        parseCmdSet((String)device.getLastCmd(groupName, setName));
                        curColorRectangle.setFill(Color.web(colorValue));
                        hexToHsb();
                    });
                }
                serverData = false;
            break;
        }
    }

    final public HBox getButton(){
        return interfaceButton;
    }
    
    @Override
    public final void removeListener(){
        device.removeDeviceValueEventListener(this, groupName, setName);
        interfaceButton.removeEventHandler(MouseEvent.MOUSE_CLICKED, this::openPopupHelper);
        this.parent = null;
    }
    
}