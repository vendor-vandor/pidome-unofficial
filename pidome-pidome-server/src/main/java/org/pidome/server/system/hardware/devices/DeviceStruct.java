/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pidome.server.system.hardware.devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceDataListener;
import org.pidome.server.connector.drivers.devices.DeviceDataStoreListener;
import org.pidome.server.connector.drivers.devices.DeviceDriverListener;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.DeviceScheduler;
import org.pidome.server.connector.drivers.devices.DeviceSchedulerException;
import org.pidome.server.connector.drivers.devices.DeviceStructProxyInterface;
import org.pidome.server.connector.drivers.devices.DeviceStructure;
import org.pidome.server.connector.drivers.devices.IllegalDeviceActionException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceAddressing;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl.DataModifierDirection;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlDataType;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlType;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsSet;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceDataControl;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.datamodifiers.DataModifierPlugin;
import org.pidome.server.connector.shareddata.SharedServerTimeService;
import org.pidome.server.connector.tools.properties.BooleanPropertyBindingBean;
import org.pidome.server.connector.tools.properties.IntegerPropertyBindingBean;
import org.pidome.server.connector.tools.properties.StringPropertyBindingBean;
import org.pidome.server.services.plugins.DataModifierPluginService;
import org.pidome.server.services.scenes.ServerScene;

/**
 * Device encapsulating class.
 * @author John
 */
public class DeviceStruct implements DeviceInterface,DeviceDriverListener,DeviceStructProxyInterface {

    Device deviceHandle;
    
    private final Map<String,Map<String,String>> deviceLocations = new HashMap<>();
    
    private PeripheralSoftwareDriverInterface driverConnect;
    private final List<DeviceDataListener> _deviceDataListeners           = Collections.synchronizedList(new ArrayList());
    private final List<DeviceDataStoreListener> _deviceDataStoreListeners = Collections.synchronizedList(new ArrayList());
    
    Map<String,List<String>> storageSet = new HashMap<>();
    
    ExecutorService notificationService = Executors.newSingleThreadExecutor();
    ExecutorService dataHandlingService = Executors.newSingleThreadExecutor();
    ExecutorService storageService      = Executors.newSingleThreadExecutor();
    
    private String driverDriverName               = "";
    
    private String lastReceiveTime = "00-00-0000 00:00:00";
    private String lastSendTime    = "00-00-0000 00:00:00";
    
    private RetentionHandler retFile;
    
    private final String meReal;
    private final String me;
    private final DeviceStructure deviceStructure;
    
    private int definitionSequence = 0;
    
    private int packageId = 0;
    private int driverDriverId = 0;
    
    /// device base info
    private int deviceId                          = 0;
    private int installedId                       = 0;
    private String driverName                     = "";
    private final StringPropertyBindingBean friendlyName     = new StringPropertyBindingBean();
    private final IntegerPropertyBindingBean locationId      = new IntegerPropertyBindingBean(0);
    private final StringPropertyBindingBean deviceName       = new StringPropertyBindingBean();
    private final IntegerPropertyBindingBean categoryId      = new IntegerPropertyBindingBean(0);
    private boolean fixed                         = true;
    private int deviceType                        = 0;
    private final BooleanPropertyBindingBean active          = new BooleanPropertyBindingBean(false);
    private final BooleanPropertyBindingBean favorite        = new BooleanPropertyBindingBean(false);
    private final StringPropertyBindingBean categoryName     = new StringPropertyBindingBean();
    private final StringPropertyBindingBean categoryConstant = new StringPropertyBindingBean();
    private final StringPropertyBindingBean locationName     = new StringPropertyBindingBean();
    
    private Map<String, Map<String,Runnable>> lastDeviceCommand = new HashMap<>();
    
    private DeviceStructure.DeviceOptions deviceOptions;
    
    boolean storeData = false;
    
    ScheduledExecutorService scheduledServiceExecutor;
    
    private boolean commandResult = false;
    
    static Logger LOG = LogManager.getLogger(DeviceStruct.class);
    
    private ServerScene sceneActive;
    
    /**
     * Constructor.
     * @param device 
     */
    protected DeviceStruct(Device device){
        this.deviceHandle = device;
        this.deviceHandle.setStructProxy(this);
        meReal = deviceHandle.getClass().getCanonicalName();
        me = meReal.substring(0, meReal.lastIndexOf("."));
        deviceStructure = new DeviceStructure(meReal);
    }
    
    protected final void setDeviceDriverDriverId(int driverDriverId){
        this.driverDriverId = driverDriverId;
    }
    
    public final int getDeviceDriverDriverId(){
        return this.driverDriverId;
    }
    
    protected final void setPackageId(int packageId){
        this.packageId = packageId;
    }
    
    public final int getPackageId(){
        return this.packageId;
    }
    
    public final Device getDevice(){
        return this.deviceHandle;
    }
    
