/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.peripherals.software;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A list of all discovered devices.
 * @author John
 */
public class DiscoveredItemsCollection {
    
    static Logger LOG = LogManager.getLogger(DiscoveredItemsCollection.class);
    
    private static final List<DiscoveredDeviceListener> _listeners = new ArrayList<>();    
    
    /**
     * List of found devices.
     */
    private static final Map<DeviceDiscoveryBaseInterface,List<DiscoveredDevice>> deviceList = new HashMap<>();
    /**
     * List of drivers enabled.
     */
    private static final List<DeviceDiscoveryBaseInterface> _enabledDrivers = new ArrayList<>();
    
    /**
     * Adds an discovered device listener.
     * @param listener 
     */
    public static void addDiscoveredDeviceListener(DiscoveredDeviceListener listener){
        if(!_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    /**
     * Removes an discovered device listener.
     * @param listener 
     */
    public static void removeDiscoveredDeviceListener(DiscoveredDeviceListener listener){
        if(_listeners.contains(listener)){
            _listeners.remove(listener);
        }
    }
    
    /**
     * Returns a list of found devices.
     * @param driver
     * @return
     * @throws DiscoveredDeviceNotFoundException 
     */
    public static List<DiscoveredDevice> getDiscoveredDevices(DeviceDiscoveryBaseInterface driver) throws DiscoveredDeviceNotFoundException {
        if(deviceList.containsKey(driver)){
            return deviceList.get(driver);
        }
        throw new DiscoveredDeviceNotFoundException("Given driver has no discovered devices");
    }
    
    /**
     * Enables discovery for a driver.
     * @param driver 
     * @param period 
     */
    protected static void enableDiscovery(DeviceDiscoveryBaseInterface driver, int period){
        if(!_enabledDrivers.contains(driver)){
            _enabledDrivers.add(driver);
        }
        LOG.info("Enabled discovery (scan(-3)/one shot(-2)/timed(>0)/indefinitely(-1) mode) for {} with a period of {} minutes", driver.getFriendlyName(), period);
        driver.discoveryEnabled();
        broadcastDiscoveryStatus(true, driver, period);
    }
    
    /**
     * Disables driver discovery.
     * @param driver 
     */
    protected static void disableDiscovery(DeviceDiscoveryBaseInterface driver){
        if(_enabledDrivers.contains(driver)){
            _enabledDrivers.remove(driver);
        }
        LOG.info("Discovery mode for {} is disabled", driver.getFriendlyName());
        driver.discoveryDisabled();
        broadcastDiscoveryStatus(false, driver, 0);
    }
    
    /**
     * Check if discovery si enabled
     * @param driver 
     * @return true if discovery is enabled.
     */
    public static boolean discoveryEnabled(DeviceDiscoveryBaseInterface driver){
        return _enabledDrivers.contains(driver);
    }
    
    /**
     * Adds a discovered device to the list.
     * This function checks if a device with the given address already exists.
     * @param driver
     * @param device 
     * @throws org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryServiceException 
     */
    public static void addDiscoveredDevice(DeviceDiscoveryBaseInterface driver, DiscoveredDevice device) throws DeviceDiscoveryServiceException {
        if(!_enabledDrivers.contains(driver)){
            throw new DeviceDiscoveryServiceException("Enable discovery first");
        } else {
            if(!deviceList.containsKey(driver)){
                deviceList.put(driver, new ArrayList<DiscoveredDevice>(){{
                    add(device);
                }});
                LOG.info("Found device with address '{}' for driver '{}'", device.getAddress(), driver.getFriendlyName());
                broadcastDiscoveredDevice(driver,device);
            } else {
                boolean found = false;
                for(DiscoveredDevice discovered:deviceList.get(driver)){
                    if(discovered.getAddress().equals(device.getAddress())){
                        found = true;
                        LOG.info("Device with address '{}' and driver '{}' already present in discovery table", device.getAddress(), driver.getFriendlyName());
                        break;
                    }
                }
                if(!found){
                    deviceList.get(driver).add(device);
                    LOG.info("Found device with address '{}' for driver '{}'", device.getAddress(), driver.getFriendlyName());
                    broadcastDiscoveredDevice(driver,device);
                }
            }
            try {
                if(driver.getDiscoveryTime()==-2){
                    driver.disableDiscovery();
                }
            } catch (TimedDiscoveryException ex) {
                //// Not enabled.
            }
        }
    }
    
    /**
     * Returns a discovered with the given device address if found.
     * @param driver
     * @param address
     * @return 
     * @throws org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException 
     */
    public static DiscoveredDevice getDiscoveredDevice(DeviceDiscoveryBaseInterface driver, String address) throws DiscoveredDeviceNotFoundException {
        if(deviceList.containsKey(driver)){
            for(DiscoveredDevice discovered:deviceList.get(driver)){
                if(discovered.getAddress().equals(address)){
                    return discovered;
                }
            }
        }
        throw new DiscoveredDeviceNotFoundException("Assumed discovered device with address " + address + " not found for driver " + driver.getName());
    }
    
    /**
     * Returns if a specific device with the given address is available.
     * @param driver
     * @param address
     * @return 
     */
    public static boolean hasDiscoveredDevice(DeviceDiscoveryBaseInterface driver, String address){
        if(deviceList.containsKey(driver)){
            if (deviceList.get(driver).stream().anyMatch((discovered) -> (discovered.getAddress().equals(address)))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes a found device based on it's address.
     * @param driver
     * @param address 
     */
    public static void removeDiscoveredDevice(DeviceDiscoveryBaseInterface driver, String address){
        if(deviceList.containsKey(driver)){
            DiscoveredDevice found = null;
            for(DiscoveredDevice discovered:deviceList.get(driver)){
                if(discovered.getAddress().equals(address)){
                    found = discovered;
                    break;
                }
            }
            if(found!=null){
                deviceList.get(driver).remove(found);
            }
        }
    }
    
    /**
     * Removes all found devices.
     * @param driver 
     */
    public static void removeAllFoundDevices(DeviceDiscoveryBaseInterface driver){
        if(_enabledDrivers.contains(driver)){
            _enabledDrivers.remove(driver);
        }
        if(deviceList.containsKey(driver)){
            deviceList.get(driver).clear();
            deviceList.remove(driver);
        }
    }
    
    /**
     * Broadcasts a newly found device to the listeners.
     * PPassing the driver is temporary.
     * @param device 
     */
    private static void broadcastDiscoveredDevice(DeviceDiscoveryBaseInterface driver,DiscoveredDevice device){
        Runnable run = () -> {
            Iterator<DiscoveredDeviceListener> listeners = _listeners.iterator();
            while(listeners.hasNext()){
                listeners.next().handleNewDiscoveredDevice(driver, device);
            }
        };
        run.run();
    }
    
    /**
     * Broadcasts a new discovery status.
     * PPassing the driver is temporary.
     * @param device 
     */
    private static void broadcastDiscoveryStatus(boolean status, DeviceDiscoveryBaseInterface driver, int period){
        Runnable run = () -> {
            Iterator<DiscoveredDeviceListener> listeners = _listeners.iterator();
            if(status){
                while(listeners.hasNext()){
                    listeners.next().deviceDiscoveryEnabled(driver, period);
                }
            } else {
                while(listeners.hasNext()){
                    listeners.next().deviceDiscoveryDisabled(driver);
                }
            }
        };
        run.run();
    }
    
    /**
     * Signal out devices scan is done.
     * Drivers and plugins implementing the scan interface must use this function to end scan state in the server.
     * @param driver 
     */
    public static void signalDevicesScanDone(DeviceDiscoveryScanInterface driver){
        disableDiscovery(driver);
    }
    
}
