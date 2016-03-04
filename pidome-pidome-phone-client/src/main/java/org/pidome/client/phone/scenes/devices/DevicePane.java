/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.devices;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlGroup;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.visuals.controls.devices.controls.VisualDeviceControlHelper;
import org.pidome.client.phone.visuals.panes.ClosableTitledPane;
import org.pidome.client.system.PCCConnection;

/**
 *
 * @author John
 */
public class DevicePane extends ClosableTitledPane {
    
    private Device device;
    private PCCConnection connection;
    private BaseScene scene;
    
    private VBox content = new VBox();
    
    public DevicePane(Device device, PCCConnection connection, BaseScene scene){
        super(device.getDeviceName());
        this.device = device;
        this.scene = scene;
        this.connection = connection;
        content.getStyleClass().add("full-device");
        createSingleControlView();
    }
    
    
    private void createSingleControlView(){
        for(DeviceControlGroup group:this.device.getControlGroups()){
            Label groupName = new Label(group.getGroupName());
            groupName.setPrefWidth(Double.MAX_VALUE);
            groupName.getStyleClass().add("controls-group-name");
            content.getChildren().add(groupName);
            for(DeviceControl control:group.getGroupControls()){
                VisualDeviceControlHelper visualControl = new VisualDeviceControlHelper(this.scene, connection, control, true);
                content.getChildren().add(visualControl);
            }
        }
        setContent(content);
    }

    @Override
    public void destroy() {
        for(Node pane:content.getChildren()){
            VisualDeviceControlHelper removePane = (VisualDeviceControlHelper)pane;
            removePane.destroy();
            System.out.println("Pane for " + device.getDeviceName() + " destroyed");
        }
        device = null;
        connection = null;
        scene = null;
    }
    
    
}