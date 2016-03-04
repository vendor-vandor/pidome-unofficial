/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public class DeviceToggleControl extends DeviceControl {

    private final Map<String,Map<String,Object>> yesnoMap = new HashMap<>();
    private final Map<String,Map<String,Object>> mimicMap = new HashMap<>();
    
    /**
     * Constructor.
     * @param controlId
     * @throws DeviceControlException 
     */
    protected DeviceToggleControl(DeviceControlsGroup group, String controlId) throws DeviceControlException {
        super(group,DeviceControlType.TOGGLE, controlId);
        setLastKnownValueKnownDatatype(false);
    }
    
    /**
     * Constructs the toggle button.
     * @param data
     * @param yesno 
     * @throws DeviceControlException 
     */
    protected final void setToggleControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialData(data);
        if(data.containsKey("parameters")){
            createTrueFalseMap((Map<String,Object>)data.get("parameters"));
        } else {
            throw new DeviceControlException("Toggle button without parameters found");
        }
    }
    
    /**
     * Creates the mapping for yes and no child siblings.
     * @param type
     * @param attributes
     * @throws DeviceControlException 
     */
    private void createTrueFalseMap(Map<String,Object> parameters) throws DeviceControlException {
        if(parameters.containsKey("on") && parameters.containsKey("off")){
            createTrueFalseItem("on", (Map<String,Object>)parameters.get("on"));
            createTrueFalseItem("off", (Map<String,Object>)parameters.get("off"));
        }
    }
    
    /**
     * Correctly map the toggle value.
     * @param itemType The yesno type value
     * @param item HAsmpa with values set.
     * @throws DeviceControlException 
     */
    private void createTrueFalseItem(String itemType, Map<String,Object> itemSet) throws DeviceControlException {
        if(!itemSet.containsKey("value") || ((String)itemSet.get("value")).isEmpty()){
            throw new DeviceControlException("Check your '"+itemSet+"' tag in the toggle button control. Incorrect value");
        }
        if(!itemSet.containsKey("label") || ((String)itemSet.get("label")).isEmpty()){
            throw new DeviceControlException("Check your '"+itemSet+"' tag in the toggle button control. Incorrect label");
        }
        Map<String,Object> newAttributes = new HashMap<>();
        newAttributes.put("label", itemSet.get("label"));
        newAttributes.put("value", itemSet.get("value"));
        yesnoMap.put(itemType, newAttributes);
        Map<String,Object> mimicAttributes = new HashMap<>();
        mimicAttributes.put("label", itemSet.get("label"));
        mimicAttributes.put("value", itemType);
        mimicMap.put(itemType, mimicAttributes);
        
    }
    
    /**
     * Returns all the toggle map options.
     * @return 
     */
    public final Map<String,Map<String,Object>> getFullToggleMap(){
        return this.mimicMap;
    }
    
    /**
     * Returns the label for the on tag.
     * @return 
     */
    public final String getOnLabel(){
        return (String)yesnoMap.get("on").get("label");
    }
    
    /**
     * Returns the value for the on tag.
     * @return 
     */
    public final Object getOnValue(){
        return yesnoMap.get("on").get("value");
    }
    
    /**
     * Returns the label for the off tag.
     * @return 
     */
    public final String getOffLabel(){
        return (String)yesnoMap.get("off").get("label");
    }
    
    /**
     * Returns the value for the off tag.
     * @return 
     */
    public final Object getOffValue(){
        return yesnoMap.get("off").get("value");
    }

    /**
     * Returns the mapped real value
     * @return 
     */
    @Override
    public final Object getValueData(){
        switch(String.valueOf(this.getValue())){
            case "true":
            case "on":
                return yesnoMap.get("on").get("value");
            default:
                return yesnoMap.get("off").get("value");
        }
    }
    
    /**
     * Sets the last known data.
     * A toggle control is ALWAYS true or false.
     * @param value 
     */
    @Override
    public void setLastKnownValue(Object value){
        if(String.valueOf(value).equals("true")){
            setLastKnownValueKnownDatatype(true);
        } else {
            setLastKnownValueKnownDatatype(false);
        }
    }
    
}
