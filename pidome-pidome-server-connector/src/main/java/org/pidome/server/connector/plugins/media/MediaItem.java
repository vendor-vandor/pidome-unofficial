/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.media;

import org.pidome.server.connector.plugins.media.MediaPlugin.ItemType;

/**
 *
 * @author John
 */
public abstract class MediaItem {
    
    int itemId = 0;
    int playlistId = 0;
    int playlistPos = 0;
    int duration = 0;
    String itemTitle = "";
    
    String thumbNail = "";
    
    
    String poster = "";
    
    /**
     * Returns the ItemType for instances which can not identify the type;
     * @return 
     */
    public abstract ItemType getItemType();
    
    /**
     * Sets the id of the item.
     * @param itemId 
     */
    public final void setId(int itemId){
        this.itemId = itemId;
    }
    
    /**
     * Sets a thumbnail.
     * @param url 
     */
    public final void setThumbnail(String url){
        if(url!=null){
            this.thumbNail = url;
        }
    }
    
    /**
     * Sets a poster.
     * @param url 
     */
    public final void setPoster(String url){
        if(url!=null){
            this.poster = url;
        }
    }
    
    /**
     * If this item is part of a playlist set the position of this item in the playlist
     * @param pos 
     */
    public final void setPlaylistPos(int pos){
        this.playlistPos = pos;
    }
    
    /**
     * The playlist id should be stored in the item for making mixing playlists possible
     * @param playlistId 
     */
    public final void setPlaylistId(int playlistId){
        this.playlistId = playlistId;
    }
    
    /**
     * Set the length of the item in seconds.
     * @param duration 
     */
    public final void setDuration(int duration){
        this.duration = duration;
    }
    
    /**
     * Sets the title of the item.
     * @param title 
     */
    public final void setTitle(String title){
        this.itemTitle = title;
    }
    
    /**
     * Returns a thumbnail.
     * @return 
     */
    public final String getThumbnail(){
        return this.thumbNail;
    }
    
    /**
     * Returns a poster.
     * @return 
     */
    public final String getPoster(){
        return this.poster;
    }
    
    /**
     * Returns the length of the item in seconds
     * @return 
     */
    public final int getDuration(){
        return this.duration;
    }
 
    /**
     * Returns the id of the item if a remote library supports this, otherwise null;
     * @return 
     */
    public final int getId(){
        return this.itemId;
    }
    
    /**
     * Returns the position in the playlist if part of a playlist otherwise null;
     * @return 
     */
    public final int getPlaylistPos(){
        return this.playlistPos;
    }
 
    /**
     * Returns a playlist id if set, otherwise null;
     * @return 
     */
    public final int getPlaylistId(){
        return this.playlistId;
    }
    
    /**
     * Returns the title of the media item.
     * @return 
     */
    public final String getTitle(){
        return this.itemTitle;
    }
    
}
