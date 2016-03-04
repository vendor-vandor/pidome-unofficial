/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.storage;

/**
 * Exception used when there is a storage exception.
 * @author John
 */
public class LocalSettingsStorageException extends Exception {

    /**
     * Creates a new instance of <code>LocalSettingsStorageException</code>
     * without detail message.
     */
    public LocalSettingsStorageException() {
    }

    /**
     * Constructs an instance of <code>LocalSettingsStorageException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public LocalSettingsStorageException(String msg) {
        super(msg);
    }
}
