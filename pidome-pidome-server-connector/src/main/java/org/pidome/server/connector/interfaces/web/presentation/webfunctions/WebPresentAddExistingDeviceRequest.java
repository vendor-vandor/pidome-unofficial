/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation.webfunctions;

import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;

/**
 * USed when device add requests are made from the device discovery.
 * @author John
 */
public final class WebPresentAddExistingDeviceRequest {
    
    /**
     * Contains all the given parameters.
     */
    Map<String,Object> result = new HashMap<>();
    
    /**
     * The selected device id to add.
     */
    int deviceId = 0;
    /**
     * location selected.
     */
    int deviceLocId = 1;
    /**
     * The category for this device.
     */
    int deviceCatId = 9;
    /**
     * The device name.
     */
    String deviceName="Nameless device";
    /**
     * If a skeleton should be created this is et to yes.
     */
    boolean createSkeleton = false;
    /**
     * Hols the custom skeleton name.
     * Only filled when the createSkeleton is set to true this var contains 
     * the skeleton name.
     */
    String customSkelName = "";
    
    DiscoveredDevice.FUNCTION_TYPE functionType = DiscoveredDevice.FUNCTION_TYPE.FUNCTION_ADD_DEVICE;
    
    /**
     * Constructs the request.
     * @param type
     */
    public WebPresentAddExistingDeviceRequest(DiscoveredDevice.FUNCTION_TYPE type){
        functionType = type;
    }
    
    /**
     * Constructs the request's parameters.
     * @param result 
     */
    public final void setResultParams(Map<String,Object> result){
        try {
            if(result.containsKey("device_id")){
                deviceId = ((Long)result.get("device_id")).intValue();
                result.remove("device_id");
            }
        } catch (Exception ex){}
        try {
            if(result.containsKey("device_locationid")){
                deviceLocId = ((Long)result.get("device_locationid")).intValue();
                result.remove("device_locationid");
            }
        } catch (Exception ex){}
        try {
            if(result.containsKey("device_categoryid")){
                deviceCatId = ((Long)result.get("device_categoryid")).intValue();
                result.remove("device_categoryid");
            }
        } catch (Exception ex){}
        try {
            if(result.containsKey("device_name")){
                deviceName = (String)result.get("device_name");
                result.remove("device_name");
            }
        } catch (Exception ex){}
        try {
            if(result.containsKey("device_skelselect") && result.containsKey("device_skelname")){
                createSkeleton = (boolean)result.get("device_skelselect") == true && !result.get("device_skelname").equals("");
                if(createSkeleton){
                    customSkelName = (String)result.get("device_skelname");
                }
                result.remove("device_skelselect");
                result.remove("device_skelname");
            }
        } catch (Exception ex){}
        try {
            if(result.containsKey("device_add_function_type")){
                switch((String)result.get("device_add_function_type")){
                    case "FUNCTION_REQUEST_ADDRESS":
                        functionType = DiscoveredDevice.FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS;
                    break;
                }
                result.remove("device_add_function_type");
            }
        } catch (Exception ex){}
        this.result = result;
    }
    
    /**
     * This is set to true when a device skeleton creation is requested.
     * @return 
     */
    public final boolean getCreateSkeleton(){
        return this.createSkeleton;
    }
    
    /**
     * Returns the requested device skeleton name.
     * Only when the getCreateSkeleton is set to true a name will be returned.
     * @return 
     */
    public final String getDeviceSkeletonName(){
        return this.customSkelName;
    }
    
    /**
     * Returns the request type.
     * @return 
     */
    public final DiscoveredDevice.FUNCTION_TYPE getRequestFunctionType(){
        return this.functionType;
    }
    
    /**
     * Returns the device base id to be added.
     * This is the id from the installed devices.
     * @return 
     */
    public final int getDeviceId(){
        return deviceId;
    }
    
    /**
     * The location id assigned.
     * @return 
     */
    public final int getLocationId(){
        return deviceLocId;
    }
    
    /**
     * The category id.
     * @return 
     */
    public final int getCategoryId(){
        return deviceCatId;
    }
    
    /**
     * The device name given by the end user.
     * @return 
     */
    public final String getName(){
        return deviceName;
    }
    
    /**
     * Returns the parameter data.
     * @return 
     */
    public final Map<String,Object> getCustomData(){
        return this.result;
    }
    
}
