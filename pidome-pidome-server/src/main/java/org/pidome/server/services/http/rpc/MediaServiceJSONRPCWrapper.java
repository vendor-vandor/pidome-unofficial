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

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.services.plugins.MediaPluginService;
import static org.pidome.server.services.http.rpc.AbstractRPCMethodExecutor.LOG;

/**
 *
 * @author John Sirach
 */
public class MediaServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements MediaServiceJSONRPCWrapperInterface {
    
    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("serverCommand", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("command", "");}});
                    }
                });
                put("playerCommand", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("command", "");}});
                    }
                });
                put("playPlaylistItem", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("playlist", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("pos", 0L);}});
                        put(3,new HashMap<String,Object>(){{put("type", "");}});
                    }
                });
                put("removePlaylistItem", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("playlist", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("pos", 0L);}});
                    }
                });
                put("getCapabilities", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("type", "");}});
                    }
                });
                put("getPlayList", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getCurrentMedia", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getVideoPlaylists", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getAudioPlaylists", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getPVRChannelSets", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getPVRChannels", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("setid", 0L);}});
                    }
                });
                put("playPlaylist", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("playlistid", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("type", "");}});
                        put(3,new HashMap<String,Object>(){{put("playlistfile", "");}});
                    }
                });
                put("getPlugins", null);
                put("getFavorites", null);
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object serverCommand(Long mediaId, String command) throws MediaException {
        int id = mediaId.intValue();
        Media plugin = MediaPluginService.getInstance().getPlugin(id);
        try {
            if(this.getCaller().getRole().hasLocationAccess(plugin.getPluginLocationId())){
                switch (command) {
                    case "UP":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.UP, (Object) null);
                        break;
                    case "DOWN":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.DOWN, (Object) null);
                        break;
                    case "LEFT":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.LEFT, (Object) null);
                        break;
                    case "RIGHT":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.RIGHT, (Object) null);
                        break;
                    case "CONFIRM":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.CONFIRM, (Object) null);
                        break;
                    case "OSD":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.OSD, (Object) null);
                        break;
                    case "BACK":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.BACK, (Object) null);
                        break;
                    case "HOME":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.HOME, (Object) null);
                        break;
                    case "VOLUP":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.VOLUP, (Object) null);
                        break;
                    case "VOLDOWN":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.VOLDOWN, (Object) null);
                        break;
                    case "MUTE":
                        plugin.handleServerCommand(MediaPlugin.ServerCommand.MUTE, (Object) null);
                        break;
                    default:
                        return false;
                }
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object playerCommand(Long mediaId, String command) throws MediaException {
        int id = mediaId.intValue();
        Media plugin = MediaPluginService.getInstance().getPlugin(id);
        try {
            if(this.getCaller().getRole().hasLocationAccess(plugin.getPluginLocationId())){
                switch (command) {
                    case "STOP":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.STOP, (Object) null);
                        break;
                    case "PLAY":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PLAY, (Object) null);
                        break;
                    case "PAUSE":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PAUSE, (Object) null);
                        break;
                    case "PREV":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.PREV, (Object) null);
                        break;
                    case "NEXT":
                        plugin.handlePlayerCommand(MediaPlugin.PlayerCommand.NEXT, (Object) null);
                        break;
                }
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object playPlaylistItem(Long mediaId, Long playlistId, Long itemId, String type) throws MediaException {
        int id = mediaId.intValue();
        Object[] playFromPlayList = new Object[3];
        playFromPlayList[0] = (int) playlistId.intValue();
        playFromPlayList[1] = (int) itemId.intValue();
        playFromPlayList[2] = (String) type;
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(id).getPluginLocationId())){
                MediaPluginService.getInstance().getPlugin(id).handlePlayerCommand(MediaPlugin.PlayerCommand.PLAYLISTITEM, playFromPlayList);
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object removePlaylistItem(Long mediaId, Long playlistId, Long itemId) throws MediaException {
        int id = mediaId.intValue();
        Object[] removeFromPlayList = new Object[2];
        removeFromPlayList[0] = (int) playlistId.intValue();
        removeFromPlayList[1] = (int) itemId.intValue();
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(id).getPluginLocationId())){
                MediaPluginService.getInstance().getPlugin(id).handlePlaylistCommand(MediaPlugin.PlaylistCommand.REMOVE, removeFromPlayList);
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getCapabilities(Long mediaId, String type) throws MediaException {
        HashMap<String,String> capabilities = new HashMap<>();
        List<Map<String,String>> capList = new ArrayList<>();
        switch(type){
            case "ServerCommand":
                capabilities.put("HOME", "Go Home");
                capabilities.put("MUTE", "Mute/Unmute sound");
            break;
            case "PlayerCommand":
                capabilities.put("PLAYER_STOP", "Stop");
                capabilities.put("PLAYER_PLAY", "Play");
                capabilities.put("PLAYER_PAUSE", "Pause");
            break;
            default:
                ///capabilities.put("ServerCommand", "Appliance Command");
                capabilities.put("PlayerCommand", "Player Command");
            break;
        }
        capabilities.entrySet().stream().map((entry) -> {     
            Map<String,String>set = new HashMap<>();
            set.put("id", entry.getKey());
            set.put("name", entry.getValue());
            return set;
        }).forEach((set) -> {
            capList.add(set);
        });
        return capList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getPlayList(Long mediaId) throws MediaException {
        int id = mediaId.intValue();
        ArrayList<Map<String, Object>> newList = new ArrayList();
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(id).getPluginLocationId())){
                Map<Integer,Map<MediaPlugin.PlayListItem, Object>> playlist = MediaPluginService.getInstance().getPlugin(id).getPlayList();
                for(int key:playlist.keySet()){
                    Map<String,Object> item = new HashMap<>();
                    item.put("id", playlist.get(key).get(MediaPlugin.PlayListItem.ID));
                    item.put("itemtype", playlist.get(key).get(MediaPlugin.PlayListItem.ITEM_TYPE).toString());
                    item.put("title", playlist.get(key).get(MediaPlugin.PlayListItem.TITLE));
                    item.put("pos", playlist.get(key).get(MediaPlugin.PlayListItem.PLAYLIST_POS));
                    item.put("playlist", playlist.get(key).get(MediaPlugin.PlayListItem.PLAYLIST_ID));
                    item.put("duration", playlist.get(key).get(MediaPlugin.PlayListItem.DURATION));
                    newList.add(item);
                }
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
        return newList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getCurrentMedia(Long mediaId) throws MediaException {
        int id = mediaId.intValue();
        Map<String,Object> mediaDetails = new HashMap<>();
        
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(id).getPluginLocationId())){
                
                mediaDetails.put(MediaPlugin.ItemDetails.ALBUM.toString().toLowerCase(), "");
                mediaDetails.put(MediaPlugin.ItemDetails.ALBUM_ARTIST.toString().toLowerCase(), "");
                mediaDetails.put(MediaPlugin.ItemDetails.TITLE.toString().toLowerCase(), "");
                mediaDetails.put(MediaPlugin.ItemDetails.TITLE_ARTIST.toString().toLowerCase(), "");
                mediaDetails.put(MediaPlugin.ItemDetails.THUMBNAIL.toString().toLowerCase(), "");
                mediaDetails.put(MediaPlugin.ItemDetails.POSTER.toString().toLowerCase(), "");
                mediaDetails.put(MediaPlugin.ItemDetails.DURATION.toString().toLowerCase(), 0);
                mediaDetails.put(MediaPlugin.ItemDetails.ID.toString().toLowerCase(), 0);
                
                for(String key: MediaPluginService.getInstance().getPlugin(id).getNowPlayingData().keySet()){
                    if(key.equals("ItemType")){
                        mediaDetails.put(key.toLowerCase(), MediaPluginService.getInstance().getPlugin(id).getNowPlayingData().get(key).toString());
                    } else {
                        mediaDetails.put(key.toLowerCase(), MediaPluginService.getInstance().getPlugin(id).getNowPlayingData().get(key));
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
        
        return mediaDetails;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getAudioPlaylists(Long mediaId) throws MediaException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPluginLocationId())){
                return MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getAudioPlaylists();
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getVideoPlaylists(Long mediaId) throws MediaException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPluginLocationId())){
                return MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getVideoPlaylists();
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getPVRChannelSets(Long mediaId) throws MediaException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPluginLocationId())){
                return MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPVRChannelSets();
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getPVRChannels(Long mediaId, Long setId) throws MediaException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPluginLocationId())){
                return MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPVRChannels(setId);
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object playPlaylist(Long mediaId, Long playlistId, String type, String playlistFile) throws MediaException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(MediaPluginService.getInstance().getPlugin(mediaId.intValue()).getPluginLocationId())){
                MediaPluginService.getInstance().getPlugin(mediaId.intValue()).playPlaylist(playlistId, type, playlistFile);
                return true;
            } else {
                throw new MediaException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
            throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getPlugins() throws MediaException {
        Map<Integer,Map<String,Object>> pluginsList = MediaPluginService.getInstance().getPlugins();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(int key: pluginsList.keySet()){
            try {
                if(this.getCaller().getRole().hasLocationAccess((int)pluginsList.get(key).get("locationid"))){
                    Map<String,Object> item = new HashMap<>();
                    item.put("id", key);
                    item.put("name", pluginsList.get(key).get("name"));
                    item.put("description", pluginsList.get(key).get("description"));
                    item.put("locationid", pluginsList.get(key).get("locationid"));
                    item.put("locationname", pluginsList.get(key).get("location"));
                    item.put("pluginname", pluginsList.get(key).get("pluginname"));
                    try {
                        item.put("active", MediaPluginService.getInstance().getPlugin(key).getRunning());
                    } catch (Exception ex){
                        item.put("active", false);
                    }
                    newList.add(item);
                }
            } catch (Exception ex) {
                LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
                throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
            }
        }
        return newList;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getFavorites() throws MediaException {
        Map<Integer,Map<String,Object>> pluginsList = MediaPluginService.getInstance().getPlugins();
        ArrayList<Map<String,Object>> newList = new ArrayList();
        for(int key: pluginsList.keySet()){
            try {
                if(this.getCaller().getRole().hasLocationAccess((int)pluginsList.get(key).get("locationid"))){
                    if((boolean)pluginsList.get(key).get("favorite") == true){
                        Map<String,Object> item = new HashMap<>();
                        item.put("id", key);
                        item.put("name", pluginsList.get(key).get("name"));
                        item.put("description", pluginsList.get(key).get("description"));
                        item.put("locationid", pluginsList.get(key).get("locationid"));
                        item.put("locationname", pluginsList.get(key).get("location"));
                        item.put("pluginname", pluginsList.get(key).get("pluginname"));
                        try {
                            item.put("active", MediaPluginService.getInstance().getPlugin(key).getRunning());
                        } catch (Exception ex){
                            item.put("active", false);
                        }
                        newList.add(item);
                    }
                }
            } catch (Exception ex) {
                LOG.error("problem executing for {}: {}", this.getCaller().getLoginName(), ex.getMessage());
                throw new MediaException("problem executing for: " + this.getCaller().getLoginName());
            }
        }
        return newList;
    }
    
}
