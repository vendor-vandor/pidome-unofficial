/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.messaging;

import org.pidome.client.system.scenes.components.mainstage.QuickAppMenu;

/**
 *
 * @author John
 */
public abstract class AbstractMessagingMessage {
    
    boolean read = false;
    
    public final boolean markRead(boolean read){
        if(this.read == false && read==true){
            QuickAppMenu.getMessagesButton().notification().substract(1);
        } else if (this.read == true && read == false){
            QuickAppMenu.getMessagesButton().notification().add(1);
        }
        this.read = read;
        return this.read;
    }
    
    public final boolean isRead(){
        return this.read;
    }
    
    public abstract String getDate();
    
    public abstract String getDateTimeStringId();
    
    public abstract String getMessage();
    public abstract String getSubject();
    public abstract String getFrom();
    
}
