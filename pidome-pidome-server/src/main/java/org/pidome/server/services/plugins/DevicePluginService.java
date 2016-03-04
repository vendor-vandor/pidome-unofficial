/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.services.plugins;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.services.hardware.DeviceService;
import static org.pidome.server.services.plugins.PluginService.packages;
import org.pidome.server.system.hardware.devices.DevicesException;
import org.pidome.server.system.packages.PackagePermissionsNotUpToDateException;
import org.pidome.server.system.plugins.PluginsDB;

/**
 *
 * @author John
 */
public class DevicePluginService extends PluginService {
    
    private final int definedPluginId = 5;
    private final int definedPluginTypeId = 5;
    
    private static DevicePluginService me;
    
    /**
     * Constructor.
     */
    protected DevicePluginService(){
        if(me!=null){
            me = this;
        }
    }

    /**
     * Returns instance.
     * @return 
     */
    public static DevicePluginService getInstance(){
        if(me==null){
            me = new DevicePluginService();
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
     * Returns the device plugins known and if active including object.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getPlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            pluginCollection.get(key).put("id", key);
            if(pluginsList.containsKey(key)){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (DevicePlugin)pluginsList.get(key));
            } else {
                pluginCollection.get(key).put("active", false);
                pluginCollection.get(key).put("pluginObject", null);
            }
        }
        return pluginCollection;
    }
    
    /**
     * Returns only the active device plugins known.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getActivePlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key) && pluginsList.get(key).getRunning()){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (DevicePlugin)pluginsList.get(key));
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
    public final DevicePlugin getBareboneDevicePluginInstance(int installed_id) throws PluginException {
        try {
            Map<String,Object> baseInfo = PluginsDB.getInstalledPlugin(installed_id);
            DevicePlugin bareBoneClass = (DevicePlugin)packages.loadPlugin(installed_id).getConstructor().newInstance();
            bareBoneClass.setBaseName((String)baseInfo.get("name"));
            return bareBoneClass;
        } catch (PackagePermissionsNotUpToDateException| ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.error("Could not load installed plugin id {}, reason: {}, cause: {}", installed_id, ex.getMessage(), ex.getCause(), ex);
            throw new PluginException("Unable to load plugin: " + ex.getMessage());
        }
    }
    
    @Override
    public PluginBase getPlugin(int pluginId) throws PluginException {
        if(pluginsList.containsKey(pluginId)) return (DevicePlugin)pluginsList.get(pluginId);
        throw new PluginException("Plugin not found/active");
    }

    @Override
    void startPluginHandlers(int pluginId) {
        try {
            DevicePlugin plugin = (DevicePlugin) getPlugin(pluginId);
            DeviceService.startPluginDeviceEmulation(plugin);
        } catch (PluginException ex) {
            LOG.error("Could not start handlers for plugin id: {}", pluginId);
        } catch (DevicesException ex) {
            LOG.error("Error starting device emulation handlers for plugin id: {}", pluginId);
        }
    }

    @Override
    void stopHandlers(int pluginId) {
        try {
            DevicePlugin plugin = (DevicePlugin) getPlugin(pluginId);
            DeviceService.stopPluginDeviceEmulation(plugin);
        } catch (PluginException ex) {
            LOG.error("Could not start handlers for plugin id: {}", pluginId);
        } catch (DevicesException ex) {
            LOG.error("Error starting device emulation handlers for plugin id: {}", pluginId);
        }
    }

    @Override
    public String getServiceName() {
        return "Devices plugin service";
    }
    
    
}
