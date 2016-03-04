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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;

/**
 *
 * @author John
 */
final class Item3DMovement extends Group {
    
    Group room;
    
    MeshedMovement item;
    
    List<Device> devices = new ArrayList<>();
    
    boolean envActive = false;
    BooleanProperty envItemActive = new SimpleBooleanProperty();
    
    private ScheduledExecutorService delayedExecutor;
    
    protected Item3DMovement(double x, double y) throws DomComponentsException {
        envItemActive.addListener(this::itemActiveListener);
        try {
            item = new MeshedMovement(new Image(VisualFloorUtils.loadMovementImage()), x, y);
            item.visibleProperty().set(false);
            getChildren().add(item);
        } catch (IOException ex) {
            throw new DomComponentsException("Could not load movement resource: " + ex.getMessage());
        }
    }
    
    public final MeshedMovement getItem(){
        return this.item;
    }
    
    protected final void addMovementDevice(Device device, String group, String control){
        if(!devices.contains(device)){
            devices.add(device);
            device.addDeviceValueEventListener(this::deviceEvent, group, control);
            if(envActive){
                if((boolean)device.getCommandGroups().get(group).getLastCmd(control)==true){
                    envItemActive.set(true);
                }
            }
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
    
    final void deviceEvent(DeviceValueChangeEvent event){
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                final boolean eventValue = (boolean)event.getValue();
                if(eventValue){
                    Platform.runLater(() -> { envItemActive.set(true); itemVisible(envActive); });
                } else {
                    Platform.runLater(() -> { envItemActive.set(false); itemVisible(false); });
                }
            break;
        }
    }
    
    protected final void setRoom(Group roomGroup){
        room = roomGroup;
    }
    
    protected final void destroy(){
        pulseRunner(false);
        for(Device device:devices){
            device.removeDeviceValueEventListener(this::deviceEvent);
        }
        room = null;
    }
    
}
