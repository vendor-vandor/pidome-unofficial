/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.dayparts;

/**
 *
 * @author John Sirach
 */
public class DayPartsEvent extends java.util.EventObject {
    
    public static final String DAYPARTADDED = "DAYPARTADDED";
    public static final String DAYPARTREMOVED = "DAYPARTREMOVED";
    public static final String DAYPARTUPDATED = "DAYPARTUPDATED";
    public static final String DAYPARTCHANGED = "DAYPARTCHANGED";
    
    int dayPartId;
    String dayPartName;
    
    String EVENT_TYPE = null;
    
    public DayPartsEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    public void setDayPartData(int id, String name){
        dayPartId = id;
        dayPartName = name;
    }
    
    public int getDayPartId(){
        return dayPartId;
    }
    
    public String getDayPartName(){
        return dayPartName;
    }
    
}
