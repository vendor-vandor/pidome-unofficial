/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.pidome.client.css.themes.Themes;
import org.pidome.client.scenes.dashboard.VisualDashboard;
import org.pidome.client.scenes.login.LoginLoader;
import org.pidome.client.scenes.panes.popups.ErrorMessage;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.scenes.panes.popups.PopUp.PopUpButton;
import org.pidome.client.menubars.MenuBarProvider;
import org.pidome.client.scenes.devicediscovery.DeviceDiscoveryLargeScene;
import org.pidome.client.scenes.devices.DevicesLargeScene;
import org.pidome.client.scenes.devices.DevicesSmallScene;
import org.pidome.client.scenes.floormap.FloorMapLargeScreen;
import org.pidome.client.scenes.floormap.FloorMapSmallScreen;
import org.pidome.client.scenes.login.OrientationLoader;
import org.pidome.client.scenes.macros.MacrosSmallScreen;
import org.pidome.client.scenes.media.MediaLargeScene;
import org.pidome.client.scenes.media.MediaSmallScene;
import org.pidome.client.scenes.navigation.ListBackHandler;
import org.pidome.client.scenes.panes.popups.SimpleDialog;
import org.pidome.client.scenes.pidomeremotes.PiDomeRemotesSmallScreen;
import org.pidome.client.scenes.presences.PresencesSmallScreen;
import org.pidome.client.scenes.scenes.ScenesSmallScreen;
import org.pidome.client.scenes.settings.FixedSettings;
import org.pidome.client.scenes.settings.MobileSettings;
import org.pidome.client.scenes.utilities.FixedUtilities;
import org.pidome.client.scenes.weather.WeatherLargeScreen;
import org.pidome.client.scenes.weather.WeatherSmallScreen;
import org.pidome.client.services.LifeCycleHandlerInterface;
import org.pidome.client.services.LifeCycleHandlerStatusListener;
import org.pidome.client.services.PhysicalDisplayInterface;
import org.pidome.client.services.PlatformOrientation;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.services.ServiceConnector.PlatformBase;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.PngEncoderFX;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public class ScenesHandler implements LifeCycleHandlerStatusListener, ScenesSwitcher, PlatformOrientation {
    
    private static Scene scene;
    private static Stage mainStage;
    private LifeCycleHandlerInterface foregroundStatus;
    private ServiceConnector serviceConnector;
    
    PhysicalDisplayInterface physicalDisplayHelper;
    
    private static ScenePaneImpl currentFrame;
    
    private static BaseScenePane containerParent = new BaseScenePane();
    
    private static StackPane masterParent = new StackPane();
    
    private static ScenePane currentScene = ScenePane.NONE;
    
    private static MenuBarProvider menuProvider = new MenuBarProvider();
    
    private boolean initialDone = false;
    
    EventHandler<MouseEvent> globalClickHandler = this::handleGlobalClicker;
    
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    
    private static DisplayType displayType = DisplayType.SMALL; /// to be on the safe side.
    
    private static Themes themes = new Themes();
    
    public static boolean isWindows(){
        return isWindows;
    }
    
    public static final ScenePane getCurrentScenePane(){
        return currentScene;
    }
    
    public ScenesHandler(Stage mainStage) throws IOException, Exception {
        ScenesHandler.mainStage = mainStage;
        mainStage.show();
        if(initialDone){
            currentFrame.start();
        }
        containerParent.addEventHandler(MouseEvent.MOUSE_CLICKED,globalClickHandler);
    }

    private void handleGlobalClicker(MouseEvent event){
        Platform.runLater(() -> {
            menuProvider.closeMenu();
        });
    }
    
    private void startLifeCycleListener(LifeCycleHandlerInterface foregroundStatus){
        this.foregroundStatus = foregroundStatus;
        foregroundStatus.addLifecycleListener(this);
    }
    
    public enum ScenePane {
        NONE,
        ORIENTATION_CHOICE,
        LOGIN,
        DASHBOARD,
        DEVICES,
        MEDIA,
        PRESENCES,
        MACROS_EVENTS,
        SCENES,
        FLOOR_MAP3D,
        FLOOR_MAP2D,
        REMOTES,
        DEVICE_DISCOVERY,
        UTILITIES,
        SETTINGS,
        WEATHER;
    }
    
    public static boolean isOnRootLevel(){
        return (currentScene == ScenePane.DASHBOARD) || (currentScene == ScenePane.LOGIN);
    }
    
    /**
     * The PCC system.
     */
    private PCCSystem system;
    
    public void setSystem(PCCSystem system, ServiceConnector serviceConnector){
        startLifeCycleListener(serviceConnector.getLifeCycleHandler());
        this.system = system;
        this.serviceConnector = serviceConnector;
        if(this.serviceConnector.getPlatformBase()==PlatformBase.FIXED){
            themes.setPreferences(this.serviceConnector.getPreferences());
            themes.loadPrefTheme();
        }
        displayType = this.serviceConnector.userDisplayType();
        if(currentFrame!=null){
            containerParent.setTitle(currentFrame.getTitle());
            if(!containerParent.hasMenuBar()){
                containerParent.setMenuBar(menuProvider.build(this.system, this.serviceConnector));
                menuProvider.start();
            }
            currentFrame.setSystem(this.system, this.serviceConnector);
            this.serviceConnector.addOrientationListener(this);
        }
        physicalDisplayHelper = this.serviceConnector.getPhysicalDisplayInterface();
        if(physicalDisplayHelper.getAvailableSupportTypes().contains(PhysicalDisplayInterface.Support.EMULATE_ON_OFF)){
            masterParent.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent event) -> {
                physicalDisplayHelper.updateBlankTimer();
            });
            Platform.runLater(() -> { 
                masterParent.getChildren().add(physicalDisplayHelper.getTouchOverlay()); 
            });
        }
    }
    
    public final Scene getScene(){
        return scene;
    }
    
    public static void exit(){
        mainStage.close();
    }

    public static void toggleFullScreen(){
        mainStage.setFullScreen(!mainStage.isFullScreen());
    }
    
    public static SimpleDoubleProperty getContentWidthProperty(){
        return containerParent.getContentWidth();
    }

    public static SimpleDoubleProperty getContentHeightProperty(){
        return containerParent.getContentHeight();
    }
    
    @Override
    public void handleOrientationChanged(PlatformOrientation.Orientation orientation){
        switch(orientation){
            case LANDSCAPE:
                DisplayType check = this.serviceConnector.userDisplayType();
                if(check == DisplayType.LARGE){
                    displayType = DisplayType.LARGE;
                } else {
                    displayType = DisplayType.SMALL;
                }
            break;
            case PORTRAIT:
                /// Portrait will ALWAYS go to
                displayType = DisplayType.SMALL;
            break;
        }
        switchScene(currentScene, true);
    }
    
    public final synchronized void switchScene(ScenePane pane, boolean forced){
        if(currentScene!=pane){
            currentScene = pane;
            try {
                ScenePaneImpl newFrame;
                switch(pane){
                    case ORIENTATION_CHOICE:
                        newFrame = new OrientationLoader();
                    break;
                    case DASHBOARD:
                        newFrame = new VisualDashboard();
                    break;
                    case DEVICES:
                        if(displayType == DisplayType.LARGE){
                            newFrame = new DevicesLargeScene();
                        } else {
                            newFrame = new DevicesSmallScene();
                        }
                    break;
                    case MEDIA:
                        if(displayType == DisplayType.LARGE){
                            newFrame = new MediaLargeScene();
                        } else {
                            newFrame = new MediaSmallScene();
                        }
                    break;
                    case PRESENCES:
                        newFrame = new PresencesSmallScreen();
                    break;
                    case SCENES:
                        newFrame = new ScenesSmallScreen();
                    break;
                    case MACROS_EVENTS:
                        newFrame = new MacrosSmallScreen();
                    break;
                    case REMOTES:
                        newFrame = new PiDomeRemotesSmallScreen();
                    break;
                    case FLOOR_MAP3D:
                        if(displayType == DisplayType.LARGE){
                            newFrame = new FloorMapLargeScreen();
                        } else {
                            newFrame = new FloorMapSmallScreen();
                        }
                    break;
                    case WEATHER:
                        if(displayType == DisplayType.LARGE){
                            newFrame = new WeatherLargeScreen();
                        } else {
                            newFrame = new WeatherSmallScreen();
                        }
                    break;
                    case DEVICE_DISCOVERY:
                        newFrame = new DeviceDiscoveryLargeScene();
                    break;
                    case UTILITIES:
                        newFrame = new FixedUtilities();
                    break;
                    case SETTINGS:
                        if(this.system.getConnection().getConnectionProfile().equals(Profile.MOBILE)){
                            newFrame = new MobileSettings();
                        } else {
                            newFrame = new FixedSettings();
                        }
                    break;
                    default:
                        newFrame = new LoginLoader();
                    break;
                }
                Platform.runLater(() -> {
                    if(scene == null){
                        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                        //mainStage.setX(bounds.getMinX());
                        //mainStage.setY(bounds.getMinY());
                        //mainStage.setMaxWidth(bounds.getWidth());
                        //mainStage.setMaxHeight(bounds.getHeight());
                        //masterParent.setMinSize(bounds.getWidth(), bounds.getHeight());
                        //masterParent.setMaxSize(bounds.getWidth(), bounds.getHeight());
                        masterParent.getChildren().add(containerParent);
                        StackPane.setAlignment(containerParent, Pos.TOP_LEFT);
                        scene = new Scene(masterParent, bounds.getWidth(), bounds.getHeight());
                        System.out.println("Width: " + bounds.getWidth() + ", height: " + bounds.getHeight());
                        mainStage.setScene(scene);
                        themes.setScene(scene);
                        themes.applyApp();
                        scene.getStylesheets().add(getClass().getResource("/org/pidome/client/css/struct.css").toExternalForm());
                        if (displayType==DisplayType.TINY){
                            double width = Screen.getPrimary().getVisualBounds().getWidth();
                            scene.getRoot().setStyle("-fx-font-size: "+((this.serviceConnector.getMaxWorkWidth()/width)*17)+"px;");
                        }
                        
                        /* 
                        /// press debug
                        mainStage.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                            System.out.println("Mouse Scene points x: " + event.getSceneX() + ", y: " + event.getSceneY());
                            System.out.println("Mouse Scene points x: " + event.getScreenX()+ ", y: " + event.getScreenY());
                        });
                        
                        mainStage.addEventHandler(TouchEvent.TOUCH_PRESSED, (TouchEvent event) -> {
                            System.out.println("Touch points: " + event.getTouchPoints());
                        });
                        */
                        
                        if(this.system.getConnection().getConnectionProfile() == Profile.FIXED){
                            mainStage.setFullScreen(true);
                        }
                        containerParent.setRoot(newFrame.getPane());
                    } else {
                        containerParent.replaceRoot(currentFrame, newFrame.getPane());
                    }
                    if(currentScene == ScenePane.DASHBOARD){
                        themes.applyDashboard();
                    } else {
                        themes.unsetDashboard();
                    }
                    currentFrame = newFrame;
                    if(this.system!=null){
                        if(menuProvider!=null){
                            menuProvider.closeMenu();
                        }
                        containerParent.setTitle(currentFrame.getTitle());
                        if(!containerParent.hasMenuBar()){
                            containerParent.setMenuBar(menuProvider.build(this.system, this.serviceConnector));
                            menuProvider.start();
                        }
                        if(menuProvider!=null){
                            menuProvider.setMainMenuItem(pane);
                        }
                        newFrame.setSystem(this.system, this.serviceConnector);
                        newFrame.start();
                    }
                });
            } catch (Exception ex) {
                Logger.getLogger(ScenesHandler.class.getName()).log(Level.SEVERE, "Error switching scenes", ex);
            }
        }
    }
    
    @Override
    public final synchronized void switchScene(ScenePane pane){
        switchScene(pane, false);
    }
    
    @Override
    public void handleInForeground(boolean focus) {
        /// Not needed.
    }
    
    public static void setSceneTitle(String title){
        if(menuProvider!=null){
            Platform.runLater( () -> { menuProvider.setTitle(title); } );
        }
    }
    
    public static void setSceneBackTitle(final ListBackHandler handler, final String id, String title){
        if(menuProvider!=null){
            menuProvider.setSceneBackTitle(handler, id, title);
        }
    }
    
    public static void hardwareBackButtonPressed(){
        menuProvider.handleExternalBackAction();
    }
    
    public static void toggleAppMenu(){
        menuProvider.toggleAppMenu();
    }
    
    public static void showError(String title, String message){
        showError(title, message, null);
    }
    
    public static void showError(String title, String message, Runnable afterClose){
        ErrorMessage msg = new ErrorMessage(title);
        msg.setMessage(message);
        if(afterClose!=null){
            PopUpButton button = new PopUpButton("close", "Close");
            button.setOnAction((ActionEvent e) -> {
                msg.close();
                afterClose.run();
            });
            msg.setButtons(button);
        } else {
            msg.setButtons();
        }
        msg.build();
        msg.show();
    }
    
    public static void openPopUp(PopUp pop){
        openPopUp(pop, false);
    }
    
    public static void openPopUp(PopUp pop, boolean unique){
        if(unique){
            containerParent.closePopUps();
        }
        containerParent.addContent(pop);
    }
    
    public static boolean hasPopUp(PopUp pop){
        return containerParent.hasContent(pop);
    }
    
    public static void closePopUp(PopUp pop){
        containerParent.removeContent(pop);
    }
    
    public static void addContent(Pane pane){
        containerParent.addContent(pane);
    }
    
    public static void removeContent(Pane pane){
        containerParent.removeContent(pane);
    }
    
    public static void snapShot(){
        try {
            PngEncoderFX encoder = new PngEncoderFX(scene.snapshot(null), true);
            byte[] bytes = encoder.pngEncode();
            File dir = new File("screenshots/");
            if(!dir.exists()){
                dir.mkdir();
            }
            String screenShotName = new StringBuilder("screenshots/screenshot_").append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())).append(".png").toString();
            FileOutputStream fos = new FileOutputStream(screenShotName);
            fos.write(bytes);
            fos.close();
            
            showScreenShotResult("File saved to: " + screenShotName);
            
        } catch (IOException ex) {
            Logger.getLogger(Themes.class.getName()).log(Level.SEVERE, null, ex);
            showScreenShotResult("Could not take/save screenshot: " + ex.getMessage());
        }
    }
    
    private static void showScreenShotResult(String message){
        SimpleDialog dialog = new SimpleDialog(MaterialDesignIcon.IMAGE_AREA_CLOSE, "Screenshot");
        dialog.setButtons();
        StackPane content = new StackPane();
        content.setMinSize(400, 100);
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        StackPane.setAlignment(messageLabel, Pos.CENTER);
        content.getChildren().add(messageLabel);
        dialog.setContent(content);
        dialog.build();
        dialog.show();
    }
    
    public static Themes getThemes(){
        return themes;
    }
    
}