/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.notifications;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;

/**
 * Notifications.
 * @author John
 */
public final class NotificationService extends Entity implements PCCConnectionNameSpaceRPCListener {
    
    static {
        Logger.getLogger(NotificationService.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Set of listeners.
     */
    private final HashSet<NotificationServiceListener> _listeners = new HashSet<>();
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public NotificationService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * List of internal notifications.
     */
    private ObservableArrayListBean<Notification> messageList = new ObservableArrayListBean();
    
    /**
     * Adds a notification listener.
     * @param listener Message notification listener.
     */
    public final void addNotificationListener(NotificationServiceListener listener){
        if(!_listeners.contains(listener)){
            _listeners.add(listener);
        }
    }
    
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        messageList.clear();
    }
    
    /**
     * Removes a notification listener.
     * @param listener Message notification listener.
     */
    public final void removeNotificationListener(NotificationServiceListener listener){
        _listeners.remove(listener);
    }
    
    /**
     * Returns the current message list.
     * @return The current message list.
     */
    public final ObservableArrayListBean<Notification> getMessageList(){
        return this.messageList;
    }
    
    /**
     * Starting up.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("NotificationService", this);
    }

    /**
     * Clear out the stuff.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("NotificationService", this);
        connection = null;
        messageList.clear();
        _listeners.clear();
    }

    /**
     * Handles notification broadcasts.
     * @param rpcDataHandler PCCEntityDataHandler with message data.
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        switch(rpcDataHandler.getMethod()){
            case "sendNotification":
                Notification clientMessage = new Notification((String)rpcDataHandler.getParameters().get("originates"), 
                                                              (String)rpcDataHandler.getParameters().get("type"), 
                                                              (String)rpcDataHandler.getParameters().get("subject"), 
                                                              (String)rpcDataHandler.getParameters().get("message"));
                broadcastMessage(clientMessage);
                switch((String)rpcDataHandler.getParameters().get("originates")){
                    case "INTERNAL":
                        messageList.add(clientMessage);
                        if(messageList.size()==25){
                            messageList.get(0).markRead(true);
                            messageList.remove(0);
                        }
                    break;
                }
            break;
        }
    }

    /**
     * Broadcasts a new notification.
     * @param notification The notification to be broadcasted.
     */
    private void broadcastMessage(Notification notification){
        Runnable run  = () -> {
            Iterator<NotificationServiceListener> listeners = _listeners.iterator();
            while(listeners.hasNext()){
                listeners.next().handleNotification(notification);
            }
        };
        run.run();
    }
    
    /**
     * Handle command results.
     * Not used.
     * @param rpcDataHandler PCCEntityDataHandler not used.
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
        /// Not used.
    }

    /**
     * Preloads the messages system.
     * @throws EntityNotAvailableException When preloading fails.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
        }
    }
    
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        preload();
    }
    
}
