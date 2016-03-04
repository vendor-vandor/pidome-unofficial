/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.pidome.client.menubars.MenuBarBase;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 * This should be used in absolute base.
 * @author John
 */
public class BaseScenePane extends BorderPane {
    
    /**
     * The PCC system to visualize progress.
     */
    PCCSystem system;

    /**
     * The device specific service connector providing backend services to the front end like GPS etc..
     */
    ServiceConnector serviceConnector;
    
    Label sceneTitle = new Label();
    StackPane content = new StackPane();
    
    private SimpleDoubleProperty contentWidth  = new SimpleDoubleProperty();
    private SimpleDoubleProperty contentHeight = new SimpleDoubleProperty();
    
    public BaseScenePane(){
        content.getStyleClass().add("app-content");
        this.setCenter(content);
        contentWidth.bind(content.widthProperty());
        contentHeight.bind(content.heightProperty());
    }
    
    private MenuBarBase getMenuBar(){
        return (MenuBarBase)this.getTop();
    }
    
    protected SimpleDoubleProperty getContentWidth(){
        return contentWidth;
    }
    
    protected SimpleDoubleProperty getContentHeight(){
        return contentHeight;
    }
    
    public final void setTitle(String title){
        Platform.runLater(() -> {
            try {
                getMenuBar().setTitle(title);
            } catch (NullPointerException ex){
                System.out.println(ex);
            }
        });
    }
    
    public final void setMenuBar(MenuBarBase menuBar){
        BorderPane.setMargin(menuBar, new Insets(0,0,0,0));
        this.setTop(menuBar);
    }
    
    public final void setMainMenuItem(ScenesHandler.ScenePane scenePane){
        if(hasMenuBar()){
            getMenuBar().setMainMenuItem(scenePane);
        }
    }
    
    public final boolean hasMenuBar(){
        return getMenuBar()!=null;
    }
    
    public final void removeMenuBar(MenuBarBase menuBar){
        Platform.runLater(() -> {
            this.getChildren().remove(menuBar);
        });
    }
    
    
    public final Region getRoot(){
        return (Region)content.getChildren().get(0);
    }
    
    public final void replaceRoot(ScenePaneImpl currentFrame, Pane newPane){
        if(currentFrame!=null){
            currentFrame.close();
            currentFrame.removeSystem();
        }
        Platform.runLater(() -> {
            closePopUps();
            if(content.getChildren().size()>0){
                content.getChildren().remove(0);
            }
            content.getChildren().add(0,newPane);
        });
    }
    
    public final void removeRoot(){
        Platform.runLater(() -> {
            if(content.getChildren().size()>0){
                content.getChildren().remove(0);
            }
        });
        closePopUps();
    }
    public final void setRoot(Pane pane){
        Platform.runLater(() -> {
            content.getChildren().add(0,pane);
        });
    }
    
    public final void closePopUps(){
        for(Node node:content.getChildren()){
            if(node instanceof PopUp){
                ((PopUp)node).close();
            }
        }
    }
    
    public final void addContent(Pane node){
        Platform.runLater(() -> {
            content.getChildren().add(node);
        });
    }
    public final void addContent(Node node){
        Platform.runLater(() -> {
            content.getChildren().add(node);
        });
    }
    
    public final boolean hasContent(Pane node){
        return content.getChildren().contains(node);
    }
    public final boolean hasContent(Node node){
        return content.getChildren().contains(node);
    }
    
    public final void removeContent(Node node){
        Platform.runLater(() -> {
            content.getChildren().remove(node);
        });
    }
    
}