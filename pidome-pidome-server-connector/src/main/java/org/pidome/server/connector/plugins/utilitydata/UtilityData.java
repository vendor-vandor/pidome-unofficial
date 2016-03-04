/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.utilitydata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.freeform.FreeformPlugin;

/**
 *
 * @author John
 */
public abstract class UtilityData extends FreeformPlugin implements UtilityDataInterface {

    static Logger LOG = LogManager.getLogger(UtilityData.class);
    
    ArrayList<UtilityDataListener> _listeners = new ArrayList<>();
    
    Map<Type, Measurement> measurements = new HashMap<>();
    
    Map<String, UtilityDataPower> powerlist = new HashMap<>();
    Map<String, UtilityDataGas>   gaslist   = new HashMap<>();
    Map<String, UtilityDataWater> waterlist = new HashMap<>();
    
    private String basePowerName    = "WATT";
    private String basePowerKwhName = "KW/h";
    
    private String baseGasName = "M³";
    private String baseWaterName = "Liter³";
    
    /**
     * Adds a power measurement calculation.
     * @param id
     * @param power 
     */
    public void addPowerMeasurement(String id, UtilityDataPower power) {
        powerlist.put(id, power);
    }
    
    /**
     * Adds a water measurement calculation.
     * @param id
     * @param water 
     */
    public void addWaterMeasurement(String id, UtilityDataWater water) {
        waterlist.put(id, water);
    }
    
    /**
     * Adds a gas measurement calculation.
     * @param id
     * @param gas 
     */
    public void addGasMeasurement(String id, UtilityDataGas gas) {
        gaslist.put(id, gas);
    }
    
    /**
     * Returns the measurement type.
     * @param type
     * @return
     * @throws UtilityDataException 
     */
    public final UtilityDataPower getPowerMeasurement(String id) throws UtilityDataException {
        try {
            return powerlist.get(id);
        } catch(Exception ex){
            throw new UtilityDataException("Power: {}, does not contain any data");
        }
    }
    
    /**
     * Returns the measurement type.
     * @param type
     * @return
     * @throws UtilityDataException 
     */
    public final UtilityDataWater getWaterMeasurement(String id) throws UtilityDataException {
        try {
            return waterlist.get(id);
        } catch(Exception ex){
            throw new UtilityDataException("Water: {}, does not contain any data");
        }
    }
    
    /**
     * Returns the measurement type.
     * @param type
     * @return
     * @throws UtilityDataException 
     */
    public final UtilityDataGas getGasMeasurement(String id) throws UtilityDataException {
        try {
            return gaslist.get(id);
        } catch(Exception ex){
            throw new UtilityDataException("Gas: {}, does not contain any data");
        }
    }
    
    /**
     * Returns the specific measurement type attached.
     * @param type 
     * @param measurement 
     * @return  
     * @throws org.pidome.server.connector.plugins.utilitydata.UtilityDataException  
     */
    public final boolean hasMeasurementType(Type type, Measurement measurement) throws UtilityDataException {
        try {
            return measurements.get(type).equals(measurement);
        } catch (Exception ex){
            throw new UtilityDataException("Type: {}, does not contain any measurement type");
        }
    }
    
    /**
     * Adds a listener for utility data usages.
     * @param l 
     */
    public final void addListener(UtilityDataListener l){
        if(!_listeners.contains(l)) _listeners.add(l);
    }
    
    /**
     * Removes a listener for utility data usages.
     * @param l 
     */
    public final void removeListener(UtilityDataListener l){
        if(_listeners.contains(l)) _listeners.remove(l);
    }
    
