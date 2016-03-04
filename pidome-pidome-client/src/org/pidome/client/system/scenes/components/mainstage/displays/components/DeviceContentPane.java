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

package org.pidome.client.system.scenes.components.mainstage.displays.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.domotics.components.devices.DevicesEvent;
import org.pidome.client.system.domotics.components.devices.DevicesEventListener;
import org.pidome.client.system.domotics.components.locations.Locations;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.CategoryList;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.CommandButtonSet;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.CommandColorPicker;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.CommandDataFieldDetail;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.CommandSelectSet;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.CommandSlider;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.CommandToggleButton;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.DeviceCmd;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.DeviceComponents;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class DeviceContentPane extends VBox implements DevicesEventListener,TabbedContentTabChangedListener {
    
    Device device;
    
    Label deviceName = new Label();
    Label deviceHeaderName = new Label();
    Label deviceHeaderLocation = new Label();
    
    DeviceComponents devComponents;
    
    Map<String,Device.CommandGroup> cmdGroup = new HashMap<>();
    
    List<DeviceCmd> buttonList = new ArrayList();
    
    double buttonContainerWidth  = 85.0 * DisplayConfig.getWidthRatio();
    double buttonContainerHeight = 40.0 * DisplayConfig.getHeightRatio();
    
    CategoryList rasterContentNode;
    VBox descriptionNode = new VBox();
    
    String deviceLocationName;
    String deviceCategoryName;
    
    static Logger LOG = LogManager.getLogger(DeviceContentPane.class);

    TitledWindow parent;
    
    TabbedContent tabs = new TabbedContent();
    
    public DeviceContentPane(TitledWindow parent, Device device){
        this.device = device;
        this.parent = parent;
        getStyleClass().add("devicedisplay");
        cmdGroup = this.device.getCommandGroups();
        setMinWidth(500*DisplayConfig.getWidthRatio());
    }

    public final void setupContent() {
        setAlignment(Pos.TOP_LEFT);
        createDeviceCatLoc();
        CreateDeviceLeftDescription();
        createDeviceDescription();
        createDeviceControlsPane();
        try {
            setLocationName(Locations.getLocation(device.getLocation()));
        } catch (DomComponentsException ex) {
            setLocationName("Unknown device location");
        }
        setDeviceName(this.device.getName());
        Devices.addDevicesEventListener(this);
        tabs.setContentMinSize(400, 500);
        tabs.addTabChangedListener(this);
        tabs.addTab("device", "Control");
        tabs.addTab("about", "About device");
        
        StackPane deviceNameBar = new StackPane();
        deviceNameBar.setPadding(new Insets(9*DisplayConfig.getHeightRatio(),5*DisplayConfig.getWidthRatio(),9*DisplayConfig.getHeightRatio(),5*DisplayConfig.getWidthRatio()));
        deviceNameBar.getStyleClass().add("devicenamebar");
        StackPane.setAlignment(deviceHeaderName, Pos.CENTER_LEFT);
        StackPane.setAlignment(deviceHeaderLocation, Pos.CENTER_RIGHT);
        deviceNameBar.getChildren().addAll(deviceHeaderName, deviceHeaderLocation);
        getChildren().addAll(deviceNameBar,tabs);
        
    }
    
    @Override
    public void tabSwitched(String oldTab, String newTab) {
        switch(newTab){
            case "device":
                tabs.setTabContent(newTab,rasterContentNode,"Device controle");
            break;
            case "about":
                tabs.setTabContent(newTab,descriptionNode,"About the device");
            break;
        }
    }
    
    final void createDeviceCatLoc(){
        try {
            deviceLocationName = Locations.getLocation(device.getLocation());
        } catch (DomComponentsException ex) {
            deviceLocationName = "Unknown";
        }
        deviceCategoryName = (String)Categories.getCategory(device.getCategory()).get("name");
    }
    
    public final void removeContent() {
        removeDeviceCmdInterfaces();
        tabs.removeTabChangedListener(this);
        getChildren().remove(tabs);
        rasterContentNode.destroy();
        rasterContentNode = null;
        Devices.removeDevicesEventListener(this);
        buttonList.clear();
    }

    final void createDeviceDescription(){
        VBox descBox = new VBox(10* DisplayConfig.getHeightRatio());
        descBox.setMaxWidth(300*DisplayConfig.getWidthRatio());
        Label descHeader = new Label("Description");
        descHeader.getStyleClass().add("descheader");
        Text deviceTypeDesc = new Text();
        deviceTypeDesc.setWrappingWidth(300 * DisplayConfig.getWidthRatio());
        deviceTypeDesc.getStyleClass().add("description");
        deviceTypeDesc.setText("Needs to be added to JSON data");
        
        descBox.setTranslateY(10 * DisplayConfig.getHeightRatio());
        
        descBox.getChildren().addAll(descHeader,deviceTypeDesc);
        
        VBox.setVgrow(deviceTypeDesc, Priority.ALWAYS);
        descriptionNode.getChildren().add(descBox);
    }
    
    final void setDeviceCategoryName(){
        deviceCategoryName = Categories.getCategoryName(this.device.getCategory());
    }
    
    final void setDeviceName(String deviceName){
        this.deviceName.setText(deviceName);
        deviceHeaderName.setText(deviceName);
    }
    
    final void setLocationName(String locationName){
        deviceLocationName = locationName;
        deviceHeaderLocation.setText(locationName);
    }
    
    final void CreateDeviceLeftDescription(){
        HBox top = new HBox(30*DisplayConfig.getWidthRatio());
        top.setPadding(new Insets(5* DisplayConfig.getHeightRatio(),0,0,10*DisplayConfig.getWidthRatio()));
        ImageView deviceCatImage = new ImageView(new Image(AppResources.getImage("device_cat/" +Categories.getCategoryConstant(device.getCategory())+ ".png")));
        deviceCatImage.setFitWidth(76 * DisplayConfig.getWidthRatio());
        deviceCatImage.setFitHeight(76 * DisplayConfig.getHeightRatio());
        deviceCatImage.setTranslateX(10*DisplayConfig.getWidthRatio());
        deviceCatImage.setTranslateY(10*DisplayConfig.getHeightRatio());
        
        VBox desc = new VBox();
        
        Label locHeader = new Label("Location");
        VBox.setMargin(locHeader, new Insets(10,0,0,0));
        locHeader.getStyleClass().add("descheader");
        Text locTypeDesc = new Text();
        locTypeDesc.setWrappingWidth(180 * DisplayConfig.getWidthRatio());
        locTypeDesc.getStyleClass().add("description");
        locTypeDesc.setText(deviceLocationName);
        
        Label catHeader = new Label("Category");
        catHeader.getStyleClass().add("descheader");
        Text catTypeDesc = new Text();
        catTypeDesc.setWrappingWidth(180 * DisplayConfig.getWidthRatio());
        catTypeDesc.getStyleClass().add("description");
        catTypeDesc.setText(deviceCategoryName);
        
        desc.getChildren().addAll(catHeader,catTypeDesc,locHeader,locTypeDesc);
        
        top.getChildren().addAll(deviceCatImage,desc);
        
        descriptionNode.getChildren().add(top);
    }
    
    public final synchronized void updateDevice(final Device eventDevice) {
        Platform.runLater(() -> {
            if (eventDevice.getId() == device.getId()) {
                if (!deviceName.getText().equals(device.getName())) {
                    setDeviceName(device.getName());
                }
                deviceCategoryName = (String)Categories.getCategory(device.getCategory()).get("name");
                try {
                    deviceLocationName = Locations.getLocation(device.getLocation());
                } catch (DomComponentsException ex) {
                    deviceLocationName = "Unknown";
                }
            }
        });
    }
    
    final CategoryList createDeviceControlsPane(){
        rasterContentNode = new CategoryList();
        rasterContentNode.setListSize(400* DisplayConfig.getWidthRatio(), 500* DisplayConfig.getHeightRatio());
        rasterContentNode.setTranslateX(20 * DisplayConfig.getWidthRatio());
        
        for(String id: cmdGroup.keySet()){
            Map<String,Map<String,Object>> setDetails = cmdGroup.get(id).getFullSetList();
            String groupName = (String)cmdGroup.get(id).getGroupDetails().get("name");
            VBox deviceContentPane = new VBox(8 * DisplayConfig.getHeightRatio());
            VBox.setMargin(deviceContentPane, new Insets(8 * DisplayConfig.getHeightRatio(),0,5 * DisplayConfig.getHeightRatio(),9 * DisplayConfig.getWidthRatio()));
            for(String setId:setDetails.keySet()){
                switch((String)setDetails.get(setId).get("type")){
                    case "button":
                        CommandButtonSet cmdButton = new CommandButtonSet(device);
                        cmdButton.setGroupName(id);
                        cmdButton.setSetName(setId);
                        cmdButton.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        cmdButton.setSet(cmdGroup.get(id).getCommandSet(setId));
                        HBox pushMe = cmdButton.getInterface();
                        pushMe.setFocusTraversable(false);
                        deviceContentPane.getChildren().add(pushMe);
                        buttonList.add(cmdButton);
                    break;
                    case "select":
                        CommandSelectSet cmdSet = new CommandSelectSet(this, device);
                        cmdSet.setGroupName(id);
                        cmdSet.setSetName(setId);
                        cmdSet.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        cmdSet.setSet(cmdGroup.get(id).getCommandSet(setId));
                        Button selectMe = cmdSet.getButton();
                        selectMe.setFocusTraversable(false);
                        deviceContentPane.getChildren().add(selectMe);
                        buttonList.add(cmdSet);
                    break;
                    case "slider":
                        CommandSlider sliderSet = new CommandSlider(device);
                        sliderSet.setGroupName(id);
                        sliderSet.setSetName(setId);
                        sliderSet.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        sliderSet.setSet(cmdGroup.get(id).getCommandSet(setId));
                        VBox slideMe = sliderSet.getInterface();
                        slideMe.setFocusTraversable(false);
                        deviceContentPane.getChildren().add(slideMe);
                        buttonList.add(sliderSet);
                    break;
                    case "toggle":
                        CommandToggleButton cmdToggle = new CommandToggleButton(device);
                        cmdToggle.setGroupName(id);
                        cmdToggle.setSetName(setId);
                        cmdToggle.setSet(cmdGroup.get(id).getCommandSet(setId));
                        cmdToggle.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        deviceContentPane.getChildren().add(cmdToggle.getButton());
                        buttonList.add(cmdToggle);
                    break;
                    case "data":
                        CommandDataFieldDetail cmdData = new CommandDataFieldDetail(device);
                        cmdData.setSize(buttonContainerWidth,buttonContainerHeight);
                        cmdData.setGroupName(id);
                        cmdData.setSetName(setId);
                        cmdData.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        cmdData.setSet(cmdGroup.get(id).getCommandSet(setId));
                        deviceContentPane.getChildren().add(cmdData.getButton());
                        buttonList.add(cmdData);
                    break;
                    case "colorpicker":
                        CommandColorPicker cmdCPicker = new CommandColorPicker(this.parent,device);
                        cmdCPicker.setSize(buttonContainerWidth,buttonContainerHeight);
                        cmdCPicker.setGroupName(id);
                        cmdCPicker.setSetName(setId);
                        cmdCPicker.setSet(cmdGroup.get(id).getCommandSet(setId));
                        cmdCPicker.setSetLabel((String)cmdGroup.get(id).getSetDetails(setId).get("label"));
                        deviceContentPane.getChildren().add(cmdCPicker.getButton());
                        buttonList.add(cmdCPicker);
                    break;
                }
            }
            if(deviceContentPane.getChildren().size()>0){
                rasterContentNode.addItem(groupName, id, deviceContentPane);
            }
        }
        return rasterContentNode;
    }
    
    @Override
    public void handleDevicesEvent(final DevicesEvent event) {
        switch (event.getEventType()) {
            case DevicesEvent.DEVICEREMOVED:
                if (event.getSource().getId() == device.getId()) {
                    Platform.runLater(() -> {
                        removeContent();
                    });
                }
            break;
            case DevicesEvent.DEVICEUPDATED:
                if (event.getSource().getId() == device.getId()) {
                    updateDevice(event.getSource());
                }
            break;
        }
    }

    final void removeDeviceCmdInterfaces(){
        buttonList.stream().forEach((buttonItem) -> {
            buttonItem.removeListener();
        });
    }

}
