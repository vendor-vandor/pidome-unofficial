/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.photoframe.screens.photoscreen.actors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.userstatus.UserStatus;
import org.pidome.client.photoframe.screens.photoscreen.components.BottomLeftTable;
import org.pidome.client.photoframe.utils.ShadedLabel;
import org.pidome.client.system.PCCClient;

/**
 *
 * @author John
 */
public class UserStatusesActor extends ShadedLabel {
    
    private final PCCClient client;
    
    PropertyChangeListener statusChangeListener = this::dataUpdateHelper;
    
    PropertyChangeListener dataHelper = this::dataUpdateHelper;
    
    public UserStatusesActor(PCCClient client) {
        super("Unknown", .8f);
        this.client = client;
        try {
            this.client.getEntities().getUserStatusService().getCurrentPresenceProperty().addPropertyChangeListener(statusChangeListener);
            this.client.getEntities().getUserStatusService().preload();
        } catch (EntityNotAvailableException ex) {
            Logger.getLogger(BottomLeftTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public final void dataUpdateHelper(PropertyChangeEvent pce){
        UserStatus newStatus = (UserStatus)pce.getNewValue();
        this.setText(newStatus.getName());
    }
    
}