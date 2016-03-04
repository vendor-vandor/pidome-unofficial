/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DeviceControl;
import org.pidome.client.entities.devices.DeviceControlGroup;
import org.pidome.client.entities.devices.DeviceDataControl;
import org.pidome.client.photoframe.FrameSettings;
import org.pidome.client.photoframe.ScreenDisplay;
import org.pidome.client.photoframe.utils.TimeTempShadedLabel;
import org.pidome.client.system.PCCClient;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener.Change;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class RoomTemperatureActor extends Table {
    
    private final PCCClient client;
    
    private final TimeTempShadedLabel temp = new TimeTempShadedLabel("0 °C/F");
    
    int deviceId = 0;
    String deviceGroupId = "";
    String deviceControlId = "";
    
    DeviceDataControl controlToListen;
    
    ReadOnlyObservableArrayListBean<Device> devices;
    
    ObservableArrayListBeanChangeListener devicesListener = this::deviceListChangeListenerHelper;
    
    PropertyChangeListener dataHelper = this::dataUpdateHelper;
    
    public RoomTemperatureActor(PCCClient client) {
        this.client = client;
        try {
            String[] devicePref = FrameSettings.getRoomDevice();
            deviceId = Integer.parseInt(devicePref[0]);
            deviceGroupId = devicePref[1];
            deviceControlId = devicePref[2];
        } catch (Exception ex) {
            deviceId = 0;
            Logger.getLogger(RoomTemperatureActor.class.getName()).log(Level.SEVERE, "Could not get room temperature device", ex);
        }
        if(deviceId!=0){
            try {
                devices = this.client.getEntities().getDeviceService().getDevices();
                devices.addListener(devicesListener);
                this.client.getEntities().getDeviceService().preload();
            } catch (EntityNotAvailableException ex) {
                Logger.getLogger(RoomTemperatureActor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Creates the table.
     */
    public final void populate(){
        Texture tex = new Texture(Gdx.files.internal("resources/appimages/bars/top/home-icon.png"));
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion texture = new TextureRegion(tex);
        Image icon = new Image(new SpriteDrawable(new Sprite(texture)));
        add(icon).center().size(34 * ScreenDisplay.getCurrentScale(),34 * ScreenDisplay.getCurrentScale());
        add(temp).center().padRight(10 * ScreenDisplay.getCurrentScale());
    }
    
    /**
     * Handles changes in device listings.
     * @param deviceChange 
     */
    public void deviceListChangeListenerHelper(Change<Device> deviceChange){
        if(deviceChange.wasAdded()){
            while(deviceChange.next()){
                List<Device> deviceList = (List<Device>)deviceChange.getAddedSubList();
                for(Device device:deviceList){
                    if(device.getDeviceId()==deviceId){
                        for(DeviceControlGroup group: device.getControlGroups()){
                            if(group.getGroupId().equals(deviceGroupId)){
                                for(DeviceControl control:group.getGroupControls()){
                                    if(control.getControlId().equals(deviceControlId) && control instanceof DeviceDataControl){
                                        controlToListen = (DeviceDataControl)control;
                                        updateText();
                                        controlToListen.getValueProperty().addPropertyChangeListener(dataHelper);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if(deviceChange.wasRemoved()){
            while(deviceChange.next()){
                List<Device> deviceList = (List<Device>)deviceChange.getRemoved();
                for(Device device:deviceList){
                    if(device.getDeviceId()==deviceId && controlToListen!=null){
                        controlToListen.getValueProperty().removePropertyChangeListener(dataHelper);
                        temp.setText("0 °C/F");
                    }
                }
            }
        }
    }
    
    public final void dataUpdateHelper(PropertyChangeEvent pce){
        updateText();
    }
    
    private void updateText(){
        temp.setText(new StringBuilder(controlToListen.getPrefix()).append(" ").append(controlToListen.getValue()).append(" ").append(controlToListen.getSuffix()).toString());
    }
    
}