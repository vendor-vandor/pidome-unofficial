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

package org.pidome.pcl.data.connection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPC;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPCException;
import org.pidome.pcl.networking.connections.server.http.HTTPConnector;
import static org.pidome.pcl.networking.connections.server.http.HTTPConnector.Method.POST;

/**
 * Connects to json http.
 * @author John
 */
public class JSONConnector extends HTTPConnector {

    static {
        Logger.getLogger(JSONConnector.class.getName()).setLevel(Level.ALL);
    }
    
    String ip;
    
    /**
     * Constructor.
     * @param ip remote host ip.
     * @param port remote host port.
     * @param endPoint the url path to the json connector.
     * @param ssl if secure set true.
     * @throws MalformedURLException When the generated url fails.
     */
    public JSONConnector(String ip, int port, String endPoint, boolean ssl) throws MalformedURLException {
        this.setServerData(ip, port, ssl);
        this.setUrl(endPoint);
    }

    /**
     * Returns data via http.
     * @param method Post or GET method.
     * @param params JSON-RPC parameters as String,Object map.
     * @param requestId The id of the request.
     * @return JSON-RPC data handler.
     * @throws org.pidome.pcl.data.parser.PCCEntityDataHandlerException When the RPC data fails (including connection).
     */
    public PCCEntityDataHandler getJSON(String method, Map<String, Object> params, String requestId) throws PCCEntityDataHandlerException {
        try {
            Map<String,String> postDataSet = new HashMap<>();
            setHTTPMethod(POST);
            postDataSet.put("rpc", PidomeJSONRPC.createExecMethod(requestId, method, params));
            setPostData(postDataSet);
            String returnData = this.getData();
            Logger.getLogger(JSONConnector.class.getName()).log(Level.FINE, "Data received from http connector: {0}", returnData);
            return new PCCEntityDataHandler(returnData);
        } catch (IOException ex){
            throw new PCCEntityDataHandlerException(ex.getMessage());
        } catch (PidomeJSONRPCException ex) {
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    /**
     * Returns data via http.
     * @param method Post or GET method.
     * @param params JSON-RPC parameters as String,Object map.
     * @param requestId The id of the request.
     * @return JSON-RPC data handler.
     * @throws org.pidome.pcl.data.parser.PCCEntityDataHandlerException When the RPC data fails (including connection).
     */
    public String getJSONAsString(String method, Map<String, Object> params, String requestId) throws PCCEntityDataHandlerException {
        try {
            Map<String,String> postDataSet = new HashMap<>();
            setHTTPMethod(POST);
            postDataSet.put("rpc", PidomeJSONRPC.createExecMethod(requestId, method, params));
            setPostData(postDataSet);
            return this.getData();
        } catch (IOException ex){
            throw new PCCEntityDataHandlerException(ex.getMessage());
        } catch (PidomeJSONRPCException ex) {
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
}
