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

package org.pidome.server.services.http.rpc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientsAuthentication;
import org.pidome.server.services.http.Webservice_renderer;
import org.pidome.server.system.network.http.HttpClientLoggedInOnOtherLocationException;
import org.pidome.server.system.network.http.HttpClientNotAuthorizedException;
import org.pidome.server.system.network.http.HttpServiceClient;

/**
 *
 * @author John Sirach
 */
public final class PiDomeJSONRPCEndpoint extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(PiDomeJSONRPCEndpoint.class);
    
    PidomeJSONRPC pidomeJSONRPC;
    
    String returnMessage = "";
    
    @Override 
    public final void collect(){
        try {
            if(getDataMap.containsKey("rpc")){
                pidomeJSONRPC = new PidomeJSONRPC(URLDecoder.decode(getDataMap.get("rpc"),"UTF-8"));
            } else if (postDataMap.containsKey("rpc")){
                String postData;
                try {
                    postData = URLDecoder.decode(postDataMap.get("rpc"),"UTF-8");
                } catch (Exception ex){
                    postData = postDataMap.get("rpc");
                }
                pidomeJSONRPC = new PidomeJSONRPC(postData);
            } else {
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.SERVER_ERROR, "Could not parse request");
            }
            try {
                pidomeJSONRPC.handle(getRemoteUser(), this.getRemoteUserInitiator());
                returnMessage = pidomeJSONRPC.getResult();
            } catch (HttpClientNotAuthorizedException ex){
                /// try to authorize.
                Map<String,Object> resultData = new HashMap<>();
                PiDomeJSONRPCAuthentificationParameters authParams = pidomeJSONRPC.getAuthenticationParameters();
                try {
                    RemoteClient remoteClient = new HttpServiceClient(authParams.getUsername(), authParams.getPassword(), authParams.isOverrideLogin(),getRemoteClientIp());
                    resultData.put("auth", true);
                    resultData.put("code", 200);
                    resultData.put("message", "Authentication ok");
                    resultData.put("key", remoteClient.getKey());
                    LOG.info("Client at {} is authenticated as {}", remoteClient.getRemoteSocketAddress(), authParams.getUsername());
                } catch (HttpClientNotAuthorizedException authEx){
                    resultData.put("auth", false);
                    resultData.put("code", 401);
                    resultData.put("message", "Authentication failed");
                    resultData.put("key", "");
                    LOG.info("Client at {} is failed to authenticated as {}", getRemoteClientIp(), authParams.getUsername());
                } catch (HttpClientLoggedInOnOtherLocationException locEx){
                    resultData.put("auth", false);
                    resultData.put("code", 406);
                    resultData.put("message", "Already logged in on other location, press login again to overtake");
                    resultData.put("key", "");
                    LOG.info("Client at {} is failed to authenticated as {} because already logged in on other location, use overwrite to take over the session.", getRemoteClientIp(), authParams.getUsername());
                }
                returnMessage = pidomeJSONRPC.constructResponse(resultData);
            }
        } catch (PidomeJSONRPCException ex) {
            returnMessage = ex.getJsonReadyMessage();
        } catch (UnsupportedEncodingException ex) {
            returnMessage = "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "+PidomeJSONRPCException.JSONError.PARSE_ERROR.toLong()+", \"message\": \"Parse error, could not encode url: "+ex.getMessage()+"\"}, \"id\": null}";
        } catch (Exception ex) {
            LOG.error("JSON function run error: {},", ex.getMessage(), ex);
            returnMessage = "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "+PidomeJSONRPCException.JSONError.SERVER_ERROR.toLong()+", \"message\": \"an internal server error occurred: "+ex.getMessage()+"\"}, \"id\": null}";
        }
    }
    
    @Override
    public final String render() throws Exception {
        return getCustomRender(returnMessage);
    }

}
