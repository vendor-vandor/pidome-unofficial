/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.hardware.devices;

/**
 *
 * @author John
 */
public class DeviceSkeletonException extends Exception {

    /**
     * Creates a new instance of <code>DeviceSkeletonException</code> without
     * detail message.
     */
    public DeviceSkeletonException() {
    }

    /**
     * Constructs an instance of <code>DeviceSkeletonException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DeviceSkeletonException(String msg) {
        super(msg);
    }
    /**
     * Constructs an instance of <code>DeviceSkeletonException</code> with the
     * specified throwable.
     *
     * @param ex the rethrown trhowable.
     */
    public DeviceSkeletonException(Throwable ex) {
        super(ex);
    }
}
