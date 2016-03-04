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

package org.pidome.server.services.http.rpc;

import org.pidome.server.connector.plugins.media.MediaException;

/**
 *
 * @author John
 */
public interface MediaServiceJSONRPCWrapperInterface {

    /**
     * Returns capabilities.
     * @param mediaId
     * @param type
     * @return
     * @throws MediaException 
     */
    public Object getCapabilities(Long mediaId, String type) throws MediaException;
    
    /**
     * Handles a server command for the player.
     * @param mediaId
     * @param command
     * @return
     * @throws MediaException 
     */
    public Object serverCommand(Long mediaId, String command) throws MediaException;
    
    /**
     * Handles a player command on the server.
     * @param mediaId
     * @param command
     * @return
     * @throws MediaException 
     */
    public Object playerCommand(Long mediaId, String command) throws MediaException;
    
    /**
     * Play an item on a playlist.
     * @param mediaId
     * @param playlistId
     * @param itemId
     * @param type
     * @return
     * @throws MediaException 
     */
    public Object playPlaylistItem(Long mediaId, Long playlistId, Long itemId, String type) throws MediaException;
    
    /**
     * Remove an item from the playlist.
     * @param mediaId
     * @param playlistId
     * @param itemId
     * @return
     * @throws MediaException 
     */
    public Object removePlaylistItem(Long mediaId, Long playlistId, Long itemId) throws MediaException;
    
    /**
     * Get the current playlist.
     * @param mediaId
     * @return 
     * @throws org.pidome.server.connector.plugins.media.MediaException 
     */
    public Object getPlayList(Long mediaId) throws MediaException;
    
    /**
     * Get the current playing media.
     * @param mediaId
     * @return 
     * @throws org.pidome.server.connector.plugins.media.MediaException 
     */
    public Object getCurrentMedia(Long mediaId) throws MediaException;
    
    /**
     * Return audio playlists.
     * @param mediaId
     * @return
     * @throws MediaException 
     */
    public Object getAudioPlaylists(Long mediaId) throws MediaException;
    
    /**
     * Return video playlists.
     * @param mediaId
     * @return
     * @throws MediaException 
     */
    public Object getVideoPlaylists(Long mediaId) throws MediaException;
    
    /**
     * Return PVR channel sets.
     * @param mediaId
     * @param setId
     * @return
     * @throws MediaException 
     */
    public Object getPVRChannels(Long mediaId, Long setId) throws MediaException;
            
    /**
     * Return PVR channel sets.
     * @param mediaId
     * @return
     * @throws MediaException 
     */
    public Object getPVRChannelSets(Long mediaId) throws MediaException;
            
    /**
     * Play a media items playlist.
     * @param mediaId
     * @param playlistId
     * @param type
     * @param playlistFile
     * @return
     * @throws MediaException 
     */
    public Object playPlaylist(Long mediaId, Long playlistId, String type, String playlistFile) throws MediaException;

    /**
     * Returns all the media plugins
     * @return 
     * @throws org.pidome.server.connector.plugins.media.MediaException 
     */
    public Object getPlugins() throws MediaException;
    
    /**
     * Returns the favorite media plugins.
     * @return 
     * @throws org.pidome.server.connector.plugins.media.MediaException 
     */
    public Object getFavorites() throws MediaException;
    
}
