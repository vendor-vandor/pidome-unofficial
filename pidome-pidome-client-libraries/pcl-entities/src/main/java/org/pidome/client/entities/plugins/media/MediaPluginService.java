/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.DeviceService;
import org.pidome.client.entities.plugins.weather.WeatherPluginException;
import org.pidome.client.entities.users.UserServiceListener;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 *
 * @author John
 */
public final class MediaPluginService extends Entity implements PCCConnectionNameSpaceRPCListener {
    
    static {
        Logger.getLogger(MediaPluginService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Handle a server command.
     * A server command is specific to control things on the server/appliance where
     * you can thing of browsing in the system, volume up, volume down, etc.
     */
    public enum ServerCommand {
        
        /**
         * Volume up.
         */
        VOLUP("VOLUP"), 
        /**
         * Volume down.
         */
        VOLDOWN("VOLDOWN"), 
        /**
         * Mute volume.
         */
        MUTE("MUTE"), 
        /**
         * Go up.
         */
        UP("UP"), 
        /**
         * Go down.
         */
        DOWN("DOWN"), 
        /**
         * Confirm selection.
         */
        CONFIRM("CONFIRM"), 
        /**
         * Go back.
         */
        BACK("BACK"), 
        /**
         * Go left.
         */
        LEFT("LEFT"), 
        /**
         * Go right.
         */
        RIGHT("RIGHT"), 
        /**
         * Go to home display.
         */
        HOME("HOME"), 
        /**
         * Show OSD.
         */
        OSD("OSD");
        
        /**
         * Private enum String.
         */
        private final String command;

        /**
         * Creates the enum.
         * @param command The command for this enum.
         */
        private ServerCommand(String command) {
            this.command = command;
        }
        
        /**
         * Returns the enum's command.
         * @return The String representation of the command to send.
         */
        protected String getCommand(){
            return this.command;
        }
        
    }

    /**
     * Handle a player command.
     * A player command is meant to control the playing of items.
     */
    public enum PlayerCommand {

        /**
         * Play.
         */
        PLAY("PLAY"), 
        /**
         * Pause.
         */
        PAUSE("PAUSE"), 
        /**
         * Next item.
         */
        NEXT("NEXT"),
        /**
         * Previous item.
         */
        PREV("PREV"), 
        /**
         * Stop item.
         */
        STOP("STOP");

        /**
         * Private enum String.
         */
        private final String command;
        
        /**
         * Creates the enum.
         * @param command The command for this enum.
         */
        private PlayerCommand(String command) {
            this.command = command;
        }
        
        /**
         * Returns the enum's command.
         * @return The string representation of the command to be send.
         */
        protected String getCommand(){
            return this.command;
        }
    }

    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Set of listeners.
     */
    private final HashSet<UserServiceListener> _listeners = new HashSet<>();

    /**
     * List of media plugins.
     */
    private ObservableArrayListBean<MediaPlugin> mediaList = new ObservableArrayListBean();
    /**
     * Read only list of media plugins.
     */
    private ReadOnlyObservableArrayListBean<MediaPlugin> readOnlyMediaList = new ReadOnlyObservableArrayListBean(mediaList);
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public MediaPluginService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Returns a read only list of the available media plugins.
     * @return The media plugins list.
     */
    public final ReadOnlyObservableArrayListBean<MediaPlugin> getMediaList(){
        return readOnlyMediaList;
    }
    
    /**
     * Initialize the service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("MediaService", this);
    }

    /**
     * De-initialize the service releasing listeners.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("MediaService", this);
    }

    /**
     * Loads initial media data.
     * @throws WeatherPluginException 
     */
    private void load() throws MediaPluginException {
        try {
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("MediaService.getPlugins", null, "MediaService.getPlugins"));
        } catch (PCCEntityDataHandlerException ex) {
            throw new MediaPluginException("Problem getting initial weather data");
        }
    }
    
    /**
     * Preloads data.
     * @throws EntityNotAvailableException When the whole plugin structure is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                load();
            } catch (MediaPluginException ex) {
                throw new EntityNotAvailableException("Could not preload media service", ex);
            }
        }
    }

    /**
     * Reloads the service contents
     * @throws EntityNotAvailableException When reloading fails.
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        Iterator<MediaPlugin> iter = mediaList.iterator();
        while(iter.hasNext()){
            iter.next().destroy();
        }
        mediaList.clear();
        preload();
    }

    /**
     * Unloads the service contents.
     * @throws EntityNotAvailableException When unloading the content fails.
     */
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        /// not used.
    }

    /**
     * Creates the initial list.
     * @param data json params data.
     */
    private void composeInitialMediaPlugins(ArrayList<Map<String,Object>> data){
        try {
            List<MediaPlugin> medias = new ArrayList<>();
            for(Map<String,Object> mediaData: data){
                MediaPlugin plugin = createMediaPlugin(mediaData);
                plugin.start();
                medias.add(plugin);
            }
            mediaList.addAll(medias);
        } catch (Exception ex){
            Logger.getLogger(DeviceService.class.getName()).log(Level.SEVERE, "Problem creating device list", ex);
        }
    }
    
    /**
     * Creates a plugin instance.
     */
    private MediaPlugin createMediaPlugin(Map<String,Object> mediaData){
        MediaPlugin plugin = new MediaPlugin(this.connection, ((Number)mediaData.get("id")).intValue(), (String)mediaData.get("name"));
        plugin.setLocationId(((Number)mediaData.get("locationid")).intValue());
        plugin.setTemporaryLocationName((String)mediaData.get("locationname"));
        plugin.setDescription((String)mediaData.get("description"));
        plugin.setActive((boolean)mediaData.get("active"));
        plugin.setPluginName((String)mediaData.get("pluginname"));
        return plugin;
    }
    
    /**
     * Returns the selected media plugin.
     * @param mediaPluginId The id of the plugin to retrieve
     * @return The media plugin
     * @throws MediaPluginException when the given id is not found.
     */
    public final MediaPlugin getMediaPlugin(int mediaPluginId) throws MediaPluginException {
        for(MediaPlugin plugin:mediaList){
            if(plugin.getPluginId()==mediaPluginId){
                return plugin;
            }
        }
        throw new MediaPluginException("Plugin with id "+mediaPluginId+" not found");
    }
    
    /**
     * Handles media data which is being broadcasted.
     * @param rpcDataHandler The composed broadcast data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        /// Not used yet.
    }

    /**
     * Handles media from an http json call result.
     * @param rpcDataHandler The result data.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
        String resultId = (String)rpcDataHandler.getId();
        Runnable run = () -> {
            switch(resultId){
                case "MediaService.getPlugins":
                    composeInitialMediaPlugins(data);
                break;
            }
        };
        run.run();
    }
    
}
