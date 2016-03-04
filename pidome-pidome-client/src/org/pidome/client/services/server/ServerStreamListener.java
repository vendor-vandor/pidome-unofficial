/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.services.server;

/**
 *
 * @author John Sirach
 */
public interface ServerStreamListener {
    public void handleStreamEvent(ServerStreamEvent event);
}
