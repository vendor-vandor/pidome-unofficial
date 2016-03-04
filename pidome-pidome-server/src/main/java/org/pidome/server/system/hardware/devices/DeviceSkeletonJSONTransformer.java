/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.devices;

import java.util.Map;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;

/**
 *
 * @author John
 */
public class DeviceSkeletonJSONTransformer {
    
    private final String jsonSet;
    private Map<String,Object> obj;
    
    protected DeviceSkeletonJSONTransformer(String jsonSet){
        this.jsonSet = jsonSet;
    }
    
    protected void compose() throws DeviceSkeletonException {
        try {
            JSONParser parser = new JSONParser();
            obj = (Map<String,Object>)parser.parse(jsonSet);
        } catch (ParseException ex) {
            /// Assume XML
            DeviceSkeletonXMLTransformer tranform = new DeviceSkeletonXMLTransformer(jsonSet);
            tranform.transform();
            try {
                obj = tranform.get();
            } catch (PidomeJSONRPCException ex1) {
                throw new DeviceSkeletonException(ex1);
            }
        }
    }
    
    protected Map<String,Object> get(){
        return obj;
    }
    
}