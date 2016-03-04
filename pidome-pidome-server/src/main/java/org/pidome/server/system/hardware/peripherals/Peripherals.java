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

package org.pidome.server.system.hardware.peripherals;

import java.io.File;
import java.io.IOException;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.emulators.PluginPeripheral;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.hardware.Hardware;
import org.pidome.server.system.hardware.HardwareEvent;
import org.pidome.server.system.hardware.HardwareListener;
import org.pidome.server.system.hardware.peripherals.emulators.HardwarePluginDeviceEmulator;
import org.pidome.server.system.packages.PackagePermissionsNotUpToDateException;

/**
 * Loads the peripheral hardware driver and attaches this to the base peripheral.
 * After loading the hardware driver it will call functions to initialize the hardware driver (which the programmer should handle) and starts the hardware driver.
 * After starting the driver an event will be fired for the driver class to load the software driver for this device.
 * 
 * When a driver is started the underlying peripheral base class will ask the driver for a software id running on the peripheral. IF this is reported the drivers class
 * will handle this.
 * 
 * @author John Sirach
 */
public class Peripherals extends Hardware implements HardwareListener {
    
    /**
     * If the base implementations are loaded.
     */
    Boolean loaded = false;
    
    /**
     * Collection of running peripherals.
     */
    Map<String,PeripheralController> peripheralCollection = new HashMap<>(); 
    /**
     * Collection of peripherals waiting to have settings to be set.
     */
    Map<String,PeripheralController> waitingPeripherals = new HashMap<>();
    
    /**
     * Unsupported peripherals which are needed to be added to the system first.
     */
    Map<String,PeripheralController> unsupportedPeripherals = new HashMap<>();
    
    /**
     * Peripheral listeners.
     */
    List _listeners = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(Peripherals.class);
    
    /**
     * Constructor.
     */
    protected Peripherals(){
        super();
        addHardwareListener(this);
    }
    
    /**
     * Loads the peripheral hardware driver for the given peripheral device.
     * This fires an event when a driver has been loaded succesfully.
     * @param peripheralDevice 
     */
    protected final void loadPeripheralDriver(PeripheralController peripheralDevice){
        try {
            if(peripheralDevice.getPeripheral().getDevicePort()==null){
                throw new PeripheralHardwareException("A port is needed to initialize hardware. A missing port indicates unsupported hardware.");
            }
            waitingPeripherals.put(peripheralDevice.getPeripheral().getDeviceKey(), peripheralDevice);
            /// First check if this peripheral has been saved before with a config.
            LOG.info("Trying to load '{}' hardware driver using saved configuration.", peripheralDevice.getPeripheral().getFriendlyName());
            resumeSavedPeripheral(peripheralDevice.getPeripheral().getDeviceKey());
            peripheralDevice.getPeripheral().error(null);
        } catch (UnsupportedPeripheralActionException ex){
            try {
                LOG.info("Using vid '{}' and pid '{}' for loading {}.", peripheralDevice.getPeripheral().getVendorId(), peripheralDevice.getPeripheral().getDeviceId(), peripheralDevice.getPeripheral().getFriendlyName());
                ///If no config is found, load a driver by it's vid/pid
                loadDriverByVidPid(peripheralDevice);
            } catch (ClassNotFoundException ex1) {
                try {
                    LOG.info("No config or vendor and product id based driver found, fall back on subsystem '{}, {}' with user settings",peripheralDevice.getPeripheral().getDeviceType(), peripheralDevice.getPeripheral().getSubSystem().toString());
                    /// Last resort, load a default fallback hardware driver.
                    loadDriverBySubType(peripheralDevice);
                } catch (PackagePermissionsNotUpToDateException ex2){
                    registerPackagePermissionsException(ex2, peripheralDevice);
                } catch (PeripheralHardwareException ex2) {
                    registerPeripheralHardwareException(ex2, peripheralDevice);
                } catch (ClassNotFoundException ex2) {
                    LOG.warn("Hardware driver not found for '{}'. Not loaded, Device not compatible with PiDome: {}", peripheralDevice.getPeripheral().getFriendlyName(), ex2.getMessage());
                }
            } catch (PackagePermissionsNotUpToDateException ex1) {
                registerPackagePermissionsException(ex1, peripheralDevice);
            } catch (PeripheralHardwareException ex1) {
                registerPeripheralHardwareException(ex1, peripheralDevice);
            }
        } catch (PackagePermissionsNotUpToDateException ex){
            registerPackagePermissionsException(ex, peripheralDevice);
        } catch (PeripheralHardwareException ex1) {
            registerPeripheralHardwareException(ex1, peripheralDevice);
        }
    }
    
