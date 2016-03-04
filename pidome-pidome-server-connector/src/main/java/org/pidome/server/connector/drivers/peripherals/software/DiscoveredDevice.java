/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.peripherals.software;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.pidome.server.connector.drivers.devices.devicestructure.builder.DeviceStructureCreator;

/**
 * Information about a discovered device.
 * With this class an user is presented a device in the discovered devices list.
 * By default when an user clicks add they will be presented a window where they
 * can select a device which is bound to the driver where this device comes from.
 * 
 * By setting the function type to FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS the end
 * user is being presented not the regular add device window, but a window which
 * is requesting for a device's address. When entered the device will disappear from
 * the list and re-added if discovered again with the given address.
 * 
 * The function type FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS has the possibility to
 * include a remark using setDescription(String description) which then will be 
 * presented to the user as information what to do or what to expect etc....
 * 
 * @author John
 */
public class DiscoveredDevice {
    
    public enum FUNCTION_TYPE {
        /**
         * Enum type for an end user to add a device.
         * This type shows the regular add device window.
         */
        FUNCTION_ADD_DEVICE, 
        /**
         * Enum type to show the user this device needs an address.
         * This will show the window the user has to assign an address
         * to the device. This function will show an input field where
         * an user can enter an address.
         */
        FUNCTION_REQUEST_ADDRESS;
    }
    
    /**
     * Visual presentative information.
     */
    private final Map<String,Object> deviceVisuals = new HashMap<>();
    /**
     * Params passed here are used for returning these to the driver.
     */
    private final Map<String,Object> functionParams = new HashMap<>();
    
    /**
     * Discovered device address.
     */
    private final String address;
    
    /**
     * Discovered device address.
     */
    private final String identifyingName;
    
    /**
     * The device driver when set.
     */
    private String deviceDriver;
    
    /**
     * A description.
     * Only available when FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS 
     */
    private String description = "";
    
    /**
     * The function type.
     * Defaults to FUNCTION_ADD_DEVICE.
     */
    private FUNCTION_TYPE cunstomFunctionType = FUNCTION_TYPE.FUNCTION_ADD_DEVICE;

    /**
     * When the device was discovered.
     * Created in the constructor.
     */
    private final String discoveryTime;
    
    /**
     * The newly proposed address.
     * Defaults to null.
     */
    private String newAddress;
    
    /**
     * True when the driver can create a device skeleton.
     */
    private boolean autoCreate;
    
    /**
     * When auto createe is available a structure is build and stored
     * in this var so the auto create is ready to add a device skeleton.
     */
    private DeviceStructureCreator deviceStructure;
    
    /**
     * Constructing a new found device.
     * The add window type defaults to FUNCTION_TYPE.FUNCTION_ADD_DEVICE
     * @param deviceAddress The device's address.
     * @param identifyingName A name to give so an end user can find out what kind of device it is.
     */
    public DiscoveredDevice(String deviceAddress, String identifyingName){
        this.address = deviceAddress;
        this.identifyingName = identifyingName;
        deviceVisuals.put("Address", deviceAddress);
        functionParams.put("address", deviceAddress);
        this.discoveryTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault()).getTime());
    }
    
    /**
     * Sets the device structure creator.
     * This automatically sets the auto create flag which enables an user to 
     * choose a name for the device skeleton and causes the device skeleton to
     * be created when saving the new device.
     * @param deviceStructure 
     */
    public void setDeviceStructure(DeviceStructureCreator deviceStructure){
        this.deviceStructure = deviceStructure;
        this.autoCreate = true;
    }
    
    /**
     * Returns the device structure creator.
     * @return 
     */
    public final DeviceStructureCreator getDeviceStructureCreator(){
        return this.deviceStructure;
    }
    
    /**
     * Set if this discovered device supports automatic creation of device skeletons.
     * @param autoCreate 
     */
    public final void setAutoCreate(boolean autoCreate){
        this.autoCreate = autoCreate;
    }
    
    /**
     * Returns true if this device support automatic creation of devices.
     * @return 
     */
    public final boolean isAutoCreate(){
        return this.autoCreate;
    }
    
    /**
     * Sets the device driver.
     * USe this when you are sure of which device it is. The driver which is mapped to is limited to the package the driver resides in.
     * You can only set this once so make sure you use the correct path!
     * @param driver 
     */
    public final void setDeviceDriver(String driver){
        if(this.deviceDriver==null) this.deviceDriver = driver;
    }
    
    /**
     * Returns the device driver for the found device.
     * Returns null when there is no driver set.
     */
    public final String getDeviceDriver(){
        return this.deviceDriver;
    }
    
    /**
     * Set the function type to show or the device add, or address request window.
     * only use this function to change FUNCTION_TYPE.FUNCTION_ADD_DEVICE to FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS
     * @param type 
     */
    public final void setFunctionType(FUNCTION_TYPE type){
        this.cunstomFunctionType = type;
    }
    
    /**
     * Set the new composed address.
     * Use this to offer the user a new address field where this value has been prefilled for the user.
     * @param address 
     */
    public final void setNewAddress(String address){
        this.newAddress = address;
    }
    
    /**
     * Returns the newly proposed address.
     * If not entered it returns null.
     * @return 
     */
    public final String getNewAddress(){
        return this.newAddress;
    }
    
    /**
     * Adds the possibility to add a description.
     * This function is only used when the function type is FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS
     * so an user can be informed about any routines.
     * @param description 
     */
    public final void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the description set.
     * @return 
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Returns the custom function if set.
     * @return 
     */
    public final FUNCTION_TYPE getFunctionType(){
        return this.cunstomFunctionType;
    }
    
    /**
     * Returns the discovery time.
     */
    public final String getDiscoveryDateTime(){
        return this.discoveryTime;
    }
    
    /**
     * Returns a name which should hint an end user what kind of device it is.
     * @return 
     */
    public final String getName(){
        return this.identifyingName;
    }
    
    /**
     * Returns the device address.
     * @return 
     */
    public final String getAddress(){
        return this.address;
    }
    
    /**
     * Sets information as device details.
     * @param key
     * @param value 
     */
    public final void addVisualInformation(String key, Object value){
        if(!key.equals("address")){
            deviceVisuals.put(key, value);
        }
    }
    
    /**
     * Returns the visual parameters.
     * @return 
     */
    public final Map<String,Object> getVisualInformation(){
        return this.deviceVisuals;
    }
    
    /**
     * Sets the information the driver will receive when an user adds a device.
     * Parameters are ALWAYS set to lower case!
     * @param key
     * @param value 
     */
    public final void addParameterValue(String key, Object value){
        if(!key.toLowerCase().equals("address")){
            functionParams.put(key.toLowerCase(), value);
        }
    }
    
    /**
     * Returns the visual parameters.
     * @return 
     */
    public final Map<String,Object> getParameterValues(){
        return this.functionParams;
    }
    
}