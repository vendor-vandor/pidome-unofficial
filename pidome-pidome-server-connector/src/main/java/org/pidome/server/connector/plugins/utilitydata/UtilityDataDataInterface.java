/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.utilitydata;

/**
 *
 * @author John
 */
public interface UtilityDataDataInterface {
    
    /**
     * Set annual threshold
     * @param value 
     */
    public abstract void setAnnualThreshold(double value);
    /**
     * Ses the current value
     * @param value 
     */
    public abstract void setCurrentValue(double value);
    /**
     * Sets the today's value.
     * @param value 
     */
    public abstract void setTodayValue(double value);
    
    /**
     * Returns current value.
     * @return 
     */
    public abstract double getCurrentValue();
    
    /**
     * Returns the today value;
     * @return 
     */
    public abstract double getTodayValue();
    
    /**
     * Returns the annual threshold.
     * @return 
     */
    public abstract double getAnnualThreshold();
    
    /**
     * Returns the daily threshold calculated from the daily threshold.
     * @return 
     */
    public abstract double getDailyThreshold();
    
    /**
     * Returns the unit value.
     * @return 
     */
    public abstract double getUnitValue();
    
    /**
     * Returns this data name.
     * @return 
     */
    public abstract String getName();
    
}
