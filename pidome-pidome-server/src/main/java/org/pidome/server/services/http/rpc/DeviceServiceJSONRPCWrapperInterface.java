/*
 * Copyright 2014 John Sirach <john.sirach@gmail.com>.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException;
import org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException;
import org.pidome.server.services.hardware.DeviceServiceException;
import org.pidome.server.system.hardware.devices.DevicesException;

/**
 * functions promise for RPC.
 * @author John Sirach
 */
interface DeviceServiceJSONRPCWrapperInterface {

    /**
     * Sends a command to a device.
     * @param deviceId
     * @param cmdGroup
     * @param cmdSet
     * @param deviceCommand
     * @return
     * @throws DeviceServiceException 
     */
    Object sendDevice(Long deviceId, String cmdGroup, String cmdSet, Map<String,Object> action) throws DeviceServiceException;
    
    /**
     * Tries to start a device.
     * @param deviceId
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    Object startDevice(Long deviceId) throws DeviceServiceException;
    
    /**
     * Edits an device.
     * @param deviceId
     * @param location
     * @param address
     * @param name
     * @param category
     * @param favorite
     * @param settings
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    Object editDevice(Long deviceId, Long location, String address, String name, Long category, boolean favorite, Map settings,List<Map<String,Object>> modifiers) throws DeviceServiceException;
    
    /**
     * Adds a device to the server.
     * @param device
     * @param location
     * @param address
     * @param name
     * @param category
     * @param favorite
     * @param settings
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    Object addDevice(Long device, Long location, String address, String name, Long category, boolean favorite, Map settings,List<Map<String,Object>> modifiers) throws DeviceServiceException;
 
    /**
     * Deletes a device from the server.
     * @param deviceId
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    Object deleteDevice(Long deviceId) throws DeviceServiceException;
    
    /**
     * Returns a list of installed devices.
     * @return
     * @throws DeviceServiceException 
     */
    Object getInstalledDevices() throws DeviceServiceException;
    
    /**
     * Returns a list of devices added to the server.
     * @return
     * @throws DeviceServiceException 
     */
    Object getDeclaredDevices() throws DeviceServiceException;
    
    /**
     * Returns a single device added to the server.
     * With this function you can not rely on the fact the device is active. Use getDevice instead.
     * @param id
     * @return
     * @throws DeviceServiceException 
     */
    Object getDeclaredDevice(Long id) throws DeviceServiceException;
    
    /**
     * Returns a list of devices currently active.
     * If a specific device should be active retrieve it with getDeclaredDevices and try to start it.
     * @return
     * @throws DeviceServiceException 
     */
    Object getActiveDevices() throws DeviceServiceException;
    
    ///// The next are convenience methods
    /**
     * Gets the groups defined for devices based on command type filter.
     * An optional list of filters can be given to limit the returned groups containing specific filter defined data types.
     * @param deviceId
     * @param filter
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    Object getDeviceActionGroups(Long deviceId, ArrayList<Object> filter) throws DeviceServiceException,UnknownDeviceException;
    
    /**
     * Returns a list of commands belonging to a group
     * @param deviceId
     * @param groups
     * @param filter
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    @PiDomeJSONRPCPrivileged
    Object getDeviceActionGroupCommands(Long deviceId, String group, ArrayList<Object> filter) throws DeviceServiceException,UnknownDeviceException;
    
    /**
     * Returns a single device.
     * @param deviceId
     * @return
     * @throws UnknownDeviceException 
     */
    Object getDevice(Long deviceId) throws UnknownDeviceException, DeviceServiceException; 
    
    /**
     * Gets a single device command detail set.
     * @param deviceId
     * @param group
     * @param commandId
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    @PiDomeJSONRPCPrivileged
    Object getDeviceCommand(Long deviceId, String group, String commandId) throws DeviceServiceException, UnknownDeviceException;
    
    /**
     * Gives the possibility to favorite a device.
     * @param deviceId
     * @param favorite
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    Object setFavorite(Long deviceId, Boolean favorite) throws DeviceServiceException, UnknownDeviceException;
    
    /**
     * Sets dimensions for a device to visualize, if the old param is !=0 the old devie is removed and the deviceid is set.
     * @param deviceId
     * @param x
     * @param y
     * @param old
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    @PiDomeJSONRPCPrivileged
    boolean setVisualDevice(Long deviceId, Long x, Long y, Long old) throws DeviceServiceException, UnknownDeviceException;
    
    /**
     * Sets dimensions for a device to visualize.
     * @param deviceId
     * @param x
     * @param y
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    @PiDomeJSONRPCPrivileged
    boolean updateVisualDevice(Long deviceId, Long x, Long y) throws DeviceServiceException, UnknownDeviceException;
    
    /**
     * Removes visualizing
     * @param deviceId
     * @return
     * @throws DeviceServiceException
     * @throws UnknownDeviceException 
     */
    @PiDomeJSONRPCPrivileged
    boolean removeVisualDevice(Long deviceId) throws DeviceServiceException, UnknownDeviceException;
    
