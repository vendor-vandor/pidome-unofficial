/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics;

/**
 *
 * @author John Sirach
 */
public final class DomoticsEvent {

    String eventType;
    
    public final static String INITDATARECEIVED = "INITDATARECEIVED";
    public final static String INITDATAUPDATED  = "INITDATAUPDATED";
    
    public DomoticsEvent(String setEventType){
        eventType = setEventType;
    }
    
    public final String getEventType(){
        return eventType;
    }
    
}
