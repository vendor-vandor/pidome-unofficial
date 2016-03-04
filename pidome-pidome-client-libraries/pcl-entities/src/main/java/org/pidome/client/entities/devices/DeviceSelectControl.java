/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.entities.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Do not use this control yet.
 * @author John
 */
public class DeviceSelectControl extends DeviceControl {

    Map<String,Object> optionsList = new HashMap<>();
    
    static {
        Logger.getLogger(DeviceSelectControl.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Constructor.
     * @param controlId the control id.
     * @throws DeviceControlException When the control can not be constructed.
     */
    protected DeviceSelectControl(String controlId) throws DeviceControlException {
        super(DeviceControlType.SELECT, controlId);
    }
 
    /**
     * Creates the set control.
     * @param data Basic select control data, refer to RPC spec.
     * @throws DeviceControlException if data is incomplete.
     */
    protected final void setSelectControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialDataStructure((Map<String,Object>)data);
        this.optionsList = (Map<String,Object>)data.get("commandset");
    }
    
    /**
     * Returns an options's label.
     * @param value The value of a selected option.
     * @return The label value.
     * @throws DeviceControlException when the option does not exist.
     */
    public final String getOptionLabel(Object value) throws DeviceControlException {
        for(Map.Entry<String,Object> item:optionsList.entrySet()){
            if(item.getValue().equals(value)){
                return item.getKey();
            }
        }
        throw new DeviceControlException("Option value " + value.toString() + " does not exist");
    }

    /**
     * Resturns the complete select list.
     * @return the full options list of the select.
     */
    public final Map<String,Object> getFullSelectList(){
        return this.optionsList;
    }
    
    /**
     * Returns an option's value.
     * @param label The label of an option.
     * @return the value of the option for this label.
     * @throws DeviceControlException If the option does not exist.
     */
    public final Object getOptionValue(String label) throws DeviceControlException {
        for(Map.Entry<String,Object> item:optionsList.entrySet()){
            if(item.getKey().equals(label)){
                return item.getValue();
            }
        }
        throw new DeviceControlException("Option label " + label + " does not exist");
    }
    
    /**
     * Returns the mapped real value
     * @return current selected option value.
     */
    @Override
    public final Object getValueData(){
        return this.getValue();
    }
    
    /**
     * Do not use, not finished yet.
     * @param command Do not use yet.
     * @return Complete structure to be used with the JSONConnector.
     * @throws DeviceControlCommandException When the type is not of type SelectCommand
     */
    public DeviceCommandStructure createSendCommand(DeviceControlCommand command) throws DeviceControlCommandException {
        throw new DeviceControlCommandException("Select control is not finished yet");
    }
    
}