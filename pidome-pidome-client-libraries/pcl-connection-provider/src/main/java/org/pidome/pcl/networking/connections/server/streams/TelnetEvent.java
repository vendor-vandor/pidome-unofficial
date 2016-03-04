/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.pcl.networking.connections.server.streams;


/**
 * a Telnet connection event.
 * @author John Sirach
 */
public class TelnetEvent extends java.util.EventObject {
    
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
    public TelnetEvent( TelnetConnectionInterface source, EventType eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    /**
     * Returns raw source.
     * @return The Telnet data source.
     */
    @Override
    public final TelnetConnectionInterface getSource(){
        return (TelnetConnectionInterface)super.getSource();
    }
    
    /**
     * Returns the event type.
     * @return The event type.
     */
    public final EventType getEventType(){
        return EVENT_TYPE;
    }
 
}
