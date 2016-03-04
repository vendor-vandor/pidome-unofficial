/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.quickappmenuitems;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;

/**
 *
 * @author John Sirach
 */
public abstract class QuickAppItem extends StackPane {

    Notification notification;
    
    String id   = "";
    String name = "";
    
    public QuickAppItem(String id, String name){
        this.id = id;
        this.name = name;
        getChildren().add(loadImage(id));
        this.setOnMouseClicked((MouseEvent me) -> {
            iconClicked(me);
        });
    }
    
    public abstract void clicked(double openX, double openY);
    
    final void iconClicked(MouseEvent me){
        clicked(me.getSceneX(), me.getSceneY());
    }
    
    public final void enableNotification(){
        notification = new Notification();
        StackPane.setAlignment(notification, Pos.TOP_RIGHT);
        getChildren().add(notification);
    }
    
    public final Notification notification(){
        return notification;
    }
    
    final ImageView loadImage(String imageId){
        Image iconImage = new Image(AppResources.getImage("menus/" + id + ".png"));
        ImageView image = new ImageView(iconImage);
        image.setFitWidth(iconImage.getWidth() * DisplayConfig.getWidthRatio());
        image.setFitHeight(iconImage.getHeight() * DisplayConfig.getHeightRatio());
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            image.setSmooth(true);
        }
        return image;
    }
    
    public final class Notification extends StackPane {
        
        IntegerProperty notificationAmount = new SimpleIntegerProperty(0);
        Label notifyAmount                 = new Label();
        
        public Notification(){
            setAlignment(Pos.TOP_RIGHT);
            setVisible(false);
            getChildren().addAll(loadImage(), notifyAmount);
            setMinWidth(19 * DisplayConfig.getWidthRatio());
            setMaxWidth(19 * DisplayConfig.getHeightRatio());
            setMinHeight(18 * DisplayConfig.getWidthRatio());
            setMaxHeight(18 * DisplayConfig.getHeightRatio());
            notificationAmount.addListener((ChangeListener)(ObservableValue observableValue, Object oldValue, Object newValue) -> {
                Integer newAmount = (Integer)newValue;
                if(newAmount>0){
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            });
            StackPane.setAlignment(notifyAmount, Pos.CENTER);
            notifyAmount.textProperty().bind(notificationAmount.asString());
        }
        
        final ImageView loadImage(){
            Image iconImage = new Image(AppResources.getImage("menus/notification/amount.png"));
            ImageView image = new ImageView(iconImage);
            image.setFitWidth(iconImage.getWidth() * DisplayConfig.getWidthRatio());
            image.setFitHeight(iconImage.getHeight() * DisplayConfig.getHeightRatio());
            if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
                image.setSmooth(true);
            }
            return image;
        }
        
        public final void add(Integer amount){
            Platform.runLater(() -> {
                if(notificationAmount.getValue()+amount > 99){
                    notificationAmount.setValue(99);
                } else {
                    notificationAmount.setValue(notificationAmount.getValue()+amount);
                }
            });
        }

        public final void substract(Integer amount){
            Platform.runLater(() -> {
                if(notificationAmount.getValue()-amount < 0){
                    notificationAmount.setValue(0);
                } else {
                    notificationAmount.setValue(notificationAmount.getValue()-amount);
                }
            });
        }
        
    }
    
}
