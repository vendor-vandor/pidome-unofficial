package org.pidome.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.services.PlatformOrientation;
import org.pidome.client.services.PlatformService;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.services.ServiceConnector.PlatformBase;
import org.pidome.client.services.ServiceConnectorListener;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;

public class PiDomeClient extends Application implements ServiceConnectorListener {

    private Stage mainStage;
    private static ScenesHandler handler;
    
    private static PCCSystem system;
    
    private PlatformService platformService;

    private static boolean pureDashboard = false;
    
    private class DevNull extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            /// Bye bye
        }
        
    }
    
    @Override
    public void init() {
        Logger topLogger = java.util.logging.Logger.getLogger("");
        Handler consoleHandler = null;
        for (Handler handler : topLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                //found the console handler
                consoleHandler = handler;
                break;
            }
        }
        if (consoleHandler == null) {
            //there was no console handler found, create a new one
            consoleHandler = new ConsoleHandler();
            topLogger.addHandler(consoleHandler);
        }
        consoleHandler.setLevel(Level.OFF);
        platformService = PlatformService.getInstance();
        if(platformService.getServiceConnector().getPlatformBase()==PlatformBase.FIXED){
            setTouchComponents();
        }
        if(platformService.getServiceConnector().getPlatformBase()!=PlatformBase.FIXED){
            DisplayTools.setUserDisplayType(platformService.getServiceConnector().userDisplayType());
            if(DisplayTools.getUserDisplayType() != DisplayType.DUNNO){
                if(DisplayTools.getUserDisplayType() == DisplayType.LARGE){
                    platformService.getServiceConnector().forceOrientation(PlatformOrientation.Orientation.LANDSCAPE);
                } else {
                    platformService.getServiceConnector().forceOrientation(PlatformOrientation.Orientation.PORTRAIT);
                }
            } else {
                platformService.getServiceConnector().forceOrientation(PlatformOrientation.Orientation.PORTRAIT);
            }
        }
    }
    
    /**
     * When running on embedded this function will try to figure out any touch components.
     */
    private void setTouchComponents(){
        if(System.getProperty("com.sun.javafx.isEmbedded")!=null && System.getProperty("com.sun.javafx.isEmbedded").equals("true")){
            if(Platform.isSupported(ConditionalFeature.INPUT_TOUCH)){
                try {
                    String touchDevice = platformService.getServiceConnector().getPreferences().getStringPreference("org.pidome.touchdevice", "");

                    String maxX = platformService.getServiceConnector().getPreferences().getStringPreference("org.pidome.touchdevice.maxX", "");
                    String maxY = platformService.getServiceConnector().getPreferences().getStringPreference("org.pidome.touchdevice.maxY", "");

                    if(!touchDevice.isEmpty() && !maxX.isEmpty() && !maxY.isEmpty()){
                    
                        System.setProperty("monocle.input."+touchDevice+".maxX", maxX);
                        System.setProperty("monocle.input."+touchDevice+".maxY", maxY);

                        platformService.getServiceConnector().getPreferences().setStringPreference("org.pidome.touchdevice", touchDevice);
                        platformService.getServiceConnector().getPreferences().setStringPreference("org.pidome.touchdevice.maxX", maxX);
                        platformService.getServiceConnector().getPreferences().setStringPreference("org.pidome.touchdevice.maxY", maxY);
                        
                    }
                        
                    try {
                        platformService.getServiceConnector().getPreferences().storePreferences();
                    } catch (IOException ex) {
                        Logger.getLogger(PiDomeClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (Exception ex){
                    Logger.getLogger(PiDomeClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static boolean asPureDashboard(){
        return pureDashboard;
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        if(platformService.getServiceConnector().getPlatformBase()==PlatformBase.FIXED){
            DisplayTools.setUserDisplayType(platformService.getServiceConnector().userDisplayType());
        }
        Map<String,String> parameters = getParameters().getNamed();
        pureDashboard = (parameters.containsKey("puredashboard") && parameters.get("puredashboard").equals("true"));
        this.mainStage = primaryStage;
        if(ScenesHandler.isWindows()){
            this.mainStage.initStyle(StageStyle.UNDECORATED);
            this.mainStage.setResizable(false);
        }
        if(handler == null){
            handler = new ScenesHandler(this.mainStage);
        }
        platformService.getServiceConnector().setServiceConnectionListener(this);
        platformService.getServiceConnector().startService();
    }

    @Override
    public void stop(){
        platformService.getServiceConnector().stopService();
        System.exit(0);
    }
    
    @Override
    public void serviceConnected(PCCSystem system) {
        this.system = system;
        handler.setSystem(system, platformService.getServiceConnector());
        if(DisplayTools.getUserDisplayType() == DisplayType.DUNNO){
            switchScene(ScenesHandler.ScenePane.ORIENTATION_CHOICE);
        } else {
            if(!system.getClient().isloggedIn()){
                switchScene(ScenesHandler.ScenePane.LOGIN);
            } else {
                preloadLocations();
                switchScene(ScenesHandler.ScenePane.DASHBOARD);
            }
        }
    }
    
    private void preloadLocations(){
        Thread thread = new Thread(() -> {
            try {
                this.system.getClient().getEntities().getLocationService().preload();
            } catch (EntityNotAvailableException ex) {
                Logger.getLogger(PiDomeClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        thread.start();
    }
    
    @Override
    public void handleUserLoggedIn() {
        if(this.system!=null){
            switchScene(ScenesHandler.ScenePane.DASHBOARD);
            preloadLocations();
        }
    }

    @Override
    public void handleUserLoggedOut() {
        switchScene(ScenesHandler.ScenePane.LOGIN);
    }

    @Override
    public void serviceDisconnectedConnected() {
        /// Lost the connection to the service, we can't do anything anymore.
        handler.switchScene(ScenesHandler.ScenePane.LOGIN);
    }

    public static void switchScene(ScenesHandler.ScenePane scene){
        if(DisplayTools.getUserDisplayType() == DisplayType.DUNNO){
            handler.switchScene(ScenesHandler.ScenePane.ORIENTATION_CHOICE);
        } else {
            if(!system.getClient().isloggedIn()){
                handler.switchScene(ScenesHandler.ScenePane.LOGIN);
            } else {
                handler.switchScene(scene);
            }
        }
    }
    
}