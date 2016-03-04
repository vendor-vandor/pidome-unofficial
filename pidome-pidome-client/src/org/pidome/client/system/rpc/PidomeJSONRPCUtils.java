/*
 * Copyright 2014 John Sirach <john.sirach@gmail.com>.
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

package org.pidome.client.system.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Util functions for JSON interpretations.
 * @author John Sirach
 */
public class PidomeJSONRPCUtils {

    static Logger LOG = LogManager.getLogger(PidomeJSONRPCUtils.class);
    
    /**
     * This function can call itself recursively until all maps/lists/selected primitives are consumed by it.
     * @param mapObject supports: ArrayList, HashMap, String, int, boolean, long (Only String, ArrayList and HashMap are object types).
     * @return
     */
    static String getParamCollection(Object params) throws PidomeJSONRPCException{
        String returnString = "";
        if (params==null) {
            return "null";
        } else if (params instanceof ArrayList) {
            returnString = "[";
            ArrayList list = (ArrayList) params;
            for (int i = 0; i < list.size(); i++) {
                returnString += getParamCollection(list.get(i)) + ",";
            }
            if (returnString.contains(",")) {
                returnString = returnString.substring(0, returnString.lastIndexOf(","));
            }
            return returnString + "]";
        } else if (params instanceof HashMap) {
            returnString = "{";
            HashMap map = (HashMap) params;
            for (Object key : map.keySet()) {
                returnString += getParamCollection(key) + ":" + getParamCollection(map.get(key)) + ",";
            }
            if (returnString.contains(",")) {
                returnString = returnString.substring(0, returnString.lastIndexOf(","));
            }
            return returnString + "}";
        }
        if (params instanceof Boolean) {
            return ((boolean) params == true ? "true" : "false");
        } else if (params instanceof String) {
            return "\"" + (String) params + "\"";
        } else if (params instanceof Long || params instanceof Integer || params instanceof Double) {
            return String.valueOf(params);
        } else {
            LOG.error("Error parsing type {}", params.getClass().getName());
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.SERVER_ERROR, "Invalid parameters");
        }
    }
    
    /**
     * Returns a hashmap from a jsonobject.
     * This is an convenience method, the only map difference is that the key is set as string instead as an object.
     * @param object
     * @return 
     */
    static Map<String,Object> jsonParamsToObjectHashMap(JSONObject object){
        Map<String,Object> returnMap = new HashMap<>();
        for(Object key: object.keySet()){
            returnMap.put((String)key, object.get(key));
        }
        return returnMap;
    }
    
    /**
     * Returns a object array from a jsonobject.
     * This is an convenience method, the only map difference is that the key is set as string instead as an object.
     * @param object
     * @return 
     */
    static Object[] jsonParamsToObjectArray(JSONArray object){
        Object[] returnObject = new Object[object.size()];
        for(int i = 0; i < object.size(); i++){
            returnObject[i] = object.get(i);
        }
        return returnObject;
    }
    
}
