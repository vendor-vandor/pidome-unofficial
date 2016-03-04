/*
 * Copyright 2014 John.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.client.system.domotics.components.notifications;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.domotics.components.messaging.ClientMessaging;
import org.pidome.client.system.scenes.MainScene;
import org.pidome.client.system.scenes.components.mainstage.QuickAppMenu;

/**
 *
 * @author John
 */
public class Notifications implements DomComponent,ClientDataConnectionListener {

    static ObservableList<Notification> messageList = FXCollections.observableArrayList();
    
    static Logger LOG = LogManager.getLogger(ClientMessaging.class);
    
    public enum NotificationType{
        INFO,WARNING,ERROR,OK
    }
    
    public enum Originator {
        INTERNAL,EXTERNAL;
    }
    
    public Notifications(){
        ClientData.addClientDataConnectionListener(this);
    }
     
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        switch(event.getEventType()){
            case ClientDataConnectionEvent.NOTIFICATIONRECEIVED:
                switch(event.getMethod()){
                    case "sendNotification":
                        Notification clientMessage = new Notification((String)event.getParams().get("originates"), 
                                                                      (String)event.getParams().get("type"), 
                                                                      (String)event.getParams().get("subject"), 
                                                                      (String)event.getParams().get("message"));
                        switch((String)event.getParams().get("originates")){
                            case "INTERNAL":
                                LOG.debug("Adding notification: {}", event.getParams());
                                messageList.add(clientMessage);
                                QuickAppMenu.getMessagesButton().notification().add(1);
                                if(messageList.size()==25){
                                    messageList.get(0).markRead(true);
                                    messageList.remove(0);
                                }
                            break;
                        }
                        MainScene.showNotification(clientMessage);
                    break;
                }
            break;
        }
    }
        
    public final class Notification {
        
        private Originator originates;
        private NotificationType type;
        private String subject;
        private String message;
        
        private boolean read = false;
        
        public Notification(String originates, String type, String subject, String message){
            switch(originates){
                case "INTERNAL":
                    this.originates = Originator.INTERNAL;
                break;
                default:
                    this.originates = Originator.EXTERNAL;
                break;
            }
            switch(type){
                case "WARNING":
                    this.type = NotificationType.WARNING;
                break;
                case "ERROR":
                    this.type = NotificationType.ERROR;
                break;
                case "OK":
                    this.type = NotificationType.OK;
                break;
                default:
                    this.type = NotificationType.INFO;
                break;
            }
            this.subject = subject;
            this.message = message;
        }
        
        public final boolean markRead(boolean read){
            if(this.read == false && read==true){
                QuickAppMenu.getMessagesButton().notification().substract(1);
            } else if (this.read == true && read == false){
                QuickAppMenu.getMessagesButton().notification().add(1);
            }
            this.read = read;
            return this.read;
        }
        
        public final Originator getOriginator(){
            return this.originates;
        }
        
        public final NotificationType getType(){
            return this.type;
        }
        
        public final String getSubject(){
            return this.subject;
        }
        
        public final String getMessage(){
            return this.message;
        }
        
    }
    
}
