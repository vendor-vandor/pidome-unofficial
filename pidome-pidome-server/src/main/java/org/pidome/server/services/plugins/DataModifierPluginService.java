/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.datamodifiers.DataModifierPlugin;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.system.packages.PackagePermissionsNotUpToDateException;
import org.pidome.server.system.plugins.PluginsDB;

/**
 *
 * @author John
 */
public class DataModifierPluginService extends PluginService {

    static Logger LOG = LogManager.getLogger(DataModifierPluginService.class);
    
    static DataModifierPluginService me;
    
    int definedPluginId = 16;
    int definedPluginTypeId = 7;
    
    /**
     * Constructor.
     */
    protected DataModifierPluginService(){
        if(me!=null){
            me = this;
        }
    }
    
    /**
     * Returns instance.
     * @return 
     */
    public static DataModifierPluginService getInstance(){
        if(me==null){
            me = new DataModifierPluginService();
        }
        return me;
    }
    
    @Override
    public int getInstalledId() {
        return definedPluginId;
    }
    
    @Override
    public int getPluginTypeId() {
        return definedPluginTypeId;
    }

    /**
     * Returns the Data modifier plugin plugin.
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException
     */
    @Override
    public final DataModifierPlugin getPlugin(int pluginId) throws PluginException {
        if(!pluginsList.isEmpty()){
            if(pluginsList.containsKey(pluginId)){
                return (DataModifierPlugin)pluginsList.get(pluginId);
            } else {
                throw new PluginException("Plugin width id " +pluginId+ " not loaded");
            }
        } else {
            throw new PluginException("Plugin not loaded");
        }
    }
    
    /**
     * Updates a plugin.
     * @param pluginId
     * @param name
     * @param description
     * @param locationId
     * @param favorite
     * @param options
     * @return
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    @Override
    public boolean updatePlugin(int pluginId, String name, String description, int locationId, boolean favorite, Map<String,String> options) throws PluginServiceException {
        if(super.updatePlugin(pluginId, name, description, locationId, favorite, options)){
            try {
                DataModifierPlugin plugin = me.getPlugin(pluginId);
                for(DeviceInterface device:DeviceService.getActiveDevices()){
                    for(DeviceControlsGroup group:device.getFullCommandSet().getControlsGroups().values()){
                        for(DeviceControl control:group.getGroupControls().values()){
                            if(control.hasModifier() && control.getModifierId() == pluginId){
                                plugin.addDeviceControl(control);
                            }
                        }
                    }
                }
                return true;
            } catch (PluginException ex) {
                LOG.warn("The modified plugin id {} is not available", pluginId);
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Returns the device plugins known and if active including object.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public List<DataModifierPlugin> getPlugins(){
        List<DataModifierPlugin> items = new ArrayList<>();
        for(PluginBase plugin:pluginsList.values()){
            items.add((DataModifierPlugin)plugin);
        }
        return items;
    }
    
    /**
     * Returns only the active media plugins known.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getActivePlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key) && pluginsList.get(key).getRunning()){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (DataModifierPlugin)pluginsList.get(key));
            } else {
                pluginCollection.remove(key);
            }
        }
        return pluginCollection;
    }
    
    
    /**
     * Returns a clean plugin instance.
     * @param installed_id
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException 
     */
    public final DataModifierPlugin getBareboneDevicePluginInstance(int installed_id) throws PluginException {
        try {
            Map<String,Object> baseInfo = PluginsDB.getInstalledPlugin(installed_id);
            DataModifierPlugin bareBoneClass = (DataModifierPlugin)packages.loadPlugin(installed_id).getConstructor().newInstance();
            bareBoneClass.setBaseName((String)baseInfo.get("name"));
            return bareBoneClass;
        } catch (PackagePermissionsNotUpToDateException| ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.error("Could not load installed plugin id {}, reason: {}, cause: {}", installed_id, ex.getMessage(), ex.getCause(), ex);
            throw new PluginException("Unable to load plugin: " + ex.getMessage());
        }
    }
    
    @Override
    void startPluginHandlers(int pluginId) {
        //// not used
    }

    @Override
    void stopHandlers(int pluginId) {
        //// not used
    }

    @Override
    public String getServiceName() {
        return "Data modifiers plugin service";
    }
}
