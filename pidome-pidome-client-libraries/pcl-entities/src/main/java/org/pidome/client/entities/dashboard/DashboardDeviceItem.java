/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.Map;

/**
 * Device data.
 * @author John
 */
public class DashboardDeviceItem extends DashboardItem {

    /**
     * Device id.
     */
    private int deviceId = 0;
    /**
     * Group id.
     */
    private String groupId = "";
    /** 
     * Control id.
     */
    private String controlId = "";
    
    /**
     * Min value used by gauges
     */
    private double minValue = 0.0;
    /**
     * Max value used by gauges
     */
    private double maxValue = 0.0;
    
    /**
     * Warn value used by gauges
     */
    private double warnValue = 0.0;
    /**
     * High value used by gauges
     */
    private double highValue = 0.0;
    
    /**
     * The visual type requested.
     */
    private VisualType visualType = VisualType.NONE;
    
    /**
     * Some controls have the option to show device name instead of control name.
     */
    private boolean showDeviceName = false;
    
    /**
     * Custom label used by gauges
     */
    private String customLabel = "";
    
    /**
     * Available visual types.
     */
    public enum VisualType {
        TEXT,GRAPH,GAUGE,NONE;
    }
            
    /**
     * Constructor.
     */
    public DashboardDeviceItem() {
        super(ItemType.DEVICE);
    }

    /**
     * Creates this item's config.
     * @param config Map of configuration values from the RPC
     */
    @Override
    protected void setConfig(Map<String, Object> config) {
        this.deviceId    = Integer.valueOf((String)config.get("data-id"));
        this.groupId     = (String)config.get("data-group");
        this.controlId   = (String)config.get("data-control");
        
        this.minValue    = createNumber((String)config.get("data-minvalue"));
        this.maxValue    = createNumber((String)config.get("data-maxvalue"));
        this.warnValue   = createNumber((String)config.get("data-warnvalue"));
        this.highValue   = createNumber((String)config.get("data-highvalue"));
        
        this.showDeviceName = config.containsKey("data-showdevice") && config.get("data-showdevice").equals("true");
        
        if(config.containsKey("data-visual")){
            switch((String)config.get("data-visual")){
                case "gauge":
                    this.visualType = VisualType.GAUGE;
                break;
                case "text":
                    this.visualType = VisualType.TEXT;
                break;
                case "graph":
                    this.visualType = VisualType.GRAPH;
                break;
            }
        }
        if(config.containsKey("data-customlabel")){
            this.customLabel = (String)config.get("data-customlabel");
        }
    }
 
    /**
     * Returns a double formatted number from a string.
     * Returns 0 if there is no data or failure in converting.
     * @param data Number as a string.
     * @return Always a double value of the given string.
     */
    private double createNumber(String data){
        try {
            return Double.valueOf(data);
        } catch (Exception ex){
            //// Unused item.
            return 0D;
        }
    }
    
    /**
     * Returns the device id.
     * @return int id.
     */
    public final int getDeviceId(){
        return this.deviceId;
    }
    
    /**
     * Returns the group id.
     * @return String id.
     */
    public final String getGroupId(){
        return this.groupId;
    }

    /**
     * Returns the control id.
     * @return String id.
     */
    public final String getControlId(){
        return this.controlId;
    }
    
    /**
     * The minimum value used by gauges
     * @return Number to be used.
     */
    public final Number getMinValue(){
        return this.minValue;
    }
    
    /**
     * The maximum value used by gauges
     * @return Number to be used.
     */
    public final Number getMaxValue(){
        return this.maxValue;
    }
    
    /**
     * The warning value used by gauges
     * @return Number to be used.
     */
    public final Number getWarnValue(){
        return this.warnValue;
    }
    
    /**
     * The high value used by gauges
     * @return Number to be used.
     */
    public final Number getHighValue(){
        return this.highValue;
    }
    
    /**
     * Visual type requested.
     * @return VisualType enum
     */
    public final VisualType getVisualType(){
        return this.visualType;
    }
    
    /**
     * The custom label used by gauges
     * @return String label.
     */
    public final String getCustomLabel(){
        return this.customLabel;
    }
    
    /**
     * Returns if the device name should be shown.
     * Some controls (data text) have the option to show device name instead of
     * the control name. Use this to determine.
     * @return boolean if device name should be shown.
     */
    public final boolean getShowDeviceName(){
        return this.showDeviceName;
    }
    
}