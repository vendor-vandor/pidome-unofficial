/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.connection;

/**
 * Exception used when data issues arise.
 * @author John
 */
public class ConnectionException extends Exception {

    /**
     * Creates a new instance of <code>ConnectionException</code> without detail
     * message.
     */
    public ConnectionException() {
    }

    /**
     * Rethrow a throwable.
     * @param ex Original exception.
     */
    public ConnectionException(Throwable ex) {
        super(ex);
    }
    
    /**
     * Constructs an instance of <code>ConnectionException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public ConnectionException(String msg) {
        super(msg);
    }
}
