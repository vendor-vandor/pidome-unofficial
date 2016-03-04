/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.devicediscovery;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.categories.Category;
import org.pidome.client.entities.devicediscovery.DiscoveredDevice;
import org.pidome.client.entities.devices.InstalledDevice;
import org.pidome.client.entities.locations.Location;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class AddDiscoveredDevicePopup extends PopUp {
 
    private VBox container = new VBox(10);
    
    TextField addressField;
    
    private ObservableList<Label> categories = FXCollections.observableArrayList();
    private ComboBox categoriesBox;
    
    private ObservableList<Label> devices = FXCollections.observableArrayList();
    private ComboBox devicesBox;
    
    TextField deviceNameTextField;
    
    private ObservableList<Label> locations = FXCollections.observableArrayList();
    private ComboBox locationsBox;
    
    GridPane form = new GridPane();
    
    private double commonControlWidth = 600;
    
    protected AddDiscoveredDevicePopup(){
        super(MaterialDesignIcon.PLUS_NETWORK, "Discovered device");
        container.setMinWidth(900);
        container.setPadding(new Insets(5));
        container.getStyleClass().add("discovered-device-popup-add");
        form.setVgap(5);
        form.setHgap(5);
        form.setAlignment(Pos.CENTER);
        VBox.setMargin(form, new Insets(5,0,5,0));
    }

    protected final void compose(DiscoveredDevice device){
        switch(device.getDeviceFunctionType()){
            case FUNCTION_REQUEST_ADDRESS:
                buildAddressRequest(device);
            break;
            case FUNCTION_ADD_DEVICE:
                buildAddDevice(device);
            break;
        }
        
        Label headerSubTitle = new Label("Device gathered information");
        headerSubTitle.getStyleClass().add("title");
        
        GridPane info = new GridPane();
        int row = 0;
        for(Map.Entry<String,Object> item:device.getDeviceInfo().entrySet()){
            Text name = new Text(item.getKey());
            name.getStyleClass().addAll("text", "name");
            Text value= new Text(": " + String.valueOf(item.getValue()));
            value.getStyleClass().addAll("text", "value");
            info.add(name, 0, row);
            info.add(value, 1, row);
            row++;
        }
        container.getChildren().addAll(form, headerSubTitle, info);
        setContent(container);
    }
    
    protected final String getNewAddress(){
        return this.addressField.getText();
    }
    
    protected final String getNewDeviceName(){
        return this.deviceNameTextField.getText();
    }
    
    protected final int getNewDeviceId(){
        return (int)((Label)devicesBox.getSelectionModel().getSelectedItem()).getUserData();
    }
    
    protected final int getNewCategory(){
        return (int)((Label)categoriesBox.getSelectionModel().getSelectedItem()).getUserData();
    }
    
    protected final int getNewLocation(){
        return (int)((Label)locationsBox.getSelectionModel().getSelectedItem()).getUserData();
    }
    
    protected final void setCategories(ReadOnlyObservableArrayListBean<Category> cats){
        Iterator<Category> iter = cats.iterator();
        while(iter.hasNext()){
            Category cat = iter.next();
            Label data = new Label(cat.getName().getValue());
            data.setUserData(cat.getCategoryId());
            categories.add(data);
        }
    }
    
    protected final void setLocations(ReadOnlyObservableArrayListBean<Location> locs){
        Iterator<Location> iter = locs.iterator();
        while(iter.hasNext()){
            Location loc = iter.next();
            Label data = new Label(loc.getCombinedName().getValue());
            data.setUserData(loc.getLocationId());
            locations.add(data);
        }
    }
    
    protected final void setInstalledDevices(List<InstalledDevice> devices){
        Iterator<InstalledDevice> iter = devices.iterator();
        while(iter.hasNext()){
            InstalledDevice device = iter.next();
            Label data = new Label(device.getName());
            data.setUserData(device.getId());
            this.devices.add(data);
        }
    }
    
    private void buildAddressRequest(DiscoveredDevice device){
        Label headerTitle = new Label("Device add information");
        headerTitle.getStyleClass().add("title");
        
        Text infoContent = new Text(device.getInfoDescription());
        infoContent.getStyleClass().add("text");
        infoContent.setWrappingWidth(container.getMinWidth()-10);
        
        container.getChildren().addAll(headerTitle, infoContent);
        
        Label setAddress = new Label("Select/Set address");
        setAddress.getStyleClass().add("title");
        addressField = new TextField(device.getProposedAddress());
        addressField.setMinWidth(commonControlWidth);
        
        form.add(setAddress, 0,0);
        form.add(addressField, 1,0);
        
    }
    
    private void buildAddDevice(DiscoveredDevice device){
        
        Label selectdevice = new Label("Select device");
        selectdevice.getStyleClass().add("title");
        
        devicesBox = new ComboBox(devices);
        devicesBox.setMinWidth(commonControlWidth);
        
        Label setDeviceName = new Label("Set device name");
        setDeviceName.getStyleClass().add("title");
        this.deviceNameTextField = new TextField(device.getProposedAddress());
        
        Label selectloc = new Label("Select location");
        selectloc.getStyleClass().add("title");
        locationsBox = new ComboBox(locations);
        locationsBox.setMinWidth(commonControlWidth);
        
        Label selectcat = new Label("Select category");
        selectcat.getStyleClass().add("title");
        
        categoriesBox = new ComboBox(categories);
        categoriesBox.setMinWidth(commonControlWidth);
        
        form.add(selectdevice, 0,0);
        form.add(devicesBox, 1,0);
        
        form.add(setDeviceName, 0,1);
        form.add(deviceNameTextField, 1,1);
        
        form.add(selectloc, 0,2);
        form.add(locationsBox, 1,2);
        
        form.add(selectcat, 0,3);
        form.add(categoriesBox, 1,3);
        
    }
    
    @Override
    public void unload() {
        ///
    }
    
}