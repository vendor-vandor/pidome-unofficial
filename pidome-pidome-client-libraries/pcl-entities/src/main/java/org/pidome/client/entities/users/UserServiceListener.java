/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.users;

/**
 * Notification listener interface.
 * @author John
 */
public interface UserServiceListener {
    
    /**
     * User notifications.
     */
    public enum NotificationType {
        /**
         * An user presence has been changed.
         */
        USER_PRESENCECHANGED;
    }
    
    /**
     * Handle a notification.
     * @param user The user object where the notification is about.
     */
    public void handleUserNotification(User user);
}
