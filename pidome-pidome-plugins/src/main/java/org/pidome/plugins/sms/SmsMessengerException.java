/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.sms;

/**
 *
 * @author John
 */
public class SmsMessengerException extends Exception {

    /**
     * Creates a new instance of <code>SmsMessengerException</code> without
     * detail message.
     */
    public SmsMessengerException() {
    }

    /**
     * Constructs an instance of <code>SmsMessengerException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SmsMessengerException(String msg) {
        super(msg);
    }
}
