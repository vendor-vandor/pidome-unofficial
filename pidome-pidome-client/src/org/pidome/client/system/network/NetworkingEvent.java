/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network;


/**
 *
 * @author John Sirach
 */
public class NetworkingEvent extends java.util.EventObject {
    
    public static final String NETWORKAVAILABLE   = NetInterfaceEvent.NETWORKAVAILABLE;
    public static final String NETWORKUNAVAILABLE = NetInterfaceEvent.NETWORKUNAVAILABLE;
    public static final String BROADCASTRECEIVED  = BroadcastReceiverEvent.BROADCASTRECEIVED;
    public static final String BROADCASTDISABLED  = BroadcastReceiverEvent.BROADCASTDISABLED;
    
    String EVENT_TYPE = null;
    
    
    public NetworkingEvent( Networking source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    
    @Override
    public final Networking getSource(){
        return (Networking)source;
    }
 
}
