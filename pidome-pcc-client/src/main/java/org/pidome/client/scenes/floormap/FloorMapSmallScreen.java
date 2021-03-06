/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.floormap;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.scenes.ScenesHandler;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class FloorMapSmallScreen implements ScenePaneImpl {
    
    FloorsViewManagement floorWindow;
    FloorsControl floorsControl;
    
    BorderPane rootPane = new BorderPane();
    
    final double maxWidth;
    final double maxHeight;
    
    final double maxViewPortWidth;
    final double maxViewPortHeight;
    
    PCCSystem system;
    
    
    public FloorMapSmallScreen(){
        maxWidth  = ScenesHandler.getContentWidthProperty().doubleValue();
        maxHeight = ScenesHandler.getContentHeightProperty().doubleValue();

        maxViewPortWidth = maxWidth - 440;
        maxViewPortHeight = maxHeight - 90;
        
        rootPane.getStyleClass().add("floormap");
        
    }
    
    @Override
    public String getTitle() {
        return "Floor map";
    }

    @Override
    public void start() {
        try {
            if(Platform.isSupported(ConditionalFeature.SCENE3D)){
                floorWindow = new FloorsViewPort3D(system.getClient().getEntities().getFloorMapService(), maxViewPortWidth-2, maxViewPortHeight-2);
                ((FloorsViewPort3D)floorWindow).setExternalFloorActiveListener(floorsControl);
            } else {
                floorWindow = new FloorsViewPort2D(system.getClient().getEntities().getFloorMapService(),maxViewPortWidth-2, maxViewPortHeight-2);
            }
            floorsControl = new FloorsControl(floorWindow);
            
            rootPane.setCenter(floorWindow);
            rootPane.setBottom(floorsControl);
            
            floorWindow.setMoveEnabledListener(floorsControl);
            
            floorsControl.setAlignment(Pos.CENTER);
            floorsControl.build();
            floorWindow.build();
            
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(FloorMapLargeScreen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() {
        floorWindow.destroy();
    }


    @Override
    public Pane getPane() {
        return rootPane;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
    }

    @Override
    public void removeSystem() {
        this.system = null;
    }
    
}