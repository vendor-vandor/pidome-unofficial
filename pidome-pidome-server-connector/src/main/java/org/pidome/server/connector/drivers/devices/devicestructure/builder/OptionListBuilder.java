/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John
 */
public final class OptionListBuilder {
    
    List<Map<String,Object>> optionsList = new ArrayList<>();
    
    public OptionListBuilder(){}
    
    public final void addOptionItem(String label, String value){
        Map<String,Object>optionType = new HashMap<>();
        optionType.put("label", label);
        optionType.put("value", value);
        optionsList.add(optionType);
    }
    
    protected final List<Map<String,Object>> getData(){
        return this.optionsList;
    }
    
}
