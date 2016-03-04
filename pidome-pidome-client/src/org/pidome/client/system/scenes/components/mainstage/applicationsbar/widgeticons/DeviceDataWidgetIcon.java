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

package org.pidome.client.system.scenes.components.mainstage.applicationsbar.widgeticons;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.scenes.components.mainstage.displays.DeviceGraphWindow;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class DeviceDataWidgetIcon extends DraggableApplicationbarWidgetIcon implements DeviceValueChangeListener {

    Device device;
    String group;
    String set;
    
    String prefix = "";
    String suffix = "";
    
    Label content = new Label();
    
    static Logger LOG = LogManager.getLogger(DeviceDataWidgetIcon.class);
    
    @Override
    public void setParams(ArrayList<String> params) throws Exception {
        try {
            device = (Device)Devices.getDeviceById(Integer.valueOf(params.get(0)));
            if(device==null){
                throw new Exception("Device not available");
            }
            group = params.get(1);
            set = params.get(2);
            String getPrefix = (String)device.getCommandGroups().get(group).getSetDetails(set).get("prefix");
            if(getPrefix!=null){
                prefix = getPrefix;
            }
            String getSuffix = (String)device.getCommandGroups().get(group).getSetDetails(set).get("suffix");
            if(getSuffix!=null){
                suffix = getSuffix;
            }
        } catch (DomComponentsException ex) {
            LOG.error("Could not create widget icon for device {}", params.get(0));
        }
    }

    @Override
    public void applicationBarWidgetRemoved() {
        destroy();
    }
    
    @Override
    public void build() {
        content.setPrefHeight(Double.MAX_VALUE);
        content.setPrefWidth(Double.MAX_VALUE);
        content.setAlignment(Pos.CENTER);
        content.setTextAlignment(TextAlignment.CENTER);
        content.setWrapText(true);
        content.setText(prefix + device.getLastCmd(group, set) + suffix);
        getChildren().add(content);
        device.addDeviceValueEventListener(this, group, set);
        setOnMouseClicked((MouseEvent t) -> {
                DeviceGraphWindow graphObject;
                try {
                    graphObject = new DeviceGraphWindow(String.valueOf(device.getId()), group + "_" + set);
                    WindowManager.openWindow(graphObject, t.getSceneX(), t.getSceneY());
                } catch (Exception ex) {
                    LOG.error("Could not open graph for: {} set: {}", device.getName(), set);
                }
            });
    }

    @Override
    public void destroy() {
        device.removeDeviceValueEventListener(this, group, set);
    }

    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                final Object eventValue= event.getValue();
                LOG.debug("Received: {}, data: {}, {}", DeviceValueChangeEvent.VALUECHANGED, eventSet, eventValue);
                if(eventSet.equals(set)){
                    Platform.runLater(() -> {
                        content.setText(prefix + eventValue + suffix);
                    });
                }
            break;
        }
    }
    
}
