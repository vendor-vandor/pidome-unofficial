/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.phone.scenes;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import org.pidome.client.phone.scenes.ScenesHandler.ScenePane;

/**
 *
 * @author John
 */
public final class EntitiesNavigationMenu extends TilePane {
    
    final ScenePane currentItem;
    ScenesSwitcher switcher;
    
    EntitiesNavigationMenu(ScenesSwitcher switcher, ScenePane currentItem){
        super(Orientation.HORIZONTAL);
        this.currentItem = currentItem;
        this.switcher = switcher;
        getStyleClass().add("entities-menu");
        setTileAlignment(Pos.CENTER);
        setPrefWidth(Double.MAX_VALUE);
        setHgap(10);
        setVgap(10);
        setPrefColumns(4);
        buildMenuButtons();
    }
    
    private void buildMenuButtons(){
        ImageView imageDash;
        if(currentItem.equals(ScenePane.DASHBOARD)){
            imageDash = new ImageView("/images/app/entitiesmenu/dashboard-active.png");
        } else {
            imageDash = new ImageView("/images/app/entitiesmenu/dashboard.png");
            imageDash.setPickOnBounds(true);
            imageDash.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                this.switcher.switchScene(ScenePane.DASHBOARD);
                this.switcher = null;
            });
        }
        imageDash.setFitHeight(60);
        imageDash.setPreserveRatio(true);
        
        
        ImageView imageDevice;
        if(currentItem.equals(ScenePane.DEVICES)){
            imageDevice = new ImageView("/images/app/entitiesmenu/devices-active.png");
        } else {
            imageDevice = new ImageView("/images/app/entitiesmenu/devices.png");
            imageDevice.setPickOnBounds(true);
            imageDevice.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                this.switcher.switchScene(ScenePane.DEVICES);
                this.switcher = null;
            });
        }
        imageDevice.setFitHeight(60);
        imageDevice.setPreserveRatio(true);
        
        ImageView imageMedia;
        if(currentItem.equals(ScenePane.MEDIA)){
            imageMedia = new ImageView("/images/app/entitiesmenu/media-active.png");
        } else {
            imageMedia = new ImageView("/images/app/entitiesmenu/media.png");
            imageMedia.setPickOnBounds(true);
            imageMedia.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                this.switcher.switchScene(ScenePane.MEDIA);
                this.switcher = null;
            });
        }
        imageMedia.setFitHeight(60);
        imageMedia.setPreserveRatio(true);
        
        ImageView imageMacroEvents;
        if(currentItem.equals(ScenePane.MACROS_EVENTS)){
            imageMacroEvents = new ImageView("/images/app/entitiesmenu/macros_media-active.png");
        } else {
            imageMacroEvents = new ImageView("/images/app/entitiesmenu/macros_media.png");
            imageMacroEvents.setPickOnBounds(true);
            imageMacroEvents.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent event) -> {
                this.switcher.switchScene(ScenePane.MACROS_EVENTS);
                this.switcher = null;
            });
        }
        imageMacroEvents.setFitHeight(60);
        imageMacroEvents.setPreserveRatio(true);
        
        
        getChildren().addAll(imageDash, imageDevice, imageMedia, imageMacroEvents);
    }
    
}