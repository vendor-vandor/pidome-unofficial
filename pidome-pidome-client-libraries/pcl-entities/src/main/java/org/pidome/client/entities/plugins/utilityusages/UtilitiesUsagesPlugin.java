/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.utilityusages;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;

/**
 * Service for the utilities plugin usages.
 * @author John
 */
public final class UtilitiesUsagesPlugin extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(UtilitiesUsagesPlugin.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * The plugin holding the utility data.
     */
    private ObjectPropertyBindingBean<UtilityPlugin> plugin = new ObjectPropertyBindingBean();
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public UtilitiesUsagesPlugin(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Initialize the plugin service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("UtilityMeasurementService", this);
    }

    /**
     * Release the service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("UtilityMeasurementService", this);
    }

    /**
     * Loads the utilities.
     * @throws UtilitiesUsagesPluginException 
     */
    private void load() throws UtilitiesUsagesPluginException {
        try {
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("UtilityMeasurementService.getPlugins", null, "UtilityMeasurementService.getPlugins"));
        } catch (PCCEntityDataHandlerException ex) {
            throw new UtilitiesUsagesPluginException("Problem getting initial utilities data");
        }
    }
    
    /**
     * Preloads data.
     * @throws EntityNotAvailableException When the whole plugin structure is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                load();
            } catch (UtilitiesUsagesPluginException ex) {
                throw new EntityNotAvailableException("Could not preload weather", ex);
            }
        }
    }

    /**
     * Reloads the service.
     * @throws EntityNotAvailableException When the plugin is unavailable.
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        if(plugin.getValue()!=null){
            plugin.getValue().unset();
        }
        preload();
    }

    /**
     * Returns the weather plugin.
     * @return The weather plugin
     * @throws UtilitiesUsagesPluginException When there is no plugin active.
     */
    public final ObjectPropertyBindingBean<UtilityPlugin> getUtilitiesUsages() throws UtilitiesUsagesPluginException {
        if(plugin==null){
            throw new UtilitiesUsagesPluginException("No utilities usages plugin available");
        } else {
            return plugin;
        }
    }
    
    /**
     * Unloads any content.
     * @throws EntityNotAvailableException When the entity is unavailable.
     */
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        //
    }

    /**
     * Handles broadcast data.
     * @param rpcDataHandler The utility usages rpc data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "getCurrentUsage":
                Map<String,Object> params = rpcDataHandler.getParameters();
                try {
                    Map<String,Map<String,Double>> set = (Map<String,Map<String,Double>>)params.get("values");
                    switch((String)params.get("type")){
                        case "POWER":
                            plugin.getValue().getPower().setTodayKwhUsage(set.get("today").get("value"));
                        break;
                        case "WATER":
                            plugin.getValue().getWater().setTodayWaterUsage(set.get("today").get("value"));
                        break;
                        case "GAS":
                            plugin.getValue().getGas().setTodayGasUsage(set.get("today").get("value"));
                        break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(UtilitiesUsagesPlugin.class.getName()).log(Level.SEVERE, "Could not update data", ex);
                }
            break;
        }
    }

    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            if(data!=null){
                for(Map<String,Object> pluginData:data){
                    try {
                        UtilityPlugin plug = new UtilityPlugin(((Number)pluginData.get("id")).intValue(), this.connection);
                        Map<String,ArrayList<Map<String,Map<String,Object>>>> currentData = (Map<String,ArrayList<Map<String,Map<String,Object>>>>)pluginData.get("currentusage");
                        try {
                            plug.getPower().setTodayKwhUsage((double)currentData.get("POWER").get(0).get("today").get("value"));
                            plug.getPower().setTodayKwhName((String)currentData.get("POWER").get(0).get("today").get("name"));
                            plug.getPower().setTodayThreshold((double)currentData.get("POWER").get(0).get("today").get("threshold"));
                        } catch (Exception ex){
                            plug.getPower().setTodayKwhUsage(0.0);
                            plug.getPower().setTodayKwhName("KW/h");
                            plug.getPower().setTodayThreshold(0.0);
                        }
                        try {
                            plug.getWater().setTodayWaterUsage((double)currentData.get("WATER").get(0).get("today").get("value"));
                            plug.getWater().setTodayWaterName((String)currentData.get("WATER").get(0).get("today").get("name"));
                            plug.getWater().setTodayThreshold((double)currentData.get("WATER").get(0).get("today").get("threshold"));
                        } catch (Exception ex){
                            plug.getWater().setTodayWaterUsage(0.0);
                            plug.getWater().setTodayWaterName("Water");
                            plug.getWater().setTodayThreshold(0.0);
                        }
                        try {
                            plug.getGas().setTodayGasUsage((double)currentData.get("GAS").get(0).get("today").get("value"));
                            plug.getGas().setTodayGasName((String)currentData.get("GAS").get(0).get("today").get("name"));
                            plug.getGas().setTodayThreshold((double)currentData.get("GAS").get(0).get("today").get("threshold"));
                        } catch (Exception ex){
                            plug.getGas().setTodayGasUsage(0.0);
                            plug.getGas().setTodayGasName("Gas");
                            plug.getGas().setTodayThreshold(0.0);
                        }
                        plugin.setValue(plug);
                        plug.start();
                    } catch (UtilitiesUsagesPluginException ex) {
                        Logger.getLogger(UtilitiesUsagesPlugin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        run.run();
    }
    
}
