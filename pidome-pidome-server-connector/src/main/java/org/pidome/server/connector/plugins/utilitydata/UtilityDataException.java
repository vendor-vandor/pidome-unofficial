/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.utilitydata;

/**
 *
 * @author John
 */
public class UtilityDataException extends Exception {

    /**
     * Creates a new instance of <code>UtilityDataException</code> without
     * detail message.
     */
    public UtilityDataException() {
    }

    /**
     * Constructs an instance of <code>UtilityDataException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UtilityDataException(String msg) {
        super(msg);
    }
}
