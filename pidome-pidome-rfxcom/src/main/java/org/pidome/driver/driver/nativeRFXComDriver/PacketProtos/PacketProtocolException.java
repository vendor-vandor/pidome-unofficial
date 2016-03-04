/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeRFXComDriver.PacketProtos;

/**
 *
 * @author John
 */
public class PacketProtocolException extends Exception {

    /**
     * Creates a new instance of <code>PacketProtocolException</code> without
     * detail message.
     */
    public PacketProtocolException() {
    }

    /**
     * Constructs an instance of <code>PacketProtocolException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public PacketProtocolException(String msg) {
        super(msg);
    }
}
