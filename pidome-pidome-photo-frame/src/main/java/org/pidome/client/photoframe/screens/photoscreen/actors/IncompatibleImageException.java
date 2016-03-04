/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

/**
 *
 * @author John
 */
public class IncompatibleImageException extends Exception {

    /**
     * Creates a new instance of <code>IncompaitbleImageException</code> without
     * detail message.
     */
    public IncompatibleImageException() {
    }

    /**
     * Constructs an instance of <code>IncompaitbleImageException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public IncompatibleImageException(String msg) {
        super(msg);
    }
}
