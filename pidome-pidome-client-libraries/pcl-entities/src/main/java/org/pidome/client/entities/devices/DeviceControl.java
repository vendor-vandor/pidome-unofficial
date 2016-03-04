/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.devices;

import java.util.HashMap;
import java.util.Map;
import org.pidome.pcl.utilities.properties.ObjectPropertyBindingBean;

/**
 * The base device control class. 
 * USed by the controls.
 * @author John
 */
public abstract class DeviceControl {
    
    /**
     * The visual type is used to visualize a control in for example a map.
     * Future implementations depending on this type will include messaging
     * based on battery level.
     */
    public enum VisualType {
        /**
         * Light in lux.
         */
        LIGHT_LUX,
        /**
         * Light intensity in percentage
         * */
        LIGHT_PERC,
        /**
         * Battery level.
         */
        BATTERY,
        /**
         * No visuals type
         */
        NONE,
        /**
         * A temperature type (in C).
         */
        TEMPERATURE_C,
        /**
         * A temperature type (in F).
         */
        TEMPERATURE_F,
        /**
         * A movement type.
         */
        MOVEMENT,
        /**
         * Value from a computer like memory, load, etc.
         */
        PCVALUE,
        /**
         * Humidity
         */
        HUMIDITY,
        /**
         * Pressure
         */
        PRESSURE,
        /**
         * Fluids like rain, water, general fluids.
         */
        FLUID,
        /**
         * Wind speed, gusts, etc.
         */
        WIND;
    }
    
    /**
     * The device control type.
     * This influences how controls can be visualized and defines available
     * commands and control structure.
     */
    public enum DeviceControlType {
        /**
         * shows data as is.
         */
        DATA, 
        /**
         * A single button, when pressed sends a true to activate.
         */
        BUTTON, 
        /**
         * A two state switch, true = on, false = off.
         */
        TOGGLE, 
        /**
         * Multi select. The selected item active when selected.
         */
        SELECT, 
        /**
         * A ranged controller with a minimum and maximum value.
         */
        SLIDER, 
        /**
         * Control with HSB values.
         */
        COLORPICKER;
    }
    
    /**
     * The data type of a control.
     * Some controls support multiple datatypes. You need this to determine
     * the datatype used by a control.
     */
    public enum DeviceControlDataType {
        /**
         * String data type.
         */
        STRING, 
        /**
         * Integer type.
         */
        INTEGER, 
        /**
         * Float type.
         */
        FLOAT, 
        /**
         * String in with hex value.
         */
        HEX, 
        /**
         * Boolean value
         */
        BOOLEAN, 
        /**
         * HashMap containing floats with h,s,b keys.
         */
        COLOR;
    }
    
    /**
     * The id of the control.
     */
    private final String controlId;
    /**
     * Name of th control.
     */
    private final String controlName = "";
    /**
     * The control type.
     */
    private final DeviceControlType controlType;
    /**
     * The command extra command.
     */
    private String commandExtra;
    /**
     * If a specific control is hidden t should not be shown.
     */
    private boolean isHidden = false;
    /**
     * Control's used datatype.
     */
    private DeviceControlDataType dataType = DeviceControlDataType.STRING;
    
    /**
     * If an shortcut is assigned this is true.
     * Shortcuts are used to show the control in listings.
     */
    boolean hasShortcut = false;
    
    /**
     * Shortcut position assigned.
     */
    private int shortCutPosition = 0;
    
    /**
     * The last known control value.
     */
    private final ObjectPropertyBindingBean lastValue = new ObjectPropertyBindingBean();
    /**
     * Control description.
     */
    private String name = "";
    
    /**
     * The visual type of this control.
     * This is mostly used with data controls, but in the future can be used with other controls.
     */
    private VisualType visualType  = VisualType.NONE;
    
    /**
     * The group this control belongs to.
     */
    private DeviceControlGroup group;
    
    /**
     * Constructor.
     * @param type The control type being constructed.
     * @param controlId The id of the control.
     */
    protected DeviceControl(DeviceControlType type, String controlId){
        this.controlId   = controlId;
        this.controlType = type;
    }
    
    /**
     * Sets the device this control belongs to.
     * @param group The controls group
     */
    protected final void setGroup(DeviceControlGroup group){
        this.group = group;
    }
    
