/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.pidome.client.phone.scenes.menus.MenuBase;
import org.pidome.client.phone.scenes.visuals.DialogBox;
import org.pidome.client.phone.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public abstract class BaseScene implements PositionedPane {
    
    /**
     * The PCC system to visualize progress.
     */
    PCCSystem system;

    /**
     * The device specific service connector providing backend services to the front end like GPS etc..
     */
    ServiceConnector serviceConnector;
    
    private final StackPane canvas = new StackPane();
    private final VBox parent = new VBox();
    private final ScrollPane content = new ScrollPane();
    private SceneHeader header;
    
    private QuickViewMenu quickViewMenu;
    
    private boolean loginScene = true;
    
    private SimpleDoubleProperty contentBaseHeightProperty = new SimpleDoubleProperty(0.0);
    
    public BaseScene(boolean systemReady){
        this(systemReady, false);
    }
    
    public BaseScene(boolean systemReady, boolean loginScene){
        this.loginScene = loginScene;
        if(systemReady){
            header = new SceneHeader(this);
            parent.getChildren().add(header);
            VBox.setVgrow(content, Priority.ALWAYS);
            contentBaseHeightProperty.bind(canvas.heightProperty().subtract(header.heightProperty()));
        } else {
            contentBaseHeightProperty.bind(canvas.heightProperty());
        }
        parent.setId("scene-parent-stack");
        
        content.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        content.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        content.setFitToWidth(true);
        parent.getChildren().add(content);
        canvas.getChildren().add(parent);
        
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
            List<MenuBase> removeList = new ArrayList<>();
            for(Node node:canvas.getChildren()){
                if(node instanceof MenuBase){
                    removeList.add((MenuBase)node);
                }
            }
            canvas.getChildren().removeAll(removeList);
        });
        
    }
    
    public final SimpleDoubleProperty getBaseContentHeightProperty(){
        return contentBaseHeightProperty;
    }
    
    public final SceneHeader getSceneHeader(){
        return this.header;
    }
    
    protected void showLeftMenu(){
        if(quickViewMenu!=null){
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(1);
            timeline.setAutoReverse(false);
            final KeyValue kv = new KeyValue(quickViewMenu.translateXProperty(), -2);
            final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        }
    }
    
    protected final void hideLeftMenu(){
        if(quickViewMenu!=null){
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(1);
            timeline.setAutoReverse(false);
            final KeyValue kv = new KeyValue(quickViewMenu.translateXProperty(), -282);
            final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        }
    }
    
    public final void setSceneTitle(String title){
        if(header!=null){
            header.setSceneTitle(title);
        }
    }
    
    public final ScrollPane getContentPane(){
        return content;
    }
    
    public final void setContent(Parent content){
        this.content.maxWidth(Screen.getPrimary().getBounds().getWidth());
        Platform.runLater(() -> {
            this.content.setContent(content);
        });
    }
    
    public StackPane getSceneContent() {
        return canvas;
    }
    
    public void setSystem(ScenesSwitcher switcher, PCCSystem system, ServiceConnector serviceConnector) {
        if(this.serviceConnector == null){
            this.serviceConnector = serviceConnector;
        }
        if(this.system==null){
            this.system = system;
        }
        if(header!=null){
            header.setSystem(this.system, this.serviceConnector);
        }
        if(!this.loginScene){
            /*canvas.setOnSwipeRight((SwipeEvent event) -> {
                event.consume();
                showLeftMenu();
            });*/
            quickViewMenu = new QuickViewMenu(switcher, this, this.system, serviceConnector);
            quickViewMenu.setPrefSize(280, Screen.getPrimary().getBounds().getHeight());
            quickViewMenu.setMaxSize(Control.USE_PREF_SIZE,Control.USE_PREF_SIZE);
            quickViewMenu.setMinSize(Control.USE_PREF_SIZE,Control.USE_PREF_SIZE);
            quickViewMenu.setTranslateX(-282);
            canvas.getChildren().add(quickViewMenu);
        }
    }
    
    public boolean hasSystem(){
        return this.system!=null;
    }
    
    public final PCCSystem getSystem(){
        return this.system;
    }
    
    public final ServiceConnector getService(){
        return this.serviceConnector;
    }
    
    public final void removeSystem() {
        this.system = null;
        this.serviceConnector = null;
    }
    
    @Override
    public final void openAtPosition(double x, double y, Parent node){
        Platform.runLater(() -> {
            canvas.getChildren().add(node);
            node.setTranslateX(x - node.getLayoutBounds().getMinX());
            node.setTranslateY(y - node.getLayoutBounds().getMinY());
        });
    }
    
    @Override
    public final boolean hasPositioned(Node node){
        return canvas.getChildren().contains(node);
    }

    @Override
    public final void closePositioned(Node node) {
        Platform.runLater(() -> {
            canvas.getChildren().remove(node);
        });
    }
    
    @Override
    public void showPopup(DialogBox node) {
        Platform.runLater(() -> {
            canvas.getChildren().add(node);
        });
    }

    @Override
    public void closePopup(DialogBox node) {
        Platform.runLater(() -> {
            canvas.getChildren().remove(node);
        });
    }

    @Override
    public boolean hasPopup(DialogBox node) {
        return canvas.getChildren().contains(node);
    }
    
    public abstract void run();
    
    public abstract void stop();
    
}