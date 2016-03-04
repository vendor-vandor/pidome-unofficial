/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.notifications;

/**
 * Notification listener interface.
 * @author John
 */
public interface NotificationServiceListener {
    /**
     * Handle a notification.
     * @param notification The noification passed to the listeners.
     */
    public void handleNotification(Notification notification);
}
