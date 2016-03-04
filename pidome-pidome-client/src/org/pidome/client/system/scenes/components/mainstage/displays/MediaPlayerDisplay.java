/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

import java.util.List;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.domotics.components.plugins.MediaPlayer;
import org.pidome.client.system.domotics.components.plugins.MediaPlayer.Item;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPlugin;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPluginException;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPluginListener;
import org.pidome.client.system.scenes.components.controls.DefaultButton;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredListItem;
import org.pidome.client.system.scenes.components.mainstage.displays.components.lists.FilteredList;
import org.pidome.client.system.scenes.windows.TitledWindow;

/**
 *
 * @author John
 */
public class MediaPlayerDisplay extends TitledWindow implements ListChangeListener,PidomeMediaPluginListener {

    MediaPlayer player;
    
    FilteredList list = new FilteredList(null);
    
    GridPane currentPlayingBox = new GridPane();
    
    Label currentTitle       = new Label();
    Label currentTitleArtist = new Label();
    Label currentAlbum       = new Label();
    Label currentDuration    = new Label();
    
    Label title = new Label("Title:");
    Label titleArtist = new Label("Artist:");
    Label titleAlbum = new Label("Album:");
    Label duration = new Label("Duration:");
    
    String currentType;
    
    double widthRatio = DisplayConfig.getWidthRatio();
    double heightRatio = DisplayConfig.getHeightRatio();
    
    static Logger LOG = LogManager.getLogger(MediaPlayerDisplay.class);
    
    public MediaPlayerDisplay(Object... mediaIds) throws Exception {
        this(Integer.valueOf((String)mediaIds[0]));
    }
    
    public MediaPlayerDisplay(int playerId) throws PidomeMediaPluginException {
        super("Media player: " + playerId, PidomeMediaPlugin.getMediaPlayer(playerId).getName());
        setId("mediaplayer");
        list.setListSize(650, 361);
        list.build();
        player = PidomeMediaPlugin.getMediaPlayer(playerId);
        currentPlayingBox.setMinHeight(80 * DisplayConfig.getHeightRatio());
        currentPlayingBox.setMinWidth(398 * DisplayConfig.getWidthRatio());
        currentPlayingBox.setMaxWidth(398 * DisplayConfig.getWidthRatio());
        currentPlayingBox.setVgap(3);
        currentPlayingBox.setHgap(3);
        setCurrentPlaingData();
        
        currentTitle.getStyleClass().add("title");
        currentTitleArtist.getStyleClass().add("title");
        currentAlbum.getStyleClass().add("title");
        title.getStyleClass().add("title");
        titleArtist.getStyleClass().add("title");
        titleAlbum.getStyleClass().add("title");

    }
    
    @Override
    protected void setupContent() {
        VBox skeleton = new VBox();
        setPlayerPlaylist();
        HBox buttonBar = new HBox(5 * heightRatio);
        DefaultButton switcher = new DefaultButton("Show playlist");
        list.visibleProperty().setValue(Boolean.FALSE);
        switcher.setOnAction((ActionEvent event) -> {
            if(list.visibleProperty().getValue().equals(Boolean.FALSE)){
                switcher.setText("Hide playlist");
                skeleton.getChildren().add(list);
            } else {
                switcher.setText("Show playlist");
                skeleton.getChildren().remove(list);
            }
            list.visibleProperty().setValue(!list.visibleProperty().getValue());
            LOG.debug("Set content size: {}", skeleton.getHeight());
        });
        switcher.setMinWidth(100 * widthRatio);
        buttonBar.getChildren().add(switcher);
        buttonBar.getStyleClass().add("buttonbar");
        buttonBar.setPadding(new Insets(3 * heightRatio,5 * widthRatio,5 * heightRatio,5 * widthRatio));
        skeleton.getChildren().addAll(getPlayerHeader(),buttonBar);
        setContent(skeleton);
        player.addListener(this);
        player.getPlayList().getList().addListener(this);
        PidomeMediaPlugin.getMediaList().addListener((ListChangeListener)(Change change) -> {
            while(change.next()){
                if(change.wasRemoved()){
                    List<MediaPlayer> items = change.getRemoved();
                    items.stream().filter((item) -> (item.getId()==player.getId())).forEach((_item) -> {
                        close("null");
                    });
                }
            }
        });
    }
    
