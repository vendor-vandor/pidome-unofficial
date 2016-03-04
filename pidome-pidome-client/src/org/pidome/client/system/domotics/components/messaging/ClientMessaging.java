/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.time.ClientTime;

/**
 *
 * @author John
 */
public final class ClientMessaging implements DomComponent,ClientDataConnectionListener {

    static ObservableList<ClientMessage> messageList = FXCollections.observableArrayList();
    
    static Logger LOG = LogManager.getLogger(ClientMessaging.class);
    
    public ClientMessaging(){
        ClientData.addClientDataConnectionListener(this);
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        switch(event.getEventType()){
            case ClientDataConnectionEvent.CLIENTRECEIVED:
                switch(event.getMethod()){
                    case "sendDisplayClientMessage":
                        ClientMessage clientMessage = new ClientMessage((String)event.getParams().get("from"), (String)event.getParams().get("message"));
                        LOG.debug("Adding message: {}", event.getParams());
                        messageList.add(clientMessage);
                    break;
                }
            break;
        }
    }
    
    public static List<ClientMessage> getMessageList() {
        return messageList;
    }
    
    public final void markAsRead(int index, boolean mark){
        try {
            messageList.get(index).markRead(mark);
        } catch (IndexOutOfBoundsException ex){
            LOG.error("Invalid message to mark as (un)read: {}", index);
        }
    }
    
    public static void addClientMessagingListener(ClientMessagingListener l){
        messageList.addListener((ListChangeListener)l);
        LOG.debug("Added client messenger listener: {}", l.getClass().getName());
    }
    
    public static void removeClientMessagingListener(ClientMessagingListener l){
        messageList.removeListener((ListChangeListener)l);
        LOG.debug("Removed client messenger listener: {}", l.getClass().getName());
    }
    
    public static void sendMessage(String to, String message){
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("displayname", to);
                put("message", message);
            }
        };
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("ClientService.sendDisplayClientMessage", "ClientService.sendDisplayClientMessage", sendObject));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send data: {}", sendObject);
        }
    }
    
    public final class ClientMessage extends AbstractMessagingMessage {
        
        String from;
        String subject;
        String message;
        String dateTimeStringId;
        String date;
        
        protected ClientMessage(String from, String message){
            this.from = from;
            this.message = message;
            this.dateTimeStringId = ClientTime.getYearNumberProperty().get() + "" +
                                    ClientTime.getMonthNumberProperty().get() + "" +
                                    ClientTime.getDayNumberProperty() + "" +
                                    ClientTime.get24HourNameProperty();
            this.date = ClientTime.getShortDateStringProperty().getValueSafe() + ", " + ClientTime.get24HourNameProperty().getValueSafe();
        }
        
        @Override
        public String getDate() {
            return this.date;
        }
        
        @Override
        public final String getDateTimeStringId(){
            return this.dateTimeStringId;
        }
        @Override
        public final String getFrom(){
            return from;
        }
        
        @Override
        public final String getMessage(){
            return message;
        }

        @Override
        public String getSubject() {
            return subject;
        }

    }
    
    
}
