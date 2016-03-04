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

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;

/**
 * Proxy for package functions.
 * 
 * This proxy handles loading of drivers, getting package details and package collections.
 * 
 * @author John Sirach
 */
public class PackageProxy {

    static Packages packages = null;
    
    static Logger LOG = LogManager.getLogger(PackageProxy.class);
    
    /**
     * Creates the package instance.
     * Because the package instance can only be loaded once, the package proxy should always be used.
     */
    public PackageProxy(){
        if(packages==null){
            try {
                packages = Packages.getInstance();
            } catch ( UnsupportedOperationException ex) {
                LOG.fatal("Could not initialize packages: {}", ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Returns the class for the given installed plugin id.
     * @param installedPluginId
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final Class<?>loadPlugin(int installedPluginId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadPlugin(installedPluginId);
    }
    
    /**
     * Loads the peripheral hardware driver based on the vendor and product id (class loader)
     * @param vendorId
     * @param productId
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final PeripheralHardwareDriverInterface loadPeripheralDriverByVidPid(String vendorId, String productId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadPeripheralDriverByVidPid(vendorId,productId);
    }
    
    /**
     * Loads the peripheral hardware driver based on the vendor and product id (class loader)
     * @param systemType
     * @param subSystemType
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final PeripheralHardwareDriverInterface loadPeripheralDriverBySubSystem(String systemType, String subSystemType) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadPeripheralDriverBySubSystem(systemType, subSystemType);
    }
    
    /**
     * Loads a peripheral driver by it's named id.
     * @param namedId
     * @return
     * @throws ClassNotFoundException
     * @throws PackagePermissionsNotUpToDateException 
     */
    public final PeripheralHardwareDriverInterface loadPeripheralDriverByNamedId(String namedId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadPeripheralDriverByNamedId(namedId);
    }
    
    /**
     * Loads the peripheral software driver based on software id (class loader)
     * @param peripheralSoftwareId
     * @param version
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final Class<?>loadPeripheralSoftwareDriver(String peripheralSoftwareId, String version) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadPeripheralSoftwareDriver(peripheralSoftwareId, version);
    }
    
    /**
     * Returns a specifc driver details.
     * @param softwareDriverId
     * @param softwareDriverVersion 
     * @return  
     * @throws java.lang.ClassNotFoundException 
     */
    public final Map<String,String> getPeripheralSoftwareDriverDetails(String softwareDriverId, String softwareDriverVersion) throws ClassNotFoundException {
        return packages.getPeripheralSoftwareDriverDetails(softwareDriverId, softwareDriverVersion);
    }
    
    /**
     * Loads the device driver based on device id (class loader)
     * @param deviceId
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final Class<?>loadDeviceDriver(int deviceId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadDeviceDriver(deviceId);
    }
    
    /**
     * Loads the device driver based on installed device id (Class loader).
     * @param deviceId
     * @return
     * @throws ClassNotFoundException 
     * @throws org.pidome.server.system.packages.PackagePermissionsNotUpToDateException 
     */
    public final Class<?>loadInstalledDeviceDriver(int deviceId) throws ClassNotFoundException, PackagePermissionsNotUpToDateException {
        return packages.loadInstalledDeviceDriver(deviceId);
    }
    
    /**
     * Returns a list of possible drivers specific for the given vendor and product id
     * @param vendorId
     * @param productId
     * @return 
     */
    public final List<Map<String,String>> getPeripheralDriversForPeripheral(String hardwareDriverName, String vendorId, String productId){
        return packages.getPeripheralDriversByProductId(hardwareDriverName, vendorId, productId);
    }
    
    /**
     * 
     * @param deviceId
     * @return 
     */
    public final String getDevicePeripheralSoftwareDriver(int deviceId){
        return packages.getMappedDevicePeripheralSoftwareDriver(deviceId);
    }
    
}
