/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

/**
 *
 * @author John
 */
public class DashboardServiceException extends Exception {

    /**
     * Creates a new instance of <code>DashboardServiceException</code> without
     * detail message.
     */
    public DashboardServiceException() {
    }

    /**
     * Constructs an instance of <code>DashboardServiceException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DashboardServiceException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>ScenesServiceException</code> with
     * the specified detail message and original exception.
     *
     * @param msg The detail message.
     * @param exc The original Exception.
     */
    public DashboardServiceException(String msg, Exception exc) {
        super(msg, exc);
    }
    
}