    private void registerPeripheralHardwareException(Exception ex, PeripheralController peripheralDevice){
        LOG.error("Problem starting peripheral '"+peripheralDevice.getPeripheral().getFriendlyName()+"': " + ex.getMessage(), ex);
        peripheralDevice.getPeripheral().error("Problem starting peripheral: " + ex.getMessage());
        unsupportedPeripherals.put(peripheralDevice.getPeripheral().getDeviceKey(), waitingPeripherals.remove(peripheralDevice.getPeripheral().getDeviceKey()));
    }
    
    private void registerPackagePermissionsException(Exception ex, PeripheralController peripheralDevice){
        LOG.error("Hardware not loaded, package permissions are not correct: " + ex.getMessage(), ex);
        peripheralDevice.getPeripheral().error("Hardware not loaded, package permissions are not correct: " + ex.getMessage());
        unsupportedPeripherals.put(peripheralDevice.getPeripheral().getDeviceKey(), waitingPeripherals.remove(peripheralDevice.getPeripheral().getDeviceKey()));
    }
    
    /**
     * Loads a peripheral driver by it's vendor/product id.
     * @param peripheralDevice
     * @return
     * @throws ClassNotFoundException
     * @throws PackagePermissionsNotUpToDateException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws PeripheralHardwareException 
     */
    private boolean loadDriverByVidPid(PeripheralController peripheralDevice) throws ClassNotFoundException, PackagePermissionsNotUpToDateException, PeripheralHardwareException{
        PeripheralHardwareDriverInterface peripheralHardwareDriver = getPackageLoader().loadPeripheralDriverByVidPid(peripheralDevice.getPeripheral().getVendorId(),peripheralDevice.getPeripheral().getDeviceId());
        finalizeDriverInit(peripheralDevice, peripheralHardwareDriver);
        LOG.debug("Loaded '{}' driver using vid-pid combination with software driver '{}'.", peripheralDevice.getPeripheral().getFriendlyName(), peripheralHardwareDriver.getFriendlyName());
        return true;
    }
    
    /**
     * Loads a driver based on the peripheral subtype.
     * These subtypes can be serial, hid, emulator, bluetooth etc..
     * @param peripheralDevice
     * @return
     * @throws ClassNotFoundException
     * @throws PackagePermissionsNotUpToDateException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws PeripheralHardwareException 
     */
    private boolean loadDriverBySubType(PeripheralController peripheralDevice) throws ClassNotFoundException, PackagePermissionsNotUpToDateException, PeripheralHardwareException {
        PeripheralHardwareDriverInterface peripheralHardwareDriver = getPackageLoader().loadPeripheralDriverBySubSystem(peripheralDevice.getPeripheral().getDeviceType(),peripheralDevice.getPeripheral().getSubSystem().toString());
        finalizeDriverInit(peripheralDevice, peripheralHardwareDriver);
        LOG.debug("Loaded '{}' driver using subsystem '{}' with software driver '{}'.", peripheralDevice.getPeripheral().getFriendlyName(), peripheralDevice.getPeripheral().getSubSystem(), peripheralHardwareDriver.getFriendlyName());
        return true;
    }
    
