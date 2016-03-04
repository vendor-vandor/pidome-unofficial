/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 *
 * @author John
 */
public class DashboardWeatherItem extends DashboardItem {

    public DashboardWeatherItem() {
        super(ItemType.WEATHER);
    }

    @Override
    protected void setConfig(Map<String, Object> config) {
        ///There is no config as there is onnly one weather plugin available.
    }
    
}