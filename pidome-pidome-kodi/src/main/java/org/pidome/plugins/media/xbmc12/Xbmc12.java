/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.media.xbmc12;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.plugins.media.MediaItem;
import org.pidome.server.connector.plugins.media.MediaPlugin.AudioCommand;
import org.pidome.server.connector.plugins.media.MediaPlugin.PlayerCommand;
import org.pidome.server.connector.plugins.media.MediaPlugin.ServerCommand;
import org.pidome.server.connector.plugins.media.MediaPlugin.VideoCommand;
import org.pidome.plugins.media.xbmc.XbmcRPC6ConnectionData;
import org.pidome.plugins.media.xbmc.XbmcBase;
import org.pidome.plugins.media.xbmc.XbmcConnection;
import org.pidome.plugins.media.xbmc.XbmcConnectionData;
import org.pidome.plugins.media.xbmc.XbmcEvent;
import org.pidome.plugins.media.xbmc.XbmcConnectionListener;

/**
 *
 * @author John
 */
public class Xbmc12 extends XbmcBase implements XbmcConnectionListener {

    static Logger LOG = LogManager.getLogger(Xbmc12.class);
    
    long currentPlayer = 0;
    
    public Xbmc12(){

    }
    
    /**
     * It is overridden here because i want to know if there is anything running now and in which state it is.
     * @throws org.pidome.pluginconnector.PluginException 
     */
    @Override
    public final void startPlugin() throws PluginException {
        super.startPlugin();
        startConnectionCheck("{ \"jsonrpc\": \"2.0\", \"method\": \"JSONRPC.Ping\", \"id\": 1 }");
        LOG.info("XBMC plugin for XBMC version 12/13 started.");
    }
    
    /**
     * Prepares for deletion.
     */
    @Override
    public void prepareDelete() {
        /// Not used
    }
    
    /**
     * Stops the plugin.
     * @throws PluginException 
     */
    @Override
    public final void stopPlugin() throws PluginException {
        String[] supplement = new String[2];
        supplement[0] = "PiDome";
        supplement[1] = "PiDome server disconnecting";
        try {
            handleServerCommand(ServerCommand.MESSAGE, (Object[]) supplement);
        } catch (MediaException ex){
            LOG.warn("Plugin stop was not correct: {}", ex.getMessage());
        }
        super.stopPlugin();
    }
    
    /**
     * Delivers mapping for video.
     * @return
     * @throws UnsupportedOperationException 
     */
    @Override
    public Map<String, String> videoLibraryUrlMapping() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Delivers mapping for audio.
     * @return
     * @throws UnsupportedOperationException 
     */
    @Override
    public Map<String, String> audioLibraryUrlMapping() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Handles a video command.
     * @param command
     * @param supplement
     * @throws MediaException 
     */
    @Override
    public void handleVideoCommand(VideoCommand command, Object... supplement) throws MediaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Handles an audio command.
     * @param command
     * @param supplement
     * @throws MediaException 
     */
    @Override
    public void handleAudioCommand(AudioCommand command, Object... supplement) throws MediaException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Handles a playlist command.
     * @param command
     * @param supplement
     * @throws MediaException 
     */
    @Override
    public void handlePlaylistCommand(PlaylistCommand command, Object... supplement) throws MediaException {
        try {
            switch(command){
                case REMOVE:
                    getConnection().send(XbmcRPC6ConnectionData.removePlaylistItem((int)supplement[0], (int)supplement[1]));
                break;
                case GET_AUDIO:
                    getConnection().send(XbmcRPC6ConnectionData.getAudioPlaylist());
                break;
                case GET_VIDEO:
                    getConnection().send(XbmcRPC6ConnectionData.getVideoPlaylist());
                break;
            }
        } catch (IOException ex) {
            throw new MediaException("Problem sending data: " + ex.getMessage());
        }
    }
    
    /**
     * Handles a player command.
     * @param command
     * @param supplement
     * @throws MediaException 
     */
    @Override
    public void handlePlayerCommand(PlayerCommand command, Object... supplement) throws MediaException {
        try {
            switch(command){
                case CURRENT:
                    getConnection().send(XbmcRPC6ConnectionData.getActivePlayers());
                break;
                case PLAYLISTITEM:
                    getConnection().send(XbmcRPC6ConnectionData.playPlaylistItem((int)supplement[0], (int)supplement[1], (String)supplement[2]));
                break;
                case PLAY:
                    getConnection().send(XbmcRPC6ConnectionData.playerPlay(currentPlayer));
                break;
                case PAUSE:
                    getConnection().send(XbmcRPC6ConnectionData.playerPause(currentPlayer));
                break;
                case NEXT:
                    getConnection().send(XbmcRPC6ConnectionData.playerNext(currentPlayer));
                break;
                case PREV:
                    getConnection().send(XbmcRPC6ConnectionData.playerPrevious(currentPlayer));
                break;
                case STOP:
                    getConnection().send(XbmcRPC6ConnectionData.playerStop(currentPlayer));
                break;
            }
        } catch (IOException ex) {
            throw new MediaException("Problem sending data: " + ex.getMessage());
        }
    }
    