    /**
     * Returns the control group this control belongs to.
     * @return The controls group.
     */
    public final DeviceControlGroup getControlGroup(){
        return this.group;
    }
    
    /**
     * Returns the command extra value.
     * @return The extra value, currently only pre defined with the color picker.
     */
    public final String getCommandExtra(){
        return this.commandExtra;
    }
    
    /**
     * Sets the initial minimal data.
     * @param data The control data.
     * @throws DeviceControlException When control data is incomplete.
     */
    protected final void setInitialDataStructure(Map<String,Object> data) throws DeviceControlException {
        setName((String)data.get("label"));
        setDataTypeByString((String)data.get("datatype"));
        if(data.containsKey("extra")){
            commandExtra = (String)data.get("extra");
        }
        if(data.containsKey("hidden") && data.get("hidden").equals("true")){
            isHidden = true;
        }
        if(data.containsKey("shortcut")){
            hasShortcut = true;
            shortCutPosition = ((Long)data.get("shortcut")).intValue();
        }
        if(data.containsKey("visual") && (boolean)data.get("visual")==true){
            
            switch((String)data.get("visualtype")){
                case "luxlevel":
                    visualType = VisualType.LIGHT_LUX;
                break;
                case "lightpercentage":
                    visualType = VisualType.LIGHT_PERC;
                break;
                case "temperature":
                    visualType = VisualType.TEMPERATURE_C;
                break;
                case "temperatureF":
                    visualType = VisualType.TEMPERATURE_F;
                break;
                case "pcvalue":
                    visualType = VisualType.PCVALUE;
                break;
                case "humidity":
                    visualType = VisualType.HUMIDITY;
                break;
                case "pressure":
                    visualType = VisualType.PRESSURE;
                break;
                case "fluid":
                    visualType = VisualType.FLUID;
                break;
                case "wind":
                    visualType = VisualType.WIND;
                break;
                case "move":
                    if(data.get("datatype").equals("boolean")){
                        visualType = VisualType.MOVEMENT;
                    }
                break;
                case "battery":
                    if(data.get("datatype").equals("float") || data.get("datatype").equals("integer")){
                        visualType = VisualType.BATTERY;
                    }
                break;
                default:
                    visualType = VisualType.NONE;
                break;
            }
        }
    }
    
    /**
     * Sets the field the fields datatype.
     * @param dataType The datatype this control uses.
     */
    protected final void setDatatype(DeviceControlDataType dataType){
        this.dataType = dataType;
    }
    
    /**
     * Sets the datatype based on the datatype string representation.
     * @param type The datatype this control uses represented in string format.
     * @throws DeviceControlException When an unknown datatype is used.
     */
    protected final void setDataTypeByString(String type) throws DeviceControlException{
        if(type==null || type.isEmpty()) throw new DeviceControlException("Need to supply a correct datatype");
        switch(type){
            case "string":
                setDatatype(DeviceControlDataType.STRING);
            break;
            case "integer":
                setDatatype(DeviceControlDataType.INTEGER);
            break;
            case "float":
                setDatatype(DeviceControlDataType.FLOAT);
            break;
            case "boolean":
                setDatatype(DeviceControlDataType.BOOLEAN);
            break;
            case "hex":
                setDatatype(DeviceControlDataType.HEX);
            break;
            case "color":
                setDatatype(DeviceControlDataType.COLOR);
            break;
            default:
                throw new DeviceControlException("Datatype " + type + " is unsupported");
        }
    }
    
    /**
     * Sets the shortcut position.
     * @param position an integer from 0 to 2 giving three positions.
     */
    protected void setShortCutPosition(int position){
        this.shortCutPosition = position;
    }
    
    /**
     * Sets the name.
     * @param description A control's name.
     */
    protected final void setName(String description) {
        this.name = description;
    }
    
    /**
     * Returns the field data type.
     * @return Returns the datatype.
     */
    public final DeviceControlDataType getDataType(){
        return this.dataType;
    }
    
    /**
     * Returns the type of control it is.
     * @return The control type.
     */
    public final DeviceControlType getControlType(){
        return this.controlType;
    }
    
    /**
     * Returns the name.
     * @return the name as String.
     */
    public final String getName(){
        return this.name;
    }
    
