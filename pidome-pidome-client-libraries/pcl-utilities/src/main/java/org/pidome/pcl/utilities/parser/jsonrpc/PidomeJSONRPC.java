/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.pcl.utilities.parser.jsonrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class used for JSON-RPC parsing and creation.
 * @author John
 */
public class PidomeJSONRPC {
    
    static {
        Logger.getLogger(PidomeJSONRPC.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * JSON parser.
     */
    JSONParser parser = new JSONParser();
    /**
     * RPC String to parse.
     */
    String RPCString;
    
    /**
     * JSON object holding the objects.
     */
    JSONObject parsedObject;
    
    /**
     * The request id.
     */
    Object requestId     = "";
    /**
     * Method namespace.
     */
    String methodCatId   = "";
    /**
     * MEthod method.
     */
    String methodExecId  = "";
    
    /**
     * Result.
     */
    String actionsResult = "";
    
    /**
     * RPC constructor.
     * @param RPCString The string to parse.
     * @throws PidomeJSONRPCException When string parsing fails
     */
    public PidomeJSONRPC(String RPCString) throws PidomeJSONRPCException {
        try {
            parse(RPCString);
        } catch (ClassCastException ex){
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, "Could not parse rpc message");
        }
    }
    
    /**
     * Tries to parse a json string.
     */
    private void parse(String RPCString) throws PidomeJSONRPCException {
        try {
            parsedObject = (JSONObject)parser.parse(RPCString);
            if(parsedObject.isEmpty()){
                throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.INVALID_REQUEST);
            } else {
                setRequestId();
                setMethodHandlers();
            }
        } catch (ParseException ex) {
            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.PARSE_ERROR, ex.getMessage());
        }
    }
    
    /**
     * Returns the object as parsed.
     * @return 
     */
    public final JSONObject getParsedObject(){
        return parsedObject;
    }
    
    /**
     * Sets the method handlers based on the method parameter.
     * @param object
     * @throws PidomeJSONRPCException 
     */
    private void setMethodHandlers() throws PidomeJSONRPCException {
        if(parsedObject.containsKey("method")){
            try {
                String[] methodSplitted = ((String)parsedObject.get("method")).split("\\.");
                methodCatId = methodSplitted[0];
                methodExecId= methodSplitted[1];
            } catch(IndexOutOfBoundsException ex){
                throw new PidomeJSONRPCException(requestId, PidomeJSONRPCException.JSONError.METHOD_NOT_FOUND);
            }
        } else {
            /// No method supplied, so it is not a broadcast. It is a response so get it byu requesting it's id.
        }
    }
    
    /**
     * Returns the parameters.
     * @return Returns the json object parameters.
     */
    public final Map<String,Object> getParameters(){
        return PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)parsedObject.get("params"));
    }
    
    /**
     * Returns the parameters.
     * @return Returns a request resultset.
     */
    public final Map<String,Object> getResult(){
        try {
            return PidomeJSONRPCUtils.jsonParamsToObjectHashMap((JSONObject)parsedObject.get("result"));
        } catch (Exception ex){
            Logger.getLogger(PidomeJSONRPC.class.getName()).log(Level.SEVERE, "getResult failed for object set: " + parsedObject, ex);
        }
        return new HashMap<>();
    }
    
    /**
     * Returns the method's namespace.
     * @return The namespace of the json request.
     */
    public final String getNameSpace(){
        return methodCatId;
    }
    
    /**
     * Returns the method.
     * @return The method of the request.
     */
    public final String getMethod(){
        return this.methodExecId;
    }
    
    /**
     * Returns the id.
     * @return The id of the request.
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
        }
    }
    
    /**
     * Constructs a response for requests.
     * @param params An object to be parsed (Map,List,boolean,int,string,float,double).
     * @return A JSON response string.
     * @throws org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPCException When non compatible objects are used.
     */
    public final String constructResponse(Object params) throws PidomeJSONRPCException{
        if(requestId!=null){
            return "{\"jsonrpc\":\"2.0\", \"id\": " + (requestId instanceof String?"\""+requestId+"\"":requestId)+",\"result\":"+PidomeJSONRPCUtils.getParamCollection(params)+"}";
        } else {
            /// asuming client broadcast
            return "";
        }
    }
    
    /**
     * Creates method string with parameters.
     * @param id the json id.
     * @param method The name of the method.
     * @param params parameters for the RPC (List,Map,boolean,string,integer,float,double).
     * @return JSON-RPC string.
     * @throws PidomeJSONRPCException When incompatible objects are used.
     */
    public static String createExecMethod(Object id, String method, Object params) throws PidomeJSONRPCException {
        if(id!=null){
            if(params!=null){
                return "{\"jsonrpc\":\"2.0\", \"id\": " + (id instanceof String?"\""+id+"\"":id)+",\"method\": \""+method+"\", \"params\":"+PidomeJSONRPCUtils.getParamCollection(params)+"}";
            } else {
                return "{\"jsonrpc\":\"2.0\", \"id\": " + (id instanceof String?"\""+id+"\"":id)+",\"method\": \""+method+"\"}";
            }
        } else {
            throw new PidomeJSONRPCException("Illegal method creation");
        }
    }
    
    /**
     * Creates method string without params.
     * @param id the RPC id.
     * @param method The RPC method.
     * @return JSON-RPC String.
     * @throws PidomeJSONRPCException When incompatible objects are used.
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
     * @param method The RPC method.
     * @param params The parameters to create (List,Map,boolean,integer,double,float).
     * @return JSON-RPC broadcast string.
     * @throws org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPCException When incompatible objects are used. 
     */
    public static String constructBroadcast(String method, Object params) throws PidomeJSONRPCException {
        return "{\"jsonrpc\":\"2.0\",\"method\":\""+method+"\",\"params\": "+PidomeJSONRPCUtils.getParamCollection(params)+" }";
    }
    
}
