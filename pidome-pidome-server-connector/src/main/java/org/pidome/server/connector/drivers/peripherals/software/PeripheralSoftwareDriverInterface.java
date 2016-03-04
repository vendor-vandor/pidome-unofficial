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
package org.pidome.server.connector.drivers.peripherals.software;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceDriverListener;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;

/**
 * The interface for service driver creation. This interface shows the minimum
 * requirements for your drivers. A tiny little explanation of the batches and
 * sendData: Batches are called by the eventService when it recognizes that
 * multiple commands are going to be send to the same proxy. When it is called
 * by an event it will use the eventName as batchName, The EventService runs the
 * batch, but does not require results of the batch being started. You should
 * implement runBatch with a thread or you will hang the EventService (at this
 * moment)
 *
 * sendData is used by the terminal or when there is no batch to handle.
 * Terminal runs in async, other methods not (at this moment).
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public interface PeripheralSoftwareDriverInterface extends DriverBaseDataEventListener {

    /**
     * Is set in the BaseDriver, you can overwrite it, but for debugging
     * purposes not handy
     */
    public final static String DriverBaseName = "driver:port";

    /**
     * Returns the driver DB id.
     * @return 
     */
    public int getId();
    
    /**
     * Starts the driver and runs the thread
     */
    public void startDriver();

    /**
     * Stops the driver and the thread the driver is running in
     */
    public void stopDriver();

    /**
     * Returns the Hardware driver which belongs to the given running software
     * driver.
     *
     * @return
     */
    public Peripheral getHardwareDriverFromSoftwareDriver();

    
    /**
     * Returns the hardware driver.
     * @return 
     */
    public PeripheralHardwareDriverInterface getHardwareDriver();
    
    /**
     * Returns the amount of active devices attached to this driver.
     *
     * @return
     */
    public int getRunningDevicesCount();

    /**
     * Returns a list of running devices whom belong to this driver.
     *
     * @return
     */
    public List<Device> getRunningDevices();

    /**
     * Send a batch of commands through the proxy. This method is preferred when
     * sending multiple commands at once with a build in delay. this function
     * should create a batch with a default name (anonymous)
     *
     * @param batchData Strings with commands
     * @see #runBatch()
     * @see #runBatch(java.lang.String)
     */
    public void addBatch(List<Map<String, String>> batchData);

    /**
     * Send a batch of commands through the proxy. This method is preferred when
     * sending multiple commands at once with a build in delay.
     *
     * @param batchData Strings with commands
     * @param batchName String with the name of the batch to use later on with
     * runBatch()
     * @see #runBatch()
     * @see #runBatch(java.lang.String)
     */
    public void addBatch(List<Map<String, String>> batchData, String batchName);

    /**
     * Runs the batch previously set with the anonymous addBatch
     */
    public void runBatch();

    /**
     * Runs the batch previously set with addBatch with a batchName
     *
     * @param batchName Name of the batch to run
     */
    public void runBatch(String batchName);

    /**
     * Sends the data to the hardware itself
     *
     * @param data The data to send
     * @param prefix When having multiple devices supported by the hardware a
     * prefix can distinct the device in your hardware
     * @return result of the send command
     * @throws java.io.IOException
     * @Obsolete Use writeBytes.
     */
    public boolean sendData(String data, String prefix) throws IOException;

    /**
     * Sends the data to the hardware itself
     *
     * @param data The data to send
     * @return the result of the command
     * @throws java.io.IOException
     * @Obsolete Use WriteBytes.
     */
    public boolean sendData(String data) throws IOException;

    /**
     * Retrieves the name of the class.
     *
     * @return the name of the class
     */
    public String getName();

    /**
     * For adding an hardware driver.
     *
     * @param l
     */
    public void setPeripheralEventListener(PeripheralHardwareDriverInterface l);

    /**
     * for removing an hardware driver.
     *
     * @param l
     */
    public void removePeripheralEventListener(PeripheralHardwareDriverInterface l);

    /**
     * Handles data received from the hardware driver to be used by overwriting
     * classes.
     *
     * @param oEvent
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent);

    /**
     * Driver proxy for hardware data.
     *
     * @param oEvent
     */
    public void driverBaseDataReceivedProxy(PeripheralHardwareDataEvent oEvent);

    /**
     * Returns the package name of the driver as used in the database.
     *
     * @return The package name of this class
     */
    public String getPackageName();

    /**
     * Adds a device to the listers for whom data can exist.
     *
     * @param device
     */
    public void addDeviceListener(DeviceDriverListener device);

    /**
     * Removes a device from the listeners list.
     *
     * @param device
     */
    public void removeDeviceListener(DeviceDriverListener device);

    /**
     * Handle the data coming from a device driver to dispatch to the hardware
     * driver. With this function you can do stuff with the data. For example if
     * needed change from string to byte array?
     *
     * @param device
     * @param group
     * @param set
     * @param deviceData
     * @return A string which represents a result, human readable please.
     * @throws java.io.IOException
     */
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException;

    /**
     * Handles data coming from a device as is in the request!
     * @param device
     * @param request
     * @return
     * @throws IOException 
     */
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException;
    
    /**
     * Sets the driver DB id.
     * @param driverDBId 
     */
    public void setId(int driverDBId);
    
    /**
     * Sets the named id
     * @param dbNameId
     */
    public void setNamedId(String dbNameId);
    
    /**
     * Gets the named id
     * @return 
     */
    public String getNamedId();
    
    /**
     * Returns if the driver supports custom devices.
     * @return 
     */
    public boolean hasCustom();
    
    /**
     * Set if a driver has custom devices.
     * @param hasCustom
     * @return 
     */
    public void setHasCustom(boolean hasCustom);
    
    /**
     * Returns the software driver id.
     *
     * @param softwareId
     */
    public void setSoftwareDriverId(String softwareId);

    /**
     * Returns the version set in the driver.
     *
     * @param softwareIdVersion
     */
    public void setSoftwareDriverVersion(String softwareIdVersion);

    /**
     * Returns the software driver id.
     *
     * @return
     */
    public String getSoftwareDriverId();

    /**
     * Returns the version set in the driver.
     *
     * @return
     */
    public String getSoftwareDriverVersion();

    /**
     * Returns true if there is a web presentation present.
     *
     * @return
     */
    public boolean hasPresentation();

    /**
     * Sets a web presentation;
     *
     * @param pres
     */
    public void addWebPresentationGroup(WebPresentationGroup pres);

    /**
     * Returns the web presentation.
     *
     * @return
     */
    public List<WebPresentationGroup> getWebPresentationGroups();

    /**
     * Sets the friendlyname.
     * @param name 
     */
    public void setFriendlyName(String name);
    
    /**
     * Returns the friendlyname.
     * @return 
     */
    public String getFriendlyName();
    
    /**
     * Sets a link with the device service.
     * @param deviceServiceLink 
     */
    public void setDeviceServiceLink(PeripheralDriverDeviceMutationInterface deviceServiceLink);
    
    /**
     * Removes a device service link.
     */
    public void removeDeviceServiceLink();
    
    /**
     * Indicator to let know a device is loaded.
     * @param device 
     */
    public void deviceLoaded(Device device);
    
}