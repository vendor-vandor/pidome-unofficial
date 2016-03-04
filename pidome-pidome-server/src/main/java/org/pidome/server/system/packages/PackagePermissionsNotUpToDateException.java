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
public class PackagePermissionsNotUpToDateException extends Exception {

    /**
     * Creates a new instance of
     * <code>PackagePermissionsNotUpToDateException</code> without detail
     * message.
     */
    public PackagePermissionsNotUpToDateException() {
    }

    /**
     * Constructs an instance of
     * <code>PackagePermissionsNotUpToDateException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public PackagePermissionsNotUpToDateException(String msg) {
        super(msg);
    }
}
