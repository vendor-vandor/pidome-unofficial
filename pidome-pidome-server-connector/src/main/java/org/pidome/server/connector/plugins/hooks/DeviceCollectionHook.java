/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class DeviceCollectionHook {
    
    static Map<DeviceCollectionListener,ArrayList<Integer>> hookList = new ConcurrentHashMap<>();
    
    static Logger LOG = LogManager.getLogger(DeviceCollectionHook.class);
    
    /**
     * So plugins can listen to device values.
     * @param plugin
     * @param driverId
     */
    public synchronized static void addDeviceCollection(DeviceCollectionListener plugin, int driverId){
        LOG.debug("Adding driver {} to {}", driverId, plugin.getClass().getName());
        if(hookList.containsKey(plugin)){
            if(!hookList.get(plugin).contains(driverId)){
                hookList.get(plugin).add(driverId);
            }
        } else {
            ArrayList<Integer> primaryHookSet = new ArrayList<>();
            primaryHookSet.add(driverId);
            hookList.put(plugin, primaryHookSet);
        }
        LOG.debug("Full hook list after adding: {}", hookList);
    }
    
    /**
     * Handles device data and delivers them to plugins.
     * @param driverId
     * @param deviceId
     * @param deviceCommand
     * @param deviceValue 
     */
    public static void handleDeviceCollectionValue(final int driverId, final int deviceId, final String deviceCommand, final Object deviceValue){
        handleDeviceCollectionValue(driverId, deviceId, deviceCommand, deviceValue, true);
    }
    
    /**
     * Handles device data and delivers them to plugins.
     * When user action is set, and is set to false, a plugin has to decide if it should handle the request as it is created by an internal process.
     * This is for example for important use with MQTT. If false an MQTT client should NOT publish the device data as it is already updated in it's receiving process. 
     * If an MQTT client would re plublish what it has received you would create an infinite loop with the remote broker.
     * @param driverId
     * @param deviceId
     * @param deviceCommand
     * @param deviceValue 
     * @param isUserAction 
     */
    public static void handleDeviceCollectionValue(final int driverId, final int deviceId, final String deviceCommand, final Object deviceValue, final boolean isUserAction){
        LOG.debug("Handling for device {} with {} containing {}", deviceId, deviceCommand, deviceValue);
        Runnable runValue = () -> {
            for(DeviceCollectionListener plugin:hookList.keySet()){
                if(hookList.get(plugin).contains(driverId)){
                    LOG.trace("Passing value {}, from driver {} and device {} with command {} to {}",driverId,deviceValue,deviceId,deviceCommand,plugin.getClass().getName());
                    plugin.handleDeviceCollectionValue(deviceId, deviceCommand, deviceValue, isUserAction);
                }
            }
        };
        runValue.run();
    }
    
    /**
     * Removes a full plugin hook.
     * @param plugin 
     */
    public static void remove(DeviceCollectionListener plugin){
        LOG.debug("Removing plugin {} from hook list", plugin.getClass().getName());
        if(hookList.containsKey(plugin)) hookList.remove(plugin);
    }

    /**
     * Removes a driver from a plugin hook.
     * @param plugin
     * @param driverId
     */
    public static void remove(DeviceCollectionListener plugin, int driverId){
        if (hookList.containsKey(plugin) && hookList.get(plugin).contains(driverId)) {
            LOG.debug("Removing driver id {} hook from {}", driverId, plugin.getClass().getName());
            hookList.get(plugin).remove(driverId);
        }
    }
    
}