    /**
     * Finalizes driver loading and attaches the hardware driver to the hardware.
     * @param peripheralDevice
     * @param peripheralHardwareDriver
     * @throws PeripheralHardwareException 
     */
    private void finalizeDriverInit(PeripheralController peripheralController, PeripheralHardwareDriverInterface peripheralHardwareDriver) throws PeripheralHardwareException {
        peripheralController.setPeripheralHardwareDriver(peripheralHardwareDriver);
        switch(peripheralController.getPeripheral().getDeviceType()){
            case Peripheral.TYPE_PLUGIN:
                DevicePlugin plugin = ((HardwarePluginDeviceEmulator)peripheralController.getPeripheral()).getPlugin();
                ((PluginPeripheral)peripheralHardwareDriver).setPluginLink(plugin);
                peripheralController.setSoftwareDriverId(plugin.getExpectedDriverId(), plugin.getExpectedDriverVersion());
            break;
        }
        if(peripheralHardwareDriver.hasPeripheralOptions()){
            waitingPeripherals.put(peripheralController.getPeripheral().getDeviceKey(), peripheralController);
        } else {
            startPeripheralInitialization(peripheralController, false);
        }
        peripheralController.getPeripheral().error(null);
    }
    
    /**
     * Resumes initialization of a peripheral stored in a settings file.
     * @param key
     * @param peripheral
     * @throws UnsupportedPeripheralActionException 
     */
    private boolean resumeSavedPeripheral(String key) throws UnsupportedPeripheralActionException, PackagePermissionsNotUpToDateException, PeripheralHardwareException {
        PeripheralController peripheral = getControllerByPeripheral(key);
        try {
            if(peripheral.resumable()){
                
                Map<String,Object> params = peripheral.getResumeSettings();
                
                peripheral.setPeripheralHardwareDriver(getPackageLoader().loadPeripheralDriverByNamedId((String)params.get("namedid")));
                peripheral.getPeripheralHardwareDriver().setPeripheralOptions((Map<String,String>)params.get("hwdriveroptions"));
                
                peripheral.setSoftwareDriverId((String)params.get("swdriverid"), (String)params.get("swdriverversion"));
                
                LOG.debug("Loading {} from saved configuration with hardware driver '{}'", peripheral.getPeripheral().getFriendlyName(), peripheral.getPeripheralHardwareDriver().getFriendlyName());

                peripheral.getPeripheral().error(null);
                startPeripheralInitialization(peripheral, false);
                
                return true;
            }
        } catch (PeripheralHardwareException | ClassNotFoundException | ConfigPropertiesException ex) {
            throw new UnsupportedPeripheralActionException(ex);
        }
        throw new UnsupportedPeripheralActionException("No peripheral with key: " + key);
    }
    
    /**
     * Returns a list of attached peripherals awaiting user interaction.
     * @return 
     */
    public final Map<String,PeripheralController> getWaitingPeripherals(){
        return waitingPeripherals;
    }
    
    /**
     * Returns a list of attached peripherals awaiting user interaction.
     * @return 
     */
    public final Map<String,PeripheralController> getRunningPeripherals(){
        return peripheralCollection;
    }
    
    /**
     * Returns a list of attached peripherals which are unsupported waiting for user addition.
     * @return 
     */
    public final Map<String,PeripheralController> getAttachedUnsupportedPeripherals(){
        return unsupportedPeripherals;
    }
    
    /**
     * Gets the specific peripheral device awaiting user interaction
     * @param peripheralKey
     * @return
     * @throws PeripheralHardwareException 
     */
    public final PeripheralController getWaitingPeripheral(String peripheralKey) throws PeripheralHardwareException {
        if(waitingPeripherals.containsKey(peripheralKey)){
            return waitingPeripherals.get(peripheralKey);
        } else {
            throw new PeripheralHardwareException("Peripheral with key '"+peripheralKey+"' is not known,unsupported or already started");
        }
    }
    
