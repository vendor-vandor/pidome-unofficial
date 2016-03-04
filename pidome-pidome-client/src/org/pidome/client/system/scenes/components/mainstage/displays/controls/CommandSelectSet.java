/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.scenes.ComponentDimensions;
import org.pidome.client.system.scenes.components.mainstage.displays.DevicesDisplay;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DeviceContentPane;

/**
 *
 * @author John Sirach
 */
public class CommandSelectSet extends DeviceCmd implements DeviceValueChangeListener {
    
    static Logger LOG = LogManager.getLogger(CommandSelectSet.class);
    
    String type;
    GridPane gp = new GridPane();
    Map<String, ToggleButton> buttonSet = new HashMap<>();
    
    ToggleGroup group = new ToggleGroup();
    
    Device device;
    
    Boolean serverData = false;
    
    int maxButtonsonRow = 3;
    
    ComponentDimensions dimensions = new ComponentDimensions();
    
    double width = 100*DisplayConfig.getHeightRatio();
    double height = 40*DisplayConfig.getHeightRatio();
    
    Pane parent;
    
    double popupWidth;
    VBox popupSkel;
    
    public CommandSelectSet(DevicesDisplay parent, Device device){
        this.parent = parent;
        setConfig(device);
    }
    
    public CommandSelectSet(DeviceContentPane parent, Device device){
        this.parent = parent;
        setConfig(device);
    }
    
    final void setConfig(Device device){
        this.device = device;
        popupWidth = 500;
    }
    
    @Override
    final void build(){
        String lastKnownCmd = (String)device.getLastCmd(groupName, setName);
        for(String id:this.cmdSet.keySet()){
            ToggleButton button = new ToggleButton(id);
            button.getStyleClass().add("devicebutton");
            button.setUserData(this.cmdSet.get(id));
            buttonSet.put(this.cmdSet.get(id).get("value").toString(), button);
            button.setUserData(this.cmdSet.get(id).get("value"));
            if(lastKnownCmd.equals(this.cmdSet.get(id).get("value"))){
                button.setSelected(true);
            }
            button.setToggleGroup(group);
            GridPane.setMargin(button, new Insets(0,10,20,10));
            int itemCount = gp.getChildren().size();
            int rowCount = (int)Math.floor(itemCount/maxButtonsonRow);
            int itemPos = itemCount - (rowCount * maxButtonsonRow);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMinWidth(75);
            gp.add(button, itemPos ,rowCount);
        }
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) -> {
            if (new_toggle != null && serverData == false){
                ToggleButton pressed = (ToggleButton)group.getSelectedToggle();
                device.sendCommand(groupName,
                        setName,
                        (String)pressed.getUserData(),"");
            }
            serverData = false;
        });
        device.addDeviceValueEventListener(this, groupName, setName);
    }
    
    @Override
    final public Pane getInterface(){
        return gp;
    }
    
    VBox createPopup(){
        popupSkel = new VBox();
        popupSkel.setId("BaseDialog");
        popupSkel.setPrefWidth(popupWidth);
        popupSkel.setMaxWidth(Region.USE_PREF_SIZE);
        popupSkel.getChildren().add(createTitle());
        popupSkel.getChildren().add(createBody(this.setLabel,gp));
        popupSkel.getChildren().add(closeButton());
        popupSkel.setPrefHeight(100);
        popupSkel.setMaxHeight(100);
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        popupSkel.setEffect(ds);
        VBox.setVgrow(popupSkel, Priority.ALWAYS);
        return popupSkel;
    }
    
    final public Button getButton(){
        Button interfaceButton = new Button(this.setLabel);
        interfaceButton.setPrefSize(width, height);
        interfaceButton.setOnAction((ActionEvent t) -> {
            parent.getChildren().add(createPopup());
        });
        return interfaceButton;
    }
    
    final HBox closeButton(){
        HBox hbox = new HBox();
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        hbox.setPrefSize(popupWidth, 40);
        hbox.setAlignment(Pos.CENTER);
        Button okButton = new Button("Close");
        okButton.setEffect(ds);
        HBox.setMargin(okButton, new Insets(20, 40, 20, 40));
        okButton.setOnAction((ActionEvent t) -> {
            parent.getChildren().remove(popupSkel);
        });
        hbox.getChildren().add(okButton);
        return hbox;
    }
    
    final HBox createTitle(){
        HBox hbox = new HBox();
        hbox.setPrefSize(popupWidth, 30);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getStyleClass().add("titlebar");
        Label titleLabel = new Label(device.getName());
        HBox.setMargin(titleLabel, new Insets(5, 0, 5, 10));
        hbox.getChildren().add(titleLabel);
        return hbox;
    }

    final HBox createBody(String label,GridPane gp){
        HBox hbox = new HBox();
        hbox.setPrefWidth(popupWidth);
        Label bodySetLabel = new Label(label);
        hbox.getChildren().add(bodySetLabel);
        hbox.getChildren().add(gp);
        VBox.setMargin(hbox, new Insets(10,10,10,10));
        return hbox;
    }
    
    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                Object eventValue= event.getValue();
                LOG.debug("Received: {}, data: {}, {}", DeviceValueChangeEvent.VALUECHANGED, eventSet, eventValue);
                if(eventSet.equals(setName) && buttonSet.containsKey(eventValue)){
                    for(String key: buttonSet.keySet()){
                        serverData = true;
                        if(key.equals(eventValue)){
                            buttonSet.get(key).setSelected(true);
                        } else {
                            buttonSet.get(key).setSelected(false);
                        }
                    }
                }
                serverData = false;
            break;
        }
    }
    
    @Override
    public final void removeListener(){
        device.removeDeviceValueEventListener(this, groupName, setName);
    }
    
}