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

package org.pidome.server.connector.tools.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

/**
 * Connects to json http.
 * @author John
 */
public class JSONConnector extends HTTPConnector {

    static Logger LOG = LogManager.getLogger(JSONConnector.class);
    
    /**
     * Constructor.
     * @param url
     * @param ssl
     * @throws MalformedURLException 
     */
    public JSONConnector(String url, boolean ssl) throws MalformedURLException {
        super(url, ssl);
    }

    /**
     * Default non ssl constructor.
     * @param url
     * @throws MalformedURLException 
     */
    public JSONConnector(String url) throws MalformedURLException {
        super(url, false);
    }
    
    /**
     * Returns data via http POST.
     * @param method
     * @param params
     * @param requestId
     * @return
     * @throws PidomeJSONRPCException 
     */
    public JsonRPCObject getJSON(String method, Map<String, Object> params, String requestId) throws PidomeJSONRPCException {
        setHTTPMethod(METHOD);
        if(method!=null && requestId != null){
            Map<String,String> postDataSet = new HashMap<>();
            postDataSet.put("rpc", PidomeJSONRPCUtils.createExecMethod(requestId, method, params));
            setPostData(postDataSet);
        }
        try {
            return new JsonRPCObject(this.getData());
        } catch (IOException ex){
            LOG.error("Problem retrieving data: {}", ex.getMessage(), ex);
            throw new PidomeJSONRPCException(ex.getMessage());
        }
    }
    
    /**
     * Returns data via http POST.
     * @param method
     * @param params
     * @param requestId
     * @return
     * @throws PidomeJSONRPCException 
     */
    public JsonRPCObject postJSON(String method, Map<String, Object> params, String requestId) throws PidomeJSONRPCException {
        Map<String,String> postDataSet = new HashMap<>();
        setHTTPMethod(METHOD);
        if(method!=null && requestId != null){
            postDataSet.put("rpc", PidomeJSONRPCUtils.createExecMethod(requestId, method, params));
            setPostData(postDataSet);
        } else {
            setPlainPostData(PidomeJSONRPCUtils.createNonRPCMethods(params));
        }
        try {
            return new JsonRPCObject(this.getData());
        } catch (IOException ex){
            LOG.error("Problem retrieving data: {}", ex.getMessage(), ex);
            throw new PidomeJSONRPCException(ex.getMessage());
        }
    }
    
    public class JsonRPCObject {
        
        Object parsedObject;
        JSONParser parser = new JSONParser();
        
        public JsonRPCObject(String data) throws PidomeJSONRPCException {
            this.parse(data);
        }
        
        
        /**
         * Tries to parse a json string.
         */
        private final void parse(String RPCString) throws PidomeJSONRPCException {
            try {
                LOG.debug("JSON String to parse: {}", RPCString);
                parsedObject = parser.parse(RPCString);
                LOG.debug("Parsed JSON Object: {}", parsedObject);
                if(parsedObject == null){
                    LOG.error("Parsed empty");
                    throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
                }
            } catch (ParseException ex) {
                LOG.debug("Error parsing request: {}", RPCString);
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, ex.getMessage());
            }
        }
        
        /**
         * Returns the parsed as map object.
         * @return 
         */
        public final Object getObjectData(){
            return (Object)parsedObject;
        }
        
        /**
         * Return the parsed as list (array) object.
         * @return 
         */
        public final ArrayList getArrayData(){
            return (ArrayList)parsedObject;
        }
        
    }
    
}
