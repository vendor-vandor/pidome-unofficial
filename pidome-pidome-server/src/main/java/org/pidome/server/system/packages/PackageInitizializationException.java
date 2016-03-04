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
public class PackageInitizializationException extends Exception {

    /**
     * Creates a new instance of <code>PackageInitizializationException</code>
     * without detail message.
     */
    public PackageInitizializationException() {
    }

    /**
     * Constructs an instance of <code>PackageInitizializationException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public PackageInitizializationException(String msg) {
        super(msg);
    }
}
