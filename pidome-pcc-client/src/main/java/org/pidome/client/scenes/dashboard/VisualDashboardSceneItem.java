/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.entities.dashboard.DashboardSceneItem;
import org.pidome.client.entities.scenes.Scene;
import org.pidome.client.entities.scenes.ScenesServiceException;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public final class VisualDashboardSceneItem extends VisualDashboardItem {

    private final PropertyChangeListener SceneRunningListener = this::sceneRunning;
    private final PropertyChangeListener SceneNamingListener = this::sceneNaming;
    private Scene scene;
    private final Label sceneNameText = new Label("");
    private final Text sceneBgText   = new Text("");
    
    protected VisualDashboardSceneItem(PCCSystem system, DashboardItem item) {
        super(system, item);
        this.getStyleClass().add("dashboard-scene");
        sceneNameText.getStyleClass().add("scenename");
        sceneNameText.setMaxWidth(this.getMaxWidth());
        sceneNameText.setTextAlignment(TextAlignment.CENTER);
        
        sceneBgText.getStyleClass().add("scenenamebg");
        sceneBgText.setTextAlignment(TextAlignment.CENTER);
        
        Rectangle clipper = new Rectangle(this.width, this.height);
        this.setClip(clipper);
        getChildren().addAll(sceneBgText, sceneNameText);
    }
    
    private void sceneRunning(PropertyChangeEvent evt){
        changeScene((boolean)evt.getNewValue());
    }
    
    private void sceneNaming(PropertyChangeEvent evt){
        Platform.runLater(() -> {
            sceneBgText.setText((String)evt.getNewValue()); 
            sceneNameText.setText((String)evt.getNewValue());
        });
    }
    
    private void changeScene(boolean active){
        Platform.runLater(() -> {
            if(active==true){
                this.getStyleClass().add("active");
            } else {
                this.getStyleClass().remove("active");
            }
        });
    }
    
    @Override
    protected void build() {
        try {
            scene = getSystem().getClient().getEntities().getScenesService().getScene(((DashboardSceneItem)this.getDashboardItem()).getSceneId());
            Platform.runLater(() -> { 
                sceneBgText.setText(scene.getSceneName().getValueSafe()); 
                sceneNameText.setText(scene.getSceneName().getValueSafe());
            });
            this.setOnMouseClicked((MouseEvent me) -> {
                if(this.getStyleClass().contains("active")){
                    scene.deActivateScene();
                } else {
                    scene.activateScene();
                }
            });
            scene.getSceneActive().addPropertyChangeListener(SceneRunningListener);
            scene.getSceneName().addPropertyChangeListener(SceneNamingListener);
            changeScene(scene.getSceneActive().getValue());
        } catch (EntityNotAvailableException | ScenesServiceException ex) {
            Logger.getLogger(VisualDashboardSceneItem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void destruct() {
        scene.getSceneName().removePropertyChangeListener(SceneNamingListener);
        scene.getSceneActive().removePropertyChangeListener(SceneRunningListener);
    }
    
}
