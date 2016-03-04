/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.interfaces.web.presentation.webfunctions;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public class WebPresentCustomFunctionRequest {
    
    Map<String,Object> result = new HashMap<>();
    String identifier = "";
    
    public final void setIdentifier(String id){
        identifier = id;
    }    
    
    public final void setResultParams(Map<String,Object> result){
        this.result = result;
    }
    
    public final Map<String,Object> getCustomData(){
        return this.result;
    }
    
    public final String getIdentifier(){
        return identifier;
    }
    
}
