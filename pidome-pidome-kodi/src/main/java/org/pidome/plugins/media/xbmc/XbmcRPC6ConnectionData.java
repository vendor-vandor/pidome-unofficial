/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.plugins.media.MediaAudioItem;
import org.pidome.server.connector.plugins.media.MediaException;
import org.pidome.server.connector.plugins.media.MediaItem;
import org.pidome.server.connector.plugins.media.MediaPVRItem;
import org.pidome.server.connector.plugins.media.MediaPlugin.ItemType;
import org.pidome.server.connector.plugins.media.MediaVideoItem;

/**
 *
 * @author John Sirach
 */
public class XbmcRPC6ConnectionData implements XbmcConnectionData {

    JSONParser parser = new JSONParser();
    
    JSONObject parsedObject;
    
    String rawData;
    private boolean success = false;
    
    DataResultType dataType;
    
    Map<String,Object> resultData = new HashMap<>();
    Map<String,Object> serverActionData = new HashMap<>();
    Map<String,Object> requestedData = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(XbmcRPC6ConnectionData.class);
    
    long currentPlayer = 0;
    
    XbmcConnection connection;
    
    public XbmcRPC6ConnectionData(XbmcConnection connection, String jsonData){
        this.connection = connection;
        rawData = jsonData;
    }
    
    /**
     * Always check this to make sure the received json is parsed.
     * @return 
     */
    public final boolean parse(){
        try{
          parsedObject = (JSONObject)parser.parse(rawData);
          setParsedData();
          success = true;
        } catch(ParseException ex){
          LOG.error("Could not parse json data: {}, error at position: {}", rawData, ex.getPosition(), ex);
          success = false;
        }
        return success;
    }
    
    /**
     * Returns the current/last known player
     * @return 
     */
    public final long getCurrentPlayer(){
        return currentPlayer;
    }
    
    /**
     * Returns the parsed json object as an array.
     * @return 
     */
    public final JSONObject getResult(){
        return parsedObject;
    }
    
    /**
     * 
     * @return 
     */
    public final XbmcConnectionData.DataResultType getDataType(){
        return dataType;
    }
    
    /**
     * Sets data ready to be fetched.
     * @param obj 
     */
    final void setParsedData(){
        if(parsedObject.containsKey("result") && parsedObject.containsKey("id") && parsedObject.get("id") instanceof Long){ /// response on initiated actions.
            LOG.trace("Handling a result: {}", parsedObject);
            dataType = DataResultType.RESULT;
            createResultData(parsedObject.get("result"));
        } else if(parsedObject.containsKey("method")) { //// Data by server action.
            LOG.trace("Handling a method: {}", parsedObject);
            dataType = DataResultType.ACTION;
            createServerActionData(parsedObject);
        } else if (parsedObject.containsKey("id")){ //// Response on our data requests.
            LOG.trace("Handling a response to send method: {}", parsedObject);
            dataType = DataResultType.REQUEST;
            createRequestedData(parsedObject);
        }
    }
    
    /**
     * Create the requested data
     * @param object 
     */
    final void createRequestedData(Object object){
        ///AudioGetItem
        if(object instanceof JSONObject){
            createRequestedMap((JSONObject)object);
        }
    }
    
