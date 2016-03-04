/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.menubars;

import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.pidome.client.PiDomeClient;
import org.pidome.client.menus.AppFixedMenu;
import org.pidome.client.menus.AppMainMenu;
import org.pidome.client.menus.AppMobileMenu;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.ScenesHandler.ScenePane;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public abstract class MenuBarBase extends HBox {
    
    private final Text visualTitle = new Text("");
    private Text menuBars = new Text("\uE5D2");
    
    private AppMainMenu mainMenu;
    
    HBox menuBox = new HBox(8);
    HBox buttonBox = new HBox(12);
    
    private boolean menuOpened = false;
    
    EventHandler<MouseEvent> menuOpenerHandler = this::menuOpenerHandler;
    
    private PCCSystem system;
    
    private ListBackHandler currentBackHandler;
    private String          currentBackHandlerId;
    
    private ScenesHandler.ScenePane currentScene = ScenesHandler.ScenePane.LOGIN;
    
    public MenuBarBase(){
        ///Material Icons
        this.getStyleClass().add("menu-bar");
        this.setPadding(Insets.EMPTY);
        
        visualTitle.getStyleClass().add("title");
        visualTitle.setStyle("-fx-font-size: 1.3em;");
        
        menuBars.getStyleClass().add("menu-icon");
        
        menuBox.setPadding(new Insets(5));
        if(PiDomeClient.asPureDashboard()){
            menuBars = GlyphsDude.createIcon(MaterialDesignIcon.TELEVISION_GUIDE, String.valueOf("2em"));
            menuBox.getChildren().addAll(menuBars, visualTitle);
        } else {
            menuBars.setStyle("-fx-font-family: 'Material Icons'; -fx-font-size:2em;");
            menuBox.getChildren().addAll(menuBars, visualTitle);
        }
        menuBox.setAlignment(Pos.CENTER_LEFT);
        menuBox.getStyleClass().add("main-menu-button");
        menuBox.setTranslateX(-1);
        HBox.setHgrow(buttonBox, Priority.ALWAYS);
        HBox.setMargin(buttonBox, new Insets(0,12,0,0));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        getChildren().addAll(menuBox, buttonBox);
    }
    
    private boolean started = false;
    
    public boolean started(){
        return this.started;
    }
    
    protected final void setStarted(boolean started){
        this.started = started;
    }
    
    protected final void addToButtonBox(Node node){
        Platform.runLater(() -> {
            buttonBox.getChildren().add(0, node);
        });
    }
    
    public final void setTitle(String title) {
        if(!PiDomeClient.asPureDashboard()){
            currentBackHandler   = null;
            currentBackHandlerId = "";
            Platform.runLater(() -> {
                menuBars.setText("\uE5D2");
                visualTitle.setText(title);
            });
        } else {
            Platform.runLater(() -> { visualTitle.setText(this.system.getLocalSettings().getStringSetting("user.login", "Display client")); });
        }
    }
    
    public final void setSceneBackTitle(final ListBackHandler handler, final String id, String title){
        this.currentBackHandler   = handler;
        this.currentBackHandlerId = id;
        Platform.runLater(() -> {
            menuBars.setText("\uE5CB");
            visualTitle.setText(title);
        });
    }
    
    public final void handleExternalBackAction(){
        if(currentScene != ScenesHandler.ScenePane.LOGIN){
            if(currentBackHandler != null && !currentBackHandlerId.isEmpty()){
                currentBackHandler.handleListBack(currentBackHandlerId);
            } else if(currentScene != ScenePane.DASHBOARD) {
                PiDomeClient.switchScene(ScenePane.DASHBOARD);
            }
        }
    }
    
    public final void setMainMenuItem(ScenesHandler.ScenePane scenePane){
        currentScene = scenePane;
        if(mainMenu!=null){
            mainMenu.setMainMenuItem(scenePane);
        }
    }
    
    public final void start(PCCSystem system, ServiceConnector serviceConnector){
        this.system = system;
        if(mainMenu==null && !PiDomeClient.asPureDashboard()){
            switch(system.getConnection().getConnectionProfile()){
                case MOBILE:
                    mainMenu = new AppMobileMenu(this.system, serviceConnector);
                break;
                default:
                    mainMenu = new AppFixedMenu(this.system, serviceConnector);
                break;
            }
            mainMenu.build();
            mainMenu.setOnSwipeLeft((SwipeEvent event) -> {
                event.consume();
                closeMenu();
            });
            menuBox.addEventHandler(MouseEvent.MOUSE_CLICKED, menuOpenerHandler);
        } else {
            visualTitle.setText(this.system.getLocalSettings().getStringSetting("user.login", "Display client"));
        }
        resume(system);
    }
    
    protected final void destroy(){
        menuBox.removeEventHandler(MouseEvent.MOUSE_CLICKED, menuOpenerHandler);
    }
    
    public final void toggleMenu(){
        if(currentScene != ScenesHandler.ScenePane.LOGIN){
            if(!menuOpened && (currentBackHandler == null || currentBackHandlerId.isEmpty())){
                currentBackHandler = null;
                currentBackHandlerId = "";
                menuOpened = true;
                menuBox.getStyleClass().add("active");
                if(system.getConnection().getConnectionProfile() == Profile.FIXED){
                    mainMenu.setTranslateX(-402);
                    ScenesHandler.addContent(mainMenu);
                    menuSlideIn();
                } else {
                    ScenesHandler.addContent(mainMenu);
                }
                mainMenu.opened();
            } else {
                closeMenu();
            }
        }
    }
    
    private void menuOpenerHandler(MouseEvent event){
        event.consume();
        if(currentScene != ScenesHandler.ScenePane.LOGIN){
            if(currentBackHandler != null && !currentBackHandlerId.isEmpty()){
                currentBackHandler.handleListBack(currentBackHandlerId);
            } else if(!menuOpened){
                currentBackHandler = null;
                currentBackHandlerId = "";
                menuOpened = true;
                menuBox.getStyleClass().add("active");
                //if(system.getConnection().getConnectionProfile() == Profile.FIXED){
                //    mainMenu.setTranslateX(-402);
                //    ScenesHandler.addContent(mainMenu);
                //    menuSlideIn();
                //} else {
                    ScenesHandler.addContent(mainMenu);
                //}
                mainMenu.opened();
            } else {
                closeMenu();
            }
        }
    }
    
    private void menuSlideIn(){
        if(mainMenu!=null){
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(1);
            timeline.setAutoReverse(false);
            final KeyValue kv = new KeyValue(mainMenu.translateXProperty(), 0);
            final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        }
    }
    
    private void menuSlideOut(){
        if(mainMenu!=null){
            //if(system.getConnection().getConnectionProfile() == Profile.FIXED){
            //    final Timeline timeline = new Timeline();
            //    timeline.setOnFinished((EventHandler)(Event event) -> {
            //        ScenesHandler.removeContent(mainMenu);
            //        menuBox.getStyleClass().remove("active");
            //        menuOpened = false;
            //    });
            //    timeline.setCycleCount(1);
            //    timeline.setAutoReverse(false);
            //    final KeyValue kv = new KeyValue(mainMenu.translateXProperty(), -402);
            //   final KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
                
            //    timeline.getKeyFrames().add(kf);
            //    timeline.play();
            //} else {
                ScenesHandler.removeContent(mainMenu);
                menuBox.getStyleClass().remove("active");
                menuOpened = false;
            //}
        }
    }
    
    protected final boolean menuIsOpen(){
        return this.menuOpened;
    }
    
    protected final void closeMenu(){
        menuSlideOut();
    }
    
    protected abstract void resume(PCCSystem system);
    
    protected abstract void resumeDestroy();
    
    protected abstract void build();
    
}