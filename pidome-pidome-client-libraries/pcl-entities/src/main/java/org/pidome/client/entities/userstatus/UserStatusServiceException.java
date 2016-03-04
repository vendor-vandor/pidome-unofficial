/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.userstatus;

import org.pidome.client.entities.macros.*;

/**
 * Used when there is an issue with the presence.
 * @author John
 */
public class UserStatusServiceException extends Exception {

    /**
     * Creates a new instance of <code>PresenceServiceException</code> without
     * detail message.
     */
    public UserStatusServiceException() {
    }

    /**
     * Constructs an instance of <code>PresenceServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UserStatusServiceException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>PresenceServiceException</code> with
     * the specified detail message.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public UserStatusServiceException(String msg, Exception exc) {
        super(msg, exc);
    }
    
}
