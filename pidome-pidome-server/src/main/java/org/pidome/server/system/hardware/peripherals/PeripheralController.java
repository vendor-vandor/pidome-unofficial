/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.peripherals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 * A peripheral holder class which contains both the peripheral software and hardware driver.
 * This class makes sure the software and hardware can communicate with eachother.
 * @author John
 */
public class PeripheralController {
 
    static Logger LOG = LogManager.getLogger(Peripherals.class);
    
    /**
     * The peripheral device as known in the server.
     */
    private final Peripheral peripheralDevice;
    
    /**
     * The peripheral hardware driver's version data.
     */
    PeripheralHardwareDriver.PeripheralVersion peripheralVersionId = null;
    
    /**
     * Settings used for resuming device initialization.
     */
    Map<String,Object> resumeSetttings = new HashMap<>();
    
    /**
     * Constructor.
     * @param peripheralDevice 
     */
    protected PeripheralController(Peripheral peripheralDevice){
        this.peripheralDevice = peripheralDevice;
    }
    
    /**
     * Stores the settings specific for this attached peripheral.
     */
    protected final void storeSettings(){
        if(peripheralDevice!=null){
            try {
                Map<String,Object> optionsNvp = new HashMap<>();
                if (getPeripheralHardwareDriver().hasPeripheralOptions()){
                    for(Map.Entry<String, PeripheralHardwareDriver.PeripheralOption> set:getPeripheralHardwareDriver().getPeripheralOptions().entrySet()){
                        optionsNvp.put(set.getKey(), set.getValue().getSelectedValue());
                    }
                }
                resumeSetttings.put("hardwarePort", peripheralDevice.getDevicePort());
                resumeSetttings.put("pid", peripheralDevice.getDeviceId());
                resumeSetttings.put("vid", peripheralDevice.getVendorId());
                resumeSetttings.put("serial", peripheralDevice.getSerial());
                resumeSetttings.put("swdriverid", peripheralVersionId.getId());
                resumeSetttings.put("swdriverversion", peripheralVersionId.getVersion());
                resumeSetttings.put("hwdriveroptions", optionsNvp);
                resumeSetttings.put("namedid", peripheralDevice.getPeripheralHardwareDriver().getNamedId());
                LOG.debug("Trying to save: {}", resumeSetttings);
                try {
                    File file = getPortSaveFile();
                    try(FileWriter writer = new FileWriter(file);
                        PrintWriter printer = new PrintWriter(writer);) {
                        printer.write(PidomeJSONRPCUtils.createNonRPCMethods(resumeSetttings));
                    } catch (IOException ex) {
                        LOG.error("Unable to write configuration: {}", ex.getMessage());
                    } catch (PidomeJSONRPCException ex) {
                        LOG.error("Unable to create JSON properties");
                    }
                } catch (ConfigPropertiesException ex) {
                    LOG.error("Problem getting hardware save configuration setting server.conf.hardware.usb: {}", ex.getMessage());
                }
            } catch (PeripheralHardwareException ex) {
                LOG.error("Problem getting peripheral hardware driver for setting data: {}", ex.getMessage());
            }
        }
    }

    /**
     * Check if the peripheral device is resumable.
     * Use this function before using getResumeSettings()
     * @return
     * @throws ConfigPropertiesException
     * @throws UnsupportedPeripheralActionException 
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException 
     */
    protected final boolean resumable() throws ConfigPropertiesException, UnsupportedPeripheralActionException, PeripheralHardwareException{
        resumeSetttings = prepareSettings();
        return true;
    }
    
    /**
     * Returns the resume settings.
     * First call resumable() to make these settings available.
     * @return 
     */
    protected final Map<String,Object> getResumeSettings(){
        return this.resumeSetttings;
    }
    
