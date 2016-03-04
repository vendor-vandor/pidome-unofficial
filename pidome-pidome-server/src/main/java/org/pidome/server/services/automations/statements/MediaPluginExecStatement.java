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
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.services.plugins.MediaPluginService;

/**
 *
 * @author John
 */
public class MediaPluginExecStatement  extends AutomationStatement {

    private int pluginId;
    private String action; 
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(RemoteExecStatement.class);
    
    public MediaPluginExecStatement(int pluginId, String action){
        super(new StringBuilder("MediaStatement_").append(pluginId).append("_").append(action).toString());
        this.pluginId = pluginId;
        this.action   = action;
    }
    
    @Override
    public boolean run() {
        try {
            switch(action){
                case "PLAY":
                    MediaPluginService.getInstance().getPlugin(pluginId).handlePlayerCommand(MediaPlugin.PlayerCommand.PLAY);
                break;
                case "PAUSE":
                    MediaPluginService.getInstance().getPlugin(pluginId).handlePlayerCommand(MediaPlugin.PlayerCommand.PAUSE);
                break;
                case "STOP":
                    MediaPluginService.getInstance().getPlugin(pluginId).handlePlayerCommand(MediaPlugin.PlayerCommand.STOP);
                break;
            }
        } catch (MediaException ex) {
            LOG.error("Could not handle media command {} in plugin id {}: ", action, pluginId, ex.getMessage());
        }
        return true;
    }

    @Override
    public void destroy() {
        /// Not used
    }
    
}