    /**
     * Sets driver parameters.
     * @param peripheralKey
     * @param optionsNvp
     * @throws PeripheralHardwareException 
     */
    public final void setDriverParameters(String peripheralKey, Map<String,String>optionsNvp) throws PeripheralHardwareException {
        if(waitingPeripherals.containsKey(peripheralKey)){
            if(waitingPeripherals.get(peripheralKey).getPeripheralHardwareDriver().hasPeripheralOptions()){
                waitingPeripherals.get(peripheralKey).getPeripheralHardwareDriver().setPeripheralOptions(optionsNvp);
            }
        }
    }
    
    /**
     * Stores peripheral settings.
     * @param deviceKey The key the peripheral is registered on.
     * @param peripheralSoftwareDriverId The software driver id used.
     * @param version The version of the software driver id.
     * @param saveData The data to be saved with this peripheral.
     * @return  
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException  
     */
    public final synchronized boolean setPeripheralSoftwareDriverId(String deviceKey, String peripheralSoftwareDriverId, String version, boolean saveData) throws PeripheralHardwareException {
        LOG.debug("Trying to start peripheral and driver with: {} and {} version {} (save settings?: {})",deviceKey, peripheralSoftwareDriverId, version, saveData);
        if(this.peripheralCollection.containsKey(deviceKey)){
            throw new PeripheralHardwareException("Peripheral already started");
        } else if (this.unsupportedPeripherals.containsKey(deviceKey)){
            throw new PeripheralHardwareException("Peripheral is unsupported");
        } else if(waitingPeripherals.containsKey(deviceKey)){
            PeripheralController controller = this.getControllerByPeripheral(deviceKey);
            controller.setSoftwareDriverId(peripheralSoftwareDriverId, version);
            resumeHardwareInitialization(deviceKey, saveData);
            return true;
        } else {
            throw new PeripheralHardwareException("Unknown peripheral selected.");
        }
    }
    
    /**
     * Resumes initialization of the attached peripheral after user interaction.
     * @param peripheralKey
     * @return true when driver started
     * @throws PeripheralHardwareException 
     */
    public final boolean resumeHardwareInitialization(String peripheralKey, boolean saveData) throws PeripheralHardwareException {
        if(waitingPeripherals.containsKey(peripheralKey)){
            startPeripheralInitialization(waitingPeripherals.get(peripheralKey), saveData);
            return true;
        } else if (peripheralCollection.containsKey(peripheralKey)){
            throw new PeripheralHardwareException("Peripheral with key '"+peripheralKey+"' is already started");
        } else {
            throw new PeripheralHardwareException("Peripheral with key '"+peripheralKey+"' is unknown");
        }
    }
    
    /**
     * Starts the initialization of the peripheral hardware driver.
     * @param peripheralDevice
     * @throws PeripheralHardwareException 
     */
    final void startPeripheralInitialization(PeripheralController peripheralDevice, boolean saveData) throws PeripheralHardwareException {
        peripheralDevice.startPeripheralInitialization();
        peripheralCollection.put(peripheralDevice.getPeripheral().getDeviceKey(), waitingPeripherals.remove(peripheralDevice.getPeripheral().getDeviceKey()));
        if(saveData){
            peripheralDevice.storeSettings();
        }
        _firePeripheralEvent(peripheralDevice, PeripheralEvent.DRIVER_LOADED);
    }

    /**
     * Adds a specific loaded peripheral software driver to the peripheral.
     * @param deviceKey
     * @param peripheralSoftwareDriver 
     */
    protected final synchronized void addPeripheralSoftwareDriver(String deviceKey, PeripheralSoftwareDriverInterface peripheralSoftwareDriver){
        if(peripheralCollection.containsKey(deviceKey)){
            peripheralCollection.get(deviceKey).addPeripheralSoftwareDriver(peripheralSoftwareDriver);
            LOG.debug("Connected {} to {}", peripheralSoftwareDriver.getName(), peripheralCollection.get(deviceKey).getPeripheral().getFriendlyName());
        }
    }
    
