/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.utilitydata;

import java.util.Date;

/**
 *
 * @author John
 */
public class UtilityDataWater extends UtilityDataData {

    int today =new Date().getDate();
    
    double prevWaterMeasurement;
    
    /**
     * Constructor.
     * @param name
     * @param unitValue
     * @param measureType 
     */
    public UtilityDataWater(String name, double unitValue, UtilityDataInterface.Measurement measureType) {
        super(name, unitValue, measureType);
    }
   
    /**
     * Sets the current water usage.
     * @param amount 
     */
    @Override
    public void setCurrentValue(double amount){
        int now = new Date().getDate();
        if(today!=now){
            today = now;
            if(!this.getMeasurementType().equals(UtilityDataInterface.Measurement.WATER_ABSOLUTE_TOTAL)) super.setTodayValue(0.0D);
        }
        switch(this.getMeasurementType()){
            case WATER_ABSOLUTE:
                if(prevWaterMeasurement!=0.0){
                    super.setCurrentValue(amount);
                }
                super.setTodayValue(this.getTodayValue() + this.getCurrentValue());
                prevWaterMeasurement = amount;
            break;
            case WATER_ABSOLUTE_TOTAL:
                super.setCurrentValue(amount);
                super.setTodayValue(amount);
            break;
            default:
                super.setCurrentValue(amount * this.getUnitValue());
                super.setTodayValue(this.getTodayValue() + this.getCurrentValue());
            break;
        }
    }
    
}