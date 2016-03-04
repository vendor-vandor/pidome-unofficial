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

package org.pidome.server.system.hardware.devices;

import org.pidome.server.system.datastorage.RoundRobinDataStorage;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceDataListener;
import org.pidome.server.connector.drivers.devices.DeviceDataStoreListener;
import org.pidome.server.connector.drivers.devices.DeviceDriverListener;
import org.pidome.server.connector.drivers.devices.DeviceScheduler;
import org.pidome.server.connector.drivers.devices.DeviceStructure;
import org.pidome.server.connector.drivers.devices.IllegalDeviceActionException;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationException;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationInterface;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceDataControl;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem.FieldType;
import org.pidome.server.connector.plugins.hooks.DeviceDeliverHookListener;
import org.pidome.server.connector.plugins.hooks.DeviceHook;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.accesscontrollers.AccessControllerProxy;
import org.pidome.server.system.hardware.drivers.DriverEvent;
import org.pidome.server.system.hardware.drivers.DriverEventListener;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.categories.BaseCategories;
import org.pidome.server.system.categories.CategoriesException;
import org.pidome.server.system.datastorage.RoundRobinDataStorage.Source;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.hardware.drivers.Drivers;
import org.pidome.server.system.location.BaseLocations;
import org.pidome.server.system.location.LocationServiceException;
import org.pidome.server.system.packages.Packages;
import org.pidome.server.services.automations.rule.AutomationRulesVarProxy;
import org.pidome.server.services.hardware.DeviceServiceException;
import org.pidome.server.system.packages.PackagePermissionsNotUpToDateException;

/**
 * Yeah, mostly the real thing here, all end user device based stuff.
 * @author John Sirach
 */
public final class Devices extends Drivers implements DriverEventListener,DeviceDataListener,DeviceDataStoreListener,DeviceDeliverHookListener,PluginDeviceMutationInterface,PeripheralDriverDeviceMutationInterface {
    
    static Logger LOG = LogManager.getLogger(Devices.class);
    
    static List<String> deviceCollection = new ArrayList();
    static Map<Integer, DeviceInterface>declaredDevices = new HashMap<>();
    
    private Map<String, List<Map<DeviceInterface,DeviceCommandRequest>>>batchList = new HashMap<>();
    
    static Map<Integer, RoundRobinDataStorage>storageList = new HashMap<>();
    
    static Map<Integer,Map<String,String>> enabledDevices = new HashMap<>();
    
    static Devices myInstance;
    
    /**
     * Constructor starts drivers listener.
     * @throws org.pidome.server.system.hardware.devices.DevicesException
     */
    public Devices() throws DevicesException {
        super();
        set();
    }

    /**
     * Sets initial parameters and runs initial functions.
     */
    final void set() throws DevicesException {
        getAllEnabledDevices();
        addDriversListener(this);
        DeviceHook.setDeliverListener(this);
        myInstance = this;
    }
    
