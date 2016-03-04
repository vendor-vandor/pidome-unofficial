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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class DeviceColorPickerControl extends DeviceControl {

    static Logger LOG = LogManager.getLogger(DeviceColorPickerControl.class);
    
    List<Map<String,String>> buttonsList = new ArrayList<>();
    String pickerMode = "hsb";
    
    public DeviceColorPickerControl(DeviceControlsGroup group, String controlId) throws DeviceControlException {
        super(group,DeviceControlType.COLORPICKER, controlId);
    }
 
    
    /**
     * Creates the color picker control.
     * @param data
     * @param buttons
     * @throws DeviceControlException 
     */
    protected final void setColorPickerControlData(Map<String,Object> data) throws DeviceControlException {
        if(!data.containsKey("mode")){
            throw new DeviceControlException("Attribute mode is required in color picker.");
        } else {
            switch((String)data.get("mode")){
                case "rgb":
                case "hsb":
                case "cie":
                case "kelvin":
                case "whites":
                    pickerMode = (String)data.get("mode");
                break;
                default:
                    throw new DeviceControlException("Unsupported color mode: " + data.get("mode"));
            }
        }
        if(!data.containsKey("parameters") || ((List)data.get("parameters")).isEmpty()) throw new DeviceControlException("Check your color picker button child nodes");
        setInitialData(data);
        Map<String,Object> initialValues = new HashMap<>();
        initialValues.put("h", 0.0F);
        initialValues.put("s", 0.0F);
        initialValues.put("b", 0.0F);
        setLastKnownValue(initialValues);
        for(Map<String,String>option:(List<Map<String,String>>)data.get("parameters")){
            if(!option.containsKey("value") || ((String)option.get("value")).isEmpty() || 
                !option.containsKey("label") || ((String)option.get("label")).isEmpty() || ((String)option.get("label")).length()>20){
                throw new DeviceControlException("Check your select tag. Incorrect parameter setup");
            }
            buttonsList.add(option);
        }
    }
 
    /**
     * Returns the full buttons list.
     * @return 
     */
    public final List<Map<String,String>> getFullButtonsList(){
        return buttonsList;
    }
    
    /**
     * Returns an option's label.
     * @param position
     * @return
     * @throws DeviceControlException 
     */
    public final String getButtonLabel(int position) throws DeviceControlException {
        try {
            return buttonsList.get(position).get("label");
        } catch (Exception ex){
            throw new DeviceControlException("Index " + position + " does not exist");
        }
    }

    /**
     * Returns an option's value.
     * @param position
     * @return
     * @throws DeviceControlException 
     */
    public final String getButtonValue(int position) throws DeviceControlException {
        try {
            return buttonsList.get(position).get("value");
        } catch (Exception ex){
            throw new DeviceControlException("Index " + position + " does not exist");
        }
    }
    
    /**
     * Returns the color visible mode.
     * Multiple modes will be supported in the future like hue, daylight etc...
     * @return 
     */
    public final String getMode(){
        return pickerMode;
    }
    
    /**
     * Returns the full color map.
     * @return 
     */
    public final Map<String,Map<String,Object>> getFullColorMap(){
        return (Map<String,Map<String,Object>>)super.getValue();
    }
    
    /**
     * Returns the mapped real value
     * @return 
     */
    @Override
    public final Object getValueData(){
        return this.getValue();
    }
    
    /**
     * Set's the last known value.
     * @param value 
     */
    @Override
    public void setLastKnownValue(Object value){
        setLastKnownValueKnownDatatype(new DeviceColorPickerControlColorData(value).getFullColorSet());
    }
    
    /**
     * Return the RGB values
     * This function returns a map with the keys r,g and b
     * @return 
     */
    public final Map<String,Integer> getRGB(){
        return ((Map<String,Map<String,Integer>>)super.getValue()).get("rgb");
    }
    
    /**
     * Return the RGB values
     * This function returns a map with the keys h,s and b
     * @return 
     */
    public final Map<String,Double> getHSB(){
        return ((Map<String,Map<String,Double>>)super.getValue()).get("hsb");
    }
    
    /**
     * Returns current kelvin value.
     * Kelvin value is only available when kelvin is used to set the data. Otherwise always returns 0.
     * @return 
     */
    public final Long getKelvin(){
        return ((Map<String,Long>)super.getValue()).get("kelvin");
    }
    
    /**
     * Returns current hex value.
     * Returns the hex values as #000000
     * @return 
     */
    public final String getHex(){
        return ((Map<String,String>)super.getValue()).get("hex");
    }
    
}