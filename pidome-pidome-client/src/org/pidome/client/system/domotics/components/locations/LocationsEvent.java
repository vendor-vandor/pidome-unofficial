/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.locations;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author John Sirach
 */
public class LocationsEvent extends java.util.EventObject {
    
    public static final String LOCATIONADDED = "LOCATIONADDED";
    public static final String LOCATIONREMOVED = "LOCATIONREMOVED";
    public static final String LOCATIONUPDATED = "LOCATIONUPDATED";
    
    int locationId;
    String locationName;
    
    String EVENT_TYPE = null;
    
    public LocationsEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    public void setLocationData(int id, String name){
        locationId = id;
        locationName = name;
    }
    
    public int getLocationId(){
        return locationId;
    }
    
    public String getLocationName(){
        return locationName;
    }
    
}