    /**
     * Prepare resume settings.
     * Use by resumable.
     * @return
     * @throws ConfigPropertiesException
     * @throws UnsupportedPeripheralActionException 
     */
    private Map<String,Object> prepareSettings() throws ConfigPropertiesException, UnsupportedPeripheralActionException, PeripheralHardwareException {
        File file = getPortSaveFile();
        if(file.exists()){
            try {
                List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
                if(lines.size()==1){
                    Map<String,Object> params = new PidomeJSONRPC(lines.get(0).trim(), false).getParsedObject();
                    if(((String)params.get("hardwarePort")).equals(peripheralDevice.getDevicePort()) && 
                       ((String)params.get("pid")).equals(peripheralDevice.getDeviceId()) && 
                       ((String)params.get("vid")).equals(peripheralDevice.getVendorId()) &&
                       ((String)params.get("serial")).equals(peripheralDevice.getSerial())){
                        String namedId = (String)params.get("namedid");
                        if((namedId==null || namedId.equals(""))){
                            file.delete();
                            throw new UnsupportedPeripheralActionException("Invalid configuration file, can not continue."); 
                        }
                        return params;
                    } else {
                        file.delete();
                        throw new UnsupportedPeripheralActionException("Saved settings are not the same of the device connected, removing config file."); 
                    }
                } else {
                    file.delete();
                    throw new UnsupportedPeripheralActionException("Incorrect content in hardware configuration file: "+file.getName()+", line numbers: " + lines.size());
                }
            } catch (IOException ex) {
                file.delete();
                LOG.error("Could not open hardware port settings file for {}: {}, {}",peripheralDevice.getFriendlyName(), file.getName(), ex.getMessage());
                throw new UnsupportedPeripheralActionException("Could not create file reference: " + ex.getMessage());
            } catch (PidomeJSONRPCException ex) {
                file.delete();
                LOG.error("Could not create option set for {} from {}: {}",peripheralDevice.getFriendlyName(), file.getName(), ex.getMessage());
                throw new UnsupportedPeripheralActionException("Could not create option set: " + ex.getMessage());
            } catch (Exception ex){
                file.delete();
                LOG.error("Unrecoverable settings file for peripheral {} from settings file {}: {}",peripheralDevice.getFriendlyName(), file.getName(), ex.getMessage(), ex);
                throw new UnsupportedPeripheralActionException("Error recovering peripheral settings: " + ex.getMessage());
            }
        } else {
            throw new PeripheralHardwareException("No custom configuration present for " + peripheralDevice.getFriendlyName());
        }
    }
    
    /**
     * Returns the file used to save settings.
     * @return
     * @throws ConfigPropertiesException 
     */
    protected File getPortSaveFile() throws ConfigPropertiesException {
        try {
            return new File(SystemConfig.getProperty("system", "server.conf.hardware.usb") + peripheralDevice.getDevicePort().substring(peripheralDevice.getDevicePort().lastIndexOf(File.separator)));
        } catch (Exception ex){
            throw new ConfigPropertiesException("Device '"+peripheralDevice.getFriendlyName()+"' on '"+peripheralDevice.getDevicePort()+"' has no settings file");
        }
    }
    
    /**
     * Starts the initialization of the peripheral hardware driver.
     * @param peripheralDevice
     * @throws PeripheralHardwareException 
     */
    final void startPeripheralInitialization() throws PeripheralHardwareException {
        this.getPeripheralHardwareDriver().setPeripheral(peripheralDevice);
        initHardwareDriver();
        startHardwareDriver();
    }
    
    /**
     * Returns the peripheral where is is all about.
     * @return 
     */
    public final Peripheral getPeripheral(){
        return this.peripheralDevice;
    }
    
    //// Peripheral hardware driver stuff.

    /**
     * Sets the hardware driver used for comunicating with the peripheral.
     * @param peripheralHardwareDriver 
     */
    public void setPeripheralHardwareDriver(PeripheralHardwareDriverInterface peripheralHardwareDriver) {
        this.peripheralDevice.setPeripheralHardwareDriver(peripheralHardwareDriver);
    }

    /**
     * Returns the hardware driver used to communicate wioth the peripheral.
     * @return
     * @throws PeripheralHardwareException 
     */
    public PeripheralHardwareDriverInterface getPeripheralHardwareDriver() throws PeripheralHardwareException {
        return this.peripheralDevice.getPeripheralHardwareDriver();
    }
    
    /**
     * Initializes the hardware peripheral driver.
     * @throws PeripheralHardwareException
     * @throws UnsupportedOperationException 
     */
    public void initHardwareDriver() throws PeripheralHardwareException, UnsupportedOperationException {
        this.peripheralDevice.getPeripheralHardwareDriver().initDriver();
    }
    
    /**
     * Starts the hardware peripheral driver.
     * @throws PeripheralHardwareException 
     */
    public void startHardwareDriver() throws PeripheralHardwareException {
        this.peripheralDevice.getPeripheralHardwareDriver().startHardwareDriver();
    }

