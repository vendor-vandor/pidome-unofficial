/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.config.AppResources;
import org.pidome.client.system.domotics.components.notifications.Notifications;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopPane;
import org.pidome.client.system.scenes.components.mainstage.ApplicationsBar;
import org.pidome.client.system.scenes.components.mainstage.NotificationBlock;
import org.pidome.client.system.scenes.components.mainstage.QuickAppMenu;
import org.pidome.client.system.scenes.components.mainstage.desktop.WidgetDesktop;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 * The main stage holder.
 * Creates the app screen.
 * @author John Sirach
 */
public final class MainScene {
    
    static Stage mainStage;
    static Pane root = new Pane();
    static Pane contentPane = new Pane();
    Scene appScene;
    
    int sizeX;
    int sizeY;

    //// Normal mode stuff
    TilePane desktop;
    QuickAppMenu quickAppMenu;
    ApplicationsBar notificationBar;
    
    /// Widget mode stuff
    NotificationBlock notificationBlock;
    WidgetDesktop widgetDesktop;
    
    static NotificationsDisplay notifications = new NotificationsDisplay();
    
    WindowManager winManager = WindowManager.getInstance();
    
    static List _listeners = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(MainScene.class);
    
    boolean mainStageOpen = true;
    
    /**
     * creates the main stage, and paints the viewport for it.
     */
    public MainScene(){
        root.getStylesheets().add(AppResources.getCss("main.css"));
        root.getStylesheets().add(AppResources.getCss("skin-dark.css"));
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            root.getStylesheets().add(AppResources.getCss("high.css"));
        }
        root.setStyle("-fx-font-size: " + DisplayConfig.getFontDpiScaler() +"px;");
        if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_WIDGET)){
            root.getStylesheets().add(AppResources.getCss("widgetdock.css"));
            notificationBlock = new NotificationBlock();
            widgetDesktop     = new WidgetDesktop();
        } else {
            desktop         = DesktopPane.getDesktop().getDesktopPlane();
            quickAppMenu    = new QuickAppMenu();
            notificationBar = new ApplicationsBar();
            try {
                if(!AppProperties.getProperty("system", "display.background").equals("default")){
                    root.setStyle("-fx-font-size: " + DisplayConfig.getFontDpiScaler() +"px; -fx-background-image:url(\"file:resources/images/backgrounds/"+AppProperties.getProperty("system", "display.background")+"\");");
                }
            } catch (AppPropertiesException ex) {
                /// do nothing with it
            }
        }
        notifications.setDisplay(contentPane);
    }
    
    /**
     * Creates the lowest scene.
     */
    public final void createScene(){
        appScene = new Scene(root, DisplayConfig.getScreenWidth(), DisplayConfig.getScreenHeight());
        if(!DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_WIDGET)){
            appScene.getStylesheets().add(AppResources.getCss("KeyboardButtonStyle.css"));
        }
        runPreloader();
        root.getStyleClass().add("rootpane");
        desktop.setTranslateX(20 * DisplayConfig.getWidthRatio());
        desktop.setTranslateY(80 * DisplayConfig.getHeightRatio());
        contentPane.getChildren().add(desktop);
        contentPane.getChildren().add(quickAppMenu);
        contentPane.getChildren().add(notificationBar);
        //contentPane.setOpacity(0.1); /// reference for brightness when build in
        root.getChildren().add(contentPane);
        root.setBackground(Background.EMPTY);
        _fireSceneBuildDone();
    }
    
    public static void showNotification(Notifications.Notification clientMessage){
        notifications.add(new NotificationMessage(clientMessage.getType(),clientMessage.getSubject(),clientMessage.getMessage()));
    }
    
    public final void createWidgetScene(){
        runPreloader();
        Widget widget = new Widget(root, mainStage);
        widget.setNotificationBlock(notificationBlock);
        widget.setDesktopBlock(widgetDesktop);
        widget.build();
        appScene = widget;
        _fireSceneBuildDone();
    }
    
    final void runPreloader(){
        ImageLoader.preload("displays/listscreenmedium.png", 764, 427);
        ImageLoader.preload("displays/device.png", 549, 634);
        ImageLoader.preload("notificationbar/windowicon.png", 81, 45);
        ImageLoader.preload("notificationbar/windowicon-subwindows.png", 12, 14);
    }
    
    public static Stage getWindow(){
        return mainStage;
    }
    
    public void setRoot(Stage stage){
        mainStage = stage;
    }
    
    public static Pane getRootPane(){
        return root;
    }
    
    public static Pane getPane(){
        return contentPane;
    }
    
    final void setRootPaneDropHandler(){

    }
    
    /**
     * Returns the Scene
     * @return Scene
     */
    public final Scene scene(){
        return appScene;
    }
    
    public final void stop(){
        //topBar.stopThreads();
    }
    
    public synchronized static void addDoneListener(MainSceneEventListener l){
        _listeners.add(l);
    }
    
    final synchronized void _fireSceneBuildDone(){
        LOG.debug("New event: {}", MainSceneEvent.SCENEBUILDDONE);
        MainSceneEvent serviceEvent = new MainSceneEvent(this, MainSceneEvent.SCENEBUILDDONE);
        Iterator listeners = _listeners.iterator();
        while (listeners.hasNext()) {
            ((MainSceneEventListener) listeners.next()).handleMainSceneEvent(serviceEvent);
        }
    }
    
}
