/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.domotics.components.utilitymeasurement;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.DomComponents;

/**
 *
 * @author John
 */
public class UtilityMeasurement {

    Map<String,String> nameMap = new HashMap<>();
    
    int pluginId;
    String pluginName;
    
    DomComponents domResource;
    
    static Logger LOG = LogManager.getLogger(UtilityMeasurement.class);
    
    CurrentWattUtilityMeasurement currentWatt = new CurrentWattUtilityMeasurement();
    TotalKwhUtilityManagement todayKwh = new TotalKwhUtilityManagement();
    TotalGassUtilityManagement todayGas = new TotalGassUtilityManagement();
    TotalWaterUtilityManagement todayWater = new TotalWaterUtilityManagement();
            
    public UtilityMeasurement(int id, String name){
        this.pluginId = id;
        this.pluginName = name;
        
        nameMap.put("WATT", "Watt");
        nameMap.put("KWH", "kW/h");
        nameMap.put("WATER", "Liter³");
        nameMap.put("GAS", "M³");
        
    }
    
    public final int getPluginId(){
        return this.pluginId;
    }
    
    /**
     * Returns the mapped name for watt,kwh,water or gas.
     * @param name
     * @return 
     */
    public final String getMappedName(String name){
        if(nameMap.containsKey(name)){
            return nameMap.get(name);
        } else {
            return "Unknown";
        }
    }
    
    /**
     * Sets the mapped name as set in the server.
     * @param name
     * @param value 
     */
    public final void setMappedName(String name, String value){
        nameMap.put(name, value);
    }
    
    /**
     * Returns a property by global used name.
     * @param name
     * @return 
     */
    public final DoubleProperty getPropertyByName(String name){
        switch(name){
            case "KWH":
                return this.getTodayKwhProperty();
            case "WATER":
                return this.getTodayWaterProperty();
            case "GAS":
                return this.getTodayGasProperty();
            default:
                return this.getCurrentWattProperty();
        }
    }
    
    /**
     * Sets the thresholds.
     * @param type
     * @param value 
     */
    public final void setThreshold(String type, double value){
        switch(type){
            case "KWH":
                this.todayKwh.setThreshold(value);
            case "WATER":
                this.todayWater.setThreshold(value);
            case "GAS":
                this.todayGas.setThreshold(value);
            case "WATT":
                this.currentWatt.setThreshold(value);
        }
    }
    
    /**
     * Updates power usage.
     * @param watt
     * @param kwh 
     * @param totalKwh 
     */
    protected final void updatePower(double watt, double kwh, double totalKwh){
        currentWatt.updateCurrentPower(watt);
        todayKwh.updateCurrentKwhPower(kwh, totalKwh);
    }
    
    /**
     * updates current water values.
     * @param water
     * @param todayWater 
     */
    protected final void updateWater(double water, double todayWater){
        this.todayWater.updateWater(water, todayWater);
    }
    
    /**
     * Updates current gas usage.
     * @param gas
     * @param todayGas 
     */
    protected final void updateGas(double gas, double todayGas){
        this.todayGas.updateGas(gas, todayGas);
    }
    
    /**
     * Returns the current watt usage property.
     * @return 
     */
    public final DoubleProperty getCurrentWattProperty(){
        return this.currentWatt.getCurrentWattProperty();
    }

    /**
     * Get upperbound watt value
     * @return 
     */
    public final double getCurrentWattUpperBound(){
        return this.currentWatt.getThreshold();
    }
    
    /**
     * Returns the today kwh usage property
     * @return 
     */
    public final DoubleProperty getTodayKwhProperty(){
        return this.todayKwh.getTodayKwhProperty();
    }
    
    /**
     * Get upperbound kwh value
     * @return 
     */
    public final double getCurrentKwhUpperBound(){
        return this.todayKwh.getThreshold();
    }
    
    /**
     * Returns the today water usage property
     * @return 
     */
    public final DoubleProperty getTodayWaterProperty(){
        return this.todayWater.getTodayWaterProperty();
    }
    
    /**
     * Get upperbound water value
     * @return 
     */
    public final double getCurrentWaterUpperBound(){
        return this.todayWater.getThreshold();
    }
    
    /**
     * Returns the today gas usage property
     * @return 
     */
    public final DoubleProperty getTodayGasProperty(){
        return this.todayGas.getTodayGasProperty();
    }
    
    /**
     * Get upperbound gas value
     * @return 
     */
    public final double getCurrentGasUpperBound(){
        return this.todayGas.getThreshold();
    }
    
}
