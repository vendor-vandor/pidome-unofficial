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

package org.pidome.client.system.network.connectors;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.ClientSystem;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;

/**
 * Connects to json http.
 * @author John
 */
public class JSONConnector extends HTTPConnector {

    static Logger LOG = LogManager.getLogger(JSONConnector.class);
    
    String ip;
    
    /**
     * Constructor.
     * @param ip
     * @param port
     * @param endPoint
     * @param ssl
     * @throws MalformedURLException 
     */
    public JSONConnector(String ip, int port, String endPoint, boolean ssl) throws MalformedURLException {
        this.setServerData(ip, port, ClientSystem.isHTTPSSL());
        this.setUrl(endPoint);
    }

    /**
     * Returns data via http POST.
     * @param method
     * @param params
     * @param requestId
     * @return
     * @throws PidomeJSONRPCException
     * @throws IOException 
     */
    public PidomeJSONRPC getJSON(String method, Map<String, Object> params, String requestId) throws PidomeJSONRPCException {
        Map<String,String> postDataSet = new HashMap<>();
        setHTTPMethod(POST);
        postDataSet.put("rpc", PidomeJSONRPC.createExecMethod(requestId, method, params));
        setPostData(postDataSet);
        try {
            return new PidomeJSONRPC(this.getData());
        } catch (IOException ex){
            LOG.error("Problem retrieving data: {}", ex.getMessage(), ex);
            throw new PidomeJSONRPCException(ex.getMessage());
        }
    }
    
}
