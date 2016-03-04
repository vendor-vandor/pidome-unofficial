/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.pcl.networking.connections.server.streams;

/**
 * Listener interface for the socket ports.
 * @author John Sirach
 */
public interface TelnetEventListener {
    public void handleTelnetEvent(TelnetEvent event);
}
