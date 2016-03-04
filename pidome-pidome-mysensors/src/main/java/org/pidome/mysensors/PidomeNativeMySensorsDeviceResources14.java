/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.mysensors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class PidomeNativeMySensorsDeviceResources14 {
    
    private static final Map<Integer,String> sensorTypes   = new HashMap<>();
    private static final Map<Integer,String> variableTypes = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(PidomeNativeMySensorsDeviceResources14.class);
    
    public PidomeNativeMySensorsDeviceResources14(){
        if(sensorTypes.isEmpty()){
            sensorTypes.put(0, "S_DOOR");
            sensorTypes.put(1, "S_MOTION");
            sensorTypes.put(2, "S_SMOKE");
            sensorTypes.put(3, "S_LIGHT");
            sensorTypes.put(4, "S_DIMMER");
            sensorTypes.put(5, "S_COVER");
            sensorTypes.put(6, "S_TEMP");
            sensorTypes.put(7, "S_HUM");
            sensorTypes.put(8, "S_BARO");
            sensorTypes.put(9, "S_WIND");
            sensorTypes.put(10, "S_RAIN");
            sensorTypes.put(11, "S_UV");
            sensorTypes.put(12, "S_WEIGHT");
            sensorTypes.put(13, "S_POWER");
            sensorTypes.put(14, "S_HEATER");
            sensorTypes.put(15, "S_DISTANCE");
            sensorTypes.put(16, "S_LIGHT_LEVEL");
            sensorTypes.put(17, "S_ARDUINO_NODE");
            sensorTypes.put(18, "S_ARDUINO_RELAY");
            sensorTypes.put(19, "S_LOCK");
            sensorTypes.put(20, "S_IR");
            sensorTypes.put(21, "S_WATER");
            sensorTypes.put(22, "S_AIR_QUALITY");
            sensorTypes.put(23, "S_CUSTOM");
            sensorTypes.put(24, "S_DUST");
            sensorTypes.put(25, "S_SCENE_CONTROLLER");
            sensorTypes.put(26, "S_RGB_LIGHT");
            sensorTypes.put(27, "S_RGBW_LIGHT");
            sensorTypes.put(28, "S_COLOR_SENSOR");
            sensorTypes.put(29, "S_HVAC");
            sensorTypes.put(30, "S_MULTIMETER");
            sensorTypes.put(31, "S_SPRINKLER");
            sensorTypes.put(32, "S_WATER_LEAK");
            sensorTypes.put(33, "S_SOUND");
            sensorTypes.put(34, "S_VIBRATION");
            sensorTypes.put(35, "S_MOISTURE");
        }
        
        if(variableTypes.isEmpty()){
            variableTypes.put(0, "V_TEMP");
            variableTypes.put(1, "V_HUM");
            variableTypes.put(2, "V_STATUS");
            variableTypes.put(3, "V_PERCENTAGE");
            variableTypes.put(4, "V_PRESSURE");
            variableTypes.put(5, "V_FORECAST");
            variableTypes.put(6, "V_RAIN");
            variableTypes.put(7, "V_RAINRATE");
            variableTypes.put(8, "V_WIND");
            variableTypes.put(9, "V_GUST");
            variableTypes.put(10, "V_DIRECTION");
            variableTypes.put(11, "V_UV");
            variableTypes.put(12, "V_WEIGHT");
            variableTypes.put(13, "V_DISTANCE");
            variableTypes.put(14, "V_IMPEDANCE");
            variableTypes.put(15, "V_ARMED");
            variableTypes.put(16, "V_TRIPPED");
            variableTypes.put(17, "V_WATT");
            variableTypes.put(18, "V_KWH");
            variableTypes.put(19, "V_SCENE_ON");
            variableTypes.put(20, "V_SCENE_OFF");
            variableTypes.put(21, "V_HVAC_FLOW_STATE");
            variableTypes.put(22, "V_HVAC_SPEED");
            variableTypes.put(23, "V_LIGHT_LEVEL");
            variableTypes.put(24, "V_VAR1");
            variableTypes.put(25, "V_VAR2");
            variableTypes.put(26, "V_VAR3");
            variableTypes.put(27, "V_VAR4");
            variableTypes.put(28, "V_VAR5");
            variableTypes.put(29, "V_UP");
            variableTypes.put(30, "V_DOWN");
            variableTypes.put(31, "V_STOP");
            variableTypes.put(32, "V_IR_SEND");
            variableTypes.put(33, "V_IR_RECEIVE");
            variableTypes.put(34, "V_FLOW");
            variableTypes.put(35, "V_VOLUME");
            variableTypes.put(36, "V_LOCK_STATUS");
            variableTypes.put(37, "V_DUST_LEVEL");
            variableTypes.put(38, "V_VOLTAGE");
            variableTypes.put(39, "V_CURRENT");
            variableTypes.put(40, "V_RGB");
            variableTypes.put(41, "V_RGBW");
            variableTypes.put(42, "V_ID");
            variableTypes.put(43, "V_UNIT_PREFIX");
            variableTypes.put(44, "V_HVAC_SETPOINT_COOL");
            variableTypes.put(45, "V_HVAC_SETPOINT_HEAT");
            variableTypes.put(46, "V_HVAC_FLOW_MODE");
        }
    }
        
    /**
     * Returns the sensor type.
     * @param type
     * @return
     * @throws NullPointerException 
     */
    public final String getSensorType(int type) {
        if(sensorTypes.containsKey(type)){
            return sensorTypes.get(type);
        } else {
            return String.valueOf(type);
        }
    }
    
    /**
     * Returns the variable type.
     * @param type
     * @return
     * @throws NullPointerException 
     */
    public final String getVarType(int type) {
        if(variableTypes.containsKey(type)){
            return variableTypes.get(type);
        } else {
            return String.valueOf(type);
        }
    }
    
    /**
     * Returns the int from the var
     * @param var
     * @return 
     */
    public final int getIntByVar(String var){
        switch (var) {
            case "V_LIGHT":
                LOG.debug("V_LIGHT is deprecated, please use V_STATUS!");
                var = "V_STATUS";
            break;
            case "V_DIMMER":
                LOG.debug("V_DIMMER is deprecated, please use V_PERCENTAGE!");
                var = "V_PERCENTAGE";
            break;
            case "V_HEATER":
                LOG.debug("V_HEATER is deprecated, this variable has been renamed to V_HVAC_FLOW_STATE!");
                var = "V_HVAC_FLOW_STATE";
            break;
            case "V_HEATER_SW":
                LOG.debug("V_HEATER_SW is deprecated, please use V_HVAC_SPEED!");
                var = "V_HVAC_SPEED";
            break;
        }
        for(Entry<Integer,String> variable: variableTypes.entrySet()){
            if(variable.getValue().equals(var)){
                return variable.getKey();
            }
        }
        return 24;
    }
    
    /**
     * Returns the int from the var
     * @param var
     * @return 
     */
    public final int getIntByType(String var){
        for(Entry<Integer,String> variable: sensorTypes.entrySet()){
            if(variable.getValue().equals(var)){
                return variable.getKey();
            }
        }
        return 23;
    }
    
}
