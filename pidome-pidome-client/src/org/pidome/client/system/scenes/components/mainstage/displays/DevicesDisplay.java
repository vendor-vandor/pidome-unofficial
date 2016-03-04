/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.domotics.components.devices.DevicesEvent;
import org.pidome.client.system.domotics.components.devices.DevicesEventListener;
import org.pidome.client.system.domotics.components.locations.Locations;
import org.pidome.client.system.scenes.components.mainstage.displays.components.DeviceContentPane;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.SliderPosition;
import org.pidome.client.system.scenes.components.mainstage.displays.controls.DeviceComponents;
import org.pidome.client.system.scenes.windows.TitledWindow;


/**
 *
 * @author John Sirach
 */
public final class DevicesDisplay extends TitledWindow implements DevicesEventListener {

    static Logger LOG = LogManager.getLogger(DevicesDisplay.class);
    
    List<Device> devices = new ArrayList();

    List<DeviceComponents> deviceList = new ArrayList();
    
    double componentHeight = 40*DisplayConfig.getHeightRatio();
    int itemsPerRow = 4;
    
    FilteredList list = new FilteredList("Location", SliderPosition.LEFT);
    StackPane devicesContent = new StackPane();
    
    ChangeListener filterUpdater = this::filteredListAmountHelper;
    
    HBox skeleton = new HBox(0);
    
    public DevicesDisplay(Object... numbs) throws Exception {
        this();
    }
    
    public DevicesDisplay(){
        super("deviceslist", "Devices list");
        setToolbarText("devices");
        list.setListSize(700, 540);
        list.isCategorized(true);
        list.highlightOnSelect(true);
        list.build();
        getStyleClass().add("deviceslist");
        skeleton.getChildren().addAll(list, devicesContent);
    }
    
    final void createDevicesRaster(){
        if(devices.isEmpty()){
            devices = Devices.getCollectionAsList();
        }
        devices.stream().forEach((device) -> {
            addDevice(device);
        });
        setContent(skeleton);
    }

    public final void setDeviceContent(DeviceContentPane content){
        if(devicesContent.getChildren().size()>0){
            DeviceContentPane node = (DeviceContentPane)devicesContent.getChildren().get(0);
            node.removeContent();
            devicesContent.getChildren().remove(node);
        }
        content.setupContent();
        devicesContent.getChildren().add(content);
    }
    
    public final void addDevice(Device device){
        if(device.getId() != 1){
            LOG.debug("Adding device: {}", device.getName());
            if (!devices.contains(device)) { devices.add(device); }
            list.addItem(createDeviceListItem(device));
            setToolbarText(list.getFilteredAmountProperty().get() + " devices of "+list.getItemsAmount()+" total");
        }
    }
    
    final FilteredListItem createDeviceListItem(Device device){
        FilteredListItem item;
        try {
            item = new FilteredListItem(String.valueOf(device.getId()), device.getName(), "Location", Locations.getLocation(device.getLocation()));
        } catch (DomComponentsException ex) {
            item = new FilteredListItem(String.valueOf(device.getId()), device.getName(), "Location", "Unknown");
        }
        item.addCategory("Category", Categories.getCategoryName(device.getCategory()));
        item.setContent(getDeviceComponents(device));
        return item;
    }
    
    public final void removeDevice(DeviceComponents obj, boolean updateRemove){
        if(updateRemove==false){
            devices.remove(obj.getDevice());
            obj.removeListeners();
            list.removeItem(String.valueOf(obj.getDevice().getId()));
        }
        setToolbarText(list.getFilteredAmountProperty().get() + " devices of "+list.getItemsAmount()+" total");
    }
    
    final HBox getDeviceComponents(Device device){
        DeviceComponents deviceComponents = new DeviceComponents(this, device);
        deviceComponents.onlyShortcuts(true);
        deviceComponents.create();
        deviceComponents.setPrefWidth(700*DisplayConfig.getWidthRatio());
        deviceComponents.setMinWidth(Region.USE_PREF_SIZE);
        deviceComponents.setMaxWidth(Region.USE_PREF_SIZE);
        deviceList.add(deviceComponents);
        return deviceComponents;
    }
    
    public final synchronized void updateDevice(final Device device) {
        Platform.runLater(() -> {
            for (final DeviceComponents obj : deviceList) {
                if (obj.getDeviceId() == device.getId()) {
                    list.highlightRow(String.valueOf(device.getId()));
                    if (!obj.getLabel().equals(device.getName())) {
                        obj.updateLabel(device.getName());
                    }
                    if (obj.getCategory() != device.getCategory()) {
                        list.removeItem(String.valueOf(device.getId()));
                        removeDevice(obj, false);
                        list.addItem(createDeviceListItem(device));
                    }
                }
            }
        });
    }
    
    @Override
    public void handleDevicesEvent(final DevicesEvent event) {
        switch (event.getEventType()) {
            case DevicesEvent.DEVICEADDED:
                Platform.runLater(() -> {
                    addDevice(event.getSource());
                });
            break;
            case DevicesEvent.DEVICEREMOVED:
                for (final DeviceComponents obj : deviceList) {
                    if (obj.getDeviceId() == event.getSource().getId()) {
                        Platform.runLater(() -> {
                            removeDevice(obj, false);
                        });
                    }
                }
            break;
            case DevicesEvent.DEVICEUPDATED:
                updateDevice(event.getSource());
            break;
        }
    }
    
    final void filteredListAmountHelper(ObservableValue ov, Object t, Object t1){
        setToolbarText(t1 + " devices of "+list.getItemsAmount()+" total");
    }
    
    @Override
    public final void setupContent(){
        createDevicesRaster();
        Devices.addDevicesEventListener(this);
        list.getFilteredAmountProperty().addListener(filterUpdater);
    }

    @Override
    public void removeContent() {
        list.getFilteredAmountProperty().removeListener(filterUpdater);
        if(devicesContent.getChildren().size()>0){
            DeviceContentPane node = (DeviceContentPane)devicesContent.getChildren().get(0);
            node.removeContent();
        }
        Devices.removeDevicesEventListener(this);
        deviceList.stream().forEach((deviceComp) -> {
            removeDevice(deviceComp, false);
        });
        list.destroy();
        deviceList.clear();
        devices.clear();
    }
    
}
