/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.devices;

/**
 *
 * @author John Sirach
 */
public class DeviceValueChangeEvent  extends java.util.EventObject {
    
    public static final String VALUECHANGED = "VALUECHANGED";
    public static final String GLOBALCHANGE = "GLOBALCHANGE";
    
    String setName;
    String groupName;
    Object value;
    
    String EVENT_TYPE = null;
    
    public DeviceValueChangeEvent( Device source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public DeviceValueChangeEvent(String eventType){
        super(new Object());
        EVENT_TYPE = eventType;
    }
    
    public final void setValues(String group, String set, Object val){
        groupName = group;
        setName = set;
        value = val;
    }
    
    public final String getGroup(){
        return groupName;
    }
    
    public final String getSet(){
        return setName;
    }
    
    public final Object getValue(){
        return value;
    }
    
    @Override
    public final Device getSource(){
        return (Device) super.source;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
}
