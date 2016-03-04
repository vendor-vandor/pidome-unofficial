/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.devices;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public class DeviceStructureStore {
    
    private static final Map<Integer,Map<String,Object>> structStore = new HashMap<>();
    
    protected static void storeStruct(int installedId, Map<String,Object> structSet){
        structStore.put(installedId, structSet);
    }
    
    protected static void removeFromStore(int installedId){
        structStore.remove(installedId);
    }
    
    protected static boolean hasDeviceStruct(int installedId){
        return structStore.containsKey(installedId);
    } 
    
    protected static Map<String,Object> getDeviceStruct(int installedId){
        return structStore.get(installedId);
    }
    
}