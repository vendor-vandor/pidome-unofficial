/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.devices;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlGroup;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.scenes.visuals.SceneBackHandler;
import org.pidome.client.phone.visuals.controls.devices.controls.VisualDeviceControlHelper;
import org.pidome.client.phone.visuals.interfaces.Destroyable;
import org.pidome.client.phone.visuals.lists.ListClickedHandler;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class DevicesLocationList extends BaseScene implements ListClickedHandler<Integer>,SceneBackHandler {

    private GridPane devicesPane = new GridPane();
    VBox deviceLocationsList = new VBox();
    VBox devicesList = new VBox();
    VBox devicesContent = new VBox();    
    
    ReadOnlyObservableArrayListBean<Device> devices;
    
    private ObservableMap<Integer,String> locationList = FXCollections.observableHashMap();
    private ObservableArrayListBeanChangeListener<Device> deviceMutator = this::deviceMutator;
    
    private int currentLocationId = 0;
    
    private String currentLocationName = "";
    
    public DevicesLocationList() {
        super(true);
        deviceLocationsList.getStyleClass().add("custom-list-view");
        devicesList.getStyleClass().add("custom-list-view");
        setupDevicesPane();
        setDefaultSceneTitle();
        getContentPane().setHmax(0.1);
    }

    private void setupDevicesPane(){
        devicesPane.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()*3);
        devicesPane.setMinWidth(Control.USE_PREF_SIZE);
        devicesPane.setMaxWidth(Control.USE_PREF_SIZE);
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(33.3);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(33.3);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(33.3);
        devicesPane.getColumnConstraints().addAll(column1, column2, column3);
        devicesPane.add(deviceLocationsList,0,0);
        devicesPane.add(devicesList,1,0);
        devicesPane.add(devicesContent,2,0);
        
        devicesContent.getStyleClass().add("full-device");
        
        deviceLocationsList.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        devicesList.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        devicesContent.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        
        deviceLocationsList.setMinWidth(Control.USE_PREF_SIZE);
        devicesList.setMinWidth(Control.USE_PREF_SIZE);
        devicesContent.setMinWidth(Control.USE_PREF_SIZE);
        
        deviceLocationsList.setMaxWidth(Control.USE_PREF_SIZE);
        devicesList.setMaxWidth(Control.USE_PREF_SIZE);
        devicesContent.setMaxWidth(Control.USE_PREF_SIZE);
        
    }
    
    private void setDefaultSceneTitle(){
        setSceneTitle("Devices");
    }
    
    @Override
    public void run() {
        this.setContent(devicesPane);
        try {
            devices = getSystem().getClient().getEntities().getDeviceService().getDevices();
            createGoodListFromDevicesCollection();
            devices.addListener(deviceMutator);
            getSystem().getClient().getEntities().getDeviceService().reload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DevicesLocationList.class.getName()).log(Level.SEVERE, null, ex);
        }
        //getContentPane().setHmax(.3);
    }

    private void deviceMutator(ObservableArrayListBeanChangeListener.Change<? extends Device> change) {
        if(change.wasAdded()){
            if(hasSystem()){
                if(change.hasNext()){
                    for(Device device:change.getAddedSubList()){
                        if(!locationList.containsKey(device.getDeviceLocationId())){
                            locationList.put(device.getDeviceLocationId(), device.getTemporaryLocationName());
                        }
                        if(currentLocationId == device.getDeviceLocationId()){
                            devicesList.getChildren().add(new DeviceListItem(device));
                        }
                    }
                }
            }
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
        locationList.addListener((MapChangeListener.Change<? extends Integer,? extends String> change) -> {
            if(change.wasAdded()){
                Platform.runLater(() -> { 
                    deviceLocationsList.getChildren().add(new DeviceLocationListItem(change.getKey(), change.getValueAdded(), DevicesLocationList.this));
                });
            }
            if(change.wasRemoved()){
                DeviceLocationListItem toRemove = null;
                for(Node node:deviceLocationsList.getChildren()){
                    DeviceLocationListItem check = (DeviceLocationListItem) node;
                    if(change.getKey()==check.getLocationId()){
                        toRemove = check;
                        break;
                    }
                }
                if(toRemove!=null){
                    final DeviceLocationListItem delete = toRemove;
                    Platform.runLater(() -> { 
                        deviceLocationsList.getChildren().remove(delete);
                    });
                }
            }
        });
    }
    
    @Override
    public void stop() {
        if(devices!=null){
            devices.removeListener(deviceMutator);
        }
    }

    @Override
    public void itemClicked(Integer locationId, String description) {
        currentLocationId = locationId;
        currentLocationName = description;
        createDeviceListPane(locationId);
        getSceneHeader().setSceneBackTitle(this, "location", description);
    }
    
    private void moveToLocations() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        final KeyValue kv = new KeyValue(this.getContentPane().hvalueProperty(), 0.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished((EventHandler)(Event event) -> {
            Platform.runLater(() -> {
                devicesList.getChildren().clear();
            });
        });
        timeline.play();
    }

    private void moveToDeviceList() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        final KeyValue kv = new KeyValue(this.getContentPane().hvalueProperty(), 0.05);
        final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
        getContentPane().setVvalue(0.0);
    }
    
    private void moveToDevice() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        final KeyValue kv = new KeyValue(this.getContentPane().hvalueProperty(), 0.1);
        final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
        getContentPane().setVvalue(0.0);
    }
    
    @Override
    public void handleSceneBack(String id) {
        switch(id){
            case "location":
                setDefaultSceneTitle();
                moveToLocations();
            break;
            case "devicelist":
                getSceneHeader().setSceneBackTitle(this, "location", currentLocationName);
                moveToDeviceList();
                for(Node node:devicesContent.getChildren()){
                    if(node instanceof VisualDeviceControlHelper){
                        ((VisualDeviceControlHelper)node).destroy();
                    }
                }
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
            Text pointer = GlyphsDude.createIcon(FontAwesomeIcons.ANGLE_RIGHT, "1.4em;");
            getChildren().addAll(nameHolder, pointer);
            
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                handler.itemClicked(locationId, locationName);
            });
            
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
            Text pointer = GlyphsDude.createIcon(FontAwesomeIcons.ANGLE_RIGHT, "1.4em;");
            
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
                VisualDeviceControlHelper visualControl = new VisualDeviceControlHelper(this, getSystem().getConnection(), control, true);
                addList.add(visualControl);
            }
        }
        devicesContent.getChildren().addAll(addList);
        moveToDevice();
        getSceneHeader().setSceneBackTitle(this, "devicelist", device.getDeviceName());
    }
    
}