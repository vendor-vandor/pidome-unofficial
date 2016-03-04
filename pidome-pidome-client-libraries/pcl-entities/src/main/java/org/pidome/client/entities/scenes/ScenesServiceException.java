/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.scenes;

/**
 *
 * @author John
 */
public class ScenesServiceException extends Exception {

    /**
     * Creates a new instance of <code>ScenesServiceException</code> without
     * detail message.
     */
    public ScenesServiceException() {
    }

    /**
     * Constructs an instance of <code>ScenesServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ScenesServiceException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ScenesServiceException</code> with
     * the specified detail message and original exception.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public ScenesServiceException(String msg, Exception exc) {
        super(msg, exc);
    }
    
}
