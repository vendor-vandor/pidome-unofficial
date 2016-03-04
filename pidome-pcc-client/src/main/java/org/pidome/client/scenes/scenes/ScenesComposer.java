/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.scenes;

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
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.scenes.Scene;
import org.pidome.client.entities.scenes.ScenesService;
import org.pidome.client.entities.scenes.ScenesServiceException;
import org.pidome.client.scenes.macros.MacrosComposer;
import org.pidome.client.scenes.presences.PresenceComposer;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.utilities.properties.ObservableArrayListBeanChangeListener;
import org.pidome.pcl.utilities.properties.ReadOnlyBooleanPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyStringPropertyBindingBean;

/**
 *
 * @author John
 */
public class ScenesComposer extends VBox {
    
    private PCCSystem system;
    
    private ScenesService scenesService;
    private ReadOnlyObservableArrayListBean<Scene> scenesList;
    private ObservableArrayListBeanChangeListener<Scene> scenesListHelper = this::scenesListChanged;
    
    protected ScenesComposer(){
        this.getStyleClass().add("custom-list-view");
    }
    
    public void start() {
        try {
            scenesService = this.system.getClient().getEntities().getScenesService();
            this.scenesList = scenesService.getScenesList();
            this.scenesList.addListener(scenesListHelper);
            scenesService.reload();
        } catch (EntityNotAvailableException | ScenesServiceException ex) {
            Logger.getLogger(PresenceComposer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        this.scenesList.removeListener(scenesListHelper);
        for(Node pane:getChildren()){
            if(pane instanceof VisualScene){
                ((VisualScene)pane).destroy();
            }
        }
    }
    
    private void scenesListChanged(ObservableArrayListBeanChangeListener.Change<? extends Scene> change){
        if(change.wasAdded()){
            if(change.hasNext()){
                final List<VisualScene> visualScenesList = new ArrayList<>();
                if(change.hasNext()){
                    for(Scene scene:change.getAddedSubList()){
                        visualScenesList.add(new VisualScene(scene));
                    }
                }
                Collections.sort(visualScenesList, (VisualScene arg0, VisualScene arg1) -> arg0.getName().compareToIgnoreCase(arg1.getName()));
                Platform.runLater(() -> {
                    getChildren().setAll(visualScenesList);
                });
            }
            if(change.wasRemoved()){
                List<VisualScene> toRemove = new ArrayList<>();
                if(change.hasNext()){
                    for(Scene scene:change.getRemoved()){
                        for(Node pane:getChildren()){
                            if(((VisualScene)pane).getPiDomeScene().equals(scene)){
                                VisualScene removePane = (VisualScene)pane;
                                removePane.destroy();
                                toRemove.add(removePane);
                            }
                        }
                    }
                }
                Platform.runLater(() -> { 
                    getChildren().removeAll(toRemove);
                });
            }
        }
    }
    
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    public void removeSystem() {
        this.system = null;
    }
    
    protected static class VisualScene extends HBox {
        
        private Scene scene;
        ReadOnlyStringPropertyBindingBean sceneName;
        ReadOnlyBooleanPropertyBindingBean sceneActive;
        Text visualSceneName = new Text();
        
        Text sceneIcon = GlyphsDude.createIcon(FontAwesomeIcon.LIGHTBULB_ALT, "1.1em");
        
        PropertyChangeListener nameChanged = this::sceneNameChanged;
        PropertyChangeListener sceneActiveChanged = this::sceneActiveChanged;
        
        protected VisualScene(Scene scene){
            super(5);
            this.getStyleClass().addAll("list-item", "scene-item");
            this.scene = scene;
            sceneName = scene.getSceneName();
            sceneActive = scene.getSceneActive();
            build();
            sceneName.addPropertyChangeListener(nameChanged);
            sceneActive.addPropertyChangeListener(sceneActiveChanged);
            addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
                if(sceneIcon.getStyleClass().contains("active")){
                    scene.deActivateScene();
                } else {
                    scene.activateScene();
                }
            });
        }
        
        public final String getName(){
            return this.visualSceneName.getText();
        }
        
        private void build(){
            visualSceneName.getStyleClass().add("text");
            if(sceneActive.getValue()==true){
                sceneIcon.getStyleClass().addAll("text", "icon", "active");
            } else {
                sceneIcon.getStyleClass().addAll("text", "icon");
            }
            visualSceneName.setText(sceneName.getValue());
            this.getChildren().addAll(sceneIcon, visualSceneName);
        }

        private void sceneActiveChanged(PropertyChangeEvent evt){
            if((boolean)evt.getNewValue()==true && !sceneIcon.getStyleClass().contains("active")){
                Platform.runLater(() -> {  sceneIcon.getStyleClass().add("active"); });
            } else {
                Platform.runLater(() -> {  sceneIcon.getStyleClass().remove("active"); });
            }
        }
        
        private void sceneNameChanged(PropertyChangeEvent evt){
            Platform.runLater(() -> { 
                visualSceneName.setText(sceneName.getValue());
            });
        }
        
        protected final void destroy(){
            sceneName.removePropertyChangeListener(nameChanged);
            sceneActive.removePropertyChangeListener(sceneActiveChanged);
            this.scene = null;
        }
        
        protected final Scene getPiDomeScene(){
            return this.scene;
        }
        
    }
    
}