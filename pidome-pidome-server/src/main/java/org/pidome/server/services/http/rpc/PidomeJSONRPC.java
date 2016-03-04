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

import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHook;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHookInterpretor;
import org.pidome.server.connector.plugins.hooks.PiDomeRPCHookListener;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientException;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.services.clients.persons.PersonBaseRole;

/**
 * This is the central RPC object class handling dispatching to the correct service.
 * @author John Sirach
 */
public class PidomeJSONRPC {

    JSONParser parser = new JSONParser();
    String RPCString;
    
    JSONObject parsedObject;

    static Logger LOG = LogManager.getLogger(PidomeJSONRPC.class);
    
    Object requestId;
    String methodCatId;
    String methodExecId;
    
    String actionsResult;
    
    public static void prepare(){
        RPCMethodLibrary.prepare();
    }
    
    /**
     * Parses a string presentation of an JSON-RPC to objects.
     * @param RPCString
     * @throws PidomeJSONRPCException 
     */
    public PidomeJSONRPC(String RPCString) throws PidomeJSONRPCException {
        this(RPCString, true);
    }
    
    /**
     * Constructor.
     * @param RPCString String to parse
     * @param methodParser set to true to parse a JSON-RPC object. False for regular json object.
     * @throws PidomeJSONRPCException 
     */
    public PidomeJSONRPC(String RPCString, boolean methodParser) throws PidomeJSONRPCException {
        try {
            parse(RPCString, methodParser);
        } catch (ClassCastException ex){
            LOG.error("JSON parser error: {}", ex.getMessage());
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, "Could not parse rpc message");
        }
    }
    
    /**
     * Returns the parsed object.
     * @return 
     */
    public final Map<String,Object> getParsedObject(){
        return (Map<String,Object>)this.parsedObject;
    }
    
    /**
     * Tries to parse a json string.
     */
    final void parse(String RPCString, boolean methodParser) throws PidomeJSONRPCException {
        try {
            LOG.debug("JSON String to parse: {}", RPCString);
            parsedObject = (JSONObject)parser.parse(RPCString);
            LOG.debug("Parsed JSON Object: {}", parsedObject);
            setRequestId();
            if(parsedObject.isEmpty()){
                LOG.error("Parsed empty");
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
            } else {
                if(methodParser){
                    setMethodHandlers(parsedObject);
                }
            }
        } catch (ParseException ex) {
            LOG.error("Error parsing request: {}, reason: {}", RPCString, ex.toString());
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, ex.toString());
        }
    }
    
    /**
     * Sets the request id if sended by the client.
     * @throws PidomeJSONRPCException 
     */
    final void setRequestId() throws PidomeJSONRPCException {
        if(parsedObject.containsKey("id")){
            requestId = parsedObject.get("id");
            LOG.debug("Set request id: {}", requestId);
        }
    }
    
    /**
     * Handles the method's send.
     * @param client The remote user.
     * @param requestBy The remote user initiator.
     * @throws PidomeJSONRPCException 
     */
    public final void handle(RemoteClientInterface client, RemoteClient requestBy) throws PidomeJSONRPCException {
        actionsResult = handleRequest(client, requestBy);
    }
    
    /**
     * Returns the method.
     * @return 
     */
    public final String getMethod(){
        return methodCatId+"."+methodExecId;
    }
    
    /**
     * Returns the authentication tokens used for authentication.
     * @return
     * @throws PidomeJSONRPCException 
     */
    public final PiDomeJSONRPCAuthentificationParameters getAuthenticationParameters() throws PidomeJSONRPCException {
        PiDomeJSONRPCAuthentificationParameters params;
        if(parsedObject.get("params") instanceof JSONObject){
            Map<String,Object> tokenParams = PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)parsedObject.get("params"));
            if(tokenParams.size()==4 && tokenParams.containsKey("loginname") && tokenParams.containsKey("type") && tokenParams.containsKey("key") && tokenParams.containsKey("clientinfo")){
                params = new PiDomeJSONRPCAuthentificationParameters(RemoteClient.DeviceType.MOBILE);
                params.setLoginname((String)tokenParams.get("loginname"));
                params.setType((String)tokenParams.get("type"));
                params.setKey((String)tokenParams.get("key"));
                params.setClientInfo((String)tokenParams.get("clientinfo"));
            } else if ((tokenParams.size()==2 || tokenParams.size()==3) && tokenParams.containsKey("username") && tokenParams.containsKey("password")){
                params = new PiDomeJSONRPCAuthentificationParameters(RemoteClient.DeviceType.WEB);
                params.setUsername((String)tokenParams.get("username"));
                params.setPassword((String)tokenParams.get("password"));
                if(tokenParams.containsKey("override")){
                    params.setOverrideLogin((boolean)tokenParams.get("override"));
                }
            } else if (tokenParams.size()==1 && tokenParams.containsKey("key")){
                params = new PiDomeJSONRPCAuthentificationParameters(RemoteClient.DeviceType.WEB);
                params.setKey((String)tokenParams.get("key"));
            } else {
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_PARAMS, "Incorrect authentication params");  
            }
        } else if (parsedObject.get("params") instanceof JSONArray){
            try {
                Object[] objects = PidomeJSONRPCUtils.jsonParamsToObjectArray((JSONArray)parsedObject.get("params"));
                switch (objects.length) {
                    case 4:
                        params = new PiDomeJSONRPCAuthentificationParameters(RemoteClient.DeviceType.MOBILE);
                        params.setLoginname((String)objects[0]);
                        params.setType((String)objects[1]);
                        params.setKey((String)objects[2]);
                        params.setClientInfo((String)objects[3]);
                    break;
                    case 2:
                        params = new PiDomeJSONRPCAuthentificationParameters(RemoteClient.DeviceType.WEB);
                        params.setUsername((String)objects[0]);
                        params.setPassword((String)objects[1]);  
                    break;
                    default:
                        throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_PARAMS, "Incorrect authentication params");
                }
            } catch (Exception ex){
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_PARAMS, "Incorrect authentication params");
            }
        } else {
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.METHOD_NOT_FOUND, "Unsupported parameter request");
        }
        return params;
    }
    
    /**
     * Returns the parameters as object array in order of function requirements.
     * @return
     * @throws PidomeJSONRPCException 
     */
    public final Object[] getExecParameters() throws PidomeJSONRPCException {
        if(parsedObject.get("params") instanceof JSONObject){
            return AbstractRPCMethodExecutor.getExecParameterObjects(methodCatId, methodExecId, (JSONObject)parsedObject.get("params"));
        } else if (parsedObject.get("params") instanceof JSONArray){
            return AbstractRPCMethodExecutor.getExecParameterObjects(methodCatId, methodExecId, (JSONArray)parsedObject.get("params"));
        } else {
            throw new PidomeJSONRPCException("Incompatible paramer set");
        }
    }
    
    
    /**
     * Returns a rpc result.
     * @return
     * @throws PidomeJSONRPCException 
     */
    public final String getResult() throws PidomeJSONRPCException {
        if(actionsResult!=null){
            return actionsResult;
        } else {
            LOG.error("Response or empty response could not be created");
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.SERVER_ERROR);
        }
    }
    
    /**
     * Sets the method handlers based on the method parameter.
     * @param object
     * @throws PidomeJSONRPCException 
     */
    final void setMethodHandlers(JSONObject object) throws PidomeJSONRPCException {
        if(parsedObject.containsKey("method")){
            try {
                String[] methodSplitted = ((String)parsedObject.get("method")).split("\\.");
                methodCatId = methodSplitted[0];
                methodExecId= methodSplitted[1];
            } catch(IndexOutOfBoundsException ex){
                LOG.error("Illegal method supplied: {}, should be like 'MethodHandler.Method'", parsedObject.get("method"));
                throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.METHOD_NOT_FOUND);
            }
        } else {
            LOG.error("No method supplied");
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.INVALID_REQUEST);
        }
    }
    
    final Object getJSONRawIntroSpect(){
        return AbstractRPCMethodExecutor.getNamedMethodMapperSet();
    }
    
    /**
     * Handles the request based on the specified service.
     * @return
     * @throws PidomeJSONRPCException 
     */
    final String handleRequest(RemoteClientInterface client, RemoteClient requestBy) throws PidomeJSONRPCException {
        try {
            if(RPCMethodLibrary.containsNameSpace(methodCatId)){
                if(methodCatId.equals("JSONService") && methodExecId.equals("getRawIntrospectCommandDefinitions")){
                    return constructResponse(getJSONRawIntroSpect());
                }
                if(parsedObject.containsKey("params")){
                    if(parsedObject.get("params") instanceof JSONObject){
                        return constructResponse(RPCMethodLibrary.getMethodWrapper(methodCatId).execMethod(client, requestBy, methodCatId,methodExecId, PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)parsedObject.get("params"))));
                    } else if (parsedObject.get("params") instanceof JSONArray){
                        return constructResponse(RPCMethodLibrary.getMethodWrapper(methodCatId).execMethod(client, requestBy, methodCatId,methodExecId, PidomeJSONRPCUtils.jsonParamsToObjectArray((JSONArray)parsedObject.get("params"))));
                    } else {
                        throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.METHOD_NOT_FOUND, "Unsupported parameter request");
                    }
                } else {
                    return constructResponse(RPCMethodLibrary.getMethodWrapper(methodCatId).execMethod(client, requestBy, methodCatId,methodExecId));
                }
            } else {
                throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.METHOD_NOT_FOUND, "Namespace "+methodCatId+" does not exist");
            }
        } catch (RPCMethodNotFoundException ex) {
            LOG.error("Method execution '{}', not found in RPC method handler '{}': {}", methodExecId, methodCatId, ex.getMessage());
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.METHOD_NOT_FOUND, "Method "+methodExecId+" does not exist in " + methodCatId);
        } catch (RPCMethodInvalidParamsException ex) {
            LOG.error("Invalid parameters for '{}', in RPC method handler '{}': ", methodExecId, methodCatId, ex.getMessage());
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.INVALID_PARAMS, "Check parameters for "+methodExecId+" in " + methodCatId);
        } catch (NullPointerException ex) {
            LOG.error("Invalid parameters for '{}', in RPC method handler '{}': ", methodExecId, methodCatId, ex.getMessage());
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.SERVER_ERROR, "Failure in handling response " + client.getLoginName());
        } catch (RemoteClientException ex) {
            LOG.error("Error handing RPC request {} for {}: {}", methodExecId+"."+methodCatId, client.getLoginName(),ex.getMessage());
            throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.SERVER_ERROR, "Failure in handling response for " + client.getLoginName() + ", client error.");
        }
    }
    
    /**
     * Constructs a response for requests.
     * @param params
     * @return 
     * @throws org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException 
     */
    public final String constructResponse(Object params) throws PidomeJSONRPCException{
        LOG.debug("Response data: {}", params);
        if(requestId!=null){
            return new StringBuilder("{\"jsonrpc\":\"2.0\", \"id\": ").append((requestId instanceof String?"\""+requestId+"\"":requestId)).append(",\"result\":").append(PidomeJSONRPCUtils.getParamCollection(params)).append("}").toString();
        } else {
            /// asuming client broadcast
            return "";
        }
    }
    
    /**
     * Constructs a broadcast string to be send out.
     * @param method
     * @param params
     * @return 
     * @throws org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException 
     */
    public static String constructBroadcast(String nameSpace, String method, Object params) throws PidomeJSONRPCException {
        return new StringBuilder("{\"jsonrpc\":\"2.0\",\"method\":\"").append(nameSpace).append(".").append(method).append("\",\"params\": ").append(PidomeJSONRPCUtils.getParamCollection(params)).append(" }").toString();
    }
    
    
    /**
     * Interpreter class used for external entities who want to execute RPC calls.
     */
    static class ExternalInterpreter implements PiDomeRPCHookInterpretor {

        private ExternalInterpreter(){
            PiDomeRPCHook.setRPCInterpreter(this);
        }
        
        @Override
        public String interpretExternal(PiDomeRPCHookListener originator, String message) {
            try {
                RemoteClientInterface interpreterClient = new InterpreterClient(originator);
                PidomeJSONRPC rpcCall = new PidomeJSONRPC(message);
                rpcCall.handle(interpreterClient, new InterpreterRemote());
                return rpcCall.getResult();
            } catch (PidomeJSONRPCException ex) {
                return ex.getJsonReadyMessage();
            }
        }
        
        private static class InterpreterRemote extends RemoteClient {

            public InterpreterRemote() {
                super(Type.INTERNAL_RPC);
            }

            @Override
            public boolean sendSocket(String nameSpace, byte[] message) {
                return false;
            }

            @Override
            public String getRemoteSocketAddress() {
                return "IntepreterRemote";
            }

            @Override
            public void finish() {
                /// Not needed
            }
            
        }
        
        private static class InterpreterClient implements RemoteClientInterface {

            private final PiDomeRPCHookListener originator;
            
            private InterpreterClient(PiDomeRPCHookListener originator){
                this.originator = originator;
            }
            
            @Override
            public String getLoginName() {
                return this.originator.getFriendlyName();
            }

            @Override
            public PersonBaseRole getRole() throws Exception {
                Map<String,Object> role = new HashMap<>();
                role.put("role", "user");
                return new PersonBaseRole(role);
            }

            @Override
            public String getLastLogin() {
                return "Not applicable";
            }

            @Override
            public boolean getIfCpwd() {
                return false;
            }

            @Override
            public int getId() {
                return this.originator.getPluginId();
            }

            @Override
            public RemoteClient.Type getType() {
                return RemoteClient.Type.MQTT;
            }
            
        }
    }
}