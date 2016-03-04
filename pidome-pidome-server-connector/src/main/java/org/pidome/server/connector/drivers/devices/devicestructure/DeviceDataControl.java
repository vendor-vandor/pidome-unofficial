/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author John
 */
public class DeviceDataControl extends DeviceControl {

    /**
     * Constants for visualizing boolean values.
     */
    public enum BoolVisualType {
        TEXT,COLOR,TEXT_COLOR;
    }
    
    /**
     * Data prefix
     */
    private String prefix           = "";
    /**
     * Data suffix
     */
    private String suffix           = "";
    /**
     * Graph type for numeric data.
     */
    private String graphType        = "";
    /**
     * Interval used to probe devies for data.
     */
    private int interval            = 0;
    /**
     * Command to send to notify a remote device for probing.
     */
    private String intervalCommand  = "";
    /**
     * Minimum value for numeric values.
     */
    private Number minValue;
    /**
     * Maximum value for numeric values.
     */
    private Number maxValue;
    /**
     * Wanring threshold for numeric values.
     */
    private Number warnValue;
    /**
     * High threshold for numeric values.
     */
    private Number highValue;
    /**
     * Visual type for boolean values.
     */
    private BoolVisualType booltype = BoolVisualType.TEXT;
    /**
     * Text to show when boolean false is active.
     */
    private String falsetext        = "false";
    /**
     * Text to show when boolean true is active.
     */
    private String truetext         = "true";
    
    /**
     * Log instance.
     */
    static Logger LOG = LogManager.getLogger(DeviceDataControl.class);
    
    /**
     * Constructor.
     * @param fieldId
     * @throws DeviceControlException 
     */
    protected DeviceDataControl(DeviceControlsGroup group, String fieldId) throws DeviceControlException {
        super(group,DeviceControlType.DATA, fieldId);
    }
    
