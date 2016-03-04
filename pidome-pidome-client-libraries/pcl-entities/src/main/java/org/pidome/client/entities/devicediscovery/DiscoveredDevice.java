/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devicediscovery;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService.DeviceAddFunctionType;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class DiscoveredDevice {
 
    /**
     * Parameters used to post info.
     */
    private final Map<String,Object> deviceParameters = new HashMap<>();
    
    /**
     * Device name.
     */
    private String deviceName;
    
    /**
     * Map containing information about the device.
     */
    private final Map<String, Object> deviceInfo = new HashMap<>();
    
    /**
     * Device address as string represtentation.
     */
    private final String deviceAddress;
    
    /**
     * Discovery datee time as string.
     */
    private final String deviceDiscoveryTime;
    
    /**
     * The function type used.
     * The function type is used to determine if it is a device address request or addition request.
     */
    private DeviceAddFunctionType functionType;
    
    /**
     * Boolean holding the info if this device is known so an user does noet have
     * to select it from a list.
     */
    private boolean isKnownDevice = false;
    
    /**
     * The driver of this device.
     */
    private String deviceDriver;
    
    /**
     * Boolean holding true when data is complete.
     */
    private boolean dataComplete;
    
    /**
     * Available when type is request address explaining what to do.
     */
    private String infoDescription = "";
    
    /**
     * Newly proposed address.
     */
    private String proposedAddress = "";
    
    /**
     * Constructor.
     * @param connection Used to fetch data when data is incomplete.
     * @param driver The driver of this device.
     * @param deviceData Guaranteed device data in a String,Object map.
     */
    protected DiscoveredDevice(PCCConnectionInterface connection, DeviceDiscoveryDriver driver, Map<String,Object> deviceData){
        this(connection, driver, deviceData, true);
    }
    
    protected DiscoveredDevice(PCCConnectionInterface connection, DeviceDiscoveryDriver driver, Map<String,Object> deviceData, boolean knownData){
        this.deviceName =          (String)deviceData.get("name");
        this.deviceAddress =       (String)deviceData.get("address");
        this.deviceDiscoveryTime = (String)deviceData.get("time");
        for(DeviceAddFunctionType type:DeviceDiscoveryService.DeviceAddFunctionType.values()){
            if(type.getValue().equals((String)deviceData.get("type"))){
                functionType = type;
            }
        }
        if(((Map)deviceData.get("knowndevice")).containsKey("devicedriver")){
            isKnownDevice = true;
            deviceDriver = ((Map<String,String>)deviceData.get("knowndevice")).get("devicedriver");
            deviceName = ((Map<String,String>)deviceData.get("knowndevice")).get("name");
        }
        if(deviceData.containsKey("newaddress") && deviceData.get("newaddress")!=null){
            proposedAddress = (String)deviceData.get("newaddress");
        }
        if(knownData){
            if(deviceData.containsKey("description")){
                this.infoDescription = (String)deviceData.get("description");
            }
            this.deviceParameters.putAll((Map<String,Object>)deviceData.get("parameters"));
            this.deviceInfo.putAll((Map<String,String>)deviceData.get("information"));
            this.deviceName = (String)deviceData.get("name");
            if(deviceData.containsKey("devicedriver") && !((String)deviceData.get("devicedriver")).isEmpty()){
                isKnownDevice = true;
                deviceDriver = (String)deviceData.get("devicedriver");
            }
        } else {
            try {
                Map<String,Object> params = new HashMap<>();
                params.put("peripheralport", driver.getPort());
                params.put("address", (String)deviceData.get("address"));
                Map<String,Object> data = (Map<String,Object>) connection.getJsonHTTPRPC("DeviceService.getDiscoveredDevice", params, "DeviceService.getDiscoveredDevice").getResult().get("data");
                if(data.containsKey("description")){
                    this.infoDescription = (String)data.get("description");
                }
                if(data.get("parameters")!=null){
                    this.deviceParameters.putAll((Map<String,Object>)data.get("parameters"));
                }
                if(data.get("information")!=null){
                    this.deviceInfo.putAll((Map<String,String>)data.get("information"));
                }
                if(data.containsKey("devicedriver") && !((String)data.get("devicedriver")).isEmpty()){
                    isKnownDevice = true;
                    deviceDriver = (String)data.get("devicedriver");
                }
                if(data.containsKey("newaddress") && data.get("newaddress")!=null){
                    proposedAddress = (String)data.get("newaddress");
                }
            } catch (NullPointerException | PCCEntityDataHandlerException ex) {
                Logger.getLogger(DeviceDiscoveryService.class.getName()).log(Level.SEVERE, "could not compose additional device information", ex);
            }
        }
    }
    
    /**
     * Returns parameter set for posting device info to the server.
     * @return Map with String,Object parameter set.
     */
    protected final Map<String,Object> getDeviceParameters(){
        return this.deviceParameters;
    }
    
    /**
     * Returns parameter set for posting device info to the server.
     * @return Map with String,Object parameter set.
     */
    public final Map<String, Object> getDeviceInfo(){
        return this.deviceInfo;
    }
    
    /**
     * Returns the server discovery time of the device as formatted string.
     * @return String formatted date and time.
     */
    public final String getDeviceDiscoveryTime(){
        return this.deviceDiscoveryTime;
    }
    
    /**
     * Returns this device's address.
     * @return 
     */
    protected final String getDeviceAddress(){
        return this.deviceAddress;
    }
    
    /**
     * Returns the discovered device name
     * @return String device name
     */
    public final String getDeviceName(){
        return this.deviceName;
    }
 
    /**
     * Returns the function type.
     * @return DeviceAddFunctionType To determine if a request is made for an address or device addition.
     */
    public final DeviceAddFunctionType getDeviceFunctionType(){
        return this.functionType;
    }
    
    /**
     * Returns if the device is known.
     * Boolean holding the info if this device is known so an user does noet have
     * to select it from a list.
     * @return 
     */
    public final boolean isKnownDevice(){
        return this.isKnownDevice;
    }
    
    /**
     * Returns information when type is request address.
     * @return String with information.
     */
    public final String getInfoDescription(){
        return this.infoDescription;
    }
    
    /**
     * Returns the proposed address.
     * Only available when address request is done.
     * @return Address as string.
     */
    public final String getProposedAddress(){
        return this.proposedAddress;
    }
    
}