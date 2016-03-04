/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.server.connector.drivers.peripherals.hardware;

/**
 * Event for peripheral events, loaded and when unloading.
 * The source for this event is the driver used.
 * @author John Sirach
 */
public class PeripheralEvent extends java.util.EventObject {
    
    public final static String DRIVER_LOADED  = "DRIVER_LOADED";
    public final static String DRIVER_UNLOAD  = "DRIVER_UNLOAD";
    
    public final static String READ_TIMEOUT   = "READ_TIMEOUT";
    public final static String DATA_RECEIVED  = "DATA_RECEIVED";
    
    
    private String EVENT_TYPE = null;
    
    private String stringData;
    
    public PeripheralEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public void setStringData(String data){
        stringData = data;
    }
    
    public String getStringData(){
        return stringData;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
}