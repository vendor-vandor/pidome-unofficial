/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices;

import java.text.NumberFormat;
import java.text.ParseException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlDataType;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlType;

/**
 * Device command request.
 * @author John
 */
public final class DeviceCommandRequest {
    
    private Object commandValue;
    
    private Object commandValueData;
    
    private String extraValue;
    
    private final DeviceControl deviceControl;
    
    String groupId;
    String controlId;
    
    /**
     * Constructor.
     */
    public DeviceCommandRequest(DeviceControl deviceControl){
        this.deviceControl = deviceControl;
    }
    
    /**
     * Sets the command group.
     * @param group 
     */
    public final void setGroupId(String group){
        this.groupId = group;
    }
    
    /**
     * Returns the command group.
     * @return 
     */
    public final String getGroupId(){
        return this.groupId;
    }
    
    /**
     * Returns the control id
     * @return 
     */
    public final String getControlId(){
        return this.deviceControl.getControlId();
    }
    
    /**
     * Returns the control type.
     * @return 
     */
    public final DeviceControlType getControlType(){
        return this.deviceControl.getControlType();
    }
    
    /**
     * Returns the datatype of the control.
     * @return 
     */
    public final DeviceControlDataType getDataType(){
        return this.deviceControl.getDataType();
    }
    
    /**
     * Sets the command value.
     * @param value 
     */
    public final void setCommandValue(Object value){
        switch(this.deviceControl.getDataType()){
            case INTEGER:
                if(value instanceof Number){
                    this.commandValue = ((Number)value).intValue();
                } else if(value.toString().contains(".")){
                    try {
                        this.commandValue = NumberFormat.getInstance().parse(value.toString()).intValue();
                    } catch (ParseException ex) {
                        this.commandValue = value;
                    }
                }
            break;
            default:
                this.commandValue = value;
            break;
        }
    }
    
    /**
     * Sets the real data as defined in the control.
     * @param data 
     */
    public final void setCommandValueData(Object data){
        this.commandValueData = data;
    }
    
    /**
     * Returns the command value.
     * @return 
     */
    public final Object getCommandValue(){
        return this.commandValue;
    }
    
    /**
     * Returns the command value.
     * @return 
     */
    public final Object getCommandValueData(){
        return this.commandValueData;
    }
    
    /**
     * Sets the command value.
     * @param value 
     */
    public final void setExtraValue(String value){
        this.extraValue = value;
    }
    
    /**
     * Returns true if there is an extra value.
     * @return 
     */
    public final boolean hasExtra(){
        return (extraValue!=null);
    }
    
    /**
     * Returns the extra value.
     * @return 
     */
    public final String getExtraValue(){
        return extraValue;
    }
    
    /**
     * Returns the control where it's all about.
     * @return 
     */
    public final DeviceControl getControl(){
        return this.deviceControl;
    }
    
}