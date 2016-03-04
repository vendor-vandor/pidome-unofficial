/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities;

import org.pidome.client.entities.system.SystemService;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.categories.CategoryService;
import org.pidome.client.entities.dashboard.DashboardService;
import org.pidome.client.entities.devicediscovery.DeviceDiscoveryService;
import org.pidome.client.entities.devices.DeviceService;
import org.pidome.client.entities.floormap.FloorMapService;
import org.pidome.client.entities.locations.LocationService;
import org.pidome.client.entities.macros.MacroService;
import org.pidome.client.entities.notifications.NotificationService;
import org.pidome.client.entities.plugins.media.MediaPluginService;
import org.pidome.client.entities.plugins.utilityusages.UtilitiesUsagesPlugin;
import org.pidome.client.entities.plugins.weather.WeatherPluginService;
import org.pidome.client.entities.presences.PresenceService;
import org.pidome.client.entities.remotes.PiDomeRemotesService;
import org.pidome.client.entities.scenes.ScenesService;
import org.pidome.client.entities.users.UserService;
import org.pidome.client.entities.userstatus.UserStatusService;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.utilities.properties.IntegerPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyIntegerPropertyBindingBean;

/**
 * Main entities class which provides all the PiDome entities.
 * Request this object using the PCCClient.
 * This object by default is empty. When the user is logged in objects
 * contained by the entities will be available. When the user is logged out
 * for whatever reason all the objects are released.
 * @author John
 */
public class Entities {
    
