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

import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException.JSONError;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClientException;
import org.pidome.server.services.clients.remoteclient.RemoteClientInterface;
import org.pidome.server.services.clients.persons.PersonBaseRole.BaseRole;

/**
 *
 * @author John
 */
abstract class AbstractRPCMethodExecutor {
    
    static Logger LOG = LogManager.getLogger(AbstractRPCMethodExecutor.class);
    
    static Map<String,Method[]> methods = new HashMap<>();
    static Map<String,Map<String,Integer>> methodMapping = new HashMap<>();
    static Map<String,Map<String,Map<Integer,Map<String, Object>>>> namedMap = new HashMap<>();
    
    RemoteClientInterface caller;
    RemoteClient initiator;
    
    /**
     * Used to create a mapping so the RPC implementation functions.
     * This is used to create a mapping of implemented functions. This makes sure the dynamic function executor
     * knows which functions are available and in which order which parameter is passed to the function.
     * 
     * This mapping structure allows the mapping of named(Map on name) and unnamed parameters(mapped on order) used in JSON RPC.
     * 
     * @return 
     */
    abstract Map<String,Map<Integer,Map<String, Object>>> createFunctionalMapping();
    
    /**
     * returns a mapping for the declared methods.
     * This makes it easier to check if a function exists. and if it does return the 
     * @return 
     */
    protected final static void setDeclaredMethods(Map<String,Method[]> methodSet, Map<String,Map<String,Map<Integer,Map<String, Object>>>> namedSet){
        methods = methodSet;
        for(String key:methods.keySet()){
            for(int i = 0; i < methods.get(key).length;i++){
                if(!methodMapping.containsKey(key)){
                    Map<String,Integer> newMap = new HashMap<>();
                    methodMapping.put(key, newMap);
                }
                methodMapping.get(key).put((methods.get(key))[i].getName(), i);
            }
        }
        for (Map.Entry<String,Map<String,Map<Integer,Map<String, Object>>>> entry : namedSet.entrySet()) {
            namedMap.put(entry.getKey(), entry.getValue());
        }
    }
    
    protected final static Object getNamedMethodMapperSet(){
        return namedMap;
    }
    
