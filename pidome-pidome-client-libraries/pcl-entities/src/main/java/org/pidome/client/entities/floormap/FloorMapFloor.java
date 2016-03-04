/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.floormap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entities;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.devices.UnknownDeviceException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;
import org.pidome.pcl.utilities.properties.StringPropertyBindingBean;

/**
 * Class holding a single floor data set.
 * All though floors supply images to visualize floors this class does not supply
 * a floor image as binary source but only as byte[]. This because this package
 * is multi platform and not all specific image handling is available on any
 * platform. Please refer to 
 * @author John
 */
public class FloorMapFloor {
    
    /**
     * The scene's name in a bindable bean.
     */
    private final StringPropertyBindingBean name = new StringPropertyBindingBean();
    
    /**
     * The floor id.
     */
    private final int floorId;
    
    /**
     * The floor level.
     */
    private final int floorLevel;
    
    /**
     * The floor width.
     */
    private double floorWidth = 0;
    
    /**
     * The floor height.
     */
    private double floorHeight = 0;
    
    /**
     * The floor image.
     */
    private final StringPropertyBindingBean image = new StringPropertyBindingBean();
    
    /**
     * List of known floors.
     */
    private final ObservableArrayListBean<FloorMapRoom> roomsList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the floors list.
     */
    private final ReadOnlyObservableArrayListBean<FloorMapRoom> readOnlyRoomsList = new ReadOnlyObservableArrayListBean<>(roomsList);
    
    /**
     * List of known devices in this room.
     */
    private final ObservableArrayListBean<FloorMapDevice> devicesList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the devices in this room.
     */
    private final ReadOnlyObservableArrayListBean<FloorMapDevice> readOnlyDevicesList = new ReadOnlyObservableArrayListBean<>(devicesList);
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * The PCC entities the floormap should have access to.
     */
    private Entities entities;
    
    /**
     * Constructor.
     * @param floorData Map containing configuration data from RPC.
     * @param connection The connection to the server.
     * @param entities The access to local entities.
     */
    protected FloorMapFloor(Map<String,Object> floorData, PCCConnectionInterface connection, Entities entities){
        floorId    = ((Number)floorData.get("id")).intValue();
        floorLevel = ((Number)floorData.get("level")).intValue();
        name.setValue((String)floorData.get("name"));
        image.setValue((String)floorData.get("image"));
        this.entities = entities;
        this.connection = connection;
        loadRooms();
        loadDevices();
    }
    
    /**
     * Returns the floor id.
     * @return The int id of the floor
     */
    public final int getFloorId(){
        return floorId;
    }
    
    /**
     * The floor level to return.
     * The floor level indicates on which level the floor is. Level 0 is ground
     * floor. Level 1 first and level -1 basement, etc..
     * This are user set levels, so theoratically a level -10 and 10 can do
     * exist.
     * @return int floor level
     */
    public final int getLevel(){
        return floorLevel;
    }

    /**
     * Retuns the floor name.
     * @return A boundable floor name.
     */
    public final StringPropertyBindingBean getName(){
        return this.name;
    }
    
    /**
     * Returns if an asste is available.
     * @return boolean true when an asset is available.
     */
    public final boolean hasFloorImageAsset() {
        return image.getValue()!= null && !image.getValue().equals("");
    }
    
    /**
     * Returns the path to the remote floor image source.
     * @return 
     */
    public final String getFloorImageAsset() {
        return image.getValueSafe();
    }
    
    /**
     * Returns image byte[] data.
     * As this library targets multiple platforms it is your responsibility to
     * apply the correct code for creating the final image. The image is 
     * guaranteed a png image.
     * @return byte array with png image data.
     * @throws PCCEntityDataHandlerException When no image data is available.
     */
    public final byte[] loadFloorAsset() throws PCCEntityDataHandlerException {
        if(hasFloorImageAsset()){
            return this.connection.getBinaryHttp(getFloorImageAsset(), null);
        } else {
            throw new PCCEntityDataHandlerException("No image asset available");
        }
    }
    
