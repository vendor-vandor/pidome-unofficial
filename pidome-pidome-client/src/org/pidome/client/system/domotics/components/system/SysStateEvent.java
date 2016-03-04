/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.system;

/**
 *
 * @author John Sirach
 */
public class SysStateEvent extends java.util.EventObject {
    
    public static final String SYSSTATECHANGED = "SYSSTATECHANGED";
    public static final String SYSSTATELISTADDED = "SYSSTATELISTADDED";
    public static final String SYSSTATELISTREMOVED = "SYSSTATELISTREMOVED";
    
    Object eventData;
    
    String EVENT_TYPE = null;
    
    public SysStateEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    public void setData(Object data){
        eventData = data;
    }
    
    public Object getData(){
        return eventData;
    }
}
