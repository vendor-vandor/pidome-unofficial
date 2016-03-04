/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.userstatus;

/**
 *
 * @author John Sirach
 */
public interface UserStatusEventListener {
    public void handleUserPresencesEvent(UserStatusEvent event);
}
