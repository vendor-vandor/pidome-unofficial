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
import org.pidome.server.system.userstatus.UserStatus;
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 *
 * @author John
 */
public class UserStatusServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements UserStatusServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer,Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getUserStatuses", null);
                put("setUserStatus", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getUserStatus", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("addUserStatus", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("updateUserStatus", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                    }
                });
                put("deleteUserStatus", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
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
    public Object getUserStatuses() throws UserStatusException {
        List<UserStatus> userstatusList = UserStatusService.getUserStatuses();
        List<Map<String, Object>> userstatuss = new ArrayList();
        for(int i=0; i < userstatusList.size(); i++){
            Map<String,Object>userstatus = new HashMap<>();
            userstatus.put("id", userstatusList.get(i).getId());
            userstatus.put("name", userstatusList.get(i).getName());
            userstatus.put("description", userstatusList.get(i).getDescription());
            userstatus.put("lastactivated", userstatusList.get(i).getLastActivated());
            userstatus.put("active", userstatusList.get(i).getId()==UserStatusService.current().getId());
            userstatus.put("fixed", userstatusList.get(i).getIsFixed());
            userstatuss.add(userstatus);
        }
        return userstatuss;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object setUserStatus(Long id) throws UserStatusException {
        return UserStatusService.setUserStatus(id.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getUserStatus(Long id) throws UserStatusException {
        UserStatus userstatus = UserStatusService.getUserStatus(id.intValue());
        Map<String,Object>userstatusMap = new HashMap<>();
        userstatusMap.put("id", userstatus.getId());
        userstatusMap.put("name", userstatus.getName());
        userstatusMap.put("description", userstatus.getDescription());
        userstatusMap.put("lastactivated", userstatus.getLastActivated());
        userstatusMap.put("active", userstatus.getId()==UserStatusService.current().getId());
        userstatusMap.put("fixed", userstatus.getIsFixed());
        return userstatusMap;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object addUserStatus(String name, String description) throws UserStatusException {
        return UserStatusService.addUserStatus(name, description);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object updateUserStatus(Long id, String name, String description) throws UserStatusException {
        return UserStatusService.updateUserStatus(id.intValue(), name, description);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deleteUserStatus(Long id) throws UserStatusException {
        return UserStatusService.deleteUserStatus(id.intValue());
    }

}
