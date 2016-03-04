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

package org.pidome.server.system.hardware.drivers;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.system.hardware.peripherals.PeripheralEventListener;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryBaseInterface;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceListener;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.ScriptedPeripheralSoftwareDriverForData;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.audit.Notifications;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.hardware.devices.DevicesException;
import org.pidome.server.system.hardware.peripherals.PeripheralController;
import org.pidome.server.system.hardware.peripherals.Peripherals;
import org.pidome.server.system.hardware.peripherals.emulators.HardwarePluginDeviceEmulator;
import org.pidome.server.system.packages.PackagePermissionsNotUpToDateException;

/**
 * This class is responsible to load the peripheral software drivers.
 * After loading the peripheral software driver it attaches the driver to the base peripheral. This means the peripheral device will take further control
 * of passing data between the hardware and the software driver.
 * When the software driver is loaded the underlying device class receives an event to load the devices belonging to the driver.
 * This class will hold references to the specific driver.
 * 
 * If the base peripheral class has a peripheral software driver id, it will automatically load the correct driver. If not, the end user must choose the correct driver.
 * 
 * @author John Sirach
 */
public class Drivers extends Peripherals implements PeripheralEventListener,DiscoveredDeviceListener {

    static Logger LOG = LogManager.getLogger(Drivers.class);
    
    List _listeners = new ArrayList();
    
    private final Map<String,PeripheralSoftwareDriverInterface> driverCollection = new HashMap<>();
    static Drivers myInstance;
    
    /**
     * Starts the peripheral software drivers and sets the peripheral hardware listener.
     */
    protected Drivers(){ 
        super();
        set();
    }
    
    /**
     * Sets the instance and enables the peripheral listener.
     */
    final void set(){
        myInstance = this;
        addPeripheralListener(this);
        DiscoveredItemsCollection.addDiscoveredDeviceListener(this);
    }
    