    /**
     * Create the mapping for the requested data.
     * @param object 
     */
    final void createRequestedMap(JSONObject object){
        if(!object.isEmpty() && object.containsKey("id") && object.get("id")!=null && !(object.get("id") instanceof Long)){
            switch((String)object.get("id")){
                case "getAudioPlaylists":
                    requestedData.put("RequestDataType", RequestDataType.AUDIO_PLAYLISTS);
                    requestedData.put("playlists", getAudioPlaylists(object));
                break;
                case "getVideoPlaylists":
                    requestedData.put("RequestDataType", RequestDataType.VIDEO_PLAYLISTS);
                    requestedData.put("playlists", getVideoPlaylists(object));
                break;
                case "getPVRPlaylist":
                    JSONArray PVRDetailsList = (JSONArray)((JSONObject)object.get("result")).get("channels");
                    List<MediaPVRItem> PVRPlaylist = new ArrayList();
                    if(PVRDetailsList!=null){
                        for(int key = 0; key < PVRDetailsList.size(); key++){
                            JSONObject listItemDetail = (JSONObject)PVRDetailsList.get(key);
                            MediaPVRItem pvrListItem = new MediaPVRItem();
                            if((String)listItemDetail.get("channel")!=null && !((String)listItemDetail.get("channel")).equals("")){
                                pvrListItem.setTitle((String)listItemDetail.get("channel"));
                            } else {
                                pvrListItem.setTitle((String)listItemDetail.get("label"));
                            }
                            pvrListItem.setDuration(0);
                            pvrListItem.setPlaylistPos(((Long)listItemDetail.get("channelid")).intValue());
                            pvrListItem.setPlaylistId(0);
                            pvrListItem.setId(((Long)listItemDetail.get("channelid")).intValue());
                            PVRPlaylist.add(pvrListItem);
                        }
                    }
                    requestedData.put("RequestDataType", RequestDataType.PVR_PLAYLIST);
                    requestedData.put("playlist", PVRPlaylist);
                break;
                case "AudioGetItem": /// requested an audio item
                    JSONObject details = (JSONObject)((JSONObject)object.get("result")).get("item");
                    requestedData.put("RequestDataType", RequestDataType.AUDIO);
                    
                    MediaAudioItem audioItem = new MediaAudioItem();
                    if(details.get("id")!=null){
                        audioItem.setId(((Long)details.get("id")).intValue());
                    }
                    audioItem.setAlbum((String)details.get("album"));
                    audioItem.setAlbumArtist(jsonArrayObjectToString((JSONArray)details.get("albumartist")));
                    if((String)details.get("title")!=null && !((String)details.get("title")).equals("")){
                        audioItem.setTitle((String)details.get("title"));
                    } else {
                        audioItem.setTitle((String)details.get("label"));
                    }
                    if(details.get("thumbnail")!=null && !details.get("thumbnail").equals("") && !((String)details.get("thumbnail")).contains("DefaultAlbumCover")){
                        audioItem.setThumbnail("http:/" + connection.ipAddress + ":" + connection.httpPort + "/image/" + URLEncoder.encode((String)details.get("thumbnail")));
                    }
                    if(details.get("art")!=null){
                        Map<String,String> artSet = (Map<String,String>)details.get("art");
                        if(artSet.containsKey("fanart") && !artSet.get("fanart").equals("")){
                            audioItem.setPoster("http:/" + connection.ipAddress + ":" + connection.httpPort + "/image/" + URLEncoder.encode((String)artSet.get("fanart")));
                        }
                    }
                    audioItem.setArtist(jsonArrayObjectToString((JSONArray)details.get("artist")));
                    audioItem.setDuration(((Long)details.get("duration")).intValue());
                    requestedData.put("item", audioItem);
                break;
                case "AudioPlaylist":
                    JSONArray detailsList = (JSONArray)((JSONObject)object.get("result")).get("items");
                    List<MediaAudioItem> playlist = new ArrayList();
                    for(int key = 0; key < detailsList.size(); key++){
                        JSONObject listItemDetail = (JSONObject)detailsList.get(key);
                        MediaAudioItem audioListItem = new MediaAudioItem();
                        if((String)listItemDetail.get("title")!=null && !((String)listItemDetail.get("title")).equals("")){
                            if(listItemDetail.get("artist")!=null){
                                audioListItem.setTitle(jsonArrayObjectToString((JSONArray)listItemDetail.get("artist")) + " - " + (String)listItemDetail.get("title"));
                            } else {
                                audioListItem.setTitle((String)listItemDetail.get("title"));
                            }
                        } else {
                            audioListItem.setTitle((String)listItemDetail.get("label"));
                        }
                        audioListItem.setArtist(jsonArrayObjectToString((JSONArray)listItemDetail.get("artist")));
                        audioListItem.setDuration(((Long)listItemDetail.get("duration")).intValue());
                        audioListItem.setPlaylistPos(key);
                        audioListItem.setPlaylistId(0);
                        if(listItemDetail.get("id")!=null){
                            audioListItem.setId(((Long)listItemDetail.get("id")).intValue());
                        }
                        playlist.add(audioListItem);
                    }
                    requestedData.put("RequestDataType", RequestDataType.AUDIO_PLAYLIST);
                    requestedData.put("playlist", playlist);
                break;
                case "VideoGetItem":
                    JSONObject videoDetails = (JSONObject)((JSONObject)object.get("result")).get("item");
                    if(videoDetails.containsKey("type") && videoDetails.get("type").equals("channel")){
                        requestedData.put("RequestDataType", RequestDataType.PVR);
                    } else {
                        requestedData.put("RequestDataType", RequestDataType.VIDEO);                        
                    }
                    MediaVideoItem videoItem = new MediaVideoItem();
                    if(videoDetails.containsKey("id")){
                        videoItem.setId(((Long)videoDetails.get("id")).intValue());
                    } else {
                        videoItem.setId(0);
                    }
                    if((String)videoDetails.get("title")!=null && !((String)videoDetails.get("title")).equals("")){
                        videoItem.setTitle((String)videoDetails.get("title"));
                    } else {
                        videoItem.setTitle((String)videoDetails.get("label"));
                    }
                    if(videoDetails.get("duration")!=null){
                        videoItem.setDuration(((Long)videoDetails.get("duration")).intValue());
                    } else if (videoDetails.containsKey("runtime")) {
                        if(videoDetails.get("runtime") instanceof String){
                            videoItem.setDuration(Integer.valueOf((String)videoDetails.get("runtime")));
                        } else {
                            videoItem.setDuration(((Long)videoDetails.get("runtime")).intValue());
                        }
                    }
                    if(videoDetails.containsKey("thumbnail") && videoDetails.get("thumbnail")!=null){
                        videoItem.setThumbnail("http:/" + connection.ipAddress + ":" + connection.httpPort + "/image/" + URLEncoder.encode((String)videoDetails.get("thumbnail")));
                    }
                    if(videoDetails.get("art")!=null){
                        Map<String,String> artSet = (Map<String,String>)videoDetails.get("art");
                        if(artSet.containsKey("fanart")){
                            videoItem.setPoster("http:/" + connection.ipAddress + ":" + connection.httpPort + "/image/" + URLEncoder.encode((String)artSet.get("fanart")));
                        }
                    }
                    requestedData.put("item", videoItem);
                break;
                case "VideoPlaylist":
                    JSONArray VideoDetailsList = (JSONArray)((JSONObject)object.get("result")).get("items");
                    List<MediaVideoItem> videoPlaylist = new ArrayList();
                    if(VideoDetailsList!=null){
                        for(int key = 0; key < VideoDetailsList.size(); key++){
                            JSONObject listItemDetail = (JSONObject)VideoDetailsList.get(key);
                            MediaVideoItem videoListItem = new MediaVideoItem();
                            if((String)listItemDetail.get("title")!=null && !((String)listItemDetail.get("title")).equals("")){
                                videoListItem.setTitle((String)listItemDetail.get("title"));
                            } else {
                                videoListItem.setTitle((String)listItemDetail.get("label"));
                            }
                            if(listItemDetail.get("duration")!=null){
                                videoListItem.setDuration(((Long)listItemDetail.get("duration")).intValue());
                            } else {
                                videoListItem.setDuration(((Long)listItemDetail.get("runtime")).intValue());
                            }
                            videoListItem.setPlaylistPos(key);
                            videoListItem.setPlaylistId(1);
                            videoListItem.setId(((Long)listItemDetail.get("id")).intValue());
                            
                            videoPlaylist.add(videoListItem);
                        }
                    }
                    requestedData.put("RequestDataType", RequestDataType.VIDEO_PLAYLIST);
                    requestedData.put("playlist", videoPlaylist);
                break;
            }
        }
    }
    
