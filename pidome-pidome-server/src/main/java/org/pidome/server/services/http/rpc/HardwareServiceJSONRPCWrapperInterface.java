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

package org.pidome.server.services.http.rpc;

import java.io.IOException;
import java.util.Map;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareException;
import org.pidome.server.system.config.ConfigPropertiesException;

/**
 *
 * @author John
 */
public interface HardwareServiceJSONRPCWrapperInterface {
    
    /**
     * Returns a list of connected peripherals.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getConnectedHardware();
    
    /**
     * Returns the software driver.
     * @param peripheralPort
     * @return
     * @throws PeripheralSoftwareException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getSoftwareDriverPresentation(String peripheralPort) throws PeripheralSoftwareException;
    
    /**
     * Returns a list of software drivers.
     * This function only returns active software drivers.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getPeripheralSoftwareDrivers();
    
    /**
     * Stops an attached peripheral including all relying entities.
     * @param peripheralPort
     * @return
     * @throws PeripheralSoftwareException 
     */
    @PiDomeJSONRPCPrivileged
    public Object disconnectPeripheral(String peripheralPort) throws PeripheralSoftwareException;

    /**
     * Only returns peripheral software drivers which have custom devices.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getPeripheralSoftwareDriversForCustomDevices();
    
    /**
     * Returns the device options set.
     * @param peripheralPort
     * @return
     * @throws PeripheralSoftwareException
     * @throws PeripheralHardwareException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getPeripheralConnectSettings(String peripheralPort) throws PeripheralSoftwareException, PeripheralHardwareException;
    
    /**
     * Returns the possible connect options for devices awaiting connect settings.
     * @param peripheralPort
     * @return
     * @throws PeripheralSoftwareException
     * @throws PeripheralHardwareException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getWaitingPeripheralConnectSettings(String peripheralPort) throws PeripheralSoftwareException, PeripheralHardwareException;
    
    /**
     * Set peripheral options, and save the settings after start.
     * @param peripheralPort
     * @param parameters
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public boolean setPeripheralConnectSettings(String peripheralPort, Map<String,String> parameters) throws PeripheralHardwareException;
    
    /**
     * Returns A connected peripherals info.
     * @param peripheralPort
     * @return
     * @throws PeripheralSoftwareException
     * @throws PeripheralHardwareException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getConnectedPeripheralInfo(String peripheralPort) throws PeripheralSoftwareException, PeripheralHardwareException;
    
    /**
     * Returns a list of custom created drivers.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getScriptedSoftwareDrivers();
    
    /**
     * Returns a list of local known dev types entries.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getLocalDeviceEntries() throws ConfigPropertiesException, IOException ;
    
    /**
     * Creates a custom serial port so it can be used with a driver.
     * @param port
     * @param friendlyName
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object createCustomSerialDevice(String port, String friendlyName) throws PeripheralHardwareException, ConfigPropertiesException, IOException;
    
}