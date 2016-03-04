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

import java.io.FilePermission;
import java.lang.reflect.InvocationTargetException;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.system.config.ConfigException;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.db.DB;

/**
 * Creates a package which will hold owner, driver and descriptive information.
 * @author John Sirach
 */
public final class Package {

    static Logger LOG = LogManager.getLogger(Package.class);
    
    Map<Integer, Map<String, String>> deviceMap     = new HashMap<>();
    Map<String,  Map<String, String>> driverMap     = new HashMap<>();
    Map<String,  Map<String, String>> peripheralMap = new HashMap<>();
    Map<Integer, Map<String, String>> pluginMap     = new HashMap<>();
    
    String Me;
    
    private int packageId = 0;
    
    PackageClassLoader loader;
    private DefaultProtectionDomain protectionDomain;
    
    private String AUTHOR       = "";
    private String AUTHOREMAIL  = "";
    private String AUTHORURL    = "";
    private String PACKAGEBASE  = "";
    private String PACKAGENAME  = "";
    
    private String permissionsSet = "";
    
    private boolean permissionsAreUpToDate = true;
    
    /**
     * Constructor sets name, package XML and also initializes the Package class Loader 
     * @param packageId
     * @throws org.pidome.server.system.config.ConfigException
     */
    protected Package(int packageId) throws ConfigException {
        this.packageId = packageId;
    }
    
    /**
     * Returns the package id.
     * @return 
     */
    public final int getId(){
        return this.packageId;
    }
    
    /**
     * Returns the package name.
     * @return 
     */
    public final String getPackageName(){
        return this.PACKAGENAME;
    }
    
    /**
     * Returns the package author.
     * @return 
     */
    public final String getPackageAuthor(){
        return this.AUTHOR;
    }
    
    /**
     * Returns the package author's website.
     * @return 
     */
    public final String getPackageWebsite(){
        return this.AUTHORURL;
    }
    
    /**
     * Returns if the current package is authorized to deliver.
     * @return 
     */
    public final boolean isAuthorized(){
        return this.permissionsAreUpToDate;
    }
    
    /**
     * Creates the package details.
     * @throws org.pidome.server.system.config.ConfigException
     */
    protected void loadPackageDelivers() throws ConfigException {
        try {
            loadPlugins();
            loadPeripheralDrivers();
            loadPeripheralSoftwareDrivers();
            loadInstalledDeviceDrivers();
        } catch (ConfigPropertiesException ex) {
            throw new ConfigException(ex);
        }
    }
    
    /**
     * Returns a complete set of what this package delivers.
     * @return 
     */
    public final Map<String,ArrayList<Map<String,String>>> getPackageDelivers(){
        Map<String,ArrayList<Map<String,String>>> fullSet = new HashMap<>();
        ArrayList<Map<String,String>> devices = new ArrayList<>();
        deviceMap.values().stream().map((installed) -> {
            Map<String,String> device = new HashMap<>();
            device.put("name", installed.get("friendlyname"));
            device.put("version", installed.get("version"));
            return device;
        }).forEach((device) -> {
            devices.add(device);
        });
        
        ArrayList<Map<String,String>> drivers = new ArrayList<>();
        driverMap.values().stream().map((installed) -> {
            Map<String,String> driver = new HashMap<>();
            driver.put("name", installed.get("friendlyname"));
            driver.put("version", installed.get("version"));
            return driver;
        }).forEach((driver) -> {
            drivers.add(driver);
        });
        
        ArrayList<Map<String,String>> peripherals = new ArrayList<>();
        peripheralMap.values().stream().map((installed) -> {
            Map<String,String> peripheral = new HashMap<>();
            peripheral.put("name", installed.get("friendlyname"));
            peripheral.put("version", installed.get("version"));
            return peripheral;
        }).forEach((peripheral) -> {
            peripherals.add(peripheral);
        });
        
        ArrayList<Map<String,String>> plugins = new ArrayList<>();
        pluginMap.values().stream().map((installed) -> {
            Map<String,String> plugin = new HashMap<>();
            plugin.put("name", installed.get("name"));
            plugin.put("version", installed.get("version"));
            return plugin;
        }).forEach((plugin) -> {
            plugins.add(plugin);
        });
        
        fullSet.put("devices", devices);
        fullSet.put("drivers", drivers);
        fullSet.put("peripherals", peripherals);
        fullSet.put("plugins", plugins);
        return fullSet;
    }
    
