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

package org.pidome.server.system.hardware;

import java.util.EventObject;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;

/**
 *
 * @author John Sirach
 */
public class HardwarePeripheralEvent extends EventObject {
    public final static String DEVICE_ADDED   = "DEVICE_ADDED";
    public final static String DEVICE_REMOVED = "DEVICE_REMOVED";
    
    private static String EVENT_TYPE = null;
    
    public HardwarePeripheralEvent( Peripheral source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    @Override
    public Peripheral getSource(){
        return (Peripheral)super.getSource();
    }
    
}
