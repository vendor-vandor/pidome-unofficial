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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.visuals.controls.devices.VisualDeviceControlHelper;
import org.pidome.client.scenes.panes.lists.ListClickedHandler;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class DevicesLargeScene extends StackPane implements ScenePaneImpl,ListClickedHandler<Integer> {

    private GridPane devicesPane = new GridPane();
    
    ScrollPane devicesLocationsScrollList = new ScrollPane();
    VBox deviceLocationsList = new VBox();
    
    ScrollPane devicesListScrollList = new ScrollPane();
    VBox devicesList = new VBox();
    
    ScrollPane deviceContentScroll = new ScrollPane();
    VBox devicesContent = new VBox();
    
    PCCSystem system;
    
    ReadOnlyObservableArrayListBean<Device> devices;
    
    private ObservableList<Location> locationList = FXCollections.observableArrayList();
    private ObservableArrayListBeanChangeListener<Device> deviceMutator = this::deviceMutator;
    
    private int currentLocationId = 0;
    
    private String currentLocationName = "";
    
    private boolean firstLoad = true;
    private Location firstItem;
    
    public DevicesLargeScene(){
        
        devicesLocationsScrollList.setContent(deviceLocationsList);
        devicesListScrollList.setContent(devicesList);
        deviceContentScroll.setContent(devicesContent);
        
        devicesLocationsScrollList.getStyleClass().add("list-view-root");
        devicesListScrollList.getStyleClass().add("list-view-root");
        deviceContentScroll.getStyleClass().addAll("list-view-root", "full-component-large");
        
        deviceLocationsList.getStyleClass().addAll("custom-list-view", "large", "first");
        devicesList.getStyleClass().addAll("custom-list-view", "large", "second");
        
        setupDevicesPane();
       
        this.heightProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
             deviceLocationsList.setMinHeight(newValue.doubleValue());
             deviceLocationsList.setMaxHeight(newValue.doubleValue());
             
             devicesList.setMinHeight(newValue.doubleValue());
             devicesList.setMaxHeight(newValue.doubleValue());
             
             deviceContentScroll.setMinHeight(newValue.doubleValue());
             deviceContentScroll.setMaxHeight(newValue.doubleValue());
        });
        
    }
    
    private void setupDevicesPane(){
        devicesPane.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        devicesPane.setMinWidth(Control.USE_PREF_SIZE);
        devicesPane.setMaxWidth(Control.USE_PREF_SIZE);
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(25);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(25);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(50);
        
        devicesPane.getColumnConstraints().addAll(column1, column2, column3);
        devicesPane.add(deviceLocationsList,0,0);
        devicesPane.add(devicesList,1,0);
        devicesPane.add(devicesContent,2,0);
        
        devicesContent.getStyleClass().addAll("full-device");
                
        
        deviceLocationsList.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty().divide(4));
        devicesList.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty().divide(4));
        devicesContent.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty().divide(2));
        
        deviceLocationsList.setMinWidth(Control.USE_PREF_SIZE);
        devicesList.setMinWidth(Control.USE_PREF_SIZE);
        devicesContent.setMinWidth(Control.USE_PREF_SIZE);
        
        deviceLocationsList.setMaxWidth(Control.USE_PREF_SIZE);
        devicesList.setMaxWidth(Control.USE_PREF_SIZE);
        devicesContent.setMaxWidth(Control.USE_PREF_SIZE);
        
        this.setPrefHeight(Double.MAX_VALUE);
        
        this.getChildren().add(devicesPane);
        
    }
    
    @Override
    public String getTitle() {
        return "Devices";
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
                    if(firstItem==null){
                        firstItem = device.getLocation();
                    }
                    if(!locationList.contains(device.getLocation())){
                        locationList.add(device.getLocation());
                    }
                    if(currentLocationId == device.getDeviceLocationId()){
                        devicesList.getChildren().add(new DeviceListItem(device));
                    }
                }
            }
            if(firstLoad){
                firstLoad = false;
                if(locationList.size()>0){
                    itemClicked(firstItem.getLocationId(), firstItem.getRoomName().getValue());
                }
            }
            Platform.runLater(() -> {
                ObservableList<Node> workingCollection = FXCollections.observableArrayList(
                    devicesList.getChildren()
                );
                Collections.sort(workingCollection, (Node arg0, Node arg1) -> ((DeviceListItem)arg0).getDevice().getDeviceName().compareToIgnoreCase(((DeviceListItem)arg1).getDevice().getDeviceName()));
                devicesList.getChildren().setAll(workingCollection);
            });
        } else if (change.wasRemoved()){
            List<DeviceListItem> toRemove = new ArrayList<>();
            if(change.hasNext()){
                for(Device device:change.getRemoved()){
                    for(Node node:devicesList.getChildren()){
                        DeviceListItem checkDevice = (DeviceListItem)node;
                        if(checkDevice.getDevice()==device){
                            toRemove.add(checkDevice);
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
            while(change.next()){
                if(change.wasAdded()){
                    Platform.runLater(() -> {
                        for(Location location:change.getAddedSubList()){
                            deviceLocationsList.getChildren().add(new DeviceLocationListItem(location.getLocationId(), location.getRoomName().getValue(), DevicesLargeScene.this));
                            if(deviceLocationsList.getChildren().size()==1){
                                this.deviceLocationsList.getChildren().get(0).getStyleClass().add("active");
                            }
                        }
                    });
                }
                if(change.wasRemoved()){
                    DeviceLocationListItem toRemove = null;
                    for(Node node:deviceLocationsList.getChildren()){
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
                            deviceLocationsList.getChildren().remove(delete);
                        });
                    }
                }
            }
            Platform.runLater(() -> {
                ObservableList<Node> workingCollection = FXCollections.observableArrayList(
                    deviceLocationsList.getChildren()
                );
                Collections.sort(workingCollection, (Node arg0, Node arg1) -> ((DeviceLocationListItem)arg0).getLocationName().compareToIgnoreCase(((DeviceLocationListItem)arg1).getLocationName()));
                deviceLocationsList.getChildren().setAll(workingCollection);
            });
        });
    }
    
    @Override
    public void close() {
        devicesPane.prefWidthProperty().unbind();
        deviceLocationsList.prefWidthProperty().unbind();
        devicesList.prefWidthProperty().unbind();
        devicesContent.prefWidthProperty().unbind();
        if(devices!=null){
            devices.removeListener(deviceMutator);
        }
    }

    @Override
    public Pane getPane() {
        return this;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    @Override
    public void removeSystem() {
        this.system = null;
    }

    @Override
    public void itemClicked(Integer item, String itemDescription) {
        currentLocationId = item;
        currentLocationName = itemDescription;
        createDeviceListPane(item);
        ScenesHandler.setSceneTitle("Devices: " + currentLocationName);
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
            Platform.runLater(() -> {
                ObservableList<Node> workingCollection = FXCollections.observableArrayList(
                    this.devicesList.getChildren()
                );
                Collections.sort(workingCollection, (Node arg0, Node arg1) -> ((DeviceListItem)arg0).getDevice().getDeviceName().compareToIgnoreCase(((DeviceListItem)arg1).getDevice().getDeviceName()));
                this.devicesList.getChildren().setAll(workingCollection);
            });
            if(this.devicesList.getChildren().size()>0){
                this.devicesList.getChildren().get(0).getStyleClass().add("active");
                createDeviceDetails(((DeviceListItem)this.devicesList.getChildren().get(0)).getDevice());
            }
        });
    }
    
    private class DeviceLocationListItem extends HBox {
        
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
                for(Node node:deviceLocationsList.getChildren()){
                    node.getStyleClass().remove("active");
                }
                this.getStyleClass().add("active");
                handler.itemClicked(locationId, locationName);
            });
            
        }
     
        @Override
        public final String toString(){
            return this.locationName;
        }
        
        public final String getLocationName(){
            return locationName;
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
                for(Node node:devicesList.getChildren()){
                    node.getStyleClass().remove("active");
                }
                this.getStyleClass().add("active");
                createDeviceDetails(device);
            });
        }
    
        @Override
        public final String toString(){
            return this.device.getDeviceName();
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
                VisualDeviceControlHelper visualControl = new VisualDeviceControlHelper(this.system.getConnection(), control, true, true);
                addList.add(visualControl);
            }
        }
        devicesContent.getChildren().addAll(addList);
        ScenesHandler.setSceneTitle("Devices: " + currentLocationName + ", " + device.getDeviceName());
    }
    
}
