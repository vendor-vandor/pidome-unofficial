/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone;

import java.io.IOException;
import java.io.OutputStream;
import org.pidome.client.phone.services.ServiceConnectorListener;
import org.pidome.client.phone.services.PlatformService;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.pidome.client.phone.scenes.ScenesHandler;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class PhoneMain extends Application implements ServiceConnectorListener {

    private Stage mainStage;
    private ScenesHandler handler;
    
    private PCCSystem system;
    
    private PlatformService platformService;

    private class DevNull extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            /// Bye bye
        }
        
    }
    
    @Override
    public void init() {
        ///System.setOut(new PrintStream(new DevNull()));
        platformService = PlatformService.getInstance();
        if(Font.loadFont(getClass().getResource("/display/fonts/Quicksand_Bold.ttf").toExternalForm(), 13)==null){
            System.out.println("Quicksand_Bold.ttf font unsupported");
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.mainStage = primaryStage;
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
        if(!system.getClient().isloggedIn()){
            handler.switchScene(ScenesHandler.ScenePane.LOGIN);
        } else {
            handler.switchScene(ScenesHandler.ScenePane.DASHBOARD);
        }
    }
    
    @Override
    public void handleUserLoggedIn() {
        if(this.system!=null){
            handler.switchScene(ScenesHandler.ScenePane.DASHBOARD);
        }
    }

    @Override
    public void handleUserLoggedOut() {
        handler.switchScene(ScenesHandler.ScenePane.LOGIN);
    }

    @Override
    public void serviceDisconnectedConnected() {
        /// Lost the connection to the service, we can't do anything anymore.
        handler.switchScene(ScenesHandler.ScenePane.LOGIN);
    }

}