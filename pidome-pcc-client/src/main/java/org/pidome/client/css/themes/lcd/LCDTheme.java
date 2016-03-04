/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes.lcd;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.text.Font;
import org.pidome.client.css.themes.AppTheme;
import org.pidome.client.css.themes.DashboardTheme;
import org.pidome.client.css.themes.Theme.CodeSupports;

/**
 *
 * @author John
 */
public class LCDTheme extends AppTheme {

    DashboardTheme dash = new LCDDashboardTheme();
    
    static boolean fonIsLoaded = false;
    
    public LCDTheme(){
        if(!fonIsLoaded){
            Font.loadFont(this.getClass().getResource("/org/pidome/client/css/themes/lcd/digital.ttf").toExternalForm(), 17);
            fonIsLoaded = true;
        }
    }
    
    @Override
    public String getName() {
        return "PiDome LCD Theme";
    }

    @Override
    public String getDescription() {
        return "Having memories to the old days, or your LCD projects? Then this one is for you. Dashboard only!";
    }
    
    @Override
    public List<CodeSupports> getCodeSupports() {
        return new ArrayList<CodeSupports>(){{ add (CodeSupports.NONE ); }};
    }

    @Override
    public List<ThemeFeature> getThemeFeatures() {
        return new ArrayList<ThemeFeature>(){{ add (ThemeFeature.NONE); }};
    }
    
    @Override
    public String getCSSPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DashboardTheme getDashboardTheme() {
        return dash;
    }

    @Override
    public List<ThemeTypes> getThemeType() {
        return new ArrayList<ThemeTypes>(){{ add(ThemeTypes.DASHBOARD); }};
    }
 
}