/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.NonFatalPluginException;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.plugindata.PluginDataException;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.datastorage.RoundRobinDataStorage;
import org.pidome.server.system.packages.PackagePermissionsNotUpToDateException;
import org.pidome.server.system.packages.PackageProxy;
import org.pidome.server.system.plugins.PluginsDB;

/*
 * Handles the external services (eg, phonserver, XBMC)
 */

public abstract class PluginService implements ServiceInterface {
    
    Connection fileDBConnection;
    
    static Logger LOG = LogManager.getLogger(PluginService.class);
    
    static final List _listeners = new ArrayList();

    static PackageProxy packages = new PackageProxy();
    
    boolean running = false;
    
    static PluginService me;
    
    protected final Map<Integer, PluginBase> pluginsList = new HashMap<>();
    
    private final static List<PluginService> activePluginTypes = new ArrayList<>();
    
    protected PluginService(){
        if(!activePluginTypes.contains(this)) activePluginTypes.add(this);
    }
    
    public final void startHandlers(int pluginId){
        this.startPluginHandlers(pluginId);
    }
    
    /**
     * Stops the graph receiver.
     * @param plugin 
     */
    private void stopGraphReceiver(PluginBase plugin){
        if (plugin.hasGraphReceiver()){
            if(plugin.getGraphReceiver() instanceof RoundRobinDataStorage){
                ((RoundRobinDataStorage)plugin.getGraphReceiver()).stop();
            }
            plugin.removeGraphReceiver();
        }
    }
    
    /**
     * Returns the id used by the plugin.
     * @return 
     */
    public abstract int getInstalledId();
    
    /**
     * Returns the plugin's type id.
     * @return 
     */
    public abstract int getPluginTypeId();
    
    /**
     * Returns a service specific plugin.
     * @param pluginId
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException 
     */
    public abstract PluginBase getPlugin(int pluginId) throws PluginException;
    
    /**
     * Starts any handlers needed like listeners etc.
     * @param pluginId 
     */
    abstract void startPluginHandlers(int pluginId);
    
    /**
     * Stops handlers which where needed like listeners etc.
     * @param pluginId 
     */
    abstract void stopHandlers(int pluginId);

    /**
     * Returns a list of all installed plugins active or not without plugin objects.
     * @return 
     */
    public static List<Map<String,Object>>getFullInstalledPluginCollection(){
        return PluginsDB.getFullInstalledPluginCollection();
    }
    
    /**
     * Stops the plugin service unloading all the plugins.
     */
    @Override
    public void interrupt() {
        for(int pluginId:pluginsList.keySet()){
            try {
                stopPlugin(pluginId);
            } catch (PluginException ex) {
                LOG.error("Could not stop plugin: {}, reason: {}", pluginsList.get(pluginId).getPluginName(), ex.getMessage());
            }
        }
    }

    /**
     * Plugin starter. loads ALL the plugins.
     */
    @Override
    public final void start() {
        Thread pluginStarter = new Thread(){
            @Override
            public final void run(){
                loader();
                running = true;
            }
        };
        pluginStarter.setName("PluginService::Start:" + this.getServiceName());
        LOG.info("Starting plugin service: {}", this.getServiceName());
        pluginStarter.start();
    }
    
    /**
     * Plugin stopper. 
     * Unloads ALL the plugins for the current plugin type.
     */
    public final void stop() {
        Thread pluginStopper = new Thread(){
            @Override
            public final void run(){
                interrupt();
            }
        };
        pluginStopper.setName("PluginService::Stop:" + this.getServiceName());
        pluginStopper.start();
    }
    
    /**
     * Returns a list of installed plugins.
     * @return 
     */
    public final Map<Integer,Map<String,Object>> getInstalledPlugins(){
        return PluginsDB.getInstalledPlugins(getPluginTypeId());
    }
    
    /**
     * Returns a list of available plugins of the current type.
     * This is a non active informational list only.
     * @return 
     */
    public final Map<Integer,Map<String,Object>> getInstalledPluginsList(){
         return getInstalledPlugins(getPluginTypeId());
    }
    
