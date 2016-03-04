/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.media;

/**
 *
 * @author John
 */
public class MediaPlaylistItem {
 
    /**
     * The id of the item.
     */
    private final int id;
    /**
     * The title of the item.
     */
    private final String title;
    /**
     * The playlist id of the item (mostly the location).
     */
    private final int playlistId;
    
    /**
     * The position in the playlist.
     * This parameter can not be final as list changes also changes this item's position.
     */
    private int playlistPos;
    
    /**
     * Item duration in seconds.
     */
    private final int duration;
    
    /**
     * The item type.
     */
    private final MediaData.MediaType type;
    
    /**
     * Media playlist item concstructor.
     * @param type The item type (AUDIO, VIDEO, NONE, PVR)
     * @param id The id of the item.
     * @param playlistId The id of the playlist.
     * @param playlistPos The position in the playlist.
     * @param title The title of the item.
     * @param duration The duration of the item.
     */
    protected MediaPlaylistItem(MediaData.MediaType type, int id, int playlistId, int playlistPos, String title, int duration){
        this.type = type;
        this.id = id;
        this.playlistId = playlistId;
        this.title = title;
        this.playlistPos = playlistPos;
        this.duration = duration;
    }
    
    /**
     * Returns the id of the item.
     * @return int id.
     */
    public final int getId(){
        return this.id;
    }
    
    /**
     * The id of the playlist.
     * @return int id.
     */
    public final int getPlaylistId(){
        return this.playlistId;
    }
    
    /**
     * The title of the item.
     * @return String title.
     */
    public final String getTitle(){
        return this.title;
    }
    
    /**
     * Returns the item's duration.
     * @return The item's duration.
     */
    public final int getDuration(){
        return this.duration;
    }
    
    /**
     * This is used when a playlist is modified and positions need to be updated.
     * @param pos The position in the playlist.
     */
    protected final void setPlaylistPostion(int pos){
        this.playlistPos = pos;
    }
    
    /**
     * Returns the position in the playlist which is also used to select the playlistitem to play.
     * @return int playlist position.
     */
    public final int getPlaylistPosition(){
        return this.playlistPos;
    }
    
    /**
     * The media type
     * @return MediaData.MediaType enum type.
     */
    public final MediaData.MediaType getMediaType(){
        return this.type;
    }
    
}