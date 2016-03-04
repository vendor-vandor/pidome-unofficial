/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John
 */
public class DeviceStructureCreator {
 
    private final Map<String,Object> device = new HashMap<>();
    
    private String name = "";
    private String description = "";
    
    /**
     * Creates a device structure base
     */
    public DeviceStructureCreator(){
        this.device.put("controlset", new HashMap<String,Object>());
        this.device.put("address", new HashMap<String,Object>());
    }
 
    public final void setName(String name){
        this.device.put("name", name);
        this.name = name;
    }
    
    public final void setDescription(String description){
        this.device.put("description", description);
        this.description = description;
    }
    
    /**
     * Returns the name.
     * @return 
     */
    public final String getName(){
        return this.name;
    }
    
    /**
     * Returns the description.
     * @return 
     */
    public final String getDescription(){
        return this.description;
    }
    
    /**
     * Sets the address definitions.
     * @param dataType
     * @param label
     * @param description 
     */
    public final void createAddressConfiguration(String dataType, String label, String description){
        Map<String,Object>addressField = new HashMap<>();
        addressField.put("datatype", dataType);
        addressField.put("description", label);
        ((Map<String,Object>)this.device.get("address")).put("input", addressField);
        ((Map<String,Object>)this.device.get("address")).put("description", description);
    }
    
    /**
     * Returns the controlsSet part of the device.
     * @return 
     */
    private ArrayList<Map<String,Object>> getControlsGroups(){
        if(!((Map<String,Object>)this.device.get("controlset")).containsKey("groups")){
            ((Map<String,Object>)this.device.get("controlset")).put("groups", new ArrayList<Map<String,Object>>());
        }
        return (ArrayList<Map<String,Object>>)((Map<String,Object>)this.device.get("controlset")).get("groups");
    }
    
    /**
     * Adds a group to the structure.
     * @param id
     * @param label
     * @return 
     */
    public final boolean addGroup(String id, String label){
        for(Map<String,Object> group:getControlsGroups()){
            if(group.containsKey("id") && ((String)group.get("id")).equals(id)){
                return false;
            }
        }
        Map<String,Object> groupDetails = new HashMap<>();
        groupDetails.put("id", id);
        groupDetails.put("label", label);
        groupDetails.put("controls", new ArrayList<Map<String,Object>>());
        getControlsGroups().add(groupDetails);
        return true;
    }
    
    /**
     * Returns a list of controls.
     * @param groupId
     * @return 
     */
    private ArrayList<Map<String,Object>> getControls(String groupId){
        for(Map<String,Object> group:getControlsGroups()){
            if(group.containsKey("id") && ((String)group.get("id")).equals(groupId)){
                return (ArrayList<Map<String,Object>>) group.get("controls");
            }
        }
        return null;
    }
    
    /**
     * Adds a color picker control.
     * @param groupId The group to add the control to
     * @param controlId
     * @param description
     * @param mode
     * @param shortcut
     * @param hidden
     * @param retention
     * @param extra
     * @param colorButtons
     * @param customData
     * @return 
     */
    public final boolean addColorControl(String groupId,
                                            String controlId, 
                                            String description,
                                            String mode,
                                            int shortcut,
                                            boolean hidden,
                                            boolean retention,
                                            String extra,
                                            ArrayList<ColorPickerButtonBuilder> colorButtons,
                                            CustomDataBuilder customData){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        Map<String,Object> control = new HashMap<>();
        control.put("id", controlId);
        control.put("description", description);
        control.put("mode", mode);
        control.put("shortcut", shortcut);
        control.put("hidden", hidden);
        control.put("retention", retention);
        control.put("extra", extra);
        control.put("type", "colorpicker");
        control.put("datatype", "color");
        control.put("parameters", new ArrayList<>());
        for(ColorPickerButtonBuilder button:colorButtons){
            Map<String,Object> data = new HashMap<>();
            data.put("label", button.getLabel());
            data.put("value", button.getValue());
            ((ArrayList<Map<String,Object>>)control.get("parameters")).add(data);
        }
        control.put("customdata", customData.getData());
        controls.add(control);
        return true;
    }
    
    public final boolean addButtonControl(String groupId,
                                            String controlId,
                                            String description,
                                            String datatype,
                                            String label,
                                            Object value,
                                            boolean hidden,
                                            int shortcut,
                                            String extra,
                                            CustomDataBuilder customData){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        Map<String,Object> control = new HashMap<>();
        control.put("id", controlId);
        control.put("description", description);
        control.put("datatype", datatype);
        control.put("label", label);
        control.put("value", value);
        control.put("hidden", hidden);
        control.put("shortcut", shortcut);
        control.put("extra", extra);
        control.put("type", "button");
        control.put("customdata", customData.getData());
        controls.add(control);
        return true;
    }
    
    public final boolean addSliderControl(String groupId,
                                          String controlId,
                                          String description,
                                          String datatype,
                                          boolean hidden,
                                          boolean retention,
                                          int shortcut,
                                          String extra,
                                          SliderDataBuilder parameters,
                                          CustomDataBuilder customData){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        Map<String,Object> control = new HashMap<>();
        control.put("id", controlId);
        control.put("description", description);
        control.put("datatype", datatype);
        control.put("hidden", hidden);
        control.put("retention", retention);
        control.put("shortcut", shortcut);
        control.put("extra", extra);
        control.put("type", "slider");
        
        Map<String,Object> data = new HashMap<>();
        data.put("min", parameters.getMin());
        data.put("max", parameters.getMax());
        
        control.put("parameters", data);
        control.put("customdata", customData.getData());
        controls.add(control);
        return true;
    }
    
