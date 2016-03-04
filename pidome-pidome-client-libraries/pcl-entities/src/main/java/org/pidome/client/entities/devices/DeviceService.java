/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.locations.LocationNotAvailableException;
import org.pidome.client.entities.locations.LocationService;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 * The Device service handles device requests.
 * @author John
 */
public class DeviceService extends Entity implements PCCConnectionNameSpaceRPCListener {

    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    /**
     * The location service for the device locations.
     */
    private LocationService locationService;
    
    /**
     * Constructor.
     * @param connection The system's connection.
     * @param locationService The location service for the device locations.
     */
    public DeviceService(PCCConnectionInterface connection, LocationService locationService){
        this.connection = connection;
        this.locationService = locationService;
    }
    
    /**
     * List of devices.
     */
    private ObservableArrayListBean<Device> devicesList = new ObservableArrayListBean();
    /**
     * Read only list of devices.
     */
    private ReadOnlyObservableArrayListBean<Device> readOnlyDevicesList = new ReadOnlyObservableArrayListBean(devicesList);
    
    static {
        Logger.getLogger(DeviceService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Initializes listeners.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("DeviceService", this);
    }

    /**
     * Clean up.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("DeviceService", this);
        connection = null;
        locationService = null;
        devicesList.clear();
    }
    
    /**
     * Preloads the Device service.
     * @throws EntityNotAvailableException When the entity fails to preload.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialDeviceList();
            } catch (DeviceServiceException ex) {
                throw new EntityNotAvailableException("Could not preload devices list", ex);
            }
        }
    }

    /**
     * Preloads the Device service.
     * @throws EntityNotAvailableException When the entity fails to preload.
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        devicesList.clear();
        preload();
    }
    
    /**
     * Returns the device's list.
     * If the list is empty it will be populated in the background.
     * @return A read only list of devices.
     */
    public final ReadOnlyObservableArrayListBean<Device> getDevices() {
        return this.readOnlyDevicesList;
    }
    
    /**
     * Loads the initial devices list
     * @throws DeviceServiceException 
     */
    private void loadInitialDeviceList() throws DeviceServiceException {
        if(devicesList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("DeviceService.getActiveDevices", null, "DeviceService.getActiveDevices"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new DeviceServiceException("Problem getting devices", ex);
            }
        }
    }
    
    /**
     * Updates a device.
     * @param rpcDataHandler The PCCEntityDataHandler.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        Map<String,Object> broadcastParameters = rpcDataHandler.getParameters();
        switch(rpcDataHandler.getMethod()){
            case "updateDevice":
                try {
                    Map<String,Object> deviceData = new HashMap<>();
                    deviceData.put("id", ((Number)broadcastParameters.get("id")).intValue());
                    updateDevice(this.connection.getJsonHTTPRPC("DeviceService.getDevice", deviceData, "DeviceService.getDevice"));
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, "Problem updating device", ex);
                }
            break;
            case "deleteDevice":
                int deviceId = ((Number)broadcastParameters.get("id")).intValue();
                Device toRemove = null;
                for(Device device:devicesList){
                    if(device.getDeviceId()==deviceId){
                        toRemove = device;
                        break;
                    }
                }
                if(toRemove!=null){
                    this.devicesList.remove(toRemove);
                }
            break;
            case "addDevice":
                try {
                    Map<String,Object> deviceData = new HashMap<>();
                    deviceData.put("id", ((Number)broadcastParameters.get("id")).intValue());
                    devicesList.add(createDeviceByResult(this.connection.getJsonHTTPRPC("DeviceService.getDevice", deviceData, "DeviceService.getDevice")));
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, "Problem updating device", ex);
                }
            break;
            case "sendDevice":
                updateDeviceData(broadcastParameters);
            break;
        }
    }

    /**
     * Returns a single device.
     * @param deviceId the id of the device to retrieve.
     * @return The device object.
     * @throws org.pidome.client.entities.devices.UnknownDeviceException When the device can not be found.
     */
    public final Device getDevice(int deviceId) throws UnknownDeviceException {
        for(Device device:this.devicesList){
            if(device.getDeviceId() == deviceId){
                return device;
            }
        }
        return loadSingleDevice(deviceId);
    }
    
    /**
     * Loads and returns a single device.
     * This function is internal only and called when a specific device does not exist.
     * This is a blocking function!
     * @param deviceId
     * @return
     * @throws UnknownDeviceException 
     */
    private Device loadSingleDevice(int deviceId) throws UnknownDeviceException{
        try {
            Map<String,Object> deviceData = new HashMap<>();
            deviceData.put("id", deviceId);
            Device device = createDeviceByResult(this.connection.getJsonHTTPRPC("DeviceService.getDevice", deviceData, "DeviceService.getDevice"));
            devicesList.add(device);
            return device;
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, "Device with id "+deviceId+" not found");
            throw new UnknownDeviceException("Device with id " + deviceId + " not found");
        }
    }
    
