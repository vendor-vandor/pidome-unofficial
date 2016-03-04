/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.media;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.plugins.media.MediaPluginService.PlayerCommand;
import org.pidome.client.entities.plugins.media.MediaPluginService.ServerCommand;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.BooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyBooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 *
 * @author John
 */
public class MediaPlugin implements PCCConnectionNameSpaceRPCListener {
    
    /**
     * The plugin id.
     */
    private final int pluginId;
    
    /**
     * Plugin name.
     */
    private final StringPropertyBindingBean name = new StringPropertyBindingBean();
    
    /**
     * The current location id.
     */
    private int locationId;
    
    /**
     * The current location name.
     */
    private String locationName;
    
    /**
     * The description.
     */
    private String description;
    
    /**
     * Boolean active or not property.
     */
    private BooleanPropertyBindingBean active = new BooleanPropertyBindingBean(false);
    
    /**
     * The current playing media item.
     */
    private ObjectPropertyBindingBean<MediaData> currentPlaying = new ObjectPropertyBindingBean(new MediaData());
    
    /**
     * The name of the plugin used for this media plugin.
     */
    private String pluginName;
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * List of items in the current playlist data.
     */
    private ObservableArrayListBean<MediaPlaylistItem> nowPlayingList = new ObservableArrayListBean();
    /**
     * Read only list of the current playlist data.
     */
    private ReadOnlyObservableArrayListBean<MediaPlaylistItem> readonlyNowPlayingList = new ReadOnlyObservableArrayListBean(nowPlayingList);
    
    /**
     * Single plugin constructor.
     * @param connection The client connection object.
     * @param id The id of the plugin.
     * @param name The user given name.
     */
    protected MediaPlugin(PCCConnectionInterface connection, int id, String name){
        this.connection = connection;
        pluginId = id;
        this.name.setValue(name);
    }
    
    /**
     * Returns the plugin id.
     * @return The plugin id.
     */
    public final int getPluginId(){
        return this.pluginId;
    }
    
    /**
     * Returns the plugin name set by the user (not the name of the acual plugin).
     * @return A boundable string property with the name.
     */
    public final StringPropertyBindingBean getName(){
        return this.name;
    }
    
    /**
     * Sets the location id.
     * @param locationId The id of the location.
     */
    protected final void setLocationId(int locationId){
        this.locationId = locationId;
    }

    /**
     * Returns the location id.
     * @return the id of the location.
     */
    public final int getLocationId(){
        return this.locationId;
    }
    
    /**
     * Sets the temporary location name as set when the plugin was loaded.
     * @param locationName The location name.
     */
    protected final void setTemporaryLocationName(String locationName){
        this.locationName = locationName;
    }
    
    /**
     * Returns the location name as set when the plugin was loaded.
     * Use this for initialization when the location service is not loaded, otherwise use the location service asap.
     * @return The name of the location.
     */
    public final String getTemporaryLocationName(){
        return this.locationName;
    }
    
    /**
     * Sets the description set by the user.
     * @param description The description.
     */
    protected final void setDescription(String description){
        this.description = description;
    }
    
    /**
     * Returns the description of the added plugin. Not he plugin itself but the instance.
     * @return Description added by the user.
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Set the plugin active or not.
     * @param active Active or not.
     */
    protected final void setActive(boolean active){
        this.active.setValue(active);
    }
    
