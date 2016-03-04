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

package org.pidome.server.services.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaEvent;
import org.pidome.server.connector.plugins.media.MediaEventListener;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.plugins.PluginsDB;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.automations.rule.AutomationRulesVarProxy;

/**
 *
 * @author John
 */
public class MediaPluginService extends PluginService implements MediaEventListener {
    
    static MediaPluginService me;
    
    int definedPluginId = 1;
    int definedPluginTypeId = 1;
    
    /**
     * Constructor.
     */
    protected MediaPluginService(){
        if(me!=null){
            me = this;
        }
    }
    
    /**
     * Returns instance.
     * @return 
     */
    public static MediaPluginService getInstance(){
        if(me==null){
            me = new MediaPluginService();
        }
        return me;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final int getInstalledId(){
        return definedPluginId;
    }
    
    @Override
    public int getPluginTypeId() {
        return definedPluginTypeId;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    final void startPluginHandlers(int pluginId){
        AutomationRulesVarProxy.addPluginVarBinding(String.valueOf(pluginId), ((Media)pluginsList.get(pluginId)).getCurrentStatusProperty());
        ((Media)pluginsList.get(pluginId)).addListener(me);
    }

    /**
     * Returns the amount of plugins.
     * @return 
     */
    public final int getPluginsCount(){
        return pluginsList.size();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    final void stopHandlers(int pluginId){
        AutomationRulesVarProxy.removePluginVarBinding(String.valueOf(pluginId));
        ((Media)pluginsList.get(pluginId)).removeListener(me);
    }
    
    /**
     * Returns the media plugins known and if active including object.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getPlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key)){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (Media)pluginsList.get(key));
            }
        }
        return pluginCollection;
    }
    
    /**
     * Returns only the active media plugins known.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getActivePlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        ArrayList<Integer> removeList = new ArrayList<>();
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key) && pluginsList.get(key).getRunning()){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (Media)pluginsList.get(key));
            } else {
                removeList.add(key);
            }
        }
        for (Integer key:removeList){
            pluginCollection.remove(key);            
        }
        return pluginCollection;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Media getPlugin(int pluginId){
        if(pluginsList.containsKey(pluginId)) return (Media)pluginsList.get(pluginId);
        return null;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public void handleMediaEvent(MediaEvent event) {
        try {
            int playerId = event.getSource().getPluginId();
            switch(event.getEventType()){
                case PLAYER_PLAY:
                    Map<String,Object> playerData = event.getSource().getNowPlayingData();
                    LOG.debug("Now playing data ready for broadcast: {}", playerData);
                    if ((MediaPlugin.ItemType)playerData.get("ItemType") == MediaPlugin.ItemType.AUDIO) {
                        ClientMessenger.send("MediaService","getCurrentMedia", event.getSource().getPluginLocationId(),
                          createNowPlayingDataBroadcast(playerId,
                                                        playerData.get("ItemType").toString(),
                                                        (int)playerData.get(MediaPlugin.ItemDetails.ID.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.THUMBNAIL.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.POSTER.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.TITLE.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.TITLE_ARTIST.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.ALBUM.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.ALBUM_ARTIST.toString()),
                                                        (int)playerData.get(MediaPlugin.ItemDetails.DURATION.toString()))
                        );
                    } else if ((MediaPlugin.ItemType)playerData.get("ItemType") == MediaPlugin.ItemType.VIDEO) {
                        ClientMessenger.send("MediaService","getCurrentMedia", event.getSource().getPluginLocationId(),
                          createNowPlayingDataBroadcast(playerId,
                                                        playerData.get("ItemType").toString(),
                                                        (int)playerData.get(MediaPlugin.ItemDetails.ID.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.THUMBNAIL.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.POSTER.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.TITLE.toString()),
                                                        "",
                                                        "",
                                                        "",
                                                        (int)playerData.get(MediaPlugin.ItemDetails.DURATION.toString()))
                        );
                    } else if ((MediaPlugin.ItemType)playerData.get("ItemType") == MediaPlugin.ItemType.PVR) {
                        ClientMessenger.send("MediaService","getCurrentMedia", event.getSource().getPluginLocationId(),
                          createNowPlayingDataBroadcast(playerId,
                                                        playerData.get("ItemType").toString(),
                                                        (int)playerData.get(MediaPlugin.ItemDetails.ID.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.THUMBNAIL.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.POSTER.toString()),
                                                        (String)playerData.get(MediaPlugin.ItemDetails.TITLE.toString()),
                                                        "",
                                                        "",
                                                        "",
                                                        0)
                        );
                    }
                    TriggerService.handleEvent("MEDIACOMMAND_" + playerId + "_" + event.getEventType().toString(), event.getEventType().toString());
                break;
                case PLAYER_PAUSE:
                    ClientMessenger.send("MediaService", "playerCommand", event.getSource().getPluginLocationId(), createPausePlayingDataBroadcast(playerId));
                    TriggerService.handleEvent("MEDIACOMMAND_" + playerId + "_" + event.getEventType().toString(), event.getEventType().toString());
                break;
                case PLAYER_STOP:
                    ClientMessenger.send("MediaService","playerCommand", event.getSource().getPluginLocationId(), createStopPlayingDataBroadcast(playerId));
                    TriggerService.handleEvent("MEDIACOMMAND_" + playerId + "_" + event.getEventType().toString(), event.getEventType().toString());
                break;
                case PLAYLIST:
                    ClientMessenger.send("MediaService","getPlayList", event.getSource().getPluginLocationId(), createPlaylistDataBroadcast(playerId));
                break;
                case PLAYLIST_ADD:
                    ClientMessenger.send("MediaService","addPlaylistItem", event.getSource().getPluginLocationId(), createPlaylistAddedDataBroadcast(playerId, event.getPlaylistAddedData()));
                break;
                case PLAYLIST_REMOVE:
                    ClientMessenger.send("MediaService","removePlaylistItem", event.getSource().getPluginLocationId(), createPlaylistRemovedDataBroadcast(playerId, event.getPlaylistAddedData()));
                break;
                case PLAYLIST_CLEAR:
                    ClientMessenger.send("MediaService","playlistCleared", event.getSource().getPluginLocationId(), createPlaylistClearedDataBroadcast(playerId));
                break;
            }
        } catch (Exception ex){
            LOG.error("Problem creating media broadcast: {}", ex.getMessage(), ex);
        }
    }
    
    //// static methods
    /**
     * Creates a now playing broadcast
     * @param id
     * @param itemId
     * @param type
     * @param title
     * @param titleArtist
     * @param album
     * @param albumArtist
     * @param duration
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createNowPlayingDataBroadcast(final int id, final String type, final int itemId, final String thumbnail, final String poster, final String title, final String titleArtist, final String album, final String albumArtist, final int duration) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("id", id);
            put("thumbnail", thumbnail);
            put("poster", poster);
            put("itemtype", type);
            put("itemid", itemId);
            put("title", title);
            put("title_artist", titleArtist);
            put("album", album);
            put("album_artist", albumArtist);
            put("duration", duration);
        }};
    }
    
    /**
     * Creates a stop method
     * @param id
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createStopPlayingDataBroadcast(final int id) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("id", id);
            put("command", "STOP");
        }};
    }
    
    /**
     * Creates a pause command
     * @param id
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createPausePlayingDataBroadcast(final int id) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("id", "PAUSE");
        }};
    }
    
    /**
     * creates a broadcast telling for which plugin a playlist has been created.
     * @param id
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createPlaylistDataBroadcast(final int id) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("id", id);
        }};
    }
    
    /**
     * creates a broadcast telling for which plugin the playlist has been updated with an added item.
     * @param id
     * @param data
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createPlaylistAddedDataBroadcast(final int id, final Map<MediaPlugin.PlayListItem,Object> data) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("itemid", data.get(MediaPlugin.PlayListItem.ID));
            put("id", id);
            put("playlist", data.get(MediaPlugin.PlayListItem.PLAYLIST_ID));
            put("pos", data.get(MediaPlugin.PlayListItem.PLAYLIST_POS));
            put("itemtype", data.get(MediaPlugin.PlayListItem.ITEM_TYPE).toString());
            put("title", data.get(MediaPlugin.PlayListItem.TITLE));
            put("duration", data.get(MediaPlugin.PlayListItem.DURATION));
        }};
    }
    
    /**
     * Create a broadcast for an item removed from the playlist.
     * @param id
     * @param data
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createPlaylistRemovedDataBroadcast(final int id, final Map<MediaPlugin.PlayListItem,Object> data) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("pos", data.get(MediaPlugin.PlayListItem.PLAYLIST_POS));
            put("id", id);
        }};
    }
    
    /**
     * Create a broadcast for a playlist cleared.
     * @param id
     * @return
     * @throws PidomeJSONRPCException 
     */
    static Map createPlaylistClearedDataBroadcast(final int id) throws PidomeJSONRPCException{
        return new HashMap<String,Object>(){{
            put("id", id);
        }};
    }
    
    @Override
    public String getServiceName() {
        return "Media plugin service";
    }
    
}
