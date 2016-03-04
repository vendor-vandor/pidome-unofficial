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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;

/**
 *
 * @author John
 */
public class CommandSlider extends DeviceCmd implements DeviceValueChangeListener {

    double width;
    double height;
    
    Device device;
    
    Slider slider;
    Label curValue;
    
    double lastKnownValue;
    
    static Logger LOG = LogManager.getLogger(CommandSlider.class);
    
    ChangeListener<Number> updateDisplayValue = this::sliderDisplayValueHelper;
    
    public CommandSlider(Device device){
        this.device = device;
    }
    
    public void setSize(double width, double height){
        this.width = width;
        this.height= height;
    }
    
    final void sliderDisplayValueHelper(ObservableValue<? extends Number> ov, Number old_val, Number new_val){
        lastKnownValue = Math.round((double)new_val);
        Platform.runLater(() -> { curValue.setText("Value: " + lastKnownValue); });
    }
    
    final void sliderSendNewValueHelper(MouseEvent event){
        device.sendCommand(groupName,
                setName,
                Math.round(slider.getValue()),"");
        event.consume();
    }
    
    @Override
    public VBox getInterface() {
        VBox sliderBox = new VBox();
        
        double minValue = Double.valueOf(device.getCommandGroups().get(groupName).getSetDetails(setName).get("min").toString());
        double maxValue = Double.valueOf(device.getCommandGroups().get(groupName).getSetDetails(setName).get("max").toString());
        
        try {
            lastKnownValue = (device.getLastCmd(groupName, setName).toString().equals(""))?0.0:Double.valueOf(device.getLastCmd(groupName, setName).toString());
        } catch (Exception ex){
            lastKnownValue = 0;
        }
        
        curValue = new Label("Value: " + lastKnownValue);
        slider = new Slider();
        slider.setMin(minValue);
        slider.setMax(maxValue);
        slider.setValue(lastKnownValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setBlockIncrement(Math.round(maxValue / minValue)/10);
        
        slider.valueProperty().addListener(updateDisplayValue);
        
        slider.addEventHandler(MouseEvent.MOUSE_RELEASED, this::sliderSendNewValueHelper);
        
        slider.setPadding(new Insets(10,0,0,0));
        curValue.setPadding(new Insets(0,0,0,5));
        
        sliderBox.getChildren().addAll(slider,curValue);
        
        device.addDeviceValueEventListener(this, groupName, setName);
        return sliderBox;
    }

    @Override
    void build() {
        /// not used;
    }

    @Override
    public void removeListener() {
        slider.valueProperty().removeListener(updateDisplayValue);
        slider.removeEventHandler(MouseEvent.MOUSE_RELEASED, this::sliderSendNewValueHelper);
        device.removeDeviceValueEventListener(this, groupName, setName);
    }

    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                final Object eventValue= event.getValue();
                LOG.debug("Received: {}, data: {}, {}", DeviceValueChangeEvent.VALUECHANGED, eventSet, eventValue);
                if(eventSet.equals(setName)){
                    Platform.runLater(() -> {
                        curValue.setText("Value: " + eventValue);
                        slider.setValue(Double.valueOf(eventValue.toString()));
                    });
                }
            break;
        }
    }
    
}