    public final Map<String,Object> getCurrentPermissionsRaw(){
        if(hasPackagePermissionsSet()){
            JSONParser parser = new JSONParser();
            try {
                return (Map<String,Object>)parser.parse(permissionsSet);
            } catch (Exception ex) {
                Map<String,Object> set = new HashMap<>();
                LOG.error("Could not parse current permission set: {}", ex.getMessage());
                set.put("error", "Could not parse current permission set, possible no permissions present yet");
                return set;
            }
        } else {
            Map<String,Object> set = new HashMap<>();
            set.put("error", "There are no permissions set");
            return set;
        }
    }
    
    /**
     * Returns the package defined permissions set.
     * @return 
     */
    public final Map<String,Object> getDefinedPermissionsRaw(){
        JSONParser parser = new JSONParser();
        Map<String,Object> set = new HashMap<>();
        try {
            try {
                return (Map<String,Object>)parser.parse(loader.getPackagePermissions());
            } catch (ParseException ex) {
                LOG.error("Could not parse package permission set: {}", ex.getMessage());
                set.put("error", "Could not parse package permission set, invalid package, contact author");
                return set;
            }
        } catch (Exception ex) {
            LOG.error("Permission format does not comply: {}", ex.getMessage());
            set.put("error", "Could not parse package permission set, invalid package, contact author");
            return set;
        }
    }
    
    /**
     * Sets the default protectiondomain.
     */
    private void setProtectionDomain(boolean passed){
        protectionDomain = new DefaultProtectionDomain(null, getPermissionsSet(passed), loader, null);
        LOG.debug("ProtectionDomain for {}: {}", loader.getPackageName(), protectionDomain.getPermissions());
    }
    
    /**
     * Returns the permission set for this package.
     * @return 
     */
    private PermissionCollection getPermissionsSet(boolean fullSet){
        CodeSource codeSource = new CodeSource(loader.getAbsoluteLibraryPath(), new Certificate[] {});
        PermissionCollection permissionCollection = Policy.getPolicy().getPermissions(codeSource);
        permissionCollection.add(new FilePermission(loader.getAbsoluteLibraryPath().toString(), "read"));
        if(fullSet){
            //// set all permissions requested
        }
        return permissionCollection;
    }
    
