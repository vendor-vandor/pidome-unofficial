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

package org.pidome.server.services.http.rpc;

import org.pidome.server.services.plugins.PluginServiceException;

/**
 *
 * @author John
 */
public interface PluginServiceJSONRPCWrapperInterface {
    
    /**
     * Get a list of available plugins
     * @return 
     */
    public Object getPlugins();
    
    /**
     * Returns a list of installed plugins.
     * @return 
     */
    public Object getInstalledPlugins();
    
    /**
     * Restart a plugin.
     * @param pluginId
     * @return 
     */
    public Object restartPlugin(Long pluginId);
    
    /**
     * Set a favorite plug in.
     * @param pluginId
     * @param favorite
     * @return 
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    public Object setFavorite(Long pluginId, Boolean favorite) throws PluginServiceException;
    
    /**
     * Activates or in-activates specific a installed plugin.
     * @param pluginTypeId
     * @param active
     * @return
     * @throws PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object setInstalledActive(Long pluginTypeId, Boolean active) throws PluginServiceException;
    
    /**
     * Deletes a plugin.
     * Does not remove an installed plugin.
     * @param pluginId
     * @return
     * @throws PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deletePlugin(Long pluginId) throws PluginServiceException;
    
}