    /**
     * Loads all the specific plugins for the current plugin type.
     */
    final void loader(){
        Runnable pluginStarter = () -> {
            Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
            LOG.debug("Found plugins: {}", pluginCollection);
            pluginCollection.keySet().stream().forEach((key) -> {
                LOG.info("Starting '{}' ({}) with the '{}' plugin service", pluginCollection.get(key).get("name"),pluginCollection.get(key).get("pluginname"), this.getServiceName());
                loadPlugin(key, pluginCollection.get(key));
            });
        };
        pluginStarter.run();
    }
    
    /**
     * Starts a list of plugins based on the base installed plugin.
     * @param installedId 
     */
    final void startPluginsByInstalledId(int installedId){
        Runnable pluginStarter = () -> {
            Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPluginsByInstalledId(installedId, getPluginTypeId());
            LOG.debug("Found plugins: {}", pluginCollection);
            pluginCollection.keySet().stream().forEach((key) -> {
                LOG.info("Starting '{}' ({}) with the '{}' plugin service", pluginCollection.get(key).get("name"),pluginCollection.get(key).get("pluginname"), this.getServiceName());
                loadPlugin(key, pluginCollection.get(key));
            });
        };
        pluginStarter.run();
    }
    
    /**
     * Returns a clean plugin instance.
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException 
     */
    public final PluginBase getBareboneInstance() throws PluginException {
        try {
            Map<String,Object> baseInfo = PluginsDB.getInstalledPlugin(getInstalledId());
            PluginBase bareBoneClass = (PluginBase)packages.loadPlugin(getInstalledId()).getConstructor().newInstance();
            bareBoneClass.setBaseName((String)baseInfo.get("name"));
            return bareBoneClass;
        } catch (PackagePermissionsNotUpToDateException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.error("Could not load installed plugin id {}, reason: {}, cause: {}", getInstalledId(), ex.getMessage(), ex.getCause(), ex);
            throw new PluginException("Unable to load plugin: " + ex.getMessage());
        }
    }
    
    /**
     * Returns DB data from an installed plugin.
     * @param installedId
     * @return 
     */
    public final Map<String,Object> getPluginBase(int installedId){
        return PluginsDB.getInstalledPlugin(installedId);
    }
    
    /**
     * Returns a clean plugin instance which is empty and without plugin starting.
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException 
     */
    public final PluginBase getBareboneInstance(int installedId) throws PluginException {
        try {
            Map<String,Object> baseInfo = PluginsDB.getInstalledPlugin(installedId);
            PluginBase bareBoneClass = (PluginBase)packages.loadPlugin(installedId).getConstructor().newInstance();
            bareBoneClass.setBaseName((String)baseInfo.get("name"));
            return bareBoneClass;
        } catch (PackagePermissionsNotUpToDateException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOG.error("Could not load installed plugin id {}, reason: {}, cause: {}", getInstalledId(), ex.getMessage(), ex.getCause(), ex);
            throw new PluginException("Unable to load plugin: " + ex.getMessage());
        }
    }
    
    /**
     * Returns the same as the barebone instance, only now with check if the given plugin id is added.
     * @param pluginId
     * @return
     * @throws PluginException 
     */
    PluginBase getPluginInstance(int pluginId) throws PluginException {
        try {
            Map<String,Object> baseInfo = PluginsDB.getPlugin(pluginId);
            LOG.debug("Loading current plugin: {}", baseInfo);
            if(!baseInfo.isEmpty()){
                PluginBase plugin = (PluginBase)packages.loadPlugin((int)baseInfo.get("installed_plugin")).getConstructor().newInstance();
                if(plugin.hasGraphData()){
                    stopGraphReceiver(plugin);
                    plugin.setGraphReceiver(new RoundRobinDataStorage(RoundRobinDataStorage.Source.PLUGIN, pluginId));
                }
               return plugin; 
            } else {
                throw new PluginException("Plugin id "+pluginId+" not found");
            }
        } catch (PackagePermissionsNotUpToDateException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new PluginException(ex.getMessage());
        }
    }
    
    /**
     * Loads the found plugin and tries to start.
     * @param pluginId
     * @param pluginDetails 
     */
    final void loadPlugin(int pluginId, Map<String,Object> pluginDetails){
        try {
            loadPlugin(pluginId, pluginDetails, false, null);
        } catch (Exception ex){
            LOG.error("Unhandled exception in plugin loader. Message: {}, Plugin details: {}", ex.getMessage(), pluginDetails, ex);
        }
    }
    