    /**
     * Unloads a peripheral software driver for the given peripheral device.
     * This fires an event so the devices can unload them self and then unloads the driver.
     * @param device 
     */
    protected final void unLoadPeripheralDriver(PeripheralController device) throws PeripheralHardwareException {
        LOG.debug("Stopping {} on port {}", device.getPeripheral().getFriendlyName(),device.getPeripheral().getDevicePort());
        switch(device.getPeripheral().getDeviceType()){
            case Peripheral.TYPE_PLUGIN:
                ((PluginPeripheral)device).removePluginLink();
            break;
        }
        _firePeripheralEvent(device, PeripheralEvent.DRIVER_UNLOAD);
        device.stopHardwareDriver();
        device.releaseHardwareDriver();
        waitingPeripherals.remove(device.getPeripheral().getDeviceKey());
        peripheralCollection.remove(device.getPeripheral().getDeviceKey());
        unsupportedPeripherals.remove(device.getPeripheral().getDeviceKey());
        LOG.info("Peripheral stopped.");
    }

    /**
     * Unload a running peripheral and put it back in the waiting list.
     * This should only be used with intended release via the web interface.
     */
    private void unLoadRunningPeripheralDriver(final PeripheralController device) throws PeripheralHardwareException{
        LOG.info("Stopping {} on port {} (back in waiting list as it is an intended release)", device.getPeripheral().getFriendlyName(),device.getPeripheral().getDevicePort());
        switch(device.getPeripheral().getDeviceType()){
            case Peripheral.TYPE_PLUGIN:
                ((PluginPeripheral)device).removePluginLink();
            break;
        }
        _firePeripheralEvent(device, PeripheralEvent.DRIVER_UNLOAD);
        device.stopHardwareDriver();
        device.releaseHardwareDriver();
        if(peripheralCollection.containsKey(device.getPeripheral().getDeviceKey())){
            waitingPeripherals.put(device.getPeripheral().getDeviceKey(), peripheralCollection.remove(device.getPeripheral().getDeviceKey()));
        }
    }
    
    /**
     * When an user indicates it no longer want's to use a peripheral, use this.
     * Because this is intentional, also settings should be removed.
     * @param device 
     */
    public final void unloadHardwareByUser(final PeripheralController device){
        Runnable run = () -> {
            try {
                unLoadRunningPeripheralDriver(device);
                forgetPeripheral(device);
            } catch (PeripheralHardwareException ex) {
                LOG.error("Could not unload peripheral: {}", ex.getMessage());
            }
        };
        run.run();
    }
    
    /**
     * Removes the peripheral's save file.
     * @param device 
     */
    protected final void forgetPeripheral(PeripheralController device){
        try {
            File file = device.getPortSaveFile();
            file.delete();
        } catch (ConfigPropertiesException ex) {
            LOG.warn("Could not get port file: {}", ex.getMessage());
        }
    }
    
    /**
     * Unloads a peripheral software driver for the given peripheral device.
     * Panic mode should only be used when a device is disconnected without stopping the driver. If a user does this, slap him/her
     * This fires an event so the devices can unload themself and then unloads the driver.
     * @param controller 
     */
    protected final void panicUnLoadPeripheralDriver(PeripheralController controller){
        LOG.debug("Panic!! Stopping {} on port {}", controller.getPeripheral().getFriendlyName(),controller.getPeripheral().getDevicePort());
        controller.getPeripheral().stopHardwareDriver();
        controller.getPeripheral().releaseHardwareDriver();
        _firePeripheralEvent(controller, PeripheralEvent.DRIVER_UNLOAD);
        waitingPeripherals.remove(controller.getPeripheral().getDeviceKey());
        peripheralCollection.remove(controller.getPeripheral().getDeviceKey());
        unsupportedPeripherals.remove(controller.getPeripheral().getDeviceKey());
        LOG.info("Peripheral {} on port {} stopped.", controller.getPeripheral().getFriendlyName(), controller.getPeripheral().getDevicePort());
    }
    
