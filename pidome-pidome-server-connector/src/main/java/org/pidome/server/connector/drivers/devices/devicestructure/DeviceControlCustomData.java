/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public final class DeviceControlCustomData {
 
    Map<String,Object> collection = new HashMap<>();
    
    public final void putString(String name, String value){
        collection.put(name, value);
    }
    
    public final void putNumber(String name, Number value){
        collection.put(name, value);
    }
    
    public final void putBoolean(String name, boolean value){
        collection.put(name, value);
    }
    
    public final String getString(String name, String defaultValue){
        if(collection.containsKey(name)){
            return (String)collection.get(name);
        } else {
            return defaultValue;
        }
    }
    
    public final Number getNumber(String name, float defaultValue) throws NumberFormatException {
        if(collection.containsKey(name)){
            return (Number)collection.get(name);
        } else {
            return defaultValue;
        }
    }
    
    public final boolean getBoolean(String name, boolean defaultValue){
        if(collection.containsKey(name)){
            return (boolean)collection.get(name);
        } else {
            return defaultValue;
        }
    }
    
}