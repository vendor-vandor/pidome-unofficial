/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.accesscontrollers;

/**
 *
 * @author John
 */
public class AccessControllerServiceException extends Exception {

    /**
     * Creates a new instance of <code>AccessControllerServiceException</code>
     * without detail message.
     */
    public AccessControllerServiceException() {
    }

    /**
     * Constructs an instance of <code>AccessControllerServiceException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public AccessControllerServiceException(String msg) {
        super(msg);
    }
}
