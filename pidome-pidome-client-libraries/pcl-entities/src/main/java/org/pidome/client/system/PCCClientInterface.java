/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import java.util.Map;
import org.pidome.client.entities.Entities;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;

/**
 *
 * @author John
 */
public interface PCCClientInterface {
 
    /**
     * Possible client statuses.
     * Also used in events.
     */
    public enum ClientStatus {
        /**
         * A client is able to connect, but needs remote approval before it can continue.
         */
        AUTHORIZATION_NEEDED,
        /**
         * Client is logged in.
         */
        LOGGED_IN,
        /**
         * Client is logged out.
         */
        LOGGED_OUT,
        /**
         * In login routines.
         */
        BUSY_LOGIN,
        /**
         * In log out routines.
         */
        BUSY_LOGOUT,
        /**
         * Logging in failed.
         */
        FAILED_LOGIN,
        /**
         * When there is no user data known.
         */
        NO_DATA,
        /**
         * Initialization error.
         */
        INIT_ERROR,
        /**
         * Initialization done, ready to go!
         */
        INIT_DONE;
    }
    
    /**
     * Returns the entities object.
     * This object by default is uninitialized. When the user is logged in objects
     * contained by the entities will be available. When the user is logged out
     * for whatever reason all the objects are released.
     * @return all entities registered in the system.
     */
    public Entities getEntities();
    
    /**
     * Returns the client settings.
     * Returns client preferences.
     * @return The preferences for user preference data.
     */
    public LocalPreferenceStorageInterface getPreferences();
    
    /**
     * Adds a listener.
     * @param listener Listener for client statuses.
     */
    public void addListener(PCCCLientStatusListener listener);
    
    /**
     * Removes a listener.
     * @param listener Listener for client statuses.
     */
    public void removeListener(PCCCLientStatusListener listener);
    
    /**
     * Used for automated login.
     */
    public void login();
    
    /**
     * Used to log off.
     */
    public void logout();
    
    /**
     * Checks if someone is logged in.
     * @return returns true if logged in.
     */
    public boolean isloggedIn();
    
    /**
     * Returns true if authorization is needed.
     * @return truw when authorization is needed.
     */
    public boolean needsAuth();
    
    /**
     * Used for manual connection to the server.
     * @param ip The ip of the server as string.
     * @param port The socket port to be used to connect to
     * @param secure true if the port data is for a secure port.
     */
    public void manualConnect(String ip, int port, boolean secure);
    
    /**
     * Handles the client login as a fixed display.
     * @param username The fixed client login name to login with.
     * @param password The fixed client password to login with.
     */
    public void loginFixed(String username, String password);
    
    /**
     * Sends default client capabilities.
     */
    public void sendCapabilities();
    
    /**
     * Sets the new capabilities and sends them.
     * @param capabilities The capabilities to send to the server.
     */
    public void sendCapabilities(ClientCapabilities capabilities);
    
    /**
     * Handles the client login as a mobile device.
     * @param username The mobile client login name to login with. Mostly a unique device id.
     * @param userinfo The information about the client.
     */
    public void loginMobile(String username, String userinfo);
    
    /**
     * Auth result data handling.
     * @param authData Map containing authentication data.
     * @param isApproval Approval result.
     */
    public void authClient(Map<String,Object> authData, boolean isApproval);
    
    /**
     * Returns the current client status.
     * @return the current client status. Use in combination with the connection status for full information.
     */
    public ClientStatus getCurrentClientStatus();
    
    /**
     * Returns if a login progress is busy.
     * @return true when loggin in, auth needed or busy logging out.
     */
    public boolean inLoginProgress();
    
    /**
     * Starts preloading all the data.
     */
    public void startPreloaders();
    
    /**
     * Handles the final resource connection initialization.
     * @param status the PCCConnectionStatus with resource success or fail.
     */
    public void handleConnectionResourceInit(PCCConnection.PCCConnectionStatus status);
    
}