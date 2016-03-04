/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes.login;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.pidome.client.phone.scenes.BaseScene;
import org.pidome.client.phone.scenes.ScenesSwitcher;
import org.pidome.client.phone.scenes.visuals.DialogBox;
import org.pidome.client.phone.scenes.visuals.DialogBox.PopUpButton;
import org.pidome.client.phone.scenes.visuals.DialogBoxActionListener;
import org.pidome.client.phone.services.ServiceConnector;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClient;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public class LoginScene extends BaseScene implements DialogBoxActionListener {

    /**
     * Loader image, animated while loading or logging in.
     */
    private final ImageView loaderImage;

    /**
     * Animation used for loading.
     */
    private final FadeTransition loaderAnimation;

    /**
     * Label showing progress info.
     */
    final Label loadingInfo;

    private final ConnectionListener connectionListener = new ConnectionListener();
    private final ClientListener clientListener = new ClientListener();

    StackPane automatic;
    StackPane manual;
    
    private StackPane content;
    
    DialogBox popit;
    
    InternalLoginConnection loginConnection;
    
    HBox buttonSet;
    
    public LoginScene() throws Exception {
        super(false, true);
        content = FXMLLoader.load(getClass().getResource("/display/fxml/Login.fxml"));
        loaderImage = (ImageView)content.lookup("#loginLoaderIcon");
        automatic = (StackPane)content.lookup("#automaticbutton");
        manual = (StackPane)content.lookup("#manualbutton");
        buttonSet = (HBox)content.lookup("#noconnectionbox");
        showConnectButtons(false);
        
        manual.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
            if(popit==null){
                loginConnection = new InternalLoginConnection(Profile.MOBILE);
                popit = new DialogBox("Connection settings");
                popit.setButtons(new PopUpButton[]{new PopUpButton("CANCEL", "Cancel"), new PopUpButton("OK", "Ok")});
                popit.setContent(loginConnection);
                popit.addListener(this);
                popit.build();
            }
            if(!content.getChildren().contains(popit)){
                content.getChildren().add(popit);
            }
        });
        
        automatic.addEventHandler(MouseEvent.MOUSE_CLICKED,(MouseEvent) -> {
            if(popit==null || (popit!=null && !content.getChildren().contains(popit))){
                new Thread() { 
                    @Override 
                    public final void run(){ 
                        if(getSystem().getConnection().hasInitialManualConnectData()){
                            getSystem().getConnection().startInitialConnection();
                        } else {
                            getSystem().getConnection().startSearch();
                        }
                    }
                }.start();
            }
        });
        
        loaderAnimation = new FadeTransition(Duration.millis(500), loaderImage);
        loaderAnimation.setFromValue(1.0);
        loaderAnimation.setToValue(0.4);
        loaderAnimation.setAutoReverse(true);
        loaderAnimation.setCycleCount(Animation.INDEFINITE);
        loadingInfo = (Label)content.lookup("#loaderText");
    }

    @Override
    public void handleDialogAction(String id) {
        content.getChildren().remove(popit);
        switch(id){
            case "CANCEL":

            break;
            case "OK":
                String ip = loginConnection.getIpFieldContent();
                int port  = loginConnection.getPortFieldContent();
                if (ip != null && !ip.trim().isEmpty() && port!=0) {
                    showConnectButtons(false);
                    getSystem().getConnection().startManualConnection(ip, port, false);
                }
            break;
        }
    }
    
    @Override
    public final StackPane getSceneContent(){
        return content;
    }
    
    @Override
    public final void run(){
        if(getSystem()!=null){
            getSystem().getConnection().addPCCConnectionListener(connectionListener);
            getSystem().getClient().addListener(clientListener);
            if(getSystem().getConnection().getCurrentStatus() == PCCConnection.PCCConnectionStatus.CONNECTED){
                handleClientStatus(getSystem().getClient().getCurrentClientStatus(), "", 0);
            } else {
                handleConnectionStatus(getSystem().getConnection().getCurrentStatus(), "", 0);
            }
        }
    }
    
    @Override
    public final void setSystem(ScenesSwitcher switcher, PCCSystem system, ServiceConnector serviceConnector) {
        super.setSystem(switcher,system, serviceConnector);
        getSystem().getConnection().addPCCConnectionListener(connectionListener);
        getSystem().getClient().addListener(clientListener);
    }
    
    @Override
    public final void stop(){
        try {
            getSystem().getConnection().removePCCConnectionListener(connectionListener);
        } catch (Exception ex){
            /// there is no binding
        }
        try {
            getSystem().getClient().removeListener(clientListener);
        } catch (Exception ex){
            /// there is no binding
        }
    }

    public final void animateLoaderImage() {
        if (loaderAnimation.getStatus() != Animation.Status.RUNNING) {
            Platform.runLater(() -> {
                loaderAnimation.play();
            });
        }
    }

    public final void stopAnimatingLoaderImage() {
        if (loaderAnimation.getStatus() == Animation.Status.RUNNING) {
            Platform.runLater(() -> {
                loaderAnimation.stop();
                loaderImage.setOpacity(1.0f);
            });
        }
    }

    public final void setLoadIngInfo(final String info) {
        Platform.runLater(() -> {
            loadingInfo.setText(info);
        });
    }

    private void showConnectButtons(boolean view){
        buttonSet.setVisible(view);
    }
    
    private void handleConnectionStatus(PCCConnection.PCCConnectionStatus status, String extra, int intExtra) {
        switch (status) {
            case SEARCHING:
                showConnectButtons(false);
                animateLoaderImage();
                setLoadIngInfo("Searching server, takes 10 seconds max.");
                break;
            case NOT_FOUND:
                showConnectButtons(true);
                if (getSystem().getConnection().getConnectionProfile() == Profile.FIXED) {
                    setLoadIngInfo("Server not found, if using IPv6 or server broadcast is disabled, connect manual with an IPv4 address.");
                } else {
                    setLoadIngInfo("Server not found, Set the data manually");
                }
                stopAnimatingLoaderImage();
                break;
            case UNAVAILABLE:
                showConnectButtons(true);
                if (getSystem().getConnection().getConnectionProfile() == Profile.FIXED) {
                    setLoadIngInfo("Not finding a method to connect, make sure network is available.");
                } else {
                    setLoadIngInfo("There seems no connection available.");
                }
                stopAnimatingLoaderImage();
                break;
            case FOUND:
                setLoadIngInfo("Server found");
                break;
            case CONNECTING:
                animateLoaderImage();
                setLoadIngInfo("Connecting");
                break;
            case CONNECTED:
                setLoadIngInfo("Connected");
                break;
            case DISCONNECTED:
                showConnectButtons(true);
                setLoadIngInfo("Disconnected");
                stopAnimatingLoaderImage();
                break;
            case CONNECT_FAILED:
                showConnectButtons(true);
                setLoadIngInfo("Connection failed");
                stopAnimatingLoaderImage();
                break;
        }
    }

    private void handleClientStatus(PCCClient.ClientStatus status, String message, int code){
        switch (status) {
            case BUSY_LOGIN:
                animateLoaderImage();
                setLoadIngInfo("Logging in");
                break;
            case LOGGED_IN:
                setLoadIngInfo("Logged in");
                break;
            case LOGGED_OUT:
                setLoadIngInfo("Logged out");
                stopAnimatingLoaderImage();
                break;
            case BUSY_LOGOUT:
                animateLoaderImage();
                setLoadIngInfo("Busy logging out");
                break;
            case AUTHORIZATION_NEEDED:
                setLoadIngInfo("This client is not authorized to connect. To authorize, log in on the server, when authorized the client will log in.");
                stopAnimatingLoaderImage();
            break;
            case FAILED_LOGIN:
                showConnectButtons(true);
                String addendum = "";
                switch (code) {
                    case 401:
                        addendum = " If name taken and this is the correct client reset it on the server and try again.";
                    break;
                    default:
                        addendum = "Unknown error " + ((code!=0)?"("+code+")":"");
                    break;
                }
                setLoadIngInfo("Login failed " + message + " (" + message + addendum + ")");
                stopAnimatingLoaderImage();
                break;
            case NO_DATA:
                showConnectButtons(true);
                setLoadIngInfo("There is no login data known, please provide.");
                stopAnimatingLoaderImage();
                break;
            case INIT_DONE:
                /**
                 * All initialization is done, we are ready to go.
                 */
                setLoadIngInfo("Init done");
                stopAnimatingLoaderImage();
                break;
            case INIT_ERROR:
                showConnectButtons(true);
                setLoadIngInfo("Unable to handle server provided resources. It is an unrecoverable error, you could try again");
                stopAnimatingLoaderImage();
                break;
        }
    }

    private class ConnectionListener implements PCCConnectionListener {

        /**
         * Handles connection events.
         *
         * @param status The connection status event.
         * @param event Event is null when no server information is available.
         * This is mainly when for example server search fails.
         */
        @Override
        public final void handlePCCConnectionEvent(PCCConnection.PCCConnectionStatus status, PCCConnectionEvent event) {
            handleConnectionStatus(status, event.getRemoteSocketAddress(), event.getRemoteSocketPort());
        }
    }

    private class ClientListener implements PCCCLientStatusListener {

        /**
         * Handles events that have to do with the client. These events have to
         * do with plain client stuff.
         *
         * @param event The PCCClientEvent to handle.
         */
        @Override
        public final void handlePCCClientEvent(PCCClientEvent event) {
            handleClientStatus(event.getStatus(), event.getMessage(), event.getErrorCode());
        }
    }
}
