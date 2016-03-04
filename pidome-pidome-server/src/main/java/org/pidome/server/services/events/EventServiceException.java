/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.events;

/**
 *
 * @author John
 */
public class EventServiceException extends Exception {

    /**
     * Creates a new instance of <code>EventServiceException</code> without
     * detail message.
     */
    public EventServiceException() {
    }

    /**
     * Constructs an instance of <code>EventServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public EventServiceException(String msg) {
        super(msg);
    }
}
