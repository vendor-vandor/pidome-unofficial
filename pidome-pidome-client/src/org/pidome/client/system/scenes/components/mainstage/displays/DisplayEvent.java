/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.displays;

/**
 *
 * @author John Sirach
 */
public class DisplayEvent extends java.util.EventObject {
    
    public static final String LOADINGDONE = "LOADINGDONE";
    public static final String UNLOADDONE  = "UNLOADDONE";
    public static final String UPDATED     = "UPDATED";
    
    String EVENT_TYPE = null;
    
    public DisplayEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }

}