    /**
     * Transforms a json audio playlist string to correct object.
     * @param data
     * @return
     * @throws ParseException 
     */
    public static ArrayList<Map<String, Object>> getAudioPlaylists(String data) throws ParseException {
        return getAudioPlaylists((JSONObject)new JSONParser().parse(data));
    }
    
    /**
     * Returns a well formed expected audio playlists list
     * @param object
     * @return 
     */
    public static ArrayList<Map<String, Object>> getAudioPlaylists(JSONObject object){
        JSONArray audiolistDetails = (JSONArray) ((JSONObject) object.get("result")).get("files");
        ArrayList<Map<String, Object>> audioItems = new ArrayList();
        if(audiolistDetails!=null){
            for (Object audiolistDetail : audiolistDetails) {
                Map<String, Object> playlistItem = new HashMap<>();
                playlistItem.put("file", ((JSONObject) audiolistDetail).get("file"));
                playlistItem.put("type", "AUDIO");
                playlistItem.put("title", ((JSONObject) audiolistDetail).get("label"));
                playlistItem.put("id", null);
                audioItems.add(playlistItem);
            }
        }
        return audioItems;
    }
    
    /**
     * Transforms a json video playlist string to correct object.
     * @param data
     * @return
     * @throws ParseException 
     */
    public static ArrayList<Map<String, Object>> getVideoPlaylists(String data) throws ParseException {
        return getVideoPlaylists((JSONObject)new JSONParser().parse(data));
    }
    
    /**
     * Returns a well formed expected video playlists list
     * @param object
     * @return 
     */
    public static ArrayList<Map<String, Object>> getVideoPlaylists(JSONObject object){
        JSONArray videolistDetails = (JSONArray) ((JSONObject) object.get("result")).get("files");
        ArrayList<Map<String, Object>> videoItems = new ArrayList();
        if(videolistDetails!=null){
            for (Object videolistDetail : videolistDetails) {
                Map<String, Object> playlistItem = new HashMap<>();
                playlistItem.put("file", ((JSONObject) videolistDetail).get("file"));
                playlistItem.put("type", "VIDEO");
                playlistItem.put("title", ((JSONObject) videolistDetail).get("label"));
                playlistItem.put("id", null);
                videoItems.add(playlistItem);
            }
        }
        return videoItems;
    }
    
    
    /**
     * Transforms a json PVR playlist string to correct object.
     * @param data
     * @return
     * @throws ParseException 
     */
    public static ArrayList<Map<String, Object>> getPVRPlaylists(String data) throws ParseException {
        return getPVRPlaylists((JSONObject)new JSONParser().parse(data));
    }
    
