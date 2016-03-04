/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.backend.data.interfaces.network.NetworkAvailabilityProvider;
import org.pidome.pcl.backend.data.interfaces.storage.LocalPreferenceStorageInterface;
import org.pidome.pcl.backend.data.interfaces.storage.LocalSettingsStorageInterface;
import org.pidome.pcl.data.connection.ConnectionException;
import org.pidome.pcl.networking.connections.server.ServerConnection;

/**
 * The main system class.
 * Use this class to get to all the system resources.
 * @author John
 */
public class PCCSystem {
    
    static {
        Logger.getLogger(PCCSystem.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Set the update mode.
     * Depending on the update mode requests are made to the server. Not all entities
     * support this. A good example of supporting UpdateMode is the DeviceService.
     * Refer to the DeviceService for examples.
     */
    public enum UpdateMode {
        /**
         * Updates entities only when they are requested.
         * When this method is active every time an entities is requested it is
         * requested from the server.
         */
        REQUEST,
        /**
         * Updates entities continuously.
         * When this method is active there are no continuous requests made
         * to the server to get entity details.
         */
        CONTINUOUS
    }
    
    /**
     * Client connection.
     */
    private final PCCConnectionInterface connection;
    
    /**
     * Client settings.
     * Client settings are which are not needed by boot. Better said client
     * preferences when running.
     */
    private final LocalSettingsStorageInterface clientSettings;
    
    /**
     * Client settings.
     * Client settings are which are not needed by boot. Better said client
     * preferences when running.
     */
    private final LocalPreferenceStorageInterface clientPreferences;
    
    /**
     * This is the client which holds login data etc..
     */
    private final PCCClientInterface client;
    
    /**
     * Constructor.
     * There are two connection profiles, these profiles have different usages. These are:
     * - FIXED profile which is used for fixed clients and uses the RAW socket ports.
     * - MOBILE profile which is used for users (persons in the server) and used for clients who are on the move.
     * @param localPreferences Local preferences, use platform specific file storage.
     * @param localSettings Local settings, use platform specific file storage.
     * @param profile The server connection profile to be used for the real time updates.
     * @param netProvider Network availability provider. Different platforms use different methods to supply network availability.
     */
    public PCCSystem(LocalPreferenceStorageInterface localPreferences, LocalSettingsStorageInterface localSettings, ServerConnection.Profile profile, NetworkAvailabilityProvider netProvider){
        clientSettings = localSettings;
        clientPreferences = localPreferences;
        try {
            connection = new PCCConnection(clientSettings, profile, netProvider);
        } catch (ConnectionException ex) {
            throw new RuntimeException(ex);
        }
        client = new PCCClient(clientSettings, clientPreferences, connection);
    }
    
    /**
     * Constructor.
     * There are two connection profiles, these profiles have different usages. These are:
     * - FIXED profile which is used for fixed clients and uses the RAW socket ports.
     * - MOBILE profile which is used for users (persons in the server) and used for clients who are on the move.
     * @param localPreferences Local preferences, use platform specific file storage.
     * @param localSettings Local settings, use platform specific file storage.
     * @param customConnection USe this if there is a need for a custom connection provider.
     * @param customClient Use this if there is a need for a custom client implementation.
     */
    public PCCSystem(LocalPreferenceStorageInterface localPreferences, LocalSettingsStorageInterface localSettings, PCCConnectionInterface customConnection, PCCClientInterface customClient){
        clientSettings    = localSettings;
        clientPreferences = localPreferences;
        connection        = customConnection;
        client            = customClient;
    }
    
    /**
     * Returns the current connection.
     * @return ConnectionInterface
     */
    public final PCCConnectionInterface getConnection(){
        return this.connection;
    }
    
    /**
     * Returns the local settings.
     * @return Returns the local settings.
     */
    public final LocalSettingsStorageInterface getLocalSettings(){
        return this.clientSettings;
    }
    
    /**
     * Returns the local settings.
     * @return Returns the local preferences.
     */
    public final LocalPreferenceStorageInterface getPreferences(){
        return this.clientPreferences;
    }
    
    /**
     * Returns the client.
     * @return Returns the client system, where all the magic happens.
     */
    public final PCCClientInterface getClient(){
        return this.client;
    }
    
}
