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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlNotFoundException;
import org.pidome.client.entities.devices.DeviceGroupNotFoundException;
import org.pidome.client.tools.ColorTools;

/**
 *
 * @author John
 */
public class FloorsViewTempIntensity extends Rectangle {
    
    List<DeviceControl> devices = new ArrayList<>();
    int width;
    int height;
    
    PropertyChangeListener changed = this::deviceEvent;
    
    public FloorsViewTempIntensity(int width, int height){
        super(width, height);
        this.width = width;
        this.height = height;
        this.setOpacity(0.5);
    }
            
    protected final void addDevice(Device device, String group, String control){
        try {
            DeviceControl deviceControl = device.getControlGroup(group).getControl(control);
            if(!devices.contains(deviceControl)){
                devices.add(deviceControl);
                calculateValues();
                deviceControl.getValueProperty().addPropertyChangeListener(changed);
            }
        } catch (DeviceGroupNotFoundException | DeviceControlNotFoundException ex) {
            Logger.getLogger(FloorsViewTempIntensity.class.getName()).log(Level.SEVERE, "Could not add control", ex);
        }
    }
    
    private double getDoubleValue(DeviceControl deviceControl){
        if(deviceControl.getDataType() == DeviceControl.DeviceControlDataType.INTEGER){
            return ((Integer)deviceControl.getValue()).doubleValue();
        } else {
            return ((Number)deviceControl.getValue()).doubleValue();
        }
    }
    
    private void calculateValues(){
        for(DeviceControl device:devices){
            double total = 0;
            switch(device.getVisualType()){
                case TEMPERATURE_C:
                    total += getDoubleValue(device);
                break;
                case TEMPERATURE_F:
                    total += ((getDoubleValue(device)-32)*5)/9;
                break;
            }
            final double[] set = ColorTools.tempToHsbVisualized(total/devices.size());
            Platform.runLater(() -> { createImage(set[0],set[1],set[2]); });
        }
    }
    
    private void createImage(double h, double s, double b){
        this.setFill(Color.hsb((float)h, (float)s, (float)b));
    }
    
    private void deviceEvent(PropertyChangeEvent event){
        calculateValues();
    }
    
    protected final void destroy(){
        for(DeviceControl device:devices){
            device.getValueProperty().removePropertyChangeListener(changed);
        }
    }
    
}