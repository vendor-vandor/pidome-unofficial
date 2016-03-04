/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.emulators;

import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationException;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.emulators.PeripheralPlugin;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.plugins.freeform.FreeformPlugin;

/**
 *
 * @author John
 */
public abstract class DevicePlugin extends FreeformPlugin implements HardwareInterfaceEmulator,PeripheralPlugin {

    Peripheral hardwareDevice;
    private PluginDeviceMutationInterface deviceServiceLink;
    
    Map<String,Integer> installedDevices = new HashMap<>();
    
    /**
     * @inheritDoc
     */
    @Override
    public abstract void setConfigurationValues(Map<String, String> configuration) throws WebConfigurationException;

    /**
     * @inheritDoc
     */
    @Override
    public abstract void startPlugin() throws PluginException;

    /**
     * @inheritDoc
     */
    @Override
    public abstract void stopPlugin();

    /**
     * @inheritDoc
     */
    @Override
    public abstract String getExpectedDriverId();
    
    /**
     * @inheritDoc
     */
    @Override
    public abstract String getExpectedDriverVersion();

    /**
     * @inheritDoc
     */
    @Override
    public abstract void handleDeviceData(Device device, String group, String set, byte[] data, boolean userIntent);

    /**
     * @inheritDoc
     */
    @Override
    public abstract void handleDeviceData(Device device, DeviceCommandRequest request);
    
    /**
     * @inheritDoc
     */
    @Override
    public final Peripheral getHardwareDevice(){
        return hardwareDevice;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final PeripheralSoftwareDriverInterface getSoftwareDriverLink(){
        return hardwareDevice.getSoftwareDriver();
    }
    /**
     * @inheritDoc
     */
    @Override
    public final void setHardwareDevice(Peripheral device){
        hardwareDevice = device;
    }
    
    /**
     * Returns the driver name used.
     * @return 
     */
    public final String getDriverName(){
        return hardwareDevice.getSoftwareDriver().getName();
    }
    
    /**
     * Deletes a device from the device listing based on the device id.
     * Only devices which belongs to the plugin &lt- peripheral software driver link
     * can be deleted
     * @param deviceId
     * @throws PluginDeviceMutationException 
     */
    public final void deleteDevice(int deviceId) throws PluginDeviceMutationException {
        if(deviceServiceLink!=null){
            int deleteDevice = 0;
            for(Device device:hardwareDevice.getSoftwareDriver().getRunningDevices()){
                if(device.getId()==deviceId) deleteDevice = device.getId();
            }
            if(deleteDevice!=0){
                deviceServiceLink.removeDeviceByPlugin(deleteDevice);
            } else {
                throw new PluginDeviceMutationException("Device id '"+deviceId+"' is not assigned to this plugin and not eligable for deletion.");
            }
        }
    }
    
    /**
     * indicator that a device is loaded and which one.
     * @param device 
     */
    @Override
    public void deviceLoaded(Device device){}
    
    /**
     * Gives the plugin the opportunity to create a device.
     * A plugin can only create device from which the driver path is known. Even
     * if there are multiple devices with the same driver path only the correct
     * device will be added due to the mapping of the driver.
     * @param deviceIdentifier
     * @param name
     * @param location
     * @param category
     * @throws PluginDeviceMutationException 
     */
    public final void saveDevice(String deviceIdentifier, String name, String location, int category) throws PluginDeviceMutationException {
        if(installedDevices.containsKey(deviceIdentifier)){
            deviceServiceLink.addDeviceByPlugin(installedDevices.get(deviceIdentifier), name, location, category);
        } else {
            throw new PluginDeviceMutationException("Device driver '"+deviceIdentifier+"' is not assigned to this plugin and thus can not be created.");
        }
    }
    
    /**
     * Creates a device from an existing installed device.
     * @param deviceId installed device id.
     * @param name Device name
     * @param address Device address
     * @param location
     * @param subCategory Device sub category (linked to main category). 
     * @throws org.pidome.server.connector.drivers.devices.PluginDeviceMutationException 
     */
    public final void createFromExistingDevice(int deviceId, String name, String address, int location, int subCategory) throws PluginDeviceMutationException {
        if(deviceServiceLink!=null){
            deviceServiceLink.addFromExistingDeviceByPlugin(deviceId, name, address, location, subCategory);
        } else {
            throw new PluginDeviceMutationException("There is no server link to create device.");
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void setDeviceServiceLink(PluginDeviceMutationInterface deviceServiceLink){
        this.deviceServiceLink = deviceServiceLink;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void removeDeviceServiceLink(){
        this.deviceServiceLink = null;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void assignDeviceInstalledIds(Map<String,Integer> installedDevices){
        this.installedDevices = installedDevices;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void removeAssignDeviceInstalledIds(){
        this.installedDevices = new HashMap<>();
    }
    
    /**
     * Enable discovery.
     * This function checks if the driver is eligable to do discovery first and fails gracefully without notice.
     * @param period 
     * @throws org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException 
     */
    public final void enableDiscovery(int period) throws TimedDiscoveryException {
        if (this instanceof DeviceDiscoveryInterface) {
            ((DeviceDiscoveryInterface)hardwareDevice.getSoftwareDriver()).enableDiscovery(period);
        }
    }
    
    /**
     * Returns true if discovery is enabled for this driver.
     * @return 
     */
    public final boolean discoveryIsEnabled(){
        return ((DeviceDiscoveryInterface)hardwareDevice.getSoftwareDriver()).discoveryIsEnabled();
    }
 

    /**
     * Returns the discovery time set.
     * @return
     * @throws TimedDiscoveryException 
     */
    public final int getDiscoveryTime() throws TimedDiscoveryException {
        if (this instanceof DeviceDiscoveryInterface) {
            return ((DeviceDiscoveryInterface)hardwareDevice.getSoftwareDriver()).getDiscoveryTime();
        } else {
            return 0;
        }
    }

    /**
     * Returns the amount of discovered devices.
     * @return 
     */
    public final int getDiscoveredAmount() {
        if (this instanceof DeviceDiscoveryInterface) {
            return ((DeviceDiscoveryInterface)hardwareDevice.getSoftwareDriver()).getDiscoveredAmount();
        } else {
            return 0;
        }
    }
    
    /**
     * Returns the amount of discovered devices.
     * @return 
     */
    public final void disableDiscovery() {
        if (this instanceof DeviceDiscoveryInterface) {
            try {
                ((DeviceDiscoveryInterface)hardwareDevice.getSoftwareDriver()).disableDiscovery();
            } catch (TimedDiscoveryException ex) {
                /// not enabled.
            }
        }
    }
    
}