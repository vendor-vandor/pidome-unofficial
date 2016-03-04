/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.plugins.graphdata;

/**
 *
 * @author John
 */
public class RoundRobinDataGraphItem {
    
    public enum FieldType {
        SUM,AVERAGE,ABSOLUTE
    }
    
    FieldType type = FieldType.AVERAGE;
    String fieldName;
    String groupName;
    
    Double curData = 0D;
    
    public RoundRobinDataGraphItem(String groupName, String fieldName, FieldType type){
        this.type = type;
        this.fieldName = fieldName;
        this.groupName = groupName;
    }
    
    /**
     * Returns the type of field.
     * @return 
     */
    public final FieldType getFieldType(){
        return type;
    }
    
    /**
     * Returns the field name.
     * @return 
     */
    public final String getFieldName(){
        return this.groupName +"_"+ this.fieldName;
    }
    
    /**
     * Resets the data to starting point.
     * @param dataName 
     */
    public final void resetData(){
        curData = 0D;
    }
    
    /**
     * Sets data values for this field
     * @param data 
     */
    public final void handleData(final Double data) {
        curData = data;
    }
    
    /**
     * Returns last known data.
     * @return 
     */
    public final double getCurData(){
        return this.curData;
    }    
}