    /**
     * Returns a list of all enabled devices.
     * @return The list of all enabled devices as reported by the package manager
     * @throws org.pidome.server.system.hardware.devices.DevicesException
     * @see Packages
     */
    public Map<Integer,Map<String,String>> getAllEnabledDevices() throws DevicesException {
        enabledDevices.clear();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT d.id,d.friendlyname,d.driver,d.selectable,id.friendlyname as drivername,d.sequence "
                                                                       + "FROM installed_devices d "
                                                                  + "LEFT JOIN installed_drivers id ON d.driver=id.id"); 
            ResultSet rsDevices = prep.executeQuery()) {
            while (rsDevices.next()) {
                Map<String, String> device = new HashMap<>();
                device.put("friendlyname", rsDevices.getString("friendlyname"));
                device.put("driver", rsDevices.getString("driver"));
                device.put("drivername", rsDevices.getString("drivername"));
                device.put("selectable", rsDevices.getString("selectable"));
                device.put("defsequence", rsDevices.getString("sequence"));
                enabledDevices.put(rsDevices.getInt("id"), device);
            }
            rsDevices.close();
        } catch (SQLException ex) {
            LOG.error("Could not load installed devices from database: {}", ex.getMessage());
            throw new DevicesException("Could not load installed devices from database: " + ex.getMessage());
        }
        return enabledDevices;
    }
    
    /**
     * Get all devices created with the xml editor.
     * @return 
     */
    public final Map<Integer,Map<String,String>> getAllCustomDevices(){
        Map<Integer,Map<String,String>> devices = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT d.id,d.name,d.friendlyname,d.driver,d.driver_driver,d.created,d.modified,d.sequence,id.friendlyname as driver_friendlyname FROM installed_devices d LEFT JOIN installed_drivers id ON d.driver_driver=id.id WHERE d.type=1")) {
            prep.executeQuery();
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    Map<String, String> device = new HashMap<>();
                    device.put("id", rsDevices.getString("id"));
                    device.put("name", rsDevices.getString("name"));
                    device.put("friendlyname", rsDevices.getString("friendlyname"));
                    device.put("driver", rsDevices.getString("driver"));
                    device.put("driver_driver", rsDevices.getString("driver_driver"));
                    device.put("driver_friendlyname", rsDevices.getString("driver_friendlyname"));
                    device.put("created", rsDevices.getString("created"));
                    device.put("modified", rsDevices.getString("modified"));
                    device.put("defsequence", rsDevices.getString("sequence"));
                    devices.put(rsDevices.getInt("id"), device);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load installed custom devices from database: {}", ex.getMessage());
        }
        return devices;
    }
    
    /**
     * Get all devices created with the xml editor.
     * @return 
     */
    public final ArrayList<Map<String,Object>> getDeclaredCustomDevices(){
        ArrayList<Map<String,Object>> devices = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT d.id,d.name,d.friendlyname,d.driver,d.customdevicepath,d.package FROM installed_drivers d WHERE d.hascustom=1")) {
            prep.executeQuery();
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    Map<String, Object> device = new HashMap<>();
                    device.put("id", rsDevices.getInt("id"));
                    device.put("name", rsDevices.getString("name"));
                    device.put("friendlyname", rsDevices.getString("friendlyname"));
                    device.put("driver", rsDevices.getString("driver"));
                    device.put("customdevicepath", rsDevices.getString("customdevicepath"));
                    device.put("package", rsDevices.getInt("package"));
                    devices.add(device);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load available custom devices from database: {}", ex.getMessage());
        }
        return devices;
    }
    
    /**
     * Loads all the defined drivers in the database for the given peripheral software driver.
     * First the packages are asked if there is a collection of devices for this driver. These are then all returned.
     * @param driver The peripheral software driver.
     */
    void loadDriverDevices(PeripheralSoftwareDriverInterface driver){
        String driverName = driver.getPackageName();
        //// Load the main class to use
        List<String> driverDevices = getDevicesByDriverName(driverName);
        for(int i = 0, n = driverDevices.size(); i < n; i++) {
            String device = driverDevices.get(i);
            LOG.debug("Loading declared devices for device: {}", device);
            try {
                List<Integer> deviceIds = loadDeclaredDevicesByName(device);
                for (int j = 0, m = deviceIds.size(); j < m; j++) {
                    if(!declaredDevices.containsKey(deviceIds.get(j))){
                        DeviceInterface instance = loadSingleDeviceInstance(deviceIds.get(j));
                        declaredDevices.put(deviceIds.get(j), instance);
                    }
                }
            } catch (DevicesException ex) {
                LOG.error("Device '{}', not found in packages or instantiation error: {}", device, ex.getMessage());
            }
        }
    }
    
    /**
     * Retrieves all the device drivers from the database of a specific device class.
     * @param driverName
     * @return 
     */
    List<String> getDevicesByDriverName(String driverName){
        List<String> devices = new ArrayList();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT DISTINCT id.driver FROM installed_devices id WHERE id.driver_driver=(SELECT id FROM installed_drivers WHERE driver=? LIMIT 1)")) {
            prep.setString(1, driverName);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    devices.add(rsDevices.getString("driver"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        LOG.debug("Found devices: {}", devices);
        return devices;
    }
    
    /**
     * Retrieves info from all the devices from the database of a specific device class.
     * @param driverPath
     * @return 
     */
    public final List<Map<String,Object>> getInstalledDevicesInfoByDriverName(String driverPath){
        List<Map<String,Object>> devices = new ArrayList();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT DISTINCT id.id,id.friendlyname FROM installed_devices id WHERE id.driver_driver=(SELECT id FROM installed_drivers WHERE driver=? LIMIT 1)")) {
            prep.setString(1, driverPath);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    Map<String,Object> deviceInfo = new HashMap<>();
                    deviceInfo.put("id", rsDevices.getInt("id"));
                    deviceInfo.put("name", rsDevices.getString("friendlyname"));
                    devices.add(deviceInfo);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        LOG.debug("Found devices: {}", devices);
        return devices;
    }
    
    /**
     * Returns device drivers by peripheral software id.
     * @param driverId
     * @return 
     */
    public final List<Map<String,Object>> getInstalledDevicesByPeripheralSoftwareDriver(int driverId){
        List<Map<String,Object>> devices = new ArrayList();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT DISTINCT id.id,id.friendlyname FROM installed_devices id WHERE id.driver_driver=? AND id.selectable=1")) {
            prep.setInt(1, driverId);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    Map<String,Object> deviceInfo = new HashMap<>();
                    deviceInfo.put("id", rsDevices.getInt("id"));
                    deviceInfo.put("name", rsDevices.getString("friendlyname"));
                    devices.add(deviceInfo);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        LOG.debug("Found devices: {}", devices);
        return devices;
    }
    
    /**
     * Loads a single device instance
     * @param deviceId the device id as known or just set in the db
     * @return DeviceInterfaceImpl 
     * @throws org.pidome.server.system.hardware.devices.DevicesException 
     */
    public static DeviceInterface loadSingleDeviceInstance(int deviceId) throws DevicesException {
        if(!declaredDevices.containsKey(deviceId)){
            /// first load the device data
            Map<String,Object>deviceData = getDeclaredDevice(deviceId);
            String devicePackageName = (String)deviceData.get("device");
            LOG.debug("Loading device instance id: {}", deviceId);
            try {
                /// First load the device instance
                Device device = (Device)myInstance.getPackageLoader().loadDeviceDriver(deviceId).getConstructor().newInstance();
                DeviceStruct deviceDriver = new DeviceStruct(device);
                LOG.debug("Loaded device data: {}", deviceData);
                deviceDriver.setRetentionFile(new RetentionHandler("databases/devices/", deviceId));
                deviceDriver.setInstalledDeviceId((int)deviceData.get("devicebaseid"));
                deviceDriver.createDeviceComponentsSet(((String)deviceData.get("xml")).trim());
                deviceDriver.getDeviceOptions().set(new DeviceOptionsJSONTransformer(((String)deviceData.get("settings")).trim()).get());
                try {
                    deviceDriver.setModifiers((List<Map<String,Object>>)new JSONParser().parse((String)deviceData.get("modifiers")));
                } catch (ParseException ex) {
                    LOG.warn("Could not parse modifiers set: {}", ex.getMessage());
                }
                deviceDriver.setId((int)deviceData.get("id"));
                deviceDriver.setDeviceDriver((String)deviceData.get("driver"));
                deviceDriver.setFriendlyName((String)deviceData.get("friendlyname"));
                deviceDriver.setLocationId((int)deviceData.get("location"));
                deviceDriver.setAddress((String)deviceData.get("address"));
                deviceDriver.setDeviceName((String)deviceData.get("name"));
                deviceDriver.setCategoryId((int)deviceData.get("category"));
                deviceDriver.setDefinitionSequence((int)deviceData.get("defsequence"));
                try {
                    deviceDriver.setCategoryName((String)BaseCategories.getCategory((int) deviceData.get("category")).get("name"));
                } catch (CategoriesException ex) {
                    deviceDriver.setCategoryName("Unknown");
                }
                try {
                     deviceDriver.setCategoryConstant((String)BaseCategories.getCategory((int) deviceData.get("category")).get("constant"));
                } catch (CategoriesException ex) {
                     deviceDriver.setCategoryName("UNKNOWN");
                }
                try {
                    deviceDriver.setLocationName((String)BaseLocations.getLocation((int)deviceData.get("location")).get("name"));
                } catch (LocationServiceException ex) {
                    deviceDriver.setLocationName("Unknown");
                }
                deviceDriver.setIsFixed((boolean)deviceData.get("fixed"));
                deviceDriver.setDeviceDriverDriver((String)deviceData.get("driver_driver"));
                deviceDriver.setDeviceType((int)deviceData.get("type"));
                deviceDriver.setIsFavorite((boolean)deviceData.get("favorite"));
                deviceDriver.setisActive(true);

                /// We now get the driver instance so we can connect the listeners.
                PeripheralSoftwareDriverInterface driver = myInstance.getDriverInstanceFor(myInstance.getPackageLoader().getDevicePeripheralSoftwareDriver(deviceId));
                LOG.debug("Attaching: driver {} to device {}", driver.getName(), deviceDriver.getName());
                /// First the listener for the driver so it can receive data from the device.
                deviceDriver.setDriverListener(driver);
                //// Add the device to the quick check list
                if(!deviceCollection.contains(devicePackageName)){
                    deviceCollection.add(devicePackageName);
                }
                /// if the device is receive enabled by implementing the DeviceDriverListener, add the listener for the driver data.
                if(deviceDriver instanceof DeviceDriverListener){
                    driver.addDeviceListener((DeviceDriverListener)deviceDriver);
                }
                deviceDriver.addDeviceDataListener(myInstance);
                if(deviceDriver.hasStorageSet()){
                    deviceDriver.addDeviceDataStorageListener(myInstance);
                }
                if(deviceDriver.hasStorageSet()) {
                    storageList.put(deviceId,new RoundRobinDataStorage(Source.DEVICE,deviceId));
                    ArrayList storageSet = new ArrayList();
                    for(Entry<String,List<String>> set :deviceDriver.getStorageSet().entrySet()){
                        for(String controlId:set.getValue()){
                            try {
                                switch(((DeviceDataControl)deviceDriver.getFullCommandSet().getControlsGroup(set.getKey()).getDeviceControl(controlId)).getGraph()){
                                    case "time-totals":
                                        storageSet.add(new RoundRobinDataGraphItem(set.getKey(),controlId,FieldType.SUM));
                                    break;
                                    default:
                                        storageSet.add(new RoundRobinDataGraphItem(set.getKey(),controlId,FieldType.AVERAGE));
                                    break;
                                }
                            } catch (DeviceControlsGroupException | DeviceControlException ex) {
                                LOG.error("Could not get control graph type: {}", ex.getMessage());
                            }
                        }
                    }
                    storageList.get(deviceId).registerDataTypes(storageSet);
                }
                if(device instanceof AccessControllerDevice){
                    ((AccessControllerDevice)device).setAccessControllerListener(new AccessControllerProxy());
                }
                deviceDriver.prepare(true);
                try {
                    deviceDriver.startupDevice();
                } catch (UnsupportedOperationException ex){}
                if(device instanceof DeviceScheduler){
                    deviceDriver.startReceivers();
                }
                declaredDevices.put(deviceId, (DeviceInterface)deviceDriver);
                //// Bind to device value proxies
                for(DeviceControlsGroup group:deviceDriver.getFullCommandSet().getControlsGroups().values()){
                    for(DeviceControl control:group.getGroupControls().values()){
                        AutomationRulesVarProxy.addDeviceVarBinding(new StringBuilder(String.valueOf(deviceId)).append(group.getGroupId()).append(control.getControlId()).toString(), control.getValueProperty());
                    }
                }
                LOG.info("Device: {}: {} has been loaded", deviceDriver.getFriendlyName(), deviceDriver.getDevice().getDeviceName());
                driver.deviceLoaded(deviceDriver.getDevice());
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", deviceId);
                    }
                };
                ClientMessenger.send("DeviceService","addDevice", ((DeviceInterface)deviceDriver).getLocationId(), sendObject);
                return (DeviceInterface)deviceDriver;
            } catch (PackagePermissionsNotUpToDateException | ClassNotFoundException ex) {
                LOG.error("Problem loading device '{}': {}",devicePackageName, ex.getMessage());
                throw new DevicesException("Problem loading device, device type '" + devicePackageName + "' not found: " + ex.getMessage());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.error("Device driver for '{}' found, but can not load, contact author. Error: {}", devicePackageName, ex.getMessage(),ex);
                throw new DevicesException("Device driver for '"+devicePackageName+"' found, but can not load, contact author. Error: " + ex.getMessage());
            } catch (UnsupportedDeviceException ex){
                LOG.error("Problem with device specifications: {}", ex.getMessage(), ex);
                throw new DevicesException("Problem with device specifications: " + ex.getMessage());
            } catch (NullPointerException ex){
                LOG.error("Problem with loaded device info: {}", ex.getMessage(), ex);
                throw new DevicesException("Incorrect device specs loaded: " + ex.getMessage());                
            }
        } else {
            throw new DevicesException("Device already loaded");
        }
    }
    
    /**
     * Unloads all the devices based on the peripheral software driver.
     * This function should only be used when a peripheral is removed from the system.
     * @param driver The peripheral software driver.
     */
    void unloadDriverDevices(PeripheralSoftwareDriverInterface driver){
        String driverName = driver.getPackageName();
        LOG.info("Stopping devices for driver {}", driverName);
        LOG.debug("Drivers set: ", deviceCollection);
        List<String> driverDevices = getDevicesByDriverName(driverName);
        for(int i = 0, n = driverDevices.size(); i < n; i++) {
            String device = driverDevices.get(i);
            if(deviceCollection.contains(device)){
                List<Integer>tmp = new ArrayList();
                for(final int devKey : declaredDevices.keySet()){
                    if(declaredDevices.containsKey(devKey)){
                        DeviceInterface dev = declaredDevices.get(devKey);
                        if(dev.getName().equals(device)){
                            stopSingleDevice(devKey, true);
                            tmp.add(devKey);
                        }
                    }
                }
                Iterator tmpKey = tmp.iterator();
                while( tmpKey.hasNext() ){
                    declaredDevices.remove((int)tmpKey.next());
                }
                deviceCollection.remove(device);
            }
        }
    }
    
    /**
     * Returns a list of devices based on a custom device skeleton id.
     * @param customDeviceId
     * @return 
     */
    private static List<Integer> getRunningCustomDevicesGroup(int customDeviceId){
        List<Integer> deviceIds = new ArrayList();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT d.[id] from devices d WHERE device=?")){
            prep.setInt(1, customDeviceId);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    deviceIds.add(rsDevices.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        LOG.debug("Found device ids: {}", deviceIds);
        return deviceIds;
    }
    
    /**
     * Stops and unloads a single device.
     * @param deviceId 
     */
    private static int stopSingleDevice(int deviceId, boolean batched){
        if(declaredDevices.containsKey(deviceId)){
            DeviceInterface dev = declaredDevices.get(deviceId);
            if(dev instanceof AccessControllerDevice){
                ((AccessControllerDevice)dev).removeAccessControllerListener();
            }
            for(DeviceControlsGroup group:dev.getFullCommandSet().getControlsGroups().values()){
                for(DeviceControl control:group.getGroupControls().values()){
                    AutomationRulesVarProxy.removeDeviceVarBinding(new StringBuilder(String.valueOf(deviceId)).append(group.getGroupId()).append(control.getControlId()).toString());
                }
            }
            dev.removeDriverListener((DeviceInterface)dev);
            dev.stopReceivers();
            if(storageList.containsKey(deviceId)){
                storageList.remove(deviceId);
            }
            if(dev.hasStorageSet()){
                dev.removeDeviceDataStorageListener(myInstance);
            }
            try {
                dev.shutdownDevice();
            } catch(UnsupportedOperationException ex){}
            dev.storeRetentionData();
            LOG.debug("Unloaded {}",dev.getFriendlyName());
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", deviceId);
                }
            };
            ClientMessenger.send("DeviceService","deleteDevice", dev.getLocationId(),sendObject);
            if(!batched){
                declaredDevices.remove(deviceId);
            }
        }
        return deviceId;
    }
    
    /**
     * Loads the devices based on the deviceClassName, and creates an instance for each declared device.
     * @return 
     * @param deviceClassName 
     */
    public final List<Integer> loadDeclaredDevicesByName(String deviceClassName){
        List<Integer> deviceIds = new ArrayList();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT d.id FROM devices d INNER JOIN installed_devices id ON id.id=d.device WHERE id.driver=?")){
            prep.setString(1, deviceClassName);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    deviceIds.add(rsDevices.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        LOG.debug("Found device ids: {}", deviceIds);
        return deviceIds;
    }    
    
    /**
     * Returns all the active devices
     * @return All the active devices with they're DeviceInterfaceImpl instance.
     */
    public static List<DeviceInterface> getActiveDevices(){
        List<DeviceInterface> deviceList = new ArrayList();
        for(Integer device : declaredDevices.keySet()){
            deviceList.add(declaredDevices.get(device));
        }
        return deviceList;
    }
    
    /**
     * Returns a all devices based on the device package name, unless device is NULL, it then returns everything from the db.
     * This function checks if a device is loaded or not.
     * @param device The device package name.
     * @return The list, or a single device.
     */
    public static List<Map<String,Object>> getAllDeclaredDevices(String device){
        List<Map<String,Object>> dbDevices = new ArrayList();
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM)) {
                ResultSet rsDevices;
                PreparedStatement prep;
                if(device==null){
                    prep = connection.prepareStatement("SELECT de.*,id.id as devicebaseid,id.driver,id.type,id.xml,id.driver_driver,id.friendlyname,'de.settings','de.screenX','de.screenY',id.sequence FROM devices de "
                            + "LEFT JOIN installed_devices id ON id.id=de.device "
                            + "ORDER BY location,name");
                } else {
                    prep = connection.prepareStatement("SELECT de.*,id.id as devicebaseid,id.driver,id.type,id.xml,id.driver_driver,id.friendlyname,'de.settings','de.screenX','de.screenY',id.sequence FROM devices de "
                            + "LEFT JOIN installed_devices id ON id.id=de.device "
                            + "ORDER BY location,name WHERE name=?");
                    prep.setString(1, device);
                }
                rsDevices = prep.executeQuery();
                while (rsDevices.next()) {
                    Map<String, Object> deviceMap = new HashMap<>();
                    deviceMap.put("id", rsDevices.getInt("id"));
                    deviceMap.put("devicebaseid", rsDevices.getInt("devicebaseid"));
                    deviceMap.put("device", rsDevices.getString("driver"));
                    deviceMap.put("friendlyname", rsDevices.getString("friendlyname"));
                    deviceMap.put("location", rsDevices.getInt("location"));
                    deviceMap.put("address", rsDevices.getString("address"));
                    deviceMap.put("name", rsDevices.getString("name"));
                    deviceMap.put("category", rsDevices.getInt("category"));
                    deviceMap.put("screenX", rsDevices.getInt("screenX"));
                    deviceMap.put("screenY", rsDevices.getInt("screenY"));
                    deviceMap.put("favorite", rsDevices.getBoolean("favorite"));
                    deviceMap.put("fixed", rsDevices.getBoolean("fixed"));
                    deviceMap.put("driver", rsDevices.getString("driver_driver"));
                    deviceMap.put("type", rsDevices.getInt("type"));
                    deviceMap.put("xml", rsDevices.getString("xml"));
                    deviceMap.put("settings", rsDevices.getString("settings"));
                    deviceMap.put("defsequence", rsDevices.getInt("sequence"));
                    try {
                        deviceMap.put("locationname", (String)BaseLocations.getLocation(rsDevices.getInt("location")).get("name"));
                    } catch (LocationServiceException ex) {
                        deviceMap.put("locationname", "Unknown");
                    }
                    try {
                        deviceMap.put("categoryname", (String)BaseCategories.getCategory(rsDevices.getInt("category")).get("name"));
                    } catch (CategoriesException ex) {
                        deviceMap.put("categoryname", "Unknown");
                    }
                    try {
                        deviceMap.put("categoryconstant", (String)BaseCategories.getCategory(rsDevices.getInt("category")).get("constant"));
                    } catch (CategoriesException ex) {
                        deviceMap.put("categoryname", "UNKNOWN");
                    }
                    if(declaredDevices.containsKey(rsDevices.getInt("id"))){
                        deviceMap.put("active", true);
                    } else {
                        deviceMap.put("active", false);
                    }
                    dbDevices.add(deviceMap);
                }
                rsDevices.close();
                prep.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not get database connection for devices: {}", ex.getMessage());
        }
        return dbDevices;
    }
    
    /**
     * As getAllDeclaredDevices but then on deviceId as set in the DB
     * @param deviceId As set in the DB
     * @return a single device.
     * @see #getAllDeclaredDevices(java.lang.String) (for reference)
     */
    public static Map<String, Object> getDeclaredDevice(int deviceId) {
        Map<String, Object> deviceMap = new HashMap<>();
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection
                    .prepareStatement("SELECT de.*,id.id as devicebaseid,id.driver,id.type,id.xml,idr.driver as driver_driver,id.friendlyname,'de.settings','de.screenX','de.screenY',id.sequence as defsequence,'de.modifiers' "
                                      + "FROM devices de "
                                + "INNER JOIN installed_devices id ON de.device=id.id "
                                + "INNER JOIN installed_drivers idr ON idr.id=id.driver_driver "
                                     + "WHERE de.id=?")) {
                prep.setInt(1, deviceId);
                ResultSet rsDevices = prep.executeQuery();
                while (rsDevices.next()) {
                    deviceMap.put("id", rsDevices.getInt("id"));
                    deviceMap.put("devicebaseid", rsDevices.getInt("devicebaseid"));
                    deviceMap.put("device", rsDevices.getString("driver"));
                    deviceMap.put("friendlyname", rsDevices.getString("friendlyname"));
                    deviceMap.put("location", rsDevices.getInt("location"));
                    deviceMap.put("address", rsDevices.getString("address"));
                    deviceMap.put("name", rsDevices.getString("name"));
                    deviceMap.put("category", rsDevices.getInt("category"));
                    deviceMap.put("screenX", rsDevices.getInt("screenX"));
                    deviceMap.put("screenY", rsDevices.getInt("screenY"));
                    deviceMap.put("favorite", rsDevices.getBoolean("favorite"));
                    deviceMap.put("fixed", rsDevices.getBoolean("fixed"));
                    deviceMap.put("driver", rsDevices.getString("driver_driver"));
                    deviceMap.put("type", rsDevices.getInt("type"));
                    deviceMap.put("xml", rsDevices.getString("xml"));
                    deviceMap.put("settings", rsDevices.getString("settings"));
                    deviceMap.put("defsequence", rsDevices.getInt("defsequence"));
                    deviceMap.put("modifiers", rsDevices.getString("modifiers"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Device sql error: {}",ex.getMessage());
        }
        return deviceMap;
    }
    
    /**
     * Gives a plugin the possibility of deleting a device.
     * The plugin should extend the DevicePlugin class. This function can not be
     * used in combination with other plugin types.
     * @param deviceId
     * @throws PluginDeviceMutationException 
     */
    @Override
    public void removeDeviceByPlugin(int deviceId) throws PluginDeviceMutationException {
        try {
            deleteDevice(deviceId);
        } catch (DevicesException ex) {
            throw new PluginDeviceMutationException("Unable to delete device: " +  ex.getMessage());
        }
    }
    
    /**
     * Removes a device by a driver.
     * @param deviceId 
     * @throws org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException 
     */
    @Override
    public void removeDeviceByDriver(int deviceId) throws PeripheralDriverDeviceMutationException {
        try {
            deleteDevice(deviceId);
        } catch (DevicesException ex) {
            throw new PeripheralDriverDeviceMutationException("Unable to delete device: " +  ex.getMessage());
        }
    }
    
    /**
     * Deletes a device from the database, and unloads it from memory if loaded.
     * @param deviceId The id of the device is known in the DB.
     * @return true when delete was successful;
     * @throws org.pidome.server.system.hardware.devices.DevicesException
     */
    public static boolean deleteDevice(final int deviceId) throws DevicesException {
        try {
            stopSingleDevice(deviceId, false);
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("delete from devices where id=? and fixed=0;")) {
                prep.setInt(1, deviceId);
                prep.execute();
            }
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", deviceId);
                }
            };
            ClientMessenger.send("DeviceService","deleteDevice", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            throw new DevicesException(ex.getMessage());
        }
    }
    
    /**
     * Retrieves a single device from the db created with the xml editor.
     * @param deviceId
     * @return 
     */
    public final Map<String,Object> getCustomDevice(int deviceId){
        Map<String,Object> customDevice = new HashMap<>();
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("select id.* from installed_devices id where id.id=? and type=1")) {
                prep.setInt(1, deviceId);
                try (ResultSet rsDevices = prep.executeQuery()) {
                    if (rsDevices.next()) {
                        DeviceSkeletonJSONTransformer json = new DeviceSkeletonJSONTransformer(rsDevices.getString("xml"));
                        json.compose();
                        customDevice.put("id", String.valueOf(rsDevices.getInt("id")));
                        customDevice.put("name", rsDevices.getString("name"));
                        customDevice.put("friendlyname", rsDevices.getString("friendlyname"));
                        customDevice.put("struct", json.get());
                        customDevice.put("driver", rsDevices.getString("driver"));
                        customDevice.put("driver_driver", rsDevices.getString("driver_driver"));
                        customDevice.put("package", rsDevices.getString("package"));
                        customDevice.put("created", rsDevices.getString("created"));
                        customDevice.put("modified", rsDevices.getString("modified"));
                        customDevice.put("defsequence", rsDevices.getString("sequence"));
                    }
                } catch (DeviceSkeletonException ex) {
                    LOG.error("Could not create device JSON: {}", ex.getMessage(), ex);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
        }
        return customDevice;
    }
    
    /**
     * Retrieves a single device from the db created with the xml editor.
     * @param deviceId
     * @return 
     */
    public final ArrayList<Map<String,Object>> getPersonalCustomDevices(){
        ArrayList<Map<String,Object>> customDevicesList = new ArrayList<>();
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("select id.*,p.name as packagename,p.version as packageversion,p.sequence as packagesequence from installed_devices id left join installed_packages p ON id.package=p.id where id.type=1 and id.origin=0")) {
                try (ResultSet rsDevices = prep.executeQuery()) {
                    while (rsDevices.next()) {
                        Map<String,Object> customDevice = new HashMap<>();
                        customDevice.put("id", rsDevices.getInt("id"));
                        customDevice.put("name", rsDevices.getString("name"));
                        customDevice.put("friendlyname", rsDevices.getString("friendlyname"));
                        customDevice.put("driver", rsDevices.getString("driver"));
                        customDevice.put("driver_driver", rsDevices.getString("driver_driver"));
                        customDevice.put("created", rsDevices.getString("created"));
                        customDevice.put("modified", rsDevices.getString("modified"));
                        customDevice.put("defsequence", rsDevices.getInt("sequence"));
                        customDevice.put("packagename", rsDevices.getString("packagename"));
                        customDevice.put("packageversion", rsDevices.getString("packageversion"));
                        customDevice.put("packagesequence", rsDevices.getInt("packagesequence"));
                        customDevicesList.add(customDevice);
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
        }
        return customDevicesList;
    }
    
    
    /**
     * Retrieves a single device from the db created with the xml editor based on it's unique id.
     * @param namedId
     * @return 
     */
    public final Map<String,Object> getCustomDevice(String namedId){
        Map<String,Object> customDevice = new HashMap<>();
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("select id.*,p.name as packagename,p.version as packageversion,p.sequence as packagesequence from installed_devices id left join installed_packages p ON id.package=p.id where id.name=? and type=1")) {
                prep.setString(1, namedId);
                try (ResultSet rsDevices = prep.executeQuery()) {
                    if (rsDevices.next()) {
                        DeviceSkeletonJSONTransformer json = new DeviceSkeletonJSONTransformer(rsDevices.getString("xml"));
                        json.compose();
                        customDevice.put("id", String.valueOf(rsDevices.getInt("id")));
                        customDevice.put("name", rsDevices.getString("name"));
                        customDevice.put("friendlyname", rsDevices.getString("friendlyname"));
                        customDevice.put("struct", json.get());
                        customDevice.put("driver", rsDevices.getString("driver"));
                        customDevice.put("driver_driver", rsDevices.getString("driver_driver"));
                        customDevice.put("created", rsDevices.getString("created"));
                        customDevice.put("modified", rsDevices.getString("modified"));
                        customDevice.put("defsequence", rsDevices.getString("sequence"));
                        customDevice.put("packagename", rsDevices.getString("packagename"));
                        customDevice.put("packageversion", rsDevices.getString("packageversion"));
                        customDevice.put("packagesequence", rsDevices.getString("packagesequence"));
                    }
                } catch (DeviceSkeletonException ex) {
                    LOG.error("Could not create device JSON: {}", ex.getMessage(), ex);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
        }
        return customDevice;
    }
    
    /**
     * Sets device dimensions
     * @param deviceId
     * @param x
     * @param y
     * @return
     * @throws DevicesException 
     */
    public static boolean setVisualDimenions(final int deviceId,final int x,final int y) throws DevicesException{
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("update devices set screenX=?, screenY=? where id=?;")) {
                prep.setInt(1, x);
                prep.setInt(2, y);
                prep.setInt(3, deviceId);
                prep.execute();
            }
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", deviceId);
                    put("x", x);
                    put("y", y);
                }
            };
            try {
                ClientMessenger.send("DeviceService","setVisualDimenions", getDevice(deviceId).getLocationId(), sendObject);
            } catch (UnknownDeviceException ex) {
                LOG.error("Could not get location id of given device id: {}", deviceId);
            }
            return true;
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            throw new DevicesException(ex.getMessage());
        }
    }
    
    /**
     * Removes dimensions
     * @param deviceId
     * @return
     * @throws DevicesException 
     */
    public static boolean removeVisualDimenions(final int deviceId) throws DevicesException {
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("update devices set screenX=0, screenY=0 where id=?;")) {
                prep.setInt(1, deviceId);
                prep.execute();
            }
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", deviceId);
                }
            };
            try {
                ClientMessenger.send("DeviceService","removeVisualDimenions", getDevice(deviceId).getLocationId(), sendObject);
            } catch (UnknownDeviceException ex) {
                LOG.error("Could not get location id of given device id: {}", deviceId);
            }
            return true;
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            throw new DevicesException(ex.getMessage());
        }
    }
    
    /**
     * Removes a list of devices on a floor map.
     * @param floorId
     * @return
     * @throws DevicesException 
     */
    public static List<Map<String,Object>> getVisualDevices(int floorId) throws DevicesException {
        List<Map<String,Object>> devices = new ArrayList<>();
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM)) {
                ResultSet rsDevices;
                try (PreparedStatement prep = connection.prepareStatement("SELECT de.id,de.name,'de.settings',de.screenX,de.screenY,l.id as location FROM devices de "
                        + "LEFT JOIN locations l ON l.id=de.location "
                        + "WHERE l.floor=?")) {
                    prep.setInt(1, floorId);
                    rsDevices = prep.executeQuery();
                    while (rsDevices.next()) {
                        Map<String, Object> deviceMap = new HashMap<>();
                        deviceMap.put("id", rsDevices.getInt("id"));
                        try {
                            deviceMap.put("name", BaseLocations.getLocation(rsDevices.getInt("location")).get("name") + ":" + rsDevices.getString("name"));
                        } catch (LocationServiceException ex) {
                            deviceMap.put("name", rsDevices.getString("name"));
                        }
                        deviceMap.put("location", rsDevices.getInt("location"));
                        deviceMap.put("screenX", rsDevices.getInt("screenX"));
                        deviceMap.put("screenY", rsDevices.getInt("screenY"));
                        devices.add(deviceMap);
                    }
                    rsDevices.close();
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get database connection for devices: {}", ex.getMessage());
             throw new DevicesException("Could not retrieve devices: " + ex.getMessage());
        }
        return devices;
        
        
    }
    
    
    /**
     * Deletes a device from the db and unloads if needed created by the xml editor.
     * @param deviceId
     * @return 
     */
    public boolean deleteCustomDevice(final int deviceId){
        try {
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("delete from installed_devices where id=? and type=1")) {
                prep.setInt(1, deviceId);
                prep.executeUpdate();
            }
            List<Integer> devices = getRunningCustomDevicesGroup(deviceId);
            for(int device:devices){
                try {
                    deleteDevice(device);
                } catch (DevicesException ex) {
                    LOG.error("Could not delete device with custom device skeleton id {}: {}", deviceId, ex.getMessage());
                }
            }
            /// Remove the structure form the cache.
            DeviceStructureStore.removeFromStore(deviceId);
            return true;
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            return false;
        }
    }
    
    /**
     * Returns a JSON structured device structure.
     * This function is limited to custom devices.
     * @param installedId The id of the deivce installed.
     * @return a JSON formatted object with the device structure.
     * @throws DeviceServiceException When device can not be found or an error rises retrieving the device.
     */
    public final Map<String,Object> getDeviceStruct(int installedId) throws DeviceServiceException {
        String structSet = null;
        if(!DeviceStructureStore.hasDeviceStruct(installedId)){
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("SELECT xml FOM installed_devices where id=? and type=1")) {
                prep.setInt(1, installedId);
                try (ResultSet rsDevice = prep.executeQuery()) {
                    if (rsDevice.next()) {
                        structSet = rsDevice.getString("xml");
                    }
                }
            } catch (SQLException ex) {
                throw new DeviceServiceException(ex);
            }
        }
        try {
            if(structSet!=null){
                DeviceSkeletonJSONTransformer transform = new DeviceSkeletonJSONTransformer(structSet);
                transform.compose();
                return transform.get();
            } else {
                throw new DeviceServiceException("Device not found");
            }
        } catch (DeviceSkeletonException ex) {
            throw new DeviceServiceException(ex);
        }
    }

    /**
     * Assigns a custom device skeleton to a new driver.
     * This function stopps all running devices, assigns to the new driver, and if the new driver is running these devices are started again.
     * @param customDriverId
     * @param customDeviceId
     * @return 
     */
    public boolean assignCustomDevice(int customDriverId, int customDeviceId) throws DeviceServiceException{
        List<Integer> deviceList = getRunningCustomDevicesGroup(customDeviceId);
        for(int id:deviceList){
            stopSingleDevice(id, false);
        }
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = connection.prepareStatement("UPDATE 'installed_devices' SET driver=(SELECT customdevicepath FROM installed_drivers WHERE id=? LIMI 1), package=(SELECT package FROM installed_drivers WHERE id=? LIMI 1), driver_driver=? WHERE id=?")) {
            prep.setInt(1, customDriverId);
            prep.setInt(2, customDriverId);
            prep.setInt(3, customDriverId);
            prep.setInt(4, customDeviceId);
            prep.executeUpdate();
        } catch (SQLException ex) {
            throw new DeviceServiceException(ex);
        }
        for(PeripheralSoftwareDriverInterface driver:getPeripheralSoftwareDrivers().values()){
            if(driver.getId()==customDriverId){
                Thread startDevices = new Thread(() -> {
                    for(int id:deviceList){
                        try {
                            loadSingleDeviceInstance(id);
                        } catch (DevicesException ex) {
                            LOG.error("Could not start device after assignment: {}", ex.getMessage());
                        }
                    }
                });
                startDevices.start();
            }
        }
        return true;
    }
    
    /**
     * Updates a custom device created by the device xml editor
     * @param deviceId
     * @param friendlyname
     * @param struct
     * @return 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException When the device is not correct
     */
    public boolean updateCustomDevice(final int deviceId, String friendlyname, String name, Map<String,Object> struct) throws UnsupportedDeviceException {
        try {
            String deviceStruct = PidomeJSONRPCUtils.getParamCollection(struct);
            List<Integer> deviceList = getRunningCustomDevicesGroup(deviceId);
            for(int id:deviceList){
                stopSingleDevice(id, false);
            }
            //Delete the current cached structure.
            DeviceStructureStore.removeFromStore(deviceId);
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("update installed_devices set friendlyname=?,name=?,xml=?,modified=datetime('now'),sequence=sequence+1 WHERE id=?  and type=1")) {
                prep.setString(1, friendlyname);
                prep.setString(2, name);
                prep.setString(3, deviceStruct);
                prep.setInt(4, deviceId);
                prep.executeUpdate();
                if(enabledDevices.containsKey(deviceId)){
                    enabledDevices.remove(deviceId);
                }
                for(int id:deviceList){
                    try {
                        loadSingleDeviceInstance(id);
                        Map<String, Object> sendObject = new HashMap<String, Object>() {
                            {
                                put("id", deviceId);
                            }
                        };
                        try {
                            ClientMessenger.send("DeviceService","editDevice", getDevice(deviceId).getLocationId(), sendObject);
                        } catch (UnknownDeviceException ex) {
                            LOG.debug("Could not get location for device id: {}", deviceId);
                        }
                    } catch (DevicesException ex) {
                        LOG.error("Could not start device {} ({}) after edit", friendlyname, deviceId);
                    }
                }
            }
            return true;
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            return false;
        } catch (PidomeJSONRPCException ex) {
            throw new UnsupportedDeviceException(ex);
        }
    }
    
    /**
     * Creates a device in the database create width the device xml editor
     * @param identifier
     * @param friendlyname
     * @param deviceDriver
     * @param struct
     * @param driverId
     * @param packageId
     * @return
     * @throws UnsupportedDeviceException When the device xml is not correct or device is based on non existing device.
     */
    public final int createCustomDevice(String identifier, String friendlyname, String deviceDriver, Map<String,Object> struct, int driverId, int packageId) throws UnsupportedDeviceException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection.prepareStatement("insert into 'installed_devices' ('name','friendlyname','driver','xml','package','selectable','type','driver_driver','created', 'modified') values (?,?,?,?,?,1,1,?,datetime('now'),datetime('now'))",Statement.RETURN_GENERATED_KEYS)) {
            prep.setString(1, identifier);
            prep.setString(2, friendlyname);
            prep.setString(3, deviceDriver);
            prep.setString(4, PidomeJSONRPCUtils.getParamCollection(struct));
            prep.setInt(5, packageId);
            prep.setInt(6, driverId);

            prep.execute();
            try (ResultSet rs = prep.getGeneratedKeys()) {
                if(rs.next()){
                    int auto_id = rs.getInt(1);
                    return auto_id;
                } else {
                    throw new UnsupportedDeviceException("No new id found, try to start the device manually");
                }
            } catch (SQLException ex){
                throw new UnsupportedDeviceException("Problem creating new id, try to start the device manually");
            }
        } catch (PidomeJSONRPCException ex) {
            throw new UnsupportedDeviceException(ex);
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            throw new UnsupportedDeviceException("Could not add new custom device: " + ex.getMessage());
        }
    }
    
    /**
     * Returns an installed device from the database limited by the driver.
     * @param deviceDriver The device driver path
     * @param driverId the id of the driver required.
     * @return 
     */
    public static final Map<String,Object> getInstalledDeviceByDriverName(String deviceDriver, int driverId){
        Map<String,Object> deviceInfo = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
             PreparedStatement prep = fileDBConnection.prepareStatement("SELECT DISTINCT id.id,id.friendlyname FROM installed_devices id WHERE id.driver_driver=? AND id.driver=? LIMIT 1")) {
            prep.setInt(1, driverId);
            prep.setString(2, deviceDriver);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    deviceInfo.put("id", rsDevices.getInt("id"));
                    deviceInfo.put("name", rsDevices.getString("friendlyname"));
                    deviceInfo.put("devicedriver", deviceDriver);
                    deviceInfo.put("driverdriverid", driverId);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load device from database: {}", ex.getMessage());
        }
        return deviceInfo;
    }

    
    
    /**
     * Adds a device by plugin.
     * @param installedId
     * @param name
     * @param address
     * @param category
     * @throws PluginDeviceMutationException 
     */
    @Override
    public final void addDeviceByPlugin(int installedId, String name, String address, int category) throws PluginDeviceMutationException {
        try {
            saveDevice(installedId, 1, address, name, category, false, null, new ArrayList<>());
        } catch (DevicesException ex) {
            throw new PluginDeviceMutationException("Could not add a device: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a custom device by a plugin.
     * @param installedId
     * @param name
     * @param address
     * @param location
     * @param category
     * @throws PluginDeviceMutationException 
     */
    @Override
    public final void addFromExistingDeviceByPlugin(int installedId, String name, String address, int location, int category) throws PluginDeviceMutationException {
        try {
            saveDevice(installedId, location, address, name, category, false, null, new ArrayList<>());
        } catch (DevicesException ex) {
            throw new PluginDeviceMutationException("Could not add a device: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a device by a driver.
     * @param installedId
     * @param name
     * @param address
     * @param location
     * @param category
     * @throws PeripheralDriverDeviceMutationException 
     */
    @Override
    public final void addDeviceByDriver(int installedId, String name, String address, int location, int category) throws PeripheralDriverDeviceMutationException {
        try {
            saveDevice(installedId, location, address, name, category, false, null, new ArrayList<>());
        } catch (DevicesException ex) {
            throw new PeripheralDriverDeviceMutationException("Could not add a device: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a device by a driver including settings for the device.
     * @param installedId
     * @param name
     * @param address
     * @param location
     * @param category
     * @param settings
     * @throws PeripheralDriverDeviceMutationException 
     */
    @Override
    public final void addDeviceByDriver(int installedId, String name, String address, int location, int category, Map settings) throws PeripheralDriverDeviceMutationException {
        try {
            saveDevice(installedId, location, address, name, category, false, settings, new ArrayList<>());
        } catch (DevicesException ex) {
            throw new PeripheralDriverDeviceMutationException("Could not add a device: " + ex.getMessage());
        }
    }
    
    /**
     * Creates a custm device by a driver.
     * @param driver
     * @param device
     * @return The database id of the created device skeleton.
     * @throws PeripheralDriverDeviceMutationException 
     */
    @Override
    public int createDeviceSkeletonByDriver(PeripheralSoftwareDriver driver, DiscoveredDevice device) throws PeripheralDriverDeviceMutationException {
        try {
            boolean exists = false;
            DeviceStructure struct = new DeviceStructure("DEVICECONSISTENCYCHECK");
            struct.createStructure(device.getDeviceStructureCreator().getObjectStructure());
            try {
                String workName = device.getDeviceStructureCreator().getName();
                if(workName.equals("")){
                    workName = device.getName();
                }
                String skeletonName = workName.replaceAll("\\s", "_").replaceAll("[^a-zA-Z0-9_]","");
                if(skeletonName.equals("")){
                    throw new PeripheralDriverDeviceMutationException("Incorrect device type name format");
                }
                try (Connection db = DB.getConnection(DB.DB_SYSTEM)) {
                    try (PreparedStatement statement = db.prepareStatement("SELECT id FROM installed_devices WHERE name=? LIMIT 1")){
                        statement.setString(1, skeletonName);
                        try (ResultSet result = statement.executeQuery()) {
                            if(result.next()){
                                exists  = true;
                            }
                        }
                    }
                    if(!exists){
                        driver.getId();
                        try (PreparedStatement insert = 
                                db.prepareStatement("INSERT INTO installed_devices (name, friendlyname, xml, selectable, type, struct, version, sequence, origin, driver_driver, package, driver) " +
                                                    "SELECT ?, ?, ?, 1, 0, '{\"type\":0}', '0.0.1', 1, 0, id, package, customdevicepath FROM installed_drivers WHERE id=? LIMIT 1", Statement.RETURN_GENERATED_KEYS)){
                            insert.setString(1, skeletonName);
                            insert.setString(2, workName);
                            insert.setString(3, device.getDeviceStructureCreator().getCollection());
                            insert.setInt(4, driver.getId());
                            insert.execute();
                            try (ResultSet rs = insert.getGeneratedKeys()) {
                                if (rs.next()) {
                                    return rs.getInt(1);
                                } else {
                                    throw new PeripheralDriverDeviceMutationException("Device type created but unable to add (reference error), add this device manually");
                                }
                            }
                        }
                    } else {
                        throw new PeripheralDriverDeviceMutationException("Device type already exists, choose another device type name.");
                    }
                } catch (SQLException ex){
                    throw new PeripheralDriverDeviceMutationException("Database error while handing new device type: " + ex.getMessage());
                }
            } catch (Exception ex) {
                LOG.error("Unable to save device skeleton: {}", ex.getMessage(), ex);
                throw new PeripheralDriverDeviceMutationException(ex.getMessage());
            }
        } catch (IllegalDeviceActionException | UnsupportedDeviceException ex) {
            LOG.error("Not possible to construct a device from given structure, check parameters: {}", ex.getMessage(), ex);
            throw new PeripheralDriverDeviceMutationException(ex.getMessage());
        }
    }
    
    /**
     * Creates a device with a driver as creation resource.
     * @param identifier
     * @param friendlyname
     * @param deviceName
     * @param driverId
     * @param struct
     * @param packageId
     * @throws PeripheralDriverDeviceMutationException 
     */
    @Override
    public final void createCustomDeviceByDriver(String identifier, String friendlyname, String deviceName, Map<String,Object> struct, int driverId, int packageId) throws PeripheralDriverDeviceMutationException {
        try {
            this.createCustomDevice(identifier, friendlyname, deviceName, struct, driverId, packageId);
        } catch (UnsupportedDeviceException ex) {
            throw new PeripheralDriverDeviceMutationException("Could not create device.");
        }
    }
    
    /**
     * Saves a new device in the database.
     * @param device
     * @param location
     * @param address
     * @param name
     * @param category
     * @param favorite
     * @param settings
     * @param modifiers
     * @return 
     * @throws org.pidome.server.system.hardware.devices.DevicesException 
     */
    public static boolean saveDevice(int device, int location, String address, String name, int category, boolean favorite, Map<String,Object> settings,List<Map<String,Object>> modifiers) throws DevicesException {
        if(!enabledDevices.containsKey(device)){
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT d.id,d.friendlyname,d.driver,d.selectable FROM installed_devices d WHERE d.id=? LIMIT 1")){
                prep.setInt(1, device);
                try (ResultSet rsDevices = prep.executeQuery()) {
                    while (rsDevices.next()) {
                        Map<String, String> deviceMap = new HashMap<>();
                        deviceMap.put("friendlyname", rsDevices.getString("friendlyname"));
                        deviceMap.put("driver", rsDevices.getString("driver"));
                        deviceMap.put("selectable", rsDevices.getString("selectable"));
                        enabledDevices.put(rsDevices.getInt("id"), deviceMap);
                    }
                } catch (SQLException ex) {
                    LOG.error("Could not load installed devices from database: {}", ex.getMessage());
                    throw new DevicesException("Could not load installed devices from database: " + ex.getMessage());
                }
            } catch (SQLException ex) {
                LOG.error("Could not load installed devices from database: {}", ex.getMessage());
                throw new DevicesException("Could not load installed devices from database: " + ex.getMessage());
            }
        }
        if (enabledDevices.containsKey(device)) {
            final int auto_id;
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = connection
                        .prepareStatement("insert into 'devices' ('device','location','address','name','screenX','screenY','category','favorite','fixed','settings','modifiers','created','modified') values (?,?,?,?,0,0,?,?,0,?,?,datetime('now'),datetime('now'));", Statement.RETURN_GENERATED_KEYS)) {
                prep.setInt(1, device);
                prep.setInt(2, location);
                prep.setString(3, address);
                prep.setString(4, name);
                prep.setInt(5, category);
                prep.setBoolean(6, favorite);
                prep.setString(7, PidomeJSONRPCUtils.getParamCollection(settings));
                prep.setString(8, PidomeJSONRPCUtils.getParamCollection(modifiers));
                prep.execute();
                try (ResultSet rs = prep.getGeneratedKeys()) {
                    if (rs.next()) {
                        auto_id = rs.getInt(1);
                        loadSingleDeviceInstance(auto_id);
                    } else {
                        auto_id = 0;
                    }
                    LOG.info("Added new device: {}, name: {}", device, name);
                }
                Map<String,Object> sendObject = new HashMap<String,Object>(){
                    {
                        put("id", auto_id);
                    }
                };
                ClientMessenger.send("DeviceService","addDevice", location, sendObject);
                return true;
            } catch (SQLException ex) {
                LOG.error("Database error '{}'", ex.getMessage());
                throw new DevicesException("Save error, refer to log file");
            } catch (DevicesException ex) {
                LOG.error("Error instantiating a new {} id instance", device);
                throw new DevicesException("Could not start device, make sure the correct peripheral is connected and try to start the driver manually, sorry....");
            } catch (NullPointerException ex) {
                LOG.error("Error instantiating a new {} id instance", device);
                throw new DevicesException("Error starting device, try to do it manually or recreate it.");
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Error instantiating a new {} id instance", device);
                throw new DevicesException("Error creating device options parameters");
            }
        }
        LOG.error("Tried to save unknown device: {}", device);
        throw new DevicesException("Tried to save unknown device");
    }
    
    /**
     * Marks a device as favorite.
     * @param deviceId
     * @param favorite
     * @return
     * @throws DevicesException
     * @throws UnknownDeviceException 
     */
    public static boolean setFavorite(final int deviceId, final boolean favorite) throws DevicesException, UnknownDeviceException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)) {
            try (PreparedStatement prep = connection
                    .prepareStatement("update devices set 'favorite'=?,'modified'=datetime('now') where id =?;")) {
                prep.setBoolean(1, favorite);
                prep.setInt(2, deviceId);
                prep.executeUpdate();
                DeviceInterface deviceInstance = getDevice(deviceId);
                deviceInstance.setIsFavorite(favorite);
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", deviceId);
                        put("favorite", favorite);
                    }
                };
                ClientMessenger.send("DeviceService","setFavorite", deviceInstance.getLocationId(), sendObject);
            }
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            throw new DevicesException("Error updating device");
        }
        return true;
    }
    
    /**
     * Edits a device and refreshes the information in a loaded device, if loaded.
     * @param deviceId
     * @param location
     * @param address
     * @param name
     * @param category
     * @param settings
     * @param favorite
     * @param modifiers
     * @return 
     * @throws org.pidome.server.system.hardware.devices.DevicesException 
     */
    public static boolean editDevice(final int deviceId, int location, String address, String name, int category, Boolean favorite, Map<String,Object> settings,List<Map<String,Object>> modifiers) throws DevicesException {
        try {
            stopSingleDevice(deviceId, false);
            try (Connection connection = DB.getConnection(DB.DB_SYSTEM)) {
                try (PreparedStatement prep = connection
                        .prepareStatement("update devices set location=?,address=?,name=?,category=?,favorite=?,settings=?,modifiers=?,'modified'=datetime('now') where id =?;")) {
                    prep.setInt(1, location);
                    prep.setString(2, address);
                    prep.setString(3, name);
                    prep.setInt(4, category);
                    prep.setBoolean(5, favorite);
                    prep.setString(6, PidomeJSONRPCUtils.getParamCollection(settings));
                    prep.setString(7, PidomeJSONRPCUtils.getParamCollection(modifiers));
                    prep.setInt(8, deviceId);
                    prep.executeUpdate();
                } catch (PidomeJSONRPCException ex) {
                    java.util.logging.Logger.getLogger(Devices.class.getName()).log(Level.SEVERE, null, ex);
                }
                LOG.info("Modified device: {}, (new) name: {}, id: {}", deviceId, name, deviceId);
                try {
                    loadSingleDeviceInstance(deviceId);
                    Map<String, Object> sendObject = new HashMap<String, Object>() {
                        {
                            put("id", deviceId);
                        }
                    };
                    try {
                        ClientMessenger.send("DeviceService","editDevice", getDevice(deviceId).getLocationId(), sendObject);
                    } catch (UnknownDeviceException ex) {
                        LOG.debug("Could not get location for device id: {}", deviceId);
                    }
                } catch (DevicesException ex) {
                    LOG.error("Could not start device {} ({}) after edit", name, deviceId);
                }
            }
            return true;
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            throw new DevicesException("Error updating device");
        }
    }
    
    
    /**
     * Checks if a specific device class has been loaded.
     * @param deviceName Package name of the device class
     * @return True when minimal one device instance has been loaded.
     */
    public static Boolean isLoaded(String deviceName){
        return deviceCollection.contains(deviceName);
    }
    
    /**
     * Well, lets send something to a device.
     * @param deviceId The declared device id from the database.
     * @param cmdGroup
     * @param cmdSet
     * @param action
     * @param force In case a device is locked (because of scene) set to true to override the scene lock and modify device state.
     * @return 
     * @throws org.pidome.server.system.hardware.devices.DevicesException 
     */
    public static boolean send(int deviceId, String cmdGroup, String cmdSet, Map<String,Object> action, boolean force) throws DevicesException {
        try {
            DeviceInterface device = getDevice(deviceId);
            device.handleCommand(cmdGroup, cmdSet, action, force);
            return device.commandResult();
        } catch (UnknownDeviceException | UnsupportedDeviceCommandException ex) {
            LOG.error("Error in devices: {}", ex.getMessage());
            throw new DevicesException(ex.getMessage());
        }
    }
    
    /**
     * Returns a device as a DeviceInterface
     * @param deviceId The declared device id as set in the database.
     * @return DeviceInterfaceImpl
     * @throws UnknownDeviceException When there is no instance loaded or when loaded the device id is not loaded. The latter should not happen when using a correct device class.
     */
    public static DeviceInterface getDevice(int deviceId) throws UnknownDeviceException {
        if(declaredDevices.containsKey(deviceId)){
            return declaredDevices.get(deviceId);
        } else {
            throw new UnknownDeviceException("Device with id "+deviceId+" is not loaded");
        }
    }
    
    /**
     * Returns a device that has not been loaded into the system, but creates a "fake" instance.
     * @param deviceId The declared device id as set in the database.
     * @return DeviceInterfaceImpl
     * @throws UnknownDeviceException When there is no instance loaded or when loaded the device id is not loaded. The latter should not happen when using a correct device class.
     */
    public static DeviceInterface getOfflineDeviceInstance(int deviceId) throws UnknownDeviceException {
        try {
            /// first load the device data
            Map<String,Object>deviceData = getDeclaredDevice(deviceId);
            /// First load the device instance
            Device device = (Device)myInstance.getPackageLoader().loadDeviceDriver(deviceId).getConstructor().newInstance();
            DeviceStruct deviceDriver = new DeviceStruct(device);
            deviceDriver.setInstalledDeviceId((int) deviceData.get("devicebaseid"));
            deviceDriver.createDeviceComponentsSet(((String)deviceData.get("xml")).trim());
            deviceDriver.getDeviceOptions().set(new DeviceOptionsJSONTransformer(((String)deviceData.get("settings")).trim()).get());
            deviceDriver.setId((int) deviceData.get("id"));
            deviceDriver.setDeviceDriver((String) deviceData.get("driver"));
            deviceDriver.setFriendlyName((String) deviceData.get("friendlyname"));
            deviceDriver.setLocationId((int) deviceData.get("location"));
            deviceDriver.setAddress((String) deviceData.get("address"));
            deviceDriver.setDeviceName((String) deviceData.get("name"));
            deviceDriver.setCategoryId((int) deviceData.get("category"));
            deviceDriver.setDefinitionSequence((int)deviceData.get("defsequence"));
            try {
                deviceDriver.setCategoryName((String)BaseCategories.getCategory((int) deviceData.get("category")).get("name"));
            } catch (CategoriesException ex) {
                deviceDriver.setCategoryName("Unknown");
            }
            try {
                 deviceDriver.setCategoryConstant((String)BaseCategories.getCategory((int)deviceData.get("category")).get("constant"));
            } catch (CategoriesException ex) {
                 deviceDriver.setCategoryName("UNKNOWN");
            }
            try {
                deviceDriver.setLocationName((String) BaseLocations.getLocation((int) deviceData.get("location")).get("name"));
            } catch (LocationServiceException ex) {
                deviceDriver.setLocationName("Unknown");
            }
            deviceDriver.setIsFixed((boolean) deviceData.get("fixed"));
            deviceDriver.setDeviceDriverDriver((String) deviceData.get("driver_driver"));
            deviceDriver.setDeviceType((int) deviceData.get("type"));
            deviceDriver.setIsFavorite((boolean)deviceData.get("favorite"));
            deviceDriver.setisActive(false);
            return deviceDriver;
        } catch (PackagePermissionsNotUpToDateException | ClassNotFoundException ex) {
            LOG.error("Problem loading device: {}", ex.getMessage());
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.error("Device driver for '{}' found, but can not load, contact author",deviceId, ex.getMessage());
        } catch (UnsupportedDeviceException ex){
            LOG.error("Problem with device specifications: {}", ex.getMessage(), ex);
        }
        throw new UnknownDeviceException("Problem loading device");
    }
    
    /**
     * Returns the driver name of the attached driver to this device.
     * @param deviceName
     * @param deviceId
     * @return The driver name as in driver package name.
     * @throws org.pidome.server.connector.drivers.devices.UnknownDeviceException
     */
    public static String getDriver(String deviceName, int deviceId) throws UnknownDeviceException {
        if(isLoaded(deviceName)){
            if(declaredDevices.containsKey(deviceId)){
                return declaredDevices.get(deviceId).getDriverName();
            } else {
                throw new UnknownDeviceException("Device not loaded from database");
            }
        } else {
            throw new UnknownDeviceException("Device is not loaded");
        }
    }
    
    /**
     * Returns loaded devices.
     * @return 
     */
    public Map<Integer, DeviceInterface> getLoadedDevices(){
        return declaredDevices;
    }
    
    /**
     * Return an object instance of a non coupled installed device.
     * @param deviceId
     * @return An object instance of an installed device.
     * @throws ClassNotFoundException 
     */
    public final DeviceInterface getInstalledDeviceInstance(int deviceId) throws ClassNotFoundException, DevicesException {
        return getLooseInstance(deviceId);
    }
    
    
    /**
     * Loads only an instance of a device and returns it, but does nothing with it.
     * @param deviceId
     * @return DeviceInterfaceImpl
     * @throws java.lang.ClassNotFoundException
     */
    public final DeviceInterface getLooseInstance(int deviceId) throws ClassNotFoundException, DevicesException {
        String deviceXml = "<device/>";
        String deviceClassName = "UNKNOWN";
        try(Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement statementDevices = fileDBConnection.prepareStatement("SELECT d.xml,d.driver,d.name,d.package,d.driver_driver FROM installed_devices d WHERE id=?");) {
            statementDevices.setInt(1, deviceId);
            try(ResultSet rsDevices = statementDevices.executeQuery()) {
                int packageId = 0;
                if (rsDevices.next()) {
                    deviceXml = rsDevices.getString("xml");
                    deviceClassName = rsDevices.getString("driver");
                    packageId = rsDevices.getInt("package");
                    Device device = (Device)myInstance.getPackageLoader().loadInstalledDeviceDriver(deviceId).getConstructor().newInstance();
                    DeviceStruct deviceDriver = new DeviceStruct(device);
                    deviceDriver.setInstalledDeviceId(deviceId);
                    deviceDriver.createDeviceComponentsSet(deviceXml, false);
                    deviceDriver.setPackageId(packageId);
                    deviceDriver.setDeviceName(rsDevices.getString("name"));
                    deviceDriver.setDeviceDriverDriverId(rsDevices.getInt("driver_driver"));
                    return deviceDriver;
                } else {
                    throw new DevicesException("Device not found");
                }
            } catch (SQLException ex) {
                LOG.error("Could not load device skeleton for: {}, {}",ex.getMessage(), ex);
                throw new DevicesException("Could not load device skeleton for: " + deviceClassName);
            } catch (UnsupportedDeviceException ex){
                LOG.error("Problem with device specifications: {}, {}",ex.getMessage(), ex);
                throw new DevicesException("Problem with device specifications: " + ex.getMessage());
            }
        } catch (PackagePermissionsNotUpToDateException | SQLException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            LOG.error("Could not load {}, Reason: {}", deviceClassName, ex.getMessage());
            throw new DevicesException("Problem loading device, refer to log file.");
        }
    }
    
    ////////////////////// BATCHING!!! YAY!
    
    /**
     * Responsible for adding a device command to a batch.
     * A batch is a list of commands which mostly contains commands from an event. This function does not use any base send commands, but prepares a command. Checks of a command is legit happens here, and not when the batch runs.
     * @param cmdGroup
     * @param deviceId
     * @param cmdControl
     * @param value
     * @param extra
     * @param batchName The name of the batch (if coming from an event it will be a name composed by the EventService)
     */
    public void addBatch(int deviceId, String cmdGroup, String cmdControl, Object value, String extra, String batchName) {
        try {
            DeviceInterface device = getDevice(deviceId);
            DeviceCommandRequest request = new DeviceCommandRequest(device.getFullCommandSet().getControlsGroup(cmdGroup).getDeviceControl(cmdControl));
            request.setGroupId(cmdGroup);
            request.setCommandValue(value);
            if(extra!=null && extra.length()>0){
               request.setExtraValue(extra);
            }
            Map<DeviceInterface, DeviceCommandRequest> addToBatch = new HashMap<>();
            addToBatch.put(device, request);
            if(batchList.containsKey(batchName)){
                batchList.get(batchName).add(addToBatch);
            } else {
                addToBatch.put(device, request);
                List<Map<DeviceInterface, DeviceCommandRequest>> batchArray = new ArrayList<>();
                batchArray.add(addToBatch);
                batchList.put(batchName, batchArray);
            }
            LOG.debug("Added to batch {} -> {}:{}", batchName, device.getName(),value);
        } catch (UnknownDeviceException | DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Could not add command '{} ({})' for device '{}' to batch '{}': {}",value,extra,deviceId,batchName,ex.getMessage());
        }
    }
    
    /**
     * Runs the batch as set by addBatch.
     * There are no notifications of any device results. Failures get lost.
     * @param batchName The name of the batch to run
     */
    public void runBatch(final String batchName){
        if(batchList.containsKey(batchName)){
            Runnable runBatch = () -> {
                for (Map<DeviceInterface, DeviceCommandRequest> listing : batchList.get(batchName)) {
                    for(Map.Entry<DeviceInterface, DeviceCommandRequest> entry:listing.entrySet()){
                        Map<String,Object> params = new HashMap<>();
                        try {
                            params.put("value", entry.getValue().getCommandValue());
                            params.put("extra", entry.getValue().getExtraValue());
                            entry.getKey().handleCommand(entry.getValue().getGroupId(), entry.getValue().getControlId(), params);
                            handleDeviceData(((DeviceStruct)entry.getKey()).getDevice(), entry.getValue().getGroupId(), entry.getValue().getControlId(), entry.getValue().getCommandValue());
                        } catch (UnsupportedDeviceCommandException ex) {
                            LOG.error("Batched command '{}' for device '{}' could not be executed. Reason: ",params,entry.getKey().getName(), ex.getMessage());
                        }
                    }
                }
                batchList.remove(batchName);
            };
            runBatch.run();
        }
    }
    
    ////////////////////// Events
    /**
     * Handles events coming from the drivers class when a driver is loaded, or when a driver is being unloaded.
     * This event is triggered when the system is done loading a driver. Unloading of devices happens BEFORE the driver is unloaded.
     * @param oEvent 
     */
    @Override
    public void handleDriverEvent(DriverEvent oEvent) {
        PeripheralSoftwareDriverInterface driver = (PeripheralSoftwareDriverInterface) oEvent.getSource();
        switch(oEvent.getEventType()){
            case DriverEvent.DRIVER_LOADED:
                loadDriverDevices(driver);
            break;
            case DriverEvent.DRIVER_UNLOAD:
                unloadDriverDevices(driver);
            break;
        }
        
    }

    /**
     * Stores numeric data coming from devices, loads needed classes automatically.
     * @param deviceId
     * @param dataGroup
     * @param dataName
     * @param data 
     */
    @Override
    public void handleDeviceStoreData(int deviceId, String dataGroup, String dataName, Object data) {
        LOG.debug("Got storage data: {} - {} - {} - {}",deviceId, dataGroup, dataName, data);
        if(storageList.containsKey(deviceId)){
            storageList.get(deviceId).store(dataGroup, dataName, Double.valueOf(data.toString()));
        }
    }

    /**
     * Returns the storage set.
     * @param deviceId
     * @return 
     */
    public static RoundRobinDataStorage getStorageList(int deviceId) throws Exception {
        if(storageList.containsKey(deviceId)){
            return storageList.get(deviceId);
        } else {
            throw new Exception("Device not found");
        }
    }
    
    /**
     * Handling for data coming from devices.
     * This does not store data! It is used for feeding the triggers and dispatching to connected clients
     * @param device
     * @param cmdGroup
     * @param cmdControl
     * @param deviceCommand 
     */
    @Override
    public void handleDeviceData(Device device, String cmdGroup, String cmdControl, Object deviceCommand) {
        try {
            DeviceHook.handleDeviceValue(device, cmdGroup, cmdControl, device.getFullCommandSet().getControlsGroup(cmdGroup).getDeviceControl(cmdControl),deviceCommand);
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Could not deliver to device hook: {}", ex.getMessage());
        }
        try {
            TriggerService.handleEvent("device_"+device.getId()+"_"+cmdGroup+"_"+cmdControl, deviceCommand);
        } catch (IndexOutOfBoundsException ex){
            /// no check;
        }
    }
    
    /**
     * Handling for data coming from devices.
     * This does not store data! It is used for feeding the triggers and dispatching to connected clients
     * @param device
     * @param cmdGroup
     * @param cmdSet
     * @param deviceCommand 
     */
    @Override
    public void handleDeviceData(Device device, String cmdGroup, String cmdSet, double deviceCommand) {
        try {
            DeviceHook.handleDeviceValue(device, cmdGroup, cmdSet, device.getFullCommandSet().getControlsGroup(cmdGroup).getDeviceControl(cmdSet),deviceCommand);
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Could not deliver to device hook: {}", ex.getMessage());
        }
        try {
            TriggerService.handleEvent("device_"+device.getId()+"_"+cmdGroup+"_"+cmdSet, deviceCommand);
        } catch (IndexOutOfBoundsException ex){
            /// no check;
        }
    }
    
    /**
     * Constructs the string needed to be send to connected clients and sends it.
     * @param deviceId
     * @param cmdGroup
     * @param cmdSet
     * @param deviceCommand 
     */
    static void sentToClients(final int deviceId, final String cmdGroup, final String cmdControl, final Object deviceCommand){
        List<Map<String, Object>> notificationStructSet = new ArrayList<>(1);
        notificationStructSet.add(new HashMap<String,Object>(2){{
            put("groupid",  cmdGroup);
            put("controls", new HashMap<String,Object>(){{ put(cmdControl, deviceCommand); }});
        }});
        try {
            sendNotification(((DeviceStruct)getDevice(deviceId)).getDevice(), notificationStructSet);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not get location for device id: {}. Data not send", deviceId);
        }
    }

    @Override
    public void handleHookDeviceDeliverData(PluginBase plugin, int deviceId, String group, String set, Object deviceData) {
        addBatch(deviceId, group, set, String.valueOf(deviceData), "", "plugindata_" + plugin.getPluginId());
        runBatch("plugindata_" + plugin.getPluginId());
    }

    /**
     * Handles the notification data.
     * @param device
     * @param notificationStructSet 
     */
    @Override
    public void handleDeviceNotificationData(Device device, List<Map<String, Object>> notificationStructSet) {
        sendNotification(device,notificationStructSet);
    }
    
    /**
     * Sends out the notification data.
     * @param device
     * @param notificationStructSet 
     */
    private static void sendNotification(Device device, List<Map<String, Object>> notificationStructSet){
        Map<String,Object> sendObject = new HashMap<>();
        sendObject.put("id", device.getId());
        sendObject.put("groups", notificationStructSet);
        ClientMessenger.send("DeviceService","sendDevice", device.getLocationId(),sendObject);
    }
}