/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.utilitydata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class UtilityDataData {
    
    static Logger LOG = LogManager.getLogger(UtilityDataData.class);
    
    private final String name;
    private double todayValue   = 0D;
    private double currentValue = 0D;
    
    private double annualThreshold = 0D;
    private double dailyThreshold  = 0D;
    
    private final double unitValue;
    
    private final UtilityDataInterface.Measurement measurementType;
    
    /**
     * Constructor.
     * @param name 
     * @param unitValue 
     * @param measureType 
     */
    public UtilityDataData(String name, double unitValue, UtilityDataInterface.Measurement measureType) {
        this.name = name;
        this.unitValue = unitValue;
        this.measurementType = measureType;
    }
    
    /**
     * Returns the current data name as set by the user.
     * @return 
     */
    public final String getName(){
        return this.name;
    }
    
    /**
     * Sets the current value.
     * @param value 
     */
    public void setCurrentValue(double value){
        this.currentValue = value;
    }
    
    /**
     * Sets the value as it is today.
     * @param value 
     */
    public final void setTodayValue(double value){
        this.todayValue = value;
    }
    
    
    /**
     * Returns the current value.
     * @return 
     */
    public final double getCurrentValue(){
        return this.currentValue;
    }
    
    /**
     * Returns the current today value.
     * @return 
     */
    public final double getTodayValue(){
        return this.todayValue;
    }
    
    /**
     * Sets the annual threshold.
     * @param value 
     */
    public final void setAnnualThreshold(double value){
        this.annualThreshold = value;
        setDailyThreshold(value/365D);
    }
    
    /**
     * Returns the annual threshold.
     * @return 
     */
    public final double getAnnualThreshold(){
        return this.annualThreshold;
    }
    
    /**
     * Returns the daily threshold.
     * @return 
     */
    public final double getDailyThreshold(){
        return this.dailyThreshold;
    }
    
    /**
     * Returns the measurement unit used.
     * @return 
     */
    public final double getUnitValue(){
        return this.unitValue;
    }
    
    /**
     * Returns the measurement type.
     * @return 
     */
    public final UtilityDataInterface.Measurement getMeasurementType(){
        return this.measurementType;
    }
    
    /**
     * Sets a threshold value for daily usage.
     * @param value
     * @return 
     */
    private void setDailyThreshold(double value){
        dailyThreshold = value;
    }
    
}