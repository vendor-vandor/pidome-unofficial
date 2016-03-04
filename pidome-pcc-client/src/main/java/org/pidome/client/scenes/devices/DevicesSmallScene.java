/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.devices;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javax.security.auth.Destroyable;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlGroup;
import org.pidome.client.entities.locations.Location;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.scenes.panes.lists.ListClickedHandler;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.visuals.controls.devices.VisualDeviceControlHelper;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class DevicesSmallScene implements ScenePaneImpl,ListClickedHandler<Integer>,ListBackHandler {

    StackPane container = new StackPane();
    
    ScrollPane mainContent = new ScrollPane();
    
    VBox devicesList = new VBox();
    VBox devicesContent = new VBox();    
    
    ReadOnlyObservableArrayListBean<Device> devices;
    
    private ObservableList<Location> locationList = FXCollections.observableArrayList();
    private ObservableArrayListBeanChangeListener<Device> deviceMutator = this::deviceMutator;
    
    SortedList<Location> sortedLocationList = new SortedList<>(locationList,
            (Location loc1, Location loc2) -> {
                return (loc1.getCombinedName().getValue().compareToIgnoreCase(loc2.getCombinedName().getValue()));
            });
    
    private int currentLocationId = 0;
    
    private String currentLocationName = "";
    
    PCCSystem system;
    
    private enum CurrentDisplay {
        LOCATIONS,DEVICES,DETAILS;
    }
    
    private CurrentDisplay currentDisplay = CurrentDisplay.LOCATIONS;
    
    public DevicesSmallScene() {
        mainContent.getStyleClass().add("list-view-root");
        setupDevicesPane();
        setDefaultSceneTitle();
        mainContent.setHmax(0.1);
    }

    private void setupDevicesPane(){
        devicesList.getStyleClass().add("custom-list-view");
        devicesContent.getStyleClass().add("full-device");
        
        devicesList.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        devicesContent.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        
        devicesList.setMinWidth(Control.USE_PREF_SIZE);
        devicesContent.setMinWidth(Control.USE_PREF_SIZE);
        
        devicesList.setMaxWidth(Control.USE_PREF_SIZE);
        devicesContent.setMaxWidth(Control.USE_PREF_SIZE);
        
        mainContent.setContent(devicesList);
        
        container.heightProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
             mainContent.setMinHeight(newValue.doubleValue());
             mainContent.setMaxHeight(newValue.doubleValue());
        });
        
        container.widthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
             mainContent.setMinWidth(newValue.doubleValue());
             mainContent.setMaxWidth(newValue.doubleValue());
        });
        
        container.getChildren().add(mainContent);
        
    }
    
    private void setDefaultSceneTitle(){
        ScenesHandler.setSceneTitle("Devices");
    }
    
    @Override
    public String getTitle() {
        return "Devices";
    }
    
    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }
    
    @Override
    public void start() {
        try {
            devices = this.system.getClient().getEntities().getDeviceService().getDevices();
            createGoodListFromDevicesCollection();
            devices.addListener(deviceMutator);
            this.system.getClient().getEntities().getDeviceService().reload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DevicesSmallScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deviceMutator(ObservableArrayListBeanChangeListener.Change<? extends Device> change) {
        if(change.wasAdded()){
            if(change.hasNext()){
                for(Device device:change.getAddedSubList()){
                    if(!locationList.contains(device.getLocation())){
                        locationList.add(device.getLocation());
                    }
                    if(currentDisplay == CurrentDisplay.DEVICES){
                        if(currentLocationId == device.getDeviceLocationId()){
                            devicesList.getChildren().add(new DeviceListItem(device));
                        }
                    }
                }
            }
        } else if (change.wasRemoved()){
            List<DeviceListItem> toRemove = new ArrayList<>();
            if(change.hasNext()){
                if(currentDisplay == CurrentDisplay.DEVICES){
                    for(Device device:change.getRemoved()){
                        for(Node node:devicesList.getChildren()){
                            DeviceListItem checkDevice = (DeviceListItem)node;
                            if(checkDevice.getDevice()==device){
                                toRemove.add(checkDevice);
                            }
                        }
                    }
                }
                for(Device device:change.getRemoved()){
                    if(isLastDeviceWithLocationId(device)){
                        locationList.remove(device.getDeviceLocationId());
                    }
                }
            }
            Platform.runLater(() -> { 
                devicesList.getChildren().removeAll(toRemove);
            });
        }
    }
    
    private boolean isLastDeviceWithLocationId(Device device){
        int locationId = device.getDeviceLocationId();
        Iterator<Device> deviceList = devices.iterator();
        while(deviceList.hasNext()){
            Device checkIt = deviceList.next();
            if(checkIt!=device && checkIt.getDeviceLocationId() == locationId){
                return true;
            }
        }
        return false;
    }
    
    private void createGoodListFromDevicesCollection(){
        locationList.addListener((ListChangeListener.Change<? extends Location> change) -> {
            if(currentDisplay == CurrentDisplay.LOCATIONS){
                while(change.next()){
                    if(change.wasAdded()){
                        Platform.runLater(() -> {
                            for(Location location:change.getAddedSubList()){
                                devicesList.getChildren().add(new DeviceLocationListItem(location.getLocationId(), location.getRoomName().getValue(), DevicesSmallScene.this));
                            }
                        });
                    }
                    if(change.wasRemoved()){
                        DeviceLocationListItem toRemove = null;
                        for(Node node:devicesList.getChildren()){
                            DeviceLocationListItem check = (DeviceLocationListItem) node;
                            for(Location location:change.getRemoved()){
                                if(location.getLocationId()==check.getLocationId()){
                                    toRemove = check;
                                    break;
                                }
                            }
                        }
                        if(toRemove!=null){
                            final DeviceLocationListItem delete = toRemove;
                            Platform.runLater(() -> { 
                                devicesList.getChildren().remove(delete);
                            });
                        }
                    }
                }
                Platform.runLater(() -> {
                    ObservableList<Node> workingCollection = FXCollections.observableArrayList(
                        devicesList.getChildren()
                    );
                    Collections.sort(workingCollection, (Node arg0, Node arg1) -> ((DeviceLocationListItem)arg0).getLocationName().compareToIgnoreCase(((DeviceLocationListItem)arg1).getLocationName()));
                    devicesList.getChildren().setAll(workingCollection);
                    //Collections.sort(deviceLocationsList.getChildren(), );
                });
            }
        });
    }
    
    @Override
    public Pane getPane() {
        return this.container;
    }
    
    @Override
    public void removeSystem() {
        system = null;
    }
    
    @Override
    public void close() {
        devicesList.prefWidthProperty().unbind();
        devicesContent.prefWidthProperty().unbind();
        if(devices!=null){
            devices.removeListener(deviceMutator);
            locationList.clear();
            devices = null;
        }
    }

    @Override
    public void itemClicked(Integer locationId, String description) {
        currentLocationId = locationId;
        currentLocationName = description;
        createDeviceListPane(locationId);
        ScenesHandler.setSceneBackTitle(this, "location", description);
    }
    
    private void moveToLocations() {
        if(currentDisplay == CurrentDisplay.DEVICES){
            List<DeviceLocationListItem> itemsToAdd = new ArrayList<>();
            for(Location entry:locationList){
                itemsToAdd.add(new DeviceLocationListItem(entry.getLocationId(), entry.getRoomName().getValue(), DevicesSmallScene.this));
            }
            Collections.sort(itemsToAdd, (DeviceLocationListItem arg0, DeviceLocationListItem arg1) -> arg0.getLocationName().compareToIgnoreCase(arg1.getLocationName()));
            Platform.runLater(() -> { 
                devicesList.getChildren().setAll(itemsToAdd);
            });
        }
        currentDisplay = CurrentDisplay.LOCATIONS;
    }

    private void moveToDeviceList() {
        List<DeviceListItem> itemsToAdd = new ArrayList<>();
        for(Device device:devices.subList(0, devices.size()-1)){
            if(device.getDeviceLocationId()==currentLocationId){
                itemsToAdd.add(new DeviceListItem(device));
            }
        }
        if(currentDisplay == CurrentDisplay.LOCATIONS){
            Collections.sort(itemsToAdd, (DeviceListItem arg0, DeviceListItem arg1) -> arg0.getDevice().getDeviceName().compareToIgnoreCase(arg1.getDevice().getDeviceName()));
            Platform.runLater(() -> { 
                devicesList.getChildren().setAll(itemsToAdd);
            });
        } else if(currentDisplay == CurrentDisplay.DETAILS) {
            for(Node node:devicesContent.getChildren()){
                if(node instanceof VisualDeviceControlHelper){
                    ((VisualDeviceControlHelper)node).destroy();
                }
            }
            Platform.runLater(() -> {
                devicesContent.getChildren().clear();
                devicesList.getChildren().addAll(itemsToAdd);
                mainContent.setContent(devicesList);
            });
        }
        currentDisplay = CurrentDisplay.DEVICES;
    }
    
    @Override
    public void handleListBack(String id) {
        switch(id){
            case "location":
                setDefaultSceneTitle();
                moveToLocations();
            break;
            case "devicelist":
                ScenesHandler.setSceneBackTitle(this, "location", currentLocationName);
                moveToDeviceList();
            break;
        }
    }
    
    private void createDeviceListPane(int locationId){
        Platform.runLater(() -> { 
            this.devicesList.getChildren().clear();
            Iterator<Device> list = devices.iterator();
            while(list.hasNext()){
                Device device = list.next();
                if(device.getDeviceLocationId()==currentLocationId){
                    this.devicesList.getChildren().add(new DeviceListItem(device));
                }
            }
            moveToDeviceList();
        });
    }
    
    private static class DeviceLocationListItem extends HBox {
        
        private final int locationId;
        private final String locationName;
        
        DeviceLocationListItem(int locationId, String locationName, ListClickedHandler<Integer> handler){
            this.locationId = locationId;
            this.locationName = locationName;
            getStyleClass().addAll("list-item", "list-item-pressable", "undecorated-list-item");
            
            Label name = new Label(locationName);
            StackPane nameHolder = new StackPane();
            StackPane.setAlignment(name, Pos.CENTER_LEFT);
            nameHolder.getChildren().add(name);
            HBox.setHgrow(nameHolder, Priority.ALWAYS);
            Text pointer = GlyphsDude.createIcon(FontAwesomeIcon.ANGLE_RIGHT, "1.4em;");
            getChildren().addAll(nameHolder, pointer);
            
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                handler.itemClicked(locationId, locationName);
            });
            
        }
     
        public final String getLocationName(){
            return this.locationName;
        }
        
        public final int getLocationId(){
            return this.locationId;
        }
    }
    
    private class DeviceListItem extends HBox implements Destroyable {
        
        private final Device device;
        
        DevicePane currentDevicePane;
        
        DeviceListItem(Device device){
            getStyleClass().addAll("list-item", "list-item-pressable", "undecorated-list-item");
            this.device = device;
            Label deviceName = new Label(device.getDeviceName());
            
            StackPane nameHolder = new StackPane();
            StackPane.setAlignment(deviceName, Pos.CENTER_LEFT);
            nameHolder.getChildren().add(deviceName);
            HBox.setHgrow(nameHolder, Priority.ALWAYS);
            Text pointer = GlyphsDude.createIcon(FontAwesomeIcon.ANGLE_RIGHT, "1.4em;");
            
            getChildren().addAll(nameHolder, pointer);
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                createDeviceDetails(device);
            });
        }
    
        private Device getDevice(){
            return this.device;
        }

        @Override
        public void destroy() {
            if(currentDevicePane!=null){
                currentDevicePane.destroy();
                currentDevicePane = null;
            }
        }
        
    }
    
    private void createDeviceDetails(Device device){
        devicesContent.getChildren().clear();
        List<Node> addList = new ArrayList();
        for(DeviceControlGroup group:device.getControlGroups()){
            Label groupName = new Label(group.getGroupName());
            groupName.setPrefWidth(Double.MAX_VALUE);
            groupName.getStyleClass().add("controls-group-name");
            addList.add(groupName);
            for(DeviceControl control:group.getGroupControls()){
                VisualDeviceControlHelper visualControl = new VisualDeviceControlHelper(this.system.getConnection(), control, true);
                addList.add(visualControl);
            }
        }
        devicesContent.getChildren().addAll(addList);
        mainContent.setContent(devicesContent);
        devicesList.getChildren().clear();
        currentDisplay = CurrentDisplay.DETAILS;
        ScenesHandler.setSceneBackTitle(this, "devicelist", device.getDeviceName());
    }
    
}