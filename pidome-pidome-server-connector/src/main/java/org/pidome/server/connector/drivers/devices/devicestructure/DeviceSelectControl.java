/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author John
 */
public class DeviceSelectControl extends DeviceControl {

    List<Map<String,Object>> optionsList = new ArrayList<>();
    
    /**
     * Constructor.
     * @param controlId
     * @throws DeviceControlException 
     */
    protected DeviceSelectControl(DeviceControlsGroup group, String controlId) throws DeviceControlException {
        super(group,DeviceControlType.SELECT, controlId);
    }
 
    /**
     * Creates the set control.
     * @param data
     * @throws DeviceControlException 
     */
    protected final void setSelectControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialData(data);
        if(data.containsKey("parameters")){
            for(Map<String,Object>option:(List<Map<String,Object>>)data.get("parameters")){
                if(!option.containsKey("value") || ((String)option.get("value")).isEmpty() || 
                    !option.containsKey("label") || ((String)option.get("label")).isEmpty() || ((String)option.get("label")).length()>20){
                    throw new DeviceControlException("Check your select tag. Incorrect parameter setup");
                }
                Map<String,Object> newAttributes = new HashMap<>();
                newAttributes.put("label", option.get("label"));
                newAttributes.put("value", getDatatypeData((String)option.get("value")));
                optionsList.add(newAttributes);
            }
        } else {
            throw new DeviceControlException("Empty select list is not possible");
        }
    }
    
    /**
     * Returns an option's label.
     * @param position
     * @return
     * @throws DeviceControlException 
     */
    public final String getOptionLabel(int position) throws DeviceControlException {
        try {
            return (String)optionsList.get(position).get("label");
        } catch (Exception ex){
            throw new DeviceControlException("Index " + position + " does not exist");
        }
    }

    /**
     * Resturns the complete select list.
     * @return 
     */
    public final List<Map<String,Object>> getFullSelectList(){
        return this.optionsList;
    }
    
    /**
     * Returns an option's value.
     * @param position
     * @return
     * @throws DeviceControlException 
     */
    public final Object getOptionValue(int position) throws DeviceControlException {
        try {
            return optionsList.get(position).get("value");
        } catch (Exception ex){
            throw new DeviceControlException("Index " + position + " does not exist");
        }
    }
    
    /**
     * Returns the mapped real value
     * @return 
     */
    @Override
    public final Object getValueData(){
        return this.getValue();
    }
    
}