    /**
     * Sets the floor width.
     * @param width 
     */
    public final void setFloorWidth(double width){
        this.floorWidth = width;
    }
    
    /**
     * Set's the floor height.
     * @param height 
     */
    public final void setFloorHeight(double height){
        this.floorHeight = height;
    }
    
    /**
     * Returns the floor height.
     * Be sure to have set the floor height before using this. As this library targets
     * different systems you have to get the image using loadFloorAsset() and create
     * an image from this byte array. After you have done this you can set the height.
     * @return double floor height.
     */
    public final double getFloorWidth(){
        return this.floorWidth;
    }
    
    /**
     * Returns the floor width.
     * Be sure to have set the floor width before using this. As this library targets
     * different systems you have to get the image using loadFloorAsset() and create
     * an image from this byte array. After you have done this you can set the width.
     * @return double floor width.
     */
    public final double getFloorHeight(){
        return this.floorHeight;
    }
    
    /**
     * Loads the available rooms.
     */
    protected final void loadRooms(){
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("id", getFloorId());
            Map<String,Object> result = this.connection.getJsonHTTPRPC("LocationService.getLocationsByFloor", params, "LocationService.getLocationsByFloor").getResult();
            if(result.containsKey("data")){
                ArrayList<FloorMapRoom> addToObservableList = new ArrayList<>();
                for(Map<String,Object> room: (List<Map<String,Object>>)result.get("data")){
                    int      id = ((Number)room.get("id")).intValue();
                    double    x = ((Number)room.get("screenX")).doubleValue();
                    double    y = ((Number)room.get("screenY")).doubleValue();
                    double    w = ((Number)room.get("screenW")).doubleValue();
                    double    h = ((Number)room.get("screenH")).doubleValue();
                    String name =  (String)room.get("name");
                    if(x!=0 && y!=0 && w!=0 && h!=0){
                        addToObservableList.add(new FloorMapRoom(id,name,x,y,w,h));
                    }
                }
                this.roomsList.addAll(addToObservableList);
            }
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(FloorMapFloor.class.getName()).log(Level.SEVERE, "Could not load floor bound locations", ex);
        }
    }
    
    /**
     * Loads device belonging to this specific floor.
     */
    protected final void loadDevices(){
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("floorid", getFloorId());
            Map<String,Object> devicesResult = this.connection.getJsonHTTPRPC("DeviceService.getVisualDevices", params, "DeviceService.getVisualDevices").getResult();
            if(devicesResult.containsKey("data")){
                for(Map<String,Object> device: (List<Map<String,Object>>)devicesResult.get("data")){
                    int id = ((Long)device.get("id")).intValue();
                    double x = ((Long)device.get("screenX")).doubleValue();
                    double y = ((Long)device.get("screenY")).doubleValue();
                    if(x!=0 && y!=0){
                        try {
                            devicesList.add(new FloorMapDevice(this.entities.getDeviceService().getDevice(id),x,y));
                        } catch (EntityNotAvailableException | UnknownDeviceException ex) {
                            Logger.getLogger(FloorMapFloor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(FloorMapFloor.class.getName()).log(Level.SEVERE, "Could not load floor bound devices", ex);
        }
    }
    
    /**
     * Returns a list of rooms.
     * @return A list of rooms in a read only observable array list.
     */
    public final ReadOnlyObservableArrayListBean<FloorMapRoom> getRooms(){
        return this.readOnlyRoomsList;
    }
    
    /**
     * Returns a list of rooms.
     * @return A list of rooms in a read only observable array list.
     */
    public final ReadOnlyObservableArrayListBean<FloorMapDevice> getDevices(){
        return this.readOnlyDevicesList;
    }
    
    /**
     * Destroy and clear inner content.
     */
    protected final void destroy(){
        for(FloorMapDevice device:this.readOnlyDevicesList.subList(0, this.readOnlyDevicesList.size())){
            device.destroy();
        }
        this.entities = null;
        this.connection = null;
        this.devicesList.clear();
        this.roomsList.clear();
    }
    
}