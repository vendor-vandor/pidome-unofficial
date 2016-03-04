/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.login;

import de.jensd.fx.glyphs.GlyphIcons;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.pidome.client.scenes.panes.popups.PopUp;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public abstract class LoginPopUp extends PopUp {
    
    /**
     * Listener for connection data.
     */
    protected final PCCConnectionListener connectionListener = this::handlePCCConnectionEvent;
    /**
     * Listener for the client's status.
     * This is only applicable when an connection is made to the server.
     */
    protected final PCCCLientStatusListener clientStatusListener = this::handlePCCClientEvent;

    /**
     * The pcc system
     */
    protected PCCSystem system;
    
    /**
     * The service connector for platform specific functions.
     */
    protected ServiceConnector connector;

    /**
     * The content form's content.
     */
    VBox content = new VBox();
    
    /**
     * Constructor.
     * @param icon
     * @param title 
     */
    public LoginPopUp(GlyphIcons icon, String title) {
        super(icon, title);
        this.setContent(content);
    }
    
    protected final void setSystem(PCCSystem system, ServiceConnector connector){
        this.system = system;
        this.connector = connector;
        this.system.getConnection().addPCCConnectionListener(connectionListener);
        this.system.getClient().addListener(clientStatusListener);
        content.getChildren().add(loginForm());
    }
    
    protected abstract GridPane loginForm();
    
    protected abstract void start();
    
    protected void stop(){
        this.system.getConnection().removePCCConnectionListener(connectionListener);
        this.system.getClient().removeListener(clientStatusListener);
        this.system = null;
    }
    
    @Override
    public final void unload(){
        this.system.getConnection().removePCCConnectionListener(connectionListener);
        this.system.getClient().removeListener(clientStatusListener);
    }
    
    protected abstract void handlePCCConnectionEvent(PCCConnection.PCCConnectionStatus status, PCCConnectionEvent event);
    
    protected abstract void handlePCCClientEvent(PCCClientEvent event);
    
}