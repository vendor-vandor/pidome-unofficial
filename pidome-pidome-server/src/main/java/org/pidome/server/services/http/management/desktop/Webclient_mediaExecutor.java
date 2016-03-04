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

package org.pidome.server.services.http.management.desktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public class Webclient_mediaExecutor extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_mediaExecutor.class);

    @Override
    public String render() {
        
        if(!getDataMap.isEmpty() && getDataMap.containsKey("mediaid") && getDataMap.containsKey("action")){
            try {
                int mediaId = Integer.parseInt(getDataMap.get("mediaid"));
                Media plugin = MediaPluginService.getInstance().getPlugin(mediaId);
                switch(getDataMap.get("action")){
                    //// Server commands
                    case "ServerCommand.UP":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.UP, (Object) null);
                        break;
                    case "ServerCommand.DOWN":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.DOWN, (Object) null);
                        break;
                    case "ServerCommand.LEFT":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.LEFT, (Object) null);
                        break;
                    case "ServerCommand.RIGHT":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.RIGHT, (Object) null);
                        break;
                    case "ServerCommand.CONFIRM":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.CONFIRM, (Object) null);
                        break;
                    case "ServerCommand.OSD":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.OSD, (Object) null);
                        break;
                    case "ServerCommand.BACK":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.BACK, (Object) null);
                        break;
                    case "ServerCommand.HOME":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.HOME, (Object) null);
                        break;
                    case "ServerCommand.VOLUP":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.VOLUP, (Object) null);
                        break;
                    case "ServerCommand.VOLDOWN":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.VOLDOWN, (Object) null);
                        break;
                    case "ServerCommand.MUTE":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.MUTE, (Object) null);
                        break;
                    //// Player commands
                    case "PlayerCommand.STOP":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.STOP, (Object) null);
                        break;
                    case "PlayerCommand.PLAY":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PLAY, (Object) null);
                        break;
                    case "PlayerCommand.PAUSE":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PAUSE, (Object) null);
                        break;
                    case "PlayerCommand.PREV":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PREV, (Object) null);
                        break;
                    case "PlayerCommand.NEXT":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.NEXT, (Object) null);
                        break;
                    case "PlayerCommand.PLAYLISTITEM":
                        Object[] playFromPlayList = new Object[3];
                        playFromPlayList[0] = (int)Integer.parseInt(getDataMap.get("playlistid"));
                        playFromPlayList[1] = (int)Integer.parseInt(getDataMap.get("itemid"));
                        playFromPlayList[2] = (String)getDataMap.get("type");
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PLAYLISTITEM, playFromPlayList);
                        break;
                    case "PlaylistCommand.REMOVE":
                        Object[] removeFromPlayList = new Object[2];
                        removeFromPlayList[0] = (int)Integer.parseInt(getDataMap.get("playlistid"));
                        removeFromPlayList[1] = (int)Integer.parseInt(getDataMap.get("itemid"));
                        plugin.handlePlaylistCommand(MediaPlugin.PlaylistCommand.REMOVE, removeFromPlayList);
                        break;
                }
            } catch (MediaException ex) {
                LOG.error("Could not execute command: {}, contents: ", getDataMap.containsKey("action"), getDataMap);
            }
        }
        return "{\"result\": true}";
    }
}
