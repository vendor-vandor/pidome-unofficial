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

package org.pidome.server.connector.drivers.peripherals.hardware;

import java.io.IOException;
import java.util.Map;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 *
 * @author John Sirach
 */
public interface PeripheralHardwareDriverInterface {
    
    /**
     * Set the peripheral where it is all about.
     * @param peripheral 
     */
    public void setPeripheral(Peripheral peripheral);
    
    /**
     * Checks if the peripheral has options set, these are used to set driver options to be set by the end user
     * @return 
     */
    public boolean hasPeripheralOptions();
    
    /**
     * Use Map<String(name),String(option description)> map to set the options, the corresponding value attached to the option description will be set.
     * @param deviceOptionsNvp 
     */
    public void setPeripheralOptions(Map<String,String>deviceOptionsNvp);
    
    /**
     * Called after setPeripheralOptions.
     * @param deviceOptionsNvp 
     */
    public void putPeripheralOptions(Map<String,String>deviceOptionsNvp);
    
    /**
     * Returns the full options list.
     * @return 
     */
    public Map<String,PeripheralHardwareDriver.PeripheralOption> getPeripheralOptions();
    
    /**
     * Starts the driver, throws exception when this does not work.
     * @throws PeripheralHardwareException 
     */
    public abstract void startHardwareDriver() throws PeripheralHardwareException;
    
    /**
     * Stops the peripheral driver
     */
    public abstract void stopHardwareDriver();
    
    /**
     * Reads the peripheral port, or implements function to read the port so it can return a string.
     * @return The string read from the device
     * @throws IOException When readPort fails a closePort is called and the driver is stopped.
     */
    public abstract String readPort() throws IOException;

    /**
     * Writes the data to the port, the function retrieves a string, it is up to the driver author to correct this
     * @param bytes
     * @throws IOException When there is a port error. An exception always calls the closePort function and then stops the driver.
     */
    public abstract void writePort(byte[] bytes) throws IOException;
    
    /**
     * Closes the port to the peripheral
     */
    public abstract void releaseDriver();
    
    /**
     * Driver initialization when needed
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     * @throws UnsupportedOperationException When init is not supported
     */
    public void initDriver() throws PeripheralHardwareException,UnsupportedOperationException;
    
    /**
     * Gets the peripheral software id, if supported.
     * @return The peripheral software id
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     * @throws UnsupportedOperationException 
     */
    public PeripheralHardwareDriver.PeripheralVersion getSoftwareId() throws PeripheralHardwareException,UnsupportedOperationException;
    
    /**
     * Retrieves the driver name
     * @return the name
     */
    public String getName();
    
    /**
     * Adds a software driver listener.
     * @param softwareDriver 
     */
    public void setPeripheralEventListener(PeripheralSoftwareDriverInterface softwareDriver);
    
    /**
     * Removes a software driver listener.
     * @param softwareDriver 
     */
    public void removePeripheralEventListener(PeripheralSoftwareDriverInterface softwareDriver);
    
    /**
     * Sets the named id form the db.
     * @param namedId 
     */
    public void setNamedId(String namedId);
    
    /**
     * Returns the named id from the db.
     * @return 
     */
    public String getNamedId();
    
    /**
     * Returns the vendor id
     * @return 
     */
    public String getVendorID();
    
    /**
     * Returns the product id
     * @return 
     */
    public String getProductID();
    
    /**
     * Set's a custom driver version.
     * @param id
     * @param version
     * @return 
     */
    public PeripheralHardwareDriver.PeripheralVersion setCustomSoftwareId(String id, String version);
    
    /**
     * Retrieves the port for the peripheral
     * @return The port name as used by the hardware
     */
    public String getPort();
    
    /**
     * Sets the friendlyName.
     * @param friendlyName 
     */
    public void setFriendlyName(String friendlyName);
    
    /**
     * Returns the friendlyName.
     * @return 
     */
    public String getFriendlyName();
 
    /**
     * Indicator the hardware driver is actively communicating.
     * @return 
     */
    public boolean isActive();
    
}