    /**
     * Execute methods based on named parameters.
     * @param MethodHandler
     * @param method
     * @param parameters
     * @return
     * @throws RPCMethodNotFoundException
     * @throws RPCMethodInvalidParamsException 
     */
    protected Object execMethod(RemoteClientInterface client, RemoteClient requestBy, String MethodHandler,String method, Map<String,Object> parameters) throws NullPointerException, RPCMethodNotFoundException, RPCMethodInvalidParamsException, PidomeJSONRPCException, RemoteClientException {
        LOG.debug("Searching named parameter set for {} in {}", method, MethodHandler);
        caller = client;
        initiator = requestBy;
        try {
            if(client.getRole().hasNameSpaceAccess(MethodHandler)){
                if(methodMapping.containsKey(MethodHandler) && methodMapping.get(MethodHandler).containsKey(method)){
                    Method runMethod = (methods.get(MethodHandler))[methodMapping.get(MethodHandler).get(method)];
                    if((runMethod.isAnnotationPresent(PiDomeJSONRPCPrivileged.class) && client.getRole().role()!=BaseRole.ADMIN) || 
                       (runMethod.isAnnotationPresent(PiDomeJSONRPCLeveraged.class) && client.getRole().role()!=BaseRole.DISPLAY)){
                        LOG.warn("Client {} is not allowed to access privileged method {}.{}", client.getLoginName(), MethodHandler,method);
                        throw new RPCMethodNotFoundException();
                    }
                    if(parameters.size()==runMethod.getParameterTypes().length){
                        Map<Integer, Map<String, Object>> methodParams = namedMap.get(MethodHandler).get(method);
                        Object[] parameterSet = new Object[parameters.size()];
                        try {
                            for (int key : methodParams.keySet()) {
                                LOG.debug("Found param key: {}", methodParams.get(key).entrySet().iterator().next().getKey());
                                parameterSet[key] = parameters.get(methodParams.get(key).entrySet().iterator().next().getKey());
                            }
                        } catch (Exception ex){
                            LOG.error("Problem executing function, possible parameters configuration mismatch: {}", ex.getMessage(), ex);
                            throw new PidomeJSONRPCException(JSONError.SERVER_ERROR);
                        }
                        return execMethod(client, requestBy, MethodHandler,method, parameterSet);
                    } else {
                        LOG.warn("Ivalid method request, parameters issue for request {}.{}: {}", MethodHandler, method, runMethod.getParameterTypes());
                        throw new RPCMethodInvalidParamsException();
                    }
                } else {
                    LOG.warn("Method {}.{} not found", MethodHandler, method);
                    throw new RPCMethodNotFoundException();
                }
            } else {
                LOG.warn("Client {} is not allowed to access namespace {}", client.getLoginName(), MethodHandler);
                throw new PidomeJSONRPCException(JSONError.METHOD_NOT_FOUND);
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", client.getLoginName(), ex.getMessage(), ex);
            throw new PidomeJSONRPCException(ex.getMessage());
        }
    }

    /**
     * Execute methods based on unnamed parameters.
     * @param MethodHandler
     * @param method
     * @param parameters
     * @return
     * @throws PidomeJSONRPCException
     * @throws RPCMethodNotFoundException
     * @throws RPCMethodInvalidParamsException 
     */
    protected Object execMethod(RemoteClientInterface client, RemoteClient requestBy, String MethodHandler, String method, Object[] parameters) throws PidomeJSONRPCException, RPCMethodNotFoundException, RPCMethodInvalidParamsException, RemoteClientException {
        LOG.debug("searching for {} in {}", method, MethodHandler);
        caller = client;
        initiator = requestBy;
        try {
            if(client.getRole().hasNameSpaceAccess(MethodHandler)){
                if(methodMapping.containsKey(MethodHandler) && methodMapping.get(MethodHandler).containsKey(method)){
                    Method runMethod = (methods.get(MethodHandler))[methodMapping.get(MethodHandler).get(method)];
                    if((runMethod.isAnnotationPresent(PiDomeJSONRPCPrivileged.class) && client.getRole().role()!=BaseRole.ADMIN) || 
                       (runMethod.isAnnotationPresent(PiDomeJSONRPCLeveraged.class) && client.getRole().role()!=BaseRole.DISPLAY)){
                        LOG.warn("Client {} is not allowed to access privileged method {}.{}", client.getLoginName(), MethodHandler,method);
                        throw new RPCMethodNotFoundException();
                    }
                    LOG.debug("Declared params for {} : {}", method, runMethod.getParameterTypes());
                    LOG.debug("Passed in params for '{}': {}", method, parameters);
                    if(parameters.length==runMethod.getParameterTypes().length){
                        Map<String,Object> result = new HashMap<>();
                        try {
                            result.put("success", true);
                            result.put("message", "");
                            result.put("data", runMethod.invoke(this, parameters));
                            return result;
                        } catch(IllegalAccessException ex){
                            LOG.error("Method {} could not be executed: {}", method, ex.getMessage(), ex);
                            throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.SERVER_ERROR, "Method "+method+" could not be executed: "+ex.getMessage());
                        } catch (InvocationTargetException ex){
                            Throwable cause = ex.getCause();
                            if(cause == null) {
                                throw new IllegalStateException("Got InvocationTargetException, but the cause is null.", ex);
                            } else if(cause instanceof RuntimeException) {
                                throw (RuntimeException) cause;
                            } else if(cause instanceof Exception) {
                                LOG.error("Invocation failed with cause: ", cause);
                                return constructMethodExceptionReturn(cause.getMessage(), ex.getCause());
                            } else {
                                LOG.error("Invocation failed with error: ", cause);
                                return constructMethodExceptionReturn(ex.getMessage(), ex.getCause());
                            }
                        } catch (IllegalArgumentException ex) {
                            LOG.debug("Incorrect parameters for '{}': {}", method, ex.getMessage(), ex);
                            throw new RPCMethodInvalidParamsException();
                        } catch (Exception ex){
                            return constructMethodExceptionReturn(ex.getMessage(), ex);
                        }
                    } else {
                        LOG.warn("Ivalid (unnamed) method request, parameters issue for request {}.{}: {}", MethodHandler, method, runMethod.getParameterTypes());
                        throw new RPCMethodInvalidParamsException();
                    }
                } else {
                    LOG.warn("Method (unnamed) {}.{} not found", MethodHandler, method);
                    throw new RPCMethodNotFoundException();
                }
            } else {
                LOG.warn("Client {} is not allowed to access namespace {}", client.getLoginName(), MethodHandler);
                throw new PidomeJSONRPCException(JSONError.METHOD_NOT_FOUND);
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", client.getLoginName(), ex.getMessage(), ex);
            throw new PidomeJSONRPCException(ex.getMessage());
        }
    }
    
    /**
     * Executes a method which does not need parameters.
     * @param MethodHandler
     * @param method
     * @return
     * @throws PidomeJSONRPCException
     * @throws RPCMethodInvalidParamsException
     * @throws RPCMethodNotFoundException 
     */
    protected Object execMethod(RemoteClientInterface client, RemoteClient requestBy, String MethodHandler,String method) throws PidomeJSONRPCException, RPCMethodInvalidParamsException, RPCMethodNotFoundException, RemoteClientException {
        LOG.debug("Searching without parameter set for {} in {}", method, MethodHandler);
        caller = client;
        initiator = requestBy;
        try {
            if(client.getRole().hasNameSpaceAccess(MethodHandler)){
                if(methodMapping.containsKey(MethodHandler) && methodMapping.get(MethodHandler).containsKey(method)){
                    Map<String,Object> result = new HashMap<>();
                    Method runMethod = (methods.get(MethodHandler))[methodMapping.get(MethodHandler).get(method)];
                    if((runMethod.isAnnotationPresent(PiDomeJSONRPCPrivileged.class) && client.getRole().role()!=BaseRole.ADMIN) || 
                       (runMethod.isAnnotationPresent(PiDomeJSONRPCLeveraged.class) && client.getRole().role()!=BaseRole.DISPLAY)){
                        LOG.warn("Client {} is not allowed to access privileged method {}.{}", client.getLoginName(), MethodHandler,method);
                        throw new RPCMethodNotFoundException();
                    }
                    try {
                        result.put("success", true);
                        result.put("message", "");
                        result.put("data", runMethod.invoke(this, (Object[]) null));
                        return result;
                    } catch (IllegalAccessException ex) {
                        LOG.error("Method {} could not be executed: {}", method, ex.getMessage(), ex);
                        throw new PidomeJSONRPCException(PidomeJSONRPCException.JSONError.SERVER_ERROR, "Method "+method+" could not be executed: "+ex.getMessage());
                    } catch (InvocationTargetException ex){
                        Throwable cause = ex.getCause();
                        if(cause == null) {
                            throw new IllegalStateException("Got InvocationTargetException, but the cause is null.", ex);
                        } else if(cause instanceof RuntimeException) {
                            throw (RuntimeException) cause;
                        } else if(cause instanceof Exception) {
                            LOG.error("Invocation failed with cause: ", cause);
                            return constructMethodExceptionReturn(cause.getMessage(), ex.getCause());
                        } else {
                            LOG.error("Invocation failed with error: ", cause);
                            return constructMethodExceptionReturn(ex.getMessage(), ex.getCause());
                        }
                    } catch (IllegalArgumentException ex) {
                        LOG.debug("Incorrect parameters for '{}': {}", method, ex.getMessage(), ex);
                        throw new RPCMethodInvalidParamsException();
                    } catch (Exception ex){
                        return constructMethodExceptionReturn(ex.getMessage(), ex);
                    }
                } else {
                    LOG.warn("Called method with namespace {} and method {} is not found.",MethodHandler,method);
                    throw new RPCMethodNotFoundException();
                }
            } else {
                LOG.warn("Client {} is not allowed to access namespace {}", client.getLoginName(), MethodHandler);
                throw new PidomeJSONRPCException(JSONError.METHOD_NOT_FOUND);
            }
        } catch (Exception ex) {
            LOG.error("problem executing for {}: {}", client.getLoginName(), ex.getMessage(), ex);
            throw new PidomeJSONRPCException(ex.getMessage());
        }
    }
    
    protected final RemoteClientInterface getCaller(){
        return this.caller;
    }
    
    protected final RemoteClient getCallerResource(){
        return this.initiator;
    }
    
    /**
     * This is used for unknown global exception or InvocationTargetException (Which wraps the original exception)
     * @param ex
     * @return 
     */
    private static Object constructMethodExceptionReturn(String errorMessage, Throwable ex){
        Map<String,Object> result = new HashMap<>();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("X");
        Map<String, Object> errorDetails = new HashMap<>();
        if (loggerConfig.getLevel().isMoreSpecificThan(Level.INFO)) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            errorDetails.put("trace", sw.toString().replaceAll("\\n", "\\\\\\\\n").replaceAll("\\t", "    "));
        }
        errorDetails.put("message", errorMessage);
        Map<String, Object> serverError = new HashMap<>();
        serverError.put("message", "Server error");
        serverError.put("code", -32000L);
        result.put("jsonrpc", "2.0");
        result.put("error", serverError);
        result.put("data", errorDetails);
        LOG.error("Constructed method exception message: {}", result);
        return result;
    }
    
    /**
     * Returns method parameters in order.
     * @param MethodHandler
     * @param method
     * @param parameters
     * @return 
     */
    protected static Object[] getExecParameterObjects(String MethodHandler,String method, JSONObject parameters){
        Map<Integer, Map<String, Object>> methodParams = namedMap.get(MethodHandler).get(method);
        Object[] parameterSet = new Object[parameters.size()];
        for (int key : methodParams.keySet()) {
            parameterSet[key] = parameters.get(methodParams.get(key).entrySet().iterator().next().getKey());
        }
        return parameterSet;
    }
    
    /**
     * Returns the given parameters.
     * Used for compatibility with the above.
     * @param MethodHandler
     * @param method
     * @param parameters
     * @return 
     */
    protected static Object[] getExecParameterObjects(String MethodHandler,String method, JSONArray parameters){
        Object[] returnParam = new Object[parameters.size()];
        for(int i=0;i<parameters.size();i++){
            returnParam[i] = parameters.get(i);
        }
        return returnParam;
    }
    
}
