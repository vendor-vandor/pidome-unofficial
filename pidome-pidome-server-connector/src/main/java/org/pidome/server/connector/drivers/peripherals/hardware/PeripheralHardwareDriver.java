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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 *
 * @author John Sirach
 */
public abstract class PeripheralHardwareDriver {
    
    private boolean isInReadTimeout = false;
    
    private final String driverName = this.getClass().getName();
    
    static Logger LOG = LogManager.getLogger(PeripheralHardwareDriver.class);
    
    private final Map<String,PeripheralOption> peripheralOptions = new HashMap<>();
    
    private final PeripheralVersion peripheralVersion = new PeripheralVersion();
    
    private String namedId;
    
    private boolean active = false;
    
    private String friendlyName = "";
    
    /** 
     * The software driver for this device.
     */
    private PeripheralSoftwareDriverInterface peripheralSoftwareDriver;
    
    public PeripheralHardwareDriver() throws PeripheralHardwareException {}
    
    private Peripheral peripheral;
    
    public final void setFriendlyName(String friendlyName){
        this.friendlyName = friendlyName;
    }
    
    public final void setPeripheral(Peripheral peripheral){
        this.peripheral = peripheral;
    }
    
    /**
     * Returns the vendor id
     * @return 
     */
    public final String getVendorID(){
        return this.peripheral.getVendorId();
    }
    
    /**
     * Returns the product id
     * @return 
     */
    public final String getProductID(){
        return this.peripheral.getDeviceId();
    }
    
    /**
     * Adds a peripheral option wich can be retrieved when a peripheral waits for input before further initialization
     * @param id
     * @param option 
     */
    public final void addPeripheralOption(String id, PeripheralOption option){
        peripheralOptions.put(id, option);
    }
    
    /**
     * Checks if there are options set.
     * @return 
     */
    public final boolean hasPeripheralOptions(){
        return !peripheralOptions.isEmpty();
    }
    
    /**
     * retrieve a single option.
     * @param optionId
     * @return 
     */
    public final PeripheralOption getOption(String optionId){
        if(peripheralOptions.containsKey(optionId)){
            return peripheralOptions.get(optionId);
        } else {
            return null;
        }
    }
    
    public final void setNamedId(String namedId){
        this.namedId = namedId;
    }
    
    public final String getNamedId(){
        return namedId;
    }
    
    /**
     * Get a list of available options.
     * @return 
     */
    public final Map<String, PeripheralOption> getPeripheralOptions(){
        return peripheralOptions;
    }
    
    /**
     * Sets the options which can be retrieved with getPeripheralOptions.
     * Internal options storage.
     * @param optionSet 
     */
    public final void setPeripheralOptions(Map<String,String> optionSet){
        for(Entry<String,String> option:optionSet.entrySet()){
            if(peripheralOptions.containsKey(option.getKey())){
                peripheralOptions.get(option.getKey()).setSelectedValue(option.getValue());
            }   
        }
        putPeripheralOptions(optionSet);
    }
    
    /**
     * Sets the options which can be retrieved with getPeripheralOptions.
     * This function should be used by the driver itself so it can sets the options needed.
     * @param optionSet 
     */
    public void putPeripheralOptions(Map<String,String> optionSet){}

    /**
     * Starts the hardware driver.
     * @throws PeripheralHardwareException 
     */
    public final void startHardwareDriver() throws PeripheralHardwareException {
        startDriver();
        active = true;
    }
    
    /**
     * Starts the driver, throws exception when this does not work.
     * @throws PeripheralHardwareException 
     */
    public abstract void startDriver() throws PeripheralHardwareException;
    
    /**
     * Starts the hardware driver.
     * @throws PeripheralHardwareException 
     */
    public final void stopHardwareDriver() {
        active = false;
        stopDriver();
    }
    
    /**
     * Stops the peripheral driver
     */
    public abstract void stopDriver();
    
    /**
     * Returns if a driver is active.
     * Active means the driver is between start and stop, it does not mean it is 
     * loaded or not, but actively busy with any form of communication.
     * @return 
     */
    public final boolean isActive(){
        return this.active;
    }
    
    /**
     * Reads the peripheral port, or implements function to read the port so it can return a string.
     * @return The string read from the device
     * @throws IOException When readPort fails a closePort is called and the driver is stopped.
     */
    public abstract String readPort() throws IOException;
    
