/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.messengers;

import org.pidome.server.connector.plugins.freeform.FreeformPlugin;

/**
 *
 * @author John
 */
public abstract class Messenger extends FreeformPlugin {
    
    public abstract void sendEmailMessage(String to, String subject, String message) throws MessengerException;
    
    public abstract void sendSmsMessage(String message) throws MessengerException;
    
    public abstract void sendPushBulletMessage(String message) throws MessengerException;
    
    public abstract void receiveMessage(String from, String subject, String message);
    
}
