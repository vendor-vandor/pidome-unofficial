/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.packages;

/**
 *
 * @author John
 */
public class PackagePermissionsNotAvailableException extends Exception {

    /**
     * Creates a new instance of <code>PackageNotApprovedException</code>
     * without detail message.
     */
    public PackagePermissionsNotAvailableException() {
    }

    /**
     * Constructs an instance of <code>PackageNotApprovedException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PackagePermissionsNotAvailableException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>PackageNotApprovedException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     * @param ex Throwable
     */
    public PackagePermissionsNotAvailableException(String msg, Throwable ex) {
        super(msg, ex);
    }
    
}
