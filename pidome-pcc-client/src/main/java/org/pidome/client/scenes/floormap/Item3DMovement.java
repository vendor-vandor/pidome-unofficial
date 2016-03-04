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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.image.Image;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlNotFoundException;
import org.pidome.client.entities.devices.DeviceGroupNotFoundException;

/**
 *
 * @author John
 */
final class Item3DMovement extends Group {
    
    Group room;
    
    MeshedMovement item;
    
    List<DeviceControl> devices = new ArrayList<>();
    
    boolean envActive = false;
    BooleanProperty envItemActive = new SimpleBooleanProperty();
    
    private ScheduledExecutorService delayedExecutor;
    
    PropertyChangeListener moved = this::deviceEvent;
    
    protected Item3DMovement(double x, double y) {
        envItemActive.addListener(this::itemActiveListener);
        try {
            item = new MeshedMovement(new Image(VisualFloorUtils.loadMovementImage()), x, y);
            item.visibleProperty().set(false);
            getChildren().add(item);
        } catch (IOException ex) {
            Logger.getLogger(Item3DMovement.class.getName()).log(Level.SEVERE, "Coulld not load movement mesh", ex);
        }
    }
    
    public final MeshedMovement getItem(){
        return this.item;
    }
    
    protected final void addMovementDevice(Device device, String group, String control){
        try {
            DeviceControl deviceControl = device.getControlGroup(group).getControl(control);
            if(!devices.contains(deviceControl)){
                devices.add(deviceControl);
                deviceControl.getValueProperty().addPropertyChangeListener(moved);
                if(envActive){
                    if((boolean)deviceControl.getValue()==true){
                        envItemActive.set(true);
                    }
                }
            }
        } catch (DeviceGroupNotFoundException | DeviceControlNotFoundException ex) {
            Logger.getLogger(Item3DMovement.class.getName()).log(Level.SEVERE, "Could not load movement controls", ex);
        }
    }
    
    protected final void setItemActive(boolean active){
        envActive = active;
        if(envActive == false){
            itemVisible(false);
        } else if(delayedExecutor!=null){
            itemVisible(true);             
        }
    }
    
    private void itemActiveListener(ObservableValue<? extends Boolean> observe,Boolean oldValue,Boolean newValue){
        itemVisible(envActive);
        pulseRunner(newValue);
    }
    
    final void itemVisible(boolean visible){
        item.visibleProperty().set(visible);
    }
    
    private void pulseRunner(boolean pulse){
        if(pulse){
            if(delayedExecutor==null){
                delayedExecutor = Executors.newSingleThreadScheduledExecutor();
                delayedExecutor.scheduleAtFixedRate(() -> { item.pulse(1000); }, 0, 2, TimeUnit.SECONDS);
            }
        } else {
            if(delayedExecutor!=null){
                delayedExecutor.shutdownNow();
                delayedExecutor = null;
            }
        }
    }
    
    final void deviceEvent(PropertyChangeEvent event){
        final boolean eventValue = (boolean)event.getNewValue();
        if(eventValue){
            Platform.runLater(() -> { envItemActive.set(true); itemVisible(envActive); });
        } else {
            Platform.runLater(() -> { envItemActive.set(false); itemVisible(false); });
        }
    }
    
    protected final void setRoom(Group roomGroup){
        room = roomGroup;
    }
    
    protected final void destroy(){
        pulseRunner(false);
        for(DeviceControl deviceControl:devices){
            deviceControl.getValueProperty().addPropertyChangeListener(moved);
        }
        room = null;
    }
    
}
