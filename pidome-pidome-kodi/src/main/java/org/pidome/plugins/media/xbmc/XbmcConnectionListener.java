/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.media.xbmc;

/**
 * Interface for handling an event during connection
 * @author John Sirach
 */
public interface XbmcConnectionListener {
    public void handleXbmcEvent(XbmcEvent event);
    public void handleConnectionEvent(String event);
}
