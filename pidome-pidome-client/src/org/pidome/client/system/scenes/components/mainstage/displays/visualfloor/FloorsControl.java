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

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;

/**
 *
 * @author John
 */
public class FloorsControl extends VBox {
    
    final private FloorsViewManagement floorStage;
    
    Glow glow = new Glow(1.0);
    
    static Logger LOG = LogManager.getLogger(FloorsControl.class);
    
    ImageView rotate3D = new ImageView(new ImageLoader("icons/3drotate.png",65,65).getImage());
    ImageView pan3D = new ImageView(new ImageLoader("icons/3dpan.png",65,65).getImage());
    ImageView zoom3D = new ImageView(new ImageLoader("icons/3dzoom.png",65,65).getImage());
    ImageView reset3D = new ImageView(new ImageLoader("icons/3dfloorreset.png",65,65).getImage());
    
    Label controlsHeader = new Label("Controls");
    Label floorsHeader = new Label("Floors");
    Label layersLabel = new Label("Options");
    
    TilePane controlTiles = new TilePane();
    
    GridPane floorList = new GridPane();
    
    int activeFloor = -1;
    
    protected FloorsControl(FloorsViewManagement floorStage){
        super(10);
        this.floorStage = floorStage;
        setId("floorscontrol");
    }
    
    protected final void build(){
        controlsHeader.getStyleClass().add("header");
        controlsHeader.setPrefWidth(Double.MAX_VALUE);
        floorsHeader.getStyleClass().add("header");
        floorsHeader.setPrefWidth(Double.MAX_VALUE);
        layersLabel.getStyleClass().add("header");
        layersLabel.setPrefWidth(Double.MAX_VALUE);
        getChildren().add(floorsHeader);
        buildFloorSelector();
        getChildren().add(controlsHeader);
        
        controlTiles.setVgap(12);
        controlTiles.setHgap(12);
        controlTiles.setAlignment(Pos.CENTER);
        
        if(floorStage instanceof FloorsViewPort3D){
            build3DControls();
        } else {
            build2DControls();
        }
        getChildren().add(layersLabel);
        buildLayersSelect();
    }
    
