/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.pidome.client.css.themes.Theme.CodeSupports;
import org.pidome.client.css.themes.Theme.ThemeFeature;
import org.pidome.client.css.themes.lcd.LCDTheme;
import org.pidome.client.css.themes.light.LightTheme;
import org.pidome.client.css.themes.standard.StandardTheme;
import org.pidome.pcl.storage.preferences.LocalPreferenceStorage;

/**
 *
 * @author John
 */
public class Themes {
    
    private AppTheme currentTheme = new StandardTheme();
    
    private Scene scene;
    
    private String dashPath;
    private String appPath;
    
    private LocalPreferenceStorage prefs;
    
    public final List<AppTheme> getRegisteredThemes(){
        return new ArrayList<AppTheme>(){{ add(new StandardTheme()); add(new LCDTheme()); add(new LightTheme()); }};
    }
    
    public final void setPreferences(LocalPreferenceStorage prefs){
        this.prefs = prefs;
    }
    
    public final void loadPrefTheme(){
        if(this.prefs!=null){
            String theme = this.prefs.getStringPreference("org.pidome.client.theme", "");
            String appScheme = this.prefs.getStringPreference("org.pidome.client.theme.app.scheme", "");
            String dashScheme = this.prefs.getStringPreference("org.pidome.client.theme.dashboard.scheme", "");
            AppTheme newTheme = null;
            if(!theme.equals("") && !currentTheme.getClass().getCanonicalName().equals(theme)){
                switch(theme){
                    case "org.pidome.client.css.themes.lcd.LCDTheme":
                        newTheme = new LCDTheme();
                    break;
                    case "org.pidome.client.css.themes.light.LightTheme":
                        newTheme = new LightTheme();
                    break;
                    default:
                        newTheme = new StandardTheme();
                    break;
                }
            }
            if(newTheme!=null){
                newTheme.setDisplayOption(appScheme);
                newTheme.getDashboardTheme().setDisplayOption(dashScheme);
                if(scene!=null){
                    this.switchTheme(newTheme, false);
                } else {
                    currentTheme = newTheme;
                }
            }
        }
    }
    
    public final void setScene(Scene scene){
        this.scene = scene;
        setSceneLink(scene);
    }
    
    private void setSceneLink(Scene scene){
        currentTheme.sceneLink(scene);
        currentTheme.getDashboardTheme().sceneLink(scene);
    }
    
