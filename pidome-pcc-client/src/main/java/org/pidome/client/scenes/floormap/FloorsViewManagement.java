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

package org.pidome.client.scenes.floormap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.floormap.FloorMapFloor;
import org.pidome.client.entities.floormap.FloorMapService;

/**
 *
 * @author John
 */
abstract class FloorsViewManagement extends StackPane {
    
    Label pleaseWait = new Label("Please wait, checking availability");
    
    private final FloorMapService floorMapService;
    
    FloorActiveListener floorEnableListener;
    FloorMoveEnabledListener floormoveEnabled;
    
    public enum Move {
        PAN,ROTATE,ZOOM,NONE
    }
    
    public FloorsViewManagement(FloorMapService service){
        floorMapService = service;
        this.getStyleClass().add("map-container");
    }
    
    final boolean isSupported() throws VisualFloorsAssetItemException {
        try {
            floorMapService.preload();
            return true;
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(FloorsViewManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    protected void build(){
        Thread loader = new Thread(){
            @Override
            public final void run(){
                if(floorMapService.getFloors().size()>0){
                    Platform.runLater(() -> {
                        buildScene();
                    });
                }
            }
        };
        loader.start();
    }
    
    public final FloorMapFloor getLowestFloor(){
        int floorNum = Integer.MAX_VALUE;
        FloorMapFloor returnFloor = null;
        for(FloorMapFloor floor:floorMapService.getFloors().subList(0, floorMapService.getFloors().size())){
            if(floor.getLevel()<floorNum){
                returnFloor = floor;
                floorNum = returnFloor.getLevel();
            }
        }
        return returnFloor;
    }
    
    public final FloorMapFloor getGroundLevel(){
        for(FloorMapFloor floor:floorMapService.getFloors().subList(0, floorMapService.getFloors().size())){
            if(floor.getLevel()==0){
                return floor;
            }
        }
        return null;
    }
    
    public final List<FloorMapFloor> getFloorsAsList(){
        return floorMapService.getFloors().subList(0, floorMapService.getFloors().size());
    }

    public void setFloorEnabledListener(FloorActiveListener floorEnableListener){
        this.floorEnableListener = floorEnableListener;
    }
    
    public void setMoveEnabledListener(FloorMoveEnabledListener floormoveEnabled){
        this.floormoveEnabled = floormoveEnabled;
    }
    
    abstract void buildScene();
    
    abstract void setMoveStyle(Move style);
    
    abstract void setFloorActive(int floorId);
    
    abstract boolean hasActiveFloor();
    
    abstract int getActiveFloorId();
    
    abstract void showRegions(boolean show, boolean initial);
    
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
