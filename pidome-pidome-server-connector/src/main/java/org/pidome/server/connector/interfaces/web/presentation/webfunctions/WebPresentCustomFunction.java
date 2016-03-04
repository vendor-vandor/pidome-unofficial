/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation.webfunctions;

import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentation;

/**
 *
 * @author John
 */
public class WebPresentCustomFunction extends WebPresentation {

    Map<String,Object> parameters = new HashMap<>();
    String label = "";
    
    public WebPresentCustomFunction(String label) {
        super(TYPE.CUSTOM_FUNCTION, label);
        parameters.put("function_id", "customFunction");
        parameters.put("function_label", label);
    }

    public void setIdentifier(String identifier){
        parameters.put("identifier", identifier);
    }
    
    /**
     * Adds a parameter map of values that can be reported back.
     * @param params 
     */
    public void setParametersMap(Map<String,Object> params){
        for(Map.Entry<String,Object> entry:params.entrySet()){
            addParameter(entry.getKey(), entry.getValue());
        }
        this.setPresentationValue(parameters);
    }
    
    public void addParameter(String name, Object value){
        if(!name.startsWith("function_")){
            parameters.put(name, value);
        }
    }
    
    @Override
    public Object getPresentationValue(){
        if(super.getPresentationValue()==null){
            return parameters;
        } else {
            return super.getPresentationValue();
        }
    }
    
    @Override
    public void setValue(Object value) {}
    
}
