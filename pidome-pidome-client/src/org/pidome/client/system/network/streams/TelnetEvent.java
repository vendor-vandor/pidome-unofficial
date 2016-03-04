/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network.streams;


/**
 *
 * @author John Sirach
 */
public class TelnetEvent extends java.util.EventObject {
    
    public static final String CONNECTIONAVAILABLE = "CONNECTIONAVAILABLE";
    public static final String CONNECTIONLOST      = "CONNECTIONLOST";
    public static final String DATARECEIVED        = "DATARECEIVED";
    
    String EVENT_TYPE = null;
    
    
    public TelnetEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
 
}