    /**
     * Loads the found plugin and tries to start.
     * @param pluginId
     * @param pluginDetails 
     */
    final void loadPlugin(int pluginId, Map<String,Object> pluginDetails, boolean firstLoad, Map<String,String> optionsOverride){
        if(!pluginDetails.isEmpty()){
            try {
                if(pluginsList.containsKey(pluginId)){
                    pluginsList.get(pluginId).stopPlugin();
                }
                PluginBase plugin = getPluginInstance(pluginId);
                plugin.setPluginId(pluginId);
                plugin.setInstalledId((int)pluginDetails.get("installed_plugin"));
                plugin.setBaseName((String)pluginDetails.get("pluginname"));
                plugin.setPluginLocation((String)pluginDetails.get("location"));
                plugin.setPluginLocationId((int)pluginDetails.get("locationid"));
                plugin.setPluginName((String)pluginDetails.get("name"));
                plugin.setPluginDescription((String)pluginDetails.get("description"));
                plugin.setPluginPath((String)pluginDetails.get("pluginpath"));
                plugin.setIsFavorite((boolean)pluginDetails.get("favorite"));
                plugin.setCustomData((String)pluginDetails.get("customdata"));
                pluginsList.put(pluginId, plugin);
                if(firstLoad==true){
                    startNewPlugin(plugin, optionsOverride);
                } else {
                    startPlugin(plugin);
                }
                LOG.info("Started plugin: {}", (String)pluginDetails.get("name"));
            } catch (PluginException ex) {
                LOG.error("Could not load plugin id {}, reason: {}, cause: {}", pluginId,ex.getMessage(), ex.getCause(), ex);
            }
        }
    }
    
    /**
     * Saves a plugin custom data.
     * @param pluginId
     * @param data
     * @throws PluginException 
     */
    final boolean saveCustomData(int pluginId, String data) throws PluginException {
        try {
            PluginsDB.saveCustomData(pluginId, data);
            getPlugin(pluginId).setCustomData(data);
            return true;
        } catch (PluginServiceException | PluginException ex) {
            LOG.error("Could not set/save custom plugin data for plugin id: {}", pluginId);
            throw new PluginException("Could not set/save custom plugin data for plugin id: " + pluginId);
        }
    }
    
    /**
     * Returns ALL the plugins based on type id.
     * There is no indication in this list if a plugin is active or not.
     * @param typeId
     * @return 
     */
    protected static Map<Integer,Map<String,Object>> getInstalledPlugins(int typeId){
        return PluginsDB.getInstalledPluginsByTypeId(typeId);
    }
    
