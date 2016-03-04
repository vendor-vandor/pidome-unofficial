/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.media;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.hooks.MediaHook;
import static org.pidome.server.connector.plugins.media.MediaPlugin.ItemType;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public abstract class Media extends PluginBase implements MediaPlugin {
    
    List<MediaEventListener> listeners = new ArrayList();
    
    Map<String, Object> currentPlayingData = new HashMap<>();
    
    Map<Integer,Map<PlayListItem, Object>> currentPlaylistData = new TreeMap<>();
    
    ObjectPropertyBindingBean currentPlayerStatus = new ObjectPropertyBindingBean();
    
    /**
     * For mapping original video library fields to PiDome fields.
     * @see org.pidome.pluginconnector.media.Media.VideoLibraryItemsMapping
     */
    Map<String,String> videoLibraryMapping = new HashMap<>();
    /**
     * For mapping original audio library fields to PiDome fields.
     * @see org.pidome.pluginconnector.media.Media.AudioLibraryItemsMapping
     */
    Map<String,String> audioLibraryMapping = new HashMap<>();
    
    /**
     * True when video playing is supported
     */
    boolean videoSupported = false;
    /**
     * True when audio playing is supported
     */
    boolean audioSupported = false;
    
    //// Creating url mappings for the client so the client knows which parameter is what corresponding to the url parameters
    public abstract Map<String,String> videoLibraryUrlMapping() throws UnsupportedOperationException;
    public abstract Map<String,String> audioLibraryUrlMapping() throws UnsupportedOperationException;
    
    public final ObjectPropertyBindingBean getCurrentStatusProperty(){
        return currentPlayerStatus;
    }
    
    /**
     * Adds a listener.
     * @param l 
     */
    public final void addListener(MediaEventListener l){
        if(!listeners.contains(l)) listeners.add(l);
    }
    
    /**
     * Removes a listener;
     * @param l 
     */
    public final void removeListener(MediaEventListener l){
        if(listeners.contains(l)) listeners.remove(l);
    }
    
    /**
     * Video command to be handled by the plugin.
     * @param command
     * @param supplement
     * @throws org.pidome.server.connector.plugins.media.MediaException
     */
    public abstract void handleVideoCommand(VideoCommand command, Object... supplement) throws MediaException;

    /**
     * Audio command to be handled by the plugin.
     * @param command
     * @param supplement
     * @throws org.pidome.server.connector.plugins.media.MediaException
     */
    public abstract void handleAudioCommand(AudioCommand command, Object... supplement) throws MediaException;
    
    /**
     * Server commands to be handled by the plugin.
     * @param command
     * @param supplement
     * @throws org.pidome.server.connector.plugins.media.MediaException
     */
    public abstract void handleServerCommand(ServerCommand command, Object... supplement) throws MediaException;

    /**
     * Player commands to be handled by the plugin.
     * @param command
     * @param supplement
     * @throws org.pidome.server.connector.plugins.media.MediaException
     */
    public abstract void handlePlayerCommand(PlayerCommand command, Object... supplement) throws MediaException;
    
    /**
     * Handles commands for playlists.
     * @param command
     * @param supplement
     * @throws MediaException 
     */
    public abstract void handlePlaylistCommand(PlaylistCommand command, Object... supplement) throws MediaException;
    
    /**
     * Returns a video playlist list.
     * @return
     * @throws MediaException 
     */
    public abstract Object getVideoPlaylists() throws MediaException;
    
    /**
     * Returns a PVR channels set list.
     * @return
     * @throws MediaException 
     */
    public abstract Object getPVRChannelSets() throws MediaException;
    
    /**
     * Returns a PVR channels list from set.
     * @return
     * @throws MediaException 
     */
    public abstract Object getPVRChannels(Long setId) throws MediaException;
    
    /**
     * Returns an audio playlist list.
     * @return
     * @throws MediaException 
     */
    public abstract Object getAudioPlaylists() throws MediaException;
    
    /**
     * Plays a playlist based on a playlist id or playlist file.
     * If playlist id is null, the file will be played.
     * @param playlistId
     * @param playlistType
     * @param playlistFile
     * @throws MediaException 
     */
    public abstract void playPlaylist(Long playlistId, String playlistType, String playlistFile) throws MediaException;
    
    /**
     * Sets the current playing item.
     * @param item 
     */
    public final void setCurrentPlayingData(MediaItem item){
        currentPlayingData.clear();
        currentPlayingData.put("ItemType", item.getItemType());
        currentPlayingData.put(ItemDetails.ID.toString(), item.getId());
        currentPlayingData.put(ItemDetails.TITLE.toString(), item.getTitle());
        currentPlayingData.put(ItemDetails.DURATION.toString(), item.getDuration());
        currentPlayingData.put(ItemDetails.THUMBNAIL.toString(), item.getThumbnail());
        currentPlayingData.put(ItemDetails.POSTER.toString(), item.getPoster());
        if(item.getItemType()==ItemType.AUDIO){
            currentPlayingData.put(ItemDetails.ALBUM.toString(), ((MediaAudioItem)item).getAlbum());
            currentPlayingData.put(ItemDetails.ALBUM_ARTIST.toString(), ((MediaAudioItem)item).getAlbumArtist());
            currentPlayingData.put(ItemDetails.TITLE_ARTIST.toString(), ((MediaAudioItem)item).getArtist());
        }
        currentPlayerStatus.setValue("PLAY");
        sendNewPlayingData(MediaEvent.EventType.PLAYER_PLAY);
    }
    
    /**
     * Broadcast player paused.
     */
    public final void sendPlayerPaused(){
        currentPlayerStatus.setValue("PAUSE");
        sendNewPlayingData(MediaEvent.EventType.PLAYER_PAUSE);
    }
    
    /**
     * Broadcast new playing data.
     * @param type
     * @param data 
     */
    final void sendNewPlayingData(MediaEvent.EventType type, Map<PlayListItem,Object> data){
        final MediaEvent event = new MediaEvent(this, type);
        event.setPlaylistAddedData(data);
        Iterator _listeners = listeners.iterator();
        while (_listeners.hasNext()) {
            ((MediaEventListener) _listeners.next()).handleMediaEvent(event);
        }
    }
    
    /**
     * Broadcast new playing data.
     * @param type 
     */
    final void sendNewPlayingData(MediaEvent.EventType type){
        final MediaEvent event = new MediaEvent(this, type);
        Iterator _listeners = listeners.iterator();
        while (_listeners.hasNext()) {
            ((MediaEventListener) _listeners.next()).handleMediaEvent(event);
        }
        MediaHook.handleMediaEvent(event);
    }
    
    /**
     * Clears the current playing data.
     */
    public final void clearNowPlayingData(){
        currentPlayingData.clear();
        currentPlayerStatus.setValue("STOP");
        sendNewPlayingData(MediaEvent.EventType.PLAYER_STOP);
    }
    
    /**
     * Returns the current playing data.
     * Check the data type in the type field.
     * @return 
     */
    public final Map<String,Object> getNowPlayingData(){
        if(!currentPlayingData.isEmpty()){
            return currentPlayingData;
        }
        return new HashMap<>();
    }
    
    /**
     * Adds an item to the playlist. 
     * @param item
     */
    public final void addToPlayList(MediaItem item){
        addItemToPlayList(item);
    }
    
    /**
     * Adds a media item to the playlist
     * @param item 
     */
    final void addItemToPlayList(MediaItem item){
        if(!currentPlaylistData.containsKey(item.getPlaylistPos())){
            Map<PlayListItem,Object> details = new HashMap<>();
            details.put(PlayListItem.ITEM_TYPE, item.getItemType());
            details.put(PlayListItem.TITLE, item.getTitle());
            details.put(PlayListItem.ID, item.getId());
            details.put(PlayListItem.PLAYLIST_ID, item.getPlaylistId());
            details.put(PlayListItem.PLAYLIST_POS, item.getPlaylistPos());
            details.put(PlayListItem.DURATION, item.getDuration());
            currentPlaylistData.put(item.getPlaylistPos(), details);
            sendNewPlayingData(MediaEvent.EventType.PLAYLIST_ADD, details);
        }
    }
    
    /**
     * Adds a list of media items to the playlist.
     * @param playlist 
     */
    public final void addListToPlaylist(List<MediaItem> playlist){
        for (int i = 0; i < playlist.size(); i++) {
            addItemToPlayList(playlist.get(i));
        }
    }
    
    /**
     * Removes an item to the playlist.
     * @param playlistPos
     */
    public final void removeFromPlayList(final int playlistPos){
        Map<Integer,Map<PlayListItem, Object>> tmpMap = new HashMap<>();
        for (Map.Entry<Integer, Map<PlayListItem, Object>> entry : currentPlaylistData.entrySet()) {
            if(entry.getKey()<playlistPos){
                tmpMap.put(entry.getKey(), entry.getValue());
            } else if (entry.getKey()>playlistPos){
                entry.getValue().put(PlayListItem.PLAYLIST_POS,entry.getKey()-1);
                tmpMap.put(entry.getKey()-1, entry.getValue());
            }
        }
        currentPlaylistData.remove(playlistPos);
        currentPlaylistData.clear();
        currentPlaylistData = tmpMap;
        Map<PlayListItem,Object> data = new HashMap<PlayListItem,Object>(){{
            put(PlayListItem.PLAYLIST_POS, playlistPos);
        }};
        sendNewPlayingData(MediaEvent.EventType.PLAYLIST_REMOVE, data);
    }
    
    /**
     * Adds an item to the playlist.
     * @param playlistId
     */
    public final void clearPlayList(String playlistId){
        currentPlaylistData.clear();
        sendNewPlayingData(MediaEvent.EventType.PLAYLIST_CLEAR);
    }
    
    /**
     * Returns the current playlist.
     * @return 
     */
    public final Map<Integer,Map<PlayListItem, Object>> getPlayList(){
        return this.currentPlaylistData;
    }
    
    /**
     * Runs initialization.
     * Initialization sets library mappings.
     * @throws org.pidome.pluginconnector.media.MediaException
     */
    protected final void initialize() throws MediaException {
        try {
            videoLibraryMapping = videoLibraryUrlMapping();
            videoSupported = true;
        } catch (UnsupportedOperationException ex){
            videoSupported = false;
        }
        try {
            audioLibraryMapping = audioLibraryUrlMapping();
            audioSupported = true;
        } catch (UnsupportedOperationException ex){
            audioSupported = false;
        }
    }
    
    /**
     * Freeing memory and stopping the plugin.
     * This frees memory and asks the plugin to stop and de-initialize it.
     * @throws MediaException
     */
    protected final void deInitialize() throws MediaException {
        videoSupported = false;
        audioSupported = false;
        videoLibraryMapping = new HashMap<>();
        audioLibraryMapping = new HashMap<>();
        listeners.clear();
    }
    
}