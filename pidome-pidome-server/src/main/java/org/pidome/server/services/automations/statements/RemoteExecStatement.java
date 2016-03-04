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

package org.pidome.server.services.automations.statements;

import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.pidomeremote.PiDomeRemoteButtonException;
import org.pidome.server.services.plugins.RemotesPluginService;

/**
 *
 * @author John
 */
public class RemoteExecStatement extends AutomationStatement {

    private int remoteId;
    private String buttonId; 
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(RemoteExecStatement.class);
    
    public RemoteExecStatement(int remoteId, String buttonId){
        super(new StringBuilder("RemoteStatement_").append(remoteId).append("_").append(buttonId).toString());
        this.remoteId = remoteId;
        this.buttonId    = buttonId;
    }
    
    @Override
    public boolean run() {
        try {
            RemotesPluginService.getInstance().getPlugin(remoteId).handleButton(buttonId);
        } catch (PluginException | PiDomeRemoteButtonException ex) {
            LOG.error("Could not press button {} on remote id {}: ", buttonId, remoteId, ex.getMessage());
        }
        return true;
    }

    @Override
    public void destroy() {
        /// Not used
    }
    
}
