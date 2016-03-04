/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.media;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.plugins.media.MediaData;
import org.pidome.client.entities.plugins.media.MediaPlugin;
import org.pidome.client.entities.plugins.media.MediaPluginException;
import org.pidome.client.entities.plugins.media.MediaPluginService;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.devices.DevicesLargeScene;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.scenes.panes.lists.ListClickedHandler;
import org.pidome.client.scenes.panes.tools.Destroyable;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class MediaSmallScene implements ScenePaneImpl,ListClickedHandler<Integer>,ListBackHandler {

    private StackPane baseContainer = new StackPane();
    
    ScrollPane mediaListPane = new ScrollPane();
    
    VBox mediaLocationsList = new VBox();
    BorderPane mediaInterface = new BorderPane();  
    
    MediaItemVisualizer visualizer;
    
    ReadOnlyObservableArrayListBean<MediaPlugin> mediaList;
    
    private ObservableArrayListBeanChangeListener<MediaPlugin> mediaMutator = this::mediaListMutator;
    
    private PCCSystem system;
    
    ServiceConnector serviceConnector;
    
    private MediaPlugin mediaPlugin;
    
    CurrentPlaylist playlist = new CurrentPlaylist();
    
    private double playerButtonPrefSize = 50;
    private double playerButtonIconSize = 2;
    
    public MediaSmallScene() {
        mediaListPane.getStyleClass().add("list-view-root");
        setupMediaPanes();
        mediaListPane.setHmax(0.1);
        playlist.noHBar();
        playlist.noHover();
        playlist.smallDisplay();
    }

    private void setupMediaPanes(){
        
        mediaLocationsList.getStyleClass().add("custom-list-view");
        mediaLocationsList.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        mediaLocationsList.setMinWidth(Control.USE_PREF_SIZE);
        mediaLocationsList.setMaxWidth(Control.USE_PREF_SIZE);
        
        mediaInterface.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        mediaInterface.setMinWidth(Control.USE_PREF_SIZE);
        mediaInterface.setMaxWidth(Control.USE_PREF_SIZE);
        mediaInterface.getStyleClass().add("media-interface-pane");
        
        baseContainer.heightProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mediaInterface.setMinHeight(newValue.doubleValue());
            mediaInterface.setMaxHeight(newValue.doubleValue());

            mediaListPane.setMinHeight(newValue.doubleValue());
            mediaListPane.setMaxHeight(newValue.doubleValue());
            
            playlist.setMinHeight(newValue.doubleValue());
            playlist.setMaxHeight(newValue.doubleValue());
            
        });
        
        baseContainer.widthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mediaInterface.setMinWidth(newValue.doubleValue());
            mediaInterface.setMaxWidth(newValue.doubleValue());

            mediaListPane.setMinWidth(newValue.doubleValue());
            mediaListPane.setMaxWidth(newValue.doubleValue());
            
            playlist.setMinWidth(newValue.doubleValue());
            playlist.setMaxWidth(newValue.doubleValue());
            
        });
        
        mediaListPane.setContent(mediaLocationsList);
        
        baseContainer.getChildren().add(mediaListPane);
        
    }
    
    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
        this.serviceConnector = connector;
    }
    
    private void setDefaultSceneTitle(){
        ScenesHandler.setSceneTitle("Media");
    }
    
    @Override
    public String getTitle() {
        return "Media";
    }
    
    @Override
    public void start() {
        try {
            mediaList = this.system.getClient().getEntities().getMediaService().getMediaList();
            mediaList.addListener(mediaMutator);
            this.system.getClient().getEntities().getMediaService().reload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(MediaSmallScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public final Pane getPane(){
        return baseContainer;
    }
    
    private void mediaListMutator(ObservableArrayListBeanChangeListener.Change<? extends MediaPlugin> change) {
        if(change.wasAdded()){
            final List<MediaLocationListItem> visualPluginsList = new ArrayList<>();
            if(change.hasNext()){
                for(MediaPlugin plugin:change.getAddedSubList()){
                    visualPluginsList.add(new MediaLocationListItem(plugin, this));
                }
            }
            Platform.runLater(() -> { 
                Collections.sort(visualPluginsList, (MediaLocationListItem arg0, MediaLocationListItem arg1) -> arg0.getPlugin().getTemporaryLocationName().compareToIgnoreCase(arg1.getPlugin().getTemporaryLocationName()));
                mediaLocationsList.getChildren().setAll(visualPluginsList);
            });
        } else if (change.wasRemoved()){
            List<MediaLocationListItem> toRemove = new ArrayList<>();
            if(change.hasNext()){
                for(MediaPlugin plugin:change.getRemoved()){
                    for(Node node:mediaLocationsList.getChildren()){
                        MediaLocationListItem checkPlugin = (MediaLocationListItem)node;
                        if(checkPlugin.getPluginId()==plugin.getPluginId()){
                            checkPlugin.destroy();
                            toRemove.add(checkPlugin);
                        }
                    }
                }
            }
            Platform.runLater(() -> { 
                mediaLocationsList.getChildren().removeAll(toRemove);
            });
        }
    }
    
    private void moveToLocations() {
        Platform.runLater(() -> {
            baseContainer.getChildren().remove(mediaInterface);
            visualizer.destroy();
            visualizer = null;
            if(!baseContainer.getChildren().contains(mediaListPane)){
                baseContainer.getChildren().add(mediaListPane);
            }
        });
    }

    private void moveToMediaItem() {
        playlist.destroy();
        Platform.runLater(() -> {
            baseContainer.getChildren().removeAll(mediaListPane, playlist);
            if(!baseContainer.getChildren().contains(mediaInterface)){
                baseContainer.getChildren().add(mediaInterface);
            }
        });
    }

    private void moveToPlaylist(MediaPlugin plugin) {
        playlist.build(plugin);
        Platform.runLater(() -> {
            baseContainer.getChildren().remove(mediaInterface);
            if(!baseContainer.getChildren().contains(playlist)){
                baseContainer.getChildren().add(playlist);
            }
            playlist.setCurrent(mediaPlugin.getCurrentPlaying().getValue());
        });
    }
    
    @Override
    public void close() {
        mediaInterface.prefWidthProperty().unbind();
        mediaLocationsList.prefWidthProperty().unbind();
        mediaList.removeListener(mediaMutator);
    }

    @Override
    public void itemClicked(Integer mediaId, String description) {
        ScenesHandler.setSceneBackTitle(this, "locations", description);
        createMediaControlPane(mediaId);
        moveToMediaItem();
    }

    private void createMediaControlPane(int mediaId) {
        try {
            mediaPlugin = this.system.getClient().getEntities().getMediaService().getMediaPlugin(mediaId);
            if(visualizer==null){
                visualizer = new MediaItemVisualizer(mediaPlugin);
            }
            if(this.serviceConnector.userDisplayType()==DisplayType.TINY){
                double width = Screen.getPrimary().getBounds().getWidth();
                playerButtonPrefSize = ((this.serviceConnector.getMaxWorkWidth()/width)*playerButtonPrefSize);
                playerButtonIconSize = ((this.serviceConnector.getMaxWorkWidth()/width)*playerButtonIconSize);
                
                mediaInterface.setTop(visualizer);
                mediaInterface.setCenter(new MediaPlayerInterface(mediaPlugin));
                mediaInterface.setBottom(new MediaVolumeInterfaceTinyHorizontal(mediaPlugin));
                mediaInterface.setBackground(setBackgroundImage(mediaPlugin.getCurrentPlaying().getValue()));
            } else {
                mediaInterface.setTop(visualizer);
                mediaInterface.setCenter(new MediaNavInterface(mediaPlugin));
                mediaInterface.setRight(new MediaVolumeInterface(mediaPlugin));
                mediaInterface.setLeft(new MediaExtrasInterface(mediaPlugin));
                mediaInterface.setBottom(new MediaPlayerInterface(mediaPlugin));
                mediaInterface.setBackground(setBackgroundImage(mediaPlugin.getCurrentPlaying().getValue()));
            }
        } catch (EntityNotAvailableException ex) {
            mediaInterface.setCenter(new Label("Could not open selected media item"));
        } catch (MediaPluginException ex) {
            mediaInterface.setCenter(new Label("Selected media item not found"));
        }
    }
    
    private Background setBackgroundImage(MediaData mediaData){
        try {
            Image image = new Image(mediaData.getThumbnail().toString());
            BackgroundSize backgroundSize = new BackgroundSize(-1, -1, false, false, true, false);
            BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);
            return new Background(backgroundImage);
        } catch (Exception ex) {
            /// No background
            return Background.EMPTY;
        }
    }
    
    @Override
    public void handleListBack(String id) {
        switch(id){
            case "locations":
                setDefaultSceneTitle();
                moveToLocations();
            break;
            case "mediaitem":
                ScenesHandler.setSceneBackTitle(this, "locations", mediaPlugin.getName().getValue());
                moveToMediaItem();
            break;
        }
    }

    @Override
    public void removeSystem() {
        this.system = null;
    }

    private static class MediaLocationListItem extends HBox implements Destroyable {
        
        private MediaPlugin plugin;
        private Label subName = new Label();
        
        private PropertyChangeListener changed = this::titleChanged;
        
        MediaLocationListItem(MediaPlugin plugin, ListClickedHandler<Integer> handler){
            this.plugin = plugin;
            getStyleClass().addAll("list-item", "list-item-pressable", "undecorated-list-item");
            
            StackPane nameHolder = new StackPane();
            VBox contents = new VBox();
            subName.getStyleClass().add("sub-title");
            contents.getChildren().addAll(new Label(this.plugin.getName().getValueSafe()), subName);
            nameHolder.getChildren().add(contents);
            HBox.setHgrow(nameHolder, Priority.ALWAYS);
            Text pointer = GlyphsDude.createIcon(FontAwesomeIcon.ANGLE_RIGHT, "1.4em;");
            getChildren().addAll(nameHolder, pointer);
            
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                handler.itemClicked(plugin.getPluginId(), plugin.getName().getValueSafe());
            });

            plugin.getCurrentPlaying().addPropertyChangeListener(changed);
            createCurrentPlaying(plugin.getCurrentPlaying().getValue());
        }
     
        public MediaPlugin getPlugin(){
            return this.plugin;
        }
        
        public void titleChanged(PropertyChangeEvent evt) {
            MediaData data = (MediaData)evt.getNewValue();
            createCurrentPlaying(data);
        }
        
        private void createCurrentPlaying(MediaData data){
            Platform.runLater(() -> { 
                switch(data.getMediaType()){
                    case NONE:
                        subName.setText(this.plugin.getTemporaryLocationName() + ", " + this.plugin.getPluginName());
                    break;
                    case AUDIO:
                        subName.setText(data.getTitle() + " - " + data.getTitleArtist());
                    break;
                    case VIDEO:
                        subName.setText(data.getTitle());
                    break;
                }
            });
        }
        
        public final int getPluginId(){
            return plugin.getPluginId();
        }

        @Override
        public void destroy() {
            plugin.getCurrentPlaying().removePropertyChangeListener(changed);
            plugin = null;
        }
    }
    
    private class MediaItemVisualizer extends HBox implements Destroyable {
        
        private StackPane coverHolder = new StackPane();
        private VBox mediaHolder = new VBox();
        
        private Label titleData = new Label();
        private Label titleArtistData = new Label();
        private Label albumData = new Label();
        private Label durationData = new Label();
        
        private final PropertyChangeListener mediaChanged = this::mediaChanged;
        private final ReadOnlyObjectPropertyBindingBean<MediaData> mediaItem;
        
        private MediaItemVisualizer(MediaPlugin plugin){
            this.mediaItem = plugin.getCurrentPlaying();
            getStyleClass().add("media-item-visualizer");
            titleData.getStyleClass().add("media-item-title");
            titleArtistData.getStyleClass().add("media-item-title-artist");
            albumData.getStyleClass().add("media-item-album");
            durationData.getStyleClass().add("media-item-duration");
            mediaHolder.getChildren().addAll(titleData, titleArtistData, durationData, albumData);
            ///coverHolder
            getChildren().addAll(mediaHolder);
            //composeCoverHolder(mediaItem.getValue());
            composeTitleData(mediaItem.getValue());
            mediaInterface.setBackground(setBackgroundImage(mediaItem.getValue()));
            mediaItem.addPropertyChangeListener(mediaChanged);
            
            mediaHolder.setOnMouseClicked((EventHandler)(Event event) -> {
                ScenesHandler.setSceneBackTitle(MediaSmallScene.this, "mediaitem", "Playlist");
                moveToPlaylist(plugin);
            });
            
        }

        private void mediaChanged(PropertyChangeEvent evt) {
            MediaData data = (MediaData)evt.getNewValue();
            //composeCoverHolder(data);
            composeTitleData(data);
            mediaInterface.setBackground(setBackgroundImage(data));
        }
        
        private void composeTitleData(MediaData item){
            switch(item.getMediaType()){
                case NONE:
                    Platform.runLater(() -> {
                        titleData.setText("No item playing");
                        titleArtistData.setText("");
                        albumData.setText("");
                        durationData.setText("");
                    });
                break;
                case AUDIO:
                    Platform.runLater(() -> {
                        titleData.setText(item.getTitle());
                        titleArtistData.setText(item.getTitleArtist());
                        albumData.setText(item.getAlbum() + " - " + item.getAlbumArtist());
                        durationData.setText(getDurationString(item.getDuration()));
                    });
                break;
                case VIDEO:
                    Platform.runLater(() -> {
                        titleData.setText(item.getTitle());
                        durationData.setText(getDurationString(item.getDuration()));
                    });
                break;
            }
        }
        
        private String getDurationString(int totalSeconds){
            int hours   = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        
        /*
        private void composeCoverHolder(MediaData item){
            Platform.runLater(() -> { coverHolder.getChildren().clear(); });
            switch(item.getMediaType()){
                case NONE:
                    
                break;
                case AUDIO:
                    ImageView audioBackground = new ImageView("/images/app/media/cdcover.png");
                    audioBackground.getStyleClass().add("cd-cover");
                    audioBackground.setFitWidth(90);
                    audioBackground.setPreserveRatio(true);
                    audioBackground.setCache(true);
                    
                    
                    
                    ImageView audioForeground = new ImageView("/images/app/media/cdcoverfront.png");
                    audioForeground.getStyleClass().add("cd-cover");
                    audioForeground.setFitWidth(90);
                    audioForeground.setPreserveRatio(true);
                    audioForeground.setCache(true);
                    Platform.runLater(() -> { coverHolder.getChildren().addAll(audioBackground, audioForeground); });
                break;
                case VIDEO:
                    ImageView videoForeground = new ImageView("/images/app/media/dvdcover.png");
                    videoForeground.getStyleClass().add("dvd-cover");
                    videoForeground.setFitHeight(120);
                    videoForeground.setPreserveRatio(true);
                    videoForeground.setCache(true);
                    
                    ImageView videoImage;
                    
                    try {
                        videoImage = new ImageView(item.getThumbnail().toString());
                        videoImage.setFitHeight(108);
                        videoImage.setTranslateX(4);
                        videoImage.setTranslateY(-1);
                        videoImage.setPreserveRatio(true);
                        videoImage.setCache(true);
                    } catch (Exception ex){
                        videoImage = new ImageView();
                    }
                    final ImageView toView = videoImage;
                    Platform.runLater(() -> { coverHolder.getChildren().addAll(toView, videoForeground); });
                break;
            }
        }
        */
        
        @Override
        public void destroy() {
            mediaItem.removePropertyChangeListener(mediaChanged);
        }
    }
        
    private Runnable createServerCommand(MediaPlugin mediaItem, MediaPluginService.ServerCommand command){
        Runnable run = () -> {
            mediaItem.handleServerCommand(command);
        };
        return run;
    }
    
    private Runnable createPlayerCommand(MediaPlugin mediaItem, MediaPluginService.PlayerCommand command){
        Runnable run = () -> {
            mediaItem.handlePlayerCommand(command);
        };
        return run;
    }
    
    private Button commandHandlerButton(Text buttonString, Runnable command){
        Button playerButton = new Button();
        playerButton.setFocusTraversable(false);
        playerButton.setOnAction((EventHandler)(Event event) -> {
            event.consume();
            new Thread() { @Override public void run() {command.run(); } }.start();
        });
        playerButton.getStyleClass().add("media-nav-button");
        playerButton.setPrefSize(playerButtonPrefSize, playerButtonPrefSize);
        playerButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        playerButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        playerButton.setGraphic(buttonString);
        return playerButton;
    }
    
    private class MediaPlayerInterface extends HBox {
        
        private MediaPlayerInterface(MediaPlugin mediaItem){
            
            this.setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-player-pane");
            
            String size = playerButtonIconSize + "em;";
            
            Button prevButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.FAST_BACKWARD, size),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PREV));
            Button stopButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.STOP,size),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.STOP));
            Button pauseButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.PAUSE,size),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PAUSE));
            Button playButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.PLAY, size),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PLAY));
            Button nextButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.FAST_FORWARD, size),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.NEXT));
            
            getChildren().addAll(prevButton, stopButton, playButton, pauseButton, nextButton);
            
        }

    }
    
    private class MediaExtrasInterface extends VBox {
        
        private MediaExtrasInterface(MediaPlugin mediaItem){
            
            setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-extras-pane");
            
            Button volupButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.UNDO, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.BACK));
            Button volMuteButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.NEWSPAPER_ALT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.OSD));
            Button volDownButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.HOME, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.HOME));
            
            getChildren().addAll(volupButton, volMuteButton, volDownButton);
            
        }

    }
    
    private class MediaVolumeInterface extends VBox {
        
        private MediaVolumeInterface(MediaPlugin mediaItem){
            
            setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-volume-pane");
            
            Button volupButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLUP));
            Button volMuteButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_OFF, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.MUTE));
            Button volDownButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_DOWN, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLDOWN));
            
            getChildren().addAll(volupButton, volMuteButton, volDownButton);
            
        }

    }
    
    private class MediaVolumeInterfaceTinyHorizontal extends HBox {
        
        private MediaVolumeInterfaceTinyHorizontal(MediaPlugin mediaItem){
            
            setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-volume-pane");
            
            String size = playerButtonIconSize + "em;";
            
            Button volupButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP, size),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLUP));
            Button volMuteButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_OFF, size),createServerCommand(mediaItem, MediaPluginService.ServerCommand.MUTE));
            Button volDownButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_DOWN, size),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLDOWN));
            
            getChildren().addAll(volDownButton, volMuteButton, volupButton);
            
        }

    }
    
    private class MediaNavInterface extends GridPane {

        private MediaNavInterface(MediaPlugin mediaItem){
            
            getStyleClass().add("media-navigation-pane");
            
            this.setAlignment(Pos.CENTER);
            
            Button upButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_UP, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.UP));
            Button leftButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_LEFT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.LEFT));
            Button centerButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.CHECK_CIRCLE, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.CONFIRM));
            Button rightButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_RIGHT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.RIGHT));
            Button downButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_DOWN, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.DOWN));
            
            add(upButton, 1,0);
            add(leftButton, 0,1);
            add(centerButton, 1,1);
            add(rightButton, 2,1);
            add(downButton, 1,2);
            
        }

    }
    
}