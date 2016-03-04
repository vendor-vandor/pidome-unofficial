/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.users;

/**
 * Exception used with user service issues.
 * @author John
 */
public class UserServiceException extends Exception {

    /**
     * Creates a new instance of <code>UserServiceException</code> without
     * detail message.
     */
    public UserServiceException() {
    }


    /**
     * Constructs an instance of <code>UserServiceException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public UserServiceException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>UserServiceException</code> with
     * the specified detail message.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public UserServiceException(String msg, Exception exc) {
        super(msg, exc);
    }
}
