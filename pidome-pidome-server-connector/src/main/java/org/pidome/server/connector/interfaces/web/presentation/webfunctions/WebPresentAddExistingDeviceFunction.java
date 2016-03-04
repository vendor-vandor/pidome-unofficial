/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation.webfunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author John
 */
public final class WebPresentAddExistingDeviceFunction extends WebPresentCustomFunction {
    
    Map<String,Object> parameters = new HashMap<>();
    String label = "";
    
    public WebPresentAddExistingDeviceFunction(String label){
        super(label);
        parameters.put("function_id", "addExistingNewDevice");
    }
    
    /**
     * Adds a parameter map of values that can be reported back.
     * @param params 
     */
    @Override
    public final void setParametersMap(Map<String,Object> params){
        for(Entry<String,Object> entry:params.entrySet()){
            addParameter(entry.getKey(), entry.getValue());
        }
        this.setPresentationValue(parameters);
    }
    
    public final void addParameter(String name, Object value){
        if(!name.equals("function_id") && 
           !name.equals("device_id") && 
           !name.equals("device_name") && 
           !name.equals("device_locationid") && 
           !name.equals("device_categoryid")){
            parameters.put(name, value);
        }
    }
    
}