    /**
     * Returns a well formed expected PVR playlists list
     * @param object
     * @return 
     */
    public static ArrayList<Map<String, Object>> getPVRPlaylists(JSONObject object){
        JSONArray PVRDetails = (JSONArray) ((JSONObject) object.get("result")).get("channelgroups");
        ArrayList<Map<String, Object>> pvrItems = new ArrayList();
        if(PVRDetails!=null){
            for (Object pvrDetail : PVRDetails) {
                Map<String, Object> pvrItem = new HashMap<>();
                pvrItem.put("file", "null");
                pvrItem.put("type", "PVR");
                pvrItem.put("title", ((JSONObject) pvrDetail).get("label"));
                pvrItem.put("id", ((JSONObject) pvrDetail).get("channelgroupid"));
                pvrItems.add(pvrItem);
            }
        }
        return pvrItems;
    }
    
    /**
     * Transforms a json PVR playlist string to correct object.
     * @param data
     * @return
     * @throws ParseException 
     */
    public static ArrayList<Map<String, Object>> getPVRPlaylist(String data) throws ParseException {
        return getPVRPlaylist((JSONObject)new JSONParser().parse(data));
    }
    
    /**
     * Returns a well formed expected PVR playlists list
     * @param object
     * @return 
     */
    public static ArrayList<Map<String, Object>> getPVRPlaylist(JSONObject object){
        JSONArray PVRDetails = (JSONArray) ((JSONObject) object.get("result")).get("channels");
        ArrayList<Map<String, Object>> pvrItems = new ArrayList();
        if(PVRDetails!=null){
            for(int key = 0; key < PVRDetails.size(); key++){
                JSONObject listItemDetail = (JSONObject)PVRDetails.get(key);
                Map<String, Object> pvrListItem = new HashMap<String, Object>();
                if((String)listItemDetail.get("channel")!=null && !((String)listItemDetail.get("channel")).equals("")){
                    pvrListItem.put("title",(String)listItemDetail.get("channel"));
                } else {
                    pvrListItem.put("title",(String)listItemDetail.get("label"));
                }
                pvrListItem.put("duration",0);
                pvrListItem.put("pos",((Long)listItemDetail.get("channelid")).intValue());
                pvrListItem.put("playlist",0);
                pvrListItem.put("itemid",((Long)listItemDetail.get("channelid")).intValue());
                pvrListItem.put("itemtype","PVR");
                pvrListItem.put("thumbnail",(String)listItemDetail.get("thumbnail"));
                pvrItems.add(pvrListItem);
            }
        }
        return pvrItems;
    }
    
    public final Map<String,Object> getRequestData(){
        return this.requestedData;
    }
    
    ////////////////////////////////////////////////// Server actions part.
    
    
    /**
     * Creates data on server actions.
     * @param object 
     */
    final void createServerActionData(Object object){
        if(object instanceof JSONObject){
            createServerActionMap((JSONObject)object);
        }
    }
    
