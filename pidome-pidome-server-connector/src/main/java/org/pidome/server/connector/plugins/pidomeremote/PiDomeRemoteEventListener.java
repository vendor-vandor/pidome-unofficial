/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.pidomeremote;

/**
 *
 * @author John
 */
public interface PiDomeRemoteEventListener {
    
    public void handleRemoteDataEvent(int pluginId, String remoteString);
    public void handleRemoteRecordingEvent(int pluginId, boolean enabled);
    
}
