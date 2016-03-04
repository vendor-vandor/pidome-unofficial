/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.media;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.pidome.client.entities.plugins.media.MediaData;
import org.pidome.client.entities.plugins.media.MediaPlaylistItem;
import org.pidome.client.entities.plugins.media.MediaPlugin;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;

/**
 *
 * @author John
 */
class CurrentPlaylist extends VBox {

    private ObservableArrayListBeanChangeListener<MediaPlaylistItem> mediaMutator = this::playlistMutator;
    private MediaPlugin mediaPluginItem;
    private ListView currentPlaying = new ListView();

    private Text totalDuration = new Text("00:00:00");
    
    private boolean smallDisplay = false;
    
    protected CurrentPlaylist(){
        getStyleClass().add("list-view-root");
        HBox actionContainer = new HBox();
        actionContainer.setPadding(new Insets(5,0,5,5));
        
        Label headerTitleText = new Label("Playlist ");
        headerTitleText.getStyleClass().add("text");
        headerTitleText.setMaxWidth(Double.MAX_VALUE);
        
        Text durationText = new Text("Total duration: ");
        durationText.getStyleClass().add("text");
        totalDuration.getStyleClass().add("text");
        
        headerTitleText.setTextAlignment(TextAlignment.LEFT);
        durationText.setTextAlignment(TextAlignment.RIGHT);
        totalDuration.setTextAlignment(TextAlignment.RIGHT);
        
        HBox.setHgrow(headerTitleText, Priority.ALWAYS);
        HBox.setHgrow(durationText, Priority.NEVER);
        HBox.setHgrow(totalDuration, Priority.NEVER);
        
        actionContainer.getChildren().addAll(headerTitleText,durationText,totalDuration);
        getChildren().addAll(actionContainer, currentPlaying);
        actionContainer.getStyleClass().add("custom-list-header");
        currentPlaying.getStyleClass().add("custom-list-view");
        currentPlaying.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    protected final void noHBar(){
        currentPlaying.getStyleClass().add("nohbar");
        currentPlaying.addEventFilter(ScrollEvent.SCROLL, (ScrollEvent event) -> {
            if (event.getDeltaX() != 0) {
                event.consume();
            }
        });
    }
    
    protected final void noHover(){
        currentPlaying.getStyleClass().add("nohover");
    }
    
    protected void smallDisplay(){
        this.smallDisplay = true;
    }
    
    protected final ListView getList(){
        return this.currentPlaying;
    }
    
    private void updateTotals(){
        int total = 0;
        for (MediaPlaylistItem item: (List<MediaPlaylistItem>)currentPlaying.getItems()){
            total += item.getDuration();
        }
        final int finalTotal = total;
        totalDuration.setText(getDurationString(finalTotal));
    }
    
    protected void build(final MediaPlugin mediaItem) {
        this.mediaPluginItem = mediaItem;
        currentPlaying.setCellFactory(new Callback<ListView<String>, ListCell<MediaPlaylistItem>>() {
                @Override
                public ListCell<MediaPlaylistItem> call(ListView<String> list) {
                    return new MediaItemCell(mediaItem, smallDisplay);
                }
            }
        );
        final List<MediaPlaylistItem> visualPluginsList = new ArrayList<>();
        if (this.mediaPluginItem.getCurrentPlaylist().size() > 0) {
            for (MediaPlaylistItem item : this.mediaPluginItem.getCurrentPlaylist().subList(0, this.mediaPluginItem.getCurrentPlaylist().size())) {
                visualPluginsList.add(item);
            }
            Platform.runLater(() -> {
                currentPlaying.getItems().addAll(visualPluginsList);
                updateTotals();
            });
        }
        this.mediaPluginItem.getCurrentPlaylist().addListener(mediaMutator);
    }

    protected final void setCurrent(MediaData newItem){
        for (MediaPlaylistItem item: (List<MediaPlaylistItem>)currentPlaying.getItems()){
            if(item.getTitle().startsWith(newItem.getTitleArtist()) && item.getTitle().endsWith(newItem.getTitle())){
                Platform.runLater(() -> { 
                    currentPlaying.getSelectionModel().select(item); 
                });
                break;
            }
        }
    }
    
    private void playlistMutator(ObservableArrayListBeanChangeListener.Change<? extends MediaPlaylistItem> change) {
        if (change.wasAdded()) {
            final List<MediaPlaylistItem> visualItemsList = new ArrayList<>();
            if (change.hasNext()) {
                for (MediaPlaylistItem listItem : change.getAddedSubList()) {
                    visualItemsList.add(listItem);
                }
            }
            Platform.runLater(() -> {
                currentPlaying.getItems().addAll(visualItemsList);
                updateTotals();
            });
        } else if (change.wasRemoved()) {
            Platform.runLater(() -> {
                List<MediaPlaylistItem> visualItemsRemoveList = new ArrayList<>();
                for (MediaPlaylistItem item : change.getRemoved()) {
                    visualItemsRemoveList.add(item);
                }
                Platform.runLater(() -> {
                    currentPlaying.getItems().removeAll(visualItemsRemoveList);
                    updateTotals();
                });
            });
        }
        updateTotals();
    }

    protected void destroy() {
        if (this.mediaPluginItem != null) {
            this.mediaPluginItem.getCurrentPlaylist().removeListener(mediaMutator);
            Platform.runLater(() -> {
                currentPlaying.getItems().clear();
            });
        }
    }

    static class MediaItemCell extends ListCell<MediaPlaylistItem> {

        StackPane itemContainer;
        Text title;
        Text duration;
        
        boolean smallDisplay = false;
        
        private MediaItemCell(MediaPlugin mediaItem, boolean smallDisplay){
            super();
            this.smallDisplay = smallDisplay;
            if(!smallDisplay){
                itemContainer = new StackPane();
                title = new Text();
                duration = new Text();
                
                StackPane.setAlignment(title, Pos.CENTER_LEFT);
                StackPane.setAlignment(duration, Pos.CENTER_RIGHT);
                duration.setTextAlignment(TextAlignment.RIGHT);
                title.getStyleClass().add("text");
                duration.getStyleClass().add("text");
                
                itemContainer.getChildren().addAll(title, duration);
                setGraphic(itemContainer);
                itemContainer.setCacheHint(CacheHint.SPEED);
                itemContainer.setCache(true);
            }
            setOnMouseClicked((EventHandler)(Event event) -> {
                event.consume();
                 mediaItem.playPlaylistItem(this.getItem());
            });
            
        }
        
        @Override
        public void updateItem(MediaPlaylistItem item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                if(smallDisplay){
                    setText(new StringBuilder().append(item.getPlaylistPosition() + 1).append(": ").append(item.getTitle()).toString());
                } else {
                    duration.setText(getDurationString(item.getDuration()));
                    title.setText(new StringBuilder().append(item.getPlaylistPosition() + 1).append(": ").append(item.getTitle()).toString());
                }
            } else {
                setText("");
            }
        }
    }
    
    private static String getDurationString(int totalSeconds){
        int hours   = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
}