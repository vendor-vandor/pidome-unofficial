/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John
 */
public class ScreenDisplay {
    
    private static final int DEV_WIDTH = 1920;
    private static final int DEV_HEIGHT = 1080;
    private static final float DEV_RATIO = (float)DEV_WIDTH/(float)DEV_HEIGHT;
    
    /**
     * Stage width.
     */
    private static float stageWidth;
    /**
     * Stage height.
     */
    private static float stageHeight;
    
    private static float screenCenterX;
    private static float screenCenterY;
    
    private static float CUR_RATIO = 1.0f;

    private static float SCALE = 1f;
    
    static {
        Logger.getLogger(ScreenDisplay.class.getName()).setLevel(Level.ALL);
    }
    
    protected static void setDisplayData(float stageWidth, float stageHeight){
        ScreenDisplay.stageHeight = stageHeight;
        ScreenDisplay.stageWidth  = stageWidth;
        
        screenCenterX = stageWidth/2.0f;
        screenCenterY = stageHeight/2.0f;
        
        CUR_RATIO = stageWidth/stageHeight;
        
        createCurrentScale();
        
        Logger.getLogger(ScreenDisplay.class.getName()).log(Level.CONFIG, "Using resolution: width: {0}, height: {1}", new Object[]{ ScreenDisplay.stageWidth, ScreenDisplay.stageHeight });
        
    }
    
    private static void createCurrentScale(){
        float scale = 1f;
        if(DEV_HEIGHT > ScreenDisplay.stageHeight){
            scale = ScreenDisplay.stageHeight/DEV_HEIGHT;
        } else if(DEV_WIDTH > ScreenDisplay.stageWidth){
            scale = ScreenDisplay.stageWidth/DEV_WIDTH;
        }
        ScreenDisplay.SCALE = scale;
    }
    
    public static float getCurrentScreenRatio(){
        return CUR_RATIO;
    }
    
    public static float getDefaultScreenRatio(){
        return DEV_RATIO;
    }
    
    public static float getDefaultWidth(){
        return DEV_WIDTH;
    }

    public static float getDefaultHeight(){
        return DEV_HEIGHT;
    }
    
    public static float getStageWidth(){
        return stageWidth;
    }

    public static float getStageHeight(){
        return stageHeight;
    }
    
    
    public static float getStageCenterX(){
        return screenCenterX;
    }

    public static float getStageCenterY(){
        return screenCenterY;
    }
    
    public static float getCurrentScale(){
        return SCALE;
    }
    
}