    /**
     * Mark a plugin as favorite or not.
     * @param pluginId
     * @param favorite
     * @return 
     */
    public static boolean setFavorite(final int pluginId, final boolean favorite) throws PluginServiceException {
        PluginsDB.setFavorite(pluginId, favorite);
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", pluginId);
                put("favorite", favorite);
            }
        };
        ClientMessenger.send("PluginService","setFavorite", 0, sendObject);
        return true;
    }
    
    /**
     * Mark a plugin as active or not.
     * @param installedId
     * @param active
     * @return 
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    public static boolean setActive(final int installedId, final boolean active) throws PluginServiceException {
        PluginsDB.setInstalledPluginActive(installedId, active);
        for(PluginService plugin:activePluginTypes){
            if(plugin!=null){
                Map<Integer, PluginBase> runningPlugins = plugin.getRunningPlugins();
                if(active){
                    if(PluginsDB.pluginTypeHasInstalledId(installedId, plugin.getInstalledId())){
                        plugin.startPluginsByInstalledId(installedId);
                    }
                } else {
                    ArrayList<Integer>stopList = new ArrayList<>();
                    for(PluginBase pluginBase:runningPlugins.values()){
                        if(pluginBase.getInstalledId()==installedId){
                            stopList.add(pluginBase.getPluginId());
                        }
                        for(int pluginId:stopList){
                            try {
                                plugin.stopPlugin(pluginId);
                            } catch (PluginException ex) {
                                try {
                                    LOG.error("Could not stop deactivated plugin: {}", plugin.getPlugin(pluginId).getPluginName());
                                } catch (PluginException ex1) {
                                    LOG.error("Could not stop deactivated plugin id: {}", pluginId);
                                }
                            }
                        }
                    }
                }
            }
        }
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("installedid", installedId);
                put("activated", active);
            }
        };
        ClientMessenger.send("PluginService","setInstalledActive", 0, sendObject);
        return true;
    }
    
    /**
     * Returns a list of currently running plugins for the current service.
     * @return 
     */
    public final Map<Integer, PluginBase> getRunningPlugins(){
        return pluginsList;
    }
    
    /**
     * Returns all the known plugins which has been added (not the installed ones).
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getPluginsCollection(){
        return PluginsDB.getPluginCollection();
    }
    
    /**
     * Sets configuration and starts a plugin 
     * @param plugin 
     */
    final void startPlugin(PluginBase plugin){
        try {
            plugin.setConfigurationValues(new PluginConfig(plugin).loadAll());
            startHandlers(plugin.getPluginId());
            plugin.startPlugin();
        } catch (NonFatalPluginException ex){
            LOG.warn("Plugin started with a warning: {}", ex.getMessage());
        } catch (PluginException | WebConfigurationException ex) {
            LOG.error("Could not start plugin: " + ex.getMessage());
            try {
                stopPlugin(plugin.getPluginId(), true);
            } catch (PluginException ex1) {
                LOG.error("Could not finalize a failing plugin: {}", ex1.getMessage());
            }
        }
    }
    
    /**
     * Sets configuration and starts a plugin 
     * @param plugin 
     */
    final void startNewPlugin(PluginBase plugin, Map<String,String> optionsOverride){
        try {
            PluginConfig config = new PluginConfig(plugin);
            for (Map.Entry<String, String> entry : optionsOverride.entrySet()) {
                config.setProperty(entry.getKey(), entry.getValue());
            }
            try {
                config.store("Initial settings save");
            } catch (IOException ex) {
                LOG.error("Could not save initial web settings, try to edit again");
            }
            plugin.setConfigurationValues(config.loadAll());
            startHandlers(plugin.getPluginId());
            plugin.startPlugin();
        } catch (PluginException | WebConfigurationException ex) {
            LOG.error("Could not start plugin: " + ex.getMessage());
        }
    }
    
    /**
     * Check if the plugin lives.
     * @return 
     */
    @Override
    public boolean isAlive() {
        return running;
    }

    /**
     * Sends out a broadcast a plugin has been added.
     * @param id
     */
    static void sendNewPluginBroadcast(final int id) {
        Map<String,Object> newData = new HashMap<String,Object>(){{
            put("id", id);
        }};
        ClientMessenger.send("PluginService","addPlugin", 0, newData);
    }
    
    /**
     * Sends out a broadcast a plugin has been deleted.
     * @param id
     */
    static void sendDeletePluginBroadcast(final int id) {
        Map<String,Object> newData = new HashMap<String,Object>(){{
            put("id", id);
        }};
        ClientMessenger.send("PluginService","deletePlugin", 0, newData);
    }
    
    /**
     * Sends out a broadcast a plugin has been updated.
     * @param id 
     */
    static void sendUpdatePluginBroadcast(final int id) {
        Map<String,Object> newData = new HashMap<String,Object>(){{
            put("id", id);
        }};
        ClientMessenger.send("PluginService","updatePlugin", 0, newData);
    }
    
    /**
     * Saves a plugin and tries to start it.
     * @param name
     * @param description
     * @param locationId
     * @param favorite
     * @param options
     * @return
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    public boolean savePlugin(String name, String description, int locationId, boolean favorite, Map<String,String> options) throws PluginServiceException {
        LOG.debug("Saving plugin: {}, {}, {}, {}, {} with options: {}", getInstalledId(), name, description, locationId, favorite, options);
        int newId = PluginsDB.savePlugin(getInstalledId(), name, description, locationId, favorite);
        loadPlugin(newId, PluginsDB.getPlugin(newId), true, options);
        sendNewPluginBroadcast(newId);
        return true;
    }

    /**
     * Saves a plugin and tries to start it.
     * @param name
     * @param description
     * @param locationId
     * @param favorite
     * @param options
     * @param installed_id
     * @return
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    public boolean savePlugin(String name, String description, int locationId, boolean favorite, Map<String,String> options, int installed_id) throws PluginServiceException {
        LOG.debug("Saving plugin: {}, {}, {}, {}, {} with options: {}", installed_id, name, description, locationId, favorite, options);
        int newId = PluginsDB.savePlugin(installed_id, name, description, locationId, favorite);
        loadPlugin(newId, PluginsDB.getPlugin(newId), true, options);
        sendNewPluginBroadcast(newId);
        return true;
    }
    
    /**
     * Should stop a plugin id.
     * @param pluginId 
     * @param removeResource
     * @throws PluginException 
     */
    final void stopPlugin(int pluginId, boolean removeResource) throws PluginException {
        stopGraphReceiver(pluginsList.get(pluginId));
        stopHandlers(pluginId);
        pluginsList.get(pluginId).stopPlugin();
        LOG.info("Stopped plugin: {}", pluginsList.get(pluginId).getPluginName());
        if(removeResource)pluginsList.remove(pluginId);
    }
    
    /**
     * Stops a plugin without removing the in memory plugin pointer.
     * @param pluginId
     * @throws PluginException 
     */
    final void stopPlugin(int pluginId) throws PluginException{
        stopPlugin(pluginId, false);
    }
    
    /**
     * Prepares for plugin deletion.
     * prepareDelete is called on a plugin instance so it can make preparations
     * to delete any stuff.
     * @param pluginId 
     */
    final void prepareDelete(int pluginId){
        if(pluginsList.containsKey(pluginId)){
            pluginsList.get(pluginId).prepareDelete();
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
    public boolean updatePlugin(int pluginId, String name, String description, int locationId, boolean favorite, Map<String,String> options) throws PluginServiceException {
        try {
            LOG.debug("Updating plugin: {}, {}, {}, {}, {} with options: {}", pluginId, name, description, locationId, favorite, options);
            stopPlugin(pluginId);
            PluginsDB.updatePlugin(pluginId, name, description, locationId, favorite);
            PluginConfig config = new PluginConfig(pluginsList.get(pluginId));
            for (Map.Entry<String, String> entry : options.entrySet()) {
                config.setProperty(entry.getKey(), entry.getValue());
            }
            try {
                config.store("Update save");
            } catch (IOException ex) {
                LOG.error("Could not update plugin configuration");
            }
            LOG.info("Updated plugin: {}", name);
            loadPlugin(pluginId, PluginsDB.getPlugin(pluginId), false, options);
            sendUpdatePluginBroadcast(pluginId);
            return true;
        } catch (PluginException ex) {
            throw new PluginServiceException(ex.getMessage());
        }
    }
    
    
    /**
     * Unloads and deletes a plugin from the database.
     * @param pluginId
     * @return 
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    public boolean deletePlugin(int pluginId) throws PluginServiceException {
        try {
            stopPlugin(pluginId);
            prepareDelete(pluginId);
            PluginsDB.removePlugin(pluginId);
            PluginConfig config = new PluginConfig(pluginsList.get(pluginId));
            config.delete();
            sendDeletePluginBroadcast(pluginId);
            pluginsList.remove(pluginId);
            return true;
        } catch (PluginException ex) {
            throw new PluginServiceException(ex.getMessage());
        }
    }

}

final class PluginData extends Properties {

    String propertiesFile;
    String propertiesDir;
    File propsFile;
    PluginBase plugin;
    
    static Logger LOG = LogManager.getLogger(PluginConfig.class);
    
    /**
     * Props constructor.
     * @param plugin
     * @throws IOException 
     */
    public PluginData(PluginBase plugin) {
        this.plugin = plugin;
        propertiesDir = "config/plugins/" + plugin.getBaseName();
        new File(propertiesDir).mkdirs();
        propertiesFile = "config/plugins/" + plugin.getBaseName() + "/" + plugin.getPluginId() + ".data.properties";
        propsFile = new File(propertiesFile);
    }
    
    /**
     * Loads saved values.
     */
    public final Map<String,String> loadAllData(){
        Map<String,String> confEntries= new HashMap<>();
        try {
            if(!propsFile.exists()) {
                propsFile.createNewFile();
            }
            try (FileInputStream stream = new FileInputStream(propsFile)){
                load(stream);
            }
            plugin.setDataValuesByProperties(this);
        } catch (PluginDataException | IOException ex) {
            LOG.error("Data set could not be loaded: {}", ex.getMessage());
        }
        return confEntries;
    }
    
    /**
     * Stores the current values.
     * @param comment
     * @throws IOException 
     */
    public final void store(String comment) throws IOException {
        if (!propsFile.exists()) {
            propsFile.createNewFile();
        }
        try (FileOutputStream stream = new FileOutputStream(propertiesFile)){
            store(stream, comment);
        }
    }
    
    /**
     * Removes a settings file.
     */
    public final void delete(){
        if (propsFile.exists()) {
            propsFile.delete();
        }
    }
    
}

/**
 * Plugin properties.
 * These are the collection of plugin options. These are saved on the server.
 * @author John Sirach <john.sirach@gmail.com>
 */
final class PluginConfig extends Properties {

    String propertiesFile;
    String propertiesDir;
    File propsFile;
    PluginBase plugin;
    
    static Logger LOG = LogManager.getLogger(PluginConfig.class);
    
    /**
     * Props constructor.
     * @param plugin
     * @throws IOException 
     */
    public PluginConfig(PluginBase plugin) {
        this.plugin = plugin;
        propertiesDir = "config/plugins/" + plugin.getBaseName();
        new File(propertiesDir).mkdirs();
        propertiesFile = "config/plugins/" + plugin.getBaseName() + "/" + plugin.getPluginId() + ".properties";
        propsFile = new File(propertiesFile);
        LOG.debug("Using configuration values from: {}", propsFile.getAbsoluteFile());
    }
    
    /**
     * Returns the configuration options list.
     * @return
     * @throws WebConfigurationException 
     */
    public final List<List<WebOption>> getUnboundConfig() throws WebConfigurationException {
        WebConfiguration config = plugin.getConfiguration();
        List<WebConfigurationOptionSet> optionSets = config.getOptions();
        List<List<WebOption>> options = new ArrayList();
        for (WebConfigurationOptionSet optionSet : optionSets) {
            options.add(optionSet.getOptions());
        }
        LOG.debug("Defined parameter values from plugin {}: {}", plugin.getName(), options);
        return options;
    }
    
    /**
     * Loads saved values.
     */
    public final Map<String,String> loadAll(){
        Map<String,String> confEntries= new HashMap<>();
        try {
            List<List<WebOption>> optionSet = getUnboundConfig();
            if(!propsFile.exists()) {
                propsFile.createNewFile();
                optionSet.stream().forEach((options) -> {
                    options.stream().forEach((option) -> {
                        setProperty(option.getId(), "");
                    });
                });
                LOG.debug("Initial plugin save for {} as there is no config file yet. Saved as: {}", plugin.getName(), propsFile.getAbsoluteFile());
                store("Initial save");
            }
            try (FileInputStream stream = new FileInputStream(propsFile)){
                load(stream);
            } catch (Exception ex){
                LOG.error("Could not load configuration for {}, reason: {}", plugin.getName(), ex.getMessage());
            }
            for(String id:this.stringPropertyNames()){
                confEntries.put(id, getProperty(id));
            }
            for(List<WebOption> options:optionSet){
                for (WebOption option : options) {
                    option.setValue(getProperty(option.getId()));
                    confEntries.put(option.getId(), option.getValue());
                }
            }
        } catch (WebConfigurationException | IOException ex) {
            LOG.error("Configuration could not be loaded: {}", ex.getMessage());
        }
        LOG.debug("Loaded parameter values for plugin {}: {}", plugin.getName(), confEntries);
        return confEntries;
    }
    
    /**
     * Stores the current values.
     * @param comment
     * @throws IOException 
     */
    public final void store(String comment) throws IOException {
        if (!propsFile.exists()) {
            propsFile.createNewFile();
        }
        try (FileOutputStream stream = new FileOutputStream(propertiesFile)){
            store(stream, comment);
        }
    }
    
    /**
     * Removes a settings file.
     */
    public final void delete(){
        if (propsFile.exists()) {
            propsFile.delete();
        }
    }
}