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

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.services.clients.persons.Person;
import org.pidome.server.services.clients.remoteclient.RemoteClientsConnectionPool;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.services.clients.persons.PersonBaseRole.BaseRole;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;

/**
 *
 * @author John
 */
public class UserServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements UserServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("signOn", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("username", "");}});
                        put(1,new HashMap<String,Object>(){{put("password", "");}});
                    }
                });
                put("signOff", null);
                put("getUsers", null);
                put("getNormalizedUsers", null);
                put("getUser", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", "");}});
                    }
                });
                put("getMyData", null);
                put("getMyDevices", null);
                put("updateMyLocation", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("lat", 0.0);}});
                        put(1,new HashMap<String,Object>(){{put("lon", 0.0);}});
                        put(2,new HashMap<String,Object>(){{put("acc", 0.0);}});
                    }
                });
                put("updatePassword", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("oldpass", "");}});
                        put(1,new HashMap<String,Object>(){{put("newpass", "");}});
                        put(2,new HashMap<String,Object>(){{put("newpassagain", "");}});
                    }
                });
                put("addUser", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("username", "");}});
                        put(1,new HashMap<String,Object>(){{put("password", "");}});
                        put(2,new HashMap<String,Object>(){{put("firstname", "");}});
                        put(3,new HashMap<String,Object>(){{put("lastname", "");}});
                        put(4,new HashMap<String,Object>(){{put("changepass", true);}});
                        put(5,new HashMap<String,Object>(){{put("roleset", new HashMap<>());}});
                        put(6,new HashMap<String,Object>(){{put("extconnect", true);}});
                    }
                });
                put("removeUser", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("updateUser", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("password", "");}});
                        put(2,new HashMap<String,Object>(){{put("firstname", "");}});
                        put(3,new HashMap<String,Object>(){{put("lastname", "");}});
                        put(4,new HashMap<String,Object>(){{put("changepass", true);}});
                        put(5,new HashMap<String,Object>(){{put("roleset", new HashMap<>());}});
                        put(6,new HashMap<String,Object>(){{put("extconnect", true);}});
                        put(7,new HashMap<String,Object>(){{put("gpsdevices", new ArrayList<>());}});
                    }
                });
                put("getUserBoundDevices", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("userid", 0);}});
                    }
                });
                put("getUserBoundDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("clientid", 0);}});
                    }
                });
                put("removeUserBoundDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                    }
                });
                put("setUserPresence", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("presenceid", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getUsers() throws PersonsManagementException {
        List<Person> persons = PersonsManagement.getInstance().getPersons();
        List<Map<String,Object>> users = new ArrayList<>();
        for(Person person:persons){
            Map<String,Object> user = new HashMap<>();
            user.put("id",         person.getId());
            user.put("clientname", person.getLoginName());
            user.put("firstname",  person.getFirstName());
            user.put("lastname",   person.getLastName());
            user.put("present",    person.getIfPresent());
            user.put("lastlogin",  person.getLastLogin());
            user.put("ext",        person.getIfExternal());
            user.put("cpwd",       person.getIfCpwd());
            user.put("presence",   person.getPresence());
            user.put("distance",   person.getCurrentDistance());
            user.put("lastLatLon", person.getLastLatLon());
            user.put("admin",      person.getRole().role().equals(BaseRole.ADMIN));
            users.add(user);
        }
        return users;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getNormalizedUsers() throws PersonsManagementException {
        List<Person> persons = PersonsManagement.getInstance().getPersons();
        List<Map<String,Object>> users = new ArrayList<>();
        for(Person person:persons){
            if(!person.getLoginName().equals("admin")){
                Map<String,Object> user = new HashMap<>();
                user.put("id",         person.getId());
                user.put("clientname", person.getLoginName());
                user.put("firstname",  person.getFirstName());
                user.put("lastname",   person.getLastName());
                user.put("present",    person.getIfPresent());
                user.put("lastlogin",  person.getLastLogin());
                user.put("ext",        person.getIfExternal());
                user.put("cpwd",       person.getIfCpwd());
                user.put("presence",   person.getPresence());
                user.put("distance",   person.getCurrentDistance());
                user.put("lastLatLon", person.getLastLatLon());
                user.put("admin",      person.getRole().role().equals(BaseRole.ADMIN));
                users.add(user);
            }
        }
        return users;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getUser(Long userId) throws PersonsManagementException {
        Person person = PersonsManagement.getInstance().getPerson(userId.intValue());
        Map<String,Object> user = new HashMap<>();
        user.put("id",         person.getId());
        user.put("present",    person.getIfPresent());
        user.put("lastlogin",  person.getLastLogin());
        user.put("clientname", person.getLoginName());
        user.put("firstname",  person.getFirstName());
        user.put("lastname",   person.getLastName());
        user.put("ext",        person.getIfExternal());
        user.put("distance",   person.getCurrentDistance());
        user.put("lastLatLon", person.getLastLatLon());
        user.put("cpwd",       person.getIfCpwd());
        user.put("roleset",    person.getRole().getPlainDefinition());
        return user;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getMyData() throws PersonsManagementException {
        Person person = PersonsManagement.getInstance().getPerson(getCaller().getId());
        Map<String,Object> user = new HashMap<>();
        user.put("id",         person.getId());
        user.put("present",    person.getIfPresent());
        user.put("lastlogin",  person.getLastLogin());
        user.put("clientname", person.getLoginName());
        user.put("firstname",  person.getFirstName());
        user.put("lastname",   person.getLastName());
        user.put("ext",        person.getIfExternal());
        user.put("cpwd",       person.getIfCpwd());
        user.put("presence",   person.getPresence());
        user.put("distance",   person.getCurrentDistance());
        user.put("roleset",    person.getRole().getPlainDefinition());
        return user;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object updatePassword(String oldPass, String newPass, String newPassAgain) throws PersonsManagementException {
        return PersonsManagement.getInstance().updatePersonPassword(this.getCaller().getId(), oldPass, newPass, newPassAgain);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object updateUser(Long clientId, String password, String firstname, String lastname, boolean cpwd, HashMap<String,Object> roleset, boolean ext,List<Map<String,Object>> gpsdevices) throws PersonsManagementException {
        return PersonsManagement.getInstance().updatePerson(clientId.intValue(), password, firstname, lastname, cpwd, roleset, ext, gpsdevices);
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object addUser(String username, String password, String firstname, String lastname, boolean cpwd, HashMap<String,Object> roleset, boolean ext) throws PersonsManagementException {
        return PersonsManagement.getInstance().addPerson(username, password, firstname, lastname, cpwd, roleset, ext);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getMyDevices() throws PersonsManagementException {
        return PersonsManagement.getInstance().getRemoteClientBindings(this.getCaller().getId());
    }
    

    @Override
    public Object updateMyLocation(Number latitude, Number longitude, Number accuracy) throws PersonsManagementException {
        if(this.getCaller()==null){
            throw new PersonsManagementException("Not allowed");
        }
        float result = PersonsManagement.getInstance().updateLocation(
                this.getCaller(),
                this.getCallerResource(),
                latitude.floatValue(), 
                longitude.floatValue(),
                accuracy.floatValue()
        );
        Map<String,Object> distance = new HashMap<>();
        distance.put("distance", result);
        return distance;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getUserBoundDevices(Long userId) throws PersonsManagementException {
        return PersonsManagement.getInstance().getRemoteClientBindings(userId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getUserBoundDevice(Long clientId) throws PersonsManagementException {
        return PersonsManagement.getInstance().getRemoteClientBinding(clientId.intValue());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object removeUserBoundDevice(Long deviceId) throws PersonsManagementException {
        return disconnectClient(PersonsManagement.getInstance().removeRemoteClientBinding(deviceId.intValue()), "Device access revoked");
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object signOn(String username, String password) {
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object signOff() {
        return false;
    }
    
    /**
     * Disconnects a client.
     * @param clientName
     * @param message
     * @return 
     */
    private Object disconnectClient(String clientName, String message) {
        return RemoteClientsConnectionPool.disconnectClient(clientName, message);
    }

    
    /**
     * @inheritDoc
     */
    @Override
    public Object setUserPresence(Long id, Long presenceId) throws PresenceException {
        return PresenceService.setPresence(id.intValue(), presenceId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object removeUser(Long userId) throws PersonsManagementException {
        List<Map<String,Object>> boundDevices = PersonsManagement.getInstance().getRemoteClientBindings(userId.intValue());
        for(Map<String,Object> bound: boundDevices){
            disconnectClient(PersonsManagement.getInstance().removeRemoteClientBinding((int)bound.get("id")), "Access removed");
        }
        return PersonsManagement.getInstance().removePerson(userId.intValue());
    }
    
}