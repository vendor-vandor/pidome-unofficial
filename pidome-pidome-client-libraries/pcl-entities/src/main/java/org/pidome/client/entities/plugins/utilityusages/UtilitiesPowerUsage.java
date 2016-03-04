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
public class UtilitiesPowerUsage {
    
    ObjectPropertyBindingBean<Double> currentWattUsage = new ObjectPropertyBindingBean(0.0);
    ObjectPropertyBindingBean<Double> currentKwhUsage = new ObjectPropertyBindingBean(0.0);
    ObjectPropertyBindingBean<Double> todayKwhUsage = new ObjectPropertyBindingBean(0.0);
    
    String todayKwhName    = "";
    String currentKwhName  = "";
    String currentWattName = "";
    
    ObjectPropertyBindingBean<Double> threshold = new ObjectPropertyBindingBean(0.0);
    
    protected UtilitiesPowerUsage(){}
    
    protected final void setTodayThreshold(double threshold){
        this.threshold.setValue(threshold);
    }
    
    public final ObjectPropertyBindingBean<Double> getThreshold(){
        return this.threshold;
    }
    
    protected final void setTodayKwhName(String name){
        todayKwhName = name;
    }
    
    public final String getTodayKwhName(){
        return this.todayKwhName;
    }
    
    protected final void setCurrentKwhName(String name){
        currentKwhName = name;
    }
    
    public final String getCurrentKwhName(){
        return this.currentKwhName;
    }
    
    protected final void setCurrentWattName(String name){
        this.currentWattName = name;
    }
    
    public final String getCurrentWattName(){
        return this.currentWattName;
    }
    
    public final ObjectPropertyBindingBean<Double> getCurrentWattUsage(){
        return this.currentWattUsage;
    }
    
    public final ObjectPropertyBindingBean<Double> getCurrentKwhUsage(){
        return this.currentKwhUsage;
    }

    public final ObjectPropertyBindingBean<Double> getTodayKwhUsage(){
        return this.todayKwhUsage;
    }
    
    protected final void setCurrentWattUsage(double value){
        currentWattUsage.setValue(value);
    }

    protected final void setCurrentKwhUsage(double value){
        currentKwhUsage.setValue(value);
    }
    
    protected final void setTodayKwhUsage(double value){
        todayKwhUsage.setValue(value);
    }
    
}