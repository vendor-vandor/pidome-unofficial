/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.weather;

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

/**
 * Weather plugin service.
 * @author John
 */
public final class WeatherPluginService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(WeatherPluginService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * The plugin holding the weather data.
     */
    private WeatherPlugin plugin;
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public WeatherPluginService(PCCConnectionInterface connection){
        this.connection = connection;
        try {
            plugin = new WeatherPlugin(this.connection);
        } catch (WeatherPluginException ex) {
            Logger.getLogger(WeatherPluginService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        if(plugin!=null){
            plugin.unset();
            plugin = null;
        }
    }
    
    /**
     * Initializes the service.
     * This is mostly used to connect a listener to the specific namespace (in this case weatherservice).
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("WeatherService", this);
    }

    /**
     * Releases any resources.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("WeatherService", this);
    }

    /**
     * Returns the weather plugin.
     * @return The weather plugin
     * @throws WeatherPluginException When there is no plugin active.
     */
    public final WeatherPlugin getWeather() throws WeatherPluginException {
        if(plugin==null){
            throw new WeatherPluginException("No weather plugin available");
        } else {
            return plugin;
        }
    }
    
    /**
     * Updates the weather data with the latest info from the server.
     * This needs to be called at least once.
     * @throws WeatherPluginException When no plugin is available.
     */
    public final void update() throws WeatherPluginException {
        if(plugin==null){
            throw new WeatherPluginException("No weather plugin available");
        } else {
            plugin.update();
        }
    }
    
    /**
     * Loads initial weather data.
     * @throws WeatherPluginException 
     */
    private void load() throws WeatherPluginException {
        try {
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("WeatherService.getPlugins", null, "WeatherService.getPlugins"));
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(WeatherPluginService.class.getName()).log(Level.SEVERE, "Problem retrieving weather plugins", ex);
            throw new WeatherPluginException("Problem getting initial weather data");
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
            } catch (WeatherPluginException ex) {
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
        plugin.unset();
        plugin = null;
        preload();
    }
    
    /**
     * Handles weather broadcast messages.
     * @param rpcDataHandler PCCEntityDataHandler with weather broadcast data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        try {
            update();
        } catch (WeatherPluginException ex) {
            Logger.getLogger(WeatherPluginService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles result returned over http.
     * @param rpcDataHandler PCCEntityDataHandler with weather request result data.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        if(data!=null){
            Runnable run = () -> {
                for(Map<String,Object> pluginData:data){
                    try {
                        plugin.setPluginData(((Number)pluginData.get("id")).intValue(), (ArrayList)pluginData.get("capabilities"));
                        plugin.update();
                    } catch (WeatherPluginException ex) {
                        Logger.getLogger(WeatherPluginService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            run.run();
        }
    }
    
}
