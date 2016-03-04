/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.media;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public class MediaEvent extends java.util.EventObject {

    public enum EventType {
        PLAYER_PLAY, PLAYER_STOP, PLAYER_PAUSE, PLAYLIST, PLAYLIST_ADD, PLAYLIST_REMOVE, PLAYLIST_CLEAR;
    }
    
    EventType type;
    
    Map<MediaPlugin.PlayListItem,Object> playlistData = new HashMap<>();
    
    public MediaEvent(Media source, EventType type) {
        super(source);
        this.type = type;
    }
    
    public final EventType getEventType(){
        return this.type;
    }
    
    @Override
    public final Media getSource(){
        return (Media)source;
    }
    
    public final void setPlaylistAddedData(Map<MediaPlugin.PlayListItem,Object> playlistData){
        this.playlistData = playlistData;
    }
    
    public final Map<MediaPlugin.PlayListItem,Object> getPlaylistAddedData(){
        return this.playlistData;
    }
    
}
