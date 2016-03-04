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

package org.pidome.client.system.rpc;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
public class PidomeJSONRPCException extends Exception {

    JSONError jsonError;
    
    long longId;
    String stringId;
    
    boolean setLongId = false;
    boolean customMEssage = false;
    
    public enum JSONError {
        PARSE_ERROR(-32700),
        INVALID_REQUEST(-32600),
        METHOD_NOT_FOUND(-32601),
        INVALID_PARAMS(-32602),
        INTERNAL_ERROR(-32603),
        SERVER_ERROR(-32000); //// - 32099
        
        private final long code;
        
        /**
         * @param text
         */
        private JSONError(final long code) {
            this.code = code;
        }

        /**
         * 
         * @return 
         */
        public final long toLong(){
            return code;
        }
        
    }

    /**
     * Constructs a default error for internal use. 
     * @param message
     */
    public PidomeJSONRPCException(String message) {
        super(message);
    }
    
    /**
     * Constructs a default error with null id.
     * @param jsonError 
     */
    public PidomeJSONRPCException(JSONError jsonError) {
        super();
        this.jsonError = jsonError;
    }
    
    /**
     * Constructs a default error with id.
     * @param id
     * @param jsonError 
     */
    public PidomeJSONRPCException(Object id, JSONError jsonError) {
        super();
        this.jsonError = jsonError;
        if(id instanceof Long){
            longId = (long)id;
            setLongId = true;
        } else if (id instanceof String){
            stringId = (String)id;
        }
    }
    
    /**
     * Constructs an instance of <code>PidomeJSONRPCException</code> with the
     * specified detail message.
     *
     * @param jsonError
     * @param msg the detail message.
     */
    public PidomeJSONRPCException(JSONError jsonError, String msg) {
        super(msg);
        this.jsonError = jsonError;
    }
    
    /**
     * Constructs exception with long id.
     * @param id
     * @param jsonError
     * @param msg 
     */
    public PidomeJSONRPCException(Object id, JSONError jsonError, String msg) {
        super(msg);
        this.jsonError = jsonError;
        if(id instanceof Long){
            longId = (long)id;
            setLongId = true;
        } else if (id instanceof String){
            stringId = (String)id;
        }
    }
    
    /**
     * Returns a well formed error message.
     * @return 
     */
    public final String getJsonReadyMessage(){
        String message = constructMessage();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("X"); 
        String exceptionAsString;
        if(loggerConfig.getLevel().isAtLeastAsSpecificAs(Level.DEBUG)){
            exceptionAsString = ", \"data\": { \"message\":\"" + getMessage() + "\", \"trace\": \"";
            StringWriter sw = new StringWriter();
            printStackTrace(new PrintWriter(sw));
            exceptionAsString += sw.toString().replaceAll("\\n", "\\\\n").replaceAll("\\t", "    ") + "\"}";
        } else {
            exceptionAsString = ", \"data\": { \"message\":\"" + getMessage() + "\"}";
        }
        return "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "+jsonError.toLong()+", \"message\": \""+message+"\""+exceptionAsString+"}, \"id\": "+(stringId!=null?"\""+stringId+"\"":(setLongId==true?longId:"null"))+"}";
    }
    
    final String constructMessage(){
        switch (jsonError) {
            case PARSE_ERROR:
                return "Parse error";
            case INVALID_REQUEST:
                return "Invalid Request";
            case METHOD_NOT_FOUND:
                return "Method not found";
            case INVALID_PARAMS:
                return "Invalid params";
            case INTERNAL_ERROR:
                return "Internal error";
            case SERVER_ERROR:
                return "Server error";
            default:
                jsonError = JSONError.SERVER_ERROR;
                return "Unknown error or server error";
        }
    }
}
