/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 * The main class holding all the devices data.
 * @author John Sirach
 */
public class Devices implements DomComponent,ClientDataConnectionListener {

    static Map<Integer, Device> deviceCollection = new HashMap<>();
    static Map<Integer, Map<String,String>> deviceCategories = new HashMap<>();
    
    ObservableMap<Integer,Device> observableDevices    = FXCollections.observableMap(deviceCollection);
    ObservableMap<Integer, Map<String,String>> observableCategories = FXCollections.observableMap(deviceCategories);
    
    static List<DevicesEventListener> _deviceListeners = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(Devices.class);
    
    /**
     * Constructor.
     */
    public Devices(){
        observableDevices.addListener((MapChangeListener.Change<? extends Integer, ? extends Device> change) -> {
            if (change.wasRemoved() && change.wasAdded()){
                _fireDeviceChangeEvent(DevicesEvent.DEVICEUPDATED, change.getValueAdded());
            } else if (change.wasRemoved()){
                _fireDeviceChangeEvent(DevicesEvent.DEVICEREMOVED, change.getValueRemoved());
            } else if(change.wasAdded()){
                _fireDeviceChangeEvent(DevicesEvent.DEVICEADDED, change.getValueAdded());
            }
        });    
    }
    
    /**
     * Returns the devices collection as list.
     * @return 
     */
    public static List<Device>getCollectionAsList(){
        List<Device> list = new ArrayList();
        deviceCollection.values().stream().forEach((device) -> {
            list.add(device);
        });
        return list;
    }
    
    /**
     * Removes a device.
     * @param deviceId 
     */
    public final void removeDeviceById(int deviceId){
        if(deviceCollection.containsKey(deviceId)){
            deviceCollection.get(deviceId).handleShortCut(true);
            observableDevices.remove(deviceId);
        }
    }
    
    /**
     * Returns a device by it's id.
     * @param deviceId
     * @return
     * @throws DomComponentsException 
     */
    public static Device getDeviceById(int deviceId) throws DomComponentsException {
        if(deviceCollection.containsKey(deviceId)){
            return deviceCollection.get(deviceId);
        } else {
            throw new DomComponentsException("Unknown device, id " + deviceId);
        }
    }
    
    /**
     * Creates a device.
     * @param deviceDetails
     * @throws DomComponentsException 
     */
    public final void createDevice(Map<String,Object> deviceDetails) throws DomComponentsException {
        observableDevices.put(((Long)deviceDetails.get("id")).intValue(), new Device(deviceDetails));
    } 

    /**
     * Updates a device.
     * @param deviceDetails 
     */
    public final void updateDevice(Map<String,Object> deviceDetails){
        LOG.debug("Updating id: {}", deviceDetails.get("id"));
        if(deviceCollection.containsKey(((Long)deviceDetails.get("id")).intValue())){
            deviceCollection.get(((Long)deviceDetails.get("id")).intValue()).updateDetails(deviceDetails);
            _fireDeviceChangeEvent(DevicesEvent.DEVICEUPDATED, deviceCollection.get(((Long)deviceDetails.get("id")).intValue()));
        }
    }
    
    public final void startEvents(){
        ClientData.addClientDataConnectionListener(this);
        LOG.debug("Started dispatcher");
    }

    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.DEVRECEIVED)){
            Map<String,Object> dataSet = (Map<String,Object>)event.getData();
            LOG.debug("Got data: {}", dataSet);
            switch(event.getMethod()){
                case "sendDevice":
                    try {
                        int devId = ((Long)dataSet.get("id")).intValue();
                        if(deviceCollection.containsKey(devId)){
                            
                            ArrayList<Map<String,Object>> paramSet = (ArrayList<Map<String,Object>>)dataSet.get("groups");
                            for (Map<String, Object> group : paramSet) {
                                String groupId = (String)group.get("groupid");
                                Map<String,Object> controlSet = (Map<String,Object>)group.get("controls");
                                for(Map.Entry<String,Object> entry:controlSet.entrySet()){
                                    deviceCollection.get(devId).setServerCmd(groupId, entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    } catch (NullPointerException e){
                        LOG.error("Faulty Device data: {}", event.getData(), e);
                    }
                break;
                case "setFavorite":
                    try {
                        int devId = ((Long)dataSet.get("id")).intValue();
                        if(deviceCollection.containsKey(devId)){
                            deviceCollection.get(devId).updateFavorite((boolean)dataSet.get("favorite"));
                        }
                    } catch (NullPointerException e){
                        LOG.error("Faulty Device data: {}", event.getData());
                    }                    
                break;
            }
        }
    }
 
    public static synchronized void addDevicesEventListener(DevicesEventListener l){
        LOG.debug("Added device event listener {}", l.getClass().getName());
        _deviceListeners.add(l);
    }

    public static synchronized void removeDevicesEventListener(DevicesEventListener l){
        LOG.debug("Removed device event listener {}", l.getClass().getName());
        _deviceListeners.remove(l);
    }
    
    private synchronized void _fireDeviceChangeEvent(String EVENTTYPE, Device device) {
        LOG.debug("Event: {}", EVENTTYPE);
        DevicesEvent event = new DevicesEvent(device, EVENTTYPE);
        Iterator listeners = _deviceListeners.iterator();
        while (listeners.hasNext()) {
            ((DevicesEventListener) listeners.next()).handleDevicesEvent(event);
        }
    }
    
    
    
}
