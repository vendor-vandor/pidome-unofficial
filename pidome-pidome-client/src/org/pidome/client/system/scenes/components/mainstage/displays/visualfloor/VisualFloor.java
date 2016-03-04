/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.scenes.components.mainstage.displays.visualfloor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.DomResourceException;

/**
 *
 * @author John
 */
final class VisualFloor {
    
    private final Map<String,Object> info;
    
    Image floorImage;
    
    ArrayList<VisualRoom>   rooms = new ArrayList<>();
    ArrayList<MeshedDevice> devices = new ArrayList<>();
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(VisualFloor.class);
    
    protected VisualFloor(Map<String,Object> info){
        this.info = info;
    }
    
    protected final int getFloorId(){
        return ((Long)info.get("id")).intValue();
    }
    
    protected final int getLevel(){
        return Integer.parseInt((String)info.get("level"));
    }

    protected final String getName(){
        return (String)info.get("name");
    }
    
    protected final boolean hasAsset() {
        String image = (String)info.get("image");
        if(image == null || image.equals("")){
            return false;
        }
        return true;
    }
    
    protected final void loadAsset() throws VisualFloorsAssetItemException {
        if(hasAsset()){
            try {
                floorImage = DomComponents.getInstance().loadRemoteImage((String)info.get("image"));
            } catch (DomResourceException ex) {
                throw new VisualFloorsAssetItemException("Could not load floor image asset: " + ex.getMessage());
            }
        }
    }
    
    protected final double getFloorWidth(){
        if(hasAsset()){
            return floorImage.getWidth();
        } else {
            return 0.0;
        }
    }
    
    protected final double getFloorHeight(){
        if(hasAsset()){
            return floorImage.getHeight();
        } else {
            return 0.0;
        }
    }
    
    protected final void loadRooms(){
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("id", getFloorId());
            Map<String,Object> result = DomComponents.getInstance().getJSONData("LocationService.getLocationsByFloor", params).getResult();
            if(result.containsKey("data")){
                for(Map<String,Object> room: (List<Map<String,Object>>)result.get("data")){
                    int id = ((Long)room.get("id")).intValue();
                    String name = (String)room.get("name");
                    double x = ((Long)room.get("screenX")).doubleValue();
                    double y = ((Long)room.get("screenY")).doubleValue();
                    double w = ((Long)room.get("screenW")).doubleValue();
                    double h = ((Long)room.get("screenH")).doubleValue();
                    if(x!=0 && y!=0 && w!=0 && h!=0){
                        rooms.add(new VisualRoom(id,name,x,y,w,h));
                    }
                }
            }
        } catch (DomResourceException ex) {
            LOG.error("Could not load rooms for: {}", getName());
        }
    }
    
    protected final void loadDevices(){
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("floorid", getFloorId());
            Map<String,Object> devicesResult = DomComponents.getInstance().getJSONData("DeviceService.getVisualDevices", params).getResult();
            if(devicesResult.containsKey("data")){
                for(Map<String,Object> device: (List<Map<String,Object>>)devicesResult.get("data")){
                    int id = ((Long)device.get("id")).intValue();
                    double x = ((Long)device.get("screenX")).doubleValue();
                    double y = ((Long)device.get("screenY")).doubleValue();
                    if(x!=0 && y!=0){
                        try {
                            devices.add(new MeshedDevice(new VisualDevice(id),x,y));
                        } catch (DomComponentsException | IOException ex){
                            LOG.error("Could not load device: {}", ex.getMessage());
                        }
                    }
                }
            }
        } catch (DomResourceException ex) {
            LOG.error("Could not load rooms for: {}", getName());
        }
    }
    
    protected final ArrayList<VisualRoom> getRooms(){
        return this.rooms;
    }
    
    protected final ArrayList<MeshedDevice> getDevices(){
        return this.devices;
    }
    
    protected final Image getAsset() throws VisualFloorsAssetItemException {
        if(floorImage==null){
            throw new VisualFloorsAssetItemException("No graphical item present");
        }
        return floorImage;
    }
    
}
