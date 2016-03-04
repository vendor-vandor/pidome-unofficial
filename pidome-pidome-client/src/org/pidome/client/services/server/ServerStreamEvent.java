/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.services.server;

/**
 *
 * @author John Sirach
 */
public class ServerStreamEvent extends java.util.EventObject {
    
    public static final String CONNECTIONAVAILABLE = "CONNECTIONAVAILABLE";
    public static final String CONNECTIONLOST      = "CONNECTIONLOST";
    public static final String DATARECEIVED        = "DATARECEIVED";
    
    Object eventData;
    
    String EVENT_TYPE = null;
    
    
    public ServerStreamEvent( Object source, String eventType ) {
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
