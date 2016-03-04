/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;

/**
 *
 * @author John Sirach
 */
public class CommandButtonSet extends DeviceCmd implements DeviceValueChangeListener {

    static Logger LOG = LogManager.getLogger(CommandDataField.class);
    
    double width = 100*DisplayConfig.getHeightRatio();
    double height = 40*DisplayConfig.getHeightRatio();
    
    Device device;
    
    boolean serverData;
    
    Text currentDataText = new Text();
    Label currenDataLabel = new Label();
    
    String prefix = "";
    String suffix = "";
    
    HBox boxLine = new HBox(5*DisplayConfig.getHeightRatio());
    Button button = new Button();
    public CommandButtonSet(Device device){
        this.device = device;
    }
    
    public void setSize(double width, double height){
        this.width = width;
        this.height= height;
    }
    
    @Override
    final public HBox getInterface(){
        boxLine.setAlignment(Pos.CENTER_LEFT);
        Label desc = new Label();
        button.getStyleClass().add("devicebutton");
        for(String id:this.cmdSet.keySet()){
            button.setUserData(this.cmdSet.get(id).get("value"));
            desc.setText((String)this.cmdSet.get(id).get("description"));
            button.setText((String)this.cmdSet.get(id).get("label"));
            button.setWrapText(true);
            button.addEventHandler(ActionEvent.ACTION, this::buttonHelper);
        }
        button.setPrefSize(width, height);
        boxLine.getChildren().addAll(button, desc);
        return boxLine;
    }

    final void buttonHelper(ActionEvent event){
        device.sendCommand(groupName,
                setName,
                ((Button)event.getSource()).getUserData(),
                "");
    }
    
    @Override
    void build() {
        /// not used anymore
    }

    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch(event.getEventType()){
            case DeviceValueChangeEvent.VALUECHANGED:
                String eventSet  = event.getSet();
                final Object eventValue= event.getValue();
                LOG.debug("Received: {}, data: {}, {}", DeviceValueChangeEvent.VALUECHANGED, eventSet, eventValue);
                if(eventSet.equals(setName)){

                }
                serverData = false;
            break;
        }
    }

    final public Button getButton(){
        Button button = new Button();
        button.getStyleClass().add("devicebutton");
        for(String id:this.cmdSet.keySet()){
            button.setUserData(this.cmdSet.get(id).get("value"));
            button.setText((String)this.cmdSet.get(id).get("label"));
            button.setWrapText(true);
            button.setOnAction((ActionEvent event) -> {
                device.sendCommand(groupName,
                        setName,
                        button.getUserData(),
                        "");
            });
        }
        button.setPrefSize(width, height);
        return button;
    }
    
    
    @Override
    public final void removeListener(){
        device.removeDeviceValueEventListener(this, groupName,setName);
        button.removeEventHandler(ActionEvent.ACTION, this::buttonHelper);
    }
    
}
