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

import java.util.Map;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.services.hardware.DeviceServiceException;
import org.pidome.server.services.plugins.PluginServiceException;

/**
 *
 * @author John
 */
public interface DevicePluginServiceJSONRPCWrapperInterface {
    
    /**
     * Returns a list of known Device Plugin service plugins.
     * @return
     * @throws PluginException 
     */
    public Object getPlugins() throws PluginException;
    
    /**
     * Deletes a device plugin
     * @param pluginId
     * @return 
     * @throws org.pidome.server.services.plugins.PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deletePlugin(Long pluginId) throws PluginServiceException;
    
    /**
     * Gives the possibility to let plugins execute functions form the web interface.
     * @param pluginId
     * @param params
     * @return
     * @throws DeviceServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object pluginFunction(Long pluginId, Map<String,Object> params) throws DeviceServiceException;
    
    /**
     * Returns a presentation a plugin can show.
     * @param pluginId
     * @return
     * @throws PluginServiceException 
     */
    public Object getPresentation(Long pluginId) throws PluginServiceException;
    
    /**
     * Returns the devices bound to this plugin.
     * @param pluginId
     * @return
     * @throws PluginServiceException 
     */
    public Object getPluginDevices(Long pluginId) throws PluginServiceException;
}
