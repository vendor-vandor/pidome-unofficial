/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.parser;

/**
 * Exception used when there is an issue with entity data.
 * @author John
 */
public class PCCEntityDataHandlerException extends Exception {

    /**
     * Creates a new instance of <code>PCCEntityDataHandlerException</code>
     * without detail message.
     */
    public PCCEntityDataHandlerException() {
    }

    /**
     * Exception, Just throw it.
     * @param thrw Original Exception.
     */
    public PCCEntityDataHandlerException(Throwable thrw) {
        super(thrw);
    }
    
    /**
     * Constructs an instance of <code>PCCEntityDataHandlerException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PCCEntityDataHandlerException(String msg) {
        super(msg);
    }
}
