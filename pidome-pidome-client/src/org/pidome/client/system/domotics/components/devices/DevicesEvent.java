/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.devices;

/**
 *
 * @author John Sirach
 */
public class DevicesEvent extends java.util.EventObject {
    
    public static final String DEVICEADDED   = "DEVICEADDED";
    public static final String DEVICEREMOVED = "DEVICEREMOVED";
    public static final String DEVICEUPDATED = "DEVICEUPDATED";
    
    String locationId;
    String locationName;
    
    String EVENT_TYPE = null;
    
    public DevicesEvent( Device source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    @Override
    public final Device getSource(){
        return (Device) super.source;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
}
