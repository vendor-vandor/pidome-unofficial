/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.scenes.components.controls.DefaultButton;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowComponent;
import org.pidome.client.utils.ColorImpl;

/**
 *
 * @author John
 */
public class CommandColorPickerPopup extends TitledWindow implements DeviceValueChangeListener {

    static Logger LOG = LogManager.getLogger(CommandColorPickerPopup.class);
    
    VBox popupSkel = new VBox();
    Rectangle curColorRectanglePicker = new Rectangle();
    Rectangle newColorRectanglePicker = new Rectangle();
    
    Device device;
    String groupName;
    String setName;
    
    String colorValue = "";
    
    String newColorValue = "";
    
    double[] hsb = new double[3];
    double[] hsbNew = new double[3];
    
    Rectangle curColorRectangle = new Rectangle();
    Double curColorRectangleSizeRatio = 0.8;
    Double buttonPartWidth = 85.0 * DisplayConfig.getWidthRatio();
    Double buttonPartHeight = 40.0 * DisplayConfig.getHeightRatio();
    
    boolean serverData;
    
    int maxItemsonRow = 3;
    int itemNumber = 0;
    
    ColorPickerHsbBar colorPicker = new ColorPickerHsbBar();
    ChangeListener<Color> updateFromPicker = this::colorPickerUpdateColorHelper;
    
    Map<String,Map<String,Object>> cmdSet;
    
    public CommandColorPickerPopup(WindowComponent parent, Device device, String groupName, String setName, Map<String,Map<String,Object>> cmdSet) {
        super(parent, groupName, "Set "+device.getName()+" color");
        this.device = device;
        this.groupName = groupName;
        this.setName = setName;
        this.cmdSet = cmdSet;
    }

    @Override
    protected void setupContent() {
        
        newColorRectanglePicker.getStyleClass().add("selectioncolors");
        newColorRectanglePicker.setWidth(buttonPartWidth * curColorRectangleSizeRatio);
        newColorRectanglePicker.setHeight(buttonPartHeight * curColorRectangleSizeRatio);
        
        curColorRectanglePicker.getStyleClass().add("selectioncolors");
        curColorRectanglePicker.setWidth(buttonPartWidth * curColorRectangleSizeRatio);
        curColorRectanglePicker.setHeight(buttonPartHeight * curColorRectangleSizeRatio);
        
        popupSkel.getStyleClass().add("colorpickerwindow");
        popupSkel.getChildren().add(createBody());
        VBox.setVgrow(popupSkel, Priority.ALWAYS);
        
        parseCmdSet((String)device.getLastCmd(groupName, setName));
        try {
            curColorRectanglePicker.setFill(Color.web(colorValue));
        } catch (IllegalArgumentException ex){
            LOG.error("Could not set current color: {}", ex.getMessage());
            colorValue = "#000000";
            curColorRectanglePicker.setFill(Color.web(colorValue));
        }
        newColorValue = colorValue;
        
        device.addDeviceValueEventListener(this, groupName, setName);
        setContent(popupSkel);
    }

    @Override
    protected void removeContent() {
        device.removeDeviceValueEventListener(this, groupName, setName);
        colorPicker.getColor().removeListener(updateFromPicker);
    }
 
    final HBox createBody(){
        HBox hbox = new HBox();
        
        HBox colorSelectionItems = new HBox();
        Label currentColor = new Label("Current");
        currentColor.getStyleClass().add("selectiontext");
        Label selectedColor = new Label("Selected");
        selectedColor.getStyleClass().add("selectiontext");
        HBox.setMargin(selectedColor, new Insets(5,10,0,5));
        HBox.setMargin(currentColor, new Insets(5,10,0,5));
        HBox.setMargin(curColorRectanglePicker, new Insets(0,0,0,10));
        
        colorSelectionItems.getChildren().addAll(curColorRectanglePicker, currentColor,newColorRectanglePicker,selectedColor);
        HBox.setMargin(colorSelectionItems, new Insets(0,0,0,10));
        
        Label selectionHeader = new Label("Color selection");
        selectionHeader.getStyleClass().add("currentcolorheader");
        VBox.setMargin(selectionHeader, new Insets(0,0,0,5));
        Label actionHeader = new Label("Color actions");
        actionHeader.getStyleClass().add("currentcolorheader");
        VBox.setMargin(actionHeader, new Insets(0,0,0,5));
        
        VBox leftSide = new VBox(10*DisplayConfig.getHeightRatio());
        leftSide.getChildren().addAll(selectionHeader,colorSelectionItems,actionHeader,buildColorPickerButtonSet());
        
        hbox.getChildren().addAll(buildColorPicker(),leftSide);
        
        VBox.setMargin(hbox, new Insets(10*DisplayConfig.getHeightRatio(),10*DisplayConfig.getWidthRatio(),10*DisplayConfig.getHeightRatio(),10*DisplayConfig.getWidthRatio()));
        return hbox;
    }

