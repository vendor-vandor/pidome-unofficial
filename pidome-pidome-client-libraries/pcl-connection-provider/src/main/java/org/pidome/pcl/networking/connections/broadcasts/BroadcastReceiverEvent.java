/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.pcl.networking.connections.broadcasts;

import org.pidome.pcl.backend.data.interfaces.network.NetworkBroadcastReceiverInterface;


/**
 * Event used when there is an broadcast event occuring.
 * @author John Sirach
 */
public class BroadcastReceiverEvent extends java.util.EventObject {

    /**
     * The broadcasted status.
     */
    NetworkBroadcastReceiverInterface.BroadcastStatus EVENT_TYPE = null;
    
    
    public BroadcastReceiverEvent( BroadcastReceiver source, NetworkBroadcastReceiverInterface.BroadcastStatus eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public NetworkBroadcastReceiverInterface.BroadcastStatus getEventType(){
        return EVENT_TYPE;
    }
 
    @Override
    public final BroadcastReceiver getSource(){
        return (BroadcastReceiver)super.getSource();
    }
    
}
