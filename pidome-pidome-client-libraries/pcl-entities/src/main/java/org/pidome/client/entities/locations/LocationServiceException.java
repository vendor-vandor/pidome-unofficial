/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.locations;

/**
 *
 * @author John
 */
public class LocationServiceException extends Exception {

    /**
     * Creates a new instance of <code>LocationServiceException</code>
     * without detail message.
     */
    public LocationServiceException() {
    }

    /**
     * Constructs an instance of <code>LocationServiceException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public LocationServiceException(String msg) {
        super(msg);
    }
    /**
     * Constructs an instance of <code>LocationServiceException</code> with
     * the specified throwable.
     *
     * @param ex the throwable.
     */
    public LocationServiceException(Throwable ex) {
        super(ex);
    }
    
}
