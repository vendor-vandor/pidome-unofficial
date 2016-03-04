/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.demo;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * This is a demo class used to demonstrate how to connect to the PiDome Server.
 * 
 * To make a connection you initially do not have to do anything but search for
 * the server. When the server is found it automatically also determines a secure
 * or non secure connection. If the server ain't found you will need to provide
 * an interface where the user can enter ip,socket port and http port and if the
 * connection is secure.
 * 
 * It is possible for an end user to put their credentials in a preferences file. if
 * this is the case you also have not to worry about login routines. There are
 * circumstances where login fails, you will need to provide a login possibility.
 * 
 * This demo relies on the server having broadcast enabled, but no user data present
 * in a configuration file. When an user successfully logs in the credentials and
 * server data is automatically saved.
 * 
 * @author John
 */
public class ConnectionDemo {
    
    /**
     * The whole system at your fingertips.
     */
    private static PCCSystem system;
    /**
     * Listener for connection data.
     */
    private static final PCCConnectionListener connectionListener = ConnectionDemo::handlePCCConnectionEvent;
    
    /**
     * Listener for the client's status.
     * This is only applicable when an connection is made to the server.
     */
    private static final PCCCLientStatusListener clientStatusListener = ConnectionDemo::handlePCCClientEvent;
    
    /**
     * Start the app.
     * @param args Application arguments.
     */
    public static void main(String[] args){
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
        consoleHandler.setLevel(Level.ALL);
        /**
         * Create a local settings provider.
         */
        LocalSettingsStorage settings = new LocalSettingsStorage(LocalPathResolver.getLocalBasePath());
        /**
         * Create a local preferences provider.
         */
        LocalPreferenceStorage prefs  = new LocalPreferenceStorage(LocalPathResolver.getLocalBasePath());
        
        /**
         * Set the PCC system with the local network interface (non mobile).
         */
        system = new PCCSystem(prefs, settings, ServerConnection.Profile.MOBILE, new NetInterface());
        
        /**
         * Register the listener for connection events.
         */
        system.getConnection().addPCCConnectionListener(connectionListener);
        
        /**
         * Start for searching the server.
         */
        system.getConnection().startSearch();
    }
    
    /**
     * Handles connection events.
     * @param status The connection status event.
     * @param event Event is null when no server information is available. This is mainly when for example server search fails.
     */
    private static void handlePCCConnectionEvent(PCCConnection.PCCConnectionStatus status, PCCConnectionEvent event){
        switch(status){
            case SEARCHING:
                System.out.println("Started server search.");
            break;
            case NOT_FOUND:
                System.out.println("Server not found, if using IPv6 or server broadcast is disabled, connect manual with an IPv4 address.");
            break;
            case UNAVAILABLE:
                System.out.println("Not finding a method to connect, make sure network is available.");
            break;
            case FOUND:
                System.out.println("Server found at: " + event.getRemoteSocketAddress()+ ", using port: " + event.getRemoteSocketPort());
            break;
            case CONNECTING:
                System.out.println("Connecting to: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort());
            break;
            case CONNECTED:
                System.out.println("Connected to: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort());
                /**
                 * When you are connected you surely want to log in.
                 * Make sure you have a fixed client created on the server first.
                 * for demo purposes use the username "client" and the password "client".
                 */
                system.getClient().addListener(clientStatusListener);
                /**
                 * Automatic login. As there is no data known an event with NO_DATA will be raised (see handlePCCClientEvent).
                 */
                system.getClient().login();
            break;
            case DISCONNECTED:
                System.out.println("Disconnected from: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort());
            break;
            case CONNECT_FAILED:
                System.out.println("Connection failed with: " + event.getRemoteSocketAddress() + ", using port: " + event.getRemoteSocketPort() + ". Check connect settings");
            break;
        }
    }
    
    /**
     * Handles events that have to do with the client.
     * These events have to do with plain client stuff.
     * @param event The PCCClientEvent to handle.
     */
    private static void handlePCCClientEvent(PCCClientEvent event) {
        switch(event.getStatus()){
            case BUSY_LOGIN:
                System.out.println("Logging in");
            break;
            case AUTHORIZATION_NEEDED:
                /**
                 * Used with the MOBILE connection profile.
                 * A mobile connection is user bound. So when this device connects
                 * it needs to be bound to an user first on the server.
                 */
                System.out.println("Authorize this client first on the server by binding it to an user. Go to Mobile connections and click the client.");
            break;
            case LOGGED_IN:
                System.out.println("Logged in");
                /**
                 * We are logged in. So we can start the preloaders.
                 */
                system.getClient().startPreloaders();
            break;
            case LOGGED_OUT:
                System.out.println("Logged out");
            break;
            case BUSY_LOGOUT:
                System.out.println("Busy logging out");
            break;
            case FAILED_LOGIN:
                String addendum = "";
                switch(event.getErrorCode()){
                    case 401:
                        addendum = " If name taken and this is the correct client reset it on the server and try again.";
                    break;
                }
                System.out.println("Login failed " + event.getMessage() + " ("+event.getErrorCode()+addendum+")");
                /**
                 * Always use the logout, even when not logged in as this does a nice cleanup.
                 */
                system.getClient().logout();
            break;
            case NO_DATA:
                System.out.println("trying to login with \"client123\" and password \"client123\", if not succeeding create them on the server.");
                system.getClient().loginFixed("client123", "client123");
            break;
            case INIT_DONE:
                /**
                 * All initialization is done, we are ready to go.
                 */
                System.out.println("Init done");
                /**
                 * In the demo we are logging out.
                 */
                system.getClient().logout();
            break;
            case INIT_ERROR:
                System.out.println("Unable to handle server provided resources. hard fail, details in log and press esc to exit");
                /**
                 * Failing intitialization. In the demo we log out.
                 */
                system.getClient().logout();
            break;
        }
    }
    
}
