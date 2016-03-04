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

package org.pidome.server.services.hardware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.hardware.devices.Devices;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.events.EventService;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.hardware.Hardware;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.system.hardware.devices.DeviceStruct;
import org.pidome.server.system.hardware.devices.DevicesException;
import org.pidome.server.system.hardware.peripherals.PeripheralController;
import org.pidome.server.system.hardware.peripherals.emulators.HardwarePluginDeviceEmulators;
import org.pidome.server.system.packages.PackageProxy;
import org.pidome.server.system.packages.Packages;

/**
 * The device service is for connecting other services, classes and sort of to the hardware 
 * devices manager.
 * The device service is nothing more then a proxy to the devices class. The Device service though is
 * the centralized authority to start the devices hardware. The following roles are for the service:
 * - Start and stop hardware stuff
 * - proxying calls to the correct hardware service part (devices, drivers, peripherals and the base hardware class)
 * - proxying calls to the packages specific to the hardware part.
 * @author John Sirach
 */
public final class DeviceService implements ServiceInterface {
    
    static Logger LOG = LogManager.getLogger(DeviceService.class);
    
    /**
     * The highest entry to the devices.
     * @see Devices
     */
    static Devices devices;
    
    /**
     * The package manager
     */
    static PackageProxy packages = new PackageProxy();
    
    /**
     * Device service initialization and fires an availability event.
     * @throws org.pidome.server.system.hardware.devices.DevicesException
     */
    public DeviceService() throws DevicesException {
        if(devices==null){
            devices = new Devices();
        }
    }

    public static Devices getDevicesInstance() throws DevicesException {
        if(devices==null) throw new DevicesException("Not initialized");
        return devices;
    }
    
    /**
     * Starts a hardware peripheral emulation for device plugins.
     * @param plugin 
     * @throws org.pidome.server.system.hardware.devices.DevicesException 
     */
    public static void startPluginDeviceEmulation(DevicePlugin plugin) throws DevicesException {
        try {
            HardwarePluginDeviceEmulators.createDevice(plugin);
        } catch (PeripheralHardwareException ex) {
            throw new DevicesException("Could not start hardware emulation for plugin: "+plugin.getPluginName()+", reason: "+ex.getMessage());
        }
    }
    
    /**
     * Stops the plugin device emulation.
     * @param plugin
     * @throws DevicesException 
     */
    public static void stopPluginDeviceEmulation(DevicePlugin plugin) throws DevicesException {
        try {
            if(plugin.getHardwareDevice()!=null){ /// Device has nog been loaded yet. This happens when a plugin fails to start which prevents device loading.
                HardwarePluginDeviceEmulators.removeDevice(plugin);
            }
        } catch (PeripheralHardwareException ex) {
            LOG.error("Problem stopping emulated hardware device: {}", ex.getMessage());
        }
    }
    
    /**
     * Stops an peripheral and all relying entities.
     * @param port 
     */
    public static void stopRunningPeripheral(String port){
        PeripheralController peripheralToStop = null;
        for (PeripheralController peripheral:devices.getRunningPeripherals().values()){
            if(peripheral.getPeripheral().getDevicePort().equals(port)){
                peripheralToStop = peripheral;
            }
        }
        if(peripheralToStop!=null){
            devices.unloadHardwareByUser(peripheralToStop);
        }
    }
    
    /**
     * Returns true if the USB watchDog is running.
     * @return 
     */
    public static boolean USBWatchdogRunning(){
        return devices.USBWatchdogRunning();
    }
    
    /**
     * Returns a device info list.
     * @param driverPath
     * @return 
     */
    public static List<Map<String,Object>> getInstalledDevicesInfoByDriverName(String driverPath){
        return devices.getInstalledDevicesInfoByDriverName(driverPath);
    }
    
    /**
     * Returns a devices list by the given peripheral software id.
     * @param driverId
     * @return 
     */
    public static List<Map<String,Object>> getInstalledDevicesByPeripheralSoftwareDriver(int driverId){
        return devices.getInstalledDevicesByPeripheralSoftwareDriver(driverId);
    }
    
