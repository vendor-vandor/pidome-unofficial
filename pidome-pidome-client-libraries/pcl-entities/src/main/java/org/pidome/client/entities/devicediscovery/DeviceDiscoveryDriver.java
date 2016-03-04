/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devicediscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService.DiscoveryMethod;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService.DiscoveryType;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class DeviceDiscoveryDriver {
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Name of the driver.
     */
    private final String driverName;
    
    /**
     * bindable property containing the driver being active or not in device discovery.
     */
    private final BooleanPropertyBindingBean active = new BooleanPropertyBindingBean(false);
    
    /**
     * Current discovery type.
     */
    private DeviceDiscoveryService.DiscoveryType discoveryType = DiscoveryType.NONE;
    
    /**
     * Supported discovery methods.
     */
    private List<DeviceDiscoveryService.DiscoveryMethod> discoveryMethods = new ArrayList<>();
    
    /**
     * The port this driver is connected to.
     */
    private final String port;
    
    /**
     * List of found devices.
     */
    private final ObservableArrayListBean<DiscoveredDevice> discoveredDevicesList = new ObservableArrayListBean();
    /**
     * Read only list of found devices.
     */
    private final ReadOnlyObservableArrayListBean<DiscoveredDevice> readOnlyDiscoveredDevicesList = new ReadOnlyObservableArrayListBean(discoveredDevicesList);
    
    /**
     * Constructor.
     * @param data Map with guaranteed data from RPC call
     * @param connection The server connection.
     */
    protected DeviceDiscoveryDriver(Map<String,Object> data, PCCConnectionInterface connection){
        this.connection = connection;
        this.driverName = (String)data.get("name");
        switch(((Number)data.get("timer")).intValue()){
            case 1:
                discoveryType = DiscoveryType.MINUTE_1;
            break;
            case 5:
                discoveryType = DiscoveryType.MINUTE_5;
            break;
            case 10:
                discoveryType = DiscoveryType.MINUTE_10;
            break;
            case 30:
                discoveryType = DiscoveryType.MINUTE_30;
            break;
            case -1:
                discoveryType = DiscoveryType.INDEFINITELY;
            break;
            case -2:
                discoveryType = DiscoveryType.SINGLE_DEVICE;
            break;
            default:
                discoveryType = DiscoveryType.NONE;
            break;
        }
        this.port = (String)data.get("port");
        this.active.setValue((boolean)data.get("active"));
        if(data.containsKey("discovertypes")){
            if(((Map<String,Object>)data.get("discovertypes")).containsKey("discovery") && (boolean)((Map<String,Object>)data.get("discovertypes")).get("discovery") == true){
                discoveryMethods.add(DeviceDiscoveryService.DiscoveryMethod.DISCOVERY);
            }
            if(((Map<String,Object>)data.get("discovertypes")).containsKey("scan") && (boolean)((Map<String,Object>)data.get("discovertypes")).get("scan") == true){
                discoveryMethods.add(DeviceDiscoveryService.DiscoveryMethod.SCAN);
            }
            if(discoveryMethods.isEmpty()){
                discoveryMethods.add(DeviceDiscoveryService.DiscoveryMethod.NONE);
            }
        } else {
            discoveryMethods.add(DeviceDiscoveryService.DiscoveryMethod.NONE);
        }
    }
    
    /**
     * When an address request has been received, use this to assign the new address.
     * When address assignment is succesfull between client and server it will remove
     * the device from the discovered devices list. It is possible that this device
     * will re-appear in the list but then with the given address. When address
     * assignment fails between server and client the device stays in the list.
     * 
     * An successfull interaction between server and client does not guarantee the
     * address assignment to the hardware requestng it!
     * 
     * @param device An DiscoveredDevice instance.
     * @param address The address to be send.
     * @return true or false if address can be assigned.
     * @throws org.pidome.client.entities.devicediscovery.DeviceDiscoveryException When there is server communication error.
     */
    public final boolean setDiscoveredDeviceNewAddress(DiscoveredDevice device, String address) throws DeviceDiscoveryException {
        Map<String,Object> functionParams = new HashMap<>();
        functionParams.putAll(device.getDeviceParameters());
        functionParams.put("function_id", "FUNCTION_REQUEST_ADDRESS");
        functionParams.put("orig_address", device.getDeviceAddress());
        functionParams.put("address", address);
        
        Map<String,Object> params = new HashMap<>();
        params.put("peripheralport", this.getPort());
        params.put("params", functionParams);
        try {
            boolean result = (boolean)this.connection.getJsonHTTPRPC("DeviceService.peripheralDeviceFunction", params, "DeviceService.peripheralDeviceFunction").getResult().get("success");
            if(result){
                this.removeDiscoveredDevice(device);
            }
            return result;
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDiscoveryDriver.class.getName()).log(Level.SEVERE, "problem communicating with the server", ex);
            throw new DeviceDiscoveryException(ex);
        } catch (Exception ex){
            Logger.getLogger(DeviceDiscoveryDriver.class.getName()).log(Level.SEVERE, "Possible client data failure", ex);
            throw new DeviceDiscoveryException(ex);
        }
    }
    
    /**
     * This function adds a DiscoveredDevice to the server.
     * When a device is successfully added to the server the device will be removed
     * from the DiscoveredDevice list. When it fails it stays there.
     * 
     * It is up to you to check the result of this function.
     * 
     * @param device a DiscoveredDevice instance
     * @param installedDeviceId The id of the device to be added retrieved with <code>DeviceServer.getInstalledDevicesByLiveDriver</code>
     * @param deviceName The name that should be assigned.
     * @param categoryId The category for this device (Category Service).
     * @param locationId The location id for this device (Location service).
     * @return 
     * @throws org.pidome.client.entities.devicediscovery.DeviceDiscoveryException 
     */
    public final boolean addDiscoveredDevice(DiscoveredDevice device, int installedDeviceId, String deviceName, int categoryId, int locationId) throws DeviceDiscoveryException {
        Map<String,Object> functionParams = new HashMap<>();
        functionParams.putAll(device.getDeviceParameters());
        functionParams.put("function_id", "FUNCTION_ADD_DEVICE");
        functionParams.put("device_id", installedDeviceId);
        functionParams.put("device_name", deviceName);
        functionParams.put("device_categoryid", categoryId);
        functionParams.put("device_locationid", locationId);
        
        Map<String,Object> params = new HashMap<>();
        params.put("peripheralport", this.getPort());
        params.put("params", functionParams);
        
        try {
            boolean result = (boolean)this.connection.getJsonHTTPRPC("DeviceService.peripheralDeviceFunction", params, "DeviceService.peripheralDeviceFunction").getResult().get("success");
            if(result){
                this.removeDiscoveredDevice(device);
            }
            return result;
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDiscoveryDriver.class.getName()).log(Level.SEVERE, "problem communicating with the server", ex);
            throw new DeviceDiscoveryException(ex);
        } catch (Exception ex){
            Logger.getLogger(DeviceDiscoveryDriver.class.getName()).log(Level.SEVERE, "Possible client data failure", ex);
            throw new DeviceDiscoveryException(ex);
        }
    }
    
    /**
     * Adds a device to the driver from broadcast innfo.
     * @param device 
     */
    protected final void addFoundDeviceFromBroadcast(DiscoveredDevice device){
        this.discoveredDevicesList.add(device);
    }
    
    /**
     * Removes a device from the list based on it's unique address.
     * @param address 
     */
    protected final void removeFoundDeviceFromBroadcast(String address){
        DiscoveredDevice device = null;
        for(DiscoveredDevice deviceCheck: this.discoveredDevicesList.subList(0, this.discoveredDevicesList.size())){
            if(deviceCheck.getDeviceAddress().equals(address)){
                device = deviceCheck;
            }
        }
        if(device != null){
            this.discoveredDevicesList.remove(device);
        }
    }
    
    /**
     * Returns the amountof known devices.
     * @return 
     */
    public final int getKnownDevicesCount(){
        return discoveredDevicesList.size();
    }
    
    /**
     * Enable discovery.
     * @param time timed methods
     */
    protected void setDiscoveryEnabled(int time){
        for(DiscoveryType type:DiscoveryType.values()){
            if(type.getLevel()==time){
                this.discoveryType = type;
                this.active.setValue(true);
            }
        }
    }
    
    protected void setDiscoveryDisabled(){
        this.discoveryType = DiscoveryType.NONE;
        this.active.setValue(false);
    }
    
    /**
     * Returns the driver name.
     * @return String name.
     */
    public final String getDriverName(){
        return driverName;
    }
    
    /**
     * Returns the amount of devices found.
     * @return Boundable integer object property
     */
    public final int getFoundDevices(){
        return this.discoveredDevicesList.size();
    }
    
    /**
     * Returns if discovery is currently running.
     * @return Boundable boolean object property.
     */
    public final BooleanPropertyBindingBean getDiscoveryIsActive(){
        return this.active;
    }
    
    /**
     * Returns the current discovery type.
     * @return DiscoverType current active.
     */
    public final DeviceDiscoveryService.DiscoveryType getDiscoveryType(){
        return this.discoveryType;
    }
    
    /**
     * Returns the supported discovery methods.
     * All though it is only informative you should check for DeviceDiscoveryService.DiscoveryMethod.NONE
     * so you know if discovery would even work. When NONE is in the list discovery is not enabled.
     * You can choose not to show this driver or be informartive to the user.
     * 
     * When trying to enable discovery when NONE is in the list it will result in
     * an exception.
     * 
     * Next to non you have SCAN and DISCOVERY. See enum for details.
     * 
     * @return The supported DiscoveryMethod's
     */
    public final List<DeviceDiscoveryService.DiscoveryMethod> getDiscoveryMethods(){
        return this.discoveryMethods;
    }
    
    /**
     * Enables device discovery.
     * Multiple calls will not result in a longer discovery period when a time limited type is selected. the NONE type will throw an Exception. Do not use.
     * @param discoveryType The type of discovery to run.
     * @throws DeviceDiscoveryException When NONE is selected, there is no method known or call failes with the server.
     */
    public final void enableDiscovery(DeviceDiscoveryService.DiscoveryType discoveryType) throws DeviceDiscoveryException {
        if(discoveryType == DeviceDiscoveryService.DiscoveryType.NONE){
            throw new DeviceDiscoveryException("Enabling discovery with type NONE is useless");
        }
        if(discoveryMethods.contains(DiscoveryMethod.NONE)){
            throw new DeviceDiscoveryException("There is no discovery method known. Contact driver author.");
        }
        Map<String,Object> params = new HashMap<>();
        params.put("peripheralport", port);
        params.put("period", discoveryType.getLevel());
        try {
            this.connection.getJsonHTTPRPC("DeviceService.enableDeviceDiscovery", params, "DeviceService.enableDeviceDiscovery");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDiscoveryDriver.class.getName()).log(Level.SEVERE, "Could not complete call to server", ex);
            throw new DeviceDiscoveryException(ex);
        }
    }
    
    /**
     * Enables device discovery.
     * Multiple calls will not result in a longer discovery period when a time limited type is selected. the NONE type will throw an Exception. Do not use.
     * @throws DeviceDiscoveryException When NONE is selected, there is no method known or call failes with the server.
     */
    public final void disableDiscovery() throws DeviceDiscoveryException {
        Map<String,Object> params = new HashMap<>();
        params.put("peripheralport", port);
        try {
            this.connection.getJsonHTTPRPC("DeviceService.disableDeviceDiscovery", params, "DeviceService.disableDeviceDiscovery");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDiscoveryDriver.class.getName()).log(Level.SEVERE, "Could not complete call to server", ex);
            throw new DeviceDiscoveryException(ex);
        }
    }
 
    /**
     * Returns the port asociated with this driver.
     * @return The port this driver is serving.
     */
    public final String getPort(){
        return this.port;
    }
    
    /**
     * Destroy any references and unset listeners.
     */
    protected final void destroy(){
        discoveredDevicesList.clear();
    }
    
    /**
     * Returns a list of found devices.
     * @return A read only observable of found devices.
     */
    public final ReadOnlyObservableArrayListBean<DiscoveredDevice> getFoundDevicesList(){
        return this.readOnlyDiscoveredDevicesList;
    }
  
    /**
     * Removes a Discovered device.
     * It is not needed to supply a driver as discovered devices are handled
     * uniquely.
     * @param device The DiscoveredDevice to remove.
     */
    public final void removeDiscoveredDevice(DiscoveredDevice device){
        Map<String,Object> removeParams = new HashMap<>();
        removeParams.put("peripheralport", getPort());
        removeParams.put("address", device.getDeviceAddress());
        try {
            this.connection.getJsonHTTPRPC("DeviceService.removeDiscoveredDevice", removeParams, "DeviceService.removeDiscoveredDevice");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDiscoveryService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}