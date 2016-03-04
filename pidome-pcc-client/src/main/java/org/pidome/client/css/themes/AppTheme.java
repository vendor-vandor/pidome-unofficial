/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.css.themes;

import java.util.List;

/**
 *
 * @author John
 */
public abstract class AppTheme extends Theme {

    public enum ThemeTypes {
        APP,
        DASHBOARD;
    }
    
    public abstract DashboardTheme getDashboardTheme();
    
    public abstract List<ThemeTypes> getThemeType();
    
    public abstract String getName();
    
    public abstract String getDescription();
    
}