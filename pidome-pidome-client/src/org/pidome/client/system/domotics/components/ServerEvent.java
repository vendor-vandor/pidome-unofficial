/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components;

/**
 *
 * @author John Sirach
 */
public class ServerEvent extends java.util.EventObject {
    
    public static final String SERVERVERSIONINFOCHANGED = "SERVERVERSIONINFOCHANGED";
    
    String EVENT_TYPE = null;
    
    public ServerEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
}
