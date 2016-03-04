/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system.scenes.components.mainstage.desktop;

import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.config.AppResources;
import org.pidome.client.config.DisplayConfig;
import org.pidome.client.system.scenes.components.helpers.ImageLoader;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class DesktopIcon extends DraggableIcon implements DesktopIconDeletableInterface {

    static Logger LOG = LogManager.getLogger(DesktopIcon.class);
    
    public final static String FOLDER = "FOLDER";
    public final static String ITEM   = "ITEM";
    public final static String MACRO  = "MACRO";
    public final static String GRAPH  = "GRAPH";
    public final static String DEVICE = "DEVICE";
    
    String iconType = ITEM;
    
    String icon = "";
    String iconName;
    Text iconText;
    String classPath;
    Object[] params;
    
    ImageLoader iconImage;
    
    private String style;
    
    Pane plane;
    
    public DesktopIcon(DraggableIconInterface source, String iconType, String iconName, String windowClassPath, List classParams, String Style) {
        this(source,iconType,iconName,windowClassPath,classParams);
        style = Style;
    }
    
    public DesktopIcon(DraggableIconInterface source, String iconType, String iconName, String windowClassPath, List classParams) {
        super(source);
        this.iconType = iconType;
        this.iconName = iconName;
        this.classPath= windowClassPath;
        if(style==null)this.style=DisplayConfig.getRunMode();
        if(style.equals(DisplayConfig.RUNMODE_WIDGET)){
            plane = new HBox();
            ((HBox)plane).setAlignment(Pos.TOP_LEFT);
            ((HBox)plane).setSpacing(2);
        } else {
            plane = new VBox();
            ((VBox)plane).setAlignment(Pos.TOP_CENTER);
        }
        plane.getStyleClass().add(".icon");
        params = new String[classParams.size()];
        for(int i = 0; i<classParams.size();i++){
            params[i]=((String)classParams.get(i)).trim();
        }
    }
    
    public final void setIcon(String icon){
        this.icon = icon;
    }
    
    public final void updateName(String name){
        Platform.runLater(() -> { this.iconText.setText(name); });
    }

    final void loadDefaultIcon(String iconType){
        if(new File(AppResources.getImagePath("desktop/"+iconType+".png")).exists()){
            if(style.equals(DisplayConfig.RUNMODE_WIDGET)){
                iconImage = new ImageLoader("desktop/"+iconType+".png", 15, 15);
            } else {
                iconImage = new ImageLoader("desktop/"+iconType+".png", 63, 63);
            }
        } else {
            if(style.equals(DisplayConfig.RUNMODE_WIDGET)){
                iconImage = new ImageLoader("desktop/item-icon.png", 15, 15);
            } else {
                iconImage = new ImageLoader("desktop/item-icon.png", 63, 63);
            }            
        }
    }
    
    final void loadDeviceIcon(String icon){
        if(new File(AppResources.getImagePath("device_cat/"+icon+".png")).exists()){
            if(style.equals(DisplayConfig.RUNMODE_WIDGET)){
                iconImage = new ImageLoader("device_cat/"+icon+".png", 15, 15);
            } else {
                iconImage = new ImageLoader("device_cat/"+icon+".png", 63, 63);
            }
        } else {
            loadDefaultIcon("unknown-icon");
        }
    }
    
    final void createIcon(){
        switch(iconType){
            case DEVICE:
                System.out.println(this.icon + "-icon");
                loadDeviceIcon(this.icon + "-icon");
            break;
            default:
                loadDefaultIcon(iconType.toLowerCase() + "-icon");
            break;
        }
        ImageView image = new ImageView(iconImage.getImage());
        image.setScaleX(DisplayConfig.getWidthRatio());
        image.setScaleY(DisplayConfig.getHeightRatio());
        plane.getChildren().add(image);
        iconText = new Text(iconName);
        if(style.equals(DisplayConfig.RUNMODE_WIDGET)){
            iconText.setWrappingWidth(140);
        } else {
            iconText.setWrappingWidth(100*DisplayConfig.getWidthRatio());
            iconText.setTextAlignment(TextAlignment.CENTER);
        }
        iconText.setStyle("-fx-fill: #d2d2d2;");
        if(DisplayConfig.getRunMode().equals(DisplayConfig.RUNMODE_DEFAULT)){
            DropShadow ds = new DropShadow();
            ds.setRadius(6);
            ds.setBlurType(BlurType.ONE_PASS_BOX);
            ds.setSpread(1.0);
            ds.setColor(Color.web("#000000"));
            iconText.setEffect(ds);
        }
        plane.getChildren().add(iconText);
        getChildren().add(plane);
        setClickHandler();
    }
    
    final void setClickHandler(){
        addEventHandler(MouseEvent.MOUSE_CLICKED, this::windowOpenerHelper);
    }
    
    final void destroy(){
        removeEventHandler(MouseEvent.MOUSE_CLICKED, this::windowOpenerHelper);
    }
    
    final void windowOpenerHelper(MouseEvent me){
        if(!dragging())openWindow(me.getSceneX(), me.getSceneY());
    }
    
    final void openWindow(double openX, double openY){
        try {
            WindowManager.openCreateWindow(classPath, iconName, params, openX, openY);
        } catch (Exception ex){
            /// Some icons do not have windows.
        }
    }
    
}
