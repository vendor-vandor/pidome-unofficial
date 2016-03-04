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

package org.pidome.server.services;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John Sirach
 */
public class ServiceEvent extends java.util.EventObject {
    
    public static final String SERVICEAVAILABLE   = "SERVICEAVAILABLE";
    public static final String SERVICEUNAVAILABLE = "SERVICEUNAVAILABLE";
    public static final String SERVICEAMOUNT      = "SERVICEAMOUNT";
    public static final String SERVICEINITIALIZED = "SERVICEINITIALIZED";
    
    public static final String SYSTEMSTATECHANGE  = "SYSTEMSTATECHANGE";
    
    private String EVENT_TYPE = null;
    
    private Map<String,Object> eventDetails = new HashMap<>();
    
    public ServiceEvent( Object source, String eventType ) {
        super( source );
        EVENT_TYPE = eventType;
    }
    
    public String getEventType(){
        return EVENT_TYPE;
    }
    
    public Map<String,Object> getDetails(){
        return eventDetails;
    }
 
    public void setDetails(Map<String,Object> details){
        eventDetails = details;
    }
    
}
