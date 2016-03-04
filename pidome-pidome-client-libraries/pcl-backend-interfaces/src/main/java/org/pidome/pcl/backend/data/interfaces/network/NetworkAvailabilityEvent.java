/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.network;

/**
 *
 * @author John
 */
public class NetworkAvailabilityEvent extends java.util.EventObject {
    
    /**
     * Holds the event type.
     */
    NetworkAvailabilityProvider.Status EVENT_TYPE = null;
    
    /**
     * Constructor
     * @param source NetInterface
     * @param eventType Event type.
     */
    public NetworkAvailabilityEvent(NetworkAvailabilityProvider source, NetworkAvailabilityProvider.Status eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    /**
     * Returns the network interface source.
     * @return The network interface.
     */
    @Override
    public final NetworkAvailabilityProvider getSource(){
        return (NetworkAvailabilityProvider)super.getSource();
    }
    
    /**
     * Event type
     * @return the event type of network available or not.
     */
    public NetworkAvailabilityProvider.Status getEventType(){
        return EVENT_TYPE;
    }
}