/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.dashboard;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.pidome.client.entities.dashboard.DashboardItem;
import org.pidome.client.entities.dashboard.DashboardSpacerItem;
import org.pidome.client.services.ServiceConnector.DisplayType;
import org.pidome.client.system.PCCSystem;
import org.pidome.client.tools.DisplayTools;

/**
 *
 * @author John
 */
public final class VisualDashboardSpacerItem extends VisualDashboardItem {

    protected VisualDashboardSpacerItem(PCCSystem system, DashboardItem item) {
        super(system, item);
        this.getStyleClass().add("dashboard-spacer");
    }

    private DashboardSpacerItem getSpacerItem(){
        return (DashboardSpacerItem)this.getDashboardItem();
    }
    
    @Override
    protected void build() {
        double newHeight = this.getPaneHeight()/2;
        if(getSpacerItem().isHeader()){
            this.getStyleClass().add("underlined");
            Text spacerHeader = new Text(getSpacerItem().getHeaderText());
            spacerHeader.getStyleClass().add("spacer-header-text");
            StackPane.setAlignment(spacerHeader, Pos.BOTTOM_LEFT);
            GridPane.setValignment(this, VPos.BOTTOM);
            spacerHeader.setWrappingWidth(getPaneWidth());
            if(DisplayTools.getUserDisplayType()==DisplayType.SMALL || DisplayTools.getUserDisplayType()==DisplayType.TINY){
                this.setPrefHeight(newHeight);
                this.setMinHeight(USE_PREF_SIZE);
                this.setMaxHeight(USE_PREF_SIZE);
            }
            this.getChildren().add(spacerHeader);
        }
    }

    @Override
    protected void destruct() {
        /// not needed.
    }
    
}
