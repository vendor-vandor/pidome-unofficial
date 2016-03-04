/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes;


/**
 *
 * @author John Sirach
 */
public class MainSceneEvent extends java.util.EventObject {
    
    public static final String SCENEBUILDDONE = "SCENEBUILDDONE";
    
    String EVENT_TYPE = null;
    
    
    public MainSceneEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
 
}
