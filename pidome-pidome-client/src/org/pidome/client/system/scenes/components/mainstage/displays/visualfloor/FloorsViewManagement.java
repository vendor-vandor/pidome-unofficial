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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.DomResourceException;

/**
 *
 * @author John
 */
abstract class FloorsViewManagement extends StackPane {
    
    Label pleaseWait = new Label("Please wait, checking availability");
    
    DomComponents httpresources = DomComponents.getInstance();
    
    TreeMap<Integer,VisualFloor> floorlist = new TreeMap<>();
    
    static Logger LOG = LogManager.getLogger(FloorsViewManagement.class);
    
    public enum Move {
        PAN,ROTATE,ZOOM,NONE
    }
    
    final boolean isSupported() throws VisualFloorsAssetItemException {
        try {
            Map<String,Object> result = httpresources.getJSONData("LocationService.getFloors", null).getResult();
            if(result.containsKey("data")){
                try {
                    for(Map<String,Object> floor: (List<Map<String,Object>>)result.get("data")){
                        VisualFloor floorAsset = new VisualFloor(floor);
                        if(floorAsset.hasAsset()){
                            floorlist.put(floorAsset.getLevel(), floorAsset);
                        }
                    }
                } catch (Exception ex){
                    throw new VisualFloorsAssetItemException("Something went wrong recieving data, please try again");
                }
            }
            if(!floorlist.isEmpty()){
                return true;
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        return false;
    }
 
    protected final void loadFloorAssets(){
        this.floorlist.values().stream().forEach((floor) -> {
            try {
                floor.loadAsset();
                floor.loadRooms();
                floor.loadDevices();
            } catch (VisualFloorsAssetItemException ex) {
                LOG.error("Floor assets could not be loaded: {}", ex.getMessage());
            }
        });
    }
    
    protected void build(){
        Thread loader = new Thread(){
            @Override
            public final void run(){
                loadFloorAssets();
                if(floorlist.size()>0){
                    Platform.runLater(() -> {
                        buildScene();
                    });
                }
            }
        };
        loader.start();
    }
    
    public final VisualFloor getLowestFloor(){
        return this.floorlist.get(this.floorlist.firstKey());
    }
    
    public final VisualFloor getGroundLevel(){
        return this.floorlist.get(0);
    }
    
    public final ArrayList<VisualFloor> getFloorsAsList(){
        ArrayList<VisualFloor> floors = new ArrayList<>();
        this.floorlist.values().stream().forEach((floor) -> {
            floors.add(floor);
        });
        return floors;
    }

    abstract void buildScene();
    
    abstract void setMoveStyle(Move style);
    
    abstract void setFloorActive(int floorId);
    
    abstract boolean hasActiveFloor();
    
    abstract int getActiveFloorId();
    
    abstract void showRegions(boolean show);
    
    abstract void showFloorImage(boolean show);
    
    abstract void showRoomNames(boolean show);
    
    abstract boolean isAnimating();
    
    abstract void destroy();
    
    abstract void showDevices(boolean show);
    
    abstract void animateDevices(boolean animate);
    
    abstract void deviceInfluences(boolean show);
    
    abstract void movementVisualization(boolean show);
    
    abstract void luxVisualization(boolean show);
    
    abstract void tempVisualization(boolean show);
    
}
