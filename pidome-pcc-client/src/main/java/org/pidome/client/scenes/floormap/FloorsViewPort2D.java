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

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.pidome.client.entities.floormap.FloorMapService;
import org.pidome.client.tools.ImgTools;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
final class FloorsViewPort2D extends FloorsViewManagement {
    
    double width, height;
    
    public FloorsViewPort2D(FloorMapService floorMapService, double width, double height){
        super(floorMapService);
        this.width = width;
        this.height = height;
        pleaseWait.setPadding(new Insets(30));
        getChildren().add(pleaseWait);
        try {
            if(!isSupported()){
                pleaseWait.setText("To use this functionality go to the server's interface\n and create a visual floor plan.");
            } else {
                pleaseWait.setText("Please wait, loading maps");
            }
        } catch (VisualFloorsAssetItemException ex) {
            pleaseWait.setText(ex.getMessage());
        }
    }
    
    @Override
    protected void buildScene() {
        try {
            getChildren().add(new ImageView(ImgTools.getJavaFXImage(getLowestFloor().loadFloorAsset())));
            getChildren().remove(pleaseWait);
        } catch (PCCEntityDataHandlerException ex) {
            Logger.getLogger(FloorsViewPort2D.class.getName()).log(Level.SEVERE, null, ex);
            Platform.runLater(() -> { pleaseWait.setText("Could not load floors");  });
        }
    }

    @Override
    void setMoveStyle(Move style) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void setFloorActive(int floorId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    boolean hasActiveFloor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void showRegions(boolean show, boolean initial) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void showFloorImage(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    boolean isAnimating() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void showRoomNames(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void destroy() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void showDevices(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void animateDevices(boolean animate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void deviceInfluences(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    int getActiveFloorId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void movementVisualization(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void luxVisualization(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void tempVisualization(boolean show) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
