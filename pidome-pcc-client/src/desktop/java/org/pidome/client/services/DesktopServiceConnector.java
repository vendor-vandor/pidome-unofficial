/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.services;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Screen;
import org.pidome.client.settings.LocalizationInfoInterface;
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
public final class DesktopServiceConnector implements ServiceConnector {

    private static PCCSystem system;
    ServiceConnectorListener listener;
    
    private final ConnectionListener    connectionListener = new ConnectionListener();
    private final ClientListener        clientListener     = new ClientListener();
    private final DummyLifeCycleHandler handler            = new DummyLifeCycleHandler();
    
    private final LocalPreferenceStorage prefStorage       = new LocalPreferenceStorage(LocalPathResolver.getLocalBasePath());
    private final LocalSettingsStorage   setStorage        = new LocalSettingsStorage(LocalPathResolver.getLocalBasePath());
    
    private double maxWorkWidth = 0;
    
    DisplayType displayType = DisplayType.LARGE;
    
    private boolean dispInit = false;
    
    private PhysicalDisplayInterface displayController;
    
    public DesktopServiceConnector(){
        try {
            displayController = new PhysicalDisplay(this);
            displayController.init();
        } catch (java.lang.UnsatisfiedLinkError ex){
            /// Not supported on the platform. Currently only pi.
            displayController = new UnsupportedPhysicalDisplayInterface(this);
        }
    }
    
    @Override
    public void startService() {
        system = new PCCSystem(prefStorage, 
                               setStorage, 
                               ServerConnection.Profile.FIXED, 
                               new NetInterface());
        this.listener.serviceConnected(system);
        system.getConnection().addPCCConnectionListener(connectionListener);
        system.getClient().addListener(clientListener);
    }

    @Override
    public PlatformBase getPlatformBase() {
        return PlatformBase.FIXED;
    }
    
    public LocalPreferenceStorage getPreferences() {
        return prefStorage;
    }
    
    public LocalSettingsStorage getSettings() {
        return setStorage;
    }
    
    @Override
    public void stopService() {
        system.getClient().logout();
    }

    @Override
    public void setServiceConnectionListener(ServiceConnectorListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void serviceLogin(){
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
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DisplayType userDisplayType() {
        if(!dispInit){
            setDisplayStuff();
        }
        return displayType;
    }

    @Override
    public final void storeUserDisplayType(DisplayType type){
        ///not needed.
    }
    
    /**
     * Only available for tiny displays.
     * return Double.MAX_VALUE when large.
     */
    @Override
    public final double getMaxWorkWidth(){
        if(!dispInit){
            setDisplayStuff();
        }
        return maxWorkWidth;
    }
    
    private void setDisplayStuff(){
        double width = Screen.getPrimary().getVisualBounds().getWidth();
        double height = Screen.getPrimary().getVisualBounds().getHeight();
        if(width <= 240){
            maxWorkWidth = 170;
            displayType = DisplayType.TINY;
        } else if(width <= 320){
            maxWorkWidth = 240;
            displayType = DisplayType.TINY;
        } else if(width <= 480){
            maxWorkWidth = 320;
            displayType = DisplayType.TINY;
        } else if (width < 800 && height < 1024){
            maxWorkWidth = 780;
            displayType = DisplayType.SMALL;
        } else if(width < 1024 && height < 800){ 
            maxWorkWidth = 1000;
            displayType = DisplayType.SMALL;
        }  else {
            maxWorkWidth = Double.MAX_VALUE;
        }
        dispInit = true;
    }
    
    @Override
    public void addOrientationListener(PlatformOrientation listener) {
        /// not used on this platform.
    }

    @Override
    public void forceOrientation(PlatformOrientation.Orientation orientation) {
        /// not used on this platform.
    }

    @Override
    public PhysicalDisplayInterface getPhysicalDisplayInterface() {
        return this.displayController;
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
                //case CONNECTED:
                //    system.getClient().loginFixed("client", "client123");
                //break;
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