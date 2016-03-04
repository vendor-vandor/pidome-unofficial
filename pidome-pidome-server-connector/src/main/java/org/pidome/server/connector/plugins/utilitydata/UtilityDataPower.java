/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.utilitydata;

import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataInterface.Measurement;

/**
 *
 * @author John
 */
public class UtilityDataPower extends UtilityDataData implements UtilityDataPowerInterface {
    
    int today =new Date().getDate();
    
    static Logger LOG = LogManager.getLogger(UtilityDataPower.class);
    
    Long lastPowerMeasurementTime    = 0L;
    Long currentPowerMeasurementTime = 0L;
    
    double prevPowerUsageKWh         = 0.0D;
    
    UtilityDataData kwh;
    
    public UtilityDataPower(String watt, String kwh, double unitValue, Measurement measureType) {
        super(watt,unitValue,measureType);
        this.kwh = new UtilityDataData(kwh,unitValue,measureType);
        this.kwh.setTodayValue(0);
        this.kwh.setAnnualThreshold(11000D);
    }
    
    /**
     * Returns KWH values.
     * @return 
     */
    @Override
    public final UtilityDataData getKwh(){
        return this.kwh;
    }
    
    /**
     * Sets current wattage usage.
     * @param value 
     */
    @Override
    public void setCurrentValue(double value){
        currentPowerMeasurementTime = new Date().getTime();
        double timeDiff = (double)((currentPowerMeasurementTime - lastPowerMeasurementTime)/1000L);
        int now = new Date().getDate();
        if(today!=now){
            today = now;
            if(!this.getMeasurementType().equals(UtilityDataInterface.Measurement.POWER_ABSOLUTE_KWHTOT)) this.kwh.setTodayValue(0);
        }
        /// discard first read because we need first start measurement time before we can calculate kwh.
        if(lastPowerMeasurementTime!=0L){
            switch(this.getMeasurementType()){
                case POWER_ABSOLUTE:
                    double timeMeasuredForKwhContAbs = 1D / timeDiff;
                    super.setCurrentValue(value);
                    getKwh().setCurrentValue((getCurrentValue() * timeMeasuredForKwhContAbs)/1000);
                    if(Double.isFinite(getKwh().getTodayValue())){
                        getKwh().setTodayValue(getKwh().getTodayValue() + getKwh().getCurrentValue());
                    } else {
                        getKwh().setTodayValue(getKwh().getCurrentValue());
                    }
                break;
                case POWER_ABSOLUTE_KWH: 
                    double timeMeasuredForKwhContTotAbs = 1D / timeDiff;
                    getKwh().setTodayValue(this.kwh.getTodayValue() + value);
                    super.setCurrentValue((1000D * value) / timeMeasuredForKwhContTotAbs);
                break;
                case POWER_ABSOLUTE_KWHTOT: 
                    double timeMeasuredForKwhContTot = 1D / timeDiff;
                    getKwh().setTodayValue(value);
                    if(prevPowerUsageKWh!=0){
                        super.setCurrentValue((1000D * (this.kwh.getTodayValue() - prevPowerUsageKWh)) / timeMeasuredForKwhContTot);
                    }
                    prevPowerUsageKWh = this.kwh.getTodayValue();
                break;
                case POWER_TIMED_RC:
                    double timeMeasuredForKwhTimed = 1D / timeDiff;
                    super.setCurrentValue(3600000D / (this.getUnitValue() * (timeDiff/value)));
                    getKwh().setCurrentValue((getCurrentValue() * timeMeasuredForKwhTimed)/1000D);
                    getKwh().setTodayValue(this.kwh.getTodayValue() + this.kwh.getCurrentValue());
                break;
                case POWER_CONTINUOUS_RC:
                    //// Almost the same as above, but now without amount division.
                    double timeMeasuredForKwhCont = 1D / timeDiff;
                    super.setCurrentValue(3600000D / (this.getUnitValue() * timeDiff));
                    getKwh().setCurrentValue((getCurrentValue() * timeMeasuredForKwhCont)/1000D);
                    getKwh().setTodayValue(this.kwh.getTodayValue() + this.kwh.getCurrentValue());
                break;
                case POWER_TIMED_KH:
                    super.setCurrentValue((3600D / timeDiff) * (this.getUnitValue() * value)); ///(Seconds) * kh value -> Watt hour
                    getKwh().setCurrentValue((getCurrentValue() /1000D)); /// divide by 1000 for kwH value
                    getKwh().setTodayValue(this.kwh.getTodayValue() + this.kwh.getCurrentValue());
                break;
                case POWER_CONTINUOUS_KH:
                    /// The same as above but without multuplying the Kh value.
                    super.setCurrentValue((3600D / timeDiff) * this.getUnitValue()); ///(Seconds) * kh value -> Watt hour
                    getKwh().setCurrentValue((getCurrentValue() /1000D)); /// divide by 1000 for kwH value
                    getKwh().setTodayValue(this.kwh.getTodayValue() + this.kwh.getCurrentValue());
                break;
                default:
                    LOG.error("Current chosen power calcultation is unsupported: {}", getMeasurementType() );
                break;
            }
        }
        if((this.getMeasurementType()==Measurement.POWER_CONTINUOUS_KH || this.getMeasurementType()==Measurement.POWER_CONTINUOUS_RC) && value>0.0D){
            lastPowerMeasurementTime = currentPowerMeasurementTime;
        } else if (this.getMeasurementType()==Measurement.POWER_TIMED_KH || this.getMeasurementType()==Measurement.POWER_TIMED_RC){
            lastPowerMeasurementTime = currentPowerMeasurementTime;
        } else {
            lastPowerMeasurementTime = currentPowerMeasurementTime;
        }
    }
    
    /**
     * Sets the current today KWH value.
     * @param value 
     */
    @Override
    public final void setTodayKwh(double value){
        this.kwh.setTodayValue(value);
    }
    
}