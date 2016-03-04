/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.devices.uniPi.wsConnection;

/**
 *
 * @author John
 */
public class WSocketEvent extends java.util.EventObject {
    
    /**
     * Possible event types for socket ports.
     */
    public enum EventType {
        /**
         * Connection is available.
         */
        CONNECTIONAVAILABLE,
        /**
         * Connection is lost.
         */
        CONNECTIONLOST,
        /**
         * New data present.
         */
        DATARECEIVED
    }
    
    EventType EVENT_TYPE;
    
    /**
     * Event constructor.
     * @param source The Telnet source, can be both the Telnet and STelnet.
     * @param eventType The type of event being fired.
     */
    public WSocketEvent( WSocketInterface source, EventType eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    /**
     * Returns raw source.
     * @return The Telnet data source.
     */
    @Override
    public final WSocketInterface getSource(){
        return (WSocketInterface)super.getSource();
    }
    
    /**
     * Returns the event type.
     * @return The event type.
     */
    public final EventType getEventType(){
        return EVENT_TYPE;
    }
 
}
