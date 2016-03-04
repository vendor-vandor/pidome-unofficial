/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.macros;

/**
 *
 * @author John Sirach
 */
public class MacroStateEvent extends java.util.EventObject {
    
    public static final String MACROADDED = "MACROADDED";
    public static final String MACROREMOVED = "MACROREMOVED";
    public static final String MACROUPDATED = "MACROUPDATED";
    
    public static final String MACROACTIVE = "MACROACTIVE";
    
    public static final String SCHEDULEDMACROADDED = "SCHEDULEDMACROADDED";
    public static final String SCHEDULEDMACROREMOVED = "SCHEDULEDMACROREMOVED";
    
    Object eventData;
    
    String EVENT_TYPE = null;
    
    public MacroStateEvent( Object source, String eventType ) {
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
