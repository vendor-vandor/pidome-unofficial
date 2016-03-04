/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.desktop;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import org.apache.logging.log4j.LogManager;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John Sirach
 */
public class DesktopPane extends DesktopBase {

    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DesktopPane.class);
    
    static DesktopPane instance = new DesktopPane();
    
    static TilePane desktopIconsPlane;
    
    private DesktopPane(){
        desktopIconsPlane = new TilePane(Orientation.VERTICAL, 10 * DisplayConfig.getWidthRatio(), 10 * DisplayConfig.getHeightRatio());
        desktopIconsPlane.setPadding(new Insets(5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio(), 5 * DisplayConfig.getHeightRatio(), 5 * DisplayConfig.getWidthRatio()));
        desktopIconsPlane.setAlignment(Pos.TOP_LEFT);
        desktopIconsPlane.setPrefHeight(DisplayConfig.getScreenHeight()-(150*DisplayConfig.getHeightRatio()));
        desktopIconsPlane.setMaxHeight(Region.USE_PREF_SIZE);
        desktopIconsPlane.getChildren().add(new DesktopDeletetionIcon().createIcon());
        addDefaultIcons();
    }
    
    public final TilePane getDesktopPlane(){
        return desktopIconsPlane;
    }
    
    public static DesktopPane getDesktop(){
        return instance;
    }
    
    public static void createShortcutDragStart(Node node){
        instance.getDesktopPlane().getChildren().add(node);
    }
    
    @Override
    void addIcon(DesktopIcon icon){
        Platform.runLater(() -> { if(!desktopIconsPlane.getChildren().contains(icon)) desktopIconsPlane.getChildren().add(icon); });
    }

    @Override
    void removeIcon(DesktopIcon icon){
        Platform.runLater(() -> { if(desktopIconsPlane.getChildren().contains(icon)) desktopIconsPlane.getChildren().remove(icon); });
    }
    
}