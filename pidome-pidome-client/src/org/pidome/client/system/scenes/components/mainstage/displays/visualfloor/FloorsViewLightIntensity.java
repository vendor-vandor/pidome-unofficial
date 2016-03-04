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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import static org.pidome.client.system.scenes.components.mainstage.displays.visualfloor.Floor3DGroup.LOG;
import org.pidome.client.utils.MathImpl;

/**
 *
 * @author John
 */
public class FloorsViewLightIntensity extends ImageView {
    
    Map<Device,Double> devices = new HashMap<>();
    int width;
    int height;
    
    public FloorsViewLightIntensity(int width, int height){
        this.width = width;
        this.height = height;
        createImage();
        this.setOpacity(0.0);
    }
            
    protected final void addDevice(Device device, String group, String control){
        if(!devices.containsKey(device)){
            double value;
            if(device.getCommandGroups().get(group).getLastCmd(control) instanceof Long){
                value = ((Long)device.getCommandGroups().get(group).getLastCmd(control)).doubleValue();
            } else {
                value = (double)device.getCommandGroups().get(group).getLastCmd(control);
            }
            devices.put(device,value);
            calculateValue(device, group, control, value);
            device.addDeviceValueEventListener(this::deviceEvent, group, control);
        }
    }
    
    private void calculateValue(Device device, String group, String control, Object value){
        if(devices.containsKey(device)){
            double newValue;
            String type = (String)device.getCommandGroups().get(group).getFullSetList().get(control).get("visualtype");
            if(device.getCommandGroups().get(group).getLastCmd(control) instanceof Long){
                newValue = ((Long)value).doubleValue();
            } else {
                newValue = (double)value;
            }
            switch(type){
                case "luxlevel":
                    devices.put(device,0.80-logToPercentageForLuxOpacity(newValue, 100000));
                break;
                case "lightlevel":
                    devices.put(device,newValue/100);
                break;
            }
            LOG.debug("Calculating values for 3d map display: {} to {}", value, 1-logToPercentageForLuxOpacity(newValue, 100000));
            double total = 0;
            for(double number:devices.values()){
                total+=number;
            }
            final double set = total/devices.size();
            Platform.runLater(() -> { this.setOpacity(set); });
        }
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
            return MathImpl.map(value,0,1.1,0,0.04);
        } else if (value<10.0d){
            return (Math.log(value)/10);
        } else {
            return ((100/Math.log(logUpperBound)) * Math.log(value))/100;
        }
    }
    
    private void createImage(){
        BufferedImage luxImage =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D luxGraphic = luxImage.createGraphics();
        luxGraphic.setColor(java.awt.Color.BLACK);
        luxGraphic.fillRect(0, 0, width, height);
        luxGraphic.dispose();
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        try {
            ImageIO.write(luxImage, "png", baos);
            baos.flush();
            this.setImage(new Image(new ByteArrayInputStream(baos.toByteArray())));
        } catch (IOException ex) {
            LOG.error("Could not create LUX image: {}", ex.getMessage());
        }
    }
    
    private void deviceEvent(DeviceValueChangeEvent event){
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                calculateValue(event.getSource(), event.getGroup(), event.getSet(), event.getValue());
            break;
        }
    }
    
    protected final void destroy(){
        for(Device device:devices.keySet()){
            device.removeDeviceValueEventListener(this::deviceEvent);
        }
    }
    
}