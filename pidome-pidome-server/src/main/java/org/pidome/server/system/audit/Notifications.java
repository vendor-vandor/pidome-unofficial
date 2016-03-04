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

package org.pidome.server.system.audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.system.db.DB;

/**
 * Very simple notifications system.
 * @author John
 */
public final class Notifications {
    
    static Logger LOG = LogManager.getLogger(Notifications.class);
    static Map<String, Object> sendObject = new HashMap<>();
    
    public enum NotificationType{
        INFO,WARNING,ERROR,OK
    }
    
    private enum Originator {
        INTERNAL,EXTERNAL;
    }

    private static synchronized void sendMessage(Originator from, NotificationType type, String subject, String message){
        sendObject.put("originates", from.toString());
        sendObject.put("type", type.toString());
        sendObject.put("subject", subject);
        sendObject.put("message", message);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("insert into 'notificationlog' ('datetime', 'originates', 'type', 'subject','message') values (?,?,?,?,?)")){
            prep.setString(1, dateFormat.format(new Date()));
            prep.setString(2, from.toString());
            prep.setString(3, type.toString());
            prep.setString(4, subject);
            prep.setString(5, message);
            prep.execute();
            prep.close();
        } catch (SQLException ex) {
            LOG.error("could not save notification: {} ", ex.getMessage(), ex);
        }
        ClientMessenger.send("NotificationService","sendNotification", 0, sendObject);
    }
    
    public static void sendMessage(NotificationType type, String subject, String message){
        if(!subject.toLowerCase().equals("system")){
            sendMessage(Originator.EXTERNAL, type, subject, message);
        }
    }
    
    public static void sendSystemMessage(NotificationType type, String message){
        sendMessage(Originator.INTERNAL, type, "System", message);
    }
    
    public static void sendPersonalizedMessage(int personId, NotificationType type, String subject, String message){
        sendObject.put("originates", Originator.EXTERNAL);
        sendObject.put("type", type.toString());
        sendObject.put("subject", subject);
        sendObject.put("message", message);
        try {
            PersonsManagement.getInstance().getPerson(personId).sendMessage("NotificationService", ClientMessenger.getConstructBroadcast("NotificationService","sendNotification", sendObject));
        } catch (PersonsManagementException ex) {
            LOG.error("Person ({}) not found to send personalized message to", personId, ex);
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Personalized message could not be contstructed: ", ex.getMessage(), ex);
        }
    }
}
