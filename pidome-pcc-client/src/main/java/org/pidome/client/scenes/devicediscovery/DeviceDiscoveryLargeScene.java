/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.devicediscovery;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryDriver;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryException;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService;
import org.pidome.client.entities.devicediscovery.DiscoveredDevice;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.devices.DevicesSmallScene;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;

/**
 *
 * @author John
 */
public class DeviceDiscoveryLargeScene implements ScenePaneImpl {

    PCCSystem system;
    
    DeviceDiscoveryService service;
    
    ///Driver list mutators and handling
    ObservableArrayListBean<DriverItem> driversList = new ObservableArrayListBean();
    private ObservableArrayListBeanChangeListener<DriverItem> driverListMutator = this::driverListMutator;
    VBox visualDriversList = new VBox();
    
    BorderPane rootPane = new BorderPane();    
    
    double maxWidth = 350;
    
    TilePane foundDevicesContent = new TilePane();
    
    public DeviceDiscoveryLargeScene(){
        rootPane.getStyleClass().add("device-discovery");
        setupDiscoveryPanes();
    }
    
    private void setupDiscoveryPanes(){
        ScrollPane driversScroll = new ScrollPane();
        driversScroll.getStyleClass().add("list-view-root");
        driversScroll.setHmax(0.1);
        
        driversScroll.setMinWidth(USE_PREF_SIZE);
        driversScroll.setMaxWidth(USE_PREF_SIZE);
        driversScroll.setPrefWidth(maxWidth);
        
        visualDriversList = new VBox();
        visualDriversList.getStyleClass().add("custom-list-view");
        
        driversScroll.setContent(visualDriversList);
        
        foundDevicesContent.setHgap(15);
        foundDevicesContent.setVgap(11);
        
        rootPane.setLeft(driversScroll);
        foundDevicesContent.setPadding(new Insets(10));
        rootPane.setCenter(foundDevicesContent);
    }
    
    @Override
    public String getTitle() {
        return "Device discovery";
    }

