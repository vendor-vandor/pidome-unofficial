/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.visuals.controls.devices;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlGroup;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.visuals.controls.devices.controls.VisualDeviceControlHelper;
import org.pidome.client.phone.visuals.panes.ItemPane;
import org.pidome.client.system.PCCConnection;

/**
 *
 * @author John
 */
public class DeviceSingleControlPane extends ItemPane {

    private final Device device;
    
    List<VisualDeviceControlHelper> visualControls = new ArrayList<>();
    private final PCCConnection connection;
    BaseScene scene;
    
    HBox controlsSet = new HBox();
    
    public DeviceSingleControlPane(BaseScene scene, PCCConnection connection, Device device) {
        super(device.getDeviceName());
        this.connection = connection;
        this.scene = scene;
        this.device = device;
        getStyleClass().add("device-control-field-single");
        HBox.setHgrow(controlsSet, Priority.ALWAYS);
        createSingleControlView();
    }
    
    public final Device getDevice(){
        return this.device;
    }
    
    private void createSingleControlView(){
        boolean controlFound = false;
        for(DeviceControlGroup group:this.device.getControlGroups()){
            if(controlFound==false){
                for(DeviceControl control:group.getGroupControls()){
                    if(control.hasShortCut() && control.getShortCutPosition()==0){
                        VisualDeviceControlHelper visualControl = new VisualDeviceControlHelper(this.scene, connection, control, false);
                        controlsSet.getChildren().add(0,visualControl);
                        controlFound = true;
                        break;
                    }
                }
            } else {
                break;
            }
        }
        /// In case no shortcut is defined, just grab the first control found, check if the control is added, if not, it was an empty group.
        if(controlsSet.getChildren().size()==0){
            for(DeviceControlGroup group:this.device.getControlGroups()){
                for(DeviceControl control:group.getGroupControls()){
                    VisualDeviceControlHelper visualControl = new VisualDeviceControlHelper(this.scene, connection, control, false);
                    controlsSet.getChildren().add(0,visualControl);
                    break;
                }
                if(controlsSet.getChildren().size()==1){
                    break;
                }
            }
        }
        this.setContent(controlsSet);
    }

    @Override
    public void destroy() {
        for(Node node:controlsSet.getChildren()){
            ((VisualDeviceControlHelper)node).destroy();
        }
    }
}