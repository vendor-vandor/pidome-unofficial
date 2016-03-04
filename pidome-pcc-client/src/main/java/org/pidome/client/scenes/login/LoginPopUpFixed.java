/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.login;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.system.ClientCapabilities;
import org.pidome.client.system.PCCClient;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.tools.SimpleTools;

/**
 *
 * @author John
 */
public class LoginPopUpFixed extends LoginPopUp {
    
    Label remoteHostLabel = new Label("Server");
    TextField remoteHost = new TextField();
    
    Label remotePortLabel = new Label("Port");
    TextField remotePort = new TextField();
    
    Label remoteUserLabel = new Label("Client name");
    TextField remoteUser = new TextField();
    
    Label remotePassLabel = new Label("Password");
    PasswordField remotePass = new PasswordField();
    
    Label remoteSecureLabel = new Label("Secure");
    CheckBox remoteSecure   = new CheckBox();
    
    PopUpButton loginButton = new PopUp.PopUpButton("login", "Login");
    
    GridPane form = new GridPane();
    
    private boolean manualConnect = false;
    
    Label infoText = new Label("");
    
    private SimpleBooleanProperty formEnabled    = new SimpleBooleanProperty(true);
    private SimpleBooleanProperty connectEnabled = new SimpleBooleanProperty(true);
    private SimpleBooleanProperty userEnabled    = new SimpleBooleanProperty(true);
    
