/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.pcl.networking.connections.broadcasts;

/**
 * Exception used when there is an issue with parsing a broadcast message.
 * @author John
 */
public class BroadcastParserException extends Exception {

    /**
     * Creates a new instance of <code>BroadcastParserException</code> without
     * detail message.
     */
    public BroadcastParserException() {
    }

    /**
     * Constructs an instance of <code>BroadcastParserException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public BroadcastParserException(String msg) {
        super(msg);
    }
}