    @Override
    public void start() {
        try {
            service = this.system.getClient().getEntities().getDeviceDiscoveryService();
            driversList.addListener(driverListMutator);
            refreshDriversList();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DevicesSmallScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void refreshDriversList(){
        try {
            for(DriverItem driver:driversList){
                driver.destroy();
            }
            driversList.clear();
            List<DriverItem> addAll = new ArrayList<>();
            for(DeviceDiscoveryDriver driver:service.getDiscoveryEnabledDrivers()){
                addAll.add(new DriverItem(driver, maxWidth));
            }
            driversList.addAll(addAll);
        } catch (DeviceDiscoveryException ex) {
            Logger.getLogger(DeviceDiscoveryLargeScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void driverListMutator(ObservableArrayListBeanChangeListener.Change<? extends DriverItem> change) {
        if(change.wasAdded()){
            if(change.hasNext()){
                Platform.runLater(() -> { 
                    visualDriversList.getChildren().addAll(change.getAddedSubList());
                });
            }
        } else if (change.wasRemoved()){
            if(change.hasNext()){
                Platform.runLater(() -> { 
                    visualDriversList.getChildren().removeAll(change.getRemoved());
                });
            }
        }
    }
    
    @Override
    public void close() {
        driversList.removeListener(driverListMutator);
        try {
            service.unloadContent();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DeviceDiscoveryLargeScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Pane getPane() {
        return rootPane;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    @Override
    public void removeSystem() {
        system = null;
    }

    private class DriverItem extends BorderPane {
        
        private final Text notRunning = GlyphsDude.createIcon(FontAwesomeIcon.EYE_SLASH, "1.3em;");
        private final Text running = GlyphsDude.createIcon(FontAwesomeIcon.EYE, "1.3em;");
        
        private final Label numFound = new Label();
        
        private final PropertyChangeListener changed = this::activeChanged;
        private final ObservableArrayListBeanChangeListener<DiscoveredDevice> devicesChanged = this::devicesChanged;
        
        private final DeviceDiscoveryDriver driver;
        
        private Label name;
        
        DiscoveryInterActionPopup popup;
        
        private DriverItem(DeviceDiscoveryDriver driver, double maxWidth){
            this.setMaxWidth(maxWidth);
            this.setMinWidth(maxWidth);
            getStyleClass().addAll("list-item", "driver-discovery-driver");
            notRunning.getStyleClass().add("disabled");
            running.getStyleClass().add("enabled");
            numFound.setText(String.valueOf(driver.getFoundDevices()));
            name = new Label(driver.getDriverName());
            HBox.setHgrow(name, Priority.ALWAYS);
            name.setPadding(new Insets(0,5,0,5));
            BorderPane.setAlignment(name, Pos.CENTER_LEFT);
            if(driver.getDiscoveryIsActive().getValue()== true){
                setLeft(running);
                name.getStyleClass().add("enabled");
                numFound.getStyleClass().add("enabled");
            } else {
                setLeft(notRunning);
            }
            setCenter(name);
            setRight(numFound);
            this.driver = driver;
            driver.getDiscoveryIsActive().addPropertyChangeListener(changed);
            List<DiscoveredDeviceDisplay> toAdd = new ArrayList<>();
            for(DiscoveredDevice device:driver.getFoundDevicesList().subList(0, driver.getFoundDevicesList().size())){
                toAdd.add(new DiscoveredDeviceDisplay(driver, device, system.getClient().getEntities()));
            }
            Platform.runLater(() -> { foundDevicesContent.getChildren().addAll(toAdd); });
            driver.getFoundDevicesList().addListener(devicesChanged);
            setSelfClickHandler();
        }
        
        private void setSelfClickHandler(){
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                popup = new DiscoveryInterActionPopup(driver);
                popup.compose();
                popup.setButtons(new PopUp.PopUpButton[]{new PopUp.PopUpButton("YES", "Yes"), new PopUp.PopUpButton("NO", "No")});
                popup.addListener((String buttonId) -> {
                    switch(buttonId){
                        case "NO":
                            /// Well, do nothing i guess :), the popup's resources are released automatically by underlying close mechanism
                        break;
                        case "YES":
                            if(driver.getDiscoveryIsActive().getValue()==true){
                                try {
                                    driver.disableDiscovery();
                                } catch (DeviceDiscoveryException ex) {
                                    Logger.getLogger(DeviceDiscoveryLargeScene.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                try {
                                    driver.enableDiscovery(popup.getSelectedOption());
                                } catch (DeviceDiscoveryException ex) {
                                    Logger.getLogger(DeviceDiscoveryLargeScene.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        break;
                    }
                });
                popup.build();
                popup.show(true);
            });
        }
        
        private void devicesChanged(ObservableArrayListBeanChangeListener.Change<? extends DiscoveredDevice> change){
            if(change.wasAdded()){
                List<DiscoveredDeviceDisplay> toAdd = new ArrayList<>();
                for(DiscoveredDevice device:change.getAddedSubList()){
                    toAdd.add(new DiscoveredDeviceDisplay(driver, device, system.getClient().getEntities()));
                }
                Platform.runLater(() -> { foundDevicesContent.getChildren().addAll(toAdd); });
            }
            if(change.wasRemoved()){
                List<DiscoveredDeviceDisplay> toRemove = new ArrayList<>();
                for(DiscoveredDevice device:change.getRemoved()){
                    for(Node node:foundDevicesContent.getChildren()){
                        if(node instanceof DiscoveredDeviceDisplay && ((DiscoveredDeviceDisplay)node).getDiscoveredDevice()==device){
                            ((DiscoveredDeviceDisplay)node).destroy();
                            toRemove.add((DiscoveredDeviceDisplay)node);
                        }
                    }
                }
                Platform.runLater(() -> { foundDevicesContent.getChildren().removeAll(toRemove); });
            }
            Platform.runLater(() -> { numFound.setText(String.valueOf(driver.getFoundDevices())); });
        }
        
        
        private void activeChanged(PropertyChangeEvent event){
            if(popup != null){
                popup.close();
                popup = null;
            }
            if((boolean)event.getNewValue()==true){
                Platform.runLater(() -> { 
                    this.setLeft(running); 
                    if(!name.getStyleClass().contains("enabled")){
                        name.getStyleClass().add("enabled");
                    }
                    if(!numFound.getStyleClass().contains("enabled")){
                        numFound.getStyleClass().add("enabled");
                    }
                });
            } else {
                Platform.runLater(() -> { 
                    this.setLeft(notRunning); 
                    name.getStyleClass().remove("enabled"); 
                    numFound.getStyleClass().remove("enabled"); 
                });
            }
        }
        
        private void destroy(){
            driver.getDiscoveryIsActive().removePropertyChangeListener(changed);
            driver.getFoundDevicesList().removeListener(devicesChanged);
        }
        
    }
    
    
}
