/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.floormap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entities;
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
public class FloorMapService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(FloorMapService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private final PCCConnectionInterface connection;
    
    /**
     * The entities to be used on the floor maps.
     */
    private final Entities entities;
    
    /**
     * List of known floors.
     */
    private final ObservableArrayListBean<FloorMapFloor> floorsList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the floors list.
     */
    private final ReadOnlyObservableArrayListBean<FloorMapFloor> readOnlyFloorsList = new ReadOnlyObservableArrayListBean<>(floorsList);
    
    /**
     * Creates the scenes service.
     * @param connection The server connection.
     * @param entities The floormap access to the local entities.
     */
    public FloorMapService(PCCConnectionInterface connection, Entities entities){
        this.entities = entities;
        this.connection = connection;
    }

    /**
     * Initializes a connection listener for the scenes service.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("DeviceService", this);
        this.connection.addPCCConnectionNameSpaceListener("LocationService", this);
    }

    /**
     * removes a connection listener for the scenes service.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("DeviceService", this);
        this.connection.removePCCConnectionNameSpaceListener("LocationService", this);
    }

    /**
     * Does preloading.
     * @throws EntityNotAvailableException 
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(floorsList.isEmpty()){
            loadFloors();
        }
    }

    /**
     * Reloads the floors list.
     * @throws EntityNotAvailableException 
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        for(FloorMapFloor floor:floorsList.subList(0, floorsList.size())){
            floor.destroy();
        }
        floorsList.clear();
        loadFloors();
    }

    /**
     * Loads the floors.
     */
    private void loadFloors(){
        try {
            handleRPCCommandByResult(this.connection.getJsonHTTPRPC("LocationService.getFloors", null, "LocationService.getFloors"));
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(FloorMapService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Returns the list of floors.
     * @return 
     */
    public final ReadOnlyObservableArrayListBean<FloorMapFloor> getFloors(){
        return this.readOnlyFloorsList;
    }
    
    /**
     * Unloads any containing content.
     * @throws EntityNotAvailableException 
     */
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        for(FloorMapFloor floor:floorsList.subList(0, floorsList.size())){
            floor.destroy();
        }
        floorsList.clear();
    }

    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        /// Not used yet
    }

    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        switch((String)rpcDataHandler.getId()){
            case "LocationService.getFloors":
                ArrayList<Map<String,Object>> data = (ArrayList<Map<String,Object>>)rpcDataHandler.getResult().get("data");
                if(data!=null){
                    List<FloorMapFloor> floors = new ArrayList<>();
                    for(Map<String,Object> floorData: data){
                        FloorMapFloor floor = new FloorMapFloor(floorData, this.connection, this.entities);
                        floors.add(floor);
                    }
                    floorsList.addAll(floors);
                }
            break;
        }
    }
}