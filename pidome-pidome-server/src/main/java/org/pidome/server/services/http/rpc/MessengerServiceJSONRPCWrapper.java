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

/**
 *
 * @author John
 */
public class MessengerServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements MessengerServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getMessageTypes", null);
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public List<Map<String, Object>> getMessageTypes() {
        List<Map<String, Object>> typeList = new ArrayList();
        Map<String,Object> types = new HashMap<>();
        types.put("id", "sms");
        types.put("name", "SMS message");
        typeList.add(types);
        return typeList;
    }
}