    /**
     * Updates device data.
     * @param broadcastParameters Map containing device structure and data.
     */
    private void updateDeviceData(Map<String,Object> broadcastParameters){
        try {
            Device device = getDevice(((Number)broadcastParameters.get("id")).intValue());
            device.updateGroupValues((List<Map<String,Object>>)broadcastParameters.get("groups"));
        } catch (UnknownDeviceException ex) {
            try {
                loadSingleDevice(((Number)broadcastParameters.get("id")).intValue());
            } catch (UnknownDeviceException ex1) {
                Logger.getLogger(DeviceService.class.getName()).log(Level.WARNING, "Device with id "+broadcastParameters.get("id")+" not found");
            }
        }
    }
    
    /**
     * Handles commands for results.
     * @param rpcDataHandler The PCCEntityDataHandler.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        String resultId = (String)rpcDataHandler.getId();
        Runnable run = () -> {
            switch(resultId){
                case "DeviceService.getActiveDevices":
                    composeInitialDevices(data);
                break;
            }
        };
        run.run();
    }
    
    /**
     * Handles a device update.
     * @param handler 
     */
    private void updateDevice(PCCEntityDataHandler handler){
        Map<String,Object> result = handler.getResult();
        int deviceId = ((Number)result.get("id")).intValue();
        for(Device device:devicesList){
            if(device.getDeviceId()==deviceId){
                device.setName((String)result.get("name"));
                device.setDeviceAddress((String)result.get("address"));
                device.setDeviceActive((boolean)result.get("active"));
                device.setDeviceIsFavorite((boolean)result.get("favorite"));
                device.setDeviceSubCategoryId((int)result.get("category"));
                try {
                    device.setLocation(locationService.getLocation(((Number)result.get("location")).intValue()));
                } catch (LocationNotAvailableException ex) {
                    Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
        }
    }
    
    /**
     * Returns a list of installed devices based on a live running driver.
     * @param port The port of the driver.
     * @return List of Installed devices.
     */
    public final List<InstalledDevice> getInstalledDevicesByLiveDriver(String port){
        List<InstalledDevice> list = new ArrayList<>();
        Map<String,Object> params = new HashMap<>();
        params.put("peripheralport", port);
        try {
            List<Map<String,Object>> dataSet = (List<Map<String,Object>>)this.connection.getJsonHTTPRPC("DeviceService.getPeripheralDeclaredDevices", params, "DeviceService.getPeripheralDeclaredDevices").getResult().get("data");
            for(Map<String,Object> data:dataSet){
                list.add(new InstalledDevice(((Number)data.get("id")).intValue(), (String)data.get("name")));
            }
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    /**
     * Composes the initial devices list.
     * @param data 
     */
    private void composeInitialDevices(ArrayList<Map<String,Object>> data){
        try {
            List<Device> devices = new ArrayList<>();
            for(Map<String,Object> deviceData: data){
                devices.add(createDevice(deviceData));
            }
            devicesList.addAll(devices);
        } catch (Exception ex){
            Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, "Problem creating device list", ex);
        }
    }
    
    /**
     * Adds a device from a direct result.
     * @param rpcDataHandler 
     */
    private Device createDevice(PCCEntityDataHandler rpcDataHandler){
        return createDevice(rpcDataHandler.getParameters());
    }
    
    /**
     * Adds a device from a direct result.
     * @param rpcDataHandler 
     */
    private Device createDeviceByResult(PCCEntityDataHandler rpcDataHandler){
        return createDevice((Map<String,Object>)rpcDataHandler.getResult().get("data"));
    }
    
    /**
     * Adds a single device to the list.
     * @param deviceData 
     */
    private Device createDevice(Map<String,Object> deviceData){
        Device device = new Device(this.connection, ((Number)deviceData.get("id")).intValue());
        device.setName((String)deviceData.get("name"));
        device.setDeviceTypeName((String)deviceData.get("friendlyname"));
        device.setDeviceAddress((String)deviceData.get("address"));
        device.setDeviceActive((boolean)deviceData.get("active"));
        device.setDeviceIsFavorite((boolean)deviceData.get("favorite"));
        device.setDeviceSubCategoryId(((Long)deviceData.get("category")).intValue());
        device.composeDevice((List<Map<String,Object>>)deviceData.get("commandgroups"));
        try {
            device.setLocation(locationService.getLocation(((Number)deviceData.get("location")).intValue()));
        } catch (LocationNotAvailableException ex) {
            Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return device;
    }

    @Override
    public void unloadContent() throws EntityNotAvailableException {
        devicesList.clear();
    }
    
}