    /**
     * Handles a server command
     * @param command
     * @param supplement
     * @throws MediaException 
     */
    @Override
    public void handleServerCommand(ServerCommand command, Object... supplement) throws MediaException {
        try {
            XbmcConnection connection = getConnection();
            switch(command){
                case PING:
                    connection.send(XbmcRPC6ConnectionData.createPingCommand());
                    break;
                case LEFT:
                    connection.send(XbmcRPC6ConnectionData.createLeftCommand());
                    break;
                case RIGHT:
                    connection.send(XbmcRPC6ConnectionData.createRightCommand());
                    break;
                case UP:
                    connection.send(XbmcRPC6ConnectionData.createUpCommand());
                    break;
                case DOWN:
                    connection.send(XbmcRPC6ConnectionData.createDownCommand());
                    break;
                case CONFIRM:
                    connection.send(XbmcRPC6ConnectionData.createConfirmCommand());
                    break;
                case BACK:
                    connection.send(XbmcRPC6ConnectionData.createBackCommand());
                    break;
                case HOME:
                    connection.send(XbmcRPC6ConnectionData.createHomeCommand());
                    break;
                case OSD:
                    connection.send(XbmcRPC6ConnectionData.createSendOSDCommand());
                    break;
                case MESSAGE:
                    connection.send(XbmcRPC6ConnectionData.createSendMessageCommand((String)supplement[0], (String)supplement[1]));
                    break;
                case VOLUP:
                    connection.send(XbmcRPC6ConnectionData.createSendVolUpCommand());
                    break;
                case VOLDOWN:
                    connection.send(XbmcRPC6ConnectionData.createSendVolDownCommand());
                    break;
                case MUTE:
                    connection.send(XbmcRPC6ConnectionData.createSendMuteCommand());
                    break;
            }
        } catch (IOException ex) {
            throw new MediaException("Problem sending data: " + ex.getMessage());
        }
    }

