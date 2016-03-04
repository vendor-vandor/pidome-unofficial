/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import org.pidome.pcl.networking.connections.server.ServerConnection.Profile;

/**
 *
 * @author John
 */
public abstract class Theme {
    
    private Scene scene;
    
    public final void sceneLink(Scene scene){
        this.scene = scene;
    }
    
    public final void unLinkScene(){
        this.scene = null;
    }
    
    public final Scene getSceneLink(){
        return this.scene;
    }
    
    public enum CodeSupports {
        NONE,
        BACKGOUND;
    }
    
    public enum ThemeFeature {
        NONE,
        EMULATE_BRIGHTNESS,
        DISPLAY_OPTIONS
    }
    
    public List<Profile>supportsProfile(){
        return new ArrayList<Profile>(){{ add ( Profile.FIXED ); }};
    }
    
    public void setDisplayOption(String selectedOption){
        /// overwrite when you have it.
    }
    
    public String getSetDisplayOption(){
        return "";
    }
    
    public Map<String,String> getDisplayOptions(){
        return new HashMap<>();
    }
    
    public void applyDisplayOptions(){}
    
    public void clearDisplayOptions(){}
    
    public abstract List<ThemeFeature> getThemeFeatures();
    
    private Theme dashboard;
    
    public abstract List<CodeSupports> getCodeSupports();
    
    public abstract String getCSSPath();
    
    public Image getBackGroundImage(double width, double height){
        return null;
    }
    
}