    /**
     * Creates server action data if it is an array.
     * @param object 
     */
    final void createServerActionMap(JSONObject object){
        LOG.debug("Received for createServerActionMap: " + object);
        switch((String)parsedObject.get("method")){
            case "System.OnQuit":
                serverActionData.put("ResultActionType", ResultActionType.SYSTEM_SHUTDOWN);
            break;
            case "Player.OnPlay":
                JSONObject playerData = getPlayerDataFromParams((JSONObject)parsedObject.get("params"));
                serverActionData.put("ResultActionType", ResultActionType.PLAYER_PLAY);
                serverActionData.put("playerid", (long)playerData.get("playerid"));
                currentPlayer = (long)playerData.get("playerid");
            break;
            case "Player.OnStop":
                serverActionData.put("ResultActionType", ResultActionType.PLAYER_STOP);
            break;
            case "Player.OnPause":
                serverActionData.put("ResultActionType", ResultActionType.PLAYER_PAUSE);
            break;
            case "Playlist.OnAdd":
                Map<String,Object> playlistDataItemAdd = getPlaylistDataItemAdd((JSONObject)parsedObject.get("params"));
                JSONObject playlistData = (JSONObject)((JSONObject)((JSONObject)parsedObject.get("params")).get("data")).get("item");
                LOG.debug("Add data items: {} - {}",playlistData, playlistDataItemAdd);
                if(playlistData.get("id")!=null){
                    serverActionData.put("ResultActionType", ResultActionType.PLAYLIST_ADD_ITEM_INCOMPLETE);
                    serverActionData.put("ItemType", playlistDataItemAdd.get("ItemType"));
                    serverActionData.put("id", ((Long)playlistData.get("id")).intValue());
                    serverActionData.put("playlistid", ((Long)playlistDataItemAdd.get("playlistid")).intValue());
                    serverActionData.put("playlistpos", ((Long)playlistDataItemAdd.get("playlistpos")).intValue());
                } else {
                    serverActionData.put("ResultActionType", ResultActionType.PLAYLIST_ADD_ITEM);
                    switch((ItemType)playlistDataItemAdd.get("ItemType")){
                        case AUDIO:
                            MediaAudioItem audioItem = new MediaAudioItem();
                            audioItem.setTitle((String)playlistDataItemAdd.get("title"));
                            audioItem.setArtist((String)playlistDataItemAdd.get("artist"));
                            audioItem.setAlbum((String)playlistDataItemAdd.get("album"));
                            audioItem.setPlaylistPos(((Long)playlistDataItemAdd.get("playlistpos")).intValue());
                            audioItem.setPlaylistId(((Long)playlistDataItemAdd.get("playlistid")).intValue());
                            audioItem.setDuration(0);
                            serverActionData.put("item", audioItem);
                        break;
                        case VIDEO:
                            MediaVideoItem videoItem = new MediaVideoItem();
                            videoItem.setTitle((String)playlistDataItemAdd.get("title"));
                            videoItem.setPlaylistPos(((Long)playlistDataItemAdd.get("playlistpos")).intValue());
                            videoItem.setPlaylistId(((Long)playlistDataItemAdd.get("playlistid")).intValue());
                            videoItem.setDuration(0);
                            serverActionData.put("item", videoItem);
                        break;
                        case PVR:
                            MediaPVRItem pvrItem = new MediaPVRItem();
                            pvrItem.setTitle((String)playlistDataItemAdd.get("title"));
                            pvrItem.setPlaylistPos(((Long)playlistDataItemAdd.get("channelid")).intValue());
                            pvrItem.setPlaylistId(0);
                            pvrItem.setDuration(0);
                            serverActionData.put("item", pvrItem);
                        break;
                    }
                    
                }
            break;
            case "Playlist.OnRemove":
                Map<String,Object> playlistDataItemRemove = getPlaylistDataItemRemove((JSONObject)parsedObject.get("params"));
                serverActionData.put("ResultActionType", ResultActionType.PLAYLIST_REMOVE_ITEM);
                serverActionData.put("playlistid", playlistDataItemRemove.get("playlistid"));
                serverActionData.put("playlistpos", playlistDataItemRemove.get("playlistpos"));
            break;
            case "Playlist.OnClear":
                serverActionData.put("ResultActionType", ResultActionType.PLAYLIST_CLEAR);
            break;
        }
    }
    
    /**
     * Used when there is a playlist add with only an id and a type.
     * @param resultText
     * @param type
     * @param playlistId
     * @param playlistPos
     * @return 
     * @throws org.pidome.server.connector.plugins.media.MediaException 
     */
    public static MediaItem createMediaItemByResultRequestForPlaylist(String resultText, ItemType type, int playlistId, int playlistPos) throws MediaException {
        try {
            JSONObject parsed = (JSONObject)((JSONObject)new JSONParser().parse(resultText)).get("result");
            LOG.debug("Adding for playlist: " + parsed);
            switch(type){
                case AUDIO:
                    JSONObject audioParsed = (JSONObject)parsed.get("songdetails");
                    MediaAudioItem audioItem = new MediaAudioItem();
                    if ((String) audioParsed.get("title") != null && !((String) audioParsed.get("title")).equals("")) {
                        if (audioParsed.get("artist") != null) {
                            audioItem.setTitle(jsonArrayObjectToString((JSONArray) audioParsed.get("artist")) + " - " + (String) audioParsed.get("title"));
                        } else {
                            audioItem.setTitle((String) audioParsed.get("title"));
                        }
                    } else {
                        audioItem.setTitle((String)audioParsed.get("label"));
                    }
                    audioItem.setAlbum((String) audioParsed.get("album"));
                    audioItem.setPlaylistPos(playlistPos);
                    audioItem.setPlaylistId(playlistId);
                    audioItem.setDuration(((Long)audioParsed.get("duration")).intValue());
                    audioItem.setId(((Long)audioParsed.get("songid")).intValue());
                    return audioItem;
                case VIDEO:
                    JSONObject videoParsed = (JSONObject)parsed.get("moviedetails");
                    MediaVideoItem videoItem = new MediaVideoItem();
                    videoItem.setTitle((String) videoParsed.get("title"));
                    videoItem.setPlaylistPos(playlistPos);
                    videoItem.setPlaylistId(playlistId);
                    videoItem.setDuration(((Long)videoParsed.get("runtime")).intValue());
                    videoItem.setId(((Long)videoParsed.get("movieid")).intValue());
                    return videoItem;
                case PVR:
                    JSONObject pvrParsed = (JSONObject)parsed.get("channeldetails");
                    MediaPVRItem pvrItem = new MediaPVRItem();
                    pvrItem.setTitle(new StringBuilder((String)pvrParsed.get("channeltype")).append(": ").append((String) pvrParsed.get("channel")).toString());
                    pvrItem.setPlaylistPos(playlistPos);
                    pvrItem.setPlaylistId(playlistId);
                    pvrItem.setDuration(0);
                    pvrItem.setId(((Long)pvrParsed.get("channelid")).intValue());
                    return pvrItem;
                default:
                    throw new MediaException("Unsupported/unknown item type: {}" + type.toString());
            }
        } catch (ParseException ex) {
            LOG.error("Could not parse string: {}", resultText);
            throw new MediaException("Could not parse string: " + resultText);
        }
    }
    
