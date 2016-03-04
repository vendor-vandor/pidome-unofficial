/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.media;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.plugins.media.MediaData;
import org.pidome.client.entities.plugins.media.MediaPlugin;
import org.pidome.client.entities.plugins.media.MediaPluginException;
import org.pidome.client.entities.plugins.media.MediaPluginService;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.scenes.devices.DevicesLocationList;
import org.pidome.client.phone.scenes.visuals.SceneBackHandler;
import org.pidome.client.phone.visuals.interfaces.Destroyable;
import org.pidome.client.phone.visuals.lists.ListClickedHandler;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class MediaLocationList extends BaseScene implements ListClickedHandler<Integer>,SceneBackHandler {

    private GridPane mediaPane = new GridPane();
    VBox mediaLocationsList = new VBox();
    BorderPane mediaInterface = new BorderPane();  
    
    MediaItemVisualizer visualizer;
    
    ReadOnlyObservableArrayListBean<MediaPlugin> mediaList;
    
    private ObservableArrayListBeanChangeListener<MediaPlugin> mediaMutator = this::mediaListMutator;
    
    public MediaLocationList() {
        super(true);
        setupMediaPanes();
        getContentPane().setHmax(0.1);
    }

    private void setupMediaPanes(){
        mediaPane.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth()*2);
        mediaPane.setMinWidth(Control.USE_PREF_SIZE);
        mediaPane.setMaxWidth(Control.USE_PREF_SIZE);
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        mediaPane.getColumnConstraints().addAll(column1, column2);
        mediaPane.add(mediaLocationsList,0,0);
        mediaPane.add(mediaInterface,1,0);
        
        mediaLocationsList.getStyleClass().add("custom-list-view");
        mediaLocationsList.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        mediaLocationsList.setMinWidth(Control.USE_PREF_SIZE);
        mediaLocationsList.setMaxWidth(Control.USE_PREF_SIZE);
        
        mediaInterface.setPrefWidth(Screen.getPrimary().getVisualBounds().getWidth());
        mediaInterface.setMinWidth(Control.USE_PREF_SIZE);
        mediaInterface.setMaxWidth(Control.USE_PREF_SIZE);
        mediaInterface.getStyleClass().add("media-interface-pane");
        
        this.getBaseContentHeightProperty().addListener((ChangeListener)(ObservableValue observable, Object oldValue, Object newValue) -> {
            mediaInterface.setPrefHeight(((Number)newValue).doubleValue());
        });
        
    }
    
    private void setDefaultSceneTitle(){
        setSceneTitle("Media");
    }
    
    @Override
    public void run() {
        setDefaultSceneTitle();
        this.setContent(mediaPane);
        try {
            mediaList = getSystem().getClient().getEntities().getMediaService().getMediaList();
            mediaList.addListener(mediaMutator);
            getSystem().getClient().getEntities().getMediaService().reload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(DevicesLocationList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mediaListMutator(ObservableArrayListBeanChangeListener.Change<? extends MediaPlugin> change) {
        if(change.wasAdded()){
            if(hasSystem()){
                final List<MediaLocationListItem> visualPluginsList = new ArrayList<>();
                if(change.hasNext()){
                    for(MediaPlugin plugin:change.getAddedSubList()){
                        visualPluginsList.add(new MediaLocationListItem(plugin, this));
                    }
                }
                Platform.runLater(() -> { 
                    mediaLocationsList.getChildren().addAll(visualPluginsList);
                });
            }
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
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        final KeyValue kv = new KeyValue(this.getContentPane().hvalueProperty(), 0.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished((EventHandler)(Event event) -> {
            visualizer.destroy();
            Platform.runLater(() -> {
                mediaInterface.getChildren().clear();
                visualizer = null;
            });
        });
        timeline.play();
    }

    private void moveToMediaItem() {
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        timeline.setAutoReverse(false);
        final KeyValue kv = new KeyValue(this.getContentPane().hvalueProperty(), 1.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
        getContentPane().setVvalue(0.0);
    }
    
    @Override
    public void stop() {
        /// Not needed yet.
    }

    @Override
    public void itemClicked(Integer mediaId, String description) {
        getSceneHeader().setSceneBackTitle(this, "locations", description);
        createMediaControlPane(mediaId);
        moveToMediaItem();
    }

    private void createMediaControlPane(int mediaId) {
        try {
            MediaPlugin mediaItem = getSystem().getClient().getEntities().getMediaService().getMediaPlugin(mediaId);
            if(visualizer==null){
                visualizer = new MediaItemVisualizer(mediaItem.getCurrentPlaying());
            }
            mediaInterface.setTop(visualizer);
            mediaInterface.setCenter(new MediaNavInterface(mediaItem));
            mediaInterface.setRight(new MediaVolumeInterface(mediaItem));
            mediaInterface.setLeft(new MediaExtrasInterface(mediaItem));
            mediaInterface.setBottom(new MediaPlayerInterface(mediaItem));
            mediaInterface.setBackground(setBackgroundImage(mediaItem.getCurrentPlaying().getValue()));
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
    public void handleSceneBack(String id) {
        switch(id){
            case "locations":
                setDefaultSceneTitle();
                moveToLocations();
            break;
        }
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
            Text pointer = GlyphsDude.createIcon(FontAwesomeIcons.ANGLE_RIGHT, "1.4em;");
            getChildren().addAll(nameHolder, pointer);
            
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                handler.itemClicked(plugin.getPluginId(), plugin.getName().getValueSafe());
            });

            plugin.getCurrentPlaying().addPropertyChangeListener(changed);
            createCurrentPlaying(plugin.getCurrentPlaying().getValue());
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
        
        private MediaItemVisualizer(ReadOnlyObjectPropertyBindingBean<MediaData> mediaItem){
            this.mediaItem = mediaItem;
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
        playerButton.setOnAction((EventHandler)(Event event) -> {
            event.consume();
            new Thread() { @Override public void run() {command.run(); } }.start();
        });
        playerButton.getStyleClass().add("media-nav-button");
        playerButton.setGraphic(buttonString);
        return playerButton;
    }
    
    private class MediaPlayerInterface extends HBox {
        
        private MediaPlayerInterface(MediaPlugin mediaItem){
            
            this.setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-player-pane");
            
            Button prevButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.FAST_BACKWARD, "2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PREV));
            Button stopButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.STOP,"2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.STOP));
            Button pauseButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.PAUSE,"2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PAUSE));
            Button playButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.PLAY, "2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PLAY));
            Button nextButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.FAST_FORWARD, "2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.NEXT));
            
            getChildren().addAll(prevButton, stopButton, playButton, pauseButton, nextButton);
            
        }

    }
    
    private class MediaExtrasInterface extends VBox {
        
        private MediaExtrasInterface(MediaPlugin mediaItem){
            
            setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-extras-pane");
            
            Button volupButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.UNDO, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.BACK));
            Button volMuteButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.NEWSPAPER_ALT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.OSD));
            Button volDownButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.HOME, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.HOME));
            
            getChildren().addAll(volupButton, volMuteButton, volDownButton);
            
        }

    }
    
    private class MediaVolumeInterface extends VBox {
        
        private MediaVolumeInterface(MediaPlugin mediaItem){
            
            setAlignment(Pos.CENTER);
            
            getStyleClass().add("media-volume-pane");
            
            Button volupButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.VOLUME_UP, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLUP));
            Button volMuteButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.VOLUME_OFF, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.MUTE));
            Button volDownButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.VOLUME_DOWN, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLDOWN));
            
            getChildren().addAll(volupButton, volMuteButton, volDownButton);
            
        }

    }
    
    private class MediaNavInterface extends GridPane {

        private MediaNavInterface(MediaPlugin mediaItem){
            
            getStyleClass().add("media-navigation-pane");
            
            this.setAlignment(Pos.CENTER);
            
            Button upButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_UP, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.UP));
            Button leftButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_LEFT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.LEFT));
            Button centerButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.CHECK_CIRCLE, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.CONFIRM));
            Button rightButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_RIGHT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.RIGHT));
            Button downButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcons.ARROW_CIRCLE_DOWN, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.DOWN));
            
            add(upButton, 1,0);
            add(leftButton, 0,1);
            add(centerButton, 1,1);
            add(rightButton, 2,1);
            add(downButton, 1,2);
            
        }

    }
    
}