    static {
        Logger.getLogger(Entities.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * All the entities collection.
     */
    private final Map<String,Entity> entities = new HashMap<>();
    
    /**
     * The amount of entities loaded.
     * This does not hold the amount of entities initialized.
     */
    private final IntegerPropertyBindingBean entitiesLoaded = new IntegerPropertyBindingBean(0);
    
    /**
     * The PCC connection object so entities can use this to send/receive entity data.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Registers if preloading has been done.
     */
    private boolean preloaded = false;
    
    /**
     * Keep track of client readyness.
     */
    private boolean clientReady = false;
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public Entities(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Initializes all the entities.
     */
    public final void initialize(){
        preloaded = true;
        entities.put("SystemService",          new SystemService(this.connection));
        entities.put("CategoryService",        new CategoryService(this.connection));
        entities.put("LocationService",        new LocationService(this.connection));
        entities.put("NotificationService",    new NotificationService(this.connection));
        entities.put("PresenceService",        new PresenceService(this.connection));
        entities.put("UserService",            new UserService(this.connection));
        entities.put("WeatherPluginService",   new WeatherPluginService(this.connection));
        entities.put("MacroService",           new MacroService(this.connection));
        entities.put("UserStatusService",      new UserStatusService(this.connection));
        entities.put("MediaService",           new MediaPluginService(this.connection));
        entities.put("UtilitiesUsages",        new UtilitiesUsagesPlugin(this.connection));
        entities.put("ScenesService",          new ScenesService(this.connection));
        entities.put("DashboardService",       new DashboardService(this.connection));
        entities.put("PiDomeRemotesService",   new PiDomeRemotesService(this.connection));
        entities.put("FloorMapService",        new FloorMapService(this.connection, this));
        entities.put("DeviceDiscoveryService", new DeviceDiscoveryService(this.connection));
        entities.put("DeviceService",          new DeviceService(this.connection, (LocationService)entities.get("LocationService")));
        for(Entity entity:entities.values()){
            entity.initilialize();
        }
    }
    
    /**
     * Some services need special attention in preloading.
     * Subsequental calls will not do. This is already handled automatically.
     */
    public final void pulseReady(){
        if(!clientReady){
            try {
                entities.get("CategoryService").preload();
            } catch (EntityNotAvailableException ex) {
                Logger.getLogger(Entities.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex){
                /// This one should not be handled as when custom implementations are used or there is no pre-init it is correct to be null
            }
            try {
                entities.get("LocationService").preload();
            } catch (EntityNotAvailableException ex) {
                Logger.getLogger(Entities.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex){
                /// This one should not be handled as when custom implementations are used or there is no pre-init it is correct to be null
            }
        }
        clientReady = true;
    }
    
    public boolean initialized(){
        return preloaded;
    }
    
    /**
     * Clears all the services their content.
     */
    public final void EmptyServices() {
        for (Entity entity : entities.values()) {
            try {
                entity.unloadContent();
            } catch (EntityNotAvailableException ex) {
                EntityErrorBroadcaster.broadcastMessage("Preload issue", ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Returns the amount of entities.
     * @return the total amount of possible entities.
     */
    public final int getEntitiesCount(){
        return entities.size();
    }
    
    /**
     * Runs all the system preloaders.
     * This can be used while initializing to show progress in combination with
     * the loaded counter property.
     */
    public final void runPreloaders(){
        try {
            for(Entity entity:entities.values()){
                entity.preload();
                entitiesLoaded.set(entitiesLoaded.get()+1);
            }
        } catch (EntityNotAvailableException ex) {
            EntityErrorBroadcaster.broadcastMessage("Preload issue", ex.getMessage(), ex);
        }
    }
    
    /**
     * Returns the amount of entities pre loaded.
     * You can use this with a preloader in combination with total entities.
     * @return A bindable bean with how many entities are loaded.
     */
    public final ReadOnlyIntegerPropertyBindingBean getLoadedEntitiesCount(){
        return this.entitiesLoaded.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns the system service.
     * @return The System service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the system entity is unavailable.
     */
    public final SystemService getSystemService() throws EntityNotAvailableException {
        if(entities.get("SystemService")!=null){
            return (SystemService)entities.get("SystemService");
        } else {
            throw new EntityNotAvailableException("System Service not available");
        }
    }
    
    /**
     * Returns the notification service.
     * @return The notification service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the notification service is unavailable.
     */
    public final NotificationService getNotificationService() throws EntityNotAvailableException {
        if(entities.get("NotificationService")!=null){
            return (NotificationService)entities.get("NotificationService");
        } else {
            throw new EntityNotAvailableException("Notification Service not available");
        }
    }
    
    /**
     * Returns the users service.
     * @return The user service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the user service is unavailable.
     */
    public final UserService getUserService() throws EntityNotAvailableException {
        if(entities.get("UserService")!=null){
            return (UserService)entities.get("UserService");
        } else {
            throw new EntityNotAvailableException("User Service not available");
        }
    }
    
    /**
     * Returns the presence service.
     * @return The presence service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the presence service is unavailable.
     */
    public final PresenceService getPresenceService() throws EntityNotAvailableException {
        if(entities.get("PresenceService")!=null){
            return (PresenceService)entities.get("PresenceService");
        } else {
            throw new EntityNotAvailableException("Presence Service not available");
        }
    }
    
    /**
     * Returns the weather service.
     * @return The weather service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the weather service is unavailable.
     */
    public final WeatherPluginService getWeatherPluginService() throws EntityNotAvailableException {
        if(entities.get("WeatherPluginService")!=null){
            return (WeatherPluginService)entities.get("WeatherPluginService");
        } else {
            throw new EntityNotAvailableException("Weather plugin Service not available");
        }
    }
    
    /**
     * Returns the device service.
     * @return The device service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the device service is unavailable.
     */
    public final DeviceService getDeviceService() throws EntityNotAvailableException {
        if(entities.get("DeviceService")!=null){
            return (DeviceService)entities.get("DeviceService");
        } else {
            throw new EntityNotAvailableException("Device Service not available");
        }
    }
    
    /**
     * Returns the macro service.
     * @return The macro service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the macro service is unavailable.
     */
    public final MacroService getMacroService() throws EntityNotAvailableException {
        if(entities.get("MacroService")!=null){
            return (MacroService)entities.get("MacroService");
        } else {
            throw new EntityNotAvailableException("Macro Service not available");
        }
    }
    
    /**
     * Returns the User status service.
     * @return The user status service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the user status service is unavailable.
     */
    public final UserStatusService getUserStatusService() throws EntityNotAvailableException {
        if(entities.get("UserStatusService")!=null){
            return (UserStatusService)entities.get("UserStatusService");
        } else {
            throw new EntityNotAvailableException("UserStatus Service not available");
        }
    }
    
    /**
     * Returns the media plugin service.
     * @return The user status service object.
     * @throws org.pidome.client.entities.EntityNotAvailableException When the user status service is unavailable.
     */
    public final MediaPluginService getMediaService() throws EntityNotAvailableException {
        if(entities.get("MediaService")!=null){
            return (MediaPluginService)entities.get("MediaService");
        } else {
            throw new EntityNotAvailableException("Media Service not available");
        }
    }
    
    /**
     * Returns the utility service.
     * @return The utility service object.
     * @throws EntityNotAvailableException When the utility service is not available.
     */
    public final UtilitiesUsagesPlugin getUtilityService() throws EntityNotAvailableException {
        if(entities.get("UtilitiesUsages")!=null){
            return (UtilitiesUsagesPlugin)entities.get("UtilitiesUsages");
        } else {
            throw new EntityNotAvailableException("Utility Service not available");
        }
    }
    
    /**
     * Returns the scenes service.
     * @return The scenes service object.
     * @throws EntityNotAvailableException When the scenes service is not available.
     */
    public final ScenesService getScenesService() throws EntityNotAvailableException {
        if(entities.get("UtilitiesUsages")!=null){
            return (ScenesService)entities.get("ScenesService");
        } else {
            throw new EntityNotAvailableException("Scenes Service not available");
        }
    }
    
    /**
     * Returns the Dashboard service.
     * @return The Dashboard service object.
     * @throws EntityNotAvailableException When the scenes service is not available.
     */
    public final DashboardService getDashboardService() throws EntityNotAvailableException {
        if(entities.get("DashboardService")!=null){
            return (DashboardService)entities.get("DashboardService");
        } else {
            throw new EntityNotAvailableException("Dashboard Service not available");
        }
    }
    
    /**
     * Returns the Remotes service.
     * @return The Remotes service object.
     * @throws EntityNotAvailableException When the remotes service is not available.
     */
    public final PiDomeRemotesService getPiDomeRemotesService() throws EntityNotAvailableException {
        if(entities.get("PiDomeRemotesService")!=null){
            return (PiDomeRemotesService)entities.get("PiDomeRemotesService");
        } else {
            throw new EntityNotAvailableException("PiDomeRemotes Service not available");
        }
    }
    
    /**
     * Returns the Floormap service.
     * @return The floormap service object.
     * @throws EntityNotAvailableException When the floor map service is not available.
     */
    public final FloorMapService getFloorMapService() throws EntityNotAvailableException {
        if(entities.get("FloorMapService")!=null){
            return (FloorMapService)entities.get("FloorMapService");
        } else {
            throw new EntityNotAvailableException("Floor Map Service not available");
        }
    }
    
    /**
     * Returns the Device discovery service.
     * @return The device discovery service object.
     * @throws EntityNotAvailableException When the discovery service is not available.
     */
    public final DeviceDiscoveryService getDeviceDiscoveryService() throws EntityNotAvailableException {
        if(entities.get("DeviceDiscoveryService")!=null){
            return (DeviceDiscoveryService)entities.get("DeviceDiscoveryService");
        } else {
            throw new EntityNotAvailableException("Device Discovery Service not available");
        }
    }
    
    /**
     * Returns the Category Service.
     * @return The Category Service object.
     * @throws EntityNotAvailableException When the Category Service is not available.
     */
    public final CategoryService getCategoryService() throws EntityNotAvailableException {
        if(entities.get("CategoryService")!=null){
            return (CategoryService)entities.get("CategoryService");
        } else {
            throw new EntityNotAvailableException("Category Service not available");
        }
    }
      
    /**
     * Returns the Location Service.
     * @return The Location Service object.
     * @throws EntityNotAvailableException When the Location Service is not available.
     */
    public final LocationService getLocationService() throws EntityNotAvailableException {
        if(entities.get("LocationService")!=null){
            return (LocationService)entities.get("LocationService");
        } else {
            throw new EntityNotAvailableException("Location Service not available");
        }
    }
    
}