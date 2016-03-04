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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import static javafx.scene.layout.Region.USE_PREF_SIZE;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.plugins.media.MediaData;
import org.pidome.client.entities.plugins.media.MediaPlugin;
import org.pidome.client.entities.plugins.media.MediaPluginException;
import org.pidome.client.entities.plugins.media.MediaPluginService;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.scenes.panes.lists.ListClickedHandler;
import org.pidome.client.scenes.panes.tools.Destroyable;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyObjectPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public class MediaLargeScene implements ScenePaneImpl,ListClickedHandler<Integer>,ListBackHandler {

    private GridPane baseContainer = new GridPane();
    private VBox mediaLocationsList = new VBox();
    
    //ListView mediaPlaylistsList = new ListView();
    CurrentPlaylist currentPlaylist = new CurrentPlaylist();
    
    ScrollPane mediaListPane = new ScrollPane();
    
    MediaItemVisualizer visualizer = new MediaItemVisualizer();
    
    ReadOnlyObservableArrayListBean<MediaPlugin> mediaList;
    
    private ObservableArrayListBeanChangeListener<MediaPlugin> mediaMutator = this::mediaListMutator;
    
    private PCCSystem system;
    
    MediaPlayerInterface playerInterface = new MediaPlayerInterface();
    
    private boolean hasFirstItem = false;
    
    private static double navButtonSize = DisplayTools.getHeightScaleRatio(50);
    private static double navButtonTextSize = DisplayTools.getHeightScaleRatio(15);
    
    public MediaLargeScene(){
        mediaListPane.getStyleClass().add("list-view-root");
        mediaListPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mediaListPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setupMediaPanes();
    }
    
    private void setupMediaPanes(){
        
        mediaListPane.setContent(mediaLocationsList);
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(33.33);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(33.33);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(33.33);
        
        ///baseContainer.setGridLinesVisible(true);
        
        baseContainer.getColumnConstraints().addAll(column1, column2, column3);
        
        visualizer.getStyleClass().add("full-component-large");
        //playerInterface.getStyleClass().add("full-component-large");
        //currentPlaylist.getStyleClass().add("full-component-large");
        
        baseContainer.add(mediaListPane,0,0,1,3);
        baseContainer.add(visualizer,1,0,2,1);

        baseContainer.add(playerInterface,1,1,2,1);
        
        ///baseContainer.add(mediaPlaylistsList,1,2,1,1);
        baseContainer.add(currentPlaylist,1,2,2,1);
        
        mediaLocationsList.getStyleClass().addAll("custom-list-view", "large", "first");
        
        baseContainer.minWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        
        //mediaPlaylistsList.getStyleClass().add("custom-list-view");
        //currentPlaylist.getStyleClass().add("custom-list-view");
        
        baseContainer.heightProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mediaLocationsList.setMinHeight(newValue.doubleValue());
            
            mediaListPane.setMinHeight(newValue.doubleValue());
            mediaListPane.setMaxHeight(newValue.doubleValue());
            
            visualizer.setMinHeight(newValue.doubleValue()*0.4);
            visualizer.setMaxHeight(newValue.doubleValue()*0.4);
            
            playerInterface.setMinHeight(newValue.doubleValue()*0.2);
            playerInterface.setMaxHeight(newValue.doubleValue()*0.2);
            
        });
        
        baseContainer.widthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mediaLocationsList.setMinWidth(newValue.doubleValue()*0.3333);
            currentPlaylist.setMinWidth(newValue.doubleValue()*0.6666);
        });
        
        
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
            Logger.getLogger(MediaLargeScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() {
        baseContainer.minWidthProperty().unbind();
        mediaList.removeListener(mediaMutator);
        playerInterface.destroy();
        visualizer.destroy();
        currentPlaylist.destroy();
    }

    @Override
    public Pane getPane() {
        return baseContainer;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    @Override
    public void removeSystem() {
        this.system = null;
    }

    @Override
    public void itemClicked(Integer mediaId, String itemDescription) {
        ScenesHandler.setSceneTitle("Media: " + itemDescription);
        
        MediaPlugin mediaItem;
        try {
            mediaItem = this.system.getClient().getEntities().getMediaService().getMediaPlugin(mediaId);
            
            /// Rebuild items.
            playerInterface.destroy();
            visualizer.destroy();
            currentPlaylist.destroy();
            playerInterface.build(mediaItem);
            visualizer.build(mediaItem.getCurrentPlaying());
            currentPlaylist.build(mediaItem);
            
        } catch (MediaPluginException | EntityNotAvailableException ex) {
            Logger.getLogger(MediaLargeScene.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void handleListBack(String id) {
        /// Not used yet
    }
    
    private void mediaListMutator(ObservableArrayListBeanChangeListener.Change<? extends MediaPlugin> change) {
        if(change.wasAdded()){
            final List<MediaLocationListItem> visualPluginsList = new ArrayList<>();
            if(change.hasNext()){
                for(MediaPlugin plugin:change.getAddedSubList()){
                    MediaLocationListItem item = new MediaLocationListItem(plugin, this);
                    if(!hasFirstItem){
                        item.getStyleClass().add("active");
                        hasFirstItem = true;
                        itemClicked(plugin.getPluginId(), plugin.getName().getValueSafe());
                    }
                    visualPluginsList.add(item);
                }
            }
            Collections.sort(visualPluginsList, (MediaLocationListItem arg0, MediaLocationListItem arg1) -> arg0.getPlugin().getTemporaryLocationName().compareToIgnoreCase(arg1.getPlugin().getTemporaryLocationName()));
            Platform.runLater(() -> { 
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
    
    private static String getDurationString(int totalSeconds){
        int hours   = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    private class MediaLocationListItem extends HBox implements Destroyable {
        
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
                for(Node node:mediaLocationsList.getChildren()){
                    node.getStyleClass().add("active");
                }
                this.getStyleClass().add("active");
                handler.itemClicked(plugin.getPluginId(), plugin.getName().getValueSafe());
            });

            plugin.getCurrentPlaying().addPropertyChangeListener(changed);
            createCurrentPlaying(plugin.getCurrentPlaying().getValue());
        }
     
        public final MediaPlugin getPlugin(){
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
    
    private class MediaItemVisualizer extends StackPane implements Destroyable {
        
        ///private final StackPane coverHolder = new StackPane();
        
        private final VBox infoHolder = new VBox();
        
        private final Label titleData = new Label();
        private final Label titleArtistData = new Label();
        private final Label albumData = new Label();
        private final Label durationData = new Label();
        
        private final PropertyChangeListener mediaChanged = this::mediaChanged;
        private ReadOnlyObjectPropertyBindingBean<MediaData> mediaData;
        
        private MediaItemVisualizer(){
            
            infoHolder.setAlignment(Pos.BOTTOM_LEFT);
            infoHolder.getStyleClass().add("media-info-container");
            
            getStyleClass().addAll("media-item-visualizer", "large");
            
            VBox.setVgrow(titleData, Priority.NEVER);
            VBox.setVgrow(titleArtistData, Priority.NEVER);
            VBox.setVgrow(albumData, Priority.NEVER);
            VBox.setVgrow(durationData, Priority.NEVER);
            
            StackPane.setAlignment(infoHolder, Pos.BOTTOM_LEFT);
            
            infoHolder.setMaxHeight(Double.MIN_VALUE);
            
            titleData.getStyleClass().add("media-item-title");
            titleArtistData.getStyleClass().add("media-item-title-artist");
            albumData.getStyleClass().add("media-item-album");
            durationData.getStyleClass().add("media-item-duration");
            
            titleData.setText("No item playing");
            titleArtistData.setText("Artist ext filled");
            albumData.setText("Album text");
            durationData.setText("00:00:00");
            
            this.setMinHeight(USE_PREF_SIZE);
            this.setMaxHeight(USE_PREF_SIZE);

            this.getChildren().add(infoHolder);
            
        }

        private Background setBackgroundImage(MediaData mediaData){
            if(mediaData == null) return Background.EMPTY;
            try {
                Image image;
                BackgroundPosition position = BackgroundPosition.CENTER;
                BackgroundSize backgroundSize;
                if(mediaData.getPoster()!=null && !mediaData.getPoster().toString().equals("")){
                    image = new Image(mediaData.getPoster().toString());
                    backgroundSize = new BackgroundSize(-1, -1, false, false, true, true);
                } else if(mediaData.getThumbnail()!=null && !mediaData.getThumbnail().toString().equals("")){
                    image = new Image(mediaData.getThumbnail().toString());
                    position = BackgroundPosition.DEFAULT;
                    backgroundSize = new BackgroundSize(-1, -1, false, false, true, false);
                } else {
                    backgroundSize = new BackgroundSize(-1, -1, false, false, true, true);
                    switch(mediaData.getMediaType()){
                        case AUDIO:
                            image = new Image("/org/pidome/client/appimages/media/audio-poster-fallback.png");
                        break;
                        case VIDEO:
                            image = new Image("/org/pidome/client/appimages/media/video-poster-fallback.png");
                        break;
                        default:
                            return Background.EMPTY;
                    }
                }
                BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, position, backgroundSize);
                return new Background(backgroundImage);
            } catch (Exception ex) {
                return Background.EMPTY;
            }
        }
            
        private void mediaChanged(PropertyChangeEvent evt) {
            MediaData data = (MediaData)evt.getNewValue();
            //composeCoverHolder(data);
            composeTitleData(data);
            setBackground(setBackgroundImage(data));
        }
        
        private void build(ReadOnlyObjectPropertyBindingBean<MediaData> mediaData){
            this.mediaData = mediaData;
            ///coverHolder
            //composeCoverHolder(mediaItem.getValue());
            composeTitleData(mediaData.getValue());
            mediaData.addPropertyChangeListener(mediaChanged);
            setBackground(setBackgroundImage(mediaData.getValue()));
        }
        
        private void composeTitleData(MediaData item){
            currentPlaylist.setCurrent(item);
            switch(item.getMediaType()){
                case NONE:
                    Platform.runLater(() -> {
                        infoHolder.getChildren().clear();
                        titleData.setText("No audio/video playing");
                        titleArtistData.setText("");
                        albumData.setText("");
                        durationData.setText("");
                        infoHolder.getChildren().addAll(titleData);
                    });
                break;
                case AUDIO:
                    Platform.runLater(() -> {
                        infoHolder.getChildren().clear();
                        titleData.setText(item.getTitle());
                        titleArtistData.setText(item.getTitleArtist());
                        albumData.setText(item.getAlbum() + " - " + item.getAlbumArtist());
                        durationData.setText(getDurationString(item.getDuration()));
                        infoHolder.getChildren().addAll(titleData, titleArtistData, durationData, albumData);
                    });
                break;
                case VIDEO:
                    Platform.runLater(() -> {
                        infoHolder.getChildren().clear();
                        titleData.setText(item.getTitle());
                        titleArtistData.setText("");
                        albumData.setText("");
                        durationData.setText(getDurationString(item.getDuration()));
                        infoHolder.getChildren().addAll(titleData, durationData);
                    });
                break;
            }
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
            if(mediaData!=null){
                mediaData.removePropertyChangeListener(mediaChanged);
            }
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
        playerButton.setStyle("-fx-font-size: "+navButtonTextSize+"px;");
        playerButton.setPrefSize(navButtonSize, navButtonSize);
        playerButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        playerButton.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        playerButton.setGraphic(buttonString);
        return playerButton;
    }
    
    private class MediaPlayerInterface extends HBox {
        
        private MediaPlayerInterface(){
            this.setAlignment(Pos.CENTER);
            getStyleClass().addAll("media-player-pane", "large");
        }
        
        private void build(MediaPlugin mediaItem){
            
            VBox parentButtons = new VBox(10);
            parentButtons.setAlignment(Pos.CENTER_LEFT);
            Button undoButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.UNDO, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.BACK));
            Button osdButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.NEWSPAPER_ALT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.OSD));
            Button homeButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.HOME, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.HOME));
            parentButtons.getChildren().addAll(undoButton, osdButton, homeButton);
            HBox.setHgrow(parentButtons, Priority.ALWAYS);
            
            GridPane mediaNavInterface = new GridPane();
            mediaNavInterface.setVgap(10);
            mediaNavInterface.setHgap(10);
            mediaNavInterface.setAlignment(Pos.CENTER_LEFT);
            Button upButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_UP, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.UP));
            Button leftButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_LEFT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.LEFT));
            Button centerButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.CHECK_CIRCLE, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.CONFIRM));
            Button rightButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_RIGHT, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.RIGHT));
            Button downButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.ARROW_CIRCLE_DOWN, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.DOWN));
            mediaNavInterface.add(upButton, 1,0);
            mediaNavInterface.add(leftButton, 0,1);
            mediaNavInterface.add(centerButton, 1,1);
            mediaNavInterface.add(rightButton, 2,1);
            mediaNavInterface.add(downButton, 1,2);
            HBox.setHgrow(mediaNavInterface, Priority.ALWAYS);
            
            
            HBox playerButtons = new HBox(10);
            playerButtons.setAlignment(Pos.BOTTOM_CENTER);
            Button prevButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.FAST_BACKWARD, "2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PREV));
            Button stopButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.STOP,"2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.STOP));
            Button pauseButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.PAUSE,"2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PAUSE));
            Button playButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.PLAY, "2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.PLAY));
            Button nextButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.FAST_FORWARD, "2em;"),createPlayerCommand(mediaItem, MediaPluginService.PlayerCommand.NEXT));
            playerButtons.getChildren().addAll(prevButton, stopButton, playButton, pauseButton, nextButton);
            HBox.setHgrow(playerButtons, Priority.ALWAYS);
            
            
            VBox mediaVolumeInterface = new VBox(10);
            mediaVolumeInterface.setAlignment(Pos.CENTER_RIGHT);
            Button volupButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_UP, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLUP));
            Button volMuteButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_OFF, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.MUTE));
            Button volDownButton = commandHandlerButton(GlyphsDude.createIcon(FontAwesomeIcon.VOLUME_DOWN, "2em;"),createServerCommand(mediaItem, MediaPluginService.ServerCommand.VOLDOWN));
            mediaVolumeInterface.getChildren().addAll(volupButton, volMuteButton, volDownButton);
            HBox.setHgrow(mediaVolumeInterface, Priority.ALWAYS);
            
            getChildren().addAll(parentButtons,mediaNavInterface,playerButtons,mediaVolumeInterface);
            
        }
        
        private void destroy(){
            getChildren().clear();
        }
        
    }
    
}