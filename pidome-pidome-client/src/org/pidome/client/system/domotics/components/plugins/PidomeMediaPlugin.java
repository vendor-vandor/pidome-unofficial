/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomoticsHttpComponentParser;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.domotics.components.plugins.MediaPlayer.Item;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.w3c.dom.Node;

/**
 *
 * @author John
 */
public final class PidomeMediaPlugin implements DomComponent,ClientDataConnectionListener {
    
    static ObservableList<MediaPlayer> mediaList = FXCollections.observableArrayList();
    
    static Logger LOG = LogManager.getLogger(PidomeMediaPlugin.class);

    public enum MediaItemType {
        AUDIO, VIDEO, PVR, UNKNOWN;
    }
    
    public PidomeMediaPlugin(){
        ClientData.addClientDataConnectionListener(this);
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        switch(event.getEventType()){
            case ClientDataConnectionEvent.PLUGINRECEIVED:
                switch(event.getMethod()){
                    case "setFavorite":
                        Map<String,Object> params = event.getParams();
                        int mediaId = ((Long)params.get("id")).intValue();
                        for (MediaPlayer mediaItem : mediaList) {
                            if(mediaItem.getId()==mediaId){
                                mediaItem.updateFavorite((boolean)params.get("favorite"));
                            }
                        }
                    break;
                }
            case ClientDataConnectionEvent.MEDIARECEIVED:
                Map<String,Object> params = event.getParams();
                int mediaId = ((Long)params.get("id")).intValue();
                switch(event.getMethod()){
                    case "getCurrentMedia":
                        for (MediaPlayer mediaList1 : mediaList) {
                            if (mediaList1.getId() == mediaId) {
                                Item item = mediaList1.newItem((String)params.get("itemtype"));
                                item.setTitle((String) params.get("title"));
                                item.setTitleArtist((String) params.get("title_artist"));
                                item.setAlbum((String) params.get("album"));
                                item.setAlbumArtist((String) params.get("album_artist"));
                                item.setDuration(((Long) params.get("duration")).intValue());
                                mediaList1.getPlayingData().set(item);
                                mediaList1.sendUpdate();
                            }
                        }
                    break;
                    case "playerCommand":
                        if(params.get("command").equals("STOP")){
                            for(int i = 0; i<mediaList.size();i++){
                                if(mediaList.get(i).getId()==mediaId){
                                    mediaList.get(i).getPlayingData().clear();
                                    mediaList.get(i).sendUpdate();
                                }
                            }
                        }
                    break;
                    case "addPlaylistItem":
                        LOG.debug("Adding item to playlist: {}, {}", mediaId, params);
                        for(int i = 0; i<mediaList.size();i++){
                            if(mediaList.get(i).getId()==mediaId){
                                Item item = mediaList.get(i).newItem((String)params.get("itemtype"));
                                item.setItemId(((Long) params.get("itemid")).intValue());
                                item.setPlaylistId(((Long) params.get("playlist")).intValue());
                                item.setPlaylistPos(((Long) params.get("pos")).intValue());
                                item.setTitle((String) params.get("title"));
                                item.setDuration(((Long)params.get("duration")).intValue());
                                mediaList.get(i).getPlayList().add(item);
                            }
                        }
                    break;
                    case "removePlaylistItem":
                        LOG.debug("Removing item from playlist: {}, {}", mediaId, params);
                        for(int i = 0; i<mediaList.size();i++){
                            if(mediaList.get(i).getId()==mediaId){
                                int itemPos = ((Long) params.get("pos")).intValue();
                                Item itemToRemove = null;
                                for(int l = 0; l<mediaList.get(i).getPlayList().getList().size();l++){
                                    if(mediaList.get(i).getPlayList().getList().get(l).getPlaylistPos()== itemPos){
                                        itemToRemove = mediaList.get(i).getPlayList().getList().get(l);
                                    }
                                }
                                if(itemToRemove!=null){
                                    mediaList.get(i).getPlayList().remove(itemToRemove);
                                }
                            }
                        }
                    break;
                    case "playlistCleared":
                        LOG.debug("Clearing playlist for media id: {}", mediaId);
                        for(int i = 0; i<mediaList.size();i++){
                            if(mediaList.get(i).getId()==mediaId){
                                mediaList.get(i).getPlayList().clear();
                            }
                        }
                }
            break;
        }
    }
    
    public final void deleteMedia(int mediaId){
        ArrayList<MediaPlayer> removeList = new ArrayList();
        for(int i=0; i< mediaList.size(); i++){
            if(mediaList.get(i).getId()==mediaId){
                mediaList.get(i).getPlayList().clear();
                mediaList.get(i).getPlayingData().clear();
                removeList.add(mediaList.get(i));
            }
        }
        LOG.debug("Got list to remove: {}", removeList);
        if(!removeList.isEmpty()){
            mediaList.removeAll(removeList);
        }
    }
    
