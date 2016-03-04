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

package org.pidome.server.system.packages;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.system.config.ConfigException;
import org.pidome.server.system.db.DB;

/**
 * Keeps an eye on packages, loads, installs en removes them.
 * @author John Sirach
 */
public final class Packages {
    
    static Logger LOG = LogManager.getLogger(Packages.class);
    
    static Packages instance = null;
    
    private final Map<Integer,Package> packages = new HashMap<>();
    
    /**
     * Constructor
     */
    protected Packages(){}
    
    /**
     * Return a list of authorized packages.
     * @return 
     */
    public final Map<Integer,Package> getPackages(){
        return packages;
    }
    
    /**
     * Preloads all the existing packages.
     */
    private void preloader() throws ConfigException {
        List<Integer> packagelist = new ArrayList<>();
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT id FROM installed_packages")) {
                try (ResultSet rsPeripheral = prepDel.executeQuery()) {
                    while(rsPeripheral.next()){
                        packagelist.add(rsPeripheral.getInt("id"));
                    }
                }
            }
        } catch (SQLException ex){
            throw new ConfigException("Could not preload packages: " + ex.getMessage());
        }
        for(int id:packagelist){
            try {
                loadPackage(id);
            } catch (PackageInitizializationException | ConfigException ex){
                LOG.error("Could not load package: {}", ex.getMessage(), ex);
            }
        }
    }
    
    private void loadPackage(int packageId) throws PackageInitizializationException,ConfigException {
        Package packaged = new Package(packageId);
        packaged.loadPackageDelivers();
        try {
            packaged.loadPackage();
        } catch (PackagePermissionsNotAvailableException ex) {
            LOG.error("Package '{}' not loaded due to permissions not being available, loading halted: {}", packaged.getPackageName(), ex.getMessage());
        }
        packages.put(packageId, packaged);
    }
    
    /**
     * Returns a single package.
     * @param packageId
     * @return 
     * @throws org.pidome.server.system.packages.PackageException 
     */
    public final Package getPackage(int packageId) throws PackageException {
        if(packages.containsKey(packageId)){
            return packages.get(packageId);
        } else {
            throw new PackageException("Package with id " + packageId + " not found");
        }
    }
    
    /**
     * Returns the package instance, creates one if it does not exists.
     * @return The Packages instance
     */
    public static synchronized Packages getInstance() throws UnsupportedOperationException {
        LOG.debug("Instance request");
        if(instance==null){
            try {
                initialize();
            } catch (ConfigException ex) {
                throw new UnsupportedOperationException("Configuration error or already initialized: " + ex.getMessage());
            }
        }
        return instance;
    }
    
    /**
     * Approve a package permissions.
     * This also starts any devices/plugins/drivers bound to this package.
     * @param packageId
     * @return
     * @throws PackageException 
     */
    public final boolean approvePackage(int packageId) throws PackageException {
        if(packages.containsKey(packageId)){
            try {
                packages.get(packageId).syncPermissions();
                Package reset = packages.remove(packageId);
                try {
                    reset.getClassLoader().close();
                } catch (IOException ex) {
                    LOG.error("Could not close package resources, server needs to be restarted: {}", ex.getMessage());
                    throw new PackageException("Package permissions have been set, but could not be further initialized, server restart needed: " + ex.getMessage());
                }
                try {
                    loadPackage(reset.getId());
                } catch (ConfigException ex) {
                    LOG.error("Could not load package: {}", ex.getMessage(), ex);
                }
            } catch (PackageInitizializationException ex) {
                throw new PackageException("Package "+packages.get(packageId).getPackageName()+" has failed initialization: " + ex.getMessage());
            } catch (PackagePermissionsNotAvailableException ex) {
                throw new PackageException("Setting permissions for package "+packages.get(packageId).getPackageName()+" has failed: " + ex.getMessage());
            }
            return true;
        } else {
            throw new PackageException("Package with id " + packageId + " not found");
        }
    }
    
    /**
     * Disapproves a package permissions.
     * This also stops any devices/plugins/drivers bound to this package.
     * @param packageId
     * @return
     * @throws PackageException 
     */
    public final boolean disApprovePackage(int packageId) throws PackageException {
        if(packages.containsKey(packageId)){
            try {
                packages.get(packageId).deSyncPermissions();
                Package reset = packages.remove(packageId);
                try {
                    reset.getClassLoader().close();
                } catch (IOException ex) {
                    LOG.error("Could not close package resources, server needs to be restarted: {}", ex.getMessage());
                    throw new PackageException("Package permissions have been set, but could not be further initialized, server restart needed: " + ex.getMessage());
                }
                try {
                    loadPackage(reset.getId());
                } catch (ConfigException ex) {
                    LOG.error("Could not load package: {}", ex.getMessage(), ex);
                }
            } catch (PackageInitizializationException ex) {
                throw new PackageException("Package "+packages.get(packageId).getPackageName()+" has failed initialization: " + ex.getMessage());
            }
            return true;
        } else {
            throw new PackageException("Package with id " + packageId + " not found");
        }
    }
    
    /**
     * Initializes the package instance.
     * @throws UnsupportedOperationException package instance can only be loaded once, otherwise throws exception when trying to initialize again.
     * @throws ConfigException When there is a configuration problem.
     */
    public static synchronized void initialize() throws UnsupportedOperationException, ConfigException {
        LOG.debug("Instance initializing");
        if(instance==null){
            instance = new Packages();
            instance.preloader();
        } else {
            LOG.debug("Already initialized");
        }
    }
    
    /**
     * Returns the class by the installed plugin id.
     * @param installedPluginId
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final Class<?>loadPlugin(int installedPluginId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        for(Package loader:packages.values()){
            if(loader.hasPlugin(installedPluginId)){
                return loader.getPlugin(installedPluginId);
            }
        }
        throw new ClassNotFoundException("No packages found which should have the installed plugin id: " + installedPluginId);
    }
    
    /**
     * Returns the class for the peripheral hardware driver.
     * @param vendorId
     * @param productId
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final PeripheralHardwareDriverInterface loadPeripheralDriverByVidPid(String vendorId, String productId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        for(Package loader:packages.values()){
            if(loader.hasPeripheralDriverByVidPid(vendorId, productId)){
                return loader.getPeripheral(vendorId, productId);
            }
        }
        throw new ClassNotFoundException("No packages found which should have the driver for VID: "+vendorId+" and PID: " + productId);
    }
    
    /**
     * Returns the class for the peripheral hardware driver.
     * @param systemType
     * @param subSystemType
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final PeripheralHardwareDriverInterface loadPeripheralDriverBySubSystem(String systemType, String subSystemType) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        for(Package loader:packages.values()){
            if(loader.hasPeripheralDriverBySubSystem(systemType, subSystemType)){
                return loader.getPeripheralBySubsystem(systemType, subSystemType);
            }
        }
        throw new ClassNotFoundException("No packages found which should have the driver for system: "+systemType+" and sub system: " + subSystemType);
    }
    
    /**
     * Loads and constructs a driver based on the peripheral driver id.
     * @param namedId
     * @return
     * @throws ClassNotFoundException
     * @throws PackagePermissionsNotUpToDateException 
     */
    public final PeripheralHardwareDriverInterface loadPeripheralDriverByNamedId(String namedId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        for(Package loader:packages.values()){
            if(loader.hasPeripheralDriverByNamedId(namedId)){
                return loader.getPeripheralByByNamedId(namedId);
            }
        }
        throw new ClassNotFoundException("No packages found which should have the driver with named id: " + namedId);
    }
    
    /**
     * Returns the class for the given peripheral software id.
     * @param softwareDriverId
     * @param softwareDriverVersion
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final Class<?>loadPeripheralSoftwareDriver(String softwareDriverId, String softwareDriverVersion) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        for(Package loader:packages.values()){
            if(loader.hasPeripheralSoftwareDriver(softwareDriverId, softwareDriverVersion)){
                return loader.getPeripheralSoftwareDriver(softwareDriverId, softwareDriverVersion);
            }
        }
        throw new ClassNotFoundException("No packages found which should have the driver for software id: "+softwareDriverId+" with version: " + softwareDriverVersion);
    }
    
    /**
     * Returns the class for the given peripheral software id.
     * @param softwareDriverId
     * @param softwareDriverVersion
     * @return
     * @throws ClassNotFoundException 
     */
    public final Map<String,String> getPeripheralSoftwareDriverDetails(String softwareDriverId, String softwareDriverVersion) throws ClassNotFoundException {
        for(Package loader:packages.values()){
            if(loader.hasPeripheralSoftwareDriver(softwareDriverId, softwareDriverVersion)){
                return loader.getPeripheralSoftwareDriverDetails(softwareDriverId, softwareDriverVersion);
            }
        }
        throw new ClassNotFoundException("No packages found which should have the driver for software id: "+softwareDriverId+" with version: " + softwareDriverVersion);
    }
    
    /**
     * Returns the class for the given deviceId from an added device.
     * @param deviceId
     * @param devicePackageName
     * @return
     * @throws ClassNotFoundException 
     */
    final Class<?>loadDeviceDriver(int deviceId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException { 
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            String driverClass;
            String friendlyName;
            int driverId  = 0;
            int packageId = 0;
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT id.[id], id.[friendlyname],id.[driver],ip.[id] as packageid "
                                                                       + " FROM devices de "
                                                                   + "INNER JOIN installed_devices id ON de.device=id.id "
                                                                   + "INNER JOIN installed_packages ip ON id.[package]=ip.[id] "
                                                                        + "WHERE de.id=? "
                                                                        + "LIMIT 1")) {
                prepDel.setInt(1, deviceId);
                try (ResultSet rsDevice = prepDel.executeQuery()) {
                    if(rsDevice.next()){
                        driverClass = rsDevice.getString("driver");
                        friendlyName = rsDevice.getString("friendlyname");
                        driverId = rsDevice.getInt("id");
                        packageId = rsDevice.getInt("packageid");
                    } else {
                        throw new ClassNotFoundException("Device id '"+deviceId+"' not found in the database");
                    }
                }
                LOG.debug("Found installed device id '"+deviceId+"' ("+friendlyName+"), trying to retrieve '"+driverClass+"' from package '"+packageId+"' for loading");
                return packages.get(packageId).getInstalledDevice(driverId);
            }
        } catch (SQLException ex){
            throw new ClassNotFoundException("Could not retrieve device driver for id '"+deviceId+" from database: " + ex.getMessage());
        }
    }

    /**
     * Returns the class for a installed device by id.
     * This is used for devices which has been installed on the server and not added to be used.
     * @param deviceId
     * @param devicePackageName
     * @return
     * @throws ClassNotFoundException 
     */
    final Class<?>loadInstalledDeviceDriver(int deviceId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException { 
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            String driverClass;
            String friendlyName;
            int driverId  = 0;
            int packageId = 0;
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT id.[id], id.[friendlyname],id.[driver],ip.[id] as packageid "
                                                                       + " FROM installed_devices id "
                                                                   + "INNER JOIN installed_packages ip ON id.[package]=ip.[id] "
                                                                        + "WHERE id.id=? "
                                                                        + "LIMIT 1")) {
                prepDel.setInt(1, deviceId);
                try (ResultSet rsDevice = prepDel.executeQuery()) {
                    if(rsDevice.next()){
                        driverClass = rsDevice.getString("driver");
                        friendlyName = rsDevice.getString("friendlyname");
                        driverId = rsDevice.getInt("id");
                        packageId = rsDevice.getInt("packageid");
                    } else {
                        throw new ClassNotFoundException("Installed device id '"+deviceId+"' not found in the database");
                    }
                }
                LOG.debug("Found '"+deviceId+"' device id ("+friendlyName+"), trying to retrieve '"+driverClass+"' for loading");
                return packages.get(packageId).getInstalledDevice(driverId);
            }
        } catch (SQLException ex){
            throw new ClassNotFoundException("Could not retrieve device driver for id '"+deviceId+" from database: " + ex.getMessage());
        }
    }
    
    /**
     * Returns the name of the driver used for a device.
     * @param deviceId
     * @return 
     */
    public final String getMappedDevicePeripheralSoftwareDriver(int deviceId){
        String driverName = "";
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT idr.[driver] "
                                                                         + "FROM devices d "
                                                                   + "INNER JOIN installed_devices ide ON d.[device]=ide.[id] "
                                                                   + "INNER JOIN installed_drivers idr ON ide.[driver_driver]=idr.[id] "
                                                                        + "WHERE d.[id]=? "
                                                                        + "LIMIT 1")) {
                prepDel.setInt(1, deviceId);
                try (ResultSet rsDriver = prepDel.executeQuery()) {
                    if(rsDriver.next()){
                        driverName = rsDriver.getString("driver");
                    }
                }
            }
        } catch (SQLException ex){
            return null;
        }
        return driverName;
    }
    
    /**
     * Returns a list of peripheral software drivers which are candidates of being loaded for a peripheral.
     * @param vendorId
     * @param productId
     * @return 
     */
    public final List<Map<String,String>> getPeripheralDriversByProductId(String hardwareDriverName, String vendorId, String productId) {
        LOG.debug(" Getting possible software drivers for hardware driver '{}'", hardwareDriverName);
        List<Map<String,String>> posssibleCandidates = new ArrayList<>();
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepGet = connection.prepareStatement("SELECT id.[driverid],id.[friendlyname],id.[version],iprf.[vid],iprf.[pid] "
                                                                   + "FROM installed_drivers id "
                                                                   + "INNER JOIN installed_peripherals iprf ON iprf.[id]=id.[peripheral_driver] "
                                                                        + "WHERE iprf.interface_type=(SELECT interface_type FROM installed_peripherals WHERE name=? LIMIT 1)")) {
                prepGet.setString(1, hardwareDriverName);
                try (ResultSet rsPeripheralDriver = prepGet.executeQuery()) {
                    while(rsPeripheralDriver.next()){
                        Map<String,String> candidate = new HashMap<>();
                        candidate.put("driverid", rsPeripheralDriver.getString("driverid"));
                        candidate.put("friendlyname", rsPeripheralDriver.getString("friendlyname"));
                        candidate.put("version", rsPeripheralDriver.getString("version"));
                        candidate.put("prefered", (rsPeripheralDriver.getString("vid").equals(vendorId)&&rsPeripheralDriver.getString("pid").equals(productId))?"true":"false");
                        posssibleCandidates.add(candidate);
                        LOG.debug("Possible candidate found: {} {},{} (vid: {}, pid: {})",rsPeripheralDriver.getString("friendlyname"),rsPeripheralDriver.getString("driverid"),rsPeripheralDriver.getString("version"), rsPeripheralDriver.getString("vid"), rsPeripheralDriver.getString("pid"));
                    }
                }
            }
        } catch (SQLException ex){
            LOG.error("Could not load peripheral driver candidates: {}",ex.getMessage());
        }
        return posssibleCandidates;
    }
 
}