    /**
     * Returns video playlist.
     * @return
     * @throws MediaException 
     */
    @Override
    public Object getVideoPlaylists() throws MediaException {
        try {
            return XbmcRPC6ConnectionData.getAudioPlaylists(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.createGetAudioPlaylists()));
        } catch (ParseException | IOException ex) {
            throw new MediaException("Problem retrieving playlists: " + ex.getMessage());
        }
    }
    
    /**
     * Returns PVR channel sets.
     * @return
     * @throws MediaException 
     */
    @Override
    public Object getPVRChannelSets() throws MediaException {
        try {
            return XbmcRPC6ConnectionData.getPVRPlaylists(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.createGetPVRChannelLists()));
        } catch (ParseException | IOException | NullPointerException ex) {
            throw new MediaException("Problem retrieving playlists: " + ex.getMessage());
        }
    }
    
    /**
     * Returns PVR channels.
     * @return
     * @throws MediaException 
     */
    @Override
    public Object getPVRChannels(Long setId) throws MediaException {
        try {
            return XbmcRPC6ConnectionData.getPVRPlaylist(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.getPVRPlaylist(setId.intValue())));
        } catch (ParseException | IOException | NullPointerException ex) {
            throw new MediaException("Problem retrieving playlists: " + ex.getMessage());
        }
    }
    
    /**
     * Returns audio playlist.
     * @return
     * @throws MediaException 
     */
    @Override
    public Object getAudioPlaylists() throws MediaException {
        try {
            return XbmcRPC6ConnectionData.getAudioPlaylists(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.createGetAudioPlaylists()));
        } catch (ParseException | IOException ex) {
            throw new MediaException("Problem retrieving playlists: " + ex.getMessage());
        }
    }
    
    /**
     * Handles XBMC events.
     * @param event 
     */
    @Override
    public void handleXbmcEvent(XbmcEvent event) {
        XbmcRPC6ConnectionData result = (XbmcRPC6ConnectionData)event.getSource();
        try {
            if(result.parse()){
                switch(result.getDataType()){
                    case RESULT:
                        Map<String,Object> resultData = result.getResultData();
                        if(!resultData.isEmpty()){
                            switch((XbmcConnectionData.ResultInfoType)resultData.get("resulttype")){
                                case AUDIOPLAYER:
                                    currentPlayer = (long)resultData.get("playerid");
                                    getConnection().send(XbmcRPC6ConnectionData.getAudioItem((long)resultData.get("playerid")));
                                    handlePlaylistCommand(PlaylistCommand.GET_AUDIO, (String) null);
                                break;
                                case VIDEOPLAYER:
                                    currentPlayer = (long)resultData.get("playerid");
                                    getConnection().send(XbmcRPC6ConnectionData.getVideoItem((long)resultData.get("playerid")));
                                    handlePlaylistCommand(PlaylistCommand.GET_VIDEO, (String) null);
                                break;
                            }
                        }
                    break;
                    case ACTION:
                        Map<String,Object> actionData = result.getActionData();
                        if(!actionData.isEmpty()){
                            switch((XbmcConnectionData.ResultActionType)actionData.get("ResultActionType")){
                                case SYSTEM_SHUTDOWN:
                                    LOG.info("XBMC shutdown/reboot");
                                    try {
                                        getConnection().stop();
                                        getConnection().internalClose();
                                        clearNowPlayingData();
                                        clearPlayList(null);
                                    } catch (MediaException ex){
                                        LOG.error("Could not get connection");
                                    }
                                break;
                                case PLAYER_PLAY:
                                    getConnection().send(XbmcRPC6ConnectionData.getActivePlayers());
                                break;
                                case PLAYER_PAUSE:
                                    sendPlayerPaused();
                                break;
                                case PLAYER_STOP:
                                    clearNowPlayingData();
                                break;
                                case PLAYLIST_ADD_ITEM:
                                    addToPlayList((MediaItem)actionData.get("item"));
                                break;
                                case PLAYLIST_ADD_ITEM_INCOMPLETE:
                                    switch((ItemType)actionData.get("ItemType")){
                                        case AUDIO:
                                            addToPlayList(XbmcRPC6ConnectionData.createMediaItemByResultRequestForPlaylist(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.createGetAudioItemFromLibraryForAddPlaylist((int)actionData.get("id"))
                                                            ),
                                                            ItemType.AUDIO,
                                                            (int)actionData.get("playlistid"),
                                                            (int)actionData.get("playlistpos")
                                                    )
                                            );
                                        break;
                                        case VIDEO:
                                            addToPlayList(XbmcRPC6ConnectionData.createMediaItemByResultRequestForPlaylist(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.createGetVideoItemFromLibraryForAddPlaylist((int)actionData.get("id"))
                                                            ),
                                                            ItemType.VIDEO,
                                                            (int)actionData.get("playlistid"),
                                                            (int)actionData.get("playlistpos")
                                                    )
                                            );
                                        break;
                                        case PVR:
                                            addToPlayList(XbmcRPC6ConnectionData.createMediaItemByResultRequestForPlaylist(getConnection().getDataViaHttp(XbmcRPC6ConnectionData.createGetPVRItemFromLibraryForAddPlaylist((int)actionData.get("id"))
                                                            ),
                                                            ItemType.PVR,
                                                            0,
                                                            (int)actionData.get("channelid")
                                                    )
                                            );
                                        break;
                                    }
                                break;
                                case PLAYLIST_CLEAR:
                                    clearPlayList(null);
                                break;
                                case PLAYLIST_REMOVE_ITEM:
                                    removeFromPlayList(((Long)actionData.get("playlistpos")).intValue());
                                break;
                            }
                        }
                    break;
                    case REQUEST:
                        Map<String,Object> requestData = result.getRequestData();
                        if(!requestData.isEmpty()){
                            switch((XbmcConnectionData.RequestDataType)requestData.get("RequestDataType")){
                                case AUDIO:
                                case VIDEO:
                                case PVR:
                                    LOG.info("Playing: {}",requestData);
                                    setCurrentPlayingData((MediaItem)requestData.get("item"));
                                break;
                                case AUDIO_PLAYLIST:
                                case VIDEO_PLAYLIST:
                                case PVR_PLAYLIST:
                                    LOG.debug("Playlist: {}",requestData.get("playlist"));
                                    List<MediaItem> playlist = (List<MediaItem>)requestData.get("playlist");
                                    addListToPlaylist(playlist);
                                break;
                            }
                        }
                    break;
                }
            }
        } catch (MediaException | IOException ex) {
            LOG.error("Could not handle event: {} - {}", ex.getMessage(),ex.getCause());
        }
    }

    /**
     * Plays a playlist.
     * @param playlistId
     * @param playlistFile
     * @throws MediaException 
     */
    @Override
    public void playPlaylist(final Long playlistId, String playlistType, String playlistFile) throws MediaException {
        try {
            clearPlayList(null);
            getConnection().send(XbmcRPC6ConnectionData.createOpenPlaylist(playlistId, playlistType, playlistFile));
        } catch (IOException ex) {
            throw new MediaException("Problem starting playlist: " + ex.getMessage());
        }
    }

    /**
     * Handles an XBMC connection event.
     * @param event 
     */
    @Override
    public void handleConnectionEvent(String event) {
        switch(event){
            case "CONNECTED":
                String[] supplement = new String[2];
                supplement[0] = "PiDome";
                supplement[1] = "Connected with plugin XBMC 12";
                try {
                    handleServerCommand(ServerCommand.MESSAGE, (Object[]) supplement);
                    handlePlayerCommand(PlayerCommand.CURRENT, (String) null);
                    super.setRunning(true);
                } catch (MediaException ex){
                    LOG.error("Plugin startup was not correct: {}", ex.getMessage());
                }
            break;
            case "DISCONNECTED":
                super.setRunning(false);
            break;
        }
    }

}
