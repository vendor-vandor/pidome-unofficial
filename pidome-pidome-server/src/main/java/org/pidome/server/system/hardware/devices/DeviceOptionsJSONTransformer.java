/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.devices;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;

/**
 *
 * @author John
 */
public class DeviceOptionsJSONTransformer {
 
    private final String jsonSet;
    private Map<String,Object> obj = new HashMap<>();
    
    protected DeviceOptionsJSONTransformer(String jsonSet){
        this.jsonSet = jsonSet;
    }
    
    
    protected Map<String,Object> get(){
        try {
            JSONParser parser = new JSONParser();
            obj = (Map<String,Object>)parser.parse(jsonSet);
        } catch (ParseException ex) {
            /// Assume XML
            DeviceOptionsXMLTransformer tranform = new DeviceOptionsXMLTransformer(jsonSet);
            tranform.transform();
            try {
                obj = tranform.get();
            } catch (PidomeJSONRPCException ex1) {
                
            }
        }
        return obj;
    }
    
}