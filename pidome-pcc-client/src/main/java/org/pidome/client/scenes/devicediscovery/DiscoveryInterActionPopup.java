/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.devicediscovery;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryDriver;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService;
import org.pidome.client.scenes.panes.popups.PopUp;

/**
 *
 * @author John
 */
public class DiscoveryInterActionPopup extends PopUp {

    private ObservableList<String> options = FXCollections.observableArrayList(
                    "Single device",
                    "1 Minute",
                    "5 minutes",
                    "10 minutes",
                    "30 minutes",
                    "indefinitely"
            );
    private final ComboBox comboBox = new ComboBox(options);
    
    private DeviceDiscoveryDriver driver;
    
    public DiscoveryInterActionPopup(DeviceDiscoveryDriver driver) {
        super(MaterialDesignIcon.PLUS_NETWORK, driver.getDiscoveryIsActive().getValue()?"Disable discovery/scanning":"Enable discovery/scanning");
        this.driver = driver;
    }

    protected final void compose(){
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        content.setMinWidth(USE_PREF_SIZE);
        content.setMaxWidth(USE_PREF_SIZE);
        Text whatToDo = new Text();
        whatToDo.getStyleClass().add("text");
        whatToDo.setWrappingWidth(560);
        if(!driver.getDiscoveryIsActive().getValue()){
            whatToDo.setText("Enable discovery/scanning for: " + driver.getDriverName() + "?");
            content.getChildren().addAll(whatToDo, comboBox);
        } else {
            whatToDo.setText("Do you want to stop discovery/scanning for: " + driver.getDriverName() + "?");
            content.getChildren().addAll(whatToDo);
        }
        comboBox.setMinWidth(560);
        this.setContent(content);
    }
    
    /**
     * Returns the selection option as string.
     * @return 
     */
    protected final DeviceDiscoveryService.DiscoveryType getSelectedOption(){
        DeviceDiscoveryService.DiscoveryType type = DeviceDiscoveryService.DiscoveryType.NONE;
        switch((String)this.comboBox.getSelectionModel().getSelectedItem()){
            case "Single device":
                type = DeviceDiscoveryService.DiscoveryType.SINGLE_DEVICE;
            break;
            case "1 Minute":
                type = DeviceDiscoveryService.DiscoveryType.MINUTE_1;
            break;
            case "5 minutes":
                type = DeviceDiscoveryService.DiscoveryType.MINUTE_5;
            break;
            case "10 minutes":
                type = DeviceDiscoveryService.DiscoveryType.MINUTE_10;
            break;
            case "30 minutes":
                type = DeviceDiscoveryService.DiscoveryType.MINUTE_30;
            break;
            case "indefinitely":
                type = DeviceDiscoveryService.DiscoveryType.INDEFINITELY;
            break;
        }
        return type;
    }
    
    @Override
    public void unload() {
        this.driver = null;
    }
    
}
