/*
 * Copyright 2014 John.
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
package org.pidome.server.services.clients.remoteclient;

import org.pidome.server.services.clients.persons.PersonBaseRole;

/**
 *
 * @author John
 */
public interface RemoteClientInterface {
    
    public String getLoginName();
    public PersonBaseRole getRole() throws Exception;    

    public String getLastLogin();
    public boolean getIfCpwd();
    
    public int getId();
    
    public RemoteClient.Type getType();
    
}
