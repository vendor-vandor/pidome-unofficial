/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.xml.sax.SAXException;

/**
 *
 * @author John
 */
public final class DeviceDataHistoricData {
    
    /**
     * The control this is for.
     */
    private final DeviceDataControl control;
    
    protected DeviceDataHistoricData(DeviceDataControl control){
        this.control = control;
    }
    
    public final Map<Date,Double> update(){
        Map<Date,Double> dataMap = new HashMap<>();
        Map<String,Object> getData = new HashMap<>();
        getData.put("id", control.getControlGroup().getDevice().getDeviceId());
        getData.put("group", control.getControlGroup().getGroupId());
        getData.put("control", control.getControlId());
        getData.put("range", new ArrayList<String>(){{ add("hour"); }});
        
        try {
            
            PCCEntityDataHandler handler = this.control.getControlGroup().getDevice().getConnection().getJsonHTTPRPC("GraphService.getDeviceGraph", getData, "GraphService.getDeviceGraph");
            
            if(handler.getParameters().containsKey("hour")){
                List<Map<String,Object>> dataSet = (List<Map<String,Object>>)handler.getParameters().get("hour");
                for(Map<String,Object> data:dataSet){
                    dataMap.put(new Date(((Number)data.get("key")).longValue()), ((Number)data.get("value")).doubleValue());
                }
            }

        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(DeviceDataHistoricData.class.getName()).log(Level.SEVERE, "Data handling error", ex);
        }
        return dataMap;
    }
    
}