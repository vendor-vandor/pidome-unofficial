/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.devices.philipsHue;

/**
 *
 * @author John
 */
public class PhilipsHueRegisterException extends Exception {

    /**
     * Creates a new instance of <code>PhilipsHueRegisterException</code>
     * without detail message.
     */
    public PhilipsHueRegisterException() {
    }

    /**
     * Constructs an instance of <code>PhilipsHueRegisterException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PhilipsHueRegisterException(String msg) {
        super(msg);
    }
}
