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
public class UtilitiesGasUsage {
 
    ObjectPropertyBindingBean<Double> currentGasUsage = new ObjectPropertyBindingBean(0.0);
    ObjectPropertyBindingBean<Double> todayGasUsage = new ObjectPropertyBindingBean(0.0);
    
    
    private ObjectPropertyBindingBean<Double> threshold = new ObjectPropertyBindingBean(0.0);
    
    private String todayName = "";
    
    protected UtilitiesGasUsage(){}
    
    protected final void setTodayThreshold(double threshold){
        this.threshold.setValue(threshold);
    }
    
    public final ObjectPropertyBindingBean<Double> getThreshold(){
        return this.threshold;
    }
    
    protected final void setTodayGasName(String name){
        todayName = name;
    }
    
    public final String getTodayGasName(){
        return this.todayName;
    }
    
    public final ObjectPropertyBindingBean<Double> getCurrentGasUsage(){
        return this.currentGasUsage;
    }
    
    protected final void setCurrentGasUsage(double value){
        currentGasUsage.setValue(value);
    }
    
    public final ObjectPropertyBindingBean<Double> getTodayGasUsage(){
        return this.todayGasUsage;
    }
    
    protected final void setTodayGasUsage(double value){
        todayGasUsage.setValue(value);
    }
    
}