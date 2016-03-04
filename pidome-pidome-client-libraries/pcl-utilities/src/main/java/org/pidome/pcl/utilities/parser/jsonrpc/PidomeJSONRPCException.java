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

package org.pidome.pcl.utilities.parser.jsonrpc;

/**
 * Exception used in JSON parsing.
 * @author John Sirach
 */
public class PidomeJSONRPCException extends Exception {

    /**
     * The JSON error raised.
     */
    JSONError jsonError;
    
    /**
     * The lon version of the request id.
     */
    long longId;
    /**
     * String version of the request id.
     */
    String stringId;
    
    /**
     * If the id is long.
     */
    boolean setLongId = false;
    /**
     * If the id is string.
     */
    boolean customMEssage = false;
    
    /**
     * Possible JSON-RPC errors.
     */
    public enum JSONError {
        /**
         * Parsing failed.
         */
        PARSE_ERROR(-32700),
        /**
         * Invalid request made.
         */
        INVALID_REQUEST(-32600),
        /**
         * Requested method is not found.
         */
        METHOD_NOT_FOUND(-32601),
        /**
         * Incorrect set of parameters used.
         */
        INVALID_PARAMS(-32602),
        /**
         * Parser error in the parser.
         */
        INTERNAL_ERROR(-32603),
        /**
         * Error on the server, not a parse error.
         */
        SERVER_ERROR(-32000); //// - 32099
        
        private final long code;
        
        /**
         * @param code The error code.
         */
        private JSONError(final long code) {
            this.code = code;
        }

        /**
         * Constant to long.
         * @return Long code of the JSON error.
         */
        public final long toLong(){
            return code;
        }
        
    }

    /**
     * Constructs a default error for internal use. 
     * @param message The error occurring.
     */
    public PidomeJSONRPCException(String message) {
        super(message);
    }
    
    /**
     * Constructs a default error with null id.
     * @param jsonError The JSON error.
     */
    public PidomeJSONRPCException(JSONError jsonError) {
        super();
        this.jsonError = jsonError;
    }
    
    /**
     * Constructs a default error with id.
     * @param id The id of the request.
     * @param jsonError The JSON error raised.
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
     * @param jsonError The JSON error.
     * @param msg the detail message.
     */
    public PidomeJSONRPCException(JSONError jsonError, String msg) {
        super(msg);
        this.jsonError = jsonError;
    }
    
    /**
     * Constructs exception with long id.
     * @param id The id of the request.
     * @param jsonError The JSON error.
     * @param msg The message about the error.
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
     * @return Exception message in JSON-RPC format.
     */
    public final String getJsonReadyMessage(){
        String message = constructMessage();
        return "{\"jsonrpc\": \"2.0\", \"error\": {\"code\": "+jsonError.toLong()+", \"message\": \""+message+"\", \"data\": { \"message\":\"" + getMessage() + "\"}}, \"id\": "+(stringId!=null?"\""+stringId+"\"":(setLongId==true?longId:"null"))+"}";
    }
    
    /**
     * Constructs the error message.
     * @return Message explaining the error type.
     */
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
