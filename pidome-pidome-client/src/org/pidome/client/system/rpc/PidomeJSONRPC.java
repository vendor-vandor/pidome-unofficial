/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.rpc;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author John
 */
public class PidomeJSONRPC {
    
    static JSONParser parser = new JSONParser();
    String RPCString;
    
    JSONObject parsedObject;
    
    Object requestId;
    String methodCatId;
    String methodExecId;
    
    String actionsResult;
    
    static Logger LOG = LogManager.getLogger(PidomeJSONRPC.class);
    
    public PidomeJSONRPC(String RPCString) throws PidomeJSONRPCException {
        try {
            parse(RPCString);
        } catch (ClassCastException ex){
            LOG.error("JSON parser error: {}", ex.getMessage());
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, "Could not parse rpc message");
        }
    }
    
    /**
     * Tries to parse a json string.
     */
    final void parse(String RPCString) throws PidomeJSONRPCException {
        try {
            LOG.debug("JSON String to parse: {}", RPCString);
            parsedObject = (JSONObject)parser.parse(RPCString);
            LOG.debug("Parsed JSON Object: {}", parsedObject);
            if(parsedObject.isEmpty()){
                LOG.error("Parsed empty");
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
            } else {
                setRequestId();
                setMethodHandlers(parsedObject);
            }
        } catch (ParseException ex) {
            LOG.debug("Error parsing request: {}", RPCString);
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, ex.getMessage());
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
            LOG.debug("No method supplied/ response object?");
        }
    }
    
    /**
     * Returns the parameters.
     * @return 
     */
    public final Map<String,Object> getParameters(){
        return PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)parsedObject.get("params"));
    }
    
    /**
     * Returns the parameters.
     * @return 
     */
    public final Map<String,Object> getResult(){
        return PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)parsedObject.get("result"));
    }
    
    /**
     * Returns the method's namespace.
     * @return 
     */
    public final String getNameSpace(){
        return methodCatId;
    }
    
    /**
     * Returns the method.
     * @return 
     */
    public final String getMethod(){
        return this.methodExecId;
    }
    
    /**
     * Returns the id.
     * @return 
     */
    public final Object getId(){
        return requestId;
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
     * Constructs a response for requests.
     * @param params
     * @return 
     * @throws org.pidome.client.system.rpc.PidomeJSONRPCException 
     */
    public final String constructResponse(Object params) throws PidomeJSONRPCException{
        LOG.debug("Response data: {}", params);
        if(requestId!=null){
            return "{\"jsonrpc\":\"2.0\", \"id\": " + (requestId instanceof String?"\""+requestId+"\"":requestId)+",\"result\":"+PidomeJSONRPCUtils.getParamCollection(params)+"}";
        } else {
            /// asuming client broadcast
            return "";
        }
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
                return "{\"jsonrpc\":\"2.0\", \"id\": " + (id instanceof String?"\""+id+"\"":id)+",\"method\": \""+method+"\", \"params\":"+PidomeJSONRPCUtils.getParamCollection(params)+"}";
            } else {
                return "{\"jsonrpc\":\"2.0\", \"id\": " + (id instanceof String?"\""+id+"\"":id)+",\"method\": \""+method+"\"}";
            }
        } else {
            throw new PidomeJSONRPCException("Illegal method createion");
        }
    }
    
    /**
     * Creates method string without params.
     * @param id
     * @param method
     * @return
     * @throws PidomeJSONRPCException 
     */
    public static String createExecMethod(Object id, String method) throws PidomeJSONRPCException {
        if(id!=null){
            return "{\"jsonrpc\":\"2.0\", \"id\": " + (id instanceof String?"\""+id+"\"":id)+",\"method\": \""+method+"\"}";
        } else {
            throw new PidomeJSONRPCException("Illegal method createion");
        }
    }
    
    /**
     * Constructs a broadcast string to be send out.
     * @param method
     * @param params
     * @return 
     * @throws org.pidome.client.system.rpc.PidomeJSONRPCException 
     */
    public static String constructBroadcast(String method, Object params) throws PidomeJSONRPCException {
        return "{\"jsonrpc\":\"2.0\",\"method\":\""+method+"\",\"params\": "+PidomeJSONRPCUtils.getParamCollection(params)+" }";
    }
    
}