    final void setCurrentPlaingData(){
        currentPlayingBox.getChildren().clear();
        if(player.getActive()){
            if(player.getPlayingData().isPlaying()){
                Item playing = player.getPlayingData().get();
                currentTitle.setText(playing.getTitle());
                currentDuration.setText(playing.getDurationString());
                switch(playing.getType()){
                    case AUDIO:
                        currentTitleArtist.setText(playing.getTitleArtist());
                        currentAlbum.setText(playing.getAlbum());
                        currentPlayingBox.add(title, 0, 0);
                        currentPlayingBox.add(currentTitle, 1, 0);
                        
                        currentPlayingBox.add(titleArtist, 0, 1);
                        currentPlayingBox.add(currentTitleArtist, 1, 1);

                        currentPlayingBox.add(titleAlbum, 0, 2);
                        currentPlayingBox.add(currentAlbum, 1, 2);
                        
                        currentPlayingBox.add(duration, 0, 3);
                        currentPlayingBox.add(currentDuration, 1, 3);
                        
                    break;
                    case VIDEO:
                        currentPlayingBox.add(title, 0, 0);
                        currentPlayingBox.add(currentTitle, 1, 0);
                        
                        currentPlayingBox.add(duration, 0, 1);
                        currentPlayingBox.add(currentDuration, 1, 1);
                    break;
                        ///getDurationString
                }
            } else {
                currentTitle.setText("Nothing playing");
                currentPlayingBox.add(currentTitle, 0, 0);
            }
        } else {
            currentTitle.setText("Not online or not active");
            currentPlayingBox.add(currentTitle, 0, 0);
        }
    }
    
    final HBox getPlayerHeader(){
        HBox skeleton = new HBox(22 * widthRatio);
        skeleton.getStyleClass().add("player");
        skeleton.getChildren().addAll(getLeftControls(),getNavBox(),getPlayerBox(),getrightControls());
        return skeleton;
    }
    
