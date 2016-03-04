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

import java.io.ByteArrayOutputStream;

/**
 * Event triggered to be used by drivers when there is a peripheral event concerning receiving data.
 * @author John Sirach
 */
public class PeripheralHardwareDataEvent extends java.util.EventObject {
    
    public final static String READ_TIMEOUT   = "READ_TIMEOUT";
    public final static String DATA_RECEIVED  = "DATA_RECEIVED";
    
    private String EVENT_TYPE = null;
    
    private String stringData = "NO_DATA";
    private ByteArrayOutputStream data;
    
    /**
     * Constructor.
     * @param source
     * @param eventType 
     */
    public PeripheralHardwareDataEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    /**
     * Sets the string data if applicable.
     * @param data 
     */
    public void setStringData(String data){
        stringData = data;
    }
    
    /**
     * Sets the raw data delivered by the device.
     * @param object 
     */
    public void setByteArrayStream(ByteArrayOutputStream object){
        this.data = object;
    }
    
    /**
     * Contains the string data from a device if applicable.
     * @return 
     */
    public String getStringData(){
        return stringData;
    }
    
    /**
     * Contains the raw data delivered by the device.
     * @return 
     */
    public ByteArrayOutputStream getByteArrayOutputStream(){
        return this.data;
    }
    
    /**
     * Returns the device event type.
     * @return 
     */
    public String getEventType(){
        return EVENT_TYPE;
    }
}