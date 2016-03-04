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

package org.pidome.client.system.scenes.components.mainstage.displays.visualfloor;

import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;
import org.apache.logging.log4j.LogManager;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.scenes.components.mainstage.displays.DeviceDisplay;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class Item3DVis extends Group {
    
    Group room;
    
    MeshedItem item;
    
    boolean envActive = false;
    BooleanProperty envItemActive = new SimpleBooleanProperty();
    ObjectProperty<Color>  envColor      = new SimpleObjectProperty();
    
    Map<String,Object> hiddenCurValues = new HashMap<>();
    Map<String,Object> itemSettings = new HashMap<>();
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Item3DVis.class);
    
    protected Item3DVis(){}
    
    protected final void setDevice(MeshedDevice device){
        this.item = device;
        switch(Categories.getCategoryConstant(device.getDevice().getCategory())){
            case "LIGHT":
                
                hiddenCurValues = new HashMap<>();
                
                PointLight light = new PointLight();
                light.lightOnProperty().bind(envItemActive);
                light.colorProperty().bind(envColor);
                
                /// A light can be toggled on and off.
                String toggleGroupName = null;
                String toggleSetName = null;
                boolean lastKnownCmdDeviceActive = false;
                Map<String,Map<String,Object>> cmdToggleSet = new HashMap<>();
                
                /// A light can have a color.
                String colorGroupName = null;
                String colorSetName = null;
                
                Device lightDevice = device.getDevice();
                
                Map<String,Device.CommandGroup> cmdGroup = lightDevice.getCommandGroups();
                
                for(String id: cmdGroup.keySet()){
                    Map<String,Map<String,Object>> setDetails = cmdGroup.get(id).getFullSetList();
                    for(String setId:setDetails.keySet()){
                        if(setDetails.get(setId).get("type").equals("toggle")){
                            toggleGroupName = id;
                            toggleSetName   = setId;
                            cmdToggleSet = cmdGroup.get(id).getCommandSet(setId);
                        } else if(setDetails.get(setId).get("type").equals("colorpicker")){
                            colorGroupName = id;
                            colorSetName   = setId;
                        }
                    }
                }
                if((toggleGroupName!= null && toggleSetName != null) || (colorGroupName != null && colorSetName != null)){
                    //// When a device is switched by a toggle use the toggle to turn the light on or off.
                    if(toggleGroupName!= null && toggleSetName != null) { 
                        itemSettings.put("toggleGroupName", toggleGroupName);
                        itemSettings.put("toggleSetName"  , toggleSetName);
                        lastKnownCmdDeviceActive = (boolean)lightDevice.getLastCmd(toggleGroupName, toggleSetName);
                        hiddenCurValues.put("active", lastKnownCmdDeviceActive);
                        lightDevice.addDeviceValueEventListener(this::deviceEvent, toggleGroupName, toggleSetName);
                    }
                    if (colorGroupName != null && colorSetName != null){
                         /// When a device does not use a toggle but uses a color picker and set the light to #000000 to turn it of, keep the light on.
                        if(toggleGroupName == null && toggleSetName == null) { 
                            hiddenCurValues.put("active", true);
                        }
                        hiddenCurValues.put("color", parseCmdColorSet((String)lightDevice.getLastCmd(colorGroupName, colorSetName)));
                        itemSettings.put("colorGroupName", colorGroupName);
                        itemSettings.put("colorSetName"  , colorSetName);
                        if(envActive) 
                            envColor.setValue(Color.web((String)hiddenCurValues.get("color")));
                        lightDevice.addDeviceValueEventListener(this::deviceEvent, colorGroupName, colorSetName);
                    } else {
                        hiddenCurValues.put("color", "#ffffff");
                        if(envActive) 
                            envColor.setValue(Color.web("#ffffff"));
                    }
                    if(room!=null){
                        light.getScope().add(room);
                    }
                    if(hiddenCurValues.containsKey("active")){
                        if(envActive) 
                            envItemActive.setValue((boolean)hiddenCurValues.get("active"));
                    }
                    getChildren().add(light);
                }
            break;
        }
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event t) -> {
            try {
                WindowManager.openWindow(new DeviceDisplay(device.getDevice()));
            } catch (Exception ex) {
                LOG.error("Could not open device: {}", device.getDevice().getName());
            }
        });
        getChildren().add(device);
    }
    
    public final MeshView getItem(){
        return this.item;
    }
    
    protected final void showInteractions(boolean show){
        item.setAnimate(show);
    }
    
    protected final void interactEnvironment(boolean show){
        envActive = show;
        if(!show){
            Platform.runLater(() -> { 
                envItemActive.setValue(false); 
            });
        } else {
            if(hiddenCurValues.containsKey("active")){
                Platform.runLater(() -> { 
                    try {
                        envColor.setValue(Color.web((String)hiddenCurValues.get("color")));
                    } catch (Exception ex){
                        //// color could not be set.
                    }
                    envItemActive.setValue((boolean)hiddenCurValues.get("active"));
                });
            }
        }
    }
    
    protected final void setRoom(Group roomGroup){
        room = roomGroup;
    }
    
    private String parseCmdColorSet(String cmd){
        return cmd;
    }
    
    protected final void destroy(){
        ((MeshedDevice)this.item).getDevice().removeDeviceValueEventListener(this::deviceEvent);
    }
    
    final void deviceEvent(DeviceValueChangeEvent event){
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                final String eventSet = event.getSet();
                final Object eventValue = event.getValue();
                if (itemSettings.containsKey("toggleSetName") && eventSet.equals((String)itemSettings.get("toggleSetName"))) {
                    Map<String,Map<String,Object>> cmdSet = ((MeshedDevice)this.item).getDevice().getCommandGroups().get((String)itemSettings.get("toggleGroupName")).getCommandSet((String)itemSettings.get("toggleSetName"));
                    for(final String id:cmdSet.keySet()){
                        if((boolean)eventValue){
                            if(envActive) Platform.runLater(() -> { envItemActive.setValue(true); });
                            hiddenCurValues.put("active",true);
                        } else {
                            if(envActive) Platform.runLater(() -> { envItemActive.setValue(false); });
                            hiddenCurValues.put("active",false);
                        }
                    }
                } else if (itemSettings.containsKey("colorSetName") && eventSet.equals(itemSettings.get("colorSetName"))){
                    String color = parseCmdColorSet(eventValue.toString());
                    hiddenCurValues.put("color", color);
                    if(envActive) {
                        Platform.runLater(() -> { envColor.setValue(Color.web(color)); });
                    }
                }
            break;
        }
    }
    
}
