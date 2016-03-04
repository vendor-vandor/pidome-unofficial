/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Exception used when there are connection issues.
 * @author John
 */
public class PCCConnectionException extends Exception {

    /**
     * Creates a new instance of <code>PCCConnectionException</code> without
     * detail message.
     */
    public PCCConnectionException() {
    }

    /**
     * Rethrow a throwable.
     * @param ex The original exception
     */
    public PCCConnectionException(Throwable ex) {
        super(ex);
    }
    
    /**
     * Constructs an instance of <code>PCCConnectionException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PCCConnectionException(String msg) {
        super(msg);
    }
}
