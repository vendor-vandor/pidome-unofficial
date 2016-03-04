/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.video.capture.faces.recognition;

/**
 *
 * @author John
 */
public class FaceRecognitionException extends Exception {

    /**
     * Creates a new instance of <code>FaceRecognitionException</code> without
     * detail message.
     */
    public FaceRecognitionException() {
    }

    /**
     * Constructs an instance of <code>FaceRecognitionException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public FaceRecognitionException(String msg) {
        super(msg);
    }
}
