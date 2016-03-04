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

package org.pidome.server.connector.tools.jsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * @param params supports: ArrayList, HashMap, String, int, boolean, long (Only String, ArrayList and HashMap are object types, others are primitives).
     * @return
     * @throws org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException
     */
    public static String getParamCollection(Object params) throws PidomeJSONRPCException{
        if (params==null) {
            return "null";
        } else if (params instanceof ArrayList) {
            StringBuilder returnString = new StringBuilder("[");
            ArrayList list = (ArrayList) params;
            for (int i = 0; i < list.size(); i++) {
                returnString.append(getParamCollection(list.get(i))).append(",");
            }
            if (returnString.toString().contains(",")) {
                returnString.deleteCharAt(returnString.lastIndexOf(","));
            }
            return returnString.append("]").toString();
        } else if (params instanceof HashMap) {
            StringBuilder returnString = new StringBuilder("{");
            HashMap map = (HashMap) params;
            for (Object key : map.keySet()) {
                returnString.append(getParamCollection(key)).append(":").append(getParamCollection(map.get(key))).append(",");
            }
            if (returnString.toString().contains(",")) {
                returnString.deleteCharAt(returnString.lastIndexOf(","));
            }
            return returnString.append("}").toString();
        }
        if (params instanceof Boolean) {
            return ((boolean) params == true ? "true" : "false");
        } else if (params instanceof String) {
            return new StringBuilder("\"").append(params).append("\"").toString();
        } else if (params instanceof Long || params instanceof Integer || params instanceof Double || params instanceof Float) {
            return String.valueOf(params);
        } else {
            try {
                return new StringBuilder("\"").append(params).append("\"").toString();
            } catch (Exception ex){
                LOG.error("Error parsing type {}", params.getClass().getName());
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, ex.getMessage());
            }
        }
    }
    
    /**
     * Returns a hashmap from a jsonobject.
     * This is an convenience method, the only map difference is that the key is set as string instead as an object.
     * @param object
     * @return 
     */
    public static Map<String,Object> jsonParamsToObjectHashMap(JSONObject object){
        return (Map<String,Object>)object;
    }
    
    /**
     * Returns a object array from a jsonobject.
     * This is an convenience method, This is a method which turns a JSONArray to an Object array.
     * @param object
     * @return 
     */
    public static Object[] jsonParamsToObjectArray(JSONArray object){
        Object[] returnObject = new Object[object.size()];
        for(int i = 0; i < object.size(); i++){
            returnObject[i] = object.get(i);
        }
        return returnObject;
    }
    
    /**
     * Returns a object array from a jsonobject.
     * This is an convenience method, This is a method which turns a JSONArray to an Object array.
     * @param object
     * @return 
     */
    public static List<Map<String,Object>> jsonParamsToObjectHashMapList(JSONArray object){
        return (List<Map<String,Object>>)object;
    }
    
    /**
     * Creates method string with parameters.
     * @param id
     * @param method
     * @param params
     * @return
     * @throws PidomeJSONRPCException 
     */
    public static String createExecMethod(Object id, String method, Object params) throws PidomeJSONRPCException {
        if(id!=null){
            if(params!=null){
                return new StringBuilder("{\"jsonrpc\":\"2.0\", \"id\": ").append((id instanceof String?"\""+id+"\"":id)).append(",\"method\": \"").append(method).append("\", \"params\":").append(PidomeJSONRPCUtils.getParamCollection(params)).append("}").toString();
            } else {
                return new StringBuilder("{\"jsonrpc\":\"2.0\", \"id\": ").append((id instanceof String?"\""+id+"\"":id)).append(",\"method\": \"").append(method).append("\"}").toString();
            }
        } else {
            throw new PidomeJSONRPCException("Illegal method createion");
        }
    }
    
    /**
     * Create a simple non rpc json string.
     * @param params
     * @return
     * @throws PidomeJSONRPCException 
     */
    public static String createNonRPCMethods(Object params) throws PidomeJSONRPCException{
        return PidomeJSONRPCUtils.getParamCollection(params);
    }
    
}