    /**
     * Loads the peripheral software driver for the attached peripheral.
     * @param peripheralDevice Name of the attached peripheral.
     */
    final void setPheripheralSoftwareDriver(PeripheralController peripheralDevice) {
        try {
            LOG.info("Loading peripheral software driver for: {}", peripheralDevice.getPeripheral().getFriendlyName());
            PeripheralHardwareDriver.PeripheralVersion peripheralVersion = peripheralDevice.getPeripheralSoftwareId();
            Map<String,String> driverDetails = getPackageLoader().getPeripheralSoftwareDriverDetails(peripheralVersion.getId(), peripheralVersion.getVersion());
            PeripheralSoftwareDriverInterface driver = (PeripheralSoftwareDriverInterface)getPackageLoader().loadPeripheralSoftwareDriver(peripheralVersion.getId(), peripheralVersion.getVersion()).getConstructor().newInstance();
            driver.setFriendlyName(driverDetails.get("friendlyname"));
            driver.setId(Integer.parseInt(driverDetails.get("dbid")));
            driver.setNamedId(driverDetails.get("driverid"));
            driver.setHasCustom(Boolean.valueOf(driverDetails.get("hascustom")));
            peripheralDevice.addPeripheralSoftwareDriver(driver);
            if(driver instanceof ScriptedPeripheralSoftwareDriverForData){
                ((ScriptedPeripheralSoftwareDriverForData)driver).prepareEngine();
                ((ScriptedPeripheralSoftwareDriverForData)driver).setScriptData(driverDetails.get("scriptcontent"));
            }
            if(peripheralDevice.getPeripheral() instanceof HardwarePluginDeviceEmulator){
                DevicePlugin pluginEmu = ((HardwarePluginDeviceEmulator)peripheralDevice.getPeripheral()).getPlugin();
                LOG.info("Is emulator plugin, setting plugin links: {}", pluginEmu.getPluginName());
                ///((PluginPeripheral)driver).setPluginLink(((HardwarePluginDeviceEmulator)peripheralDevice).getPlugin());
                try {
                    LOG.info("Is emulator plugin, setting device instance link");
                    pluginEmu.setDeviceServiceLink((PluginDeviceMutationInterface)DeviceService.getDevicesInstance());
                } catch (DevicesException ex) {
                    LOG.warn("Plugin will run, but can not perform any device mutations: {}", ex.getMessage());
                }
                Map<String,Integer> assignList = getInstalledDevicesByDriverName(driver.getPackageName());
                LOG.info("Assigning the next installed devices to emulator plugin '{}': {}", pluginEmu.getPluginName(), assignList);
                pluginEmu.assignDeviceInstalledIds(assignList);
            }
            try {
                driver.setDeviceServiceLink(DeviceService.getDevicesInstance());
            } catch (DevicesException ex) {
                LOG.error("Could not set devices link, driver will not be able to execute device mutations: {}", ex.getMessage());
            }
            peripheralDevice.startSoftwareDriver();
            driverCollection.put(driver.getPackageName(), driver);
            _fireDriverEvent(driver, DriverEvent.DRIVER_LOADED);
            LOG.info("Driver {} on {} on port {} is ready for use", driver.getName(),peripheralDevice.getPeripheral().getFriendlyName(),peripheralDevice.getPeripheral().getDevicePort());
        } catch (PackagePermissionsNotUpToDateException ex){
            LOG.warn("Package permissions not up to date: {}", ex.getMessage());
        } catch (ClassNotFoundException ex){
            LOG.info("The software on the {} on port {} not found in PiDome: {}. It seems missing, contact author, or file a bug if it is installed.", peripheralDevice.getPeripheral().getFriendlyName(), peripheralDevice.getPeripheral().getDevicePort(), ex.getMessage());
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.info("Peripheral shotware driver for " + peripheralDevice.getPeripheral().getFriendlyName() + " found, but can not load, contact author", ex);
        } catch (PeripheralHardwareException ex){ 
            LOG.debug(ex.getMessage());
            LOG.info("Something went wrong width the hardware: {}",ex.getMessage());
        } catch (UnsupportedOperationException ex) {
            /// Plugins have no Unuspported operations, and if they do they are incorrectly implemented.
            if(!(peripheralDevice.getPeripheral() instanceof HardwarePluginDeviceEmulator)){
                LOG.info("(Discard this message if you are not adding your own custom hardware utilizing 'REQUESTDRIVER_ID' methods.) Something went wrong or autodiscovery is unsupported to get the peripheral software id on the {} on port {}, you must set the driver manually.",peripheralDevice.getPeripheral().getFriendlyName(),peripheralDevice.getPeripheral().getDevicePort());
            } else {
                LOG.error("Could not attach a peripheral software emulator link to {}", peripheralDevice.getPeripheral().getFriendlyName(), ex);
            }
        }
    }
    
