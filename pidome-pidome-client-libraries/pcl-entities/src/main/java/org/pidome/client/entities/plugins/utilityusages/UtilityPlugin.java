/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.utilityusages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class UtilityPlugin {
 
    UtilitiesPowerUsage powerUsage = new UtilitiesPowerUsage();
    UtilitiesWaterUsage waterUsage = new UtilitiesWaterUsage();
    UtilitiesGasUsage   gasUsage   = new UtilitiesGasUsage();
    
    static {
        Logger.getLogger(UtilityPlugin.class.getName()).setLevel(Level.ALL);
    }
    
    PCCConnectionInterface connection;
    
    private final int id;
    
    protected UtilityPlugin(int id, PCCConnectionInterface connection) throws UtilitiesUsagesPluginException {
        this.connection = connection;
        this.id = id;
    }
    
    protected final void start() throws UtilitiesUsagesPluginException {
        Map<String,Object> params = new HashMap<>();
        params.put("id", this.id);
        params.put("filter", new ArrayList());
        try {
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("UtilityMeasurementService.getCurrentTotalUsage", params, "UtilityMeasurementService.getCurrentTotalUsage"));
        } catch (PCCEntityDataHandlerException ex) {
            throw new UtilitiesUsagesPluginException("Problem getting initial utilities data");
        }
    }
    
    public final UtilitiesPowerUsage getPower(){
        return powerUsage;
    }
    
    public final UtilitiesWaterUsage getWater(){
        return waterUsage;
    }
    
    public final UtilitiesGasUsage getGas(){
        return gasUsage;
    }
    
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        Map<String,Map<String,Map<String,Object>>> data = (Map<String,Map<String,Map<String,Object>>>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            try {
                powerUsage.setTodayKwhUsage(((Number)data.get("POWER").get("today").get("value")).doubleValue());
                waterUsage.setTodayWaterUsage(((Number)data.get("WATER").get("today").get("value")).doubleValue());
                gasUsage.setTodayGasUsage(((Number)data.get("GAS").get("today").get("value")).doubleValue());
            } catch (Exception ex){
                Logger.getLogger(UtilityPlugin.class.getName()).log(Level.SEVERE, "Could not read utilities data", ex);
            }
        };
        run.run();
    }
    
    protected final void unset(){
        this.connection = null;
    }
    
}