    /**
     * Returns a list of hardware peripherals running happily.
     * @return 
     */
    public static Map<String,PeripheralController> getRunningHardwarePeripherals(){
        return devices.getRunningPeripherals();
    }
    
    /**
     * Returns a list of active peripheral software drivers.
     * @return 
     */
    public static Map<String,PeripheralSoftwareDriverInterface> getPeripheralSoftwareDrivers(){
        return devices.getPeripheralSoftwareDrivers();
    }
    
    /**
     * Returns a list of attached peripherals which currently are unsupported.
     * @return 
     */
    public static Map<String,PeripheralController> getAttachedUnsupportedPeripherals(){
        return devices.getAttachedUnsupportedPeripherals();
    }
    
    /**
     * Returns a list of peripherals which needs extra settings before they can continue
     * @return 
     */
    public static Map<String,PeripheralController> getWaitingHardwarePeripherals(){
        return devices.getWaitingPeripherals();
    }

    /**
     * Returns a list of current active peripheral software drivers.
     * @return 
     */
    public static Map<String,PeripheralSoftwareDriverInterface> getRunningDriversCollection(){
        return devices.getRunningDriversCollection();
    }
    
    /**
     * Returns a single waiting peripheral.
     * @param peripheralKey
     * @return
     * @throws PeripheralHardwareException 
     */
    public static PeripheralController getWaitingHardwarePeripheral(String peripheralKey) throws PeripheralHardwareException {
        return devices.getWaitingPeripheral(peripheralKey);
    }

    /**
     * Retrieves a driver which is awaiting to have a peripheral software driver set
     * @param peripheralKey
     * @return
     * @throws PeripheralHardwareException 
     */
    public static PeripheralController getDriverWaitingHardwarePeripheral(String peripheralKey) throws PeripheralHardwareException {
        return devices.getWaitingPeripheral(peripheralKey);
    }

    /**
     * Loads a peripheral by it's driver id.
     * @param deviceKey
     * @param peripheralSoftwareDriverId
     * @param version
     * @param storeData
     * @return
     * @throws PeripheralHardwareException 
     */
    public static boolean loadPeripheralWithDriverId(String deviceKey, String peripheralSoftwareDriverId, String version, boolean storeData) throws PeripheralHardwareException {
        LOG.debug("Loading peripheral driver for device '{}' by user set id. Driver: {}, version: {} (Store? : {})", deviceKey, peripheralSoftwareDriverId, version, storeData);
        return devices.setPeripheralSoftwareDriverId(deviceKey, peripheralSoftwareDriverId, version, storeData);
    }
    
