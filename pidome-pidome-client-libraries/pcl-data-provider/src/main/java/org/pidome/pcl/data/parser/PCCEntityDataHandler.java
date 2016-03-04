/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.parser;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPC;
import org.pidome.pcl.utilities.parser.jsonrpc.PidomeJSONRPCException;

/**
 * Main class for handling data meant for PiDome entities.
 * It parses received RPC and provides the namespaces and methods used with their parameters.
 * @author John
 */
public final class PCCEntityDataHandler {
    
    static {
        Logger.getLogger(PCCEntityDataHandler.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * RPC parser.
     */
    PidomeJSONRPC parser;
    
    /**
     * Plain string data.
     */
    private String plainData;
    
    /**
     * Constructor parses received data.
     * 
     * @param data RPC String to be handled.
     * @throws PCCEntityDataHandlerException When parsing fails.
     */
    public PCCEntityDataHandler(String data) throws PCCEntityDataHandlerException {
        try {
            parser = new PidomeJSONRPC(data);
            plainData = data;
        } catch (PidomeJSONRPCException ex) {
            Logger.getLogger(PCCEntityDataHandler.class.getName()).log(Level.SEVERE, null, ex);
            throw new PCCEntityDataHandlerException(ex);
        }
    }
    
    
    /**
     * Return the plain data.
     * @return String
     */
    public final String getPlainData(){
        return plainData;
    }
    
    /**
     * Returns the received namespace.
     * @return Namespace string.
     */
    public final String getNamespace(){
        return parser.getNameSpace();
    }

    /**
     * Returns the method used.
     * @return Method string.
     */
    public final String getMethod(){
        return parser.getMethod();
    }
    
    /**
     * Returns the parameters.
     * Parameters are always name value pairs
     * The parameter receiving party must know how these are constructed.
     * Parameters are used by RPC broadcasts.
     * @return always be one of: Map, ArrayList, Number, Boolean, String.
     */
    public final Map<String,Object> getParameters(){
        return this.parser.getParameters();
    }
    
    /**
     * Returns the RPC result.
     * Parameters are always name value pairs
     * The parameter receiving party must know how these are constructed.
     * Parameters are used by RPC broadcasts.
     * @return always be one of: Map, ArrayList, Number, Boolean, String.
     */
    public final Map<String,Object> getResult(){
        return this.parser.getResult();
    }
    
    /**
     * Returns the id of a result.
     * The RPC id is only available in combination with a result.
     * @return The id which can be number or string.
     */
    public final Object getId(){
        return this.parser.getId();
    }
    
}