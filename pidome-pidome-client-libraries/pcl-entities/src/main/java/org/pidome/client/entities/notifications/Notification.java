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
package org.pidome.client.entities.notifications;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple notification
 * @author John
 */
public class Notification {

    static {
        Logger.getLogger(Notification.class.getName()).setLevel(Level.ALL);
    }
    
    /**
     * Types of notifications.
     */
    public enum NotificationType {
        /**
         * Informative.
         */
        INFO, 
        /**
         * Needs attention.
         */
        WARNING, 
        /**
         * Not good.....
         */
        ERROR, 
        /**
         * Mostly after Warning or Error, we are ok.
         */
        OK
    }

    /**
     * Where an message originates from.
     */
    public enum Originator {
        /**
         * A system generated message. important.
         */
        INTERNAL, 
        /**
         * A message created by rules or other user intervention.
         */
        EXTERNAL;
    }

    private Originator originates;
    private NotificationType type;
    private String subject;
    private String message;

    private boolean read = false;

    /**
     * Constructor.
     * @param originates Where it comes from (INTERNAL/EXTERNAL).
     * @param type The message type (WARNING/ERROR/OK/INFO).
     * @param subject The message subject.
     * @param message The message body.
     */
    public Notification(String originates, String type, String subject, String message) {
        switch (originates) {
            case "INTERNAL":
                this.originates = Originator.INTERNAL;
                break;
            default:
                this.originates = Originator.EXTERNAL;
                break;
        }
        switch (type) {
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

    /**
     * Mark a message read.
     * @param read Marks a notification as read.
     * @return if read.
     */
    public final boolean markRead(boolean read) {
        this.read = read;
        return this.read;
    }

    /**
     * Returns originator.
     * This is internal or external. An internal message is a system message.
     * @return s the message originator.
     */
    public final Originator getOriginator() {
        return this.originates;
    }

    /**
     * Returns message type.
     * @return The notification type.
     */
    public final NotificationType getType() {
        return this.type;
    }

    /**
     * Returns subject.
     * @return The subject of the message.
     */
    public final String getSubject() {
        return this.subject;
    }

    /**
     * The message itself.
     * @return The message body.
     */
    public final String getMessage() {
        return this.message;
    }
        
}