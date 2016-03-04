/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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

package org.pidome.server.connector.drivers.peripherals.software;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceDriverListener;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroups;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;

/**
 *
 * @author John Sirach
 */
public abstract class PeripheralSoftwareDriver {

    Logger LOG;
    
    protected final String driverName = this.getClass().getName();
    private String friendlyName;
    protected String lastCommand = null;

    Boolean running = false;
    
    private int dbId;
    
    /**
     * Internal used batch list.
     */
    final private Map<String, List<List<Map<String,String>>>> batchList = new HashMap<>();    
    /**
     * Time out for every batched command.
     */
    private int batchTimeOut = 500;
    
    /**
     * The peripheral hardware driver.
     */
    PeripheralHardwareDriverInterface peripheralHardwareDriver;
    
    /**
     * There can be only one :).
     */
    private static DataNotificationListener notificationListener;
    
    /**
     * Listener list for devices to receive data.
     */
    private final List<DeviceDriverListener> _deviceListeners = Collections.synchronizedList(new ArrayList<>());
    
    private final WorkQueue hwSendQueue = new WorkQueue();
    
    private String definedSoftwareDriverId = "";
    private String definedSoftwareDriverIdVersion = "";
    
    private PeripheralDriverDeviceMutationInterface deviceServiceLink;
    
    private final WebPresentationGroups presentation = new WebPresentationGroups();
    
    WebPresentationGroup present      = new WebPresentationGroup("Hardware link", "Information about the hardware used by this driver");
    WebPresentSimpleNVP  hardware     = new WebPresentSimpleNVP("Hardware");
    WebPresentSimpleNVP  port         = new WebPresentSimpleNVP("Hardware port");
    
    private TimerTask discoveryTimer;
    private int discoveryTime;
    private boolean scanActive = false;
    private boolean oneShot = false;
    
    private String namedId = "";
    
    private boolean supportsCustom = false;
    
    /**
     * Constructor.
     */
    public PeripheralSoftwareDriver(){
        LOG = LogManager.getLogger(this.getClass().getName());
        present.add(hardware);
        present.add(port);
        addWebPresentationGroup(present);
    }
    
    /**
     * Sets the DB Id.
     * @param driverDBId 
     */
    public void setId(int driverDBId){
        this.dbId = driverDBId;
    }
    
    /**
     * Sets the named id of the driver.
     * @param namedId 
     */
    public final void setNamedId(String namedId){
        this.namedId = namedId;
    }
    
    /**
     * returns the named id of the driver.
     * @return 
     */
    public final String getNamedId(){
        return this.namedId;
    }
    
    /**
     * Returns the DB id.
     * @return 
     */
    public final int getId(){
        return this.dbId;
    }
    
    /**
     * Returns if the driver supports custom devices.
     * @return 
     */
    public boolean hasCustom(){
        return this.supportsCustom;
    }
    
    /**
     * Set if a driver has custom devices.
     * @param hasCustom
     * @return 
     */
    public void setHasCustom(boolean hasCustom){
        this.supportsCustom = hasCustom;
    }
    
    /**
     * Sets the software driver id.
     * @param softwareId
     */
    public void setSoftwareDriverId(String softwareId){}

    /**
     * Sets the version set in the driver.
     * @param softwareIdVersion
     */
    public void setSoftwareDriverVersion(String softwareIdVersion){}

    /**
     * Sets the friendlyname.
     * @param name 
     */
    public final void setFriendlyName(String name){
        if(friendlyName==null){
            friendlyName = name;
        }
    }
    
    /**
     * Returns the friendlyname.
     * @return 
     */
    public final String getFriendlyName(){
        return this.friendlyName;
    }
    
    /**
     * Returns true if there is a web presentation present.
     * @return 
     */
    public final boolean hasPresentation(){
        return !presentation.getList().isEmpty();
    }
    
    /**
     * Sets a web presentation;
     * @param pres 
     */
    public final void addWebPresentationGroup(WebPresentationGroup pres){
        this.presentation.getList().add(pres);
    }
    
    /**
     * Returns the web presentation.
     * @return 
     */
    public final List<WebPresentationGroup> getWebPresentationGroups(){
        return this.presentation.getList();
    }
    
    /**
     * Returns the software driver id.
     * @return 
     */
    public String getSoftwareDriverId(){
        return definedSoftwareDriverId;
    }

    /**
     * Returns the version set in the driver.
     * @return 
     */
    public String getSoftwareDriverVersion(){
        return definedSoftwareDriverIdVersion;
    }
    