    /**
     * Prepares any environment vars needed for peripheral drivers to initialize.
     * For example set the an extra class path to the needed native libraries.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public final void prepare() throws PeripheralHardwareException {
        try {
            String path = Paths.get(Peripheral.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
            path = path.substring(0, path.lastIndexOf(File.separator));
        } catch (URISyntaxException ex) {
            throw new PeripheralHardwareException("Could not determine native libary path(s): " + ex.getMessage());
        } catch (Exception ex) {
            throw new PeripheralHardwareException("Could not set native libary path(s): " + ex.getMessage());
        }
    }
    
    /**
     * Writes the data to the port, the function retrieves a string, it is up to the driver author to correct this
     * @param string The data to write
     * @throws IOException When there is a port error. An exception always calls the closePort function and then stops the driver.
     * @throws UnsupportedOperationException When this function is not overwritten.
     */
    public void writePort(String string) throws IOException,UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Writes the data to the port, the function retrieves a string, it is up to the driver author to correct this
     * @param data
     * @throws IOException When there is a port error. An exception always calls the closePort function and then stops the driver.
     * @throws UnsupportedOperationException When this function is not overwritten.
     */
    public void writePort(byte[] data) throws IOException,UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Closes the port to the peripheral
     */
    public abstract void releaseDriver();
    
    /**
     * Driver initialization when needed
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     * @throws UnsupportedOperationException When init is not supported
     */
    public void initDriver() throws PeripheralHardwareException,UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Gets the peripheral software id, if supported.
     * @return The peripheral software id
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     * @throws UnsupportedOperationException 
     */
    public PeripheralVersion getSoftwareId() throws PeripheralHardwareException,UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Retrieves the port for the peripheral
     * @return The port name as used by the hardware
     */
    public final String getPort(){
        return this.peripheral.getDevicePort();
    }
    
    /**
     * Returns the Full Qualified Name of the driver.
     * @return The name
     */
    public final String getFQNName(){
        return driverName;
    }
    
    /**
     * Returns only the class name.
     * @return the class name
     */
    public final String getName(){
         return driverName.substring(driverName.lastIndexOf(".")+1);
    }
    
    /**
     * Adds peripheral event listener.
     * @param l The peripheral software driver.
     */
    public synchronized void setPeripheralEventListener( PeripheralSoftwareDriverInterface l ) {
        LOG.debug("Set peripheral listener: {}", l.getClass().getName());
        peripheralSoftwareDriver = l;
    }

    /**
     * Removes peripheral event listener.
     * @param l The peripheral software driver.
     */
    public synchronized void removePeripheralEventListener( PeripheralSoftwareDriverInterface l ) {
        LOG.debug("Removed peripheral listener: {}", l.getClass().getName());
        peripheralSoftwareDriver = null;
    }
    
    public String getFriendlyName(){
        return this.friendlyName;
    }
    
    /**
     * Dispatches event whom require data and an object
     * @param EVENTTYPE An event type which holds data
     * @param data The data belonging to the event type.
     * @param unknown
     */
    protected synchronized void dispatchData(String EVENTTYPE, ByteArrayOutputStream data) {
        if(data==null){
            LOG.error("Got null data from device, timeout?");
        } else {
            LOG.trace("Data received from device: {} byte(s)", data.size());
            PeripheralHardwareDataEvent oEvent = new PeripheralHardwareDataEvent(data, EVENTTYPE);
            oEvent.setStringData(data.toString());
            oEvent.setByteArrayStream(data);
            if(peripheralSoftwareDriver!=null) peripheralSoftwareDriver.driverBaseDataReceivedProxy(oEvent);
        }
    }
    
    /**
     * Dispatches event whom require data and an object
     * @param EVENTTYPE An event type which holds data
     * @param stringData
     * @param data The data belonging to the event type.
     */
    protected synchronized void dispatchData(String EVENTTYPE, String stringData, ByteArrayOutputStream data) {
        if(data==null){
            LOG.error("Got null data from device, timeout?");
        } else {
            LOG.trace("Data received from device: {} byte(s)", data.size());
            PeripheralHardwareDataEvent oEvent = new PeripheralHardwareDataEvent(data, EVENTTYPE);
            oEvent.setStringData(stringData);
            oEvent.setByteArrayStream(data);
            if(peripheralSoftwareDriver!=null) peripheralSoftwareDriver.driverBaseDataReceivedProxy(oEvent);
        }
    }
    
