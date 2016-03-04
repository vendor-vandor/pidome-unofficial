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
public final class ToggleDataBuilder {
 
    private final Map<String,Map<String,Object>> yesnoMap = new HashMap<>();
    
    public ToggleDataBuilder(){}
    
    public final void setOnData(String label, String value){
        Map<String,Object> yesMap = new HashMap<>();
        yesMap.put("label", label);
        yesMap.put("value", value);
        yesnoMap.put("on", yesMap);
    }
    
    public final void setOffData(String label, String value){
        Map<String,Object>  noMap = new HashMap<>();
        noMap.put("label", label);
        noMap.put("value", value);
        yesnoMap.put("off", noMap);

    }
    
    protected final Map<String,Map<String,Object>> getData(){
        return this.yesnoMap;
    }
    
}