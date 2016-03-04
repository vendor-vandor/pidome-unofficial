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

package org.pidome.client.system.scenes.components.mainstage.displays;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.domotics.components.locations.Locations;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DeviceDataGraph;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class DeviceGraphWindow extends TitledWindow implements DeviceValueChangeListener {

    DeviceDataGraph graph;
    String groupName;
    String setName;
    Device device;
    
    Text value = new Text();
    
    public DeviceGraphWindow(Object... params) throws Exception {
        this((String)params[0], (String)params[1]);
    }
    
    public DeviceGraphWindow(String windowId, String windowName) throws Exception {
        super("devicegraph"+windowId+windowName, 
                ((Device)Devices.getDeviceById(Integer.valueOf(windowId))).getName() + " " + 
                ((Device)Devices.getDeviceById(Integer.valueOf(windowId))).getCommandGroups().get(windowName.split(":")[0]).getSetDetails(windowName.split(":")[1]).get("label"));
        setId("graphwindow");
        device = (Device)Devices.getDeviceById(Integer.valueOf(windowId));
        groupName = windowName.split(":")[0];
        setName = windowName.split(":")[1];
    }

    @Override
    protected void setupContent() {
        graph = new DeviceDataGraph(device, groupName, setName);
        graph.setupContent();
        VBox content = new VBox();
        
        TilePane contentDesc = new TilePane();
        contentDesc.getStyleClass().add("graphdescription");
        contentDesc.setPrefColumns(2);
        contentDesc.setPadding(new Insets(10, 0, 10, 5));
        
        GridPane contentDescLeft = new GridPane();
        contentDescLeft.setHgap(5);
        contentDescLeft.setVgap(10);
        
        contentDescLeft.add(new Text("Device"), 1, 0); 
        contentDescLeft.add(new Text(":"), 2, 0); 
        contentDescLeft.add(new Text(device.getName()), 3, 0);
        
        contentDescLeft.add(new Text((String)device.getCommandGroups().get(groupName).getSetDetails(setName).get("label")), 1, 1); 
        contentDescLeft.add(new Text(":"), 2, 1); 
        Text prefix = new Text();
        String getPrefix = (String)device.getCommandGroups().get(groupName).getSetDetails(setName).get("prefix");
        if(getPrefix!=null){
            prefix.setText(getPrefix);
        }
        Text suffix = new Text();
        String getSuffix = (String)device.getCommandGroups().get(groupName).getSetDetails(setName).get("suffix");
        if(getSuffix!=null){
            suffix.setText(getSuffix);
        }
        value.setText(device.getLastCmd(groupName, setName).toString());
        HBox dataContent = new HBox();
        dataContent.getChildren().addAll(prefix, value, suffix);
        contentDescLeft.add(dataContent, 3, 1);

        GridPane contentDescRight = new GridPane();
        contentDescRight.setHgap(5);
        contentDescRight.setVgap(10);
        contentDescRight.add(new Text("Category"), 1, 0); 
        contentDescRight.add(new Text(":"), 2, 0);
        contentDescRight.add(new Text(Categories.getCategoryName(device.getCategory())), 3, 0);
        
        
        contentDescRight.add(new Text("Location"), 1, 1); 
        contentDescRight.add(new Text(":"), 2, 1);
        try {
            contentDescRight.add(new Text(Locations.getLocation(device.getLocation())), 3, 1);
        } catch (DomComponentsException ex) {
            contentDescRight.add(new Text("Unknown"), 3, 1);
        }
        contentDesc.getChildren().addAll(contentDescLeft, contentDescRight);
        
        content.getChildren().addAll(graph, contentDesc);
        this.setContent(content);
        device.addDeviceValueEventListener(this, groupName, setName);
    }

    @Override
    protected void removeContent() {
        getChildren().remove(graph);
        graph.removeContent();
        device.removeDeviceValueEventListener(this, groupName, setName);
    }

    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                final Object eventValue= event.getValue();
                if(eventSet.equals(setName)){
                    Platform.runLater(() -> {
                        value.setText(eventValue.toString());
                    });
                }
            break;
        }
    }
    
}
