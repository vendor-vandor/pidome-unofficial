/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.utilitydata;

import java.util.List;
import java.util.Map;

/**
 *
 * @author John
 */
public interface UtilityDataInterface {
    
    /**
     * Utilities types.
     */
    public enum Type {
        POWER,WATER,GAS,POWER_LOW,POWER_HIGH,POWER_RETURN,POWER_RETURN_LOW,POWER_RETURN_HIGH;
    }
    
    /**
     * Used measurements types
     */
    public enum Measurement {
        POWER_TIMED_RC,       //// [AMOUNT] beats/Rotations is one kwh
        POWER_CONTINUOUS_RC,  //// [AMOUNT] beats/Rotations is one kwh
        POWER_TIMED_KH,       //// Watt-hour [kH] (beats)
        POWER_CONTINUOUS_KH,  //// Watt-hour [kH] (beats)
        POWER_ABSOLUTE,       //// Uses absolute current watt values.
        POWER_ABSOLUTE_KWH,   //// Uses absolute current kwh values.
        POWER_ABSOLUTE_KWHTOT,//// Uses absolute total kwh values.
        
        WATER_DEFAULT_SERIES, //// Used for water calculation in a += way
        WATER_ABSOLUTE,       //// Uses absolute values.
        WATER_ABSOLUTE_TOTAL, //// Uses absolute today's values.
        
        GAS_DEFAULT_SERIES,   //// Used for gas calculation in a += way
        GAS_ABSOLUTE,         //// Uses absolute values.
        GAS_ABSOLUTE_TOTAL,   //// Uses absolute today's values.
    }
    
    /**
     * Returns single usage.
     * @param type
     * @return 
     */
    public Map<String,Map<String,Object>> getCurrentUsage(Type type, String id) throws UtilityDataException;
    
    /**
     * Returns current usages.
     * @return 
     */
    public Map<Type, List<Map<String,Map<String,Object>>>> getCurrentUsages();
    
    /**
     * Returns an object with all the totals creating an end total.
     * @return 
     */
    public Map<Type, Map<String,Map<String,Object>>> getCurrentTotalUsages();
    
    /**
     * Possible to set total power usage value.
     * Used when a plugin starts, data is logged and there is data known.
     * @param id
     * @param kwh 
     */
    public void setPowerPreTotals(String id,double kwh);
    
    /**
     * Possible to set known water totals.
     * Used when a plugin starts, data previous logged can be set with this function.
     * @param id
     * @param water 
     */
    public void setWaterPreTotals(String id,double water);
    
    /**
     * Possible to set gas totals.
     * Used when a plugin starts, data previous logged can be set with this function.
     * @param id
     * @param gas 
     */
    public void setGasPreTotals(String id,double gas);
    
}