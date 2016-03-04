/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.Map;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.services.plugins.PluginServiceException;

/**
 *
 * @author John
 */
public interface DataModifierServiceJSONRPCWrapperInterface {
    
    /**
     * Returns a list of data modifiers and suppliers.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getPlugins();
    
    /**
     * Returns a list of installed plugins.
     * @return 
     */
    @PiDomeJSONRPCPrivileged
    public Object getInstalledPlugins();
    
    /**
     * Returns a set of plugin options for the installed plugin id.
     * @param installedId
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getInstalledPluginOptions(Number installedId) throws PluginException;
    
    /**
     * Returns a single plugin.
     * @return
     * @throws PluginException 
     */
    @PiDomeJSONRPCPrivileged
    public Object getPlugin(Number pluginId) throws PluginException;
    
    /**
     * Saves a plugin in the database.
     * @param installedId
     * @param name
     * @param description
     * @param optionsSet
     * @return 
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean savePlugin(Number installedId, String name, String description, Map<String,String> optionsSet) throws PluginServiceException;
    
    /**
     * Updates an plugin.
     * @param pluginId
     * @param name
     * @param description
     * @param optionsSet
     * @return 
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean updatePlugin(Number pluginId, String name, String description, Map<String,String> optionsSet) throws PluginServiceException;
    
    /**
     * Deletes a plugin.
     * @param pluginId
     * @return
     * @throws PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean deletePlugin(Number pluginId) throws PluginServiceException;
    
}
