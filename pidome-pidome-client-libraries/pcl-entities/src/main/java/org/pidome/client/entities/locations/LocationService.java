/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.locations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
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
public class LocationService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(LocationService.class.getName()).setLevel(Level.ALL);
    }
    
    private boolean initialized = false;
    
    /**
     * The server connection.
     */
    PCCConnectionInterface connection;
    
    /**
     * List of known locations.
     */
    private final ObservableArrayListBean<Location> locations = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the locations list.
     */
    private final ReadOnlyObservableArrayListBean<Location> readOnlyLocations = new ReadOnlyObservableArrayListBean<>(locations);
    
    /**
     * Constructor
     * @param connection The server connection.
     */
    public LocationService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Initializes a connection listener for the location service.
     */
    @Override
    protected void initilialize() {
        if(!initialized){
            this.connection.addPCCConnectionNameSpaceListener("LocationsService", this);
        }
        initialized = true;
    }

    /**
     * removes a connection listener for the location service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("LocationsService", this);
    }

    /**
     * Loads the initial location list.
     * @throws LocationServiceException when locations can not be loaded 
     */
    private void loadInitialLocationsList() throws LocationServiceException {
        if(locations.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("LocationService.getLocations", null, "LocationService.getLocations"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new LocationServiceException(ex);
            }
        }
    }
    
    /**
     * Preloads the locations.
     * @throws EntityNotAvailableException When the whole macro entity is unavailable.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialLocationsList();
            } catch (LocationServiceException ex) {
                throw new EntityNotAvailableException("Could not preload locations list", ex);
            }
        }
    }

    /**
     * Reloads the locations.
     * @throws EntityNotAvailableException 
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        locations.clear();
        preload();
    }

    /**
     * Returns a single location.
     * Use this only for quick viewing, refer to getting a full list as locations
     * can be deleted where you should be notified of.
     * @param locationId the id of the category you want.
     * @return A Location.
     * @throws LocationNotAvailableException When the requested location is not available.
     */
    public final Location getLocation(int locationId) throws LocationNotAvailableException {
        for(Location loc:this.locations){
            if (loc.getLocationId()==locationId){
                return loc;
            }
        }
        throw new LocationNotAvailableException();
    }
    
    /**
     * Returns a list of loctions.
     * @return a read only observable list of locations.
     */
    public final ReadOnlyObservableArrayListBean<Location> getLocations(){
        return this.readOnlyLocations;
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        this.locations.clear();
    }

    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "addLocation":
                try {
                    Map<String,Object> params = new HashMap<>();
                    params.put("id", rpcDataHandler.getParameters().get("id"));
                    handleRPCCommandByResult(this.connection.getJsonHTTPRPC("LocationService.getLocation", params, "LocationService.getLocation"));
                } catch (PCCEntityDataHandlerException ex) {
                    Logger.getLogger(LocationService.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
            case "deleteLocation":
                try {
                    Location loc = getLocation(((Number)rpcDataHandler.getParameters().get("id")).intValue());
                    this.locations.remove(loc);
                } catch (LocationNotAvailableException ex) {
                    Logger.getLogger(LocationService.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
            case "editLocation":
                try {
                    getLocation(((Number)rpcDataHandler.getParameters().get("id")).intValue()).update(rpcDataHandler.getParameters());
                } catch (LocationNotAvailableException ex) {
                    Logger.getLogger(LocationService.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
        }
    }

    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        switch((String)rpcDataHandler.getId()){
            case "LocationService.getLocations":
                List<Map<String,Object>> locs = (List<Map<String,Object>>)rpcDataHandler.getResult().get("data");
                List<Location> toAdd = new ArrayList<>();
                for(Map<String,Object> loc:locs){
                    toAdd.add(new Location(loc));
                }
                this.locations.addAll(toAdd);
            break;
            case "LocationService.getLocation":
                this.locations.add(new Location((Map<String,Object>)rpcDataHandler.getResult().get("data")));
            break;
        }
    }
    
}
