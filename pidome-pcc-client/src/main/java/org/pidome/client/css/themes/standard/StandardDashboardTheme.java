/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes.standard;

import java.util.ArrayList;
import java.util.List;
import org.pidome.client.css.themes.DashboardTheme;

/**
 *
 * @author John
 */
public class StandardDashboardTheme extends DashboardTheme {

    @Override
    public List<CodeSupports> getCodeSupports() {
        return new ArrayList<CodeSupports>(){{ add (CodeSupports.NONE); }};
    }

    @Override
    public String getCSSPath() {
       return "/org/pidome/client/css/themes/standard/dashboard.css";
    }
    
    @Override
    public List<ThemeFeature> getThemeFeatures() {
        return new ArrayList<ThemeFeature>(){{ add (ThemeFeature.NONE); }};
    }
    
}