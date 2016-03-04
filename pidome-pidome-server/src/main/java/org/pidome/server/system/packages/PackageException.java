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
public class PackageException extends Exception {

    /**
     * Creates a new instance of <code>PackageException</code> without detail
     * message.
     */
    public PackageException() {
    }

    /**
     * Constructs an instance of <code>PackageException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PackageException(String msg) {
        super(msg);
    }
}
