/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.floormap;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 *
 * @author John
 */
public class FloorsOptions extends VBox {
    
    final private FloorsViewManagement floorStage;
    
    protected FloorsOptions(FloorsViewManagement floorStage, double width){
        super(10);
        this.floorStage = floorStage;
        getStyleClass().add("floor-layers");
        this.setPrefWidth(width);
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
    }
    
    protected final void build(){
        
        Label controlsHeader = new Label("Options");
        controlsHeader.getStyleClass().add("header");
        controlsHeader.setPrefWidth(Double.MAX_VALUE);
        
        getChildren().add(controlsHeader);
        
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
                floorStage.showRegions(false, false);
            } else {
                ShowRoomsRegions.getStyleClass().add("selected");
                floorStage.showRegions(true, false);
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
        
        getChildren().addAll(ShowFloorPlanImage,
                             ShowRoomsRegions,
                             ShowRoomNames,
                             temp,
                             lux,
                             mov,
                             showDevices,
                             animateDevices,
                             influenceDevices);
    }
    
    
}