    /**
     * Retrieves all the devices from the database of a specific device class.
     * @param driverName
     * @return 
     */
    Map<String,Integer> getInstalledDevicesByDriverName(String driverName){
        LOG.trace("Installed devices requested for driver: {}", driverName);
        Map<String,Integer> devices = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); PreparedStatement prep = fileDBConnection.prepareStatement("SELECT id.[id],id.[driver] FROM installed_devices id WHERE id.driver_driver=(SELECT id FROM installed_drivers WHERE driver=? LIMIT 1)")) {
            prep.setString(1, driverName);
            try (ResultSet rsDevices = prep.executeQuery()) {
                while (rsDevices.next()) {
                    devices.put(rsDevices.getString("driver"), rsDevices.getInt("id"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        return devices;
    }
    
    
    /**
     * Returns a list of active peripheral software drivers.
     * @return 
     */
    public final Map<String,PeripheralSoftwareDriverInterface> getPeripheralSoftwareDrivers(){
        return driverCollection;
    }
    
    /**
     * Returns a list of possible peripheral drivers for a specific peripheral which is loaded.
     * @param peripheral
     * @return List<Map<Integer,String>>
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public final List<Map<String,String>> getPossibleLoadedPeripheralDrivers(PeripheralController peripheral) throws PeripheralHardwareException {
        return getPackageLoader().getPeripheralDriversForPeripheral(peripheral.getPeripheralHardwareDriver().getName(), 
                peripheral.getPeripheral().getVendorId(),peripheral.getPeripheral().getDeviceId());
    }
    
    /**
     * Returns the driver instance for the given driver name.
     * @param driverName
     * @return PeripheralSoftwareDriverInterface
     * @throws java.lang.ClassNotFoundException
     */
    protected PeripheralSoftwareDriverInterface getDriverInstanceFor(String driverName) throws ClassNotFoundException {
        if(driverCollection.containsKey(driverName)){
            return driverCollection.get(driverName);
        }
        throw new ClassNotFoundException("Driver "+driverName+" not instantiated");
    }
    
    /**
     * Unloads the peripheral software driver for the peripheral device.
     * @param device Name of the device being detached.
     */
    final void unsetPheripheralSoftwareDriver(PeripheralController controller) {
        PeripheralSoftwareDriverInterface driver = controller.getSoftwareDriver();
        if(driver!=null){
            if(controller.getPeripheral() instanceof HardwarePluginDeviceEmulator){
                DevicePlugin pluginEmu = ((HardwarePluginDeviceEmulator)controller.getPeripheral()).getPlugin();
                pluginEmu.removeDeviceServiceLink();
                pluginEmu.removeAssignDeviceInstalledIds();
            }
            LOG.debug("Stopping driver for {}", controller.getPeripheral().getFriendlyName());
            /// first fire the event for the devices so they can unregister listeners and then stop the driver
            _fireDriverEvent(controller.getPeripheral().getSoftwareDriver(), DriverEvent.DRIVER_UNLOAD);
            driver.removeDeviceServiceLink();
            try {
                controller.stopSoftwareDriver();
            } catch (Exception ex){
                //// When a driver is unloading faster then hardware can respond it will fail with a or a runtime exception from nashorn- or ioexception from java drivers.
            }
            if(driver instanceof ScriptedPeripheralSoftwareDriverForData){
                ((ScriptedPeripheralSoftwareDriverForData)driver).destructEngine();
            }
            if(controller.getSoftwareDriver() instanceof WebPresentAddExistingDeviceInterface){
                DiscoveredItemsCollection.removeAllFoundDevices((WebPresentAddExistingDeviceInterface)controller.getSoftwareDriver());
            }
            driverCollection.remove(controller.getSoftwareDriver().getPackageName());
            try {
                controller.removeSoftwareDriver();
            } catch (PeripheralHardwareException ex) {
                ///
            }
        }
    }
    
    /**
     * Handles the adding or removing of a peripheral.
     * @param oEvent 
     */
    @Override
    public void handlePeripheralEvent(PeripheralEvent oEvent) {
        PeripheralController device = (PeripheralController)oEvent.getSource();
        switch(oEvent.getEventType()){
            case PeripheralEvent.DRIVER_LOADED:
                setPheripheralSoftwareDriver(device);
            break;
            case PeripheralEvent.DRIVER_UNLOAD:
                unsetPheripheralSoftwareDriver(device);
            break;
        }
    }
    
    /**
     * Returns a list of currently running drivers.
     * @return 
     */
    public final Map<String,PeripheralSoftwareDriverInterface> getRunningDriversCollection(){
        return this.driverCollection;
    }
    
    /**
     * Returns the amount of running devices attached to the given driver.
     * @param driverName
     * @return 
     */
    public final int getRunningDevicesCountForDriver(String driverName){
        if(driverCollection.containsKey(driverName)){
            return driverCollection.get(driverName).getRunningDevicesCount();
        } else {
            return 0;
        }
    }
    
    /**
     * Returns the peripheral hardware driver for the given software driver.
     * @param driverName
     * @return 
     */
    public final Peripheral getHardwareDriverFromSoftwareDriver(String driverName){
        if(driverCollection.containsKey(driverName)){
            return driverCollection.get(driverName).getHardwareDriverFromSoftwareDriver();
        } else {
            return null;
        }
    }
    
    /**
     * Add listeners for changed hardware.
     * @param l 
     */
    protected final synchronized void addDriversListener( DriverEventListener l ) {
        _listeners.add( l );
        LOG.debug("Added listener: {}", l.getClass().getName());
    }
    
    /**
     * Removes listeners for changed hardware.
     * @param l 
     */
    protected final synchronized void removeDriversListener( DriverEventListener l ) {
        _listeners.remove( l );
        LOG.debug("Removed listener: {}", l.getClass().getName());
    }
    
    /**
     * Fires events after a hardware change has been detected.
     * @param driver
     * @param eventType 
     */
    protected final synchronized void _fireDriverEvent(PeripheralSoftwareDriverInterface driver, String eventType) {
        LOG.debug("Event: {}", eventType);
        DriverEvent event = new DriverEvent(driver, eventType);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (DriverEventListener) listeners.next() ).handleDriverEvent( event );
        }
    }

    @Override
    public void handleNewDiscoveredDevice(DeviceDiscoveryBaseInterface driver, DiscoveredDevice device) {
        Runnable run = () -> {
            LOG.info("New device discovery. Driver: {}, Device info - name: {}, address: {}, visual: {}, parameters: {}",driver.getName(), device.getName(), device.getAddress(), device.getVisualInformation(), device.getParameterValues());
            Map<String,Object> sendObject = new HashMap<String,Object>(){
                {
                    put("drivername",  driver.getFriendlyName());
                    put("port",        ((PeripheralSoftwareDriverInterface)driver).getHardwareDriver().getPort());
                    put("name",        device.getName());
                    put("address",     device.getAddress());
                    put("time",        device.getDiscoveryDateTime());
                    put("information", device.getVisualInformation());
                    put("type",        device.getFunctionType());
                    put("newaddress",  device.getNewAddress());
                    put("description", device.getDescription());
                    put("parameters",  device.getParameterValues());
                    put("newaddress",  device.getNewAddress());
                    if(device.getDeviceDriver()!=null){
                        put("devicedriver", device.getDeviceDriver());
                    }
                    put("deviceparameters", device.getParameterValues());
                    put("knowndevice", DeviceService.getInstalledDeviceByDriverName(device.getDeviceDriver(), ((PeripheralSoftwareDriverInterface)driver).getId()));
                }
            };
            ClientMessenger.send("DeviceService","discoveredNewDevice", 0, sendObject);
            Notifications.sendMessage(Notifications.NotificationType.INFO, "Discovered new device",
                                                                           new StringBuilder("Discovered: ")
                                                                               .append(device.getName())
                                                                               .toString()
            );
        };
        run.run();
    }

    /**
     * Broadcasting an enabled discovery.
     * @param driver 
     */
    @Override
    public void deviceDiscoveryEnabled(DeviceDiscoveryBaseInterface driver, int period) {
        if(period==-1){
            Notifications.sendMessage(Notifications.NotificationType.WARNING, "Discovery enabled indefinitely!",
                                                                           new StringBuilder("Discovery enabled ").append("until disabled for: ")
                                                                               .append(driver.getFriendlyName())
                                                                               .toString()
            );
        } else if(period == -2){
            Notifications.sendMessage(Notifications.NotificationType.INFO, "Discovery enabled",
                                                                           new StringBuilder("Single device discovery started for one device for ")
                                                                               .append(driver.getFriendlyName())
                                                                               .toString()
            );
        } else if (period == -3){
            Notifications.sendMessage(Notifications.NotificationType.INFO, "Discovery scan started",
                                                                           new StringBuilder("Discovery scan started for ")
                                                                               .append(driver.getFriendlyName())
                                                                               .toString()
            );
        } else {
            Notifications.sendMessage(Notifications.NotificationType.INFO, "Discovery enabled",
                                                                           new StringBuilder("Discovery enabled for ").append(period).append(" minutes for: ")
                                                                               .append(driver.getFriendlyName())
                                                                               .toString()
            );
        }
        Map<String,Object> sendObject = new HashMap<String,Object>(){
            {
                put("drivername", driver.getName());
                put("port", ((PeripheralSoftwareDriverInterface)driver).getHardwareDriver().getPort());
                put("time", period);
            }
        };
        ClientMessenger.send("DeviceService","discoveryEnabled", 0, sendObject);
    }

    /**
     * Broadcasting a disabled discovery.
     * @param driver 
     */
    @Override
    public void deviceDiscoveryDisabled(DeviceDiscoveryBaseInterface driver) {
        Map<String,Object> sendObject = new HashMap<String,Object>(){
            {
                put("drivername", driver.getName());
                put("port", ((PeripheralSoftwareDriverInterface)driver).getHardwareDriver().getPort());
            }
        };
        ClientMessenger.send("DeviceService","discoveryDisabled", 0, sendObject);
        Notifications.sendMessage(Notifications.NotificationType.INFO, "Discovery disabled/Scan ended",
                                                                       new StringBuilder("Discovery disabled/Scan ended for: ")
                                                                           .append(driver.getFriendlyName())
                                                                           .toString()
        );
    }
    
    /**
     * Returns a map containing id and name of scripted drivers.
     * @return Map containsing id as key and name as value of scripted drivers.
     */
    public final List<Map<String,Object>> getScriptedDrivers(){
        List<Map<String,Object>> drivers = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
             PreparedStatement prep = fileDBConnection.prepareStatement("SELECT sd.id,sd.name,sd.description,sd.fixed,sd.editable,id.friendlyname as drivername FROM scripted_drivers sd INNER JOIN installed_drivers id ON sd.driverid=id.id");
             ResultSet rsDevices = prep.executeQuery()) {
            while (rsDevices.next()) {
                Map<String,Object> driverInfo = new HashMap<>();
                driverInfo.put("id", rsDevices.getInt("id"));
                driverInfo.put("name", rsDevices.getString("name"));
                driverInfo.put("description", rsDevices.getString("description"));
                driverInfo.put("fixed", rsDevices.getString("description"));
                driverInfo.put("editable", rsDevices.getString("description"));
                driverInfo.put("driver", rsDevices.getString("drivername"));
                drivers.add(driverInfo);
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        return drivers;
    }
    
    public final Map<String,Object> getScriptedDriver(int id){
        Map<String,Object> driverInfo = new HashMap<>();
        driverInfo.put("id", 0);
        driverInfo.put("name", "");
        driverInfo.put("description", "");
        driverInfo.put("fixed", "");
        driverInfo.put("editable", "");
        driverInfo.put("script", "");
        driverInfo.put("instancefor", 0);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
             PreparedStatement prep = fileDBConnection.prepareStatement("SELECT sd.id,sd.name,sd.description,sd.fixed,sd.editable,sd.scriptcontent,sd.driverid FROM scripted_drivers sd WHERE sd.id=? LIMIT 1")) {
            prep.setInt(1, id);
            try (ResultSet rsDevices = prep.executeQuery()){
                if (rsDevices.next()) {
                    driverInfo.put("id", rsDevices.getInt("id"));
                    driverInfo.put("name", rsDevices.getString("name"));
                    driverInfo.put("description", rsDevices.getString("description"));
                    driverInfo.put("fixed", rsDevices.getInt("fixed"));
                    driverInfo.put("editable", rsDevices.getInt("editable"));
                    driverInfo.put("script", rsDevices.getString("scriptcontent"));
                    driverInfo.put("instancefor", rsDevices.getInt("driverid"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load devices from database: {}", ex.getMessage());
        }
        return driverInfo;
    }
    
    public final boolean updateScriptedDriver(int instanceFor, int driverId, String name, String description, String script){
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM); 
             PreparedStatement prep = connection.prepareStatement("UPDATE scripted_drivers SET name=?, description=?, scriptcontent=? WHERE id=? AND driverid=? AND editable=1")) {
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setString(3, script);
            prep.setInt(4, driverId);
            prep.setInt(5, instanceFor);
            prep.executeUpdate();
            prep.close();
            connection.close();
            for(PeripheralSoftwareDriverInterface driver:driverCollection.values()){
                if(driver.getId()== instanceFor && driver instanceof ScriptedPeripheralSoftwareDriverForData){
                    ScriptedPeripheralSoftwareDriverForData workWith = (ScriptedPeripheralSoftwareDriverForData) driver;
                    workWith.stopDriver();
                    workWith.destructEngine();
                    workWith.prepareEngine();
                    workWith.setScriptData(script);
                    workWith.startDriver();
                }
            }
        } catch (SQLException ex) {
            LOG.error("Database error '{}'", ex.getMessage());
            return false;
        }
        return true;
    }
    
}