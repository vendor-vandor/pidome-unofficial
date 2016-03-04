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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 * The base peripheral device.
 * This class is used to be a proxy between the communication with the hardware and the peripheral software.
 * Through this class the peripheral software can communicate with the hardware through the "low level" hardware driver.
 * @author John Sirach
 */
public abstract class Peripheral {

    public enum SubSystem {
        UNKNOWN("Unknown"),
        SERVER("Server"),
        I2C("I2C"),
        PLUGIN("Plugin"),
        INTERNAL("Internal"),
        SERIAL("Serial"),
        HID("USB HID"),
        BLUETOOTH("Bluetooth");

        private final String fieldDescription;

        private SubSystem(String value) {
            fieldDescription = value;
        }

        public String getDescription() {
            return fieldDescription;
        }
    }
    
    private String peripheralInternalName;
    private String peripheralFriendlyName;
    private String peripheralVendorId;
    private String peripheralDeviceId;
    private String peripheralKey;
    private String peripheralPort;
    private String peripheralSerial = "NONE";
    private SubSystem peripheralSubSystem = SubSystem.UNKNOWN;
    private String lastKnownError;
    
    PeripheralHardwareDriver.PeripheralVersion softwareDriverId = null;
    
    public final static String TYPE_USB    = "TYPE_USB";
    public final static String TYPE_I2C    = "TYPE_I2C";
    public final static String TYPE_SERIAL = "TYPE_SERIAL";
    public final static String TYPE_SERVER = "TYPE_SERVER";
    public final static String TYPE_PLUGIN = "TYPE_PLUGIN";
    
    static Logger LOG = LogManager.getLogger(Peripheral.class);
    
    String TYPE = "UNKNOWN";
    
    PeripheralHardwareDriverInterface peripheralDriver;
    PeripheralSoftwareDriverInterface peripheralSoftware;

    /**
     * Initializes the peripheral and sets the peripheral type.
     * The currently supported peripheral types are USB and I2C. The server device is used internally
     * @param peripheralType
     * @throws PeripheralHardwareException 
     */
    public Peripheral(String peripheralType) throws PeripheralHardwareException {
        if(peripheralType.equals(TYPE_USB)||peripheralType.equals(TYPE_I2C)||peripheralType.equals(TYPE_SERIAL)||peripheralType.equals(TYPE_SERVER)||peripheralType.equals(TYPE_PLUGIN)) {
            TYPE = peripheralType;
            LOG.debug("New peripheral: {}", peripheralType);
        } else {
            throw new PeripheralHardwareException("Peripheral type " + peripheralType + " is unsupported");
        }
    }
    
    /**
     * Sets an peripheral error.
     * @param error 
     */
    public final void error(String error){
        lastKnownError = error;
    }
    
    /**
     * Returns the peripheral error.
     * @return 
     */
    public final String getError(){
        return lastKnownError;
    }
    
    /**
     * Set's the peripheral's subsystem.
     * The subsystem is used to determine if the peripheral is of a supported type.
     * @param subSystem 
     */
    public final void setSubSystem(SubSystem subSystem){
        this.peripheralSubSystem = subSystem;
    }
    
    /**
     * Returns the registered subsystem.
     * The sub system is needed to determine if it is a supported interface type.
     * @return 
     */
    public final SubSystem getSubSystem(){
        return this.peripheralSubSystem;
    }
    
    /**
     * Set's a peripheral serial number
     * @param serial 
     */
    public final void setSerial(String serial){
        this.peripheralSerial = serial;
    }
    
    /**
     * Returns a peripherals serial number.
     * @return 
     */
    public final String getSerial(){
        return this.peripheralSerial;
    }
    
    /**
     * Adds the peripheral hardware driver.
     * The driver added is the driver for communicating over the device port, and not the software running on the device.
     * @param peripheralHardwareDriver 
     */
    public final void setPeripheralHardwareDriver(PeripheralHardwareDriverInterface peripheralHardwareDriver){
        peripheralDriver = peripheralHardwareDriver;
    }

    /**
     * Returns the hardware driver belonging to this peripheral
     * @return
     * @throws PeripheralHardwareException 
     */
    public final PeripheralHardwareDriverInterface getPeripheralHardwareDriver() throws PeripheralHardwareException {
        if(peripheralDriver!=null){
            return peripheralDriver;
        } else {
            throw new PeripheralHardwareException("There is no hardware driver present");
        }
    }
    
    /**
     * Returns the identifier of the software on the peripheral.
     * When an hardware driver does not support this it must throw an UnsupportedOperationException.
     * @return The identification of the software running on the peripheral if supported.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     * @throws UnsupportedOperationException 
     */
    public PeripheralHardwareDriver.PeripheralVersion getPeripheralSoftwareId() throws PeripheralHardwareException,UnsupportedOperationException {
        if(softwareDriverId!=null){
            return softwareDriverId;
        } else {
            if(peripheralDriver!=null){
                softwareDriverId = peripheralDriver.getSoftwareId();
                return softwareDriverId;
            } else {
                throw new PeripheralHardwareException("There is no peripheral software driver present");
            }
        }
    }
    