    private void buildFloorSelector(){
        floorList.getStyleClass().add("floorlist");
        floorList.setPrefWidth(getMinWidth());
        int row = 0;
        for (int i = floorStage.getFloorsAsList().size() - 1 ; i >= 0 ; i-- ){
            Label level = new Label(String.valueOf(floorStage.getFloorsAsList().get(i).getLevel()));
            level.setPadding(new Insets(2,5,2,0));
            level.setUserData(i);
            level.setAlignment(Pos.CENTER_RIGHT);
            level.setMinWidth(25*DisplayConfig.getWidthRatio());
            level.setMaxWidth(25*DisplayConfig.getHeightRatio());
            GridPane.setHalignment(level, HPos.RIGHT);
            floorList.add(level, 0, row);
            
            final Label floorName = new Label(floorStage.getFloorsAsList().get(i).getName());
            floorName.setUserData(i);
            floorName.setPadding(new Insets(2,0,2,0));
            floorName.setPrefWidth((floorList.getPrefWidth() - level.getMaxWidth()));
            
            floorList.add(floorName, 2, row);
            floorName.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
                if(!floorStage.isAnimating()){
                    if((int)floorName.getUserData()!=activeFloor){
                        setSelectedFloor((int)floorName.getUserData());
                        floorStage.setFloorActive((int)floorName.getUserData());
                        floorStage.setMoveStyle(FloorsViewManagement.Move.ROTATE);
                        setRotateActive();
                        t.consume();
                    }
                }
            });
            row++;
        }
        getChildren().add(floorList);
    }
    
    private void setSelectedFloor(int floorId){
        for (Node label:floorList.getChildren()){
            if(label.getUserData()!= null && (int)label.getUserData()==floorId){
                activeFloor = (int)label.getUserData();
                label.getStyleClass().add("selected");
            } else {
                label.getStyleClass().remove("selected");
            }
        }
    }
    
    protected void setRotateActive(Integer externalFloorId){
        if(externalFloorId!=null){
            for (int i = floorStage.getFloorsAsList().size() - 1 ; i >= 0 ; i-- ){
                if(floorStage.getFloorsAsList().get(i).getFloorId() == externalFloorId){
                    setSelectedFloor(i);
                }
            }
        }
        rotate3D.setEffect(glow);
        zoom3D.setEffect(null);
        pan3D.setEffect(null);
        reset3D.setEffect(null);
    }
    
    protected void setRotateActive(){
        setRotateActive(null);
    }
    
    private void build3DControls(){
        reset3D.setEffect(glow);
        rotate3D.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(floorStage.hasActiveFloor()){
                setRotateActive();
                floorStage.setMoveStyle(FloorsViewManagement.Move.ROTATE);
                t.consume();
            }
        });
        pan3D.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(floorStage.hasActiveFloor()){
                pan3D.setEffect(glow);
                zoom3D.setEffect(null);
                rotate3D.setEffect(null);
                reset3D.setEffect(null);
                floorStage.setMoveStyle(FloorsViewManagement.Move.PAN);
                t.consume();
            }
        }); 
        zoom3D.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(floorStage.hasActiveFloor()){
                zoom3D.setEffect(glow);
                rotate3D.setEffect(null);
                pan3D.setEffect(null);
                reset3D.setEffect(null);
                floorStage.setMoveStyle(FloorsViewManagement.Move.ZOOM);
                t.consume();
            }
        }); 
        reset3D.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(floorStage.hasActiveFloor()){
                reset3D.setEffect(glow);
                zoom3D.setEffect(null);
                rotate3D.setEffect(null);
                pan3D.setEffect(null);
                floorStage.setMoveStyle(FloorsViewManagement.Move.NONE);
                setSelectedFloor(-1000);
                t.consume();
            }
        });
        controlTiles.getChildren().addAll(reset3D, rotate3D, pan3D, zoom3D);
        getChildren().add(controlTiles);
    }
    
    private void build2DControls(){
        pan3D.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(floorStage.hasActiveFloor()){
                pan3D.setEffect(glow);
                zoom3D.setEffect(null);
                floorStage.setMoveStyle(FloorsViewManagement.Move.PAN);
                t.consume();
            }
        }); 
        zoom3D.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(floorStage.hasActiveFloor()){
                zoom3D.setEffect(glow);
                pan3D.setEffect(null);
                floorStage.setMoveStyle(FloorsViewManagement.Move.ZOOM);
                t.consume();
            }
        });
        controlTiles.getChildren().addAll(pan3D,zoom3D);
        getChildren().add(controlTiles);
    }
    
    private void buildLayersSelect(){
        VBox layerList = new VBox(3);
        layerList.getStyleClass().add("layers");
        
        Label ShowFloorPlanImage = new Label("Floorplan image");
        
        ShowFloorPlanImage.setPrefWidth(Double.MAX_VALUE);
        ShowFloorPlanImage.setPadding(new Insets(2,0,2,5));
        
        ShowFloorPlanImage.getStyleClass().add("selected");
        
        ShowFloorPlanImage.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(ShowFloorPlanImage.getStyleClass().contains("selected")){
                ShowFloorPlanImage.getStyleClass().remove("selected");
                floorStage.showFloorImage(false);
            } else {
                ShowFloorPlanImage.getStyleClass().add("selected");
                floorStage.showFloorImage(true);
            }
        });
        
        Label ShowRoomsRegions = new Label("Rooms/Regions");
        
        ShowRoomsRegions.setPrefWidth(Double.MAX_VALUE);
        ShowRoomsRegions.setPadding(new Insets(2,0,2,5));
        
        ShowRoomsRegions.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(ShowRoomsRegions.getStyleClass().contains("selected")){
                ShowRoomsRegions.getStyleClass().remove("selected");
                floorStage.showRegions(false);
            } else {
                ShowRoomsRegions.getStyleClass().add("selected");
                floorStage.showRegions(true);
            }
        });
        
        Label ShowRoomNames = new Label("Room/Region names");
        
        ShowRoomNames.setPrefWidth(Double.MAX_VALUE);
        ShowRoomNames.setPadding(new Insets(2,0,2,5));
        
        ShowRoomNames.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(ShowRoomNames.getStyleClass().contains("selected")){
                ShowRoomNames.getStyleClass().remove("selected");
                floorStage.showRoomNames(false);
            } else {
                ShowRoomNames.getStyleClass().add("selected");
                floorStage.showRoomNames(true);
            }
        });
        
        Label animateDevices = new Label("Show device activity");
        animateDevices.setPadding(new Insets(2,0,2,10));
        
        animateDevices.setPrefWidth(Double.MAX_VALUE);
        
        animateDevices.getStyleClass().add("selected");
        
        animateDevices.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(animateDevices.getStyleClass().contains("selected")){
                animateDevices.getStyleClass().remove("selected");
                floorStage.animateDevices(false);
            } else if (!animateDevices.getStyleClass().contains("disabled")){
                animateDevices.getStyleClass().add("selected");
                floorStage.animateDevices(true);
            }
        });
        
        Label influenceDevices = new Label("Show device influence (rooms)");
        
        influenceDevices.setPrefWidth(Double.MAX_VALUE);
        influenceDevices.setPadding(new Insets(2,0,2,10));
        
        influenceDevices.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(influenceDevices.getStyleClass().contains("selected")){
                influenceDevices.getStyleClass().remove("selected");
                floorStage.deviceInfluences(false);
            } else if (!influenceDevices.getStyleClass().contains("disabled")){
                influenceDevices.getStyleClass().add("selected");
                floorStage.deviceInfluences(true);
            }
        });
        
        Label showDevices = new Label("Show devices");
        
        showDevices.setPrefWidth(Double.MAX_VALUE);
        showDevices.setPadding(new Insets(2,0,2,5));
        
        showDevices.getStyleClass().add("selected");
        
        showDevices.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(showDevices.getStyleClass().contains("selected")){
                floorStage.deviceInfluences(false);
                floorStage.animateDevices(false);
                floorStage.showDevices(false);
                showDevices.getStyleClass().remove("selected");
                animateDevices.getStyleClass().remove("selected");
                animateDevices.getStyleClass().add("disabled");
                influenceDevices.getStyleClass().remove("selected");
                influenceDevices.getStyleClass().add("disabled");
            } else {
                showDevices.getStyleClass().add("selected");
                floorStage.showDevices(true);
                animateDevices.getStyleClass().remove("disabled");
                influenceDevices.getStyleClass().remove("disabled");
            }
        });
        
        Label temp = new Label("Temperature map");
        temp.setPrefWidth(Double.MAX_VALUE);
        temp.setPadding(new Insets(2,0,2,5));
        
        temp.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(temp.getStyleClass().contains("selected")){
                floorStage.tempVisualization(false);
                temp.getStyleClass().remove("selected");
            } else {
                temp.getStyleClass().add("selected");
                floorStage.tempVisualization(true);
            }
        });
        
        Label lux = new Label("Light map");
        lux.setPrefWidth(Double.MAX_VALUE);
        lux.setPadding(new Insets(2,0,2,5));
        
        lux.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(lux.getStyleClass().contains("selected")){
                floorStage.luxVisualization(false);
                lux.getStyleClass().remove("selected");
            } else {
                lux.getStyleClass().add("selected");
                floorStage.luxVisualization(true);
            }
        });
                
        Label mov = new Label("Show movements");
        mov.setPrefWidth(Double.MAX_VALUE);
        mov.setPadding(new Insets(2,0,2,5));
        
        mov.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            if(mov.getStyleClass().contains("selected")){
                floorStage.movementVisualization(false);
                mov.getStyleClass().remove("selected");
            } else {
                mov.getStyleClass().add("selected");
                floorStage.movementVisualization(true);
            }
        });
        
        layerList.getChildren().addAll(ShowFloorPlanImage,
                                       ShowRoomsRegions,
                                       ShowRoomNames,
                                       temp,
                                       lux,
                                       mov,
                                       showDevices,
                                       animateDevices,
                                       influenceDevices);
        getChildren().add(layerList);
    }
    
}