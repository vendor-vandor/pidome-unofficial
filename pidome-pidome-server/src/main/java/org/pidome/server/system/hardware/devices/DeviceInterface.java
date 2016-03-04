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

import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsSet;
import java.util.List;
import java.util.Map;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceDataListener;
import org.pidome.server.connector.drivers.devices.DeviceDataStoreListener;
import org.pidome.server.connector.drivers.devices.DeviceStructure.DeviceOptions;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 *
 * @author John Sirach
 */
public interface DeviceInterface {
    
    /**
     * Returns the available device options.
     * @return 
     */
    public DeviceOptions getDeviceOptions();
    
    /**
     * Returns the encapsulated device.
     * @return 
     */
    public abstract Device getDevice();
    
    /**
     * handles the device command.
     * @param group
     * @param set
     * @param action
     * @param force In case a device is locked (because of scene) set to true to override the scene lock and modify device state.
     * @throws UnsupportedDeviceCommandException 
     */
    public void handleCommand(String group, String set, Map<String,Object> action, boolean force) throws UnsupportedDeviceCommandException;
    
    /**
     * handles the device command.
     * @param group
     * @param set
     * @param action
     * @throws UnsupportedDeviceCommandException 
     */
    public void handleCommand(String group, String set, Map<String,Object> action) throws UnsupportedDeviceCommandException;
    
    /**
     * handles the device command.
     * @param group
     * @param set
     * @param action
     * @throws UnsupportedDeviceCommandException 
     */
    public void handleCommand(String group, String set, String action) throws UnsupportedDeviceCommandException;
            
    /**
     * Creates the device components based on the xml.
     * @param xml
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException
     */
    public void createDeviceComponentsSet(String xml) throws UnsupportedDeviceException;
    
    /**
     * Returns the storage set.
     * @return 
     */
    public Map<String,List<String>> getStorageSet();
    
    /**
     * Returns boolean if there is a storage set.
     * @return 
     */
    public boolean hasStorageSet();
    
    /**
     * Function called so instead of using the device constructor where little is known use this function to do initial setup.
     * @param firstPrepare
     */
    public void prepare(boolean firstPrepare);
    
    /**
     * Should return the result of a send command.
     * @return true or false if a command is dispatched to the driver.
     */
    public boolean commandResult();

    /**
     * instructs the peripheral software driver to include this device.
     * @param driverClass 
     */
    public void setDriverListener(PeripheralSoftwareDriverInterface driverClass);
    /**
     * Removes the peripheral software driver from the device.
     * But it also removes the listener the other way around.
     * @param device The device itself (i know it is strange, but it is for checking if the device implements DeviceDriverListener)
     */
    public void removeDriverListener(DeviceInterface device);
    
    /**
     * Adds a listener for data storage
     * @param l 
     */
    public void addDeviceDataStorageListener(DeviceDataStoreListener l);

    /**
     * Removes a Data storage listener
     * @param l 
     */
    public void removeDeviceDataStorageListener(DeviceDataStoreListener l);
    
    /**
     * Adds a listener for device data.
     * @param l 
     */
    public void addDeviceDataListener(DeviceDataListener l);

    /**
     * Removes a listener for device data.
     * @param l 
     */
    public void removeDeviceDataListener(DeviceDataListener l);
    
    /**
     * Retrieves details about a command set.
     * @param setId
     * @return 
     * @throws org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException 
     */
    public DeviceControl getDeviceCommandSet(String setId) throws DeviceControlException;
    
    /**
     * Returns the full command set for this device.
     * @return 
     */
    public DeviceControlsSet getFullCommandSet();
    
    /**
     * Gets the location details.
     * @param location
     * @return Map<String,String> with location information
     */
    public Map<String,String> getLocation(String location);
    
    /**
     * Gets all the device locations.
     * @return Map with locations
     */
    public Map getDeviceLocations();
    
    /**
     * Returns the XML of the device.
     * @return the XML of the device as a string
     * @throws org.pidome.server.connector.drivers.devices.UnsupportedDeviceException
     */
    public String getDeviceXml() throws UnsupportedDeviceException;
 
    /**
     * Returns the attached driver.
     * @return The name of the attached driver
     */
    public String getDriverName();
    
    /**
     * Returns the devices package name.
     * @return The package name of the device as defined.
     */
    public String getName();
 
    /**
     * Returns the history of the last known cmd set.
     * The returned object is always i representation of the data type of the control.
     * To determine the control's data type get the data type first.
     * @return Map<String,Map<String,Object>>
     */
    public Map<String,Map<String,Object>> getStoredCmdSet();
    
    /**
     * Starts listeners in devices.
     * For example if retrieving of data does need an handleCommand instruction a looping thread can be used.
     */
    public void startReceivers();
    
