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

package org.pidome.server.system.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.plugins.PluginServiceException;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John Sirach
 */
public class PluginsDB {
    
    static Logger LOG = LogManager.getLogger(PluginsDB.class);
    
    /**
     * Returns a list of added plugins for running.
     * @param typeId
     * @param activated
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getPlugins(int typeId, boolean activated){
        Map<Integer,Map<String,Object>> pluginsList = new HashMap<>();
        String ifActive = ";";
        if(activated){
            ifActive = " AND ip.active=?";
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT p.[id], p.[name], p.[description], p.[data], p.[location] as locationid, p.[favorite] as favorite, p.[fixed] as fixed, l.[name] as location, ip.[type] as typeid, ip.[id] as installed_id, ip.[active] as activated, ip.[name] as pluginname, ip.[path] as pluginpath "
                                                                       + "FROM plugins p "
                                                                 + "LEFT JOIN installed_plugins ip ON ip.id=p.installed_plugin "
                                                                 + "LEFT JOIN locations l ON l.id=p.location "
                                                                      + "WHERE ip.type=?" + ifActive); 
            prep.setInt(1, typeId);
            if(activated) prep.setBoolean(2, true);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    Map<String, Object> plugin = new HashMap<>();
                    plugin.put("name", rsPlugins.getString("name"));
                    plugin.put("description", rsPlugins.getString("description"));
                    plugin.put("location", rsPlugins.getString("location"));
                    plugin.put("locationid", rsPlugins.getInt("locationid"));
                    plugin.put("favorite", rsPlugins.getBoolean("favorite"));
                    plugin.put("fixed", rsPlugins.getBoolean("fixed"));
                    plugin.put("pluginname", rsPlugins.getString("pluginname"));
                    plugin.put("pluginpath", rsPlugins.getString("pluginpath"));
                    plugin.put("installed_plugin", rsPlugins.getInt("installed_id"));
                    plugin.put("customdata", rsPlugins.getString("data"));
                    plugin.put("typeid", rsPlugins.getInt("typeid"));
                    plugin.put("activated", rsPlugins.getBoolean("activated"));
                    pluginsList.put(rsPlugins.getInt("id"), plugin);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get plugins from database: {}", ex.getMessage());
        }
        return pluginsList;
    }
    
    
    public static Map<Integer,Map<String,Object>> getPluginsByInstalledId(int installedId, int typeId){
        Map<Integer,Map<String,Object>> pluginsList = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT p.[id], p.[name], p.[description], p.[data], p.[location] as locationid, p.[favorite] as favorite, p.[fixed] as fixed, l.[name] as location, ip.[type] as typeid, ip.[id] as installed_id, ip.[active] as activated, ip.[name] as pluginname, ip.[path] as pluginpath "
                                                                       + "FROM plugins p "
                                                                 + "LEFT JOIN installed_plugins ip ON ip.id=p.installed_plugin "
                                                                 + "LEFT JOIN locations l ON l.id=p.location "
                                                                      + "WHERE ip.id=? AND ip.type=?");
            prep.setInt(1, installedId);
            prep.setInt(2, typeId);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    Map<String, Object> plugin = new HashMap<>();
                    plugin.put("name", rsPlugins.getString("name"));
                    plugin.put("description", rsPlugins.getString("description"));
                    plugin.put("location", rsPlugins.getString("location"));
                    plugin.put("locationid", rsPlugins.getInt("locationid"));
                    plugin.put("favorite", rsPlugins.getBoolean("favorite"));
                    plugin.put("fixed", rsPlugins.getBoolean("fixed"));
                    plugin.put("pluginname", rsPlugins.getString("pluginname"));
                    plugin.put("pluginpath", rsPlugins.getString("pluginpath"));
                    plugin.put("installed_plugin", rsPlugins.getInt("installed_id"));
                    plugin.put("customdata", rsPlugins.getString("data"));
                    plugin.put("typeid", rsPlugins.getInt("typeid"));
                    plugin.put("activated", rsPlugins.getBoolean("activated"));
                    pluginsList.put(rsPlugins.getInt("id"), plugin);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get plugins from database: {}", ex.getMessage());
        }
        return pluginsList;
    }
    
    /**
     * Returns plugins based on type id
     * @param installedId
     * @param typeId
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getInstalledPluginsByTypeId(int typeId){
        Map<Integer,Map<String,Object>> pluginsList = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ip.[type] as typeid, ip.[id] as installed_id, ip.[active] as activated, ip.[name] as pluginname, ip.[path] as pluginpath "
                                                                       + "FROM installed_plugins ip "
                                                                      + "WHERE ip.type=?");
            prep.setInt(1, typeId);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    Map<String, Object> plugin = new HashMap<>();
                    plugin.put("pluginname", rsPlugins.getString("pluginname"));
                    plugin.put("pluginpath", rsPlugins.getString("pluginpath"));
                    plugin.put("installed_plugin", rsPlugins.getInt("installed_id"));
                    plugin.put("typeid", rsPlugins.getInt("typeid"));
                    plugin.put("activated", rsPlugins.getBoolean("activated"));
                    pluginsList.put(rsPlugins.getInt("installed_id"), plugin);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get plugins from database: {}", ex.getMessage());
        }
        return pluginsList;
    }
    
    /**
     * Check if a specific plugin type id belongs to the specified installed plugin id.
     * @param installedId
     * @param typeId
     * @return 
     */
    public static boolean pluginTypeHasInstalledId(int installedId, int typeId){
        boolean result = false;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ip.[id] "
                                                                       + "FROM installed_plugins ip "
                                                                      + "WHERE ip.id=? AND ip.type=? LIMIT 1");
            prep.setInt(1, installedId);
            prep.setInt(2, typeId);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                rsPlugins.next();
                if(rsPlugins.getRow()>0) result = true;
            }
        } catch (SQLException ex) {
            LOG.error("Could not check if installed id '{}' belongs to type '{}'. Reason: {}", installedId, typeId, ex.getMessage());
        }
        return result;
    }
    
    /**
     * Returns a list of active plugins based on plugin type.
     * @param typeId
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getPlugins(int typeId){
        return getPlugins(typeId, true);
    }
    
    /**
     * Returns a full list of added plugins for running.
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getPluginCollection(){
        Map<Integer,Map<String,Object>> plugins = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT p.[id], p.[name], p.[description], p.[data], p.[location] as locationid, p.[favorite] as favorite, p.[fixed] as fixed, l.[name] as location, ip.[type] as typeid, ip.[id] as installed_id, ip.[active] as activated, ip.[name] as pluginname, ip.[path] as pluginpath "
                                                                       + "FROM plugins p "
                                                                 + "LEFT JOIN installed_plugins ip ON ip.id=p.installed_plugin "
                                                                 + "LEFT JOIN locations l ON l.id=p.location "); 
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    Map<String, Object> plugin = new HashMap<>();
                    plugin.put("name", rsPlugins.getString("name"));
                    plugin.put("description", rsPlugins.getString("description"));
                    plugin.put("location", rsPlugins.getString("location"));
                    plugin.put("locationid", rsPlugins.getInt("locationid"));
                    plugin.put("favorite", rsPlugins.getBoolean("favorite"));
                    plugin.put("fixed", rsPlugins.getBoolean("fixed"));
                    plugin.put("pluginname", rsPlugins.getString("pluginname"));
                    plugin.put("pluginpath", rsPlugins.getString("pluginpath"));
                    plugin.put("installed_plugin", rsPlugins.getInt("installed_id"));
                    plugin.put("customdata", rsPlugins.getString("data"));
                    plugin.put("typeid", rsPlugins.getInt("typeid"));
                    plugin.put("activated", rsPlugins.getBoolean("activated"));
                    plugins.put(rsPlugins.getInt("id"), plugin);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get plugins collection from database: {}", ex.getMessage());
        }
        return plugins;
    }
    
    public static List<Map<String,Object>> getFullInstalledPluginCollection(){
        List<Map<String,Object>> plugins = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ip.[type] as typeid, ip.[id], ip.[active] as activated, ip.[name] as pluginname, pt.[name] as typename, ipa.[name] as packagename, ipa.[version] as packageversion "
                                                                       + "FROM installed_plugins ip "
                                                                       + "LEFT JOIN plugin_types pt ON pt.id=ip.type "
                                                                       + "LEFT JOIN installed_packages ipa ON ipa.id=ip.package "
                                                                       + "ORDER BY pt.[name], ip.[name] ASC"); 
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    Map<String, Object> plugin = new HashMap<>();
                    plugin.put("pluginname", rsPlugins.getString("pluginname"));
                    plugin.put("id", rsPlugins.getInt("id"));
                    plugin.put("typeid", rsPlugins.getInt("typeid"));
                    plugin.put("typename", rsPlugins.getString("typename"));
                    plugin.put("packagename", rsPlugins.getString("packagename"));
                    plugin.put("packageversion", rsPlugins.getString("packageversion"));
                    plugin.put("activated", rsPlugins.getBoolean("activated"));
                    plugins.add(plugin);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not get plugins collection from database: {}", ex.getMessage());
        }
        return plugins;
    }    
    
    /**
     * Returns a single plugin.
     * @param pluginId
     * @return 
     */
    public static Map<String,Object> getPlugin(int pluginId){
        Map<String,Object> plugin = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT p.[id], p.[name], p.[description], p.[data], p.[location] as locationid, p.[favorite] as favorite, p.[fixed] as fixed, l.[name] as location, ip.[type] as typeid, ip.[id] as installed_id, ip.[active] as activated, ip.[name] as pluginname, ip.[path] as pluginpath "
                                                                       + "FROM plugins p "
                                                                 + "LEFT JOIN installed_plugins ip ON ip.id=p.installed_plugin "
                                                                 + "LEFT JOIN locations l ON l.id=p.location "
                                                                      + "WHERE p.id=? LIMIT 1")){
            prep.setInt(1, pluginId);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    plugin.put("name", rsPlugins.getString("name"));
                    plugin.put("description", rsPlugins.getString("description"));
                    plugin.put("location", rsPlugins.getString("location"));
                    plugin.put("locationid", rsPlugins.getInt("locationid"));
                    plugin.put("favorite", rsPlugins.getBoolean("favorite"));
                    plugin.put("fixed", rsPlugins.getBoolean("fixed"));
                    plugin.put("pluginname", rsPlugins.getString("pluginname"));
                    plugin.put("pluginpath", rsPlugins.getString("pluginpath"));
                    plugin.put("installed_plugin", rsPlugins.getInt("installed_id"));
                    plugin.put("customdata", rsPlugins.getString("data"));
                    plugin.put("typeid", rsPlugins.getInt("typeid"));
                    plugin.put("activated", rsPlugins.getBoolean("activated"));
                    LOG.debug(plugin);
                }
                rsPlugins.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not get plugin from database: {}", ex.getMessage());
        }
        return plugin;
    }
    
    /**
     * Saves custom plugin data to the database.
     * This should only be used with larger data sets or data that ain't configuration values.
     * @param pluginId
     * @param data
     * @return
     * @throws PluginServiceException 
     */
    public static int saveCustomData(int pluginId, String data) throws PluginServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE plugins SET data=? WHERE id=?")){
            prep.setString(1, data);
            prep.setInt(2, pluginId);
            return prep.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error saving plugin custom data: {}", ex.getMessage(), ex);
            throw new PluginServiceException("Error saving plugin custom data: " + ex.getMessage());
        }
    }
    
    /**
     * Saves a plugin to the db.
     * @param installed_id
     * @param name
     * @param description
     * @param locationId
     * @param favorite
     * @return
     * @throws PluginServiceException 
     */
    public static int savePlugin(int installed_id, String name, String description, int locationId, boolean favorite) throws PluginServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO plugins (name,description,location,favorite,installed_plugin) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setInt(3, locationId);
            prep.setBoolean(4, favorite);
            prep.setInt(5, installed_id);
            prep.execute();
            try (ResultSet rs = prep.getGeneratedKeys()) {
                if (rs.next()) {
                    int auto_id = rs.getInt(1);
                    return auto_id;
                } else {
                    throw new PluginServiceException("Could not determine created plugin id");
                }
            }
        } catch (SQLException ex) {
            LOG.error("Error saving plugin: {}", ex.getMessage(), ex);
            throw new PluginServiceException("Error saving plugin: " + ex.getMessage());
        }
    }
    
    /**
     * Updates a plugin
     * @param pluginId
     * @param name
     * @param description
     * @param locationId
     * @param favorite
     * @return
     * @throws PluginServiceException 
     */
    public static int updatePlugin(int pluginId, String name, String description, int locationId, boolean favorite) throws PluginServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE plugins SET name=?,description=?,location=?,favorite=? WHERE id=?")){
            prep.setString(1, name);
            prep.setString(2, description);
            prep.setInt(3, locationId);
            prep.setBoolean(4, favorite);
            prep.setInt(5, pluginId);
            return prep.executeUpdate();
        } catch (SQLException ex) {
            LOG.error("Error saving plugin: {}", ex.getMessage(), ex);
            throw new PluginServiceException("Error saving plugin: " + ex.getMessage());
        }
    }
    
    /**
     * Marks a plugin as favorite.
     * @param pluginId
     * @param favorite
     * @return
     * @throws PluginServiceException 
     */
    public static boolean setFavorite(int pluginId, boolean favorite) throws PluginServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE plugins SET favorite=? WHERE id=?")){
            prep.setBoolean(1, favorite);
            prep.setInt(2, pluginId);
            prep.executeUpdate();
            return true;
        } catch (SQLException ex) {
            LOG.error("Error setting plugin favorite plugin: {}", ex.getMessage(), ex);
            throw new PluginServiceException("Error setting plugin favorite: " + ex.getMessage());
        }
    }
    
    /**
     * Deletes a plugin from the database.
     * @param pluginId 
     * @return  
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    public static boolean removePlugin(int pluginId) throws PluginServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM plugins WHERE id=? and fixed=0;")){
            prep.setInt(1, pluginId);
            return prep.execute();
        } catch (SQLException ex) {
            LOG.error("Could not delete from database: {}", ex.getMessage());
            throw new PluginServiceException("Problem deleting plugin "+pluginId+" from database: " + ex.getMessage());
        }
    }
    
    /**
     * Returns a list if installed plugins from packages.
     * @param type
     * @param activated when true only return activated plugins, when false all the installed plugins.
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getInstalledPlugins(int type, boolean activated){
        Map<Integer,Map<String,Object>> pluginsList = new HashMap<>();
        String ifActive = ";";
        if(activated){
            ifActive = "AND ip.active=?";
        }
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ip.* "
                                                                       + "FROM installed_plugins ip "
                                                                      + "WHERE ip.type=? "+ifActive);
            prep.setInt(1, type);
            if(activated) prep.setBoolean(2, true);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    Map<String, Object> plugin = new HashMap<>();
                    plugin.put("name", rsPlugins.getString("name"));
                    plugin.put("activated", rsPlugins.getBoolean("active"));
                    pluginsList.put(rsPlugins.getInt("id"), plugin);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load installed plugins from database: {}", ex.getMessage());
        }
        return pluginsList;
    }
    
    /**
     * Return only active plugins
     * @param id
     * @return 
     */
    public static Map<Integer,Map<String,Object>> getInstalledPlugins(int type){
        return getInstalledPlugins(type, true);
    }
    
    /**
     * Return a single installed plugin.
     * @param id
     * @return 
     */
    public static Map<String,Object> getInstalledPlugin(int id){
        Map<String,Object> plugin = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ip.* "
                                                                       + "FROM installed_plugins ip "
                                                                      + "WHERE ip.id=? LIMIT 1")){
            prep.setInt(1, id);
            try (ResultSet rsPlugins = prep.executeQuery()) {
                while (rsPlugins.next()) {
                    plugin.put("name", rsPlugins.getString("name"));
                    plugin.put("activated", rsPlugins.getBoolean("active"));
                    plugin.put("id", rsPlugins.getInt("name"));
                }
            }
        } catch (SQLException ex) {
            LOG.error("Could not load installed plugin from database: {}", ex.getMessage());
        }
        return plugin;
    }
    
    /**
     * Saves the Utility Measurement plugin to the db.
     * @throws PluginServiceException 
     */
    public static void saveUtilityMeasurementPlugin() throws PluginServiceException {
        int installed_id = 2;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO plugins (name,description,location,favorite,installed_plugin) VALUES (?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            prep.setString(1, "PiDome utility measurements");
            prep.setString(2, "The default utility measurement plugin");
            prep.setInt(3, 1);
            prep.setBoolean(4, false);
            prep.setInt(5, installed_id);
            prep.execute();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(PluginsDB.class.getName()).log(Level.SEVERE, null, ex);
            LOG.error("Error saving plugin: {}", ex.getMessage(), ex);
            throw new PluginServiceException("Error saving plugin: " + ex.getMessage());
        }
    }
    
    /**
     * Sets an installed plugin as active or not.
     * @param pluginId
     * @param active
     * @return
     * @throws PluginServiceException 
     */
    public static boolean setInstalledPluginActive(int pluginId, boolean active) throws PluginServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM); 
            PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE installed_plugins SET active=? WHERE id=?")){
            prep.setBoolean(1, active);
            prep.setInt(2, pluginId);
            prep.executeUpdate();
            return true;
        } catch (SQLException ex) {
            LOG.error("Error setting plugin active: {}", ex.getMessage(), ex);
            throw new PluginServiceException("Error setting plugin active: " + ex.getMessage());
        }
    }
    
}
