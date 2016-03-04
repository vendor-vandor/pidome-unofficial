/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.system;

/**
 * Exception used when an unknown setting is requested.
 * @author John
 */
public class UnknownPCCSettingException extends Exception {

    /**
     * Creates a new instance of <code>UnknownPCCSettingException</code> without
     * detail message.
     */
    public UnknownPCCSettingException() {
    }

    /**
     * Constructs an instance of <code>UnknownPCCSettingException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownPCCSettingException(String msg) {
        super(msg);
    }
}
