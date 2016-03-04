/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.plugindata;

/**
 *
 * @author John
 */
public class PluginData {
    
    public enum DataType {
        Boolean,String,Integer,Long,Double
    }
    
    DataType type;
    
    Object value;
    
    public PluginData(){}
    
    public Object getSetting(){
        return value;
    }
    
    public DataType getSettingDataType(){
        return type;
    }
    
    public void setSetting(Object value) throws PluginDataException {
        if(value instanceof Boolean){
            setSettingValue((boolean)value);
        } else if(value instanceof String){
            setSettingValue((String)value);
        } else if(value instanceof Integer){
            setSettingValue((int)value);
        } else if(value instanceof Long){
            setSettingValue((long)value);
        } else if(value instanceof Double){
            setSettingValue((double)value);
        } else {
            throw new PluginDataException("Unsupported datatype");
        }
    }
    
    private void setSettingValue(double value){
        type = DataType.Double;
        this.value = value;
    }
    
    private void setSettingValue(long value){
        type = DataType.Long;
        this.value = value;
    }

    private void setSettingValue(int value){
        type = DataType.Integer;
        this.value = value;
    }
    
    private void setSettingValue(String value){
        type = DataType.String;
        this.value = value;
    }
    
    private void setSettingValue(boolean value){
        type = DataType.Boolean;
        this.value = value;
    }
    
}