    final VBox getLeftControls(){
        VBox controlSkeleton = new VBox(5 * heightRatio);
        ImageView homeButton = new ImageView(new ImageLoader("icons/home.png", 46, 38).getImage());
        homeButton.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("HOME", player.getId());
        });
        ImageView backButton = new ImageView(new ImageLoader("icons/back.png", 46, 38).getImage());
        backButton.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("BACK", player.getId());
        });
        ImageView osdButton = new ImageView(new ImageLoader("icons/osd.png", 46, 38).getImage());
        osdButton.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("OSD", player.getId());
        });
        controlSkeleton.getChildren().addAll(homeButton,backButton,osdButton);
        return controlSkeleton;
    }
    
    final GridPane getNavBox(){
        GridPane gp = new GridPane();
        gp.setHgap(5 * widthRatio);
        gp.setVgap(5 * heightRatio);
        
        ImageView up = new ImageView(new ImageLoader("icons/up.png", 46, 38).getImage());
        up.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("UP", player.getId());
        });
        ImageView left = new ImageView(new ImageLoader("icons/left.png", 46, 38).getImage());
        left.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("LEFT", player.getId());
        });
        ImageView ok = new ImageView(new ImageLoader("icons/ok.png", 46, 38).getImage());
        ok.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("CONFIRM", player.getId());
        });
        ImageView right = new ImageView(new ImageLoader("icons/right.png", 46, 38).getImage());
        right.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("RIGHT", player.getId());
        });
        ImageView down = new ImageView(new ImageLoader("icons/down.png", 46, 38).getImage());
        down.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("DOWN", player.getId());
        });
        
        gp.add(up, 1, 0); 
        gp.add(left, 0, 1); 
        gp.add(ok, 1, 1); 
        gp.add(right, 2, 1); 
        gp.add(down, 1, 2); 
        
        return gp;
    }
    
    final VBox getPlayerBox(){
        VBox playerBox = new VBox(5 * heightRatio);        
        
        HBox playerControls = new HBox(5 * widthRatio);
        ImageView seekback = new ImageView(new ImageLoader("icons/seekback.png", 46, 38).getImage());
        seekback.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendPlayerCommand("PREV", player.getId());
        });
        ImageView stop = new ImageView(new ImageLoader("icons/stop.png", 46, 38).getImage());
        stop.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendPlayerCommand("STOP", player.getId());
        });
        ImageView pause = new ImageView(new ImageLoader("icons/pause.png", 46, 38).getImage());
        pause.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendPlayerCommand("PAUSE", player.getId());
        });
        ImageView play = new ImageView(new ImageLoader("icons/play.png", 46, 38).getImage());
        play.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendPlayerCommand("PLAY", player.getId());
        });
        ImageView seeknext = new ImageView(new ImageLoader("icons/seeknext.png", 46, 38).getImage());
        seeknext.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendPlayerCommand("NEXT", player.getId());
        });
        playerControls.getChildren().addAll(seekback,stop,pause,play,seeknext);
        
        playerBox.getChildren().addAll(currentPlayingBox,playerControls);
        return playerBox;
    }
    
    final VBox getrightControls(){
        VBox controlSkeleton = new VBox(5 * heightRatio);
        ImageView volupButton = new ImageView(new ImageLoader("icons/volup.png", 46, 38).getImage());
        volupButton.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("VOLUP", player.getId());
        });
        ImageView mute_unmuteButton = new ImageView(new ImageLoader("icons/unmuted.png", 46, 38).getImage());
        mute_unmuteButton.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("MUTE", player.getId());
        });
        ImageView voldownButton = new ImageView(new ImageLoader("icons/voldown.png", 46, 38).getImage());
        voldownButton.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.sendServerCommand("VOLDOWN", player.getId());
        });
        controlSkeleton.getChildren().addAll(volupButton,mute_unmuteButton,voldownButton);
        return controlSkeleton;
    }
    
    final void setPlayerPlaylist(){
        ObservableList<Item> itemsList = player.getPlayList().getList();
        for (Item mediaItem : itemsList) {
            list.addItem(createPlaylistItem(mediaItem));
        }
        setToolbarText(player.getName() + " with " + player.getPlayList().getList().size() + " playlist items");
    }
    
    final FilteredListItem createPlaylistItem(Item mediaItem){
        FilteredListItem item = new FilteredListItem(String.valueOf(mediaItem.getUniqueId()), String.valueOf(mediaItem.getUniqueId()), "playlist", "Playlist");
        item.setContent(getPlayListItem(mediaItem));
        return item;
    }
    
    @Override
    public void onChanged(Change change) {
        while(change.next()){
            if(change.wasAdded()){
                List<Item> items = change.getAddedSubList();
                for(Item item:items){
                    Platform.runLater(() -> {
                        list.addItem(createPlaylistItem(item));
                    });
                }
            } else if(change.wasRemoved()){
                List<Item> items = change.getRemoved();
                for(Item item:items){
                    Platform.runLater(() -> {
                        list.removeItem(String.valueOf(item.getUniqueId()));
                    });
                }
            }
        }
        setToolbarText(player.getName() + " with " + player.getPlayList().getList().size() + " playlist items");
    }
    
    final HBox getPlayListItem(Item item){
        HBox itemRow = new HBox();
        ImageView curIcon = new ImageView();
        switch (item.getType()) {
            case AUDIO:
                curIcon.setImage(new ImageLoader("icons/audio.png", 46, 38).getImage());
                break;
            case VIDEO:
                curIcon.setImage(new ImageLoader("icons/video.png", 46, 38).getImage());
                break;
            default:
                curIcon.setImage(new ImageLoader("icons/unknown.png", 46, 38).getImage());
                break;
        }
        VBox titleRow = new VBox();
        titleRow.setPrefWidth(512 * widthRatio);
        titleRow.setMinWidth(Region.USE_PREF_SIZE);
        titleRow.setMaxWidth(Region.USE_PREF_SIZE);
        titleRow.getChildren().addAll(new Label(item.getTitle()),new Label("Duration: " + item.getDurationString()));
        
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        ImageView playItem = new ImageView(new ImageLoader("icons/play.png", 46, 38).getImage());
        playItem.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.playPlaylistItem(item.getPlaylistPos(), item.getPlaylistId(), player.getId(), item.getItemType());
        });
        ImageView deleteItem = new ImageView(new ImageLoader("icons/delete.png", 46, 38).getImage());
        deleteItem.setOnMouseClicked((MouseEvent t) -> {
            PidomeMediaPlugin.removePlaylistItem(item.getPlaylistPos(), item.getPlaylistId(), player.getId());
        });
        actions.getChildren().addAll(playItem,deleteItem);
        
        itemRow.getChildren().addAll(curIcon, titleRow, actions);
        return itemRow;
    }
    
    @Override
    protected void removeContent() {
        player.getPlayList().getList().removeListener(this);
        player.removeListener(this);
    }

    @Override
    public void handlePluginUpdate() {
        Platform.runLater(() -> {
            setCurrentPlaingData();
        });
    }

}
