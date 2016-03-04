/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.userpresence;

/**
 *
 * @author John Sirach
 */
public class UserPresenceEvent extends java.util.EventObject {
    
    public static final String PRESENCEADDED = "PRESENCEADDED";
    public static final String PRESENCEREMOVED = "PRESENCEREMOVED";
    public static final String PRESENCEUPDATED = "PRESENCEUPDATED";
    public static final String PRESENCECHANGED = "PRESENCECHANGED";
    
    int presenceId;
    String presenceName;
    
    String EVENT_TYPE = null;
    
    public UserPresenceEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    public void setPresenceData(int id, String name){
        presenceId = id;
        presenceName = name;
    }
    
    public int getPresenceId(){
        return presenceId;
    }
    
    public String getPresenceName(){
        return presenceName;
    }
    
}
