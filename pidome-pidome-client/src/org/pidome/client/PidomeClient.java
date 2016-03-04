/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.comtel.javafx.control.KeyBoardPopup;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.loggers.LoggerStream;
import org.pidome.client.preloader.PidomeClientPreloader;
import org.pidome.client.preloader.PidomeClientPreloader.PreloaderCredentials;
import org.pidome.client.system.ClientSystem;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.Domotics;
import org.pidome.client.system.domotics.DomoticsEvent;
import org.pidome.client.system.domotics.DomoticsEventListener;
import org.pidome.client.system.network.Networking;
import org.pidome.client.system.network.NetworkingEvent;
import org.pidome.client.system.network.NetworkingEventListener;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.MainSceneEvent;
import org.pidome.client.system.scenes.MainSceneEventListener;
import org.pidome.client.system.threads.SystemThreads;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.AppProperties;
import org.pidome.client.config.AppPropertiesException;
import org.pidome.client.system.client.data.ClientSessionException;
import org.pidome.client.system.network.CertHandler;
import org.pidome.client.system.network.CertHandlerException;
import org.pidome.client.system.scenes.components.helpers.Osdk;
import org.pidome.client.system.time.ClientTime;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class PidomeClient extends Application implements NetworkingEventListener,ClientDataConnectionListener,DomoticsEventListener,MainSceneEventListener,PreloaderCredentials {
    
    ClientSystem client;
    MainScene mainStage;
    
    SystemThreads systemThreads = new SystemThreads();
    
    Stage rootStage; 
    
    public final static String appLogo = "/org/pidome/client/app/resources/applogo.png";
    
    static Logger LOG = LogManager.getLogger(PidomeClient.class);
    
    BooleanProperty ready = new SimpleBooleanProperty(false);
    
    PidomeClient me = this;
    
    PidomeClientPreloader preloader;
    Boolean preloaderShown = false;
    
    private KeyBoardPopup osdk;
    
    Stage widgetPreloadserStage;
    
    BooleanProperty setSettings = new SimpleBooleanProperty(true);
    
    ClientTime clientTime;
    
    @Override
    public void start(Stage primaryStage) {
        ///redirectOutputToLog();
        Locale.setDefault(Locale.ENGLISH);
        try {
            CertHandler.init();
        } catch (CertHandlerException ex) {
            LOG.error(ex.getMessage());
        }
        rootStage = primaryStage;
        try {
            AppProperties.initialize();
        } catch (IOException ex){
            throw new RuntimeException("Could not start without correct settings files", ex);
        }
        Font.loadFont(AppResources.getFont("Quicksand_Bold.otf"), DisplayConfig.getFontDpiScaler());
        Font.loadFont(AppResources.getFont("FreeSans.ttf"), DisplayConfig.getFontDpiScaler());
        Font.loadFont(AppResources.getFont("DigitalSevenMonoItalic.ttf"), DisplayConfig.getFontDpiScaler());
        clientTime = new ClientTime();
        ready.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (Boolean.TRUE.equals(t1)) {
                rootStage.setScene(mainStage.scene());
                rootStage.sizeToScene();
                rootStage.getIcons().add(new Image(appLogo));
                rootStage.setTitle("PiDome Client");
                Osdk.setStage(rootStage);
                if(!DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_WIDGET)){
                    rootStage.setFullScreen(false);
                    rootStage.setFullScreen(true);
                    /*
                    mainStage.scene().focusOwnerProperty().addListener((ObservableValue<? extends Node> value, Node n1, Node n2) -> {
                        if (n2 != null && n2 instanceof TextInputControl) {
                            Osdk.show(true, (TextInputControl) n2);
                        } else {
                            Osdk.show(false, null);
                        }
                    });
                    */
                } else {
                    rootStage.setMaxHeight(400);
                    rootStage.setMaxWidth(200);
                    rootStage.setX(DisplayConfig.getScreenWidth()-20 - 500);
                    rootStage.setY((DisplayConfig.getScreenHeight()/2) - (400/2));
                }
                Networking.removeEventListener(me);
                ClientData.removeClientDataConnectionListener(me);
                ClientData.removeClientLoggedInConnectionListener(me);
                Domotics.removeDomoticsListener(me);
                preloader = null;
                rootStage.show();
            }
        });
        Osdk.load(rootStage);
        setSettings.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (Boolean.FALSE.equals(t1)) {
                initialize();
            }
        });
        try {
            if(!AppProperties.getProperty("system", "client.mode").equals("ask") && !AppProperties.getProperty("system", "display.quality").equals("ask")){
                setSettings.setValue(Boolean.FALSE);
            }
        } catch (AppPropertiesException ex) {
            /// just ask for settings
        }
        double preloaderWidth = 711 * DisplayConfig.getWidthRatio();
        double preloaderHeight = 711 * DisplayConfig.getWidthRatio();
        widgetPreloadserStage = new Stage();
        widgetPreloadserStage.initOwner(rootStage);
        widgetPreloadserStage.initStyle(StageStyle.TRANSPARENT);
        widgetPreloadserStage.setTitle("PiDome loading");
        widgetPreloadserStage.centerOnScreen();
        widgetPreloadserStage.setWidth(preloaderWidth);
        widgetPreloadserStage.setHeight(preloaderHeight);
        widgetPreloadserStage.getIcons().add(new Image(appLogo));
        preloader = new PidomeClientPreloader(widgetPreloadserStage, setSettings);
        widgetPreloadserStage.show();
        preloaderShown = true;
    }

    public final void initialize(){
        LOG.debug("Hello!!");
        DisplayConfig.setStageProps(rootStage);
        mainStage = new MainScene();
        mainStage.setRoot(rootStage);
        Networking.addEventListener(this);
        ClientData.addClientDataConnectionListener(this);
        ClientData.addClientLoggedInConnectionListener(this);
        Domotics.addDomoticsListener(this);
        MainScene.addDoneListener(this);
        Task task = new Task<Void>() {
            @Override
            public final Void call() throws Exception {
                LOG.debug("Starting system threads");
                systemThreads.start();
                LOG.debug("Initializing client system");
                client = new ClientSystem();
                LOG.debug("Starting client system");
                client.start();
                LOG.debug("Client system routines started.");
                return null;
            }
        };
        new Thread(task).start();
    }
    
    @Override
    public void stop(){
        LOG.debug("Stopping");
        systemThreads.stop();
        if(client!=null){
            client.stopClient(true);
        }
        if(mainStage!=null){
            mainStage.stop();
        }
        LOG.debug("Bye!!");
        System.exit(0);
    }
    
    Text getTitle( int titleSize ){
        Text t = new Text();
        t.setCache(true);
        t.setText("PiDome");
        t.setFill(Color.BLACK);
        Reflection r = new Reflection();
        r.setFraction(0.7f);
        t.setEffect(r);
        t.setTranslateY(40);
        return t;
    }
    
    
    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void handleDomoticsEvent(DomoticsEvent event) {
        switch(event.getEventType()){
            case (DomoticsEvent.INITDATARECEIVED):////5
                preloader.handleApplicationProgress(15.0);
                Runnable build = () -> {
                    try {
                        if(AppProperties.getProperty("system", "client.mode").equals("widget")){
                            mainStage.createWidgetScene();                            
                        } else {
                            mainStage.createScene();
                        }
                    } catch (AppPropertiesException ex) {
                        java.util.logging.Logger.getLogger(PidomeClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }
                };
                Platform.runLater(build);
            break;
            default:
                /// nothing to do with that.
            break;
        }
    }

    @Override
    public void handleNetworkingEvent(NetworkingEvent event) {
        LOG.info("Got network event: {}", event.getEventType());
        switch(event.getEventType()){
            case NetworkingEvent.NETWORKAVAILABLE: ///1
            case NetworkingEvent.BROADCASTRECEIVED:///2
                preloader.handleApplicationProgress(15.0);
            break;
            case NetworkingEvent.BROADCASTDISABLED:
                boolean doCustom = false;
                try {
                    if(!AppProperties.getProperty("system", "TELNETADDRESS").equals("") &&
                            !AppProperties.getProperty("system", "TELNETPORT").equals("") &&
                            !AppProperties.getProperty("system", "STREAMSSL").equals("")){
                        doCustom = true;
                        LOG.info("Autoconnect with: {}, {}, {}",AppProperties.getProperty("system", "TELNETADDRESS"),AppProperties.getProperty("system", "TELNETPORT"),AppProperties.getProperty("system", "STREAMSSL"));
                        ClientData.goCustomConnect(AppProperties.getProperty("system", "TELNETADDRESS"),
                                Integer.parseInt(AppProperties.getProperty("system", "TELNETPORT")),
                                Boolean.parseBoolean(AppProperties.getProperty("system", "STREAMSSL")));
                    }
                } catch (Exception ex) {
                    LOG.error("Could not use predefined values: {}", ex.getMessage());
                }
                if(!doCustom){
                    ClientDataConnectionEvent connectProblem = new ClientDataConnectionEvent(ClientDataConnectionEvent.NOSERVERFOUND);
                    connectProblem.setClientData(ClientData.getLoginName(), -1, "Server not found in discovery.");
                    preloader.handleLoginErrorEvent(connectProblem.getLoginName(),connectProblem.getLoginFailureReason(), connectProblem.getLoginFailureReasonMessage());                    
                }
            break;
            default:
                /// nothing to do with that
            break;
        }
    }

    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        LOG.info("Got connection event: {}", event.getEventType());
        switch(event.getEventType()){
            case ClientDataConnectionEvent.CONNECTED:///3
            case ClientDataConnectionEvent.LOGGEDIN:////4
                preloader.handleApplicationProgress(15.0);
            break;
            case ClientDataConnectionEvent.LOGINFAILURE:////4
                preloader.handleLoginErrorEvent(event.getLoginName(),event.getLoginFailureReason(), event.getLoginFailureReasonMessage());
            break;
            case ClientDataConnectionEvent.CONNECTERROR:////4
                preloader.handleLoginErrorEvent(event.getLoginName(),-2, (String)event.getData());
            break;
            default:
                /// nothing to do with that
            break;
        }
    }

    @Override
    public void handleMainSceneEvent(MainSceneEvent event) {
        if(event.getEventType().equals(MainSceneEvent.SCENEBUILDDONE)){
            preloader.handleApplicationProgress(100.0);
            widgetPreloadserStage.close();
            preloader.removeFromStage();
            widgetPreloadserStage = null;
            ready.setValue(Boolean.TRUE);
        }
    }

    public final KeyBoardPopup getOsdk(){
        return osdk;
    }
    
    @Override
    public void setClientName(String clientName, String password) {
        try {
            ClientData.login(clientName, password);
        } catch (ClientSessionException ex) {
            LOG.error("Could not set client name: {}", ex.getMessage());
        }
    }

    @Override
    public void setServerCredentials(String serverIp, String serverPort) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    static void redirectOutputToLog(){
        setOutToLog();
        setErrToLog();
    }
    
    static void setOutToLog(){
        System.setOut(new PrintStream(new LoggerStream(LogManager.getLogger("out"), Level.TRACE, System.out)));
    }

    static void setErrToLog(){
        System.setErr(new PrintStream(new LoggerStream(LogManager.getLogger("err"), Level.ERROR, System.err)));
    }
    
}