    /**
     * Get visible devices based on floor id.
     * @param floorId
     * @return
     * @throws DeviceServiceException 
     */
    Object getVisualDevices(Long floorId) throws DeviceServiceException;
    
    /**
     * Returns a list of custom created devices.
     * @return
     * @throws DeviceServiceException 
     */
    Object getCustomDevices() throws DeviceServiceException;
    
    /**
     * Returns a list of installed device based on an active (running) hardware peripheral port.
     * @param peripheralPort
     * @return 
     */
    Object getPeripheralDeclaredDevices(String peripheralPort) throws DeviceServiceException;
    
    /**
     * Executes a device action in a peripheral software driver which much be 
     * active on a specific port.
     * @param peripheralPort
     * @param params
     * @return
     * @throws DeviceServiceException 
     */
    Object peripheralDeviceFunction(String peripheralPort, Map<String,Object> params) throws DeviceServiceException;
    
    /**
     * Returns only the active favorite devices
     * @return
     * @throws DeviceServiceException 
     */
    Object getFavoriteDevices() throws DeviceServiceException;
    
    /**
     * Returns a full device list with all it's current data.
     * @return 
     */
    Object getDeclaredDevicesWithFullDetails() throws DeviceServiceException;
    
    /**
     * Returns a list of devices by the peripheral software id.
     * @param id
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    Object getDevicesByPeripheralSoftwareDriver(Long id);
    
    /**
     * Get all declared custom devices.
     * These devices are provided by it's drivers.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    Object getDeclaredCustomDevices();
    
    /**
     * Returns a list of discovered devices.
     * @return 
     */
    public Object getDiscoveredDevices();
    
    /**
     * Returns a list of discovered devices.
     * @return 
     */
    public Object getDiscoveredDevice(String port, String deviceAddress) throws DiscoveredDeviceNotFoundException;
    
    /**
     * Deletes a discovered device from the list.
     * If a device is rediscovered, it will be re-added.
     * @return 
     */
    public boolean removeDiscoveredDevice(String peripheralPort, String deviceAddress) throws DiscoveredDeviceNotFoundException;
    
    /**
     * Enables discovery for a particular driver.
     * If a device is rediscovered, it will be re-added.
     * @return 
     */
    public Object enableDeviceDiscovery(String peripheralPort, Long period) throws TimedDiscoveryException;
    
    /**
     * Disables discovery for a particular driver.
     * If a device is rediscovered, it will be re-added.
     * @return 
     */
    public Object disableDeviceDiscovery(String peripheralPort) throws TimedDiscoveryException;
    
    /**
     * Returns a list of drivers capable of doing discovery.
     * @return 
     */
    public Object getDiscoveryEnabledDrivers();
    
    /**
     * Returns a custom device by it's named id.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getCleanCustomDevice(String deviceId);
    
    /**
     * Deletes a created custom device.
     * When deleting a custom device skeleton it will also unload and delete all devices created with this skeleton.
     * @param customDeviceId
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deleteCustomDevice(Number customDeviceId);
    
    /**
     * Returns the device structure of an installed device.
     * @param installedId
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getDeviceStructure(Number installedId) throws DeviceServiceException;
 
    /**
     * 
     * @param deviceId
     * @return
     * @throws DeviceServiceException 
     */
    public Object getDeviceSettings(Number deviceId) throws DeviceServiceException, UnknownDeviceException;
    
    /**
     * Returns the settings for a device based in the installed device.
     * @param installedDeviceId The id of the device installed.
     * @return The common settings of an installed device.
     * @throws DeviceServiceException When there is an error within the device.
     * @throws UnknownDeviceException When the device can not be found.
     */
    @PiDomeJSONRPCPrivileged
    public Object getInstalledDeviceSettings(Number installedDeviceId) throws DeviceServiceException, UnknownDeviceException, DevicesException;
    
    /**
     * Returns the skeleton structure of an installed device.
     * @param installedDeviceId
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getDeviceSkeleton(Number installedDeviceId);
    
    /**
     * Add a custom device.
     * @param identifier
     * @param friendlyname
     * @param deviceName
     * @param struct
     * @param driverId
     * @param packageId
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public int addCustomDevice(String identifier, String friendlyname, String deviceDriver, Map<String,Object> struct, Number driverId, Number packageId) throws UnsupportedDeviceException;
    
    /**
     * Updates a custom device.
     * @param customDeviceId
     * @param friendlyName
     * @param struct
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public boolean updateCustomDevice(Number customDeviceId, String friendlyName, String name, Map<String,Object> struct) throws UnsupportedDeviceException;
    
    /**
     * Assigns a custom device to a driver.
     * This function retreives all the devices based on the current custom device and iof they are running they are restarted so they
     * are immediately running. The new driver must be running for devices to start, otherwise they are only stopped.
     * @param customDriverId
     * @param customDeviceId
     * @return
     * @throws UnsupportedDeviceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean assignCustomDevice(Number customDriverId, Number customDeviceId) throws DeviceServiceException;
}