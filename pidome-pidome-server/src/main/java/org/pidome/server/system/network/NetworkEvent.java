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

package org.pidome.server.system.network;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John Sirach
 */
public class NetworkEvent extends java.util.EventObject {
    
    public static final String AVAILABLE   = "AVAILABLE";
    public static final String UNAVAILABLE = "UNAVAILABLE";
    
    private String EVENT_TYPE = null;
    
    private Map<String,Object> eventDetails = new HashMap<>();
    
    public NetworkEvent( Network source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    @Override 
    public Network getSource(){
        return (Network)source;
    }
    
}