    /**
     * Sets the data controls specific data.
     * @param data
     * @throws DeviceControlException 
     */
    protected void setDataControlData(Map<String,Object> data) throws DeviceControlException {
        if(!data.containsKey("prefix") || !data.containsKey("suffix")){
            throw new DeviceControlException("Missing prefix and/or suffix attribute in data control");
        }
        setInitialData(data);
        prefix = (String)data.get("prefix");
        suffix = (String)data.get("suffix");
        switch(getDataType()){
            case STRING:
                setLastKnownValueKnownDatatype("");
            break;
            case BOOLEAN:
                setLastKnownValueKnownDatatype(false);
                if(data.containsKey("boolvis")){
                    switch((String)data.get("boolvis")){
                        case "color":
                            booltype = BoolVisualType.COLOR;
                        break;
                        case "text-color":
                            booltype = BoolVisualType.TEXT_COLOR;
                        break;
                        default:
                            booltype = BoolVisualType.TEXT;
                        break;
                    }
                }
                falsetext = (data.containsKey("falsetext"))?(String)data.get("falsetext"):"false";
                truetext = (data.containsKey("truetext"))?(String)data.get("truetext"):"true";
            break;
            case FLOAT:
            case INTEGER:
                if(data.containsKey("graph")){
                    if(data.get("graph").equals("time-series") || data.get("graph").equals("time-totals") || data.get("graph").equals("time-log")){
                        graphType = (String)data.get("graph");
                    }
                }
                setLastKnownValueKnownDatatype(0);
                minValue = (data.containsKey("minvalue"))?(Number)data.get("minvalue"):0;
                maxValue = (data.containsKey("maxvalue"))?(Number)data.get("maxvalue"):0;
                warnValue = (data.containsKey("warnvalue"))?(Number)data.get("warnvalue"):0;
                highValue = (data.containsKey("highvalue"))?(Number)data.get("highvalue"):0;
            break;
            default:
                throw new DeviceControlException("Datatype " + data.get("datatype") + " is unsupported in data control");
        }
        if(data.containsKey("interval") && data.containsKey("intervalcommand")){
            try { 
                interval = ((Number)data.get("interval")).intValue();
                intervalCommand = (String)data.get("intervalcommand");
            } catch (NumberFormatException ex){
               LOG.error("interval set, but not with a valid interval setup in data control: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Returns the boolean visual type.
     * This is color, text or both. defaults to text.
     * @return Tehviausl type.
     */
    public final BoolVisualType getBoolVisualType(){
        return this.booltype;
    }
    
    /**
     * Returns the text stored under a false value.
     * @return 
     */
    public final String getFalseText(){
        return this.falsetext;
    }
    
    /**
     * Returns the text stored under the true value.
     * @return 
     */
    public final String getTrueText(){
        return this.truetext;
    }
    
    /**
     * Returns the min value as an int.
     * @return the min value as int or null when it does not exist.
     * @throws NotAnIntDataTypeException when an int is requested while the datatype is different.
     */
    public final int getMinValueInt() throws NotAnIntDataTypeException {
        if(getDataType()==DeviceControlDataType.INTEGER){
            return (minValue!=null)?minValue.intValue():0;
        } else {
            throw new NotAnIntDataTypeException();
        }
    }
    
    /**
     * Returns the max value as an int.
     * @return the max value as int or null when it does not exist.
     * @throws NotAnIntDataTypeException when an int is requested while the datatype is different.
     */
    public final int getMaxValueInt() throws NotAnIntDataTypeException{
        if(getDataType()==DeviceControlDataType.INTEGER){
            return (maxValue!=null)?maxValue.intValue():0;
        } else {
            throw new NotAnIntDataTypeException();
        }
    }
    
    /**
     * Returns the warn value as an int.
     * @return the warn value as int or null when it does not exist.
     * @throws NotAnIntDataTypeException when an int is requested while the datatype is different.
     */
    public final int getWarnValueInt() throws NotAnIntDataTypeException{
        if(getDataType()==DeviceControlDataType.INTEGER){
            return (warnValue!=null)?warnValue.intValue():0;
        } else {
            throw new NotAnIntDataTypeException();
        }
    }
    
    /**
     * Returns the high value as an int.
     * @return the high value as int or null when it does not exist.
     * @throws NotAnIntDataTypeException when an int is requested while the datatype is different.
     */
    public final int getHighValueInt() throws NotAnIntDataTypeException{
        if(getDataType()==DeviceControlDataType.INTEGER){
            return (highValue!=null)?highValue.intValue():0;
        } else {
            throw new NotAnIntDataTypeException();
        }
    }
    
    /**
     * Returns the min value as a float.
     * @return the min value as a float or null when it does not exist.
     * @throws NotAFloatDataTypeException when a float is requested while the datatype is different.
     */
    public final float getMinValueFloat() throws NotAFloatDataTypeException{
        if(getDataType()==DeviceControlDataType.FLOAT){
            return (minValue!=null)?minValue.floatValue():0.0f;
        } else {
            throw new NotAFloatDataTypeException();
        }
    }
    
    /**
     * Returns the max value as a float.
     * @return the max value as float or null when it does not exist.
     * @throws NotAFloatDataTypeException when a float is requested while the datatype is different.
     */
    public final float getMaxValueFloat() throws NotAFloatDataTypeException{
        if(getDataType()==DeviceControlDataType.FLOAT){
            return (maxValue!=null)?maxValue.floatValue():0.0f;
        } else {
            throw new NotAFloatDataTypeException();
        }
    }
    
    /**
     * Returns the warn value as a float.
     * @return the warn value as float or null when it does not exist.
     * @throws NotAFloatDataTypeException when a float is requested while the datatype is different.
     */
    public final float getWarnValueFloat() throws NotAFloatDataTypeException{
        if(getDataType()==DeviceControlDataType.FLOAT){
            return (warnValue!=null)?warnValue.floatValue():0.0f;
        } else {
            throw new NotAFloatDataTypeException();
        }
    }
    
    /**
     * Returns the high value as a float.
     * @return the high value as float or null when it does not exist.
     * @throws NotAFloatDataTypeException when a float is requested while the datatype is different.
     */
    public final float getHighValueFloat() throws NotAFloatDataTypeException{
        if(getDataType()==DeviceControlDataType.FLOAT){
            return (highValue!=null)?highValue.floatValue():0.0f;
        } else {
            throw new NotAFloatDataTypeException();
        }
    }
    
    /**
     * Returns if there is graph plotting for this control.
     * @return 
     */
    public final boolean hasGraph(){
        return !graphType.isEmpty();
    }
    
    /**
     * Returns the graph type.
     * @return 
     */
    public final String getGraph(){
        return graphType;
    }
    
    /**
     * Returns if a interval has been set for this device.
     * @return 
     */
    public final boolean hasIntervalCommand(){
        return (interval>0 && !intervalCommand.isEmpty());
    }
    
    /**
     * Returns the interval time.
     * @return 
     */
    public final int getInterval(){
        return this.interval;
    }
    
    /**
     * Returns the interval command.
     * @return 
     */
    public final String getIntervalCommand(){
        return this.intervalCommand;
    }
    
    /**
     * Returns the prefix.
     * @return 
     */
    public final String getPrefix(){
        return this.prefix;
    }
    
    /**
     * Returns the suffix.
     * @return 
     */
    public final String getSuffix(){
        return this.suffix;
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