    final void colorPickerUpdateColorHelper(ObservableValue<? extends Color> ov, Color t, Color t1){
        hsbNew[0] = t1.getHue();
        hsbNew[1] = t1.getSaturation();
        hsbNew[2] = t1.getBrightness();
        hsbToHexNew();
        newColorRectanglePicker.setFill(t1);
    }
    
    final VBox buildColorPicker(){
        VBox pickerContainer = new VBox();
        pickerContainer.setAlignment(Pos.CENTER_LEFT);
        colorPicker.getColor().addListener(updateFromPicker);
        colorPicker.setPrefSize(300*DisplayConfig.getWidthRatio(), 300*DisplayConfig.getHeightRatio());
        colorPicker.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        colorPicker.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pickerContainer.getChildren().add(colorPicker);
        
        HBox predefinedColors = new HBox(6);
        predefinedColors.setPrefWidth(300*DisplayConfig.getWidthRatio());
        predefinedColors.setMinWidth(Region.USE_PREF_SIZE);
        predefinedColors.setMaxWidth(Region.USE_PREF_SIZE);
        predefinedColors.setAlignment(Pos.CENTER);
        VBox.setMargin(predefinedColors, new Insets(5*DisplayConfig.getHeightRatio(),0,0,0));
        Rectangle one = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        one.getStyleClass().add("selectioncolors");
        one.setFill(Color.web("ff0000"));
        one.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("ff0000"));
        });
        Rectangle two = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        two.getStyleClass().add("selectioncolors");
        two.setFill(Color.web("ffff00"));
        two.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("ffff00"));
        });
        Rectangle three = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        three.getStyleClass().add("selectioncolors");
        three.setFill(Color.web("00ff00"));
        three.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("00ff00"));
        });
        Rectangle four = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        four.getStyleClass().add("selectioncolors");
        four.setFill(Color.web("00ffff"));
        four.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("00ffff"));
        });
        Rectangle five = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        five.getStyleClass().add("selectioncolors");
        five.setFill(Color.web("0000ff"));
        five.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("0000ff"));
        });
        Rectangle six = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        six.getStyleClass().add("selectioncolors");
        six.setFill(Color.web("ff00ff"));
        six.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("ff00ff"));
        });
        Rectangle seven = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        seven.getStyleClass().add("selectioncolors");
        seven.setFill(Color.web("ffffff"));
        seven.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("ffffff"));
        });
        Rectangle eight = new Rectangle(30*DisplayConfig.getWidthRatio(), 30*DisplayConfig.getHeightRatio());
        eight.getStyleClass().add("selectioncolors");
        eight.setFill(Color.web("000000"));
        eight.setOnMouseClicked((MouseEvent mouseEvent) -> {
            predefinedToSelection(Color.web("000000"));
        });
        predefinedColors.getChildren().addAll(one,two,three,four,five,six,seven,eight);
        pickerContainer.getChildren().add(predefinedColors);
        return pickerContainer;
    }
    
    final void predefinedToSelection(Color color){
        hsbNew[0] = color.getHue();
        hsbNew[1] = color.getSaturation();
        hsbNew[2] = color.getBrightness();
        hsbToHexNew();
        newColorRectanglePicker.setFill(color);
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
    
    GridPane buildColorPickerButtonSet(){
        GridPane gp = new GridPane();
        gp.setHgap(10*DisplayConfig.getWidthRatio());
        gp.setVgap(10*DisplayConfig.getWidthRatio());
        this.cmdSet.keySet().stream().forEach((String id1) -> {
            final Button button = new DefaultButton((String) CommandColorPickerPopup.this.cmdSet.get(id1).get("label"));
            button.setUserData(CommandColorPickerPopup.this.cmdSet.get(id1).get("value"));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMinWidth(100*DisplayConfig.getWidthRatio());
            button.setOnMouseClicked((MouseEvent mouseEvent) -> {
                sendDeviceAction((String)button.getUserData());
            });
            int itemCount = gp.getChildren().size();
            int rowCount = (int)Math.floor(itemCount/maxItemsonRow);
            if(itemNumber==maxItemsonRow){
                itemNumber = 0;
            }
            gp.add(button, itemNumber ,rowCount);
            itemNumber++;
        });
        return gp;
    }
    
    void sendDeviceAction(String action){
        Map<String,String> colorSend = new HashMap<>();
        colorSend.put("hex", new StringBuilder("#").append(newColorValue).toString());
        device.sendCommand(groupName,
                           setName,
                           colorSend,action);
    }
    
    void parseCmdSet(String cmd){
        colorValue = cmd;
    }
    
    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                final Object eventValue= event.getValue();
                if(eventSet.equals(setName)){
                    Platform.runLater(() -> {
                        parseCmdSet((String)device.getLastCmd(groupName, setName));
                        curColorRectangle.setFill(Color.web(colorValue));
                        curColorRectanglePicker.setFill(Color.web(colorValue));
                        hexToHsb();
                    });
                }
                serverData = false;
            break;
        }
    }
    
}