    /**
     * Sets package details, loads the package classloader and sets the package wide security domain.
     * @throws org.pidome.server.system.packages.PackageInitizializationException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotAvailableException 
     */
    protected void loadPackage() throws PackageInitizializationException, PackagePermissionsNotAvailableException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT * FROM installed_packages ipa WHERE ipa.id=? LIMIT 1")) {
                prepDel.setInt(1, packageId);
                try (ResultSet rsAuthor = prepDel.executeQuery()) {
                    if(rsAuthor.next()){
                        AUTHOR         = rsAuthor.getString("author");
                        AUTHOREMAIL    = rsAuthor.getString("email");
                        AUTHORURL      = rsAuthor.getString("website");
                        PACKAGEBASE    = rsAuthor.getString("install_base");
                        PACKAGENAME    = rsAuthor.getString("name");
                        permissionsSet = rsAuthor.getString("permissions");
                    }
                }
            }
            try {
                loader = new PackageClassLoader(PACKAGENAME,PACKAGEBASE);
                if(SystemConfig.getProperty("system", "server.securitymanagerenabled").equals("true")){
                    try {
                        permissionsAreUpToDate = packagePermissionsAreUpToDate();
                    } catch (PackagePermissionsNotAvailableException ex) {
                        LOG.debug("Package initialization error due to permissions", ex);
                        permissionsAreUpToDate = false;
                        throw new PackagePermissionsNotAvailableException(ex.getMessage());
                    }
                }
                loader.setPackageLibraries();
                if(SystemConfig.getProperty("system", "server.securitymanagerenabled").equals("true")){
                    setProtectionDomain(true);
                }
            } catch (ConfigPropertiesException ex) {
                LOG.error("Initialization error for package '{}': {}", PACKAGENAME, ex.getMessage(), ex);
                throw new PackageInitizializationException("Could not load package: " + ex.getMessage());
            }
        } catch (SQLException ex){
            LOG.error("Details initialization error for package id '{}': {}", packageId, ex.getMessage(), ex);
            throw new PackageInitizializationException("Could not load package details: " + ex.getMessage());
        }
    }
    
    /**
     * Check if the permissions have been set.
     * @return 
     */
    public final boolean hasPackagePermissionsSet(){
        return !this.permissionsSet.isEmpty();
    }
    
    /**
     * Checks if the permissions are still up to date.
     * @return 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotAvailableException 
     */
    public final boolean packagePermissionsAreUpToDate() throws PackagePermissionsNotAvailableException {
        if(this.permissionsSet.equals(loader.getPackagePermissions())){
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Synchronizes the package permissions with permissions in the database.
     * @throws org.pidome.server.system.packages.PackageInitizializationException
     * @throws org.pidome.server.system.packages.PackagePermissionsNotAvailableException
     */
    protected final void syncPermissions() throws PackageInitizializationException, PackagePermissionsNotAvailableException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("UPDATE installed_packages SET permissions=? WHERE id=?")) {
                prepDel.setString(1, loader.getPackagePermissions());
                prepDel.setInt(2, packageId);
                prepDel.executeUpdate();
            }
            permissionsSet = loader.getPackagePermissions();
        } catch (SQLException ex){
            LOG.error("Could not update package permissions for '{}': {}", PACKAGENAME, ex.getMessage(), ex);
            throw new PackageInitizializationException("Could not update package permissions for "+PACKAGENAME+": " + ex.getMessage());
        }
    }
    
    /**
     * Removes permissions set.
     * @throws PackageInitizializationException 
     */
    protected final void deSyncPermissions() throws PackageInitizializationException {
        permissionsSet = "{}";
        permissionsAreUpToDate = false;
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("UPDATE installed_packages SET permissions=? WHERE id=?")) {
                prepDel.setString(1, "{}");
                prepDel.setInt(2, packageId);
                prepDel.executeUpdate();
            }
        } catch (SQLException ex){
            LOG.error("Could not update package permissions for '{}': {}", PACKAGENAME, ex.getMessage(), ex);
            throw new PackageInitizializationException("Could not update package permissions for "+PACKAGENAME+": " + ex.getMessage());
        }
    }
    
    /**
     * Loads the known plugins in this package.
     * @throws ConfigPropertiesException 
     */
    private void loadPlugins() throws ConfigPropertiesException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT ip.[id],ip.[name],ip.[path],ipa.[install_base],ipa.[version] "
                                                                         + "FROM installed_plugins ip "
                                                                   + "INNER JOIN installed_packages ipa ON ip.[package]=ipa.[id] "
                                                                        + "WHERE ipa.id=? ")) {
                prepDel.setInt(1, packageId);
                try (ResultSet rsPeripheral = prepDel.executeQuery()) {
                    while(rsPeripheral.next()){
                        Map<String,String> pluginDetails = new HashMap<>();
                        pluginDetails.put("path", rsPeripheral.getString("path"));
                        pluginDetails.put("name", rsPeripheral.getString("name"));
                        pluginDetails.put("version", rsPeripheral.getString("version"));
                        pluginDetails.put("install_base", rsPeripheral.getString("install_base"));
                        pluginMap.put(rsPeripheral.getInt("id"), pluginDetails);
                    }
                }
            }
        } catch (SQLException ex){
            throw new ConfigPropertiesException("Could not retrieve plugins: " + ex.getMessage());
        }
    }

    /**
     * Returns if this package has a specific installed plugin.
     * @param installedId
     * @return 
     */
    protected final boolean hasPlugin(int installedId){
        return pluginMap.containsKey(installedId);
    }
    
    /**
     * Returns a plugin class.
     * @param installedId
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    protected final Class<?> getPlugin(int installedId) throws ClassNotFoundException,PackagePermissionsNotUpToDateException {
        if(isAuthorized()){
            return loader.classFromPackage(pluginMap.get(installedId).get("path"));
        } else {
            throw new PackagePermissionsNotUpToDateException("Plugin not loaded as package '"+loader.getPackageName()+"' permissions are not up to date");
        }
    }
    
    /**
     * Returns this package class loader.
     * @return 
     */
    public final PackageClassLoader getClassLoader(){
        return loader;
    }
    
    /**
     * Loads the possible peripheral hardware drivers.
     * @throws ConfigPropertiesException 
     */
    private void loadPeripheralDrivers() throws ConfigPropertiesException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT iprf.[id],iprf.[name],iprf.[friendlyname],iprf.[driver],iprf.[vid],iprf.[pid],iprf.[type],iprf.[interface_type],iprf.[version],iprf.[selectable] "
                                                                         + "FROM installed_peripherals iprf "
                                                                   + "INNER JOIN installed_packages ip ON iprf.[package]=ip.[id] "
                                                                        + "WHERE ip.id=? ")) {
                prepDel.setInt(1, packageId);
                try (ResultSet rsPeripheral = prepDel.executeQuery()) {
                    while(rsPeripheral.next()){
                        Map<String,String> driverdetails = new HashMap<>();
                        driverdetails.put("driver", rsPeripheral.getString("driver"));
                        driverdetails.put("friendlyname", rsPeripheral.getString("friendlyname"));
                        driverdetails.put("version", rsPeripheral.getString("version"));
                        driverdetails.put("subtype", rsPeripheral.getString("interface_type"));
                        driverdetails.put("type", rsPeripheral.getString("type"));
                        driverdetails.put("namedid", rsPeripheral.getString("name"));
                        driverdetails.put("fallback", (rsPeripheral.getBoolean("selectable"))?"true":"false");
                        peripheralMap.put(rsPeripheral.getString("vid") +"&"+ rsPeripheral.getString("pid"), driverdetails);
                    }
                }
            }
        } catch (SQLException ex){
            throw new ConfigPropertiesException("Could not load peripheral drivers from database: " + ex.getMessage());
        }
    }
    
    /**
     * Returns true id the package contains a driver based on vendor and product id.
     * @param vendorId
     * @param productId
     * @return 
     */
    public final boolean hasPeripheralDriverByVidPid(String vendorId, String productId){
        return peripheralMap.containsKey(vendorId +"&"+ productId);
    }
    
    /**
     * Check if there is a minimum of one driver available with the given system type and subsystem type
     * @param systemType
     * @param subSystemType
     * @return boolean true if found
     */
    public final boolean hasPeripheralDriverBySubSystem(String systemType, String subSystemType){
        for(Map.Entry<String,Map<String,String>> driver:peripheralMap.entrySet()){
            Map<String,String> item = driver.getValue();
            if((item.containsKey("type") && item.containsKey("subtype") && item.containsKey("fallback")) && (item.get("type").equals(systemType) && item.get("subtype").equals(subSystemType) && item.get("fallback").equals("true"))){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if a driver with the named id is found.
     * @param namedId
     * @return 
     */
    public final boolean hasPeripheralDriverByNamedId(String namedId){
        for(Map.Entry<String,Map<String,String>> driver:peripheralMap.entrySet()){
            Map<String,String> item = driver.getValue();
            if(item.containsKey("namedid") && item.get("namedid").equals(namedId)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns a peripheral class.
     * @param namedId
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    protected final PeripheralHardwareDriverInterface getPeripheralByByNamedId(String namedId) throws ClassNotFoundException,PackagePermissionsNotUpToDateException {
        if(isAuthorized()){
            for(Map.Entry<String,Map<String,String>> driver:peripheralMap.entrySet()){
                Map<String,String> item = driver.getValue();
                if(item.containsKey("namedid") && item.get("namedid").equals(namedId)){
                    try {
                        PeripheralHardwareDriverInterface peripheralDriver = (PeripheralHardwareDriverInterface)loader.classFromPackage(item.get("driver")).getConstructor().newInstance();
                        peripheralDriver.setNamedId(item.get("namedid"));
                        peripheralDriver.setFriendlyName(item.get("friendlyname"));
                        return peripheralDriver;
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOG.error("Driver could not be instantiated: {} ()", ex.getMessage(), item);
                        throw new ClassNotFoundException("Class found but could not be instantiated");
                    }
                }
            }
            throw new ClassNotFoundException("No driver found in '"+loader.getPackageName()+"' as this is not present");
        } else {
            throw new PackagePermissionsNotUpToDateException("Peripheral not loaded as package '"+loader.getPackageName()+"' permissions are not up to date");
        }
    }
    
    /**
     * Returns some details about the hardware driver.
     * @param driverId
     * @param driverVersion
     * @return 
     */
    public final Map<String,String> getPeripheralHardwareDriverDetails(String driverId, String driverVersion){
        return driverMap.get(driverId+"_"+driverVersion);
    }
    
    /**
     * Returns a peripheral class.
     * @param systemType
     * @param subSystemType
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    protected final PeripheralHardwareDriverInterface getPeripheralBySubsystem(String systemType, String subSystemType) throws ClassNotFoundException,PackagePermissionsNotUpToDateException {
        if(isAuthorized()){
            for(Map.Entry<String,Map<String,String>> driver:peripheralMap.entrySet()){
                Map<String,String> item = driver.getValue();
                if((item.containsKey("type") && item.containsKey("subtype") && item.containsKey("fallback")) && (item.get("type").equals(systemType) && item.get("subtype").equals(subSystemType) && item.get("fallback").equals("true"))){
                    try {
                        PeripheralHardwareDriverInterface peripheralDriver = (PeripheralHardwareDriverInterface)loader.classFromPackage(item.get("driver")).getConstructor().newInstance();
                        peripheralDriver.setNamedId(item.get("namedid"));
                        peripheralDriver.setFriendlyName(item.get("friendlyname"));
                        return peripheralDriver;
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOG.error("Driver could not be instantiated: {} ()", ex.getMessage(), item);
                        throw new ClassNotFoundException("Class found but could not be instantiated");
                    }
                }
            }
            throw new ClassNotFoundException("No driver found in '"+loader.getPackageName()+"' as this is not present");
        } else {
            throw new PackagePermissionsNotUpToDateException("Peripheral not loaded as package '"+loader.getPackageName()+"' permissions are not up to date");
        }
    }
    
    /**
     * Returns a peripheral class.
     * @param vendorId
     * @param productId
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    protected final PeripheralHardwareDriverInterface getPeripheral(String vendorId, String productId) throws ClassNotFoundException,PackagePermissionsNotUpToDateException {
        if(isAuthorized()){
            try {
                PeripheralHardwareDriverInterface peripheralDriver = (PeripheralHardwareDriverInterface)loader.classFromPackage(peripheralMap.get(vendorId +"&"+ productId).get("driver")).getConstructor().newInstance();
                peripheralDriver.setNamedId(peripheralMap.get(vendorId +"&"+ productId).get("namedid"));
                peripheralDriver.setFriendlyName(peripheralMap.get(vendorId +"&"+ productId).get("friendlyname"));
                return peripheralDriver;
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOG.error("Driver could not be instantiated: {} ()", ex.getMessage(), peripheralMap.get(vendorId +"&"+ productId));
                throw new ClassNotFoundException("Class found but could not be instantiated");
            }
        } else {
            throw new PackagePermissionsNotUpToDateException("Peripheral not loaded as package '"+loader.getPackageName()+"' permissions are not up to date");
        }
    }
    
    /**
     * Returns the class for the given peripheral software id.
     * @throws ConfigPropertiesException 
     */
    public final void loadPeripheralSoftwareDrivers() throws ConfigPropertiesException {
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT id.[id],id.[driverid], id.[friendlyname],id.[driver],id.[version],id.[hascustom],sd.[scriptcontent],sd.[name] as scriptname "
                                                                         + "FROM installed_drivers id "
                                                                         + "LEFT JOIN scripted_drivers sd ON sd.driverid=id.id "
                                                                   + "INNER JOIN installed_packages ip ON id.[package]=ip.[id] "
                                                                        + "WHERE ip.id=?")) {
                prepDel.setInt(1, packageId);
                try (ResultSet rsPeripheral = prepDel.executeQuery()) {
                    while(rsPeripheral.next()){
                        Map<String,String> driverdetails = new HashMap<>();
                        driverdetails.put("dbid", rsPeripheral.getString("id"));
                        driverdetails.put("driverid", rsPeripheral.getString("driverid"));
                        driverdetails.put("driver", rsPeripheral.getString("driver"));
                        if(rsPeripheral.getString("scriptname")!=null && rsPeripheral.getString("scriptcontent")!=null && !rsPeripheral.getString("scriptcontent").isEmpty() && !rsPeripheral.getString("scriptname").isEmpty()){
                            driverdetails.put("friendlyname", rsPeripheral.getString("friendlyname") + " - " + rsPeripheral.getString("scriptname"));
                        } else {
                            driverdetails.put("friendlyname", rsPeripheral.getString("friendlyname"));
                        }
                        driverdetails.put("version", rsPeripheral.getString("version"));
                        driverdetails.put("scriptcontent", rsPeripheral.getString("scriptcontent"));
                        driverdetails.put("hascustom", (rsPeripheral.getString("hascustom").equals("1"))?"true":"false");
                        driverMap.put(rsPeripheral.getString("driverid")+"_"+rsPeripheral.getString("version"), driverdetails);
                    }
                }
            }
        } catch (SQLException ex){
            throw new ConfigPropertiesException("Could not retrieve peripheral software drivers from database: " + ex.getMessage());
        }
    }
    
    /**
     * Returns some details about the software driver.
     * @param driverId
     * @param driverVersion
     * @return 
     */
    public final Map<String,String> getPeripheralSoftwareDriverDetails(String driverId, String driverVersion){
        return driverMap.get(driverId+"_"+driverVersion);
    }
    
    /**
     * Returns if this package has a specific installed plugin.
     * @param softwareId
     * @param version
     * @return 
     */
    protected final boolean hasPeripheralSoftwareDriver(String softwareId, String version){
        return driverMap.containsKey(softwareId+"_"+version);
    }
    
    /**
     * Returns a plugin class.
     * @param softwareId
     * @param version
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    protected final Class<?> getPeripheralSoftwareDriver(String softwareId, String version) throws ClassNotFoundException,PackagePermissionsNotUpToDateException {
        if(isAuthorized()){
            return loader.classFromPackage(driverMap.get(softwareId+"_"+version).get("driver"));
        } else {
            throw new PackagePermissionsNotUpToDateException("Peripheral software not loaded as package '"+loader.getPackageName()+"' permissions are not up to date");
        }
    }
    
    /**
     * Loads the device drivers.
     * @param deviceId
     * @param devicePackageName
     * @return
     * @throws ClassNotFoundException 
     */
    private void loadInstalledDeviceDrivers() throws ConfigPropertiesException { 
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT id.[id], id.[friendlyname],id.[driver],id.[version] "
                                                                       + " FROM installed_devices id "
                                                                   + "INNER JOIN installed_packages ip ON id.[package]=ip.[id] "
                                                                        + "WHERE ip.id=? ")) {
                prepDel.setInt(1, packageId);
                try (ResultSet rsDevices = prepDel.executeQuery()) {
                    while(rsDevices.next()){
                        Map<String,String> driverdetails = new HashMap<>();
                        driverdetails.put("driver", rsDevices.getString("driver"));
                        driverdetails.put("friendlyname", rsDevices.getString("friendlyname"));
                        driverdetails.put("version", rsDevices.getString("version"));
                        deviceMap.put(rsDevices.getInt("id"), driverdetails);
                    }
                }
            }
        } catch (SQLException ex){
            throw new ConfigPropertiesException("Could not retrieve installed device drivers from database: " + ex.getMessage());
        }
    }
    
    /**
     * Loads a single device driver info.
     * USed for custom devices.
     * @param deviceDriverId 
     */
    protected final void loadSingleDeviceDriver(int deviceDriverId) throws ConfigPropertiesException{
        try (Connection connection = DB.getConnection(DB.DB_SYSTEM)){
            try (PreparedStatement prepDel = connection.prepareStatement("SELECT id.[id], id.[friendlyname],id.[driver] "
                                                                        + " FROM installed_devices id "
                                                                        + "WHERE id.[id]=? LIMIT 1")) {
                prepDel.setInt(1, deviceDriverId);
                try (ResultSet rsDevices = prepDel.executeQuery()) {
                    while(rsDevices.next()){
                        Map<String,String> driverdetails = new HashMap<>();
                        driverdetails.put("driver", rsDevices.getString("driver"));
                        driverdetails.put("friendlyname", rsDevices.getString("friendlyname"));
                        deviceMap.put(rsDevices.getInt("id"), driverdetails);
                    }
                }
            }
        } catch (SQLException ex){
            throw new ConfigPropertiesException("Could not retrieve installed device driver from database: " + ex.getMessage());
        }
    }
    
    /**
     * Returns if this package has a specific installed device.
     * @param installedId
     * @return 
     */
    protected final boolean hasInstalledDevice(int installedId){
        return deviceMap.containsKey(installedId);
    }
    
    /**
     * Returns a device class from an installed device.
     * @param installedId
     * @return 
     * @throws java.lang.ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    protected final Class<?> getInstalledDevice(int installedId) throws ClassNotFoundException,PackagePermissionsNotUpToDateException {
        if(isAuthorized()){
            if(!deviceMap.containsKey(installedId)){
                try {
                    loadSingleDeviceDriver(installedId);
                } catch (ConfigPropertiesException ex) {
                    throw new ClassNotFoundException("Loading driver details for id " + installedId + "failed");
                }
            }
            return loader.classFromPackage(deviceMap.get(installedId).get("driver"));
        } else {
            throw new PackagePermissionsNotUpToDateException("Installed device not loaded as package '"+loader.getPackageName()+"' permissions are not up to date");
        }
    }
    
}