    public final boolean addDataControl(String groupId,
                                           String controlId,
                                           String description,
                                           String datatype,
                                           boolean hidden,
                                           boolean retention,
                                           int shortcut,
                                           String prefix,
                                           String suffix,
                                           Number minValue,
                                           Number maxValue,
                                           Number warnValue,
                                           Number highValue,
                                           String visual,
                                           String graph,
                                           CustomDataBuilder customData){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        Map<String,Object> control = new HashMap<>();
        control.put("id", controlId);
        control.put("description", description);
        control.put("datatype", datatype);
        control.put("hidden", hidden);
        control.put("retention", retention);
        control.put("shortcut", shortcut);
        control.put("prefix", prefix);
        control.put("suffix", suffix);
        control.put("minValue", minValue);
        control.put("maxValue", maxValue);
        control.put("warnValue", warnValue);
        control.put("highValue", highValue);
        control.put("visual", "visual");
        control.put("graph", "graph");
        control.put("type", "data");
        control.put("customdata", customData.getData());
        controls.add(control);
        return true;
    }
    
    public boolean addToggleControl(String groupId,
                                    String controlId,
                                    String description,
                                    String datatype,
                                    boolean hidden,
                                    boolean retention,
                                    int shortcut,
                                    String extra,
                                    ToggleDataBuilder parameters,
                                    CustomDataBuilder customData){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        Map<String,Object> control = new HashMap<>();
        control.put("id", controlId);
        control.put("description", description);
        control.put("datatype", datatype);
        control.put("hidden", hidden);
        control.put("retention", retention);
        control.put("shortcut", shortcut);
        control.put("parameters", parameters.getData());
        control.put("type", "toggle");
        control.put("customdata", customData.getData());
        controls.add(control);
        return true;
    }
    
    public boolean addSelectControl(String groupId,
                                    String controlId,
                                    String description,
                                    String datatype,
                                    boolean hidden,
                                    boolean retention,
                                    int shortcut,
                                    String extra,
                                    OptionListBuilder parameters,
                                    CustomDataBuilder customData
                                    ){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        Map<String,Object> control = new HashMap<>();
        control.put("id", controlId);
        control.put("description", description);
        control.put("datatype", datatype);
        control.put("hidden", hidden);
        control.put("retention", retention);
        control.put("shortcut", shortcut);
        control.put("parameters", parameters.getData());
        control.put("type", "select");
        control.put("customdata", customData.getData());
        controls.add(control);
        return true;
    }
    
    private boolean controlCheck(String groupId, String controlId){
        ArrayList<Map<String,Object>> controls = getControls(groupId);
        if(controls == null){
            return false;
        }
        for(Map<String,Object> control:getControls(groupId)){
            if(control.containsKey("id") && ((String)control.get("id")).equals(controlId)){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the collection as a json string.
     * @return
     * @throws Exception 
     */
    public String getCollection() throws Exception {
        Map<String,Object>deviceSet = new HashMap<>();
        deviceSet.put("device", device);
        return getParamCollection(deviceSet);
    }
    
    /**
     * Returns the object structure of the composed device.
     * @return 
     */
    public Map<String,Object> getObjectStructure(){
        Map<String,Object>deviceSet = new HashMap<>();
        deviceSet.put("device", device);
        return deviceSet;
    }
    
    /**
     * Creates the json param collection.
     * @param params
     * @return
     * @throws Exception 
     */
    public static String getParamCollection(Object params) throws Exception{
        if (params==null) {
            return "null";
        } else if (params instanceof ArrayList) {
            StringBuilder returnString = new StringBuilder("[");
            ArrayList list = (ArrayList) params;
            for (int i = 0; i < list.size(); i++) {
                returnString.append(getParamCollection(list.get(i))).append(",");
            }
            if (returnString.toString().contains(",")) {
                returnString.deleteCharAt(returnString.lastIndexOf(","));
            }
            return returnString.append("]").toString();
        } else if (params instanceof HashMap) {
            StringBuilder returnString = new StringBuilder("{");
            HashMap map = (HashMap) params;
            for (Object key : map.keySet()) {
                returnString.append(getParamCollection(key)).append(":").append(getParamCollection(map.get(key))).append(",");
            }
            if (returnString.toString().contains(",")) {
                returnString.deleteCharAt(returnString.lastIndexOf(","));
            }
            return returnString.append("}").toString();
        }
        if (params instanceof Boolean) {
            return ((boolean) params == true ? "true" : "false");
        } else if (params instanceof String) {
            return new StringBuilder("\"").append(params).append("\"").toString();
        } else if (params instanceof Long || params instanceof Integer || params instanceof Double || params instanceof Float) {
            return String.valueOf(params);
        } else {
            try {
                return new StringBuilder("\"").append(params).append("\"").toString();
            } catch (Exception ex){
                throw new Exception(ex.getMessage());
            }
        }
    }
    
    
}