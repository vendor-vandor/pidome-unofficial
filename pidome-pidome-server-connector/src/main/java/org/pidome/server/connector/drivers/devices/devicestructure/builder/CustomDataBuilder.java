/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure.builder;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public class CustomDataBuilder {
    
    Map<String,Object> data = new HashMap<>();
    
    public CustomDataBuilder(){}
    
    public void setStringValue(String name, String value){
        data.put(name, value);
    }

    public void setIntValue(String name, int value){
        data.put(name, value);
    }
    
    public void setBoolValue(String name, boolean value){
        data.put(name, value);
    }
    
    public void setFloatValue(String name, float value){
        data.put(name, value);
    }
    
    protected final Map<String,Object> getData(){
        return data;
    }
    
}