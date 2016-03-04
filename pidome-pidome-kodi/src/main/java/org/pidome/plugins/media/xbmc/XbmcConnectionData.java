/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc;

/**
 *
 * @author John Sirach
 */
public interface XbmcConnectionData {
    
    public enum DataResultType {
        RESULT,ACTION,REQUEST;
    }
    
    public enum ResultInfoType {
        AUDIOPLAYER, VIDEOPLAYER, PVR, UNSUPPORTED;
    }
    
    public enum ResultActionType {
        PLAYER_PLAY, PLAYER_PAUSE, PLAYER_STOP, PLAYLIST_ADD_ITEM, PLAYLIST_CLEAR, PLAYLIST_REMOVE_ITEM,PLAYLIST_ADD_ITEM_INCOMPLETE,SYSTEM_SHUTDOWN,SYSTEM_GONE;
    }
    
    public enum RequestDataType {
        AUDIO, VIDEO, PVR, AUDIO_PLAYLIST, VIDEO_PLAYLIST, PVR_PLAYLIST, AUDIO_PLAYLISTS, VIDEO_PLAYLISTS;
    }
    
}