    /**
     * Responsible to send the given command to the device from RPC.
     * @param deviceId
     * @param cmdGroup
     * @param cmdSet
     * @param action
     * @param force In case a device is locked (because of scene) set to true to override the scene lock and modify device state.
     * @return Results of the command send.
     * @throws org.pidome.server.services.hardware.DeviceServiceException
     */
    public static boolean sendDevice(int deviceId, String cmdGroup, String cmdSet, Map<String,Object> action, boolean force) throws DeviceServiceException {
        try {
            return Devices.send(deviceId, cmdGroup, cmdSet, action, force);
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * Returns a list of possible drivers for a specific vendorId and productId.
     * These vendor and product id's are the official vendor and product id. Unless these are pidome specific.
     * @param vendorId
     * @param productId
     * @return 
     */
    public static List<Map<String,String>> getPossiblePeripheralSoftwareDrivers(String hardwareDriverName, String vendorId, String productId){
        return packages.getPeripheralDriversForPeripheral(hardwareDriverName, vendorId, productId);
    }
    
    /**
     * Responsible for adding a device command to a batch.
     * A batch is a list of commands which mostly contains commands from an event.
     * @param deviceId
     * @param cmdGroup
     * @param cmdControl
     * @param value
     * @param extra
     * @param batchName The name of the batch (if coming from an event it will be a name composed by the EventService)
     */
    public static void addBatch(int deviceId, String cmdGroup, String cmdControl, Object value, String extra, String batchName) {
        devices.addBatch(deviceId, cmdGroup, cmdControl, value, extra, batchName);
    }
    
    /**
     * Runs the batch previously created or commands added by the addBatch command.
     * @see #addBatch(java.lang.String, java.lang.String, java.lang.String, java.lang.String);
     * @see EventService
     * @param batchName The name of the batch (if coming from an event it will be a name composed by the EventService)
     */
    public static void runBatch(String batchName){
        devices.runBatch(batchName);
    }

    /**
     * Returns a list of all enabled devices.
     * @return The list of all enabled devices as reported by the package manager
     * @throws org.pidome.server.services.hardware.DeviceServiceException
     * @see Packages
     */
    public static Map<Integer,Map<String,String>> getAllEnabledDevices() throws DeviceServiceException {
        try {
            return devices.getAllEnabledDevices();
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * Returns a list of ALL the declared devices, active or not.
     * If you have got a lot of devices, well we have a problem
     * @return The list of declared devices in the DB.
     */
    public static List<Map<String,Object>> getAllDeclaredDevices(){
        return getAllDeclaredDevices(null);
    }
    
    /**
     * Returns a all devices based on the device package name, unless device is NULL, it then returns everything from the db.
     * This function checks if a device is loaded or not.
     * @param device The device package name.
     * @return The list, or a single device.
     */
    public static List<Map<String,Object>> getAllDeclaredDevices(String device){
        return Devices.getAllDeclaredDevices(device);
    }
    
    /**
     * As getAllDeclaredDevices but then on deviceId as set in the DB
     * @param deviceId As set in the DB
     * @return a single device.
     * @see #getAllDeclaredDevices(java.lang.String) (for reference)
     */
    public static Map<String, Object> getDeclaredDevice(int deviceId) {
        return Devices.getDeclaredDevice(deviceId);
    }
    
    /**
     * Returns an installed device from the database limited by the driver.
     * @param deviceDriver The device driver path
     * @param driverId the id of the driver required.
     * @return 
     */
    public static Map<String,Object> getInstalledDeviceByDriverName(String deviceDriver, int driverId){
        return Devices.getInstalledDeviceByDriverName(deviceDriver, driverId);
    }
    
    /**
     * Deletes a device.
     * @param deviceId
     * @return true when deleted
     * @throws org.pidome.server.services.hardware.DeviceServiceException
     */
    public static boolean deleteDevice(int deviceId) throws DeviceServiceException{
        try {
            return Devices.deleteDevice(deviceId);
        } catch (DevicesException ex) {
            throw new DeviceServiceException(ex);
        }
    }
    
    /**
     * Adds a new device.
     * @param device
     * @param location
     * @param address
     * @param name
     * @param category
     * @param favorite
     * @param settings
     * @return true when ok
     * @throws org.pidome.server.services.hardware.DeviceServiceException
     */
    public static boolean saveDevice(int device, int location, String address, String name, int category, boolean favorite, Map settings,List<Map<String,Object>> modifiers) throws DeviceServiceException {
        try {
            return Devices.saveDevice(device, location, address, name, category, favorite, settings,modifiers);
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex);
        }
    }
    
    /**
     * Returns a single device instance.
     * @param deviceId
     * @return DeviceInterfaceImpl
     * @throws UnknownDeviceException 
     */
    public static DeviceInterface getDeviceInstance(int deviceId) throws UnknownDeviceException {
        return Devices.getDevice(deviceId);
    }
    
    /**
     * Returns a single device instance.
     * @param deviceId
     * @return DeviceInterfaceImpl
     * @throws UnknownDeviceException 
     */
    public static DeviceInterface getOfflineDeviceInstance(int deviceId) throws UnknownDeviceException {
        return Devices.getOfflineDeviceInstance(deviceId);
    }
    
    /**
     * Edits a known device.
     * @param deviceId
     * @param location
     * @param address
     * @param name
     * @param category
     * @param favorite
     * @param settings
     * @return 
     * @throws org.pidome.server.services.hardware.DeviceServiceException 
     */
    public static boolean editDevice(int deviceId, int location, String address, String name, int category, boolean favorite, Map<String,Object> settings,List<Map<String,Object>> modifiers) throws DeviceServiceException {
        try {
            return Devices.editDevice(deviceId, location, address, name, category, favorite, settings, modifiers);
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex);
        }
    }
    
    /**
     * returns the list of currently active devices.
     * @return Map<String,DeviceInterfaceImpl>
     */
    public static Map<Integer,DeviceInterface> getActiveDeviceList(){
        return devices.getLoadedDevices();
    }
    
    /**
     * Returns a list of custom created devices.
     * @return 
     */
    public static Map<Integer,Map<String,String>> getAllCustomDevices(){
        return devices.getAllCustomDevices();
    }
    
    /**
     * Returns a list of custom created devices.
     * @param namedId
     * @return 
     */
    public static Map<String,Object> getCustomDevice(String namedId){
        return devices.getCustomDevice(namedId);
    }
    
    /**
     * Returns a list of all possible custom devices.
     * @return 
     */
    public static ArrayList<Map<String,Object>> getDeclaredCustomDevices(){
        return devices.getDeclaredCustomDevices();
    }
    
    /**
     * Deletes a custom device.
     * @param deviceId
     * @return 
     */
    public static boolean deleteCustomDevice(int deviceId){
        return devices.deleteCustomDevice(deviceId);
    }
    
    /**
     * Updates a custom device.
     * @param deviceId
     * @param friendlyname
     * @param struct
     * @return 
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException When device is incorrect or xml can not be read correctly.
     */
    public static boolean updateCustomDevice(int deviceId, String friendlyname, String name, Map<String,Object> struct) throws UnsupportedDeviceException {
        return devices.updateCustomDevice(deviceId, friendlyname, name, struct);
    }
    
    /**
     * Assigns a custom device to a driver.
     * All devices based on the custom device are stopped. If the given driver is running all devices are started again with the assigned driver.
     * @param customDriverId
     * @param customDeviceId
     * @return
     * @throws DeviceServiceException 
     */
    public static boolean assignCustomDevice(int customDriverId, int customDeviceId) throws DeviceServiceException {
        return devices.assignCustomDevice(customDriverId, customDeviceId);
    }
    
    /**
     * Returns a custom device.
     * @param deviceId
     * @return 
     */
    public static Map<String,Object> getCustomDevice(int deviceId){
        return devices.getCustomDevice(deviceId);
    }
    
    /**
     * Returns a JSON formed device structure.
     * @param installedId
     * @return 
     * @throws org.pidome.server.services.hardware.DeviceServiceException 
     */
    public static Map<String,Object> getDeviceStruct(int installedId) throws DeviceServiceException {
        return devices.getDeviceStruct(installedId);
    }
    
    /**
     * Return an object instance of a non coupled installed device.
     * @param installedDeviceId The id of the installed device.
     * @return An object instance of an installed device.
     * @throws UnknownDeviceException 
     */
    public static Map<String,Object> getInstalledDeviceSettings(int installedDeviceId) throws UnknownDeviceException, DevicesException {
        try {
            DeviceStruct device = (DeviceStruct)devices.getInstalledDeviceInstance(installedDeviceId);

            Map<String,Object> settingsCollection = new HashMap<>();

            Map<String,Object> optionsCollection  = new HashMap<>();

            optionsCollection.put("struct", device.getDeviceOptions().getOriginalSet());
            optionsCollection.put("values", device.getDeviceOptions().getSimpleSettingsMap());

            Map<String,Object> addressStruct = new HashMap<>();
            addressStruct.put("inputtype", device.getAddressing().getInputType());
            addressStruct.put("label", device.getAddressing().getAddressInputDescription());
            addressStruct.put("description", device.getAddressing().getLargeAddressDescription());
            addressStruct.put("currentvalue", device.getAddressing().getAddress());

            
            Map<String,Object> deviceInfo = new HashMap<>();
            deviceInfo.put("packageid", device.getPackageId());
            deviceInfo.put("softwaredriverid", device.getDeviceDriverDriverId());
            deviceInfo.put("name", device.getName());
            
            settingsCollection.put("address", addressStruct);
            settingsCollection.put("options", optionsCollection);
            settingsCollection.put("deviceinfo", deviceInfo);

            return settingsCollection;
        } catch (ClassNotFoundException ex) {
            throw new UnknownDeviceException(ex);
        }
            
    }
    
    /**
     * Returns device settings.
     * @param deviceId
     * @return
     * @throws DeviceServiceException 
     * @throws org.pidome.server.connector.drivers.devices.UnknownDeviceException 
     */
    public static Map<String,Object> getDeviceSettings(int deviceId) throws DeviceServiceException, UnknownDeviceException {
        Map<String,Object> settingsCollection = new HashMap<>();
        DeviceStruct device;
        try {
            device = (DeviceStruct)DeviceService.getDeviceInstance(deviceId);
        } catch (UnknownDeviceException ex){
            device = (DeviceStruct)DeviceService.getOfflineDeviceInstance(deviceId);
        }
        
        Map<String,Object> deviceColletion = new HashMap<>();
        deviceColletion.put("id", device.getId());
        deviceColletion.put("name", device.getDeviceName());
        deviceColletion.put("location", device.getLocationId());
        deviceColletion.put("category", device.getCategoryId());
        deviceColletion.put("favorite", device.getIsFavorite());
        
        List<Map<String,Object>> modifiersCollection = new ArrayList<>();
        for(DeviceControlsGroup group : device.getFullCommandSet().getControlsGroups().values()){
            for(DeviceControl control : group.getGroupControls().values()){
                if(control.isModifierCompatible()){
                    Map<String,Object> base = new HashMap<>();
                    base.put("group", group.getGroupId());
                    base.put("grouplabel", group.getGroupLabel());
                    base.put("control", control.getControlId());
                    base.put("controllabel", control.getDescription());
                    base.put("datatype", control.getDataType().toString());
                    base.put("modifierid", control.getModifierId());
                    modifiersCollection.add(base);
                }
            }
        }
        
        Map<String,Object> optionsCollection  = new HashMap<>();
        
        optionsCollection.put("struct", device.getDeviceOptions().getOriginalSet());
        optionsCollection.put("values", device.getDeviceOptions().getSimpleSettingsMap());
        
        Map<String,Object> addressStruct = new HashMap<>();
        addressStruct.put("inputtype", device.getAddressing().getInputType());
        addressStruct.put("label", device.getAddressing().getAddressInputDescription());
        addressStruct.put("description", device.getAddressing().getLargeAddressDescription());
        addressStruct.put("currentvalue", device.getAddressing().getAddress());
        
        settingsCollection.put("address", addressStruct);
        settingsCollection.put("options", optionsCollection);
        settingsCollection.put("device", deviceColletion);
        settingsCollection.put("modifiers", modifiersCollection);
        
        return settingsCollection;
        
    }
    
    /**
     * Creates a custom device.
     * @param identifier
     * @param friendlyname
     * @param deviceDriver
     * @param struct
     * @param driverId
     * @param packageId
     * @return
     * @throws UnsupportedDeviceException 
     */
    public static int createCustomDevice(String identifier, String friendlyname, String deviceDriver, Map<String,Object> struct, int driverId, int packageId) throws UnsupportedDeviceException {
        return devices.createCustomDevice(identifier, friendlyname, deviceDriver, struct, driverId, packageId);
    }
    
    /**
     * Returns a single loaded device.
     * @param deviceId The id of the device as known in the DB.
     * @return The device.
     * @throws UnknownDeviceException 
     */
    public static DeviceInterface getDevice(int deviceId) throws UnknownDeviceException {
        return Devices.getDevice(deviceId);
    }
    
    /**
     * Returns a list of personal custom devices.
     * @return
     * @throws DeviceServiceException 
     */
    public static ArrayList<Map<String,Object>> getPersonalCustomDevices() throws DeviceServiceException {
        return devices.getPersonalCustomDevices();
    }
    
    /**
     * Returns a list of all active devices.
     * @return 
     */
    public static List<DeviceInterface> getActiveDevices(){
        return Devices.getActiveDevices();
    }
    
    /**
     * Returns just an device instance with what you can't do a thing, one usability is to retrieve the XML for the device type.
     * @param deviceId
     * @return DeviceInterfaceImpl 
     * @throws org.pidome.server.connector.drivers.devices.UnknownDeviceException 
     */
    public static DeviceInterface getLooseInstance(int deviceId) throws UnknownDeviceException, DevicesException {
        try {
            return devices.getLooseInstance(deviceId);
        } catch (ClassNotFoundException ex){
            throw new UnknownDeviceException(ex);
        }
    }
    
    /**
     * Tries to start a device.
     * @param deviceId
     * @return
     * @throws DeviceServiceException 
     */
    public static boolean startDevice(int deviceId) throws DeviceServiceException {
        try {
            Devices.loadSingleDeviceInstance(deviceId);
            return true;
        } catch (DevicesException ex) {
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * Set a device as favorite.
     * @param deviceId
     * @param favorite
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    public static boolean setAsFavorite(int deviceId, boolean favorite) throws DeviceServiceException, UnknownDeviceException{
        try {
            Devices.setFavorite(deviceId, favorite);
            return true;
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * Sets dimensions for a device.
     * @param deviceId
     * @param x
     * @param y
     * @return
     * @throws DeviceServiceException 
     */
    public static boolean setVisualDimenions(int deviceId, int x, int y) throws DeviceServiceException {
        try {
            return Devices.setVisualDimenions(deviceId, x, y);
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * Removes dimensions.
     * @param deviceId
     * @return
     * @throws DeviceServiceException 
     */
    public static boolean removeVisualDimenions(int deviceId) throws DeviceServiceException {
        try {
            return Devices.removeVisualDimenions(deviceId);
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * Gets devices for a floor.
     * @param floorId
     * @return
     * @throws DeviceServiceException 
     */
    public static List<Map<String,Object>> getVisualDevices(int floorId) throws DeviceServiceException {
        try {
            return Devices.getVisualDevices(floorId);
        } catch (DevicesException ex){
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    public static ArrayList<String> getFilteredCustomDeviceSet() throws ConfigPropertiesException, IOException {
        return devices.getFilteredCustomDeviceSet();
    }
    
    public static void createCustomSerialDevice(String port, String friendlyName) throws PeripheralHardwareException, ConfigPropertiesException, IOException {
        devices.createCustomSerialDevice(port, friendlyName);
    }
    
    public static final List<Map<String,Object>> getScriptedDrivers(){
        return devices.getScriptedDrivers();
    }
    
    public static boolean updateScriptedDriver(int instanceFor, int driverId, String name, String description, String script){
        return devices.updateScriptedDriver(instanceFor, driverId, name, description, script);
    }
    
    public static Map<String,Object> getScriptedDriver(int id){
        return devices.getScriptedDriver(id);
    }
    
    @Override
    public void interrupt() {
        if(devices!=null) devices.stop();
    }

    @Override
    public void start() {
        devices.init();
        devices.start();
    }

    @Override
    public boolean isAlive() {
        return devices!=null && Hardware.isRunning().getValue();
    }
    
    @Override
    public String getServiceName() {
        return "Devices service";
    }

}