/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.plugins.utilityusages;

import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;

/**
 *
 * @author John
 */
public class UtilitiesWaterUsage {
    
    private ObjectPropertyBindingBean<Double> currentWaterUsage = new ObjectPropertyBindingBean(0.0);
    private ObjectPropertyBindingBean<Double> todayWaterUsage = new ObjectPropertyBindingBean(0.0);
    
    private ObjectPropertyBindingBean<Double> threshold = new ObjectPropertyBindingBean(0.0);
    
    private String todayName = "";
    
    protected UtilitiesWaterUsage(){}
    
    protected final void setTodayThreshold(double threshold){
        this.threshold.setValue(threshold);
    }
    
    public final ObjectPropertyBindingBean<Double> getThreshold(){
        return this.threshold;
    }
    
    protected final void setTodayWaterName(String name){
        todayName = name;
    }
    
    public final String getTodayWaterName(){
        return this.todayName;
    }
    
    public final ObjectPropertyBindingBean<Double> getCurrentWaterUsage(){
        return this.currentWaterUsage;
    }
    
    protected final void setCurrentWaterUsage(double value){
        currentWaterUsage.setValue(value);
    }

    public final ObjectPropertyBindingBean<Double> getTodayWaterUsage(){
        return this.todayWaterUsage;
    }
    
    protected final void setTodayWaterUsage(double value){
        todayWaterUsage.setValue(value);
    }
    
}