    /**
     * Convenience method which can be used to set a timeout on operations.
     * @param readTimeOutVar
     * @throws UnsupportedPeripheralActionException 
     */
    public void runMethodTimeout(final int readTimeOutVar) throws UnsupportedPeripheralActionException {
        Thread timeoutThread = new Thread() {
            @Override
            public void run() {
                try {
                    isInReadTimeout = true;
                    Thread.sleep(readTimeOutVar);
                    if(isInReadTimeout==true){
                        LOG.error("peripheral event timeout");
                        throw new RuntimeException();
                    }
                } catch (InterruptedException ex) {
                    /// if interrupted, well, serious shit, should never ever happen
                }
            }
        };
        timeoutThread.setUncaughtExceptionHandler((Thread t, Throwable e) -> {
            
        });
        timeoutThread.start();
    }

    /**
     * Creates the version data based on a single string.
     * @param peripheralStringId version string like "hardwaredrivername_1.0.0"
     */
    public final void createPeripheralVersion(String peripheralStringId){
        if(peripheralStringId.length()>0 && peripheralStringId.contains("_")){
            peripheralVersion.create(peripheralStringId.substring(0, peripheralStringId.lastIndexOf("_")), peripheralStringId.substring(peripheralStringId.lastIndexOf("_")+1));
        }
    }

    /**
     * Returns the current hardware version.
     * @return 
     */
    public final PeripheralVersion getVersion(){
        return peripheralVersion;
    }
    
    /**
     * Sets a custom software id used on a hardware device.
     * @param id
     * @param version
     * @return 
     */
    public final PeripheralVersion setCustomSoftwareId(String id, String version){
        peripheralVersion.create(id, version);
        return peripheralVersion;
    }
    
    /**
     * Peripheral hardware options.
     */
    public final class PeripheralOption {
        
        public final static String OPTION_SELECT = "OPTION_SELECT";
        
        Map<Integer,Map<String,Object>> optionList;
        
        String optionType = "none";
        
        String optionName;
        
        String selectedValue = "";
        
        /**
         * Constructor setting initial option type data.
         * @param optionName
         * @param optionType 
         */
        public PeripheralOption(String optionName, String optionType){
            this.optionType = optionType;
            this.optionName = optionName;
        }
        
        /**
         * Returns the selected value.
         * @return 
         */
        public final String getSelectedValue(){
            return selectedValue;
        }
        
        /**
         * Returns the selected value.
         * @param value
         */
        public final void setSelectedValue(String value){
            selectedValue = value;
        }
        
        /**
         * Returns the option type.
         * @return 
         */
        public final String getOptionType(){
            return optionType;
        }
        
        /**
         * Returns the option name.
         * @return 
         */
        public final String getOptionName(){
            return optionName;
        }
        
        /**
         * Adds a select option.
         * @param id
         * @param description
         * @param value 
         */
        public final void addSelectOption(int id, String description, Object value){
            if (optionList == null) {
                optionList = new TreeMap<>();
            }
            Map<String,Object> newOption = new HashMap<>();
            newOption.put("name", description);
            newOption.put("value", value);
            optionList.put(id, newOption);
        }
        
        /**
         * Returns a list of possible options.
         * @return 
         */
        public final Map<Integer,Map<String,Object>> getSelectOptions(){
            if(optionList!=null){
                return optionList;
            } else {
                return null;
            }
        }
        
        /**
         * Returns the value set for an option.
         * @param id
         * @return 
         */
        public final Object getSelectedOptionValue(int id){
            if(!optionList.isEmpty() && optionList.containsKey(id)){
                return optionList.get(id).get("value");
            } else {
                return null;
            }
        }
    }
    
    /**
     * Peripheral hardware version.
     */
    public class PeripheralVersion {
        
        String id = "";
        String version = "0";
        
        /**
         * Private constructor.
         */
        private PeripheralVersion(){}
        
        /**
         * Creates the version data.
         * @param peripheralId
         * @param version 
         */
        private void create(String peripheralId, String version){
            this.id = peripheralId;
            this.version = version;            
        }
        
        /**
         * Returns the hardware identifier.
         * @return 
         */
        public final String getId(){
            return id;
        }
        
        /**
         * Returns the hardware version number.
         * @return 
         */
        public final String getVersion(){
            return version;
        }
        
    }
    
}