    /**
     * Set a software driver id, only valid when there is no driver started.
     * @param peripheralSoftwareDriverId
     * @param peripheralSoftwareVersion
     * @return true when new id can be and is attached
     */
    public final boolean setSoftwareDriverId(String peripheralSoftwareDriverId, String peripheralSoftwareVersion){
        if(peripheralSoftware==null){
            softwareDriverId = peripheralDriver.setCustomSoftwareId(peripheralSoftwareDriverId, peripheralSoftwareVersion);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Adds the peripheral software driver to the peripheral
     * @param peripheralSoftwareDriver 
     */
    public final void addPeripheralSoftwareDriver(PeripheralSoftwareDriverInterface peripheralSoftwareDriver){
        peripheralSoftware = peripheralSoftwareDriver;
    }
    
    /**
     * Gets the software driver for this peripheral.
     * @return the software driver instance.
     */
    public final PeripheralSoftwareDriverInterface getSoftwareDriver(){
        return peripheralSoftware;
    }
    
    /**
     * Stops the hardware driver.
     * This stops the hardware driver. Be sure to close ports!
     */
    public final void stopHardwareDriver(){
        if(peripheralDriver!=null){
            peripheralDriver.stopHardwareDriver();
        }
    }
    
    /**
     * Stops the hardware driver.
     * This stops the hardware driver. Be sure to close ports!
     */
    public final void releaseHardwareDriver(){
        if(peripheralDriver!=null){
            peripheralDriver.releaseDriver();
        }
    }
    
    /**
     * Starts the hardware driver.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public final void startHardwareDriver() throws PeripheralHardwareException {
        try {
            softwareDriverId = getPeripheralSoftwareId();
        } catch (PeripheralHardwareException ex) {
            LOG.error("Problem getting peripheral software id: {}", ex.getMessage());
        } catch (UnsupportedOperationException ex){
            LOG.error("Retrieving the software id on the peripheral is unsupported: {}", ex.getMessage());
        }
        if(peripheralDriver!=null){
            peripheralDriver.startHardwareDriver();
        }
    }

    /**
     * Hardware driver initialization.
     * @throws PeripheralHardwareException When initialization fails.
     * @throws UnsupportedOperationException When initialization is not needed.
     */
    public final void initHardwareDriver() throws PeripheralHardwareException,UnsupportedOperationException {
        if(peripheralDriver!=null){
            peripheralDriver.initDriver();
        } else {
            throw new PeripheralHardwareException("There is no hardware driver present");
        }
    }
    
    /**
     * Starts the peripheral software driver.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public final void startSoftwareDriver() throws PeripheralHardwareException {
        if(peripheralDriver!=null){
            peripheralDriver.setPeripheralEventListener(peripheralSoftware);
            peripheralSoftware.setPeripheralEventListener(peripheralDriver);
            peripheralSoftware.startDriver();
        } else {
            throw new PeripheralHardwareException("There is no peripheral driver");
        }
    }
    
    /**
     * Stops the peripheral software driver.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public final void stopSoftwareDriver() throws PeripheralHardwareException {
        if(peripheralDriver!=null){
            peripheralDriver.removePeripheralEventListener(peripheralSoftware);
            peripheralSoftware.removePeripheralEventListener(peripheralDriver);
            peripheralSoftware.stopDriver();
        } else {
            throw new PeripheralHardwareException("There is no peripheral driver");
        }
    }
    
    /**
     * Removes the link.
     * @throws PeripheralHardwareException 
     */
    public void removeSoftwareDriver() throws PeripheralHardwareException {
        if(peripheralSoftware == null){
            throw new PeripheralHardwareException("Driver already removed");
        } else {
            peripheralSoftware = null;
            softwareDriverId   = null;
        }
    }
    
    /**
     * Sets the peripheral internal used name for identification.
     * @param internalName 
     */
    public final void setInternalName(String internalName){
        peripheralInternalName = internalName;
    }
    
    /**
     * Sets the peripheral vendor.
     * @param vendorId 
     */
    public final void setVendorId(String vendorId){
        peripheralVendorId = vendorId;
    }
    
    /**
     * Sets the peripheral product id as set by the vendor
     * @param productId 
     */
    public final void setDeviceId(String productId){
        peripheralDeviceId = productId;
    }
    
    /**
     * Sets the device key, is used for internal reference.
     * @param key 
     */
    public final void setDeviceKey(String key){
        peripheralKey = key;
    }
    
    /**
     * Sets the device port.
     * This can be com[1-9]+ or an i2c port.
     * @param port 
     */
    public final void setDevicePort(String port){
        peripheralPort = port;
    }
    
    /**
     * Sets the name we all understand.
     * @param friendlyName 
     */
    public final void setFriendlyName(String friendlyName){
        peripheralFriendlyName = friendlyName;
    }
    
    /**
     * Retrieves the understandable name
     * @return The peripheral friendly name
     */
    public final String getFriendlyName(){
        return peripheralFriendlyName;
    }
    
    /**
     * Retrieves the internally used name.
     * @return The peripheral internally used name (PiDome used) 
     */
    public final String getInternalName(){
        return peripheralFriendlyName;
    }
    
    /**
     * Retrieves the vendor id.
     * @return The vendor id.
     */
    public final String getVendorId(){
        return peripheralVendorId;
    }

    /**
     * Retrieves the device id as set by the vendor.
     * @return The device id.
     */
    public final String getDeviceId(){
        return peripheralDeviceId;
    }
    
    /**
     * Retrieves the internally used device key.
     * @return internally used device key, must be unique
     */
    public final String getDeviceKey(){
        return peripheralKey;
    }
    
    /**
     * Retrieves the device port.
     * @see #setDevicePort(java.lang.String) 
     * @return The port set earlier
     */
    public final String getDevicePort(){
        return peripheralPort;
    }
    
    /**
     * Retrieves the device type.
     * @return Returns the device type (USB/I2C)
     */
    public final String getDeviceType(){
        return TYPE;
    }
    
}