    /**
     * Initiates the scheduler.
     */
    @Override
    public final void initiateScheduler(){
        for(DeviceControlsGroup group:this.getFullCommandSet().getControlsGroups().values()){
            for(DeviceControl control:group.getGroupControls().values()){
                if(control.hasTimeOutSet()){
                    control.startTimeOutScheduler();
                }
            }
        }
        scheduledServiceExecutor = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Schedule an executable item for repeated executions.
     * @param runnable
     * @param interval
     * @param timeUnit 
     * @throws org.pidome.server.connector.drivers.devices.DeviceSchedulerException 
     */
    @Override
    public final void scheduleItem(Runnable runnable, int interval, TimeUnit timeUnit) throws DeviceSchedulerException {
        if(scheduledServiceExecutor==null) {
            initiateScheduler();
        }
        if(timeUnit.equals(TimeUnit.MICROSECONDS) || timeUnit.equals(TimeUnit.MILLISECONDS) || timeUnit.equals(TimeUnit.NANOSECONDS) || (timeUnit.equals(TimeUnit.SECONDS) && interval<30)) throw new DeviceSchedulerException("Minimum schedule is 30 seconds");
        scheduledServiceExecutor.schedule(runnable, 10, TimeUnit.SECONDS);
        scheduledServiceExecutor.scheduleWithFixedDelay(runnable, interval, interval, timeUnit);
    }
    
    /**
     * Handles the command.
     * This function handles the command coming from outside and creates a device command request
     * and passes it to the device handling.
     * @param group
     * @param control
     * @param action 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException 
     */
    @Override
    public final void handleCommand(String group, String control, String action) throws UnsupportedDeviceCommandException {
        Map<String,Object> send = new HashMap<>();
        send.put("value", action);
        handleCommand(group, control, send);
    }
    
    /**
     * Handles the command.
     * This function handles the command coming from outside and creates a device command request
     * and passes it to the device handling.
     * @param group
     * @param control
     * @param action 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException 
     */
    @Override
    public final void handleCommand(String group, String control, Map<String,Object> action) throws UnsupportedDeviceCommandException {
        handleCommand(group, control, action, false);
    }
    
    /**
     * Returns the last known command ran based on group and control id.
     * @param groupId
     * @param controlId
     * @return
     * @throws UnsupportedDeviceCommandException 
     */
    public final Runnable getLastDeviceCommand(String groupId, String controlId) throws UnsupportedDeviceCommandException {
        try {
            return lastDeviceCommand.get(groupId).get(controlId);
        } catch (Exception ex){
            /// If there is no last command known, we try to return the last known state
            try {
                DeviceControl deviceControl = getFullCommandSet().getControlsGroup(groupId).getDeviceControl(controlId);
                Map<String,Object> action = new HashMap<>();
                action.put("value", deviceControl.getValue());
                action.put("extra", deviceControl.getExtra());
                return getDeviceCommandExecution(createDeviceExecCommand(groupId, controlId, action));
            } catch (DeviceControlsGroupException | DeviceControlException ex1) {
                LOG.warn("Last command not known and could not simulate a command from current control status: {}", ex.getMessage(), ex1);
                throw new UnsupportedDeviceCommandException("Last command not known and could not simulate a command from current control status");
            }
        }
    }
    
    /**
     * Stores the last known command.
     * @param group
     * @param controlId
     * @param command 
     */
    private void storeLastCommand(String group, String controlId, Runnable command){
        if(!lastDeviceCommand.containsKey(group)){
            Map<String,Runnable> addMap = new HashMap<>();
            lastDeviceCommand.put(group, addMap);
        }
        lastDeviceCommand.get(group).put(controlId, command);
    }
    
    /**
     * Handles the command.
     * This function handles the command coming from outside and creates a device command request
     * and passes it to the device handling.
     * @param group
     * @param control
     * @param action 
     * @param sceneForced Used when swapping scenes to make sure the last known command is not overwritten.
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException 
     */
    @Override
    public final void handleCommand(String group, String control, Map<String,Object> action, boolean sceneForced) throws UnsupportedDeviceCommandException {
        LOG.trace("Composing command data with group: {}, control: {}, actiondata: {}", group, control, action);
        try {
            if(!action.containsKey("value")){
                throw new UnsupportedDeviceCommandException("No value passed to control");
            }
            DeviceCommandRequest dcr = createDeviceExecCommand(group, control, action);
            Runnable lastCommand = getDeviceCommandExecution(dcr);
            if(!isPartOfScene(group, control) || sceneForced){
                if(dcr.getControl().hasModifier() && dcr.getControl().getDataDirection()!=null && dcr.getControl().getDataDirection().equals(DataModifierDirection.INPUT_OUTPUT)){
                    DataModifierPlugin plugin = DataModifierPluginService.getInstance().getPlugin(dcr.getControl().getModifierId());
                    plugin.handleInput(dcr);
                } else {
                    storeLastCommand(group, control, lastCommand);
                    lastCommand.run();
                }
            } else {
                this.sceneActive.registerLastKnownCommand(this.getId(),group, control, getDeviceCommandExecution(dcr));
            }
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            throw new UnsupportedDeviceCommandException("Unknown device control addressed " + ex.getMessage());
        } catch (UnsupportedDeviceCommandException ex){
            throw new UnsupportedDeviceCommandException(ex);
        } catch (Exception ex){
            LOG.error("Unhandled Exception in {}: {}",this.getFriendlyName(), ex.getMessage(), ex);
            throw new UnsupportedDeviceCommandException("Unhandled error (Exception) in device, refer to log file");
        }
    }
    
    /**
     * Creates the command responsible to change a control's state.
     * @param group
     * @param control
     * @param action
     * @return
     * @throws DeviceControlsGroupException
     * @throws DeviceControlException 
     */
    public final DeviceCommandRequest createDeviceExecCommand(String group, String control, Map<String,Object> action) throws DeviceControlsGroupException, DeviceControlException {
        DeviceControl deviceControl = getFullCommandSet().getControlsGroup(group).getDeviceControl(control);
        DeviceCommandRequest command = new DeviceCommandRequest(deviceControl);
        command.setGroupId(group);
        command.setCommandValue(action.get("value"));
        if(action.containsKey("extra")){
            command.setExtraValue((String)action.get("extra"));
        }
        command.setCommandValueData(action.get("value"));
        return command;
    }

    /**
     * Creates a runnable from a device command created.
     * @param command
     */
    @Override
    public final void handleCommandRequestFromModifier(final DeviceCommandRequest command) {
        getDeviceCommandExecution(command).run();
    }
    
    /**
     * Creates a runnable from a device command created.
     * @param command
     * @return 
     */
    public final Runnable getDeviceCommandExecution(final DeviceCommandRequest command) {
        Runnable run = () -> {
            try {
                deviceHandle.handleCommandRequest(command);
                Object commandValue = command.getCommandValue();
                if(commandValue == null){
                    commandValue = command.getCommandValueData();
                }
                command.getControl().setLastKnownValue(commandValue);
                DeviceNotification notification = new DeviceNotification();
                notification.addData(command.getGroupId(), command.getControlId(), command.getControl().getValue(), true);
                notifyClients(notification);
            } catch (UnsupportedDeviceCommandException ex) {
                LOG.error("Command '{}' for device '{}' with group: '{}' and control '{}' is unsupported: {}", command.getCommandValue(), this.getFriendlyName(), command.getGroupId(), command.getControlId(), ex.getMessage(),ex);
            }
        };
        return run;
    }
    
    /**
     * Check if a group and control combination is part of a scene.
     * @param groupId
     * @param controlId
     * @return 
     */
    private boolean isPartOfScene(String groupId, String controlId){
        return this.sceneActive !=null && this.sceneActive.hasDependency(this.getId(), groupId, controlId);
    }
    
    /**
     * Set's the current/new scene.
     * @param scene 
     */
    public final void setScene(ServerScene scene){
        this.sceneActive = scene;
    }
    
    /**
     * Returns a map with the storage set containing data names and types to be stored.
     * @return 
     */
    @Override
    public final Map<String,List<String>> getStorageSet(){
        return storageSet;
    }

    /**
     * Just in case a device needs to prepare stuff before any routines are started.
     * This runs before startupdevice.
     * @param firstPrepare 
     */
    @Override
    public final void prepare(boolean firstPrepare) {
        deviceHandle.prepare(firstPrepare);
    }

    
    /**
     * The result of the previous send command if a command does return a value based on the send command.
     * @return String command result.
     */
    @Override
    public final boolean commandResult(){
        return commandResult;
    }

    /**
     * Sets the driver where the data from the device should go to.
     * @param driverClass 
     */
    @Override
    final public synchronized void setDriverListener(PeripheralSoftwareDriverInterface driverClass){
        driverConnect = driverClass;
        driverDriverName = driverClass.getClass().getName().substring(0, driverClass.getClass().getName().lastIndexOf("."));
        LOG.debug("Added driver listener: {}", driverClass.getClass().getName());
    }

    /**
     * Removes the driver where the data should go to.
     * @param device 
     */
    @Override
    final public synchronized void removeDriverListener(DeviceInterface device){
        if(device instanceof DeviceDriverListener){
            try {
                driverConnect.removeDeviceListener((DeviceDriverListener)device);
            } catch (Exception ex) {}
        }
        LOG.debug("Removed driver listener: {}", driverConnect.getClass().getName());
        driverConnect = null;
    }

    /**
     * Adds a listener for data storage pusposes.
     * @param l 
     */
    @Override
    public final void addDeviceDataStorageListener(DeviceDataStoreListener l){
        if(!_deviceDataStoreListeners.contains(l)) _deviceDataStoreListeners.add(l);
    }
    
    /**
     * Removes a listener from the data storage.
     * @param l 
     */
    @Override
    public final void removeDeviceDataStorageListener(DeviceDataStoreListener l){
        if(_deviceDataStoreListeners.contains(l)) _deviceDataStoreListeners.remove(l);
    }
    
    /**
     * Adds a listener for listening to incoming data dispatched by the device (not to the driver)
     * @param l 
     */
    @Override
    public final void addDeviceDataListener(DeviceDataListener l){
        if(!_deviceDataListeners.contains(l)) _deviceDataListeners.add(l);
    }

    /**
     * Removes an incoming data listener.
     * @param l 
     */
    @Override
    public final void removeDeviceDataListener(DeviceDataListener l){
        if(_deviceDataListeners.contains(l)) _deviceDataListeners.remove(l);
    }

    /**
     * Returns the devices location information.
     * @param location
     * @return 
     */
    @Override
    public final Map<String,String> getLocation(String location){
        if(deviceLocations.containsKey(location)){
            return deviceLocations.get(location);
        } else {
            return new HashMap<>();
        }
    }
    
    /**
     * Returns a map with device locations
     * @return 
     */
    @Override
    public final Map getDeviceLocations(){
        return deviceLocations;
    }

    /**
     * Returns the attached driver.
     * @return the driver name (as package).
     */
    @Override
    final public String getDriverName(){
        return driverDriverName;
    }

    /**
     * Returns the package name of the driver.
     * Drivers are always defined with they're package names.
     * @return The package name (without the class name itself)
     */
    @Override
    public String getName(){
        return me;
    }

    /**
     * When a device has an receiver built in (receiving data at a fixed interval) this will be ran.
     */
    @Override
    public void startReceivers(){
        for(DeviceControlsGroup group:this.getFullCommandSet().getControlsGroups().values()){
            for(DeviceControl control:group.getGroupControls().values()){
                if(control.hasTimeOutSet()){
                    control.startTimeOutScheduler();
                }
            }
        }
    }
    
    /**
     * Routines for stopping any data receiver threads.
     */
    @Override
    public void stopReceivers(){
        for(DeviceControlsGroup group:this.getFullCommandSet().getControlsGroups().values()){
            for(DeviceControl control:group.getGroupControls().values()){
                if(control.hasTimeOutSet()){
                    control.startTimeOutScheduler();
                }
            }
        }
        if(scheduledServiceExecutor!=null){
            scheduledServiceExecutor.shutdownNow();
        }
    }

    /**
     * Returns a list of data receiver sets and commands.
     * @return 
     */
    public final List<DeviceControlsSet.IntervalCommand> getReceiverSet(){
        return deviceStructure.getControlsSet().getReceiverSet();
    }
    
    /**
     * Returns device options if set.
     * @return 
     */
    @Override
    public final DeviceStructure.DeviceOptions getDeviceOptions(){
        return deviceOptions;
    }
    
    /**
     * Sets a device id
     * @param deviceId 
     */
    @Override
    public final void setId(int deviceId){
        this.deviceId = deviceId;
    }

    /**
     * Sets the modifiers on the controls
     * @param modifiersSet 
     */
    public final void setModifiers(List<Map<String,Object>> modifiersSet){
        for(Map<String,Object> item:modifiersSet){
            if(item.containsKey("group") && item.containsKey("control") && item.containsKey("id")){
                try {
                    int modifierId = ((Number)item.get("id")).intValue();
                    this.getFullCommandSet().getControlsGroup((String)item.get("group")).getDeviceControl((String)item.get("control")).attachModifier(modifierId);
                } catch (DeviceControlsGroupException | DeviceControlException | NumberFormatException ex) {
                    LOG.error("Could not set modifier: {}", ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Sets the id of the installed device.
     * @param installedId 
     */
    @Override
    public final void setInstalledDeviceId(int installedId){
        this.installedId = installedId;
    }

    /**
     * Sets the driver name of the device.
     * @param driverName 
     */
    @Override
    public final void setDeviceDriver(String driverName){
        this.driverName = driverName;
    }
    
    /**
     * Sets the device friendly name
     * @param friendlyName 
     */
    @Override
    public final void setFriendlyName(String friendlyName){
        this.friendlyName.set(friendlyName);
    }
    
    /**
     * Sets the location id of this device.
     * @param locationId 
     */
    @Override
    public final void setLocationId(int locationId){
        this.locationId.set(locationId);
    }
    
    /**
     * Sets the textual representation of the device address.
     * @param deviceAddress 
     */
    @Override
    public final void setAddress(String deviceAddress){
        this.deviceStructure.getAddress().setAddress(deviceAddress);
    }
    
    /**
     * Sets the base name of the device.
     * @param deviceName 
     */
    @Override
    public final void setDeviceName(String deviceName){
        this.deviceName.set(deviceName);
    }
    
    /**
     * Sets the category this device belongs to.
     * @param categoryId 
     */
    @Override
    public final void setCategoryId(int categoryId){
        this.categoryId.set(categoryId);
    }
    
    /**
     * Sets if the device is fixed.
     * @param fixed 
     */
    @Override
    public final void setIsFixed(boolean fixed){
        this.fixed = fixed;
    }
    
    /**
     * Sets what type of device it is.
     * @param deviceType 
     */
    @Override
    public final void setDeviceType(int deviceType){
        this.deviceType = deviceType;
    }
    
    /**
     * Sets if the device is active.
     * @param active 
     */
    @Override
    public final void setisActive(boolean active){
        this.active.set(active);
    }
    
    /**
     * Sets if it is a favorite device.
     * @param favorite 
     */
    @Override
    public final void setIsFavorite(boolean favorite){
        this.favorite.set(favorite);
    }
    
    /**
     * Set the category name.
     * @param name 
     */
    @Override
    public void setCategoryName(String name){
        this.categoryName.set(name);
    }
    
    /**
     * Sets the category constant.
     * @param constant 
     */
    @Override
    public void setCategoryConstant(String constant){
        this.categoryConstant.set(constant);
    }
    
    /**
     * Sets the location name.
     * @param name 
     */
    @Override
    public void setLocationName(String name){
        this.locationName.set(name);
    }
    
    /**
     * Gets a device id
     * @return 
     */
    @Override
    public int getId(){
        return this.deviceId;
    }
    
    /**
     * Gets the id of the installed device.
     * @return 
     */
    @Override
    public int getInstalledDeviceId(){
        return this.installedId;
    }
    
    /**
     * Gets the driver name of the device.
     * @return 
     */
    @Override
    public String getDeviceDriver(){
        return this.driverName;
    }
    
    /**
     * Gets the device friendly name
     * @return 
     */
    @Override
    public String getFriendlyName(){
        return this.friendlyName.getValueSafe();
    }
    
    /**
     * Gets the location id of this device.
     * @return 
     */
    @Override
    public int getLocationId(){
        return this.locationId.get();
    }
    
    /**
     * Gets the textual representation of the device address.
     * @return 
     */
    public DeviceAddressing getAddressing(){
        return this.deviceStructure.getAddress();
    }
    
    /**
     * Gets the textual representation of the device address.
     * @return 
     */
    @Override
    public String getAddress(){
        return String.valueOf(this.deviceStructure.getAddress().getAddress());
    }
    
    /**
     * Gets the base name of the device.
     * @return 
     */
    @Override
    public String getDeviceName(){
        return this.deviceName.getValueSafe();
    }
    
    /**
     * Gets the category this device belongs to.
     * @return 
     */
    @Override
    public int getCategoryId(){
        return this.categoryId.get();
    }
    
    /**
     * Gets if the device is fixed.
     * @return 
     */
    @Override
    public boolean getIsFixed(){
        return this.fixed;
    }
    
    /**
     * Gets what type of device it is.
     * @return 
     */
    @Override
    public int getDeviceType(){
        return this.deviceType;
    }
    
    /**
     * Gets if the device is active.
     * @return 
     */
    @Override
    public boolean getisActive(){
        return this.active.get();
    }
    
    /**
     * Gets if it is a favorite device. 
     * @return 
     */
    @Override
    public boolean getIsFavorite(){
        return this.favorite.get();
    }
    
    /**
     * Get the category name.
     * @return 
     */
    @Override
    public String getCategoryName(){
        return this.categoryName.getValueSafe();
    }
    
    /**
     * Returns the constant name of the device category.
     * @return 
     */
    @Override
    public String getCategoryConstant(){
        return this.categoryConstant.getValueSafe();
    }

    /**
     * Get the location name.
     * @return 
     */
    @Override
    public String getLocationName(){
        return locationName.getValueSafe();
    }
    
    /**
     * Returns the full command set collection.
     * @return 
     */
    @Override
    public final DeviceControlsSet getFullCommandSet(){
        return deviceStructure.getControlsSet();
    }
    
    /**
     * Returns a single device command control.
     * @param setId
     * @return 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    @Override
    public final DeviceControl getDeviceCommandSet(String setId) throws DeviceControlException {
        for(DeviceControlsGroup group:deviceStructure.getControlsSet().getControlsGroups().values()){
            return group.getDeviceControl(setId);
        }
        throw new DeviceControlException("Control id '"+setId+"' does not exist");
    }
    
    /**
     * Returns the device's barebone name, aka class package name.
     * @return 
     */
    public final String getBareboneName(){
        return deviceStructure.getCanonicalBaseName();
    }
    
    /**
     * Sets the name of the driver of the device (not the device driver self).
     * @param driverDriverName 
     */
    @Override
    public final void setDeviceDriverDriver(String driverDriverName){
        this.driverDriverName = driverDriverName;
    }

    /**
     * Sets the data retention file.
     * Storage in this file is depending on the device control.
     * @param retFile 
     */
    @Override
    public final void setRetentionFile(RetentionHandler retFile){
        this.retFile = retFile;
    }
    
    /**
     * Stores the retention data.
     */
    @Override
    public final void storeRetentionData(){
        this.retFile.storeData();
    }

    /**
     * Gets the name of the driver of the device (not the device driver self).
     * @return 
     */
    @Override
    public String getDeviceDriverDriver(){
        return this.driverDriverName;
    }

    /**
     * Returns the last time this device has received data.
     * @return 
     */
    @Override
    public final String getLastReceiveTime(){
        return this.lastReceiveTime;
    }

    /**
     * returns the last time this device has send some data.
     * @return 
     */
    @Override
    public final String getLastSendTime(){
        return this.lastSendTime;
    }

    /**
     * Function to be ran when a device needs to be shut down.
     * This is executed after everything has been disabled data wise so you can safely stop any running code.
     */
    @Override
    public void shutdownDevice() {
        for(DeviceControlsGroup group:this.getFullCommandSet().getControlsGroups().values()){
            for(DeviceControl control:group.getGroupControls().values()){
                if(control.hasModifier()){
                    try {
                        DataModifierPlugin plugin = DataModifierPluginService.getInstance().getPlugin((control.getModifierId()));
                       control.removeModifierListener(plugin);
                    } catch (PluginException ex) {
                        LOG.warn("Could not fetch modifier({}) for control {}", control.getModifierId(), control.getControlId());
                    }
                }
            }
        }
        this.deviceHandle.shutdownDevice();
        if(deviceHandle instanceof DeviceScheduler){
            if(scheduledServiceExecutor != null){
                scheduledServiceExecutor.shutdownNow();
            }
        }
    }

    /**
     * Function to be ran when a device starts.
     * This is executed before a device can handle any data.
     */
    @Override
    public void startupDevice() {
        this.deviceHandle.getFullCommandSet().setDevice(deviceHandle);
        for(DeviceControlsGroup group:this.getFullCommandSet().getControlsGroups().values()){
            for(DeviceControl control:group.getGroupControls().values()){
                if(control.hasModifier()){
                    try {
                        DataModifierPlugin plugin = DataModifierPluginService.getInstance().getPlugin(control.getModifierId());
                        control.setModifierListener(plugin);
                        LOG.info("Connected modifier {} to device {} with controlid {}", plugin.getPluginName(), this.getDeviceName(), control.getControlId());
                    } catch (PluginException ex) {
                        LOG.warn("Could not fetch modifier({}) for control {}", control.getModifierId(), control.getControlId());
                    }
                }
            }
        }
        this.deviceHandle.startupDevice();
        if(deviceHandle instanceof DeviceScheduler){
            ((DeviceScheduler)deviceHandle).setScheduledItems();            
        }
    }
    
    /**
     * Handles the data from a driver.
     * @param data
     * @param object
     */
    @Override
    public final void handleDataFromDriver(String data, Object object ){
        updateReceive();
        ((Runnable)() -> { deviceHandle.handleData(data, object); }).run();
    }
    
    /**
     * Dispatches device data with command/result as string.
     * @param cmdGroup
     * @param cmdSet
     * @param deviceCommand 
     */
    private void storeDeviceData(String cmdGroup, String cmdSet){
        LOG.trace("Called storeDeviceData: {}, {}", cmdGroup, cmdSet);
        try {
            DeviceControl control = this.getFullCommandSet().getControlsGroup(cmdGroup).getDeviceControl(cmdSet);
            if(storageSet.containsKey(cmdGroup) && storageSet.get(cmdGroup).contains(cmdSet)){
                switch(control.getDataType()){
                    case FLOAT:
                    case INTEGER:
                        dispatchDataStorage(cmdGroup,cmdSet, control.getValueData());
                    break;
                }
            }
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Data could not be send because of: {}", ex.getMessage());
        }
    }
    
    /**
     * updates last receive time.
     */
    @Override
    public final void updateReceive(){
        lastReceiveTime = SharedServerTimeService.getDateTimeConverted(new GregorianCalendar());
    }

    /**
     * updates last receive time.
     */
    public final void updateSend(){
        lastSendTime = SharedServerTimeService.getDateTimeConverted(new GregorianCalendar());
    }
    
    /**
     * Dispatches data to be stored, always as double.
     * @param dataGroup
     * @param dataName
     * @param data 
     */
    private void dispatchDataStorage(String dataGroup, String dataName, Object data){
        storageService.execute(() -> {
            synchronized(_deviceDataStoreListeners){
                Iterator driverListeners = _deviceDataStoreListeners.iterator();
                while( driverListeners.hasNext() ){
                     ( (DeviceDataStoreListener) driverListeners.next() ).handleDeviceStoreData(getId(), dataGroup, dataName, data);
                }
            }
        });
    }
    
    /**
     * Check if the last device command is the same as the previous one for the group and set.
     * @param cmdGroup
     * @param cmdSet
     * @param deviceCommand
     * @return 
     */
    private boolean lastCmdSetIsSame(String cmdGroup, String cmdSet, Object deviceCommand){
        try {
            return deviceStructure.getControlsSet().getControlsGroup(cmdGroup).getDeviceControl(cmdSet).valueIsSame(deviceCommand);
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            return false;
        }
    }
    
    /**
     * Used to create device structure.
     * This sets the controls, the options, device address and data storage.
     * @param structSet 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException 
     */
    @Override
    public final void createDeviceComponentsSet(String structSet) throws UnsupportedDeviceException {
        createDeviceComponentsSet(structSet, true);
    }
    
    /**
     * Used to create device structure.
     * This sets the controls, the options, device address and data storage.
     * @param structSet 
     * @param saveSet set to true to store the created device structure in the device structure store for other exact same devices.
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException 
     */
    public final void createDeviceComponentsSet(String structSet, boolean saveSet) throws UnsupportedDeviceException {
        try {
            if(!DeviceStructureStore.hasDeviceStruct(installedId)){
                LOG.debug("Creating struct for: {}", installedId);
                DeviceSkeletonJSONTransformer transform = new DeviceSkeletonJSONTransformer(structSet);
                transform.compose();
                if(saveSet) {
                    DeviceStructureStore.storeStruct(installedId, transform.get());
                    deviceStructure.createStructure(DeviceStructureStore.getDeviceStruct(installedId));
                } else {
                     deviceStructure.createStructure(transform.get());
                }
            } else {
                deviceStructure.createStructure(DeviceStructureStore.getDeviceStruct(installedId));
            }
            deviceOptions = deviceStructure.getOptions();
            for(DeviceControlsGroup group:deviceStructure.getControlsSet().getControlsGroups().values()){
                for(DeviceControl control:group.getGroupControls().values()){
                    if(control.hasRetention()){
                        try {
                            Object lastKnownValue = this.retFile.getRetentionData(group.getGroupId(), control.getControlId());
                            if(lastKnownValue!=null){
                                if(control.getDataType().equals(DeviceControlDataType.COLOR)){
                                    String lastData = (String)lastKnownValue;
                                    Map<String,Object> colorSet = new HashMap<>();
                                    try {
                                        if(lastData.startsWith("kelvin:")){
                                            colorSet.put("kelvin", Integer.valueOf(lastData.split(":")[1]));
                                        } else {
                                            String[] splittedHsb = lastData.split(",");
                                            colorSet.put("h", Float.valueOf(splittedHsb[0]));
                                            colorSet.put("s", Float.valueOf(splittedHsb[1]));
                                            colorSet.put("b", Float.valueOf(splittedHsb[2]));
                                        }
                                    } catch (Exception ex){
                                        //// Not correctly stored or no initial data.
                                        colorSet.put("h", 0.0F);
                                        colorSet.put("s", 0.0F);
                                        colorSet.put("b", 0.0F);
                                    }
                                } else {
                                    control.setLastKnownValue(lastKnownValue);
                                }
                            }
                        } catch (Exception ex){
                            LOG.warn("Control {} has retention set, but no data is present: {}", control.getControlId(), ex.getMessage());
                        }
                    }
                    if(control.getControlType().equals(DeviceControlType.DATA) && 
                        (control.getDataType().equals(DeviceControlDataType.FLOAT) || control.getDataType().equals(DeviceControlDataType.INTEGER))){
                        if(((DeviceDataControl)control).hasGraph()){
                            if(storageSet.containsKey(group.getGroupId())){
                                storageSet.get(group.getGroupId()).add(control.getControlId());
                            } else {
                                List<String> list = new ArrayList<>();
                                list.add(control.getControlId());
                                storageSet.put(group.getGroupId(), list);
                            }
                            storeData = true;
                        }
                    }
                }
            }
        } catch (IllegalDeviceActionException ex) {
            LOG.error(ex);
            throw new UnsupportedDeviceException("An illegal device action has been performed: " + ex.getMessage() + ", because of: " + ex.getCause().getMessage());
        } catch (DeviceSkeletonException ex) {
            throw new UnsupportedDeviceException("Device composition failed: " + ex.getMessage());
        }
    }
    
    /**
     * Returns true when data is supposed to be stored.
     * Convenience method for so specific classes and routines will not be loaded when false.
     * @return 
     */
    @Override
    public final boolean hasStorageSet(){
        return storeData;
    }
    
    /**
     * Get the last stored commando set for this device.
     * @return 
     */
    @Override
    public final Map<String,Map<String,Object>> getStoredCmdSet(){
        Map<String,Map<String,Object>> lastCmdSet = new HashMap<>();
        for(DeviceControlsGroup group:deviceStructure.getControlsSet().getControlsGroups().values()){
            Map<String,Object> values = new HashMap<>();
            for(DeviceControl control:group.getGroupControls().values()){
                values.put(control.getControlId(), control.getValue());
            }
            lastCmdSet.put(group.getGroupId(), values);
        }
        return lastCmdSet;
    }
    
    /**
     * Returns the xml explaining the device setup.
     * @return 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException 
     */
    @Override
    public final String getDeviceXml() throws UnsupportedDeviceException {
        return "";
        ///return deviceStructure.getDeviceXml(internalXMLReference);
    }
    
    /**
     * Dispatches device data to the corresponding driver.
     * @param group
     * @param set
     * @param data 
     */
    @Override
    final public void dispatchToDriver(String group, String set, String data){
        LOG.trace("Data to be dispatched to driver: {}, {}, {}", group, set, data);
        try {
            commandResult = ( (PeripheralSoftwareDriverInterface) driverConnect ).handleDeviceData(deviceHandle, group, set, data );
        } catch (IOException ex) {
            LOG.error("problem dispatching data by driver: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Dispatches data to a driver as is!
     * @param request 
     */
    @Override
    final public void dispatchToDriver(DeviceCommandRequest request) {
        try {
            commandResult = ( (PeripheralSoftwareDriverInterface) driverConnect ).handleDeviceData(deviceHandle, request);
        } catch (IOException ex) {
            LOG.error("problem dispatching data by driver: {}", ex.getMessage(), ex);
        }
    }

    /**
     * Notifies the clients of the last set device values. 
     * storeCmdSet should me merge into this.
     * @param notification The notification command group and controls set.
     */
    @Override
    public final void notifyClients(final DeviceNotification notification){
        notifyClients(notification, true);
    }
    
    /**
     * Notifies the clients of the last set device values. 
     * storeCmdSet should me merge into this.
     * @param notification The notification command group and controls set.
     * @param userIntent If this is an action done by an user or not.
     */
    @Override
    public final void notifyClients(final DeviceNotification notification, boolean userIntent){
        List<DeviceNotification.GroupData> notificationStruct = notification.getStruct();
        List<Map<String,Object>> notificationStructSet = new ArrayList<>();
        for(int i = 0; i < notification.getStruct().size(); i++){
            final DeviceNotification.GroupData notificationGroup = notificationStruct.get(i);
            
            Map<String,Object> groupMap = new HashMap<String,Object>(2){{
                put("groupid", notificationGroup.getId());
                put("controls", new HashMap<>());
            }};
            LOG.debug("Adding groupmap '{}' to notification set", groupMap);
            notificationStructSet.add(groupMap);
            
            for(int j = 0; j < notificationGroup.getControlData().size();j++){
                final DeviceNotification.ControlData notificationControl = notificationGroup.getControlData().get(j);
                
                if(notificationControl.getForced() == false){
                    LOG.debug("Current notification control item is NOT FORCED to send with control id '{}' and value: '{}'", notificationControl.getId(),notificationControl.getValue());
                    boolean lastCommandIsSame = !lastCmdSetIsSame(notificationGroup.getId(), notificationControl.getId(),notificationControl.getValue());
                    LOG.debug("The last value is the same as the previous value: {} (if true it is not send)", lastCommandIsSame);
                    if(!lastCmdSetIsSame(notificationGroup.getId(), notificationControl.getId(),notificationControl.getValue())){
                         ((Map<String,Object>)groupMap.get("controls")).put(notificationControl.getId(), notificationControl.getValue());
                    }
                } else {
                    LOG.debug("Current notification control item is FORCED to send with control id '{}' and value: '{}'", notificationControl.getId(),notificationControl.getValue());
                     ((Map<String,Object>)groupMap.get("controls")).put(notificationControl.getId(), notificationControl.getValue());
                }
                
                handleDataInternal(notificationGroup.getId(), notificationControl.getId(),notificationControl.getValue());
                
                dataHandlingService.execute(() -> {
                    Iterator driverListeners = _deviceDataListeners.iterator();
                    while( driverListeners.hasNext() ){
                        ((DeviceDataListener)driverListeners.next()).handleDeviceData(deviceHandle, 
                                                                                      notificationGroup.getId(), 
                                                                                      notificationControl.getId(), 
                                                                                      notificationControl.getValue() );
                    }
                });
            }
            LOG.debug("Notification group '{}', is empty: {} (if true, removed from send list)", notificationGroup.getId(), ((Map<String,Object>)groupMap.get("controls")).isEmpty());
            if(((Map<String,Object>)groupMap.get("controls")).isEmpty()){
                notificationStructSet.remove(groupMap);
            }
        }
        LOG.debug("Current notification structure to be send: {}", notificationStructSet);
        if(!notificationStructSet.isEmpty()){
            notificationService.execute(() -> {
                LOG.debug("Handing over notification structure to all device data listeners (amount: {}) ", _deviceDataListeners.size());
                Iterator driverListeners = _deviceDataListeners.iterator();
                while( driverListeners.hasNext() ){
                    ((DeviceDataListener)driverListeners.next()).handleDeviceNotificationData(deviceHandle, notificationStructSet);
                }
            });
        }
    }
    
    /**
     * Notifies the clients of the last command. 
     * storeCmdSet should me merge into this.
     * @param cmdGroup
     * @param cmdSet
     * @param deviceCommand
     * @param forceSend
     */
    private void handleDataInternal(String cmdGroup, String cmdSet, Object deviceCommand){
        DeviceControl deviceControl;
        try {
            deviceControl = getFullCommandSet().getControlsGroup(cmdGroup).getDeviceControl(cmdSet);
            deviceControl.setLastKnownValue(deviceCommand);
            if(deviceControl.hasRetention()){
                if(deviceControl.getDataType().equals(DeviceControlDataType.COLOR)){
                    if(((DeviceColorPickerControl)deviceControl).getKelvin()!=0){
                        retFile.setRetentionData(cmdGroup, cmdSet, new StringBuilder("kelvin:").append(((DeviceColorPickerControl)deviceControl).getKelvin()).toString());
                    } else {
                        Map<String,Double> colorData = ((DeviceColorPickerControl)deviceControl).getHSB();
                        retFile.setRetentionData(cmdGroup, cmdSet, new StringBuilder(colorData.get("h").toString()).append(",").append(colorData.get("s").toString()).append(",").append(colorData.get("b").toString()).toString());
                    }
                } else {
                    try {
                       retFile.setRetentionData(cmdGroup, 
                                                cmdSet, 
                                                deviceCommand.toString());
                    } catch (Exception ex){
                        LOG.warn("Error using retention: {} ({}, {}, {})", ex.getMessage(), cmdGroup, cmdSet, deviceCommand, ex);
                        /// Needs further investigation.
                    }
                }
            }
            storeDeviceData(cmdGroup,cmdSet);
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Could not update control data: {}", ex.getMessage());
        }
    }

    @Override
    public int getDefinitionSequence() {
        return definitionSequence;
    }

    @Override
    public void setDefinitionSequence(int sequence) {
        definitionSequence = sequence;
    }
    
}