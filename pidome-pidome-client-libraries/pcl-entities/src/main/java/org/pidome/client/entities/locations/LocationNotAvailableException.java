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
public class LocationNotAvailableException extends Exception {

    /**
     * Creates a new instance of <code>LocationNotAvailableException</code>
     * without detail message.
     */
    public LocationNotAvailableException() {
    }

    /**
     * Constructs an instance of <code>LocationNotAvailableException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public LocationNotAvailableException(String msg) {
        super(msg);
    }
    /**
     * Constructs an instance of <code>LocationNotAvailableException</code> with
     * the specified throwable.
     *
     * @param ex the throwable.
     */
    public LocationNotAvailableException(Throwable ex) {
        super(ex);
    }
    
}