    /**
     * Broadcasts current value change to listeners.
     * @param type 
     */
    public final void broadcastResultValue(final Type type, String id){
        final UtilityData me = this;
        Runnable broadcast = () -> {
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                try {
                    ((UtilityDataListener) listeners.next()).handleUtilityData(me, type, getCurrentUsage(type, id));
                } catch (UtilityDataException ex) {
                    LOG.error("Utility data not found: {}", ex.getMessage());
                }
            }
        };
        broadcast.run();
    }
    
    /**
     * Returns the current usage of the given type and id.
     * @param type
     * @param id
     * @return 
     * @throws org.pidome.server.connector.plugins.utilitydata.UtilityDataException 
     */
    @Override
    public final Map<String,Map<String,Object>> getCurrentUsage(Type type, String id) throws UtilityDataException {
        Map<String,Map<String,Object>> usage = new HashMap<>();
        switch(type){
            case POWER:
                for(Map.Entry<String,UtilityDataPower> item: powerlist.entrySet()){
                    if(item.getKey().equals(id)){
                        Map<String,Object> detailsPowerWatt = new HashMap<>();
                        detailsPowerWatt.put("name" , item.getValue().getName());
                        detailsPowerWatt.put("value", item.getValue().getCurrentValue());
                        usage.put("unitcurrent", detailsPowerWatt);

                        Map<String,Object> detailsPowerKwh = new HashMap<>();
                        detailsPowerKwh.put("name" , item.getValue().getKwh().getName());
                        detailsPowerKwh.put("value", item.getValue().getKwh().getCurrentValue());
                        usage.put("current", detailsPowerKwh);

                        Map<String,Object> detailsPowerKwhToday = new HashMap<>();
                        detailsPowerKwhToday.put("name"     , item.getValue().getKwh().getName());
                        detailsPowerKwhToday.put("value"    , item.getValue().getKwh().getTodayValue());
                        detailsPowerKwhToday.put("threshold", item.getValue().getKwh().getDailyThreshold());
                        usage.put("today", detailsPowerKwhToday);
                        return usage;
                    }
                }
            break;
            case WATER:
                for(Map.Entry<String,UtilityDataWater> item: waterlist.entrySet()){
                    if(item.getKey().equals(id)){
                        Map<String,Object> detailsWater = new HashMap<>();
                        detailsWater.put("name" , item.getValue().getName());
                        detailsWater.put("value", item.getValue().getCurrentValue());
                        usage.put("current", detailsWater);

                        Map<String,Object> detailsWaterToday = new HashMap<>();
                        detailsWaterToday.put("name"     , item.getValue().getName());
                        detailsWaterToday.put("value"    , item.getValue().getTodayValue());
                        detailsWaterToday.put("threshold", item.getValue().getDailyThreshold());
                        usage.put("today", detailsWaterToday);
                        return usage;
                    }
                }
            break;
            case GAS:
                for(Map.Entry<String,UtilityDataGas> item: gaslist.entrySet()){
                    if(item.getKey().equals(id)){
                        Map<String,Object> detailsWater = new HashMap<>();
                        detailsWater.put("name" , item.getValue().getName());
                        detailsWater.put("value", item.getValue().getCurrentValue());
                        usage.put("current", detailsWater);

                        Map<String,Object> detailsWaterToday = new HashMap<>();
                        detailsWaterToday.put("name"     , item.getValue().getName());
                        detailsWaterToday.put("value"    , item.getValue().getTodayValue());
                        detailsWaterToday.put("threshold", item.getValue().getDailyThreshold());
                        usage.put("today", detailsWaterToday);
                        return usage;
                    }
                }
            break;
        }
        throw new UtilityDataException("Combination if type "+type.toString()+" width id "+id+" not found");
    }
    
    
    /**
     * Returns a list of power usages.
     * @return 
     */
    public final List<Map<String,Map<String,Object>>> getPowerList(){
        List<Map<String,Map<String,Object>>> powers = new ArrayList<>();
        for(UtilityDataPower item: powerlist.values()){
            
            Map<String,Map<String,Object>> usage = new HashMap<>();
            
            Map<String,Object> detailsPowerWatt = new HashMap<>();
            detailsPowerWatt.put("name" , item.getName());
            detailsPowerWatt.put("value", item.getCurrentValue());
            usage.put("unitcurrent", detailsPowerWatt);

            Map<String,Object> detailsPowerKwh = new HashMap<>();
            detailsPowerKwh.put("name" , item.getKwh().getName());
            detailsPowerKwh.put("value", item.getKwh().getCurrentValue());
            usage.put("current", detailsPowerKwh);

            Map<String,Object> detailsPowerKwhToday = new HashMap<>();
            detailsPowerKwhToday.put("name"     , item.getKwh().getName());
            detailsPowerKwhToday.put("value"    , item.getKwh().getTodayValue());
            detailsPowerKwhToday.put("threshold", item.getKwh().getDailyThreshold());
            
            usage.put("today", detailsPowerKwhToday);
            powers.add(usage);
        }
        return powers;
    }
    
    /**
     * Returns the water list.
     * @return 
     */
    public final List<Map<String,Map<String,Object>>> getWaterList(){
        List<Map<String,Map<String,Object>>> waters = new ArrayList<>();
        for(UtilityDataWater item: waterlist.values()){

            Map<String,Map<String,Object>> usage = new HashMap<>();

            Map<String,Object> detailsWater = new HashMap<>();
            detailsWater.put("name" , item.getName());
            detailsWater.put("value", item.getCurrentValue());
            usage.put("current", detailsWater);

            Map<String,Object> detailsWaterToday = new HashMap<>();
            detailsWaterToday.put("name"     , item.getName());
            detailsWaterToday.put("value"    , item.getTodayValue());
            detailsWaterToday.put("threshold", item.getDailyThreshold());
            usage.put("today", detailsWaterToday);
            waters.add(usage);
        }
        return waters;
    }
    
    /**
     * Returns the gas list.
     * @return 
     */
    public final List<Map<String,Map<String,Object>>> getGasList(){
        List<Map<String,Map<String,Object>>> gasses = new ArrayList<>();
        for(UtilityDataGas item: gaslist.values()){

            Map<String,Map<String,Object>> usage = new HashMap<>();

            Map<String,Object> detailsWater = new HashMap<>();
            detailsWater.put("name" , item.getName());
            detailsWater.put("value", item.getCurrentValue());
            usage.put("current", detailsWater);

            Map<String,Object> detailsWaterToday = new HashMap<>();
            detailsWaterToday.put("name"     , item.getName());
            detailsWaterToday.put("value"    , item.getTodayValue());
            detailsWaterToday.put("threshold", item.getDailyThreshold());
            usage.put("today", detailsWaterToday);
            gasses.add(usage);
        }
        return gasses;
    }
    
    /**
     * Returns the current usages.
     * @return 
     */
    @Override
    public final Map<Type, List<Map<String,Map<String,Object>>>> getCurrentUsages() {
        Map<Type, List<Map<String,Map<String,Object>>>> returns = new HashMap<>();
        returns.put(Type.POWER, getPowerList());
        returns.put(Type.WATER, getWaterList());
        returns.put(Type.GAS,   getGasList());
        return returns;
    }
    
    /**
     * Returns the current usages.
     * @return 
     */
    @Override
    public final Map<Type, Map<String,Map<String,Object>>> getCurrentTotalUsages() {
        Map<Type, Map<String,Map<String,Object>>> returns = new HashMap<>();
        double currentWatt    = 0.0D;
        double currentKwh     = 0.0D;
        double todayKwh       = 0.0D;
        double powerThreshold = 0.0D;
        double currentKwhThr  = 0.0D;
        for(UtilityDataPower item: powerlist.values()){
            currentWatt    = currentWatt    + item.getCurrentValue();
            currentKwh     = currentKwh     + item.getKwh().getCurrentValue();
            todayKwh       = todayKwh       + item.getKwh().getTodayValue();
            powerThreshold = powerThreshold + item.getKwh().getDailyThreshold();
        }
        
        Map<String,Map<String,Object>> powerUsage = new HashMap<>();
        
        Map<String,Object> detailsPowerWatt = new HashMap<>();
        detailsPowerWatt.put("name" , basePowerName);
        detailsPowerWatt.put("value", currentWatt);
        powerUsage.put("unitcurrent", detailsPowerWatt);

        Map<String,Object> detailsPowerKwh = new HashMap<>();
        detailsPowerKwh.put("name" , basePowerKwhName);
        detailsPowerKwh.put("value", currentKwh);
        detailsPowerKwh.put("threshold", powerThreshold);
        powerUsage.put("current", detailsPowerKwh);

        Map<String,Object> detailsPowerKwhToday = new HashMap<>();
        detailsPowerKwhToday.put("name"     , basePowerKwhName);
        detailsPowerKwhToday.put("value"    , todayKwh);
        detailsPowerKwhToday.put("threshold", powerThreshold);
        powerUsage.put("today", detailsPowerKwhToday);
        
        returns.put(Type.POWER, powerUsage);
        
        double currentWater   = 0.0D;
        double totalWater     = 0.0D;
        double waterThreshold = 0.0D;
        
        for(UtilityDataWater item: waterlist.values()){
            currentWater   = currentWater   + item.getCurrentValue();
            totalWater     = totalWater     + item.getTodayValue();
            waterThreshold = waterThreshold + item.getDailyThreshold();
        }
        
        Map<String,Map<String,Object>> waterUsage = new HashMap<>();

        Map<String,Object> detailsWater = new HashMap<>();
        detailsWater.put("name" , baseWaterName);
        detailsWater.put("value", currentWater);
        detailsWater.put("threshold", waterThreshold);
        waterUsage.put("current", detailsWater);

        Map<String,Object> detailsWaterToday = new HashMap<>();
        detailsWaterToday.put("name"     , baseWaterName);
        detailsWaterToday.put("value"    , totalWater);
        detailsWaterToday.put("threshold", waterThreshold);
        waterUsage.put("today", detailsWaterToday);

        returns.put(Type.WATER, waterUsage);
            
        double currentGas   = 0.0D;
        double totalGas     = 0.0D;
        double gasThreshold = 0.0D;

        for(UtilityDataGas item: gaslist.values()){
            currentGas   = currentGas   + item.getCurrentValue();
            totalGas     = totalGas     + item.getTodayValue();
            gasThreshold = gasThreshold + item.getDailyThreshold();
        }
        
        Map<String,Map<String,Object>> gasUsage = new HashMap<>();

        Map<String,Object> detailsgas = new HashMap<>();
        detailsgas.put("name" , baseGasName);
        detailsgas.put("value", currentGas);
        detailsgas.put("threshold", gasThreshold);
        gasUsage.put("current", detailsgas);

        Map<String,Object> detailsgasToday = new HashMap<>();
        detailsgasToday.put("name"     , baseGasName);
        detailsgasToday.put("value"    , totalGas);
        detailsgasToday.put("threshold", gasThreshold);
        gasUsage.put("today", detailsgasToday);
        
        returns.put(Type.GAS,   gasUsage);
        
        
        return returns;
    }
    
    /**
     * When there is database data cumulatives are set using this.
     * @param kwh 
     */
    @Override
    public void setPowerPreTotals(String id, double kwh) {
        try {
            if (Double.isInfinite(kwh)){
                this.getPowerMeasurement(id).getKwh().setTodayValue(kwh);
            }
        } catch (UtilityDataException ex) {
            LOG.error("Power id {} not found, todays values will not be set");
        }
    }

    /**
     * When there is database data cumulatives are set using this.
     * @param water 
     */
    @Override
    public void setWaterPreTotals(String id, double water) {
        try {
            if (Double.isInfinite(water)){
                this.getPowerMeasurement(id).setTodayValue(water);
            }
        } catch (UtilityDataException ex) {
            LOG.error("Water id {} not found, todays values will not be set");
        }
    }

    /**
     * When there is database data cumulatives are set using this. 
     * @param gas
     */
    @Override
    public void setGasPreTotals(String id, double gas) {
        try {
            if (Double.isInfinite(gas)){
                this.getPowerMeasurement(id).setTodayValue(gas);
            }
        } catch (UtilityDataException ex) {
            LOG.error("Gas id {} not found, todays values will not be set");
        }
    }
    
}