    /**
     * Gets information from a removed playlist item.
     * @param object
     * @return 
     */
    final Map<String,Object> getPlaylistDataItemRemove(JSONObject object){
        Map<String,Object> ItemDetails = new HashMap<>();
        JSONObject itemData = (JSONObject)object.get("data");
        ItemDetails.put("playlistid", itemData.get("playlistid"));
        ItemDetails.put("playlistpos", itemData.get("position"));
        return ItemDetails;
    }
    
    /**
     * Gets information from playlist add action
     * @param object
     * @return 
     */
    final Map<String,Object> getPlaylistDataItemAdd(JSONObject object){
        Map<String,Object> ItemDetails = new HashMap<>();
        JSONObject itemData = (JSONObject)((JSONObject)object.get("data")).get("item");
        LOG.debug("Playlist item data: {}", itemData);
        switch((String)itemData.get("type")){
            case "song":
                ItemDetails.put("ItemType", ItemType.AUDIO);
            break;
            case "channel":
                ItemDetails.put("ItemType", ItemType.PVR);
            break;
            case "movie":
                ItemDetails.put("ItemType", ItemType.VIDEO);
            break;
            default:
                ItemDetails.put("ItemType", ItemType.UNKNOWN);
            break;
        }
        if ((String) itemData.get("title") != null && !((String)itemData.get("title")).equals("")) {
            if(itemData.get("artist") != null && !(jsonArrayObjectToString((JSONArray)itemData.get("artist"))).equals("")){
                ItemDetails.put("title", jsonArrayObjectToString((JSONArray)itemData.get("artist")) + " - " + itemData.get("title"));
            } else {
                ItemDetails.put("title", itemData.get("title"));
            }
        } else {
            ItemDetails.put("title", itemData.get("label"));
        }
        JSONObject playlistData = (JSONObject)object.get("data");
        ItemDetails.put("playlistid", playlistData.get("playlistid"));
        ItemDetails.put("playlistpos", playlistData.get("position"));
        return ItemDetails;
    }
    
    /**
     * Retrieves player info from the params key when a method is received.
     * @param object
     * @return 
     */
    final JSONObject getPlayerDataFromParams(JSONObject object){
        return (JSONObject)((JSONObject)object.get("data")).get("player");
    }
    
    /**
     * Returns the server actions data.
     * @return 
     */
    public final Map<String,Object> getActionData(){
        return this.serverActionData;
    }
    
    ///////////////////////////////// Server Result parts  
    
    
    /**
     * Creates data based on result.
     * @param object 
     */
    final void createResultData(Object object){
        if(object instanceof JSONArray){
            createResultMap((JSONArray)object);
        }
    }
    
    /**
     * Fills a map with the results of the result array object.
     * @param object 
     */
    final void createResultMap(JSONArray object){
        if(!((JSONArray)object).isEmpty() && ((JSONObject)object.get(0)).containsKey("playerid")){
            String type = (String)((JSONObject)object.get(0)).get("type");
            switch (type) {
                case "audio":
                    resultData.put("resulttype", ResultInfoType.AUDIOPLAYER);
                    break;
                case "video":
                    resultData.put("resulttype", ResultInfoType.VIDEOPLAYER);
                    break;
                default:
                    resultData.put("resulttype", ResultInfoType.UNSUPPORTED);
                    break;
            }
            resultData.put("playerid"  , (long)((JSONObject)object.get(0)).get("playerid"));
            resultData.put("playertype", (String)((JSONObject)object.get(0)).get("type"));
        }
    }
    
    /**
     * Returns the server result data.
     * @return 
     */
    public final Map<String,Object> getResultData(){
        return resultData;
    }
    
    
    //// helper methods
    /**
     * Creates a comma separated string representation of a JSONArray object
     * @param object
     * @return 
     */
    static String jsonArrayObjectToString(JSONArray object){
        String line = "";
        for(int i = 0; i < object.size(); i++){
            line += ((String)object.get(i) + ", ");
        }
        if(line.contains(",")){
            return line.substring(0, line.lastIndexOf(","));
        } else {
            return line;
        }
    }
    
    
    
