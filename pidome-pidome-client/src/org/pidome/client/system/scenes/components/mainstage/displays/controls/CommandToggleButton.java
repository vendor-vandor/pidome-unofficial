/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.scenes.components.mainstage.displays.controls;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.scenes.ComponentDimensions;

/**
 *
 * @author John Sirach
 */
public class CommandToggleButton extends DeviceCmd implements DeviceValueChangeListener {

    static Logger LOG = LogManager.getLogger(CommandToggleButton.class);

    ToggleButton button = new ToggleButton();
    ToggleGroup group = new ToggleGroup();

    Rectangle indicator = new Rectangle();

    Boolean serverData = false;

    Device device;

    ComponentDimensions dimensions = new ComponentDimensions();

    double width = 100 * DisplayConfig.getHeightRatio();
    double height = 40 * DisplayConfig.getHeightRatio();

    VBox interfaceButton;

    public CommandToggleButton(Device device) {
        this.device = device;
        button.setFocusTraversable(false);
    }

    @Override
    void build() {
        boolean lastKnownCmd = (boolean)device.getLastCmd(groupName, setName);
        button.getStyleClass().add("unknown");
        button.setWrapText(true);
        indicator.getStyleClass().add("unknown");
        if (lastKnownCmd == false || lastKnownCmd == true) {
            for (String id : this.cmdSet.keySet()) {
                button.getStyleClass().remove("unknown");
                indicator.getStyleClass().remove("unknown");
                if (lastKnownCmd == false && this.cmdSet.get(id).get("type").equals("off")){
                    button.setText((String) this.cmdSet.get(id).get("label"));
                    button.setSelected(false);
                    button.getStyleClass().add("off");
                    indicator.getStyleClass().add("off");
                } else if (lastKnownCmd == true && this.cmdSet.get(id).get("type").equals("on")) {
                    button.setText((String) this.cmdSet.get(id).get("label"));
                    button.setSelected(true);
                    button.getStyleClass().add("on");
                    indicator.getStyleClass().add("on");
                }
            }
        }
        if (button.getText().equals("")) {
            button.setText("Unknown");
            button.getStyleClass().add("unknown");
            indicator.getStyleClass().add("unknown");
        }
        button.setToggleGroup(group);
        group.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) -> {
            if (new_toggle != null && serverData == false) {
                ToggleButton pressed = (ToggleButton) group.getSelectedToggle();
                if (pressed != null) {
                    for (String id : cmdSet.keySet()) {
                        if (cmdSet.get(id).get("type").equals("on")) {
                            device.sendCommand(groupName,
                                    setName,
                                    true, "");
                        }
                    }
                }
            }
            if (toggle != null && serverData == false) {
                for (String id : cmdSet.keySet()) {
                    if (cmdSet.get(id).get("type").equals("off")) {
                        device.sendCommand(groupName,
                                setName,
                                false, "");
                    }
                }
            }
            serverData = false;
        });
        buildButton();
    }

    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        switch (event.getEventType()) {
            case DeviceValueChangeEvent.VALUECHANGED:
                final String eventSet = event.getSet();
                final Object eventValue = event.getValue();
                LOG.debug("Received: {}, data: {}, {}", DeviceValueChangeEvent.VALUECHANGED, eventSet, eventValue);
                if (eventSet.equals(setName)) {
                    serverData = true;
                    for(final String id:cmdSet.keySet()){
                        if (this.cmdSet.get(id).get("type").equals("on") && (boolean) eventValue == true) {
                            Platform.runLater(() -> {
                                button.setText((String) cmdSet.get(id).get("label"));
                                button.getStyleClass().remove("unknown");
                                indicator.getStyleClass().remove("unknown");
                                button.setSelected(true);
                                button.getStyleClass().remove("off");
                                indicator.getStyleClass().remove("off");
                                button.getStyleClass().add("on");
                                indicator.getStyleClass().add("on");
                            });
                        } else if (this.cmdSet.get(id).get("type").equals("off") && (boolean) eventValue == false) {
                            Platform.runLater(() -> {
                                button.setText((String) cmdSet.get(id).get("label"));
                                button.getStyleClass().remove("unknown");
                                indicator.getStyleClass().remove("unknown");
                                button.setSelected(false);
                                button.getStyleClass().remove("on");
                                indicator.getStyleClass().remove("on");
                                button.getStyleClass().add("off");
                                indicator.getStyleClass().add("off");
                            });
                        }
                    }
                    serverData = false;
                }
                break;
        }
    }

    void buildButton() {
        interfaceButton = new VBox();
        interfaceButton.setPrefSize(width, height);
        interfaceButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        interfaceButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        interfaceButton.setAlignment(Pos.CENTER);
        interfaceButton.getStyleClass().add("devicebutton");
        interfaceButton.getChildren().add(button);
        button.setPrefSize(width, height - (12 * dimensions.heightRatio));
        button.setFocusTraversable(false);
        indicator.setWidth(width - (26 * dimensions.widthRatio));
        indicator.setHeight(6 * dimensions.heightRatio);
        indicator.getStyleClass().add("indicator");
        interfaceButton.getChildren().add(indicator);
        device.addDeviceValueEventListener(this, groupName, setName);
    }

    @Override
    public final Pane getInterface() {
        return getButton();
    }

    public VBox getButton() {
        return interfaceButton;
    }

    @Override
    public final void removeListener() {
        device.removeDeviceValueEventListener(this, groupName, setName);
    }

}
