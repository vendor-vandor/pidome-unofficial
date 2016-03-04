/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.phone.dialogs.settings.LocalizationInfoInterface;
import org.pidome.client.system.PCCCLientStatusListener;
import org.pidome.client.system.PCCClientEvent;
import org.pidome.client.system.PCCConnection;
import org.pidome.client.system.PCCConnectionEvent;
import org.pidome.client.system.PCCConnectionListener;
import org.pidome.client.system.PCCSystem;
import org.pidome.pcl.networking.connections.server.ServerConnection;
import org.pidome.pcl.networking.interfaces.NetInterface;
import org.pidome.pcl.storage.preferences.LocalPreferenceStorage;
import org.pidome.pcl.storage.settings.LocalSettingsStorage;

/**
 *
 * @author John
 */
public class DesktopServiceConnector implements ServiceConnector {

    private static PCCSystem system;
    ServiceConnectorListener listener;
    
    private final ConnectionListener    connectionListener = new ConnectionListener();
    private final ClientListener        clientListener     = new ClientListener();
    private final DummyLifeCycleHandler handler            = new DummyLifeCycleHandler();
    
    @Override
    public void startService() {
        system = new PCCSystem(new LocalPreferenceStorage(LocalPathResolver.getLocalBasePath()), 
                               new LocalSettingsStorage(LocalPathResolver.getLocalBasePath()), 
                               ServerConnection.Profile.FIXED, 
                               new NetInterface());
        this.listener.serviceConnected(system);
        system.getConnection().addPCCConnectionListener(connectionListener);
        system.getClient().addListener(clientListener);
        serviceLogin();
    }

    @Override
    public void stopService() {
        system.getClient().logout();
    }

    @Override
    public void setServiceConnectionListener(ServiceConnectorListener listener) {
        this.listener = listener;
    }
    
    private void serviceLogin(){
        new Thread() { 
            @Override 
            public final void run(){ 
                if(!system.getClient().isloggedIn()){
                    if(system.getConnection().hasInitialManualConnectData()){
                        system.getConnection().startInitialConnection();
                    } else {
                        system.getConnection().startSearch();
                    }
                }
            } 
        }.start();
    }

    @Override
    public LifeCycleHandlerInterface getLifeCycleHandler() {
        return handler;
    }

    @Override
    public LocalizationInfoInterface getLocalizationService() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            Logger.getLogger(DesktopServiceConnector.class.getName()).log(Level.INFO, "Received connection event: " + status.toString());
            switch (status) {
                case CONNECTED:
                    system.getClient().loginFixed("client22", "client22");
                break;
                case DISCONNECTED:
                    if(listener!=null){
                        listener.handleUserLoggedOut();
                    }
                break;
            }
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
            Logger.getLogger(DesktopServiceConnector.class.getName()).log(Level.INFO, "Received client event: " + event.getStatus().toString());
            switch (event.getStatus()) {
                case LOGGED_IN:
                    try {
                        system.getLocalSettings().storeSettings();
                    } catch (IOException ex) {
                        Logger.getLogger(DesktopServiceConnector.class.getName()).log(Level.SEVERE, "Could not store connection settings", ex);
                    }
                break;
                case INIT_DONE:
                    if(listener!=null){
                        listener.handleUserLoggedIn();
                    }
                break;
            }
        }
    }
    
}