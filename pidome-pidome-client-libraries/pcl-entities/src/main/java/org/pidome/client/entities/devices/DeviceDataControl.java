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
 * This is a read only data control for displaying data using prefixes and suffixes.
 * @author John
 */
public final class DeviceDataControl extends DeviceControl {
    
    public enum BoolVisualType {
        TEXT,COLOR,TEXT_COLOR;
    }
    
    static {
        Logger.getLogger(DeviceDataControl.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Data prefix.
     */
    private String prefix          = "";
    /**
     * Data suffix.
     */
    private String suffix          = "";
    /**
     * Data graph type.
     */
    private GraphType graphType    = GraphType.NONE;
    
    /**
     * Minimum possible value.
     * Only available for numeric data.
     */
    private Number minValue = 0.0;
    /**
     * Maximum possible value.
     * Only available for numeric data.
     */
    private Number maxValue = 0.0;
    /**
     * Warning value.
     * Only available for numeric data.
     */
    private Number warnValue = 0.0;
    /**
     * A high value.
     * Only available for numeric data.
     */
    private Number highValue = 0.0;
    
    /**
     * Object to be able to fetch historical device data.
     */
    private DeviceDataHistoricData historicData;
    
    /**
     * When datatype is boolean this holds the visual representation type.
     */
    private BoolVisualType booltype = BoolVisualType.TEXT;
    /**
     * When datatype is boolean this holds the visual representation when false.
     */
    private String falsetext        = "false";
    /**
     * When datatype is boolean this holds the visual representation when true.
     */
    private String truetext         = "true";
    
    /**
     * Graph data type.
     */
    public enum GraphType {
        /**
         * No graph.
         */
        NONE,
        /**
         * Series graph (averages).
         */
        SERIES,
        /**
         * Series graph (totals).
         */
        TOTAL,
        /**
         * Series graph (Logarithmic).
         */
        LOG
    }
    
    /**
     * Construct a data control.
     * @param fieldId id of this control.
     * @throws DeviceControlException When control construct fails.
     */
    protected DeviceDataControl(String fieldId) throws DeviceControlException {
        super(DeviceControlType.DATA, fieldId);
    }
    
    /**
     * Sets the data controls specific data.
     * @param data Data control specific data.
     * @throws DeviceControlException When data is incomplete.
     */
    protected final void setDataControlData(Map<String,Object> data) throws DeviceControlException {
        setInitialDataStructure(data);
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
                        case "text_color":
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
                if(data.containsKey("graph") && (boolean)data.get("graph")==true){
                    switch((String)data.get("graphtype")){
                        case "time-series":
                            graphType = GraphType.SERIES;
                        break;
                        case "time-totals":
                            graphType = GraphType.TOTAL;
                        break;
                        case "time-log":
                            graphType = GraphType.LOG;
                        break;
                    }
                    historicData = new DeviceDataHistoricData(this);
                }
                switch(getDataType()){
                    case FLOAT:
                        setLastKnownValueKnownDatatype(0.0f);
                    break;
                    case INTEGER:
                        setLastKnownValueKnownDatatype(0);
                    break;
                }
                createDataThresholdValues(data);
            break;
            default:
                throw new DeviceControlException("Datatype " + data.get("datatype") + " is unsupported in data control");
        }
    }
    
    /**
     * Returns the boolean visual type.
     * This is color, text or both. defaults to text.
     * @return BoolVisualType The visual type.
     */
    public final BoolVisualType getBoolVisualType(){
        return this.booltype;
    }
    
    /**
     * Returns the text stored under a false value.
     * @return String text for false status
     */
    public final String getFalseText(){
        return this.falsetext;
    }
    
    /**
     * Returns the text stored under the true value.
     * @return String text for true status
     */
    public final String getTrueText(){
        return this.truetext;
    }
    
    /**
     * Creates numeric threshold data if available.
     * @param data 
     */
    private void createDataThresholdValues(Map<String,Object> data){
        if(data.containsKey("minvalue")){
            minValue = (Number)data.get("minvalue");
        }
        if(data.containsKey("maxvalue")){
            maxValue = (Number)data.get("maxvalue");
        }
        if(data.containsKey("warnvalue")){
            warnValue = (Number)data.get("warnvalue");
        }
        if(data.containsKey("highvalue")){
            highValue = (Number)data.get("highvalue");
        }
    }
    
    /**
     * Minimum possible value.
     * Only available for numeric data. This is user data, and should be threaded that way.
     * @return Numeric minimum value
     */
    public final Number getMinValue(){
        return this.minValue;
    }

    /**
     * Maximum possible value.
     * Only available for numeric data. This is user data, and should be threaded that way.
     * @return Numeric maximum value
     */
    public final Number getMaxValue(){
        return this.maxValue;
    }
    
    /**
     * Warning value.
     * Only available for numeric data. This is user data, and should be threaded that way.
     * @return Numeric warning value
     */
    public final Number getWarnValue(){
        return this.warnValue;
    }
    
    /**
     * A high value.
     * Only available for numeric data. This is user data, and should be threaded that way.
     * @return Numeric high value
     */
    public final Number getHighValue(){
        return this.highValue;
    }
    
    /**
     * Return an object so you are able to get historical device data.
     * @return DeviceDataHistoricData
     */
    public final DeviceDataHistoricData getHistoricData(){
        return this.historicData;
    }
    
    /**
     * Returns if there is graph plotting for this control.
     * @return tur if graph type!=NONE
     */
    public final boolean hasGraph(){
        return !graphType.equals(GraphType.NONE) && historicData != null;
    }
    
    /**
     * Returns the graph type.
     * @return the graph type. NONE for no graph.
     */
    public final GraphType getGraph(){
        return graphType;
    }
    
    /**
     * Returns the prefix.
     * @return Text to be put before data.
     */
    public final String getPrefix(){
        return this.prefix;
    }
    
    /**
     * Returns the suffix.
     * @return The text to be put after the data.
     */
    public final String getSuffix(){
        return this.suffix;
    }

    /**
     * Returns the real data.
     * Use the getDataType() to determine what kind of data it is.
     * @return the primitive data.
     */
    @Override
    public Object getValueData() {
        return this.getValue();
    }
    
}