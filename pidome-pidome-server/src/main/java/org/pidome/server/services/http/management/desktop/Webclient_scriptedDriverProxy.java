/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.management.desktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_scriptedDriverProxy extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_scriptedDriverProxy.class);
    
    boolean saveResult = false;
    String errorMessage = "";
    
    @Override
    public void collect(){
        if(!postDataMap.isEmpty()){
            if(postDataMap.containsKey("instanceFor") && postDataMap.containsKey("id") && postDataMap.containsKey("name") && postDataMap.containsKey("description") && postDataMap.containsKey("script")){
                try {
                    DeviceService.updateScriptedDriver(Integer.parseInt(postDataMap.get("instanceFor")), 
                                                       Integer.parseInt(postDataMap.get("id")), 
                                                       postDataMap.get("name"), 
                                                       postDataMap.get("description"), 
                                                       postDataMap.get("script"));
                    saveResult = true;
                } catch (Exception ex){
                    LOG.error("Problem updating/saving a scripted driver: {}", ex.getMessage(), ex);
                    errorMessage = ex.getMessage();
                }
            } else {
                errorMessage = "All data is needed, check the post values";
            }
        }
    }
    
    @Override
    public final String render() throws Exception {
        if(saveResult == false){
            return "{ \"result\" : { \"exec\":false, \"reason\":\""+errorMessage+"\" } }";
        } else {
            return "{ \"result\" : { \"exec\":true } }";               
        }
    }
    
}