    /////////// Static methods for this version.
    
    /**
     * Returns a string to retrieve only the items needed for adding an audio item to the playlist.
     * @param itemId
     * @return 
     */
    public static String createGetAudioItemFromLibraryForAddPlaylist(int itemId){
        return "{\"jsonrpc\":\"2.0\",\"method\":\"AudioLibrary.GetSongDetails\",\"params\":{\"songid\":"+itemId+", \"properties\":[\"title\", \"album\", \"artist\", \"duration\"]},\"id\":\"detailsAudioForAdd\"}";
    }

    /**
     * Returns a string to retrieve only the items needed for adding an video item to the playlist.
     * @param itemId
     * @return 
     */
    public static String createGetVideoItemFromLibraryForAddPlaylist(int itemId){
        return "{\"jsonrpc\":\"2.0\",\"method\":\"VideoLibrary.GetMovieDetails\",\"params\":{\"movieid\":"+itemId+", \"properties\":[\"title\",\"runtime\"]},\"id\":\"detailsVideoForAdd\"}";
    }
    
    /**
     * Returns a string to retrieve only the items needed for adding a PVR item to the playlist.
     * @param itemId
     * @return 
     */
    public static String createGetPVRItemFromLibraryForAddPlaylist(int itemId){
        return "{\"jsonrpc\":\"2.0\",\"method\":\"PVR.GetChannelDetails\",\"params\":{\"channelid\":"+itemId+", \"properties\":[\"thumbnail\",\"channeltype\",\"channel\"]},\"id\":\"detailsPVRForAdd\"}";
    }
    
    /**
     * Returns the current audio playlist.
     * @return 
     */
    public static String getAudioPlaylist(){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.GetItems\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"duration\"], \"playlistid\": 0 }, \"id\": \"AudioPlaylist\"}";
    }
    
    /**
     * Requests the current video playlist.
     * @return 
     */
    public static String getVideoPlaylist(){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.GetItems\", \"params\": { \"properties\": [\"title\", \"runtime\"], \"playlistid\": 1 }, \"id\": \"VideoPlaylist\"}";
    }
    
    /**
     * Returns a PVR playlist, all though not a real playlist.
     * It returns the vailable channels.
     * @param channelGroupId
     * @return 
     */
    public static String getPVRPlaylist(int channelGroupId){
        return "{\"id\":\"getPVRPlaylist\",\"jsonrpc\":\"2.0\",\"method\":\"PVR.GetChannels\", \"params\": { \"channelgroupid\":"+channelGroupId+", \"properties\": [\"thumbnail\", \"channeltype\", \"channel\"]}}";
    }
    
    /**
     * Plays the last known player.
     * @param player
     * @return 
     */
    public static String playerPlay(long player){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": "+player+" }, \"id\": 1}";
    }
    
    /**
     * Pauses the current player.
     * @param player
     * @return 
     */
    public static String playerPause(long player){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.PlayPause\", \"params\": { \"playerid\": "+player+" }, \"id\": 1}";
    }
    
    /**
     * Stops the current player.
     * @param player
     * @return 
     */
    public static String playerStop(long player){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.Stop\", \"params\": { \"playerid\": "+player+" }, \"id\": 1}";
    }

    /**
     * Plays next item in playlist.
     * @param player
     * @return 
     */
    public static String playerNext(long player){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": "+player+", \"to\": \"next\" }, \"id\": 1}";
    }
    
    /**
     * Plays previous item in playlist.
     * @param player
     * @return 
     */
    public static String playerPrevious(long player){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GoTo\", \"params\": { \"playerid\": "+player+", \"to\": \"previous\" }, \"id\": 1}";
    }
    
    /**
     * Play an item in the playlist.
     * @param playlistId
     * @param playlistItemId
     * @return 
     */
    public static String playPlaylistItem(int playlistId, int playlistItemId, String type){
        switch(type){
            case "PVR":
                return "{ \"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"channelid\": "+playlistItemId+"} }, \"id\": 1 }";
            default:
                return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\": { \"playlistid\": "+playlistId+", \"position\": "+playlistItemId+" } }, \"id\": 1}";
        }
    }
    
    /**
     * Removes an item from a playlist.
     * @param playlistId
     * @param playlistItemId
     * @return 
     */
    public static String removePlaylistItem(int playlistId, int playlistItemId){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Playlist.Remove\", \"params\": { \"playlistid\": "+playlistId+", \"position\": "+playlistItemId+" }, \"id\": 1}";
    }
    
    /**
     * Returns a single audio item from the player.
     * @param playerId
     * @return 
     */
    public static String getAudioItem(long playerId){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetItem\", \"params\": { \"properties\": [\"title\", \"album\", \"artist\", \"duration\", \"albumartist\", \"thumbnail\", \"art\"], \"playerid\": "+playerId+" }, \"id\": \"AudioGetItem\"}";
    }
    
