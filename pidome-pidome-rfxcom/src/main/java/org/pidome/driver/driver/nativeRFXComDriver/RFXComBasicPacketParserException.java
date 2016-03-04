/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver;

/**
 *
 * @author John
 */
public class RFXComBasicPacketParserException extends Exception {

    /**
     * Creates a new instance of <code>RFXComBasicPacketParserException</code>
     * without detail message.
     */
    public RFXComBasicPacketParserException() {
    }

    /**
     * Constructs an instance of <code>RFXComBasicPacketParserException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public RFXComBasicPacketParserException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>RFXComBasicPacketParserException</code>
     * with the specified detail message and original exception.
     *
     * @param msg the detail message.
     * @param thrown
     */
    public RFXComBasicPacketParserException(String msg, Throwable thrown) {
        super(msg, thrown);
    }
    
}
