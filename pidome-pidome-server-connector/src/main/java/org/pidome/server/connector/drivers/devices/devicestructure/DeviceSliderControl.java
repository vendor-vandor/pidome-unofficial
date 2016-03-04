/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.Map;

/**
 *
 * @author John
 */
public class DeviceSliderControl extends DeviceControl {

    private Number min    = 0;
    private Number max    = 0;
    
    /**
     * Constructor.
     * @param controlId
     * @throws DeviceControlException 
     */
    public DeviceSliderControl(DeviceControlsGroup group, String controlId) throws DeviceControlException {
        super(group,DeviceControlType.SLIDER, controlId);
        setLastKnownValueKnownDatatype(0);
    }
    
    /**
     * Creates the slider control.
     * @param data
     * @throws DeviceControlException 
     */
    protected final void setSliderControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialData(data);
        if(data.containsKey("parameters")){
            Map<String,Object> params = (Map<String,Object>)data.get("parameters");
            if(!params.containsKey("min") || !params.containsKey("max")){
                throw new DeviceControlException("Missing min and/or max attribute in slider control");
            } else {
                try {
                    min = (Number)params.get("min");
                    max = (Number)params.get("max");
                } catch(NumberFormatException ex){
                    throw new DeviceControlException("Wrong min and/or max value in slider control: " + ex.getMessage());
                }
            }
        } else {
            throw new DeviceControlException("Slider missing parameters");
        }
    }
    
    /**
     * Returns the minimal value.
     * @return 
     */
    public final Number getMin(){
        switch(getDataType()){
            case FLOAT:
                return this.min.floatValue();
            case INTEGER:
                return this.min.intValue();
            default:
                return 0;
        }
    }
    
    /**
     * Returns the maximum value.
     * @return 
     */
    public final Number getMax(){
        switch(getDataType()){
            case FLOAT:
                return this.max.floatValue();
            case INTEGER:
                return this.max.intValue();
            default:
                return 0;
        }
    }
    
    /**
     * Returns the mapped real value
     * @return 
     */
    public final Object getValueData(){
        return this.getValue();
    }
    
}
