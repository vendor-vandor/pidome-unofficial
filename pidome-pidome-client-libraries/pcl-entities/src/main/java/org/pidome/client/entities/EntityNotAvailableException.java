/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities;

/**
 * When an entity is not available.
 * @author John
 */
public class EntityNotAvailableException extends Exception {

    /**
     * Creates a new instance of <code>EntityNotAvailableException</code>
     * without detail message.
     */
    public EntityNotAvailableException() {
    }

    /**
     * Constructs an instance of <code>EntityNotAvailableException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public EntityNotAvailableException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>EntityNotAvailableException</code> with
     * the specified detail message.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public EntityNotAvailableException(String msg, Exception exc) {
        super(msg, exc);
    }
    
}
