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

import javafx.application.Platform;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 *
 * @author John
 */
public class FloorsControl extends HBox implements FloorMoveEnabledListener {
    
    final private FloorsViewManagement floorStage;
    
    Glow glow = new Glow(1.0);
    
    FloorActiveListener floorActiveListener;
    
    ImageView rotate3D = new ImageView(new Image(this.getClass().getResource("/org/pidome/client/appimages/floormap/navigation/3drotate.png").toExternalForm()));
    ImageView pan3D = new ImageView(new Image(this.getClass().getResource("/org/pidome/client/appimages/floormap/navigation/3dpan.png").toExternalForm()));
    ImageView zoom3D = new ImageView(new Image(this.getClass().getResource("/org/pidome/client/appimages/floormap/navigation/3dzoom.png").toExternalForm()));
    ImageView reset3D = new ImageView(new Image(this.getClass().getResource("/org/pidome/client/appimages/floormap/navigation/3dfloorreset.png").toExternalForm()));
    
    protected FloorsControl(FloorsViewManagement floorStage){
        super(20);
        this.floorStage = floorStage;
        rotate3D.setFitWidth(65);
        pan3D.setFitWidth(65);
        zoom3D.setFitWidth(65);
        reset3D.setFitWidth(65);
        getStyleClass().add("floors-control");
        this.setMinHeight(80);
    }
    
    protected final void build(){
        if(floorStage instanceof FloorsViewPort3D){
            build3DControls();
        } else {
            build2DControls();
        }
    }
    
    public void setFloorMoveEneabled(){
        setRotateActive(0);
    }
    
    protected void setRotateActive(Integer externalFloorId){
        Platform.runLater(() -> {
            rotate3D.setEffect(glow);
            zoom3D.setEffect(null);
            pan3D.setEffect(null);
            reset3D.setEffect(null);
        });
    }
    
    protected void setRotateActive(){
        setRotateActive(null);
    }
    
    protected void setFloorActiveListener(FloorActiveListener floorActiveListener){
        this.floorActiveListener = floorActiveListener;
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
                if(this.floorActiveListener != null){
                    this.floorActiveListener.setSelectedFloor(-1000);
                }
                t.consume();
            }
        });
        getChildren().addAll(reset3D, pan3D, rotate3D, zoom3D);
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
        getChildren().addAll(pan3D,zoom3D);
    }
    
}