/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.locations.Location;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 * Base device class.
 * Holds the device information and control groups.
 * @author John
 */
public class Device {
    
    static {
        Logger.getLogger(Device.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * The device id.
     */
    private final int deviceId;
    
    /**
     * The device name as set by the user.
     */
    private String deviceName;

    /**
     * The device type name as known in the server.
     */
    private String deviceTypeName;
    
    /**
     * The address of the device somewhere in a network.
     */
    private String deviceAddress;
    
    /**
     * If a device is active or not.
     */
    private boolean deviceActive;
    
    /**
     * If a device is active or not.
     */
    private boolean deviceIsFavorite;
    
    /**
     * The device's sub category id
     */
    private int deviceSubCategoryId;
    
    /**
     * When the initial device list is retrieved it contains the location name of that moment, if a the location service is not loaded yet, this can be initialy used.
     */
    private Location deviceLocation;
    
    /**
     * List of control groups containing the controls.
     */
    private List<DeviceControlGroup> controlGroups = new ArrayList<>();
    
    /**
     * List of listeners to receive a pulse signal when new data arrives for this device.
     */
    private List<DevicePulseListener> pulseListeners = new ArrayList<>();
    
    /**
     * Connection interface.
     */
    private final PCCConnectionInterface connection;
    
    /**
     * Constructor.
     * Setting device id.
     * @param connection The server connection object.
     * @param deviceId The id of the device as known in the server.
     */
    protected Device(PCCConnectionInterface connection, int deviceId){
        this.deviceId = deviceId;
        this.connection = connection;
    }
    
    /**
     * Returns the connection.
     * @return The connection interface for this device.
     */
    protected final PCCConnectionInterface getConnection(){
        return this.connection;
    }
    
    /**
     * Send a device constructed command to the server.
     * @param command The command to send
     * @throws org.pidome.client.entities.devices.DeviceControlCommandException When a command can not be send.
     */
    public final void sendCommand(DeviceControl.DeviceCommandStructure command) throws DeviceControlCommandException {
        try {
            this.connection.getJsonHTTPRPC(command.getMethod(), command.getParameters(), command.getId());
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(Device.class.getName()).log(Level.SEVERE, "Could not send command", ex);
            throw new DeviceControlCommandException("Could not send command");
        }
    }
    
    /**
     * Sets the device name as defined by the user.
     * @param name The name as defined by the user.
     */
    protected final void setName(String name){
        this.deviceName = name;
    }
    
    /**
     * The device type name as known in the server.
     * @param name The device type name
     */
    protected final void setDeviceTypeName(String name){
        this.deviceTypeName = name;
    }
    
    /**
     * Set's the device address.
     * @param address The device address.
     */
    protected final void setDeviceAddress(String address){
        this.deviceAddress = address;
    }
    
    /**
     * Set's if a device is active or not.
     * @param active true if active, false if non active
     */
    protected final void setDeviceActive(boolean active){
        this.deviceActive = active;
    }
    
    /**
     * Set's the device favorite flag.
     * @param fav True if favorite, false if not.
     */
    protected final void setDeviceIsFavorite(boolean fav){
        this.deviceIsFavorite = fav;
    }
    
    /**
     * Set's the device's sub category id.
     * @param id The device's subcategory id as found in the CategoryService.
     */
    protected final void setDeviceSubCategoryId(int id){
        this.deviceSubCategoryId = id;
    }
    
    /**
     * Creates the device commands structure.
     * @param deviceGroups The device command groups
     */
    protected void composeDevice(List<Map<String,Object>> deviceGroups){
        for(Map<String,Object> group:deviceGroups){
            DeviceControlGroup deviceGroup = new DeviceControlGroup(this,(String)group.get("id"), (String)group.get("name"));
            deviceGroup.createControlsGroup((List<Map<String,Object>>)group.get("commands"));
            controlGroups.add(deviceGroup);
        }
    }
    
    /**
     * Add a pulse listener.
     * A listener object will only be added once despite how often this is called.
     * @param listener 
     */
    public final void addDevicePulseListener(DevicePulseListener listener){
        if(!this.pulseListeners.contains(listener)){
            this.pulseListeners.add(listener);
        }
    }

    /**
     * Sets the device location.
     * @param location The location for this device. 
     */
    protected final void setLocation(Location location){
        this.deviceLocation = location;
    }
    
    /**
     * Removes a pulse listener.
     * @param listener 
     */
    public final void removeDevicePulseListener(DevicePulseListener listener){
        this.pulseListeners.remove(listener);
    }
    
    /**
     * Used for notifying pulse handlers new data has been received.
     */
    private void notifyPulseHandlers(){
        Iterator<DevicePulseListener> iter = pulseListeners.iterator();
        while(iter.hasNext()){
            iter.next().pulse();
        }
    }
    
    /**
     * Updates groups of controls with new control data.
     * @param groups The list of groups with their controls.
     */
    protected final void updateGroupValues(List<Map<String,Object>> groups){
        new Thread(() -> {
            notifyPulseHandlers();
        }).start();
        for(Map<String,Object> groupData:groups){
            try {
                getControlGroup((String)groupData.get("groupid")).updateControlValues((Map<String,Object>)groupData.get("controls"));
            } catch (DeviceGroupNotFoundException ex) {
                Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Returns a controls group by it's id.
     * @param groupId The group id.
     * @return The group containing controls.
     * @throws org.pidome.client.entities.devices.DeviceGroupNotFoundException Thrown when the group id is not found.
     */
    public final DeviceControlGroup getControlGroup(String groupId) throws DeviceGroupNotFoundException {
        for(DeviceControlGroup group:this.controlGroups){
            if(group.getGroupId().equals(groupId)){
                return group;
            }
        }
        throw new DeviceGroupNotFoundException("Group with id " + groupId + " does not exist.");
    }
    
    /**
     * Returns the device id.
     * @return Thedevice id.
     */
    public final int getDeviceId(){
        return this.deviceId;
    }
 
    /**
     * Returns the device name as defined by the user.
     * @return The device name.
     */
    public final String getDeviceName(){
        return this.deviceName;
    }
    
    /**
     * Returns the device type name as known in the server.
     * @return The device type name.
     */
    public final String getDeviceTypeName(){
        return this.deviceTypeName;
    }
    
    /**
     * Returns the device address somewhere in a network.
     * This address is not related to any server bound address, which is always
     * the device's id.
     * @return The device address.
     */
    public final String getDeviceAddress(){
        return this.deviceAddress;
    }
    
    /**
     * If a device is active or not. 
     * @return true if active, false if not.
     */
    public final boolean getDeviceActive(){
        return this.deviceActive;
    }
    
    /**
     * If the device is an favorite. 
     * @return true if favorite, false if not.
     */
    public final boolean getDeviceIsFavorite(){
        return this.deviceIsFavorite;
    }
    
    /**
     * Returns the device's location id.
     * @return The id as found in the LocationService.
     */
    public final int getDeviceLocationId(){
        return this.deviceLocation.getLocationId();
    }    

    /**
     * Returns the device location.
     * @return The location object for this device.
     */
    public final Location getLocation(){
        return this.deviceLocation;
    }
    
    /**
     * Temporary location name.
     * Do only use this for initial displaying of the location name, initialize 
     * the location service asap to bind the device location id to the location 
     * service location id.
     * @return The location name at moment of initialization of the device.
     */
    public final String getTemporaryLocationName(){
        return this.deviceLocation.getRoomName().getValue();
    }
    
    /**
     * Returns the device's sub category id.
     * @return The id as found in the CategoryService.
     */
    public final int getDeviceSubcategoryId(){
        return this.deviceSubCategoryId;
    }
    
    /**
     * Returns the list of device controls groups.
     * @return a list of control groups.
     */
    public final List<DeviceControlGroup> getControlGroups(){
        return this.controlGroups;
    }
    
    /**
     * Returns a list of controls defined as a shortcut.
     * This list returns a maximum of three controls.
     * @return A list of controls to be used in shortcut positions.
     */
    public final List<DeviceControl> getShortcutControls(){
        List<DeviceControl> controls = new ArrayList<>();
        for(DeviceControlGroup group: getControlGroups()){
            for(DeviceControl control: group.getGroupControls()){
                if(control.hasShortCut()){
                    controls.add(control);
                }
            }
        }
        return controls;
    }
    
}
