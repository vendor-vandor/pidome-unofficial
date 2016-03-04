/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 *
 * @author John
 */
public class PidomeClients implements DomComponent,ClientDataConnectionListener {

    static ObservableList<String> clientList = FXCollections.observableArrayList();
    
    static Logger LOG = LogManager.getLogger(PidomeClients.class);
    
    public PidomeClients(){
        ClientData.addClientDataConnectionListener(this);
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        switch(event.getEventType()){
            case ClientDataConnectionEvent.CLIENTRECEIVED:
                switch(event.getMethod()){
                    case "signOn":
                        clientList.add((String)event.getParams().get("name"));
                    break;
                    case "signOff":
                        clientList.remove((String)event.getParams().get("name"));
                    break;
                }
            break;
        }
    }
    
    public final void addClient(String clientName){
        clientList.add(clientName);
    }
    
    public static ObservableList getClientList(){
        return FXCollections.unmodifiableObservableList(clientList);
    }
    
}
