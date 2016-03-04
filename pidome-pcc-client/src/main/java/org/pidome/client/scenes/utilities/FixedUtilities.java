/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.utilities;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import org.pidome.client.scenes.ScenePaneImpl;
import org.pidome.client.services.ServiceConnector;
import org.pidome.client.system.PCCSystem;

/**
 *
 * @author John
 */
public class FixedUtilities extends StackPane implements ScenePaneImpl {

    TilePane iconsPane = new TilePane();
    
    public FixedUtilities(){
        this.setPadding(new Insets(20));
        StackPane.setAlignment(iconsPane, Pos.TOP_LEFT);
        iconsPane.setHgap(20);
        iconsPane.setVgap(20);
        this.getChildren().addAll(iconsPane, new Label("Not yet available"));
    }
    
    
    @Override
    public String getTitle() {
        return "Utilities";
    }

    @Override
    public void start() {
        iconsPane.getChildren().add(composeIcon("Timer", MaterialDesignIcon.TIMER));
    }

    private Label composeIcon(String name, GlyphIcons icon){
        Label iconName = new Label(name);
        GlyphsDude.setIcon(iconName, icon, "3em", ContentDisplay.TOP);
        return iconName;
    }
    
    @Override
    public void close() {
        ////
    }

    @Override
    public Pane getPane() {
        return this;
    }

    @Override
    public void setSystem(PCCSystem system, ServiceConnector connector) {
        //
    }

    @Override
    public void removeSystem() {
        ///
    }
    
}