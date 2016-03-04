/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.devicediscovery;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.Entities;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryDriver;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryException;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService.DeviceAddFunctionType;
import org.pidome.client.entities.devicediscovery.DiscoveredDevice;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.scenes.panes.popups.SimpleDialog;

/**
 *
 * @author John
 */
public class DiscoveredDeviceDisplay extends VBox {
    
    private DiscoveredDevice device;
    
    private DeviceDiscoveryDriver driver;
    
    AddDiscoveredDevicePopup popup;
    
    protected DiscoveredDeviceDisplay(DeviceDiscoveryDriver driver, DiscoveredDevice device, Entities entities){
        this.device = device;
        this.getStyleClass().add("discovered-device");
        this.setPrefSize(290, 140);
        this.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        this.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        this.setPadding(new Insets(5));
        this.driver  =driver;
        setTitle();
        build();

        addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
            popup = new AddDiscoveredDevicePopup();
            if(device.getDeviceFunctionType() == DeviceAddFunctionType.FUNCTION_REQUEST_ADDRESS){
                popup.setButtons(new PopUp.PopUpButton[]{new PopUp.PopUpButton("SET_ADDRESS", "Set address"), new PopUp.PopUpButton("CANCEL", "Cancel")});
                popup.addListener((String buttonId) -> {
                    switch(buttonId){
                        case "CANCEL":
                            /// Well, do nothing i guess :), the popup's resources are released automatically by underlying close mechanism
                        break;
                        case "SET_ADDRESS":
                            SimpleDialog dialog = new SimpleDialog(MaterialDesignIcon.PLUS_NETWORK, "Assigning address");
                            Label whatsHappening = new Label("Please wait, sending new address");
                            StackPane whatsHappeningHolder = new StackPane();
                            whatsHappeningHolder.setPadding(new Insets(10));
                            whatsHappeningHolder.setMinWidth(400);
                            whatsHappeningHolder.getChildren().add(whatsHappening);
                            dialog.setContent(whatsHappeningHolder);
                            dialog.setButtons();
                            dialog.build();
                            dialog.show(true);
                            new Thread(() -> {
                                try {
                                    if (driver.setDiscoveredDeviceNewAddress(device, popup.getNewAddress())) {
                                        Platform.runLater(() -> {
                                            whatsHappening.setText("New address request send");
                                        });
                                    } else {
                                        Platform.runLater(() -> {
                                            whatsHappening.setText("Failed to assign addres, check server.");
                                        });
                                    }
                                } catch (DeviceDiscoveryException ex) {
                                    Platform.runLater(() -> {
                                        whatsHappening.setText("An error occured while communicating with the server: " + ex.getMessage());
                                    });
                                }
                            }).start();
                        break;
                    }
                });
            } else {
                popup.setButtons(new PopUp.PopUpButton[]{new PopUp.PopUpButton("ADD", "Add"), new PopUp.PopUpButton("CANCEL", "Cancel")});
                try {
                    popup.setCategories(entities.getCategoryService().getCategories());
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(DiscoveredDeviceDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    popup.setInstalledDevices(entities.getDeviceService().getInstalledDevicesByLiveDriver(driver.getPort()));
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(DiscoveredDeviceDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    popup.setLocations(entities.getLocationService().getLocations());
                } catch (EntityNotAvailableException ex) {
                    Logger.getLogger(DiscoveredDeviceDisplay.class.getName()).log(Level.SEVERE, null, ex);
                }
                popup.addListener((String buttonId) -> {
                    switch(buttonId){
                        case "CANCEL":
                            /// Well, do nothing i guess :), the popup's resources are released automatically by underlying close mechanism
                        break;
                        case "ADD":
                            SimpleDialog dialog = new SimpleDialog(MaterialDesignIcon.PLUS_NETWORK, "Add device");
                            Label whatsHappening = new Label("Please wait, adding device to the server");
                            StackPane whatsHappeningHolder = new StackPane();
                            whatsHappeningHolder.setMinWidth(400);
                            whatsHappeningHolder.setPadding(new Insets(10));
                            whatsHappeningHolder.getChildren().add(whatsHappening);
                            dialog.setContent(whatsHappeningHolder);
                            dialog.setButtons();
                            dialog.build();
                            dialog.show(true);
                            new Thread(() -> {
                                try {
                                    if (driver.addDiscoveredDevice(device, popup.getNewDeviceId(),popup.getNewDeviceName(),popup.getNewCategory(), popup.getNewLocation())) {
                                        Platform.runLater(() -> {
                                            whatsHappening.setText("Device has been added to the server.");
                                        });
                                    } else {
                                        Platform.runLater(() -> {
                                            whatsHappening.setText("Failed to to device to the server. Check server log.");
                                        });
                                    }
                                } catch (DeviceDiscoveryException ex) {
                                    Platform.runLater(() -> {
                                        whatsHappening.setText("An error occured while communicating with the server: " + ex.getMessage());
                                    });
                                }
                            }).start();
                        break;
                    }
                });
            }
            popup.compose(device);
            popup.build();
            popup.show(true);
        });
        
    }
    
    private void setTitle(){
        HBox titleBox = new HBox(5);
        Label title;
        if(device.isKnownDevice()){
            this.getStyleClass().add("known-device");
            title = new Label("Unknown device, please verify");
        } else {
            this.getStyleClass().add("unknown-device");
            title = new Label("Unknown device");
        }
        title.getStyleClass().add("title");
        title.setMaxWidth(Double.MAX_VALUE);
        Text closeIcon = GlyphsDude.createIcon(FontAwesomeIcon.CLOSE, String.valueOf("1.4em"));
        closeIcon.setPickOnBounds(true);
        closeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            event.consume();
            new Thread(() -> {
                driver.removeDiscoveredDevice(device);
            }).start();
        });
        HBox.setHgrow(title, Priority.ALWAYS);
        titleBox.getChildren().addAll(title, closeIcon);
        getChildren().add(titleBox);
    }
    
    private void build(){
        Label driverName = new Label(driver.getDriverName());
        getChildren().add(driverName);
        Label deviceName = new Label(device.getDeviceName());
        deviceName.setWrapText(true);
        getChildren().add(deviceName);
        Label discoveryTime = new Label(device.getDeviceDiscoveryTime());
        getChildren().add(discoveryTime);
    }
    
    protected final DiscoveredDevice getDiscoveredDevice(){
        return this.device;
    }
    
    public final void destroy(){
        this.device = null;
        this.driver = null;
        if(popup!=null){
            popup.close();
            popup = null;
        }
    }
    
}