/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.pidomeremotes;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.remotes.PiDomeRemote;
import org.pidome.client.entities.remotes.PiDomeRemotesService;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.scenes.panes.lists.ListClickedHandler;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;

/**
 *
 * @author John
 */
public class PiDomeRemotesSmallScreen implements ScenePaneImpl,ListClickedHandler<Integer>,ListBackHandler {

    StackPane container = new StackPane();
    ScrollPane mainContent = new ScrollPane();
    
    VBox remotesList = new VBox();
    
    PCCSystem system;
    
    PiDomeRemotesService service;
    
    PiDomeRemoteDisplay currentRemote;
    
    private enum CurrentDisplay {
        LIST,REMOTE;
    }
    
    private CurrentDisplay currentDisplay = CurrentDisplay.LIST;
    
    private ObservableArrayListBeanChangeListener<PiDomeRemote> remotesMutator = this::remotesMutator;
    
    public PiDomeRemotesSmallScreen() {
        mainContent.getStyleClass().add("list-view-root");
        setupRemotesPane();
        mainContent.setHmax(0.1);
    }
    
    @Override
    public String getTitle() {
        return "Remotes";
    }

    private void setupRemotesPane(){
        remotesList.getStyleClass().add("custom-list-view");
        
        remotesList.prefWidthProperty().bind(ScenesHandler.getContentWidthProperty());
        remotesList.setMinWidth(Control.USE_PREF_SIZE);
        remotesList.setMaxWidth(Control.USE_PREF_SIZE);
        mainContent.setContent(remotesList);
        
        container.heightProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
             mainContent.setMinHeight(newValue.doubleValue());
             mainContent.setMaxHeight(newValue.doubleValue());
        });
        
        container.widthProperty().addListener((ChangeListener<Number>)(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
             mainContent.setMinWidth(newValue.doubleValue());
             mainContent.setMaxWidth(newValue.doubleValue());
        });
        
        container.getChildren().add(mainContent);
        
    }
    
    @Override
    public void start() {
        try {
            service = this.system.getClient().getEntities().getPiDomeRemotesService();
            service.getRemotes().addListener(remotesMutator);
            service.preload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(PiDomeRemotesSmallScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void remotesMutator(ObservableArrayListBeanChangeListener.Change<? extends PiDomeRemote> change) {
        if(change.wasAdded()){
            if(currentDisplay == CurrentDisplay.LIST){
                if(change.hasNext()){
                    List<PiDomeRemoteItem> toAdd = new ArrayList<>();
                    for(PiDomeRemote remote:change.getAddedSubList()){
                        boolean found = false;
                        for(Node node:remotesList.getChildren()){
                            if(node instanceof PiDomeRemoteItem && ((PiDomeRemoteItem)node).getRemoteId()==remote.getId()){
                                found = true;
                                break;
                            }
                        }
                        if(!found){
                            toAdd.add(new PiDomeRemoteItem(remote, this));
                        }
                    }
                    remotesList.getChildren().addAll(toAdd);
                }
            }
        } else if (change.wasRemoved()){
            if(currentDisplay == CurrentDisplay.LIST){
                List<PiDomeRemoteItem> toRemove = new ArrayList<>();
                if(change.hasNext()){
                    for(PiDomeRemote remote:change.getRemoved()){
                        PiDomeRemoteItem found = null;
                        for(Node node:remotesList.getChildren()){
                            if(node instanceof PiDomeRemoteItem && ((PiDomeRemoteItem)node).getRemoteId()==remote.getId()){
                                found = (PiDomeRemoteItem)node;
                                break;
                            }
                        }
                        if(found!=null){
                            toRemove.add(found);
                        }
                    }
                    Platform.runLater(() -> { 
                        remotesList.getChildren().removeAll(toRemove);
                    });
                }
            }
        }
    }
    
    private static class PiDomeRemoteItem extends HBox {
        
        PiDomeRemote remote;
        
        PiDomeRemoteItem(PiDomeRemote remote,ListClickedHandler<Integer> handler){
            this.remote = remote;
            Text name = new Text(remote.getName());
            name.getStyleClass().add("text");
            this.getChildren().add(name);
            getStyleClass().addAll("list-item", "list-item-pressable", "undecorated-list-item");
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                handler.itemClicked(remote.getId(), remote.getName());
            });
        }
        
        private int getRemoteId(){
            return this.remote.getId();
        }
        
        private PiDomeRemote getRemote(){
            return this.remote;
        }
        
        private void destroy(){
            this.remote = null;
        } 
        
    }
    
    @Override
    public void close() {
        remotesList.prefWidthProperty().unbind();
        service.getRemotes().removeListener(remotesMutator);
    }

    @Override
    public Pane getPane() {
        return this.container;
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
    public void itemClicked(Integer item, String itemDescription) {
        for(PiDomeRemote remote:service.getRemotes().subList(0, service.getRemotes().size())){
            if(remote.getId() == item){
                ScenesHandler.setSceneBackTitle(this, "locations", itemDescription);
                currentRemote = new PiDomeRemoteDisplay(remote);
                currentRemote.setupContent();
                Platform.runLater(() -> { mainContent.setContent(currentRemote); });
                break;
            }
        }
    }

    @Override
    public void handleListBack(String id) {
        ScenesHandler.setSceneTitle("Remotes");
        if(currentRemote!=null){
            //currentRemote
        }
        Platform.runLater(() -> { mainContent.setContent(remotesList); });
    }
    
}