    /**
     * Returns true if a shortcut is assigned.
     * @return true if a shortcut is assigned.
     */
    public final boolean hasShortCut(){
        return hasShortcut;
    }
    
    /**
     * Returns the shortcut position.
     * @return The position of this shortcut.
     */
    public final int getShortCutPosition(){
        return this.shortCutPosition;
    }
    
    /**
     * Returns the id of the control.
     * @return The id of the control.
     */
    public final String getControlId(){
        return this.controlId;
    }
    
    /**
     * Returns the name of the control.
     * @return The name of the control.
     */
    public final String getControlName(){
        return this.controlName;
    }
    
    /**
     * Returns data by it's data type.
     * @param data A string representation of the data.
     * @return The data as meant by the setDataType.
     */
    public final Object getDatatypeData(String data){
        switch(dataType){
            case INTEGER:
                return Integer.parseInt(data);
            case BOOLEAN:
                return Boolean.valueOf(data);
            case FLOAT:
                return Float.valueOf(data);
            default:
                return data;
        }
    }
    
    /**
     * Sets the last known data.
     * Be sure to use primitives and not the objects!
     * @param value Only use this if you are sure using the correct datatype.
     */
    protected void setLastKnownValue(Object value){
        switch(dataType){
            case INTEGER:
                lastValue.setValue(Integer.parseInt(value.toString()));
            break;
            case BOOLEAN:
                lastValue.setValue(Boolean.parseBoolean(value.toString()));
            break;
            case FLOAT:
                lastValue.setValue(Float.parseFloat(value.toString()));
            break;
            case COLOR:
                lastValue.setValue(value);
            break;
            default:
                lastValue.setValue(value.toString());
            break;
        }
    }
    
    /**
     * Sets the last known data where you MUST use the correct datatype yourself.
     * Use setLastKnownValue if you are not sure about your used datatype.
     * @param value Only use this if you are sure using the correct datatype.
     */
    protected final void setLastKnownValueKnownDatatype(Object value){
        lastValue.setValue(value);
    }
    
    /**
     * returns the button set value.
     * @return Returns the value as their primitive type. Use getDataType to determine this.
     */
    public final Object getValue(){
        return lastValue.getValue();
    }
    
    /**
     * Returns the value binding.
     * This would be the best function to use if you want to keep live updated
     * of changes. But if you use this you need all the types in advance
     * like datatype and control type used.
     * @return Returns a bean to listen to. It uses the primitive types.
     */
    public final ObjectPropertyBindingBean getValueProperty(){
        return this.lastValue;
    }
    
    /**
     * Returns this control's visual type.
     * @return The visual type to be used for example in maps.
     */
    public final VisualType getVisualType(){
        return this.visualType;
    }
    
    /**
     * Returns the current value.
     * @return The real value data.
     */
    public abstract Object getValueData();
 
    /**
     * Class for the correct structure for a device command to be send.
     */
    public static class DeviceCommandStructure {
        
        /**
         * The full parameter set to be returned.
         */
        private final Map<String,Object> parameters = new HashMap<>();
        
        /**
         * Constructs a command set.
         * @param control The control where this command is for.
         * @param parameters Parameters as set by iplementing classes, refer to the RPC spec.
         * @param extra A control's configured extra field.
         */
        protected DeviceCommandStructure(DeviceControl control, Object parameters, String extra){
            this.parameters.put("id", control.getControlGroup().getDevice().getDeviceId());
            this.parameters.put("group", control.getControlGroup().getGroupId());
            this.parameters.put("control", control.getControlId());
            Map<String,Object>ValueSet = new HashMap<>();
            ValueSet.put("value", parameters);
            ValueSet.put("extra", extra);
            this.parameters.put("action", ValueSet);
        }
        
        /**
         * Returns the command parameters.
         * @return Returns a Map ready to be used in the JSONConnector.
         */
        public final Map<String,Object> getParameters(){
            return parameters;
        }
        
        /**
         * Returns the method to be executed.
         * @return the method string.
         */
        public final String getMethod(){
            return "DeviceService.sendDevice";
        }
        
        /**
         * Returns the id to be used.
         * @return The id string, often the method string.
         */
        public final String getId(){
            return getMethod();
        }
        
    }
    
}
