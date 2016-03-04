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
package org.pidome.client.system.domotics.components.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopBase;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopIcon;
import org.pidome.client.system.scenes.components.mainstage.desktop.DraggableIconInterface;

/**
 *
 * @author John
 */
public class MediaPlayer implements DraggableIconInterface {

    int playerId;
    boolean active;
    String name;
    String description;
    int locationId;
    boolean isFavorite = false;

    DesktopIcon desktopIcon;

    NowPlaying current = new NowPlaying();
    PlayList playlist = new PlayList();

    List<PidomeMediaPluginListener> listeners = new ArrayList<>();

    static Logger LOG = LogManager.getLogger(MediaPlayer.class);
    
    public MediaPlayer(int mediaIds) {
        this.playerId = mediaIds;
    }

    public final void updateFavorite(boolean favorite) {
        isFavorite = favorite;
        handleShortCut();
    }

    final void handleShortCut() {
        handleShortCut(false);
    }

    public final boolean isFavorite() {
        return isFavorite;
    }

    final void handleShortCut(boolean removed) {
        if (!removed && isFavorite() && desktopIcon == null) {
            ArrayList mediaData = new ArrayList();
            mediaData.add(String.valueOf(this.playerId));
            desktopIcon = new DesktopIcon(this, "icon_media", getName(), "org.pidome.client.system.scenes.components.mainstage.displays.MediaPlayerDisplay", mediaData);
            DesktopBase.addDesktopIcon(desktopIcon);
        } else if ((!isFavorite() || removed) && desktopIcon != null) {
            DesktopBase.removeDesktopIcon(desktopIcon);
            desktopIcon = null;
        }
    }

    @Override
    public void iconRemoved() {
        Map<String, Object> serverParams = new HashMap<>();
        serverParams.put("id", getId());
        serverParams.put("favorite", false);
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("PluginService.setFavorite", "PluginService.setFavorite", serverParams));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send device favorite:false");
        }
    }

    @Override
    public void iconAdded() {
        Map<String, Object> serverParams = new HashMap<>();
        serverParams.put("id", getId());
        serverParams.put("favorite", true);
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("DeviceService.setFavorite", "DeviceService.setFavorite", serverParams));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send device favorite:false");
        }
    }

    public final int getId() {
        return this.playerId;
    }

    public final void addListener(PidomeMediaPluginListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public final void removeListener(PidomeMediaPluginListener l) {
        if (listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    public final Item newItem(String type) {
        return new Item(type);
    }

    public final NowPlaying getPlayingData() {
        return current;
    }

    public final PlayList getPlayList() {
        return this.playlist;
    }

    protected final void sendUpdate() {
        Iterator _listeners = listeners.iterator();
        while (_listeners.hasNext()) {
            ((PidomeMediaPluginListener) _listeners.next()).handlePluginUpdate();
        }
    }

    protected final void setActive(boolean active) {
        this.active = active;
    }

    protected final void setName(String name) {
        this.name = name;
    }

    protected final void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    protected final void setDescription(String description) {
        this.description = description;
    }

    public final boolean getActive() {
        return this.active;
    }

    public final String getName() {
        return this.name;
    }

    public final int getLocationId() {
        return this.locationId;
    }

    public final String getDescription() {
        return this.description;
    }

    public final class NowPlaying {

        Item playingItem;

        public NowPlaying() {
        }

        public final boolean isPlaying() {
            return playingItem != null;
        }

        public final void clear() {
            playingItem = null;
        }

        public final void set(Item item) {
            playingItem = item;
        }

        public final Item get() {
            return playingItem;
        }

    };

    public final class PlayList {

        ObservableList<Item> items = FXCollections.observableArrayList();

        int playlistId;

        public PlayList() {
        }

        public final ObservableList<Item> getList() {
            return items;
        }

        public final int itemsCount() {
            return items.size();
        }

        public final void add(Item item) {
            items.add(item);
        }

        public final void remove(Item item) {
            items.remove(item);
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getPlaylistPos() > item.getPlaylistPos()) {
                    items.get(i).setPlaylistPos(items.get(i).getPlaylistPos() - 1);
                }
            }
        }

        public final void clear() {
            items.clear();
        }

    }

    public final class Item {

        String title = "";
        String album = "";
        int duration = 0;
        String titleArtist = "";
        String albumArtist = "";

        String durationString = "Unknown";

        int playlistId = 0;
        int itemId = 0;
        int playlistPos = 0;

        String uniqueId = UUID.randomUUID().toString();

        PidomeMediaPlugin.MediaItemType itemType;

        public Item(String itemType) {
            switch (itemType) {
                case "AUDIO":
                    this.itemType = PidomeMediaPlugin.MediaItemType.AUDIO;
                    break;
                case "VIDEO":
                    this.itemType = PidomeMediaPlugin.MediaItemType.VIDEO;
                    break;
                case "PVR":
                    this.itemType = PidomeMediaPlugin.MediaItemType.PVR;
                    break;
                default:
                    this.itemType = PidomeMediaPlugin.MediaItemType.UNKNOWN;
                    break;
            }
        }

        public final String getUniqueId() {
            return uniqueId;
        }

        public final PidomeMediaPlugin.MediaItemType getType() {
            return itemType;
        }

        protected final void setPlaylistPos(int pos) {
            this.playlistPos = pos;
        }

        protected final void setTitle(String title) {
            this.title = title;
        }

        protected final void setTitleArtist(String titleArtist) {
            this.titleArtist = titleArtist;
        }

        protected final void setAlbum(String album) {
            this.album = album;
        }

        protected final void setAlbumArtist(String albumArtist) {
            this.albumArtist = albumArtist;
        }

        protected final void setDuration(int duration) {
            this.duration = duration;
            int hr = duration / 3600;
            int rem = duration % 3600;
            int mn = rem / 60;
            int sec = rem % 60;
            durationString = (hr != 0 ? ((hr < 10 ? "0" : "") + hr + ":") : "") + (mn < 10 ? "0" : "") + mn + ":" + (sec < 10 ? "0" : "") + sec;
        }

        protected final void setPlaylistId(int id) {
            playlistId = id;
        }

        protected final void setItemId(int id) {
            this.itemId = id;
        }

        public final int getPlaylistPos() {
            return this.playlistPos;
        }

        public final String getTitle() {
            return this.title;
        }

        public final String getTitleArtist() {
            return this.titleArtist;
        }

        public final String getAlbum() {
            return this.album;
        }

        public final String getAlbumArtist() {
            return this.albumArtist;
        }

        public final int getDuration() {
            return this.duration;
        }

        public final int getPlaylistId() {
            return this.playlistId;
        }

        public final int getItemId() {
            return this.itemId;
        }

        public final String getDurationString() {
            return durationString;
        }

        public final PidomeMediaPlugin.MediaItemType getItemType(){
            return this.itemType;
        }
        
    }

}
