/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.DragWindow;
import org.pidome.client.system.scenes.components.mainstage.quickappmenuitems.QuickAppClockButton;
import org.pidome.client.system.scenes.components.mainstage.quickappmenuitems.QuickAppItem;
import org.pidome.client.system.scenes.components.mainstage.quickappmenuitems.QuickAppMailButton;
import org.pidome.client.system.scenes.components.mainstage.quickappmenuitems.QuickAppMessagesButton;
import org.pidome.client.system.scenes.components.mainstage.quickappmenuitems.QuickAppUtilityUsagesButton;
import org.pidome.client.system.scenes.components.mainstage.quickappmenuitems.QuickAppTemperatureButton;

/**
 *
 * @author John Sirach
 */
public class QuickAppMenu extends StackPane {

    TranslateTransition translateTransition;
    
    double locationX = 250 * DisplayConfig.getWidthRatio();
    double locationY = (DisplayConfig.getScreenHeight() - 80 * DisplayConfig.getHeightRatio());
    
    List<String> menuItems = new ArrayList();
    Map<String,QuickAppItem> menuButtons = new HashMap<>();
    
    Insets position = new Insets(0,0,35 * DisplayConfig.getHeightRatio(),20 * DisplayConfig.getWidthRatio());
    
    static QuickAppItem messages = new QuickAppMessagesButton();
    static QuickAppItem mail     = new QuickAppMailButton();
    static QuickAppItem utilusage= new QuickAppUtilityUsagesButton();
    static QuickAppItem clock    = new QuickAppClockButton();
    static QuickAppItem temp     = new QuickAppTemperatureButton();
    
    DragWindow draggable = new DragWindow(this);
    
    public QuickAppMenu (){
        relocate(locationX,locationY);
        if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_DEFAULT)){
            createMenuBox();
        } else {
            setMaxHeight(80);
            createWidgetBox();
        }
    }
    
    public static QuickAppMessagesButton getMessagesButton(){
        return (QuickAppMessagesButton)messages;
    }
    
    final void createWidgetBox(){
        TilePane skeleton = new TilePane();
        skeleton.setMaxWidth(190);
        messages.setScaleX(0.8);messages.setScaleY(0.8);
        mail.setScaleX(0.8);mail.setScaleY(0.8);
        utilusage.setScaleX(0.8);utilusage.setScaleY(0.8);
        clock.setScaleX(0.8);clock.setScaleY(0.8);
        temp.setScaleX(0.8);temp.setScaleY(0.8);
        skeleton.getChildren().addAll(messages, mail, utilusage, clock, temp);
        getChildren().add(skeleton);
    }
    
    final void createMenuBox(){
        Image iconImage = new Image(AppResources.getImage("menus/main_menu.png"));
        ImageView image = new ImageView(iconImage);
        image.setFitWidth(iconImage.getWidth() * DisplayConfig.getWidthRatio());
        image.setFitHeight(iconImage.getHeight() * DisplayConfig.getHeightRatio());
        HBox skeleton = new HBox();
        HBox.setMargin(messages, position);
        HBox.setMargin(mail, position);
        HBox.setMargin(utilusage, position);
        HBox.setMargin(clock, position);
        HBox.setMargin(temp, position);
        HBox.setHgrow(this, Priority.ALWAYS);
        if(DisplayConfig.getQuality().equals(DisplayConfig.QUALITY_HIGH)){
            image.setSmooth(true);
        }
        skeleton.getChildren().addAll(messages,mail,utilusage,clock,temp);
        skeleton.setTranslateX(43 * DisplayConfig.getWidthRatio());
        getChildren().addAll(image,skeleton);
    }
    
}
