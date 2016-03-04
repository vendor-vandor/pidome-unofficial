/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pidome.client.phone.scenes.dashboard.Dashboard;
import org.pidome.client.phone.scenes.devices.DevicesLocationList;
import org.pidome.client.phone.scenes.login.LoginScene;
import org.pidome.client.phone.scenes.macros_events.MacrosEvents;
import org.pidome.client.phone.scenes.media.MediaLocationList;
import org.pidome.client.phone.services.LifeCycleHandlerInterface;
import org.pidome.client.phone.services.LifeCycleHandlerStatusListener;
import org.pidome.client.phone.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class ScenesHandler implements LifeCycleHandlerStatusListener, ScenesSwitcher {
    
    private Scene scene;
    private final Rectangle2D bounds;
    private final Stage mainStage;
    private LifeCycleHandlerInterface foregroundStatus;
    private ServiceConnector serviceConnector;
    
    BaseScene currentFrame;
    
    private static ScenePane currentScene = ScenePane.NONE;
    
    public static final ScenePane getCurrentScenePane(){
        return currentScene;
    }
    
    public ScenesHandler(Stage mainStage) throws IOException, Exception {
        this.mainStage = mainStage;
        bounds = Screen.getPrimary().getVisualBounds();
        try {
            currentFrame = new LoginScene();
        } catch (Exception ex) {
            Logger.getLogger(ScenesHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        scene  = new Scene(currentFrame.getSceneContent(), bounds.getWidth(), bounds.getHeight());
        System.out.println("ScenesHandler initialized with bounds width: " + bounds.getWidth() + ", height: " + bounds.getHeight());
        currentScene = ScenePane.LOGIN;
        mainStage.setScene(scene);
        currentFrame.run();
        mainStage.show();
    }

    private void startLifeCycleListener(LifeCycleHandlerInterface foregroundStatus){
        this.foregroundStatus = foregroundStatus;
        foregroundStatus.addLifecycleListener(this);
    }
    
    public enum ScenePane {
        NONE,
        LOGIN,
        DASHBOARD,
        DEVICES,
        MEDIA,
        MACROS_EVENTS
    }
    
    /**
     * The PCC system.
     */
    private PCCSystem system;
    
    public void setSystem(PCCSystem system, ServiceConnector serviceConnector){
        startLifeCycleListener(serviceConnector.getLifeCycleHandler());
        this.system = system;
        this.serviceConnector = serviceConnector;
        if(currentFrame!=null){
            currentFrame.setSystem(this, this.system, this.serviceConnector);
        }
    }
    
    public final Scene getScene(){
        return scene;
    }
    
    @Override
    public final synchronized void switchScene(ScenePane pane){
        System.out.println("ScenesHandler: Switching scenes " + (currentScene!=pane) + " (from "+currentScene+" to "+pane.toString()+")");
        if(currentScene!=pane){
            try {
                System.out.println("Stopping: " + currentScene);
                currentFrame.stop();
                currentFrame.removeSystem();
            } catch (Exception ex){
                Logger.getLogger(ScenesHandler.class.getName()).log(Level.WARNING, "Issue unsetting current scene data", ex);
            }
            currentScene = pane;
            try {
                BaseScene newFrame;
                switch(pane){
                    case DASHBOARD:
                        newFrame = new Dashboard();
                    break;
                    case DEVICES:
                        newFrame = new DevicesLocationList();
                    break;
                    case MEDIA:
                        newFrame = new MediaLocationList();
                    break;
                    case MACROS_EVENTS:
                        newFrame = new MacrosEvents();
                    break;
                    default:
                        newFrame = new LoginScene();
                    break;
                }
                Platform.runLater(() -> {
                    scene = new Scene(newFrame.getSceneContent(), bounds.getWidth(), bounds.getHeight());
                    scene.getStylesheets().add(getClass().getResource("/display/css/platform.css").toExternalForm());
                    scene.getStylesheets().add(getClass().getResource("/display/css/default.css").toExternalForm());
                    scene.getStylesheets().add(getClass().getResource("/display/css/dark.css").toExternalForm());
                    mainStage.setScene(scene);
                    System.err.println("Setting null system: " + this.system);
                    newFrame.setSystem(this,this.system, this.serviceConnector);
                    newFrame.run();
                    currentFrame = newFrame;
                });
            } catch (Exception ex) {
                Logger.getLogger(ScenesHandler.class.getName()).log(Level.SEVERE, "Error switching scenes", ex);
            }
        }
    }
    
    @Override
    public void handleInForeground(boolean focus) {
        /// Not needed.
    }
    
    private void setScene(BaseScene newFrame){

    }
    
}