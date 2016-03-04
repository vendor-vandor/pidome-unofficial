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

import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException;
import org.pidome.server.services.plugins.PluginServiceException;

/**
 *
 * @author John
 */
public interface RemotesServiceJSONRPCWrapperInterface {
    
    /**
     * Returns the available remotes.
     * @return 
     */
    public Object getRemotes();
    
    /**
     * Returns a single remote including all the config options etc.
     * @param remoteId
     * @return 
     * @throws org.pidome.server.connector.plugins.PluginException 
     */
    public Object getRemote(Long remoteId) throws PluginException;
    
    /**
     * Deletes a remote.
     * @param remoteId
     * @return
     * @throws PluginException
     * @throws PluginServiceException 
     */
    @PiDomeJSONRPCPrivileged
    public Object deleteRemote(Long remoteId) throws PluginException, PluginServiceException;
 
    /**
     * updates the remotes visual representation.
     * @param remoteId
     * @param data
     * @return
     * @throws PluginException 
     */
    @PiDomeJSONRPCPrivileged
    public Object updateRemoteVisual(Long remoteId, Object data) throws PluginException;
    
    /**
     * Presses a remote's button.
     * @param remoteId
     * @param buttonId
     * @return 
     * @throws org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException 
     */
    public Object pressButton(Long remoteId, String buttonId) throws PiDomeRemoteButtonException;
    
    /**
     * Returns a list of buttons from a remote.
     * @param remoteId
     * @return
     * @throws org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException
     */
    public Object getRemoteButtons(Long remoteId) throws PiDomeRemoteButtonException;
    
}
