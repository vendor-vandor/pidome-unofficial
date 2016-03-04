/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.network;


/**
 *
 * @author John Sirach
 */
public class BroadcastReceiverEvent extends java.util.EventObject {
    public static final String BROADCASTRECEIVED  = "BROADCASTRECEIVED";
    public static final String BROADCASTDISABLED  = "BROADCASTDISABLED";
    
    String EVENT_TYPE = null;
    
    
    public BroadcastReceiverEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
 
    @Override
    public final BroadcastReciever getSource(){
        return (BroadcastReciever)source;
    }
    
}
