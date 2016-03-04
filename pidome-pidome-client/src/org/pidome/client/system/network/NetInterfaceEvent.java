/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network;


/**
 *
 * @author John Sirach
 */
public class NetInterfaceEvent extends java.util.EventObject {
    public static final String NETWORKAVAILABLE   = "SERVICEAVAILABLE";
    public static final String NETWORKUNAVAILABLE = "SERVICEUNAVAILABLE";
    
    String EVENT_TYPE = null;
    
    
    public NetInterfaceEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
 
}
