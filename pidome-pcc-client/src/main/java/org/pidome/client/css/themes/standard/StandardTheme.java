/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes.standard;

import java.util.ArrayList;
import java.util.List;
import org.pidome.client.css.themes.AppTheme;
import org.pidome.client.css.themes.DashboardTheme;
import org.pidome.client.css.themes.Theme.CodeSupports;

/**
 *
 * @author John
 */
public class StandardTheme extends AppTheme {

    DashboardTheme themeDash = new StandardDashboardTheme();
    
    @Override
    public String getCSSPath() {
        return "/org/pidome/client/css/themes/standard/app.css";
    }

    @Override
    public List<CodeSupports> getCodeSupports() {
        return new ArrayList<CodeSupports>(){{ add (CodeSupports.NONE); }};
    }

    @Override
    public DashboardTheme getDashboardTheme() {
        return themeDash;
    }

    @Override
    public List<ThemeTypes> getThemeType() {
        return new ArrayList<ThemeTypes>(){{ add(ThemeTypes.APP); add(ThemeTypes.DASHBOARD); }};
    }

    @Override
    public List<ThemeFeature> getThemeFeatures() {
        return new ArrayList<ThemeFeature>(){{ add (ThemeFeature.NONE); }};
    }

    @Override
    public String getName() {
        return "Default PiDome theme";
    }

    @Override
    public String getDescription() {
        return "The default PiDome theme which has the goal to keep the UI consistent between all interfaces.";
    }
    
}