    /**
     * Stops the hardware peripheral driver.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public void stopHardwareDriver() throws PeripheralHardwareException {
        this.peripheralDevice.getPeripheralHardwareDriver().stopHardwareDriver();
    }

    /**
     * Releases the hardware peripheral driver, detaches it from the peripheral.
     * This does NOT unset the peripheral hardware driver.
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException
     */
    public void releaseHardwareDriver() throws PeripheralHardwareException {
        this.peripheralDevice.releaseHardwareDriver();
    }
    
    /**
     * Set's the software driver id which is used to identify which software driver
     * should be used.
     * @param peripheralSoftwareDriverId
     * @param peripheralSoftwareVersion
     * @return 
     * @throws org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException 
     */
    public boolean setSoftwareDriverId(String peripheralSoftwareDriverId, String peripheralSoftwareVersion) throws PeripheralHardwareException {
        if(this.peripheralDevice.getSoftwareDriver()==null){
            peripheralVersionId = this.peripheralDevice.getPeripheralHardwareDriver().setCustomSoftwareId(peripheralSoftwareDriverId, peripheralSoftwareVersion);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Returns the software driver id.
     * This is used to determine which peripheral software driver ha sto be used.
     * @return
     * @throws PeripheralHardwareException
     * @throws UnsupportedOperationException 
     */
    public PeripheralHardwareDriver.PeripheralVersion getPeripheralSoftwareId() throws PeripheralHardwareException, UnsupportedOperationException {
        if(peripheralVersionId!=null){
            return peripheralVersionId;
        } else {
            if(this.peripheralDevice.getPeripheralHardwareDriver()!=null){
                peripheralVersionId = this.peripheralDevice.getPeripheralHardwareDriver().getSoftwareId();
                return peripheralVersionId;
            } else {
                throw new PeripheralHardwareException("There is no peripheral software driver present");
            }
        }
    }
    
    /// Peripheral software driver stuff.
    
    /**
     * Attaches the peripheral software driver.
     * @param peripheralSoftwareDriver 
     */
    public void addPeripheralSoftwareDriver(PeripheralSoftwareDriverInterface peripheralSoftwareDriver) {
        this.peripheralDevice.addPeripheralSoftwareDriver(peripheralSoftwareDriver);
    }

    /**
     * Returns the software driver.
     * @return 
     */
    public PeripheralSoftwareDriverInterface getSoftwareDriver() {
        return this.peripheralDevice.getSoftwareDriver();
    }

    /**
     * Starts the sofwtare driver
     * @throws PeripheralHardwareException When there is no hadrware driver. 
     */
    public void startSoftwareDriver() throws PeripheralHardwareException {
        if(this.peripheralDevice.getPeripheralHardwareDriver()!=null){
            this.peripheralDevice.getPeripheralHardwareDriver().setPeripheralEventListener(this.peripheralDevice.getSoftwareDriver());
            this.peripheralDevice.getSoftwareDriver().setPeripheralEventListener(this.peripheralDevice.getPeripheralHardwareDriver());
            this.peripheralDevice.getSoftwareDriver().startDriver();
        } else {
            throw new PeripheralHardwareException("There is no peripheral driver");
        }
    }

    /**
     * Stops the software driver.
     * @throws PeripheralHardwareException When there is no software driver registered.
     */
    public void stopSoftwareDriver() throws PeripheralHardwareException {
        if(this.peripheralDevice.getPeripheralHardwareDriver()!=null){
            this.peripheralDevice.getPeripheralHardwareDriver().removePeripheralEventListener(this.peripheralDevice.getSoftwareDriver());
            this.peripheralDevice.getSoftwareDriver().removePeripheralEventListener(this.peripheralDevice.getPeripheralHardwareDriver());
            this.peripheralDevice.getSoftwareDriver().stopDriver();
        } else {
            throw new PeripheralHardwareException("There is no peripheral driver");
        }
    }

    /**
     * Removes the software driver.
     * @throws PeripheralHardwareException When the driver is already removed. 
     */
    public void removeSoftwareDriver() throws PeripheralHardwareException {
        if(this.peripheralDevice.getSoftwareDriver() == null){
            throw new PeripheralHardwareException("Driver already removed");
        } else {
            this.peripheralDevice.removeSoftwareDriver();
            this.peripheralVersionId      = null;
        }
    }
    
}