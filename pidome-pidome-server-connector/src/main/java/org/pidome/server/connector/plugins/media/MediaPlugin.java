/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.media;

/**
 *
 * @author John
 */
public interface MediaPlugin {
    
    /**
     * List of supported video playing commands.
     * Use this if only a video player is supported.
     */
    public enum VideoCommand {
        PLAY,PAUSE,NEXT,PREVIOUS,STOP;
    }
    
    /**
     * List of supported audio playing commands.
     * Use this if only an audio player is supported.
     */
    public enum AudioCommand {
        PLAY,PAUSE,NEXT,PREVIOUS,STOP;
    }
    
    /**
     * List of supported combined/common player controls.
     * Use this if the Video/Audio players are merged or unavailable.
     */
    public enum PlayerCommand {
        PLAY,PAUSE,NEXT,PREV,STOP,CURRENT,PLAYLISTITEM;
    }
    
    /**
     * Supported audio details.
     */
    public enum AudioDetails {
        ARTIST, ALBUM, ALBUM_ARTIST, TITLE, TITLE_ARTIST, DURATION, CURRENT;
    }
    
    /**
     * Supported video details.
     */
    public enum VideoDetails {
        TITLE, DURATION, CURRENT;
    }
    
    /**
     * Type of item.
     */
    public enum ItemType {
        AUDIO,VIDEO,UNKNOWN,PVR;
    }
    
    public enum ItemDetails {
        ID, ARTIST, ALBUM, ALBUM_ARTIST, TITLE, TITLE_ARTIST, DURATION, ITEM_TYPE, THUMBNAIL, POSTER;
    }
    
    /**
     * Playlist types.
     */
    public enum PlayListItem {
        ITEM_TYPE,PLAYLIST_ID,ID,TITLE,PLAYLIST_POS,DURATION;
    }
    
    public enum PlaylistCommand {
        REMOVE,ADD,GET_AUDIO,GET_VIDEO,GET_PVR,GET;
    }
    
    /**
     * List of supported Server commands.
     */
    public enum ServerCommand {
        VOLUP, VOLDOWN, MUTE, SHUTDOWN, RESTART, UP, DOWN, CONFIRM, BACK, PING, LEFT, RIGHT, HOME, MESSAGE, OSD, AUDIO_PLAYLISTS, VIDEO_PLAYLISTS;
    }
    
    /**
     * Used for mapping items from an audio library if supported.
     */
    public enum AudioLibraryItemsMapping {
        FILEPATH, FILENAME, FILEIMAGE, FILEID;
    }

    /**
     * Used for mapping items from a video library if supported.
     */
    public enum VideoLibraryItemsMapping {
        FILEPATH, FILENAME, FILEIMAGE, FILEID;
    }
}
