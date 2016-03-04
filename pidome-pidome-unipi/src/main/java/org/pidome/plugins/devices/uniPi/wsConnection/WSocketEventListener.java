/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.devices.uniPi.wsConnection;

/**
 * Listener interface for the socket ports.
 * @author John Sirach
 */
public interface WSocketEventListener {
    public void handleWSocketEvent(WSocketEvent event);
}
