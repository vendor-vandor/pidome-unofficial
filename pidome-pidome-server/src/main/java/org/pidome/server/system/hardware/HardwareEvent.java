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

import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;

/**
 * Hardware event when a hardware device has been attached or removed.
 * The source of this event is the device which has been added.
 * @author John Sirach
 */
public final class HardwareEvent extends java.util.EventObject {
    
    public final static String HARDWARE_ADDED   = "HARDWARE_ADDED";
    public final static String HARDWARE_REMOVED = "HARDWARE_REMOVED";
    
    private static String EVENT_TYPE = null;
    
    public HardwareEvent( Peripheral source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public final String getEventType(){
        return EVENT_TYPE;
    }
    
    @Override
    public final Peripheral getSource(){
        return (Peripheral)super.getSource();
    }
    
}
