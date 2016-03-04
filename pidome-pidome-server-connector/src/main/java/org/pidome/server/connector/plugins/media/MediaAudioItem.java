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
public final class MediaAudioItem extends MediaItem {
    
    MediaPlugin.ItemType type = MediaPlugin.ItemType.AUDIO;
        
    String album;
    String artist;
    String albumArtist;

    /**
     * Returns the ItemType for instances which can not identify the type;
     * @return 
     */
    @Override
    public final ItemType getItemType(){
        return type;
    }
    
    /**
     * Sets a a song artist.
     * @param artist 
     */
    public final void setArtist(String artist){
        this.artist = artist;
    }
    
    /**
     * Sets album name
     * @param album 
     */
    public final void setAlbum(String album){
        this.album = album;
    }
    
    /**
     * Sets th album artist
     * @param albumartist 
     */
    public final void setAlbumArtist(String albumartist){
        this.albumArtist = albumartist;
    }
    
    /**
     * Returns album if set, otherwise null;
     * @return 
     */
    public final String getAlbum(){
        return this.album;
    }
    
    /**
     * Returns album artist if set, otherwise null;
     * @return 
     */
    public final String getAlbumArtist(){
        return this.albumArtist;
    }
    
    /**
     * Returns song artist if set, otherwise null;
     * @return 
     */
    public final String getArtist(){
        return this.artist;
    }
    
}