    /**
     * Stops listeners in devices
     * @see #startReceivers() 
     */
    public void stopReceivers();
    
    /**
     * Sets a device id
     * @param deviceId 
     */
    public void setId(int deviceId);
    
    /**
     * Sets the id of the installed device.
     * @param installedId 
     */
    public void setInstalledDeviceId(int installedId);
    
    /**
     * Sets the driver name of the device.
     * @param driverName 
     */
    public void setDeviceDriver(String driverName);
    
    /**
     * Sets the device friendly name
     * @param friendlyName 
     */
    public void setFriendlyName(String friendlyName);
    
    /**
     * Sets the location id of this device.
     * @param locationId 
     */
    public void setLocationId(int locationId);
    
    /**
     * Sets the textual representation of the device address.
     * @param deviceAddress 
     */
    public void setAddress(String deviceAddress);
    
    /**
     * Sets the base name of the device.
     * @param deviceName 
     */
    public void setDeviceName(String deviceName);
    
    /**
     * Sets the category this device belongs to.
     * @param categoryId 
     */
    public void setCategoryId(int categoryId);
    
    /**
     * Sets if the device is fixed.
     * @param fixed 
     */
    public void setIsFixed(boolean fixed);
    
    /**
     * Sets the name of the driver of the device (not the device driver self).
     * @param driverDriverName 
     */
    public void setDeviceDriverDriver(String driverDriverName);
    
    /**
     * Sets what type of device it is.
     * @param deviceType 
     */
    public void setDeviceType(int deviceType);
    
    /**
     * Sets if the device is active.
     * @param active 
     */
    public void setisActive(boolean active);
    
    /**
     * Sets a favorite or not.
     * @param favorite 
     */
    public void setIsFavorite(boolean favorite);
    
    /**
     * Set the category name.
     * @param name 
     */
    public void setCategoryName(String name);
    
    /**
     * Sets the constant name for a device category
     * @param constant 
     */
    public void setCategoryConstant(String constant);
    
    /**
     * Sets the location name.
     * @param name 
     */
    public void setLocationName(String name);
    
    /**
     * Sets the data retention file.
     * Storage in this file is depending on the device control.
     * @param retFile 
     */
    public void setRetentionFile(RetentionHandler retFile);
    
    /**
     * Stores the retention data.
     */
    public void storeRetentionData();
    
    /**
     * Gets a device id
     * @return 
     */
    public int getId();
    
    /**
     * Gets the id of the installed device.
     * @return 
     */
    public int getInstalledDeviceId();
    
    /**
     * Gets the driver name of the device.
     * @return 
     */
    public String getDeviceDriver();
    
    /**
     * Gets the device friendly name
     * @return 
     */
    public String getFriendlyName();
    
    /**
     * Gets the location id of this device.
     * @return 
     */
    public int getLocationId();
    
    /**
     * Returns the device xml definition sequence number.
     * This is leading to determine the version.
     * @return 
     */
    public int getDefinitionSequence();
    
    /**
     * Sets the device xml definition sequence.
     * @param sequence 
     */
    public void setDefinitionSequence(int sequence);
    
    /**
     * Gets the textual representation of the device address.
     * @return 
     */
    public String getAddress();
    
    /**
     * Gets the base name of the device.
     * @return 
     */
    public String getDeviceName();
    
    /**
     * Gets the category this device belongs to.
     * @return 
     */
    public int getCategoryId();
    
    /**
     * Gets if the device is fixed.
     * @return 
     */
    public boolean getIsFixed();
    
    /**
     * Gets the name of the driver of the device (not the device driver self).
     * @return 
     */
    public String getDeviceDriverDriver();
    
    /**
     * Gets what type of device it is.
     * @return 
     */
    public int getDeviceType();
    
    /**
     * Gets if the device is active.
     * @return 
     */
    public boolean getisActive();
    
    /**
     * Returns if a device is favorite or not.
     * @return 
     */
    public boolean getIsFavorite();
    
    /**
     * Returns the location name.
     * @return 
     */
    public String getLocationName();
    
    /**
     * Returns the category name.
     * @return 
     */
    public String getCategoryName();
    
    /**
     * Returns the constant name of a device category.
     * @return 
     */
    public String getCategoryConstant();
    
    /**
     * Returns the last time this device has received data.
     * @return 
     */
    public String getLastReceiveTime();

    /**
     * returns the last time this device has send some data.
     * @return 
     */
    public String getLastSendTime();
    
    /**
     * Used to shutdown devices.
     */
    public void shutdownDevice();
    
    /**
     * Used to shutdown devices.
     */
    public void startupDevice();
    
}
