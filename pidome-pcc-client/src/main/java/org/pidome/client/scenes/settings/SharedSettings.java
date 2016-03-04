/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.scenes.settings;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
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
public abstract class SharedSettings extends StackPane implements ScenePaneImpl {

    private PCCSystem system;
    private ServiceConnector serviceConnector;
    
    TilePane iconsPane = new TilePane();
    
    protected SharedSettings(){
        this.setPadding(new Insets(20));
        iconsPane.setHgap(20);
        iconsPane.setVgap(20);
        this.getChildren().addAll(iconsPane);
    }
    
    public final ServiceConnector getServiceConnector(){
        return this.serviceConnector;
    }
    
    public final PCCSystem getSystem(){
        return this.system;
    }
    
    protected final void addSetting(String name, GlyphIcons icon, Runnable runner){
        Label iconName = new Label(name);
        GlyphsDude.setIcon(iconName, icon, "3em", ContentDisplay.TOP);
        iconsPane.getChildren().add(iconName);
        iconName.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
            if(runner != null){
                runner.run();
            }
        });
    }
    
    @Override
    public final String getTitle() {
        return "Settings";
    }

    @Override
    public final void start() {
        Platform.runLater(() -> {
            composeSettings(serviceConnector); 
        });
    }

    protected abstract void composeSettings(ServiceConnector serviceConnector);
    
    @Override
    public final void close() {
        /////
    }

    @Override
    public final Pane getPane() {
        return this;
    }

    @Override
    public final void setSystem(PCCSystem system, ServiceConnector connector) {
        this.system = system;
        this.serviceConnector = connector;
    }

    @Override
    public final void removeSystem() {
        this.system = null;
        this.serviceConnector = null;
    }
    
}