    public final void createMedia(Node mediaNode){
        if(mediaNode.getNodeName().equals("mediaplugin")){
            try {
                Map<String,String> instanceDetails = DomoticsHttpComponentParser.getNodeAttributes(mediaNode);
                MediaPlayer player = new MediaPlayer(Integer.parseInt(instanceDetails.get("id")));
                player.setActive(instanceDetails.get("active").equals("true"));
                player.setName(instanceDetails.get("name"));
                player.setLocationId(Integer.parseInt(instanceDetails.get("location")));
                player.updateFavorite(true);
                if(mediaNode.hasChildNodes()){
                    for(int i = 0; i < mediaNode.getChildNodes().getLength(); i++){
                        Node element = mediaNode.getChildNodes().item(i);
                        if(element.getNodeType()==Node.ELEMENT_NODE){
                            switch(element.getNodeName()){
                                case "playing":
                                    if(element.hasChildNodes()){
                                        for(int j = 0; j < element.getChildNodes().getLength(); j++){
                                            if(element.getChildNodes().item(j).getNodeType()== Node.ELEMENT_NODE && element.getChildNodes().item(j).getNodeName().equals("item")){
                                                Map<String,String> nowData = DomoticsHttpComponentParser.getNodeAttributes(element.getChildNodes().item(j));
                                                Item item = player.newItem(nowData.get("type")); 
                                                item.setTitle(nowData.get("title"));
                                                item.setTitleArtist(nowData.get("title_artist"));
                                                item.setAlbum(nowData.get("album"));
                                                item.setAlbumArtist(nowData.get("album_artist"));
                                                item.setDuration(Integer.parseInt(nowData.get("duration")));
                                                player.getPlayingData().set(item);
                                            }
                                        }
                                    }
                                break;
                                case "playlist":
                                    if(element.hasChildNodes()){
                                        for(int j = 0; j < element.getChildNodes().getLength(); j++){
                                            if(element.getChildNodes().item(j).getNodeType()== Node.ELEMENT_NODE && element.getChildNodes().item(j).getNodeName().equals("item")){
                                                Map<String,String> nowData = DomoticsHttpComponentParser.getNodeAttributes(element.getChildNodes().item(j));
                                                LOG.debug("Having playlist data: {}", nowData);
                                                Item item = player.newItem(nowData.get("type"));
                                                item.setTitle(nowData.get("title"));
                                                item.setItemId(Integer.parseInt(nowData.get("itemid")));
                                                item.setPlaylistPos(Integer.parseInt(nowData.get("pos")));
                                                item.setPlaylistId(Integer.parseInt(nowData.get("playlistid")));
                                                item.setDuration(Integer.parseInt(nowData.get("duration")));
                                                player.getPlayList().add(item);
                                            }
                                        }
                                    }
                                break;
                            }
                        }
                    }
                }
                mediaList.add(player);
            } catch (Exception ex){
                LOG.error("Error instantiating a new media player data collection: {}", ex.getMessage(), ex);
            }
        }
    }
    
    public static ObservableList<MediaPlayer> getMediaList(){
        return FXCollections.unmodifiableObservableList(mediaList);
    }
    
    public static MediaPlayer getMediaPlayer(int playerId) throws PidomeMediaPluginException {
        for(int i = 0; i < mediaList.size(); i++){
            if(mediaList.get(i).getId()==playerId){
                return mediaList.get(i);
            }
        }
        throw new PidomeMediaPluginException("Media plugin with id " + playerId + " does not exist" );
    }
    
    public static void sendServerCommand(String command, int playerId) {
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("command", command);
                put("id", playerId);
            }
        };
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("MediaService.serverCommand", "MediaService.serverCommand", sendObject));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not media servercommand: {}", sendObject);
        }
    }
    
    public static void sendPlayerCommand(String command, int playerId) {
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("command", command);
                put("id", playerId);
            }
        };
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("MediaService.playerCommand", "MediaService.playerCommand", sendObject));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not media servercommand: {}", sendObject);
        }
    }
    
    public static void playPlaylistItem(int item, int playlistId, int playerId, PidomeMediaPlugin.MediaItemType itemType) {
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", playerId);
                put("playlist", playlistId);
                put("pos", item);
                put("type", itemType.toString());
            }
        };
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("MediaService.playPlaylistItem", "MediaService.playPlaylistItem", sendObject));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not media servercommand: {}", sendObject);
        }
    }
    
    public static void removePlaylistItem (int item, int playlistId, int playerId) {
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", playerId);
                put("playlist", playlistId);
                put("pos", item);
            }
        };
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("MediaService.removePlaylistItem", "MediaService.removePlaylistItem", sendObject));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not media servercommand: {}", sendObject);
        }
    }
}
