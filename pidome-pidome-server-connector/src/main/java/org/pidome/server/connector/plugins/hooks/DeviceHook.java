/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.plugins.PluginBase;

/**
 *
 * @author John
 */
public class DeviceHook {
    
    static Map<DeviceHookListener,Map<Integer,ArrayList<String>>> hookList = new ConcurrentHashMap<>();
    
    static DeviceDeliverHookListener deliverListener;
    
    static Logger LOG = LogManager.getLogger(DeviceHook.class);
    
    /**
     * So plugins can listen to device values.
     * @param plugin
     * @param deviceId
     * @param deviceCommand 
     */
    public synchronized static void addDevice(DeviceHookListener plugin, int deviceId, String deviceCommand){
        LOG.debug("Adding device {} with {} to {}", deviceId, deviceCommand, plugin.getClass().getName());
        if(hookList.containsKey(plugin)){
            if(hookList.get(plugin).containsKey(deviceId)){
                if(!hookList.get(plugin).get(deviceId).contains(deviceCommand)){
                    hookList.get(plugin).get(deviceId).add(deviceCommand);
                }
            } else {
                ArrayList commandList = new ArrayList<>();
                commandList.add(deviceCommand);
                hookList.get(plugin).put(deviceId, commandList);
            }
        } else {
            ArrayList commandList = new ArrayList<>();
            commandList.add(deviceCommand);
            Map<Integer,ArrayList<String>> primaryHookSet = new HashMap<>();
            primaryHookSet.put(deviceId, commandList);
            hookList.put(plugin, primaryHookSet);
        }
        LOG.debug("Full hook list after: {}", hookList);
    }
    
    /**
     * So plugins can listen to all devices their values.
     * @param plugin
     */
    public synchronized static void addAllDevices(DeviceHookListener plugin){
        LOG.debug("Adding all devices to {}", plugin.getClass().getName());
        if(hookList.containsKey(plugin)){
            if(!hookList.get(plugin).containsKey(0)){
                hookList.get(plugin).put(0, new ArrayList<>());
            }
        } else {
            Map<Integer,ArrayList<String>> primaryHookSet = new HashMap<>();
            primaryHookSet.put(0, new ArrayList<>());
            hookList.put(plugin, primaryHookSet);
        }
        LOG.debug("Full hook list after: {}", hookList);
    }
    
    /**
     * Handles device data and delivers them to plugins.
     * @param device
     * @param group
     * @param control
     * @param deviceValue 
     * @param deviceControl 
     */
    public static void handleDeviceValue(final Device device, final String group, final String control, final DeviceControl deviceControl, final Object deviceValue){
        LOG.debug("Handling for device {} with group {}, control: {}, containing {}", device.getName(), group, control, deviceValue);
        Runnable runValue = () -> {
            for(DeviceHookListener plugin:hookList.keySet()){
                if(hookList.get(plugin).containsKey(0)){
                    plugin.handleDeviceData(device, group, control, deviceControl, deviceValue);
                } else if(hookList.get(plugin).containsKey(device.getId()) && hookList.get(plugin).get(device.getId()).contains(control)){
                    LOG.trace("Passing value {}, from device {} with command {} to {}",deviceValue,device.getName(),control,plugin.getClass().getName());
                    plugin.handleDeviceData(device, group, control, deviceControl, deviceValue);
                } else if(hookList.get(plugin).containsKey(device.getId()) && hookList.get(plugin).get(device.getId()).contains("")){
                    LOG.trace("Passing value {}, from device {} with command {} to {}",deviceValue,device.getName(),control,plugin.getClass().getName());
                    plugin.handleDeviceData(device, group, control, deviceControl, deviceValue);
                }
            }
        };
        runValue.run();
    }
    
    /**
     * Delivery instance for plugins to send data to devices.
     * Prerequisite is that a plugin must already have been connected to a device
     * for listening to device data. Even when it is not needed.
     * @param plugin
     * @param deviceId
     * @param group
     * @param set
     * @param deviceCommand 
     */
    public static void deliver(DeviceHookListener plugin, int deviceId, String group, String set, String deviceCommand){
        if(hookList.containsKey(plugin) && 
           hookList.get(plugin).containsKey(deviceId) && 
           hookList.get(plugin).get(deviceId).contains(set)){
            deliverListener.handleHookDeviceDeliverData((PluginBase)plugin, deviceId, group, set, deviceCommand);
        }
    }
    
    /**
     * Sets the device data deliver listener.
     * This should be the server.
     * @param listener 
     */
    public static void setDeliverListener(DeviceDeliverHookListener listener){
        deliverListener = listener;
    }
    
    /**
     * Removes the device data deliver listener.
     * This should be the server.
     * @param listener 
     */
    public static void unSetDeliverListener(DeviceDeliverHookListener listener){
        if(deliverListener == listener){
            deliverListener = null;
        }
    }
    
    /**
     * Removes a full plugin hook.
     * @param plugin 
     */
    public static void remove(DeviceHookListener plugin){
        LOG.debug("Removing plugin {} from hook list", plugin.getClass().getName());
        if(hookList.containsKey(plugin)) hookList.remove(plugin);
    }

    /**
     * Removes a device from a plugin hook.
     * @param plugin
     * @param deviceId 
     */
    public static void remove(DeviceHookListener plugin, int deviceId){
        if (hookList.containsKey(plugin) && hookList.get(plugin).containsKey(deviceId)) {
            LOG.debug("Removing deviceid {} hook from {}", deviceId, plugin.getClass().getName());
            hookList.get(plugin).remove(deviceId);
        }
    }
    
    /**
     * Removes a single device command from a plugin hook.
     * @param plugin
     * @param deviceId
     * @param deviceCommand 
     */
    public static void remove(DeviceHookListener plugin, int deviceId, String deviceCommand){
        if (hookList.containsKey(plugin) && hookList.get(plugin).containsKey(deviceId) && hookList.get(plugin).get(deviceId).contains(deviceCommand)) {
            LOG.debug("Removing hook {} with deviceid {} from {}", deviceCommand, deviceId, plugin.getClass().getName());
            hookList.get(plugin).get(deviceId).remove(deviceCommand);
        }
    }
}
