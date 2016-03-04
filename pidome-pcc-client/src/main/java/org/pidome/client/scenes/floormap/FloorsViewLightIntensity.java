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
import org.pidome.client.entities.devices.DeviceControl.DeviceControlDataType;
import org.pidome.client.entities.devices.DeviceControlNotFoundException;
import org.pidome.client.entities.devices.DeviceGroupNotFoundException;
import org.pidome.pcl.utilities.math.MathUtilities;

/**
 *
 * @author John
 */
public class FloorsViewLightIntensity extends Rectangle {
    
    List<DeviceControl> devices = new ArrayList<>();
    int width;
    int height;
    
    PropertyChangeListener deviceEvent = this::deviceEvent;
    
    public FloorsViewLightIntensity(int width, int height){
        super(width, height);
        this.width = width;
        this.height = height;
        createImage();
        this.setOpacity(0.0);
    }
            
    protected final void addDevice(Device device, String group, String control){
        try {
            DeviceControl deviceControl = device.getControlGroup(group).getControl(control);
            if(!devices.contains(deviceControl)){
                devices.add(deviceControl);
                calculateValues();
                deviceControl.getValueProperty().addPropertyChangeListener(deviceEvent);
            }
        } catch (DeviceGroupNotFoundException | DeviceControlNotFoundException ex) {
            Logger.getLogger(FloorsViewLightIntensity.class.getName()).log(Level.SEVERE, "Control could not be determined", ex);
        }
    }
    
    private double getDoubleValue(DeviceControl deviceControl){
        if(deviceControl.getDataType() == DeviceControlDataType.INTEGER){
            return ((Integer)deviceControl.getValue()).doubleValue();
        } else {
            return ((Number)deviceControl.getValue()).doubleValue();
        }
    }
    
    private void calculateValues(){
        double total = 0;
        for(DeviceControl control:devices){
            switch(control.getVisualType()){
                case LIGHT_LUX:
                    total+=(logToPercentageForLuxOpacity(getDoubleValue(control), 100000));
                break;
                case LIGHT_PERC:
                    total+=(getDoubleValue(control)/100);
                break;
            }
        }
        final double set = total/devices.size();
        Platform.runLater(() -> { this.setOpacity(0.8 - set); });
    }
    
    /**
     * Returns a percentage value of the given log value based on a maximum log value.
     * This function is based on log10
     * @param value
     * @param logUpperBound
     * @return 
     */
    private static double logToPercentageForLuxOpacity(double value, double logUpperBound){
        if (value == 0){ // log is undefined for 0, log(1) = 0
            return 0;
        } else if (value<1.1d){
            return MathUtilities.map(value,0,1.1,0,0.04);
        } else if (value<10.0d){
            return (Math.log(value)/10);
        } else {
            return ((100/Math.log(logUpperBound)) * Math.log(value))/100;
        }
    }
    
    private void createImage(){
        this.setFill(Color.BLACK);
    }
    
    private void deviceEvent(PropertyChangeEvent event){
        calculateValues();
    }
    
    protected final void destroy(){
        for(DeviceControl device:devices){
            device.getValueProperty().removePropertyChangeListener(deviceEvent);
        }
    }
    
}