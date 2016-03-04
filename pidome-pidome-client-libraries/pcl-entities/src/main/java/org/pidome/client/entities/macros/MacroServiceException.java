/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.macros;

/**
 * Used when there is an issue with the presence.
 * @author John
 */
public class MacroServiceException extends Exception {

    /**
     * Creates a new instance of <code>MacroServiceException</code> without
     * detail message.
     */
    public MacroServiceException() {
    }

    /**
     * Constructs an instance of <code>MacroServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MacroServiceException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>MacroServiceException</code> with
     * the specified detail message.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public MacroServiceException(String msg, Exception exc) {
        super(msg, exc);
    }
    
}
