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

package org.pidome.client.scenes.floormap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceColorPickerControl;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControl.DeviceControlType;
import org.pidome.client.entities.devices.DeviceControlGroup;

/**
 *
 * @author John
 */
public class Item3DVis extends Group {
    
    Group room;
    
    MeshedItem item;
    
    boolean envActive = false;
    BooleanProperty envItemActive = new SimpleBooleanProperty(false);
    ObjectProperty<Color>  envColor      = new SimpleObjectProperty();
    
    Map<String,Object> hiddenCurValues = new HashMap<>();
    Map<String,Object> itemSettings = new HashMap<>();
    
    PropertyChangeListener deviceToggleEvent = this::deviceToggleEvent;
    private boolean hasToggle = false;
    
    PropertyChangeListener deviceColorEvent = this::deviceColorEvent;
    private boolean hasPicker = false;
    
    protected Item3DVis(){}
    
    protected final void setDevice(MeshedDevice device){
        this.item = device;
        switch(device.getDevice().getDeviceSubcategoryId()){
            case 3:
                
                hiddenCurValues = new HashMap<>();
                
                PointLight light = new PointLight();
                light.lightOnProperty().bind(envItemActive);
                light.colorProperty().bind(envColor);
                
                Device lightDevice = device.getDevice();
                
                for(DeviceControlGroup group:lightDevice.getControlGroups()){
                    for(DeviceControl control:group.getGroupControls()){
                        if(control.getControlType() == DeviceControlType.TOGGLE){
                            hiddenCurValues.put("active", (boolean)control.getValueData());
                            control.getValueProperty().addPropertyChangeListener(deviceToggleEvent);
                            hasToggle = true;
                        } else if(control.getControlType() == DeviceControlType.COLORPICKER){
                            hiddenCurValues.put("color", ((DeviceColorPickerControl)control).getHex());
                            control.getValueProperty().addPropertyChangeListener(deviceColorEvent);
                            hasPicker = true;
                        }
                    }
                }
                if(!hiddenCurValues.containsKey("color")){
                    hiddenCurValues.put("color", "#ffffff");
                }
                envColor.setValue(Color.web((String)hiddenCurValues.get("color")));
                if(envActive){
                    envItemActive.setValue((boolean)hiddenCurValues.get("active"));
                } else {
                    envItemActive.setValue(false);
                }
                light.getScope().add(room);
                getChildren().add(light);
            break;
        }
        this.addEventHandler(MouseEvent.MOUSE_CLICKED, (Event t) -> {
            try {
                
            } catch (Exception ex) {
                
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
    
    protected final void destroy(){
        for(DeviceControlGroup group:((MeshedDevice)this.item).getDevice().getControlGroups()){
            for(DeviceControl control:group.getGroupControls()){
                if(control.getControlType() == DeviceControlType.TOGGLE){
                    control.getValueProperty().removePropertyChangeListener(deviceToggleEvent);
                } else if(control.getControlType() == DeviceControlType.COLORPICKER){
                    control.getValueProperty().removePropertyChangeListener(deviceColorEvent);
                }
            }
        }
    }
    
    private void deviceToggleEvent(PropertyChangeEvent evt){
        envItemActive.setValue((boolean)evt.getNewValue());
    }
    
    private void deviceColorEvent(PropertyChangeEvent evt){
        Platform.runLater(() -> { 
            envColor.setValue(Color.web("#"+(String)evt.getNewValue()));
        });
    }
    
}