/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.scenes.components.mainstage.quickappmenuitems;

import java.util.List;
import javafx.collections.ListChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.components.messaging.AbstractMessagingMessage;
import org.pidome.client.system.domotics.components.messaging.ClientMessaging;
import org.pidome.client.system.domotics.components.messaging.ClientMessagingListener;
import org.pidome.client.system.scenes.components.mainstage.displays.MessagesDisplay;
import org.pidome.client.system.scenes.windows.WindowManager;

/**
 *
 * @author John Sirach
 */
public class QuickAppMessagesButton extends QuickAppItem implements ClientMessagingListener {

    static Logger LOG = LogManager.getLogger(QuickAppMessagesButton.class);
    
    public QuickAppMessagesButton(){
        super("messages", "Messages");
        enableNotification();
        ClientMessaging.addClientMessagingListener(this);
    }
    
    @Override
    public final void clicked(double openX, double openY){
        WindowManager.openWindow(new MessagesDisplay("Messages", "Messages"), openX, openY);
    }

    @Override
    public void onChanged(ListChangeListener.Change change) {
        while(change.next())
        if (change.wasRemoved() && change.wasAdded()) {
            List<AbstractMessagingMessage> addedList = change.getAddedSubList();
            for(int i=0; i< addedList.size();i++){
                if(!addedList.get(i).isRead()){
                    notification().add(1);
                }
            }
            List<AbstractMessagingMessage> removedList = change.getRemoved();
            for(int i=0; i< removedList.size();i++){
                if(!removedList.get(i).isRead()){
                    notification().substract(1);
                }
            }
        } else if (change.wasRemoved()) {
            List<AbstractMessagingMessage> removedList = change.getRemoved();
            for(int i=0; i< removedList.size();i++){
                if(!removedList.get(i).isRead()){
                    notification().substract(1);
                }
            }
        } else if (change.wasAdded()) {
            List<AbstractMessagingMessage> addedList = change.getAddedSubList();
            for(int i=0; i< addedList.size();i++){
                if(!addedList.get(i).isRead()){
                    notification().add(1);
                }
            }
        }
    }
    
}
