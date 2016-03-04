/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.components.plugins.MediaPlayer;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPlugin;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPluginException;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPluginListener;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.desktop.NewDesktopShortcut;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.windows.TitledWindow;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John
 */
public class MediaListing extends TitledWindow implements ListChangeListener {

    FilteredList list = new FilteredList(null);
    ObservableMap<Integer,MediaItem> mediaRows = FXCollections.observableHashMap();
    
    static Logger LOG = LogManager.getLogger(MediaListing.class);
    
    final MapChangeListener addRemoveMedia = this::mediaAddRemoveHelper;
    
    public MediaListing(Object... numbs) throws Exception {
        this();
    }
    
    public MediaListing() {
        super("All media", "All media");
        list.setListSize(650, 361);
        list.isCategorized(true);
        list.build();
    }

    final void mediaAddRemoveHelper(MapChangeListener.Change<? extends Integer, ? extends MediaItem> change){
        if (change.wasAdded()) {
            Platform.runLater(() -> {
                list.addItem(createPlayerItem(change.getValueAdded()));
            });
            change.getValueAdded().startListener();
        } else if (change.wasRemoved()) {
            change.getValueRemoved().removeListener();
            Platform.runLater(() -> {
                try { list.removeItem(String.valueOf(change.getValueRemoved().getMediaId())); } catch (Exception ex) {}
            });
        }
    }
    
    final FilteredListItem createPlayerItem(MediaItem mediaItem){
        Map<String,Object> shortcutOptions = new HashMap<>();
        shortcutOptions.put("id", mediaItem.getMediaId());
        shortcutOptions.put("favorite", true);
        NewDesktopShortcut creator = new NewDesktopShortcut(mediaItem);
        creator.setServerCall("PluginService.setFavorite", shortcutOptions);
        creator.setIconType("icon_media");
        FilteredListItem item = new FilteredListItem(String.valueOf(mediaItem.getMediaId()), String.valueOf(mediaItem.getMediaId()), "Player type", "XBMC");
        item.setContent(mediaItem);
        return item;
    }
    
    @Override
    protected void setupContent() {
        mediaRows.addListener(addRemoveMedia);
        List<MediaPlayer> mediaList = PidomeMediaPlugin.getMediaList();
        for (MediaPlayer mediaListItem : mediaList) {
            MediaItem item = new MediaItem(mediaListItem);
            item.updateCurrentData();
            item.build();
            mediaRows.put(item.getMediaId(), item);
        }
        setContent(list);
        PidomeMediaPlugin.getMediaList().addListener(this);
    }
    
    @Override
    protected void removeContent() {
        mediaRows.clear();
        mediaRows.removeListener(addRemoveMedia);
        PidomeMediaPlugin.getMediaList().removeListener(this);
        list.destroy();
        list = null;
    }

    @Override
    public void onChanged(Change change) {
        while(change.next()){
            if(change.wasAdded()){
                List<MediaPlayer> items = change.getAddedSubList();
                for(MediaPlayer item:items){
                    Platform.runLater(() -> {
                        MediaItem mediaItem = new MediaItem(item);
                        mediaItem.updateCurrentData();
                        mediaItem.build();
                        mediaRows.put(mediaItem.getMediaId(), mediaItem);
                    });
                }
            } else if(change.wasRemoved()){
                List<MediaPlayer> items = change.getRemoved();
                for(MediaPlayer item:items){
                    Platform.runLater(() -> {
                        mediaRows.remove(item.getId());
                    });
                }
            }
        }
    }
    
    class MediaItem extends HBox implements PidomeMediaPluginListener,ListChangeListener {
        
        ImageView currentIcon = new ImageView();
        
        MediaPlayer player;
        
        Label playingCurrent = new Label();
        Label playlistText = new Label();
        int PlaylistCount = 0;
        
        public MediaItem(MediaPlayer player){
            setAlignment(Pos.CENTER_LEFT);
            this.player = player;
        }
        
        final MediaPlayer getPlayer(){
            return this.player;
        }
        
        final void mediaPlayerOpenerHelper(MouseEvent t){
            try {
                MediaPlayerDisplay mediaWindow;
                mediaWindow = new MediaPlayerDisplay(player.getId());
                WindowManager.openWindow(mediaWindow);
            } catch (PidomeMediaPluginException ex) {

            }
        }
        
        public final int getMediaId(){
            return player.getId();
        }
        
        public final void updateCurrentData(){
            if(player.getActive()==true){
                if(player.getPlayingData().isPlaying()==true){
                    switch(player.getPlayingData().get().getType()){
                        case AUDIO:
                            currentIcon.setImage(new ImageLoader("icons/audio.png", 46, 38).getImage());
                        break;
                        case VIDEO:
                            currentIcon.setImage(new ImageLoader("icons/video.png", 46, 38).getImage());
                        break;
                        default:
                            currentIcon.setImage(new ImageLoader("icons/unknown.png", 46, 38).getImage());
                        break;
                    }
                    if(!player.getPlayingData().get().getTitleArtist().equals("")){
                        playingCurrent.setText("Playing: " + player.getPlayingData().get().getTitleArtist() + " - " + player.getPlayingData().get().getTitle());
                    } else {
                        playingCurrent.setText("Playing: " + player.getPlayingData().get().getTitle());
                    }
                    playlistText.setText(" ("+player.getPlayList().itemsCount()+" playlist items)");
                } else {
                    currentIcon.setImage(new ImageLoader("icons/on.png", 46, 38).getImage());
                    playingCurrent.setText("Nothing playing now");
                    playlistText.setText(" ("+player.getPlayList().itemsCount()+" playlist items)");
                }
            } else {
                currentIcon.setImage(new ImageLoader("icons/off.png", 46, 38).getImage());
                playingCurrent.setText("Offline or plugin not running");
                playlistText.setText("");
            }
        }
        
        public final void build(){
            HBox details = new HBox(2);
            details.getChildren().addAll(playingCurrent,playlistText);
            VBox mediaItem = new VBox();
            mediaItem.getChildren().addAll(new Label(player.getName()), details);
            getChildren().addAll(currentIcon, mediaItem);
        }
        
        public final void startListener(){
            addEventFilter(MouseEvent.MOUSE_CLICKED,(this::mediaPlayerOpenerHelper));
            player.addListener(this);
            player.getPlayList().getList().addListener(this);
        }
        
        public final void removeListener(){
            player.removeListener(this);
            player.getPlayList().getList().removeListener(this);
            addEventFilter(MouseEvent.MOUSE_CLICKED,(this::mediaPlayerOpenerHelper));
        }

        @Override
        public void handlePluginUpdate() {
            Platform.runLater(() -> {
                updateCurrentData();
            });
        }

        @Override
        public void onChanged(Change change) {
            PlaylistCount = change.getList().size();
            Platform.runLater(() -> {
                playlistText.setText(" ("+PlaylistCount+" playlist items)");
            });
        }
        
    }
    
}