    /**
     * Returns a single video item from the player.
     * @param playerId
     * @return 
     */
    public static String getVideoItem(long playerId){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetItem\", \"params\": { \"properties\": [\"title\", \"runtime\", \"thumbnail\", \"art\"], \"playerid\": "+playerId+" }, \"id\": \"VideoGetItem\"}";
    }
    
    /**
     * Ping.
     * @return 
     */
    public static String createPingCommand() {
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"JSONRPC.Ping\", \"id\": 1 }";
    }
    
    /**
     * Left.
     * @return 
     */
    public static String createLeftCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Left\", \"id\": 1 }";
    }
    
    /**
     * Right.
     * @return 
     */
    public static String createRightCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Right\", \"id\": 1 }";
    }
    
    /**
     * Confirm (Select)
     * @return 
     */
    public static String createConfirmCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Select\", \"id\": 1 }";
    }
    
    /**
     * Up.
     * @return 
     */
    public static String createUpCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Up\", \"id\": 1 }";
    }
    
    /**
     * Down.
     * @return 
     */
    public static String createDownCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Down\", \"id\": 1 }";
    }
    
    /**
     * Down.
     * @return 
     */
    public static String createBackCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Back\", \"id\": 1 }";
    }
    
    /**
     * Down.
     * @return 
     */
    public static String createHomeCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.Home\", \"id\": 1 }";
    }

    /**
     * Down.
     * @param title
     * @param message
     * @return 
     */
    public static String createSendMessageCommand(String title, String message){
        return "{\"jsonrpc\":\"2.0\",\"method\":\"GUI.ShowNotification\",\"params\":{\"title\":\""+title+"\",\"message\":\""+message+"\"}, \"id\": 1 }";
    }
    
    /**
     * Displays the OSD.
     * @return 
     */
    public static String createSendOSDCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Input.ShowOSD\", \"id\": 1 }";
    }
    
    /**
     * Command for getting current player.
     * @return 
     */
    public static String getActivePlayers(){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.GetActivePlayers\", \"id\": 1}";
    }
    
    /**
     * Volume up
     * @return 
     */
    public static String createSendVolUpCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Application.SetVolume\", \"params\": { \"volume\": \"increment\" }, \"id\": 1 }";
    }
    
    /**
     * Volume down.
     * @return 
     */
    public static String createSendVolDownCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Application.SetVolume\", \"params\": { \"volume\": \"decrement\" }, \"id\": 1 }";
    }
    
    /**
     * Mute/unmute
     * @return 
     */
    public static String createSendMuteCommand(){
        return "{ \"jsonrpc\": \"2.0\", \"method\": \"Application.SetMute\", \"params\": { \"mute\": \"toggle\" }, \"id\": 1 }";
    }
    
    /**
     * Creates a String for returning audio/music playlists.
     * @return 
     */
    public static String createGetAudioPlaylists(){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Files.GetDirectory\", \"params\": {\"directory\": \"special://musicplaylists\"}, \"id\": \"getAudioPlaylists\"}";
}
    
    /**
     * Creates a String for returning video playlists.
     * @return 
     */
    public static String createGetVideoPlaylists(){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"Files.GetDirectory\", \"params\": {\"directory\": \"special://videoplaylists\"}, \"id\": \"getVideoPlaylists\"}";
    }
    
    /**
     * Opens an xbmc playlist by id or playlistfile
     * @param playlistId
     * @param playlistType
     * @param playlistFile
     * @return 
     */
    public static String createOpenPlaylist(Long playlistId, String playlistType, String playlistFile){
        switch(playlistType){
            case "PVR":
                return getPVRPlaylist(playlistId.intValue());
            default:
                if(playlistFile==null){
                    return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\": { \"item\" : { \"playlistid\" : "+playlistId+" }, \"id\": \"Player.Openplaylist\" }";
                } else {
                    return "{\"jsonrpc\": \"2.0\", \"method\": \"Player.Open\", \"params\" : { \"item\" : { \"file\": \""+playlistFile+"\" } }, \"id\": 1}";
                }
        }
    }
    
    /**
     * Returns a list of available PVR channels.
     * @return 
     */
    public static String createGetPVRChannelLists(){
        return "{\"jsonrpc\": \"2.0\", \"method\": \"PVR.GetChannelGroups\", \"id\":\"PVR.GetChannelGroups\", \"params\":{\"channeltype\":\"tv\"}}";
    }
    
    /**
     * Returns command to open a PVR channel.
     * @param channelId
     * @return 
     */
    public static String createOpenPVRChannel(Long channelId){
        return "{\"id\":1,\"jsonrpc\":\"2.0\",\"method\":\"Player.Open\",\"params\":{\"item\":{\"channelid\":"+channelId+"}}}";
    }
    
}
