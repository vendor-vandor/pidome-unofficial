/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.entities.devices;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A slider control.
 * @author John
 */
public class DeviceSliderControl extends DeviceControl {

    /**
     * The slider's minimal value.
     */
    private Number min = 0;
    /**
     * The slider's maximum value.
     */
    private Number max = 0;
    
    static {
        Logger.getLogger(DeviceSliderControl.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Constructor.
     * @param controlId The id of the control.
     * @throws DeviceControlException When the control can not be constructed.
     */
    public DeviceSliderControl(String controlId) throws DeviceControlException {
        super(DeviceControlType.SLIDER, controlId);
        setLastKnownValueKnownDatatype(0);
    }
    
    /**
     * Creates the slider control.
     * @param data Base slider control data, refer to RPC spec.
     * @throws DeviceControlException When data is incomplete or faulty.
     */
    protected final void setSliderControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialDataStructure((Map<String,Object>)data);
        if(!data.containsKey("min") || !data.containsKey("max")){
            throw new DeviceControlException("Missing min and/or max attribute in slider control");
        }
        try {
            switch(getDataType()){
                case FLOAT:
                    min = ((Number)data.get("min")).floatValue();
                    max = ((Number)data.get("max")).floatValue();
                break;
                case INTEGER:
                    min = ((Number)data.get("min")).intValue();
                    max = ((Number)data.get("max")).intValue();
                break;
            }
        } catch(NumberFormatException ex){
            throw new DeviceControlException("Wrong min and/or max value in slider control");
        }
    }
    
    /**
     * Returns the minimal value.
     * Use getDataType() to determine if it is a FLOAT or INTEGER.
     * @return The min value refer to datatype for type.
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
     * Use getDataType() to determine if it is a FLOAT or INTEGER.
     * @return The max value refer to datatype for type.
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
     * Returns the mapped real value.
     * Use getDataType() to determine if it is a FLOAT or INTEGER.
     * @return The current value refer to datatype for type.
     */
    @Override
    public final Object getValueData(){
        return this.getValue();
    }
 
    /**
     * Returns a ready to send command.
     * @param command command type of SliderCommand.
     * @return Complete structure to be used with the JSONConnector.
     * @throws DeviceControlCommandException When the type is not of type SliderCommand
     */
    public DeviceCommandStructure createSendCommand(SliderCommand command) throws DeviceControlCommandException {
        return new DeviceCommandStructure(this, command.getSendValue(), this.getCommandExtra());
    }
    
    /**
     * The Slider command.
     */
    public static class SliderCommand implements DeviceControlCommand {

        /**
         * The number holding the value.
         */
        Number value;
        
        /**
         * Sets the double value;
         * @param value double value to be send.
         */
        public SliderCommand(double value){
            this.value = value;
        }
        
        /**
         * Sets the integer value.
         * @param value Integer value to be send.
         */
        public SliderCommand(int value){
            this.value = value;
        }
        
        /**
         * Returns the value to be send.
         * @return 
         */
        private Number getSendValue(){
            return value;
        }
        
    }
    
}