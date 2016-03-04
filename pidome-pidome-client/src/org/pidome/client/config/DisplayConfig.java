/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.config;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John Sirach
 */
public final class DisplayConfig {

    static Logger LOG = LogManager.getLogger(DisplayConfig.class);
    
    static ObservableList<Screen> screens = Screen.getScreens();
    
    public final static double BottomBarHeightPercentage = 20;
    
    public final static String QUALITY_HIGH   = "high";
    public final static String QUALITY_MEDIUM = "medium";
    public final static String QUALITY_LOW    = "low";
    
    public final static String RUNMODE_DEFAULT = "default";
    public final static String RUNMODE_WIDGET  = "widget";
    
    static String QUALITY;
    
    static final double defaultWidth = 1920;
    static final double defaultHeight= 1080;
    
    static double screenPosX = 0; /// depending on second screen
    static double screenPosY = 0; /// always
    
    static final Rectangle2D primaryScreenBounds = Screen.getPrimary().getBounds();
    
    static double curScreenWidth  = primaryScreenBounds.getWidth();
    static double curScreenHeight = primaryScreenBounds.getHeight();

    static double ratioX = 1.0;
    static double ratioY = 1.0;
    
    static double defaultDpi = 96.0;
    static double dpi = 0.0;
    
    static double defaultFontSize = 13;
    
    static double usedFontSize = 0;
    
    static String runMode = RUNMODE_DEFAULT;
    
    public static void setStageProps(Stage primaryStage){
        try {
            try {
                QUALITY = AppProperties.getProperty("system", "display.quality");
            } catch (AppPropertiesException ex){
                LOG.error("Problem setting quality, defaulting: " + ex.getLocalizedMessage());
                QUALITY = QUALITY_LOW;
            }
            if(AppProperties.getProperty("system", "client.mode").equals(RUNMODE_WIDGET)){
                runMode = RUNMODE_WIDGET;
                QUALITY = QUALITY_HIGH;
                setWidgetAppProps(primaryStage);
            } else {
                setDefaultAppProps(primaryStage);
            }
        } catch (AppPropertiesException ex) {
            setDefaultAppProps(primaryStage);
        }
        LOG.debug("Screen properties: runMode: {}, posX: {}, posY: {}, width: {}, height: {}, dpi: {}, ratioX: {}, ratioY: {}, scaled font size: {}, quality: {}", runMode,screenPosX, screenPosY, curScreenWidth,curScreenHeight, dpi, ratioX,ratioY,getFontDpiScaler(),QUALITY);
    }
    
    static void setWidgetAppProps(Stage primaryStage){
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        screenPosX = curScreenWidth+1;
        screenPosY = curScreenHeight+1;
        primaryStage.setX(screenPosX);
        primaryStage.setY(screenPosY);
        ratioX = 1.0;
        ratioY = 1.0;
        dpi = Screen.getPrimary().getDpi();
    }
    
    static void setDefaultAppProps(Stage primaryStage){
        primaryStage.initStyle(StageStyle.UNDECORATED);
        LOG.debug("Got {} screens", screens.size());
        //// Here we will be checking if there is a second screen, and if there is force the app to go there
        if(screens.size()>1){
            for (Screen curScreen : screens) {
                LOG.debug("Current screen check is primary: {}", curScreen.equals(Screen.getPrimary()));
                if(!curScreen.equals(Screen.getPrimary())) {
                    LOG.debug("Going secondary screen");
                    Rectangle2D newScreenBounds = curScreen.getBounds();
                    screenPosX = newScreenBounds.getMinX();
                    screenPosY = newScreenBounds.getMinY();
                    curScreenWidth = newScreenBounds.getWidth();
                    curScreenHeight = newScreenBounds.getHeight();
                    dpi = curScreen.getDpi();
                    primaryStage.setWidth(curScreenWidth);
                    primaryStage.setHeight(curScreenHeight);
                    primaryStage.setX(screenPosX);
                    primaryStage.setY(screenPosY);
                    primaryStage.toFront();
                    break;
                }
            }
        } else {
            LOG.debug("Staying primary");
            dpi = Screen.getPrimary().getDpi();
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreen(true);
        }
        ratioX = (1/defaultWidth) * curScreenWidth;
        ratioY = (1/defaultHeight) * curScreenHeight;
    }
    
    public static String getRunMode(){
        return runMode;
    }
    
    /**
     * Return the font size based on DPI and screen sizes.
     * We develop on 1920*1080 width font size 12. The lesser pixels there are on the screen the bigger the fonts get. so this is scaled.
     * When resolution changes smaller resolutions give bigger fonts. So this also needs to scale.
     * @return 
     */
    public static double getFontDpiScaler(){
        if(usedFontSize==0) usedFontSize = (int)Math.ceil(((defaultFontSize/100)*((100/defaultDpi)*dpi))*ratioX);
        return usedFontSize;
    }
    
    public static double getScreenXPos(){
        return screenPosX;
    }
    
    public static double getScreenWidth(){
        return curScreenWidth;
    }
    
    public static double getScreenHeight(){
        return curScreenHeight;
    }
    
    public static double getWidthRatio(){
        return ratioX;
    }
    
    public static double getHeightRatio(){
        return ratioY;
    }
    
    public static String getQuality(){
        if(QUALITY==null){
            return QUALITY_HIGH;
        }
        return QUALITY;
    }

    public static void setQuality(String quality){
        QUALITY = quality;
    }
    
}