    /**
     * This function catches changes in the hardware.
     * @param event 
     */
    @Override
    public final void hardwareChange(HardwareEvent event) {
        Peripheral device = event.getSource();
        switch(event.getEventType()){
            case HardwareEvent.HARDWARE_ADDED:
                loadPeripheralDriver(new PeripheralController(device));
            break;
            case HardwareEvent.HARDWARE_REMOVED:
                try {
                    panicUnLoadPeripheralDriver(getControllerByPeripheral(device.getDeviceKey())); ///Panic! unless the driver has already been stopped.
                } catch (PeripheralHardwareException ex) {
                    LOG.warn(ex.getMessage());
                }
            break;
        }
        
    }
    
    private PeripheralController getControllerByPeripheral(String key) throws PeripheralHardwareException {
        if(peripheralCollection.containsKey(key)){
            return peripheralCollection.get(key);
        }
        if(waitingPeripherals.containsKey(key)){
            return waitingPeripherals.get(key);
        }
        if(unsupportedPeripherals.containsKey(key)){
            return unsupportedPeripherals.get(key);
        }
        throw new PeripheralHardwareException("Peripheral with key '"+key+"' not connected");
    }
    
    /**
     * Add listeners for changed hardware.
     * @param l 
     */
    protected final synchronized void addPeripheralListener( PeripheralEventListener l ) {
        _listeners.add( l );
        LOG.debug("Added eventlistener: {}", l.getClass().getName());
    }
    
    /**
     * Removes listeners for changed hardware.
     * @param l 
     */
    protected final synchronized void removePeripheralListener( PeripheralEventListener l ) {
        _listeners.remove( l );
        LOG.debug("Removed eventlistener: {}", l.getClass().getName());
    }
    
    /**
     * Fires events after a hardware change has been detected.
     * @param device
     * @param eventType 
     */
    protected final synchronized void _firePeripheralEvent(PeripheralController device, String eventType) {
        LOG.debug("Event: {}", eventType);
        PeripheralEvent event = new PeripheralEvent(device, eventType);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (PeripheralEventListener) listeners.next() ).handlePeripheralEvent( event );
        }
    }
    
    /**
     * Creates a custom serial device.
     * This creates a custom serial device to be used for server interaction.
     * @param port
     * @param friendlyName
     * @throws PeripheralHardwareException
     * @throws ConfigPropertiesException
     * @throws IOException 
     */
    @Override
    public final void createCustomSerialDevice(String port, String friendlyName) throws PeripheralHardwareException, ConfigPropertiesException, IOException {
        super.createCustomSerialDevice(port, friendlyName);
    }
    
    public final ArrayList<String> getFilteredCustomDeviceSet() throws ConfigPropertiesException, IOException {
        String devLoc = SystemConfig.getProperty("system", "server.linuxdevlocation");
        List<String> inUse = getMergedUsedPorts();
        return new ArrayList<>(Arrays.asList(
                Files.list(new File(devLoc).toPath())
                .filter(p -> (p.getFileName().toString().matches("tty\\D+.*") && !p.getFileName().toString().contains("ttyprintk") && !inUse.contains(p.getFileName().toString())))
                .map(String::valueOf)
                .toArray(size -> new String[size])
        ));
    }
    
    private List<String> getMergedUsedPorts(){
        List<String> used = new ArrayList<>();
        for(PeripheralController peripheral:peripheralCollection.values()){
            try {
                used.add(peripheral.getPeripheral().getDevicePort().substring(peripheral.getPeripheral().getDevicePort().lastIndexOf(File.separator)+1));
            } catch (Exception ex){
                /// internally mapped port used.
            }
        }
        for(PeripheralController peripheral:waitingPeripherals.values()){
            try {
                used.add(peripheral.getPeripheral().getDevicePort().substring(peripheral.getPeripheral().getDevicePort().lastIndexOf(File.separator)+1));
            } catch (Exception ex){
                /// internally mapped port used.
            }
        }
        for(PeripheralController peripheral:unsupportedPeripherals.values()){
            try {
                used.add(peripheral.getPeripheral().getDevicePort().substring(peripheral.getPeripheral().getDevicePort().lastIndexOf(File.separator)+1));
            } catch (Exception ex){
                /// internally mapped port used.
            }
        }
        return used;
    }
    
}