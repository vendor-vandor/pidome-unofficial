/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.system.audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.pidome.server.system.audit.Notifications.LOG;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public final class NotificationsReader {
    
    static Logger LOG = LogManager.getLogger(NotificationsReader.class);
    
    /**
     * Returns the amount of unread notifications.
     * @return 
     */
    public static int getUnreadNotifications(){
        int amount = 0;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             Statement statementNot = fileDBConnection.createStatement();
             ResultSet rsNot = statementNot.executeQuery("SELECT count(id) as amount FROM notificationlog WHERE read=0;")) {
            while (rsNot.next()) {
                amount = rsNot.getInt("amount");
            }
        } catch (SQLException ex) {
            LOG.error("Could not get the amount of unread notifications: {}", ex.getMessage(), ex);
        }
        return amount;
    } 
    
    /**
     * Returns the list of notifications.
     * @return 
     */
    public static List<Notification> getNotifications(){
        List<Notification> notList = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             Statement statementNot = fileDBConnection.createStatement();
             ResultSet rsNot = statementNot.executeQuery("SELECT id,datetime,originates,type,subject,message,read FROM notificationlog ORDER BY read,datetime DESC;")) {
            while (rsNot.next()) {
                Notification not = new Notification(rsNot.getInt("id"));
                not.setDate(rsNot.getString("datetime"));
                not.setType(rsNot.getString("type"));
                not.setSubject(rsNot.getString("subject"));
                not.setMessage(rsNot.getString("message"));
                not.setRead(rsNot.getBoolean("read"));
                notList.add(not);
            }
        } catch (SQLException ex) {
            LOG.error("Could not get notifications: {}", ex.getMessage(), ex);
        }
        return notList;
    }
    
    public static boolean markNotificationRead(int id){
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE 'notificationlog' SET `read`=1 WHERE id=?")){
            prep.setInt(1, id);
            prep.execute();
            prep.close();
        } catch (SQLException ex) {
            LOG.error("could not mark notification '{}' read: {}.", id, ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
    public static boolean markAllNotificationsRead(){
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE 'notificationlog' SET `read`=1")){
            prep.execute();
            prep.close();
        } catch (SQLException ex) {
            LOG.error("could not mark all notifications read: {}.", ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
    public static boolean deleteNotification(int id){
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM 'notificationlog' WHERE id=?")){
            prep.setInt(1, id);
            prep.execute();
            prep.close();
        } catch (SQLException ex) {
            LOG.error("could not delete notification '{}': {}.", id, ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
    public static boolean deleteAllNotifications(){
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM 'notificationlog'")){
            prep.execute();
            prep.close();
        } catch (SQLException ex) {
            LOG.error("could not delete all notifications: {}.", ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
}