    /**
     * Returns a bindable property if the plugin is active or not.
     * @return A bindable read only boolean property.
     */
    public final ReadOnlyBooleanPropertyBindingBean getActive(){
        return this.active.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Sets the name of the plugin used.
     * @param pluginName The plugin name.
     */
    protected final void setPluginName(String pluginName){
        this.pluginName = pluginName;
    }
    
    /**
     * Starts any listeners.
     */
    protected final void start(){
        try {
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", pluginId);
                }
            };
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("MediaService.getCurrentMedia", sendObject, "MediaService.getCurrentMedia"));
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("MediaService.getPlayList", sendObject, "MediaService.getPlayList"));
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MediaPlugin.class.getName()).log(Level.WARNING, "Could not get current data", ex);
        }
        this.connection.addPCCConnectionNameSpaceListener("MediaService", this);
    }
    
    /**
     * Destroy's this instance vars and listeners.
     */
    protected final void destroy(){
        this.connection.removePCCConnectionNameSpaceListener("MediaService", this);
        this.connection = null;
    }
    
    /**
     * Handles results by broadcasts.
     * @param rpcDataHandler JSON-RPC data object
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        if(rpcDataHandler.getMethod().equals("playerCommand")){
            if(rpcDataHandler.getParameters().containsKey("command") && rpcDataHandler.getParameters().get("command").equals("STOP")){
                currentPlaying.setValue(new MediaData());
                nowPlayingList.clear();
            }
        } else if (rpcDataHandler.getMethod().equals("getCurrentMedia")){
            currentPlaying.setValue(createCurrentMedia(rpcDataHandler.getParameters()));
        } else if (rpcDataHandler.getMethod().equals("playlistCleared")){
            nowPlayingList.clear();
        } else if (rpcDataHandler.getMethod().equals("removePlaylistItem")){
            Map<String,Object> mediaData = rpcDataHandler.getParameters();
            int itemPos = ((Long)mediaData.get("pos")).intValue();
            List<MediaPlaylistItem> itemsToRemove = new ArrayList<>();
            for(MediaPlaylistItem item:nowPlayingList){
                if(item.getPlaylistPosition() == itemPos){
                    itemsToRemove.add(item);
                }
            }
            if(itemsToRemove.size()>0){
                /// Update playlist positions.
                for(MediaPlaylistItem item:nowPlayingList){
                    if (item.getPlaylistPosition() > itemPos) {
                        item.setPlaylistPostion(item.getPlaylistPosition() - 1);
                    }
                }
                nowPlayingList.removeAll(itemsToRemove);
            }
        } else if (rpcDataHandler.getMethod().equals("addPlaylistItem")){
            Map<String,Object> mediaData = rpcDataHandler.getParameters();
            nowPlayingList.add(new MediaPlaylistItem(getMediaTypeEnumByString((String)mediaData.get("itemtype")),
                    ((Number)mediaData.get("id")).intValue(),
                    ((Number)mediaData.get("playlist")).intValue(),
                    ((Number)mediaData.get("pos")).intValue(),
                    (String)mediaData.get("title"),
                    ((Number)mediaData.get("duration")).intValue()
            ));
        }
    }

    public final void playPlaylistItem(MediaPlaylistItem item) {
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", MediaPlugin.this.pluginId);
                put("playlist", item.getPlaylistId());
                put("pos", item.getPlaylistPosition());
                put("type", item.getMediaType().toString());
            }
        };
        try {
            connection.getJsonHTTPRPC("MediaService.playPlaylistItem", sendObject, "MediaService.playPlaylistItem");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MediaPlugin.class.getName()).log(Level.SEVERE, "Could not send command to play playlist item", ex);
        }
    }
    
    /**
     * Creates current playing data.
     * @param currentData
     * @return 
     */
    private static MediaData createCurrentMedia(Map<String,Object> currentData){
        MediaData data = new MediaData();
        if(currentData.containsKey("itemtype")){
            data.setTitle((String)currentData.get("title"));
            data.setDuration(((Number)currentData.get("duration")).intValue());
            try {
                data.setThumbnail(new URL((String)currentData.get("thumbnail")));
            } catch (MalformedURLException ex) {
                ///Logger.getLogger(MediaPlugin.class.getName()).log(Level.WARNING, "No thumbnail", ex);
            }
            try {
                data.setPoster(new URL((String)currentData.get("poster")));
            } catch (MalformedURLException ex) {
                ///Logger.getLogger(MediaPlugin.class.getName()).log(Level.WARNING, "No poster", ex);
            }
            switch((String)currentData.get("itemtype")){
                case "AUDIO":
                    data.setMediaType(MediaData.MediaType.AUDIO);
                    data.setTitleArtist((String)currentData.get("title_artist"));
                    data.setAlbum((String)currentData.get("album"));
                    data.setAlbumArtist((String)currentData.get("album_artist"));
                break;
                case "VIDEO":
                    data.setMediaType(MediaData.MediaType.VIDEO);
                break;
            }
        }
        return data;
    }
    
    /**
     * Create now playing playlist data.
     * @param playlistData 
     */
    private void composeNowPlayingList(List<Map<String,Object>> playlistData){
        nowPlayingList.clear();
        for(Map<String,Object> item:playlistData){
            nowPlayingList.add(new MediaPlaylistItem(getMediaTypeEnumByString((String)item.get("itemtype")),
                    ((Number)item.get("id")).intValue(),
                    ((Number)item.get("playlist")).intValue(),
                    ((Number)item.get("pos")).intValue(),
                    (String)item.get("title"),
                    ((Number)item.get("duration")).intValue()
            ));
        }
    }
    
    /**
     * Returns the enum type composed from the media type string.
     * @param string
     * @return
     * @throws MediaPluginException 
     */
    private MediaData.MediaType getMediaTypeEnumByString(String string) {
        for(MediaData.MediaType type: MediaData.MediaType.values()){
            if(type.toString().equals(string)){
                return type;
            }
        }
        return MediaData.MediaType.NONE;
    }
    
    /**
     * Returns a read only version of the current playlist.
     * @return An observable array list of the current playlist.
     */
    public final ReadOnlyObservableArrayListBean<MediaPlaylistItem> getCurrentPlaylist(){
        return this.readonlyNowPlayingList;
    }
    
    
    /**
     * Handles results by their result.
     * @param rpcDataHandler JSON-RPC data object
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        if(rpcDataHandler.getId().equals("MediaService.getCurrentMedia")){
            currentPlaying.setValue(createCurrentMedia((Map<String,Object>)rpcDataHandler.getResult().get("data")));
        }
        if(rpcDataHandler.getId().equals("MediaService.getPlayList")){
            composeNowPlayingList((List<Map<String,Object>>)rpcDataHandler.getResult().get("data"));
        }
    }
    
    /**
     * Returns the name of the plugin used.
     * @return the plugin name.
     */
    public final String getPluginName(){
        return this.pluginName;
    }
    
    /**
     * Returns a read only boundable object to current playing data.
     * @return Read only boundable object of the current playing data.
     */
    public final ReadOnlyObjectPropertyBindingBean<MediaData> getCurrentPlaying(){
        return currentPlaying.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Handles a server command to be send to the server.
     * @param command The server command to handle.
     */
    public final void handleServerCommand(ServerCommand command){
        HashMap<String,Object> params = new HashMap<>();
        params.put("id", this.pluginId);
        params.put("command", command.getCommand());
        try {
            connection.getJsonHTTPRPC("MediaService.serverCommand", params, "MediaService.serverCommand");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MediaPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles a player command to be send to the server.
     * @param command A player command to handle.
     */
    public final void handlePlayerCommand(PlayerCommand command){
        HashMap<String,Object> params = new HashMap<>();
        params.put("id", this.pluginId);
        params.put("command", command.getCommand());
        try {
            connection.getJsonHTTPRPC("MediaService.playerCommand", params, "MediaService.playerCommand");
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(MediaPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}