    /**
     * Returns a list of running devices whom belong to this driver.
     * @return 
     */
    public final List<Device> getRunningDevices(){
        List<Device> returnList = new ArrayList<>();
        synchronized(_deviceListeners) {
            _deviceListeners.stream().forEach((device) -> {
                returnList.add(device.getDevice());
            });
        }
        return returnList;
    }
    
    /**
     * Returns the amount of devices connected to this driver.
     * @return 
     */
    public final int getRunningDevicesCount(){
        return _deviceListeners.size();
    }
    
    /**
     * Returns the driver as the server expects it.
     * @return 
     */
    public final Peripheral getHardwareDriverFromSoftwareDriver(){
        return (Peripheral)peripheralHardwareDriver;
    }
    
    /**
     * Returns the hardware driver as a software driver expects it.
     * @return 
     */
    public final PeripheralHardwareDriverInterface getHardwareDriver(){
        return peripheralHardwareDriver;
    }
    
    /**
     * Handles data coming from an peripheral hardware driver, defaults throws an UnsupportedOperationException
     * @param oEvent
     * @throws UnsupportedOperationException When not overridden.
     */
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Handles data coming from an peripheral hardware driver, acts as the proxy
     * @param oEvent
     * @throws UnsupportedOperationException When not overridden.
     */
    public final void driverBaseDataReceivedProxy(PeripheralHardwareDataEvent oEvent) throws UnsupportedOperationException {
        notifyLedRcv();
        driverBaseDataReceived(oEvent);
    }
    
    /**
     * Adds a peripheral hardware driver.
     * @param l 
     */
    public final synchronized void setPeripheralEventListener(PeripheralHardwareDriverInterface l){
        LOG.debug("Added listener: {}", l.getClass().getName());
        peripheralHardwareDriver = l;
        hardware.setValue(peripheralHardwareDriver.getFriendlyName());
        port.setValue(peripheralHardwareDriver.getPort());
    }

    /**
     * Removes a peripheral hardware driver.
     * @param l 
     */
    public final synchronized void removePeripheralEventListener(PeripheralHardwareDriverInterface l){
        LOG.debug("Removed listener: {}", l.getClass().getName());
        peripheralHardwareDriver = null;
    }
    
    /**
     * Deletes a device from the device listing based on the device id.
     * This only is available for devices which can be controlled by this driver.
     * @param deviceId
     * @throws org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException 
     */
    public final void deleteDevice(int deviceId) throws PeripheralDriverDeviceMutationException {
        if(deviceServiceLink!=null){
            int deleteDevice = 0;
            for(Device device:getRunningDevices()){
                if(device.getId()==deviceId) deleteDevice = device.getId();
            }
            if(deleteDevice!=0){
                deviceServiceLink.removeDeviceByDriver(deleteDevice);
            } else {
                throw new PeripheralDriverDeviceMutationException("Device id '"+deviceId+"' is not assigned to this driver and not eligable for deletion.");
            }
        }
    }
    
    /**
     * Called when a device is loaded and which device it is.
     * Some drivers want to know which device is loaded so they are able to perform
     * actions on them if needed.
     * @param device 
     */
    public void deviceLoaded(Device device){}
    
    /**
     * Creates a device from an existing installed device.
     * @param deviceId installed device id.
     * @param name Device name
     * @param address Device address
     * @param location
     * @param subCategory Device sub category (linked to main category).
     * @throws PeripheralDriverDeviceMutationException 
     */
    public final void createFromExistingDevice(int deviceId, String name, String address, int location, int subCategory) throws PeripheralDriverDeviceMutationException{
        if(deviceServiceLink!=null){
            deviceServiceLink.addDeviceByDriver(deviceId, name, address, location, subCategory);
        } else {
            throw new PeripheralDriverDeviceMutationException("There is no server link to create device.");
        }
    }
    
    /**
     * Creates a device skeleton formed by an DeviceStructureCreator.
     * @param deviceStructureCreator The device skeleton creator.
     * @throws PeripheralDriverDeviceMutationException 
     */
    public final int createDeviceSkeleton(DiscoveredDevice deviceStructureCreator) throws PeripheralDriverDeviceMutationException {
        if(deviceServiceLink!=null){
            return deviceServiceLink.createDeviceSkeletonByDriver(this, deviceStructureCreator);
        } else {
            throw new PeripheralDriverDeviceMutationException("There is no server link to create device skeleton.");
        }
    }
    
    /**
     * Creates a device from an existing installed device with settings.
     * Creates a device with xml settings. Use DeviceStructure.OptionSettings to create these.
     * @param deviceId installed device id.
     * @param name Device name
     * @param address Device address
     * @param location
     * @param subCategory Device sub category (linked to main category).
     * @param settings
     * @throws PeripheralDriverDeviceMutationException 
     */
    public final void createFromExistingDevice(int deviceId, String name, String address, int location,  int subCategory, Map<String,Object> settings) throws PeripheralDriverDeviceMutationException{
        if(deviceServiceLink!=null){
            deviceServiceLink.addDeviceByDriver(deviceId, name, address, location, subCategory,settings);
        } else {
            throw new PeripheralDriverDeviceMutationException("There is no server link to create device.");
        }
    }
    
    /**
     * Overwrite this to use the raw device command.
     * @param device
     * @param request
     * @return
     * @throws IOException 
     */
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        return false;
    }
    
    /**
     * Device base peripheral dispatch, used for plugins.
     * @param device
     * @param group
     * @param set
     * @param command
     * @return 
     */
    public final boolean write(Device device, String group, String set, byte[] command){
        dispatchDataToHardwareDriver(device, group, set, command);
        return true;
    }
    
    /**
     * The driver implementation of writing to the device, used by the DriverService and parent
     * @param data The data to be send to the device as a string
     * @param prefix The prefix for a specific device, can be null
     * @return true on success, exception on false;
     */
    public final boolean write(String data, String prefix) throws IOException {
        if (running == true) {
            if (data.length() > 0) {
                if (prefix != null) {
                    data = prefix + data;
                }
                lastCommand = data;
                LOG.debug("sending: " + lastCommand);
                dispatchDataToHardwareDriver(lastCommand);
                return true;
            } else {
                LOG.error("DRIVER_DATA_EMPTY:No data to be send");
                throw new IOException("No driver loaded");
            }
        } else {
            LOG.error("DRIVER_NOT_SET:Driver never started");
            throw new IOException("Driver not started");
        }
    }
    
    /**
     * The driver implementation of writing to the device, used by the DriverService and parent
     * @param data The data to be send to the device as a string
     * @return true on success, exception on false;
     * @throws java.io.IOException
     */
    public final boolean writeBytes(byte[] data) throws IOException {
        if (running == true) {
            if (data.length > 0) {
                lastCommand = String.valueOf(data);
                LOG.debug("sending: " + lastCommand);
                dispatchDataToHardwareDriver(data);
                return true;
            } else {
                LOG.error("DRIVER_DATA_EMPTY:No data to be send");
                throw new IOException("No driver loaded");
            }
        } else {
            LOG.error("DRIVER_NOT_SET:Driver never started");
            throw new IOException("Driver not started");
        }
    }
    
    /**
     * Dispatches the data to the hardware driver.
     * @param data
     * @throws IOException 
     */
    final void dispatchDataToHardwareDriver(final String data) {
        try {
            dispatchDataToHardwareDriver( data.getBytes("US-ASCII") );
        } catch (UnsupportedEncodingException ex) {
            LOG.error("Data not send, can not decode string to bytes with US-ASCII: {}", ex.getMessage());
        }
    }
    
    /**
     * Dispatches the data to the hardware driver.
     * @param data
     * @throws IOException 
     */
    final void dispatchDataToHardwareDriver(final Device device, final String group, final String set, final byte[] data) {
        if(peripheralHardwareDriver!=null){
            hwSendQueue.execute(() -> {
                try {
                    Thread.sleep(100);
                    peripheralHardwareDriver.writePort(data);
                    notifyLedSnd();
                } catch (IOException ex) {
                    LOG.error("Data could not be send to hardware driver {}, reason: {}", peripheralHardwareDriver.getName(), ex.getMessage());
                } catch (InterruptedException ex) {
                    LOG.warn("Data has been send to hardware driver {} without threshold: {} ", peripheralHardwareDriver.getName(), ex.getMessage());
                }
            });
        }
    }
    
    /**
     * Dispatches the data to the hardware driver.
     * @param data
     * @throws IOException 
     */
    final void dispatchDataToHardwareDriver(final byte[] data) {
        if(peripheralHardwareDriver!=null){
            hwSendQueue.execute(() -> {
                try {
                    Thread.sleep(100);
                    peripheralHardwareDriver.writePort(data);
                    notifyLedSnd();
                } catch (IOException ex) {
                    LOG.error("Data could not be send to hardware driver {}, reason: {}", peripheralHardwareDriver.getName(), ex.getMessage());
                } catch (InterruptedException ex) {
                    LOG.warn("Data has been send to hardware driver {} without threshold: {} ", peripheralHardwareDriver.getName(), ex.getMessage());
                }
            });
        }
    }
    
    /**
     * Returns the Full Qualified Name of the driver.
     * @return The name
     */
    public final String getFQNName(){
        return driverName;
    }
    
    /**
     * Returns the package name of the driver as used in the xml's.
     * @return The package name of this class
     */
    public final String getPackageName(){
        return driverName.substring(0,driverName.lastIndexOf("."));
    }
    /**
     * Returns only the class name.
     * @return the class name
     */
    public final String getName(){
         return driverName.substring(driverName.lastIndexOf(".")+1);
    }
 
    /**
     * Just an internal setting to true;
     */
    public final void startDriver(){
        running = true;
        notifyLedSnd();
        notifyLedRcv();
        driverStart();
    }
    
    /**
     * Notifier for starting a driver.
     * Overwrite this function if you need a driver start initialization.
     */
    public void driverStart(){}
    
    /**
     * Blink send led.
     */
    public final void notifyLedSnd(){
        if (notificationListener!=null) notificationListener.notifyLedSnd();
    }
    
    /**
     * Blink receive led.
     */
    public final void notifyLedRcv(){
        if (notificationListener!=null) notificationListener.notifyLedRcv();
    }
    
    /**
     * Add a notificationListener.
     * @param listener 
     */
    public final static void addDataNotificationListener(DataNotificationListener listener){
        if (notificationListener==null) notificationListener = listener;
    }

    /**
     * Removes the notification listener.
     */
    public final void removeDataNotificationListener(){
        if (notificationListener!=null) notificationListener = null;
    }
    
    /**
     * Just an internal setting to false;
     */
    public final void stopDriver(){
        driverStop();
        running = false;
    }    
    
    /**
     * Notifier for stopping the driver.
     * If you need unloading, detaching, etc. overwrite this function and do it.
     */
    public void driverStop(){}
    
    ///////////////// Batch handling
    
    /**
     * Sets the timeout for batched commands (default is 1000 ms).
     * Every command send by the server is batched, the timeout specifies how many milliseconds
     * the driver should wait between the send commands to the devices
     * @param timeout Timeout in milliseconds to be set
     */
    public final void setBatchTimeOut(int timeout){
        LOG.info("Batch timout for {} set at: {}",driverName, timeout);
        batchTimeOut = timeout;
    }

    
    /**
     * Adds data to the internal batching system, defaults to batch name "default"
     * @param batchData List with Mappings with strings <type, content>
     */
    public final void addBatch(List<Map<String,String>> batchData){
        addBatch(batchData, "default");
    }

    /**
     * Adds data to the internal batching system
     * @param batchData Map with strings <command, prefix>
     * @param batchName The name of the batch to be used
     */
    public final void addBatch(List<Map<String,String>> batchData, String batchName) {
        if(batchList.containsKey(batchName)){
            batchList.get(batchName).add(batchData);
        } else {
            List<List<Map<String,String>>> curCommandList = new ArrayList<>();
            curCommandList.add(batchData);
            batchList.put(batchName, curCommandList);
        }
    }

    /**
     * Runs the batch with the name "default"
     */
    public final void runBatch() {
        runBatch("default");
    }

    /**
     * Runs the batch with the specified batchName
     * @param batchName Name of the butch to be run
     */
    public final void runBatch(final String batchName) {
        if(batchList.containsKey(batchName)){
            new Runnable() {
                @Override
                public void run() {
                    List<List<Map<String, String>>> curCommandList = batchList.get(batchName);
                    for (int i = 0, n = curCommandList.size(); i < n; i++) {
                        List<Map<String, String>> commandCollection = (List<Map<String, String>>) curCommandList.get(i);
                        Map<String, String> curCommand = (Map<String, String>) commandCollection.get(0);
                        try {
                            write(curCommand.get("data"), curCommand.get("prefix"));
                        } catch (IOException ex) {
                            LOG.error("Data could not be send to hardware driver: {}", ex.getMessage());
                        }
                    }
                    batchList.get(batchName).clear();
                }
            }.run();
        }
    }
    
    /**
     * Sends data to the devices.
     * @param data
     * @param object 
     */
    public void dispatchDataToDevices(String data, Object object){
        synchronized(_deviceListeners) {
            Iterator driverListeners = _deviceListeners.iterator();
            while( driverListeners.hasNext() ){
                ( (DeviceDriverListener)driverListeners.next() ).handleDataFromDriver( data, object );
            }
        }
    }
    
    /**
     * Adds a device listener.
     * @param device 
     */
    public synchronized void addDeviceListener(DeviceDriverListener device){
        synchronized(_deviceListeners) {
            _deviceListeners.add(device);
            LOG.debug("Added listener: {}",device.getClass().getName());
        }
    }
    
    /**
     * removes a device listener.
     * @param device 
     */
    public synchronized void removeDeviceListener(DeviceDriverListener device){
        synchronized(_deviceListeners) {
            _deviceListeners.remove(device);
            LOG.debug("Removed listener: {}",device.getClass().getName());
        }
    }
    
    /**
     * Sets a link with the device service.
     * @param deviceServiceLink 
     */
    public final void setDeviceServiceLink(PeripheralDriverDeviceMutationInterface deviceServiceLink){
        if(this.deviceServiceLink==null)this.deviceServiceLink = deviceServiceLink;
    }
    
    /**
     * Removes a device service link.
     */
    public final void removeDeviceServiceLink(){
        this.deviceServiceLink = null;
    }
    
    /**
     * Used for the workqueue sending device data to devices.
     */
    public class WorkQueue {
        
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        int scheduleTimeOut = 0;
        
        public WorkQueue(){}

        /**
         * Sets the timeout between commands.
         * @param amount 
         */
        protected final void setExecutionTimeOut(int amount){
            scheduleTimeOut = amount;
        }
        
        /**
         * Executes a command after a specific timeout.
         * @param r 
         */
        protected void execute(Runnable r) {
            service.schedule(r, scheduleTimeOut, TimeUnit.MILLISECONDS);
        }
        
        /**
         * Stops the queue.
         */
        protected final void shutdown(){
            service.shutdown();
        }
    }
    
    /**
     * Enable discovery.
     * This function checks if the driver is eligable to do discovery first and fails gracefully without notice.
     * @param period 
     * @throws org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException 
     */
    public final void enableDiscovery(final int period) throws TimedDiscoveryException {
        final DeviceDiscoveryInterface self = (DeviceDiscoveryInterface)this;
        this.discoveryTime = period;
        if(period==0){
            disableDiscovery();
        } else if (period==-1){
            DiscoveredItemsCollection.enableDiscovery(self, period);
        } else if (period ==-2){
            oneShot = true;
            DiscoveredItemsCollection.enableDiscovery(self, period);
        } else if (period==-3){
            DiscoveredItemsCollection.enableDiscovery(self, period);
            if (this instanceof DeviceDiscoveryScanInterface) {
                ((DeviceDiscoveryScanInterface)this).scanForNewDevices();
                this.scanActive = true;
            }
        } else {
            if (this instanceof DeviceDiscoveryInterface) {
                if (discoveryTimer == null) {
                    Timer timer = new Timer();
                    discoveryTimer = new TimerTask() {
                        @Override
                        public void run() {
                            disableDiscovery();
                        }
                    };
                    timer.schedule(discoveryTimer, period * 60000);
                }
                DiscoveredItemsCollection.enableDiscovery(self, period);
            }
        }
    }
    
    public final void disableDiscovery(){
        oneShot = false;
        this.discoveryTime = 0;
        this.scanActive = false;
        if (this instanceof DeviceDiscoveryInterface && discoveryTimer!=null) {
            if(discoveryTimer!=null){
                discoveryTimer.cancel();
                discoveryTimer = null;
            }
        } else if (this instanceof DeviceDiscoveryScanInterface) {
            ((DeviceDiscoveryScanInterface)this).stopScanForNewDevices();
        }
        DiscoveredItemsCollection.disableDiscovery((DeviceDiscoveryInterface)this);
    }
    
    /**
     * Returns the amount of discovery time set.
     * @return 
     */
    public final int getDiscoveryTime(){
        return this.discoveryTime;
    }
    
    public final int getDiscoveredAmount(){
        try {
            return DiscoveredItemsCollection.getDiscoveredDevices((WebPresentAddExistingDeviceInterface)this).size();
        } catch (DiscoveredDeviceNotFoundException ex) {
            //// Discovery is not enabled.
            return 0;
        }
    }
    
    /**
     * Returns true if discovery is enabled for this driver.
     * @return 
     */
    public final boolean discoveryIsEnabled(){
        return (discoveryTimer != null || scanActive == true || oneShot == true || discoveryTime == -1);
    }
    
}