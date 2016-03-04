/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.automations.variables;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.weatherplugin.CurrentWeatherData;
import org.pidome.server.connector.plugins.weatherplugin.WeatherData;
import org.pidome.server.connector.tools.properties.ObjectPropertyBindingBean;
import org.pidome.server.services.events.CustomEvent;
import org.pidome.server.services.events.EventService;
import org.pidome.server.services.plugins.WeatherPluginService;
import org.pidome.server.services.plugins.WeatherPluginService.WeatherStatusListener;

/**
 *
 * @author John
 */
public class WeatherStatusVariable extends AutomationVariable implements WeatherStatusListener {

    int listenTo;
    
    static Logger LOG = LogManager.getLogger(WeatherStatusVariable.class);
    
    public WeatherStatusVariable(ObjectPropertyBindingBean var) {
        super(var, "WeatherStatusListener");
        WeatherPluginService.getInstance().addListener(this);
    }
 
    @Override
    public final void destroy(){
        WeatherPluginService.getInstance().removeListener(this);
        this.unlink();
    }

    @Override
    public void handleNewWeatherStatus(CurrentWeatherData data) {
        this.set(data.getStateIcon().getBaseValue());
    }
    
}
