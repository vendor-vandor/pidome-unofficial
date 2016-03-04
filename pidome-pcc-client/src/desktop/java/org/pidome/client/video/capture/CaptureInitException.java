/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture;

/**
 *
 * @author John
 */
public class CaptureInitException extends Exception {

    /**
     * Creates a new instance of <code>CaptureInitException</code> without
     * detail message.
     */
    public CaptureInitException() {
    }

    /**
     * Constructs an instance of <code>CaptureInitException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public CaptureInitException(String msg) {
        super(msg);
    }
}
