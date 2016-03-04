/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.backend.data.interfaces.storage;

/**
 * Exception used when storage fails.
 * @author John
 */
public class LocalPreferenceStorageException extends Exception {

    /**
     * Creates a new instance of <code>LocalSettingsStorageException</code>
     * without detail message.
     */
    public LocalPreferenceStorageException() {
    }

    /**
     * Constructs an instance of <code>LocalSettingsStorageException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public LocalPreferenceStorageException(String msg) {
        super(msg);
    }
}
