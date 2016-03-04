/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.floormap;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.pidome.client.entities.floormap.FloorMapFloor;

/**
 *
 * @author John
 */
public class FloorsList extends GridPane implements FloorActiveListener {
    
    final private FloorsViewManagement floorStage;
    final private FloorsControl control;
    final double width;
    
    int activeFloor = -1;
    
    protected FloorsList(FloorsViewManagement floorStage, FloorsControl control, double width){
        this.floorStage = floorStage;
        this.control = control;
        this.width = width;
        this.setPrefWidth(width);
        this.setMinWidth(USE_PREF_SIZE);
        this.setMaxWidth(USE_PREF_SIZE);
    }
    
    
    protected final void build(){
        getStyleClass().add("floor-list");
        Label controlsHeader = new Label("Floors");
        controlsHeader.getStyleClass().add("header");
        
        add(controlsHeader, 0, 0, 2, 1);
        
        int row = 1;
        
        /// Make sure the lowest floor is added last so we get the levels from top to bottom in logical order.
        List<FloorMapFloor> ordered = new ArrayList<>();
        for (FloorMapFloor floor : floorStage.getFloorsAsList()) {
            if(ordered.isEmpty()){
                ordered.add(floor);
            } else {
                int putPos = 0;
                for(FloorMapFloor floorCheck:ordered){
                    if(floor.getLevel()<floorCheck.getLevel()){
                        putPos++;
                    } else {
                        break;
                    }
                }
                ordered.add(putPos, floor);
            }
        }
        
        for (FloorMapFloor floor:ordered){
            Label level = new Label(String.valueOf(floor.getLevel()));
            level.setPadding(new Insets(2,5,2,0));
            level.setUserData(floor.getFloorId());
            level.setAlignment(Pos.CENTER_RIGHT);
            level.setMinWidth(25);
            level.setMaxWidth(25);
            GridPane.setHalignment(level, HPos.RIGHT);
            add(level, 0, row);
            
            final Label floorName = new Label(floor.getName().getValue());
            floorName.setUserData(floor.getFloorId());
            floorName.setPadding(new Insets(2,0,2,0));
            floorName.setPrefWidth((getPrefWidth() - level.getMaxWidth()));
            
            add(floorName, 1, row);
            floorName.addEventFilter(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
                if(!floorStage.isAnimating()){
                    if((int)floorName.getUserData()!=activeFloor){
                        setSelectedFloor((int)floorName.getUserData());
                        floorStage.setFloorActive((int)floorName.getUserData());
                        floorStage.setMoveStyle(FloorsViewManagement.Move.ROTATE);
                        this.control.setRotateActive();
                        t.consume();
                    }
                }
            });
            row++;
        }
    }    
    
    @Override
    public void setSelectedFloor(int floorId){
        for (Node label:getChildren()){
            label.getStyleClass().remove("selected");
            if(label.getUserData()!= null && (int)label.getUserData()==floorId){
                activeFloor = (int)label.getUserData();
                label.getStyleClass().add("selected");
            }
        }
    }
    
}
