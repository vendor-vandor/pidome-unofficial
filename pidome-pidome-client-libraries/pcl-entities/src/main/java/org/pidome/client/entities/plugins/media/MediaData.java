/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.media;

import java.net.URL;

/**
 *
 * @author John
 */
public class MediaData {
 
    public enum MediaType {
        NONE,AUDIO,VIDEO,PVR;
    }
    
    /**
     * Title of the current playing item.
     */
    private String title = "Idle";
    
    /**
     * The current playing mediaType.
     * Defaults to none if nothing is playing.
     */
    private MediaType mediaType = MediaType.NONE;
    
    /**
     * The location of a thumbnail.
     */
    private URL thumbnail;

    /**
     * The location of a poster.
     */
    private URL poster;
    
    /**
     * the audio title artist.
     */
    private String titleArtist = "";
    
    /**
     * The audio type album.
     */
    private String album = "";
    
    /**
     * The audio type album artist.
     */
    private String albumArtist = "";
    
    /**
     * The duration of the current item.
     */
    private int duration = 0;
    
    /**
     * Protected constructor.
     */
    protected MediaData(){}
    
    /**
     * Current media type.
     * The type is MediaType.NONE by default, it must be set when there is data.
     * @param isEmpty boolean if empty data.
     */
    protected final void setMediaType(MediaType isEmpty){
        this.mediaType = isEmpty;
    }
    
    /**
     * Returns the current playing data.
     * If there is no playing data MediaType.NONE is returned and there will be 
     * no playing data.
     * @return MediaType.NONE if there is no data otherwise VIDEO,AUDO or PVR.
     */
    public final MediaType getMediaType(){
        return mediaType;
    }
    
    /**
     * Sets the current playing title.
     * @param title The title of the current item.
     */
    protected final void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Sets the location of a thumbnail.
     * @param location String with location.
     */
    protected final void setThumbnail(URL location){
        this.thumbnail = location;
    }
    
    /**
     * Sets the location of a poster.
     * @param location String with location.
     */
    protected final void setPoster(URL location){
        this.poster = location;
    }
    
    /**
     * When the media type is audio set a title artist.
     * @param artist The name of the title artist.
     */
    protected final void setTitleArtist(String artist){
        this.titleArtist = artist;
    }
    
    /**
     * When the media type is audio set an album name.
     * @param album Name of the album.
     */
    protected final void setAlbum(String album){
        this.album = album;
    }
    
    /**
     * When the media type is audio set an album artist.
     * @param albumArtist Name of the artist of the album
     */
    protected final void setAlbumArtist(String albumArtist){
        this.albumArtist = albumArtist;
    }
    
    /**
     * Sets the duration of the current item.
     * @param duration in seconds.
     */
    protected final void setDuration(int duration){
        this.duration = duration;
    }
    
    /**
     * Returns the current playing title.
     * @return The current title.
     */
    public final String getTitle(){
        return this.title;
    }
    
    /**
     * Returns a remote thumbnail of the current playing item.
     * This returns a thumbnail of the current playing item.
     * @return The url or null when no item is available.
     */
    public URL getThumbnail(){
        return this.thumbnail;
    }
    
    /**
     * Returns a remote poster of the current playing item.
     * This returns a poster of the current playing item.
     * @return The url or null when no item is available.
     */
    public URL getPoster(){
        return this.poster;
    }
    
    /**
     * Returns the title artist.
     * Check the media type for availability as this is only available when the
     * media type is audio.
     * @return The title artist or an empty string when not available.
     */
    public final String getTitleArtist(){
        return this.titleArtist;
    }
    
    /**
     * Returns the album.
     * Check the media type for availability as this is only available when the
     * media type is audio.
     * @return The album name or an empty string when not available.
     */
    public final String getAlbum(){
        return this.album;
    }
    
    /**
     * Returns the album artist.
     * Check the media type for availability as this is only available when the
     * media type is audio.
     * @return The album artist or an empty string when not available.
     */
    public final String getAlbumArtist(){
        return this.albumArtist;
    }
    
    /**
     * Returns the current duration.
     * @return the duration in seconds for the current item.
     */
    public final int getDuration(){
        return this.duration;
    }
    
}