    protected LoginPopUpFixed(){
        super(FontAwesomeIcon.WIFI,"Connection");
        this.setMaxWidth(370);
        this.setMaxHeight(370);
        infoText.setTextAlignment(TextAlignment.CENTER);
        //createLoading();
        formEnabled.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            Platform.runLater(() -> {
                //loading.setVisible(!newValue);
                form.setVisible(newValue);
            });
        });
        connectEnabled.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            Platform.runLater(() -> {
                remoteHost.setDisable(!newValue);
                remotePort.setDisable(!newValue);
                loginButton.setDisable(!newValue);
                if(newValue==true){
                    formEnabled.setValue(newValue);
                }
            });
        });
        userEnabled.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            Platform.runLater(() -> {
                remoteUser.setDisable(!newValue);
                remotePass.setDisable(!newValue);
                loginButton.setDisable(!newValue);
                if(newValue==true){
                    formEnabled.setValue(newValue);
                }
            });
        });
        loginButton.setOnAction((ActionEvent e) -> {
            userEnabled.setValue(Boolean.FALSE);
            connectEnabled.setValue(Boolean.FALSE);
            formEnabled.setValue(Boolean.FALSE);
            manualConnect = true;
            if(remoteHost.getText().isEmpty() || remotePort.getText().isEmpty() || !SimpleTools.isInteger(remotePort.getText())){
                ScenesHandler.showError("Input error", "Please use correct connect settings",() -> {
                    connectEnabled.setValue(Boolean.TRUE);
                    userEnabled.set(Boolean.TRUE);
                });
            } else {
                if(this.system.getConnection().getCurrentStatus()!=PCCConnection.PCCConnectionStatus.CONNECTED){
                    this.system.getConnection().startManualConnection(remoteHost.getText(), Integer.valueOf(remotePort.getText()), remoteSecure.isSelected());
                } else {
                    this.system.getClient().loginFixed(remoteUser.getText(), remotePass.getText());
                }
            }
        });
        connectEnabled.set(Boolean.FALSE);
        userEnabled.set(Boolean.FALSE);
        formEnabled.set(Boolean.FALSE);
        this.setButtons(loginButton);
        VBox.setMargin(infoText, new Insets(10));
        content.getChildren().add(infoText);
        infoText.setStyle("-fx-fill: whitesmoke;");
    }
    
    @Override
    protected GridPane loginForm(){
        form.setHgap(10);
        form.setVgap(10);
        form.setMaxWidth(USE_PREF_SIZE);
        form.setMinWidth(USE_PREF_SIZE);
        form.setMaxWidth(394);
        form.setMinWidth(394);
        infoText.setMaxWidth(374);
        GridPane.setHgrow(remoteHost, Priority.ALWAYS);
        GridPane.setHgrow(remotePort, Priority.ALWAYS);
        GridPane.setHgrow(remoteUser, Priority.ALWAYS);
        GridPane.setHgrow(remotePass, Priority.ALWAYS);
        form.setPadding(new Insets(12, 12, 12, 12));
        form.add(remoteHostLabel, 0, 0);
        form.add(remoteHost, 1, 0);
        form.add(remotePortLabel, 0, 1);
        form.add(remotePort, 1, 1);
        form.add(remoteUserLabel, 0, 2);
        form.add(remoteUser, 1, 2);
        form.add(remotePassLabel, 0, 3);
        form.add(remotePass, 1, 3);
        form.add(remoteSecureLabel, 0, 4);
        form.add(remoteSecure, 1, 4);
        return form;
    }
    
    @Override
    protected final void start(){
        if(this.system.getConnection().getCurrentStatus()==PCCConnection.PCCConnectionStatus.CONNECTED){
            handlePCCClientEventStuff(this.system.getClient().getCurrentClientStatus(), null);
        } else {
            if(this.system.getConnection().getConnectionData()!=null){
                handleConnectionStuff(this.system.getConnection().getCurrentStatus(), this.system.getConnection().getConnectionData().getHost(), this.system.getConnection().getConnectionData().getSocketPort());
            } else {
                this.connector.serviceLogin();
            }
        }
    }
    
    @Override
    protected final void stop(){
        super.stop();
        form.prefWidthProperty().unbind();
    }
    
    private void handleConnectionStuff(PCCConnection.PCCConnectionStatus status, String socketAddress, int socketPort){
        switch(status){
            case SEARCHING:
                connectEnabled.setValue(Boolean.FALSE);
                userEnabled.set(Boolean.FALSE);
                setInfoText("Started server search.");
            break;
            case NOT_FOUND:
                connectEnabled.setValue(Boolean.TRUE);
                userEnabled.set(Boolean.TRUE);
                setInfoText("Server not found, if using IPv6 or server broadcast is disabled, connect manual with an IPv4 address.");
            break;
            case UNAVAILABLE:
                connectEnabled.setValue(Boolean.TRUE);
                setInfoText("Not finding a method to connect, make sure network is available.");
            break;
            case FOUND:
                Platform.runLater(() -> { 
                    remoteHost.setText(socketAddress);
                    remotePort.setText(String.valueOf(socketPort));
                });
            break;
            case CONNECTING:
                connectEnabled.setValue(Boolean.FALSE);
                setInfoText("Connecting");
            break;
            case CONNECTED:
                setInfoText("Connected");
                Platform.runLater(() -> { 
                    remoteHost.setText(socketAddress);
                    remotePort.setText(String.valueOf(socketPort));
                });
                if(manualConnect){
                    this.system.getClient().loginFixed(remoteUser.getText(), remotePass.getText());
                } else {
                    this.system.getClient().login();
                }
            break;
            case DISCONNECTED:
                connectEnabled.setValue(Boolean.TRUE);
                userEnabled.set(Boolean.TRUE);
                setInfoText("Disconnected");
            break;
            case CONNECT_FAILED:
                connectEnabled.setValue(Boolean.TRUE);
                userEnabled.set(Boolean.TRUE);
                setInfoText("Connection failed");
            break;
        }
    }
    
    private void setInfoText(String text){
        Platform.runLater(() -> {
            infoText.setText(text);
        });
    }
    
    /**
     * Handles connection events.
     * @param status The connection status event.
     * @param event Event is null when no server information is available. This is mainly when for example server search fails.
     */
    @Override
    protected void handlePCCConnectionEvent(PCCConnection.PCCConnectionStatus status, PCCConnectionEvent event){
        handleConnectionStuff(status, event.getRemoteSocketAddress(), event.getRemoteSocketPort());
    }
    
    private void handlePCCClientEventStuff(PCCClient.ClientStatus status, PCCClientEvent event){
        switch(status){
            case BUSY_LOGIN:
                userEnabled.set(Boolean.FALSE);
                setInfoText("Logging in.\n");
            break;
            case AUTHORIZATION_NEEDED:
                /**
                 * Used with the MOBILE connection profile.
                 * A mobile connection is user bound. So when this device connects
                 * it needs to be bound to an user first on the server.
                 */
            break;
            case LOGGED_IN:
                setInfoText("Logged in\n");
                /**
                 * We are logged in. So we can start the preloaders.
                 */
                ClientCapabilities capabs = new ClientCapabilities();
                capabs.setDisplayDimensions(Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight());
                this.system.getClient().sendCapabilities(capabs);
                this.system.getClient().startPreloaders();
            break;
            case LOGGED_OUT:
                userEnabled.setValue(Boolean.TRUE);
                setInfoText("Logged out");
            break;
            case BUSY_LOGOUT:
                /// bye bye.
            break;
            case FAILED_LOGIN:
                userEnabled.setValue(Boolean.TRUE);
                if(event!=null){
                    String addendum = "";
                    switch(event.getErrorCode()){
                        case 401:
                            addendum = " If name taken and this is the correct client reset it on the server and try again.";
                        break;
                    }
                    setInfoText("Login failed " + event.getMessage() + " ("+event.getErrorCode()+addendum+")");
                } else {
                    setInfoText("Login failed");
                }
            break;
            case NO_DATA:
                userEnabled.setValue(Boolean.TRUE);
                setInfoText("Please supply username and password.");
            break;
            case INIT_DONE:
                /**
                 * All initialization is done, we are ready to go.
                 */
                ///ScenesHandler.switchScene();
            break;
            case INIT_ERROR:
                setInfoText("Unable to handle server provided resources. Please report with server log.");
                /**
                 * Failing intitialization. In the demo we log out.
                 */
                this.system.getClient().logout();
            break;
        }
    }
    
    /**
     * Handles events that have to do with the client.
     * These events have to do with plain client stuff.
     * @param event The PCCClientEvent to handle.
     */
    @Override
    protected void handlePCCClientEvent(PCCClientEvent event) {
        handlePCCClientEventStuff(event.getStatus(), event);
    }
    
    
    
}