    public void switchTheme(AppTheme theme, boolean saveSettings){
        if(saveSettings){
            this.prefs.setStringPreference("org.pidome.client.theme", theme.getClass().getCanonicalName());
            this.prefs.setStringPreference("org.pidome.client.theme.app.scheme", theme.getSetDisplayOption());
            this.prefs.setStringPreference("org.pidome.client.theme.dashboard.scheme", theme.getDashboardTheme().getSetDisplayOption());
            try {
                this.prefs.storePreferences("Save by theme selector");
            } catch (IOException ex) {
                Logger.getLogger(Themes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        unsetApp();
        unsetDashboard();
        currentTheme.unLinkScene();
        currentTheme.getDashboardTheme().unLinkScene();
        dashPath = null;
        appPath = null;
        currentTheme = theme;
        setSceneLink(scene);
        applyApp();
    }
    
    public final AppTheme getCurrentTheme(){
        return currentTheme;
    }
    
    public final void setTheme(AppTheme theme){
        this.currentTheme = theme;
    }
    
    public final Map<String,Map<String,String>> getDisplayOptions(){
        Map<String,Map<String,String>> fullSet = new HashMap<>();
        fullSet.put("APP", new HashMap<>());
        fullSet.put("DASHBOARD", new HashMap<>());
        if(currentTheme.getThemeFeatures().contains(ThemeFeature.DISPLAY_OPTIONS)){
            fullSet.put("APP", currentTheme.getDisplayOptions());
        }
        if(currentTheme.getDashboardTheme().getThemeFeatures().contains(ThemeFeature.DISPLAY_OPTIONS)){
            fullSet.put("DASHBOARD", currentTheme.getDashboardTheme().getDisplayOptions());
        }
        return fullSet;
    }
    
    
    public final void setDisplayOptions(Map<String,String> options){
        if(currentTheme.getThemeFeatures().contains(ThemeFeature.DISPLAY_OPTIONS)){
            currentTheme.setDisplayOption(options.get("APP"));
        }
        if(currentTheme.getDashboardTheme().getThemeFeatures().contains(ThemeFeature.DISPLAY_OPTIONS)){
            currentTheme.getDashboardTheme().setDisplayOption(options.get("DASHBOARD"));
        }
    }
    
    public final void unsetApp(){
        if(currentTheme.getThemeType().contains(AppTheme.ThemeTypes.APP)){
            if(appPath==null) appPath = getClass().getResource(currentTheme.getCSSPath()).toExternalForm();
            if(scene.getStylesheets().contains(appPath)){
                scene.getStylesheets().remove(appPath);
                clearDisplayOptions(currentTheme);
            }
        } else {
            if(appPath==null) appPath = getClass().getResource("/org/pidome/client/css/themes/standard/app.css").toExternalForm();
            if(scene.getStylesheets().contains(appPath)){
                scene.getStylesheets().remove(appPath);
            }
        }
    }
    
    public final void applyApp(){
        if(currentTheme.getThemeType().contains(AppTheme.ThemeTypes.APP)){
            if(appPath==null) appPath = getClass().getResource(currentTheme.getCSSPath()).toExternalForm();
            if(!scene.getStylesheets().contains(appPath)){
                applyDisplayOptions(currentTheme);
                scene.getStylesheets().add(appPath);
                applySceneComponents(currentTheme);
            }
        } else {
            if(appPath==null) appPath = getClass().getResource("/org/pidome/client/css/themes/standard/app.css").toExternalForm();
            if(!scene.getStylesheets().contains(appPath)){
                scene.getStylesheets().add(appPath);
            }
        }
    }
    
    public final void applyDashboard(){
        if(currentTheme.getThemeType().contains(AppTheme.ThemeTypes.DASHBOARD)){
            if(dashPath == null ) dashPath = getClass().getResource(currentTheme.getDashboardTheme().getCSSPath()).toExternalForm();
            if(!scene.getStylesheets().contains(dashPath)){
                applyDisplayOptions(currentTheme.getDashboardTheme());
                scene.getStylesheets().add(dashPath);
                applySceneComponents(currentTheme.getDashboardTheme());
            }
        } else {
            if(dashPath == null ) dashPath = getClass().getResource("/org/pidome/client/css/themes/standard/dashboard.css").toExternalForm();
            if(!scene.getStylesheets().contains(dashPath)){
                scene.getStylesheets().add(dashPath);
            }
        }
    }
    
    public final void unsetDashboard(){
        if(scene!=null){
            if(currentTheme.getThemeType().contains(AppTheme.ThemeTypes.DASHBOARD)){
                if(dashPath == null ) dashPath = getClass().getResource(currentTheme.getDashboardTheme().getCSSPath()).toExternalForm();
                if(scene.getStylesheets().contains(dashPath)){
                    scene.getStylesheets().remove(dashPath);
                    clearDisplayOptions(currentTheme.getDashboardTheme());
                }
            } else {
                if(dashPath == null ) dashPath = getClass().getResource("/org/pidome/client/css/themes/standard/dashboard.css").toExternalForm();
                if(scene.getStylesheets().contains(dashPath)){
                    scene.getStylesheets().remove(dashPath);
                }
            }
        }
    }
    
    private void clearDisplayOptions(Theme theme){
        if(!theme.getThemeFeatures().contains(Theme.ThemeFeature.NONE)){
            if(theme.getThemeFeatures().contains(Theme.ThemeFeature.DISPLAY_OPTIONS)){
                theme.clearDisplayOptions();
            }
        }
    }
    
    private void applyDisplayOptions(Theme theme){
        if(!theme.getThemeFeatures().contains(Theme.ThemeFeature.NONE)){
            if(theme.getThemeFeatures().contains(Theme.ThemeFeature.DISPLAY_OPTIONS)){
                theme.applyDisplayOptions();
            }
        }
    }
    
    private void applySceneComponents(Theme theme){
        if(!theme.getCodeSupports().contains(CodeSupports.NONE)){
            if(theme.getCodeSupports().contains(CodeSupports.BACKGOUND)){
                final Region root = ((StackPane)scene.getRoot());
                Image img = theme.getBackGroundImage(scene.getWidth(), scene.getHeight());
                Platform.runLater(() -> { 
                    root.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,new BackgroundSize(scene.getWidth(), scene.getHeight(), false, false, false, true))));
                });
            }
        }
    }
    
}