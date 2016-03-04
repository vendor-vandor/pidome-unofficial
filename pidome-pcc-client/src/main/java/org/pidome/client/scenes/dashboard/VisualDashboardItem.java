/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.scenes.dashboard.svg.SVGBase;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public abstract class VisualDashboardItem extends StackPane {
    
    private final DashboardItem item;
    protected final double width;
    protected final double height;
    
    private PCCSystem system;
    
    protected VisualDashboardItem(PCCSystem system, DashboardItem item){
        this.system = system;
        this.getStyleClass().add("dashboard-tile");
        this.item = item;
        width = (this.item.getSizeX() * 90) + ((this.item.getSizeX()-1)*10);
        height = (this.item.getSizeY() * 90) + ((this.item.getSizeY()-1)*10);
        this.setPrefSize(width,height);
        this.setMinSize(USE_PREF_SIZE,USE_PREF_SIZE);
        this.setMaxSize(USE_PREF_SIZE,USE_PREF_SIZE);
    }
    
    protected final PCCSystem getSystem(){
        return system;
    }
    
    protected abstract void build();
    
    protected abstract void destruct();
    
    protected final void removeSystem(){
        this.system = null;
    }
    
    protected final double getPaneWidth(){
        return this.width;
    }

    protected final double getPaneHeight(){
        return this.height;
    }
    
    protected final void setBackGround(SVGBase background){
        background.build(width, height);
        getChildren().add(0, background.getSVG());
    }
    
    protected final void removeBackground(){
        if(getChildren().size()>0 && getChildren().get(0) instanceof SVGBase){
            getChildren().remove(0);
        }
    }
    
    protected final void setContent(Pane pane){
        pane.setMinSize(width,height);
        pane.setMaxSize(width,height);
        pane.setPrefSize(width,height);
        Platform.runLater(() -> {
            getChildren().add(pane);
        });
    }
    
    protected final void removeContent(Pane pane){
        Platform.runLater(() -> {
            getChildren().remove(pane);
        });
    }
    
    protected final DashboardItem getDashboardItem(){
        return this.item;
    }
    
    protected final double calcFontSize(double baseSize, boolean useAbsGap){
        /// We have a 10 pixel margin between each block size, size 1 is no gap, size 2 is a 10 pixel gap needed in consideration to left and height.
        double defaultGrow = (this.item.getSizeY() * baseSize);
        if(this.item.getSizeY() > 1){
            if(useAbsGap){
                return defaultGrow + (this.item.getSizeY() * 10);
            } else {
                return defaultGrow + (defaultGrow*((1/75)*10));
            }
        } else {
            return defaultGrow;
        }
        
    }
    
}