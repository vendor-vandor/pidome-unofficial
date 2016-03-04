/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.data.settings;

/**
 * Exception used when a preference is not found.
 * @author John
 */
public class UnknownPreferenceException extends Exception {

    /**
     * Creates a new instance of <code>UnknownSettingException</code> without
     * detail message.
     */
    public UnknownPreferenceException() {
    }

    /**
     * Constructs an instance of <code>UnknownSettingException</code>.
     *
     * @param ex Original Exception.
     */
    public UnknownPreferenceException(Exception ex) {
        super(ex);
    }
    
    /**
     * Constructs an instance of <code>UnknownSettingException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownPreferenceException(String msg) {
        super(msg);
    }
}
