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

package org.pidome.server.services.http.rpc;

import java.sql.SQLException;
import org.json.simple.parser.ParseException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;

/**
 *
 * @author John
 */
public interface SystemServiceJSONRPCWrapperInterface {
    
    /**
     * Get the clients initialization paths.
     * @return 
     * @throws org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException 
     */
    public Object getClientInitPaths() throws PidomeJSONRPCException;
    
    /**
     * Set server settings.
     * @param setting
     * @param value
     * @return 
     * @throws org.pidome.server.services.http.rpc.RPCMethodInvalidParamsException 
     */
    @PiDomeJSONRPCPrivileged
    public boolean setServerSetting(String setting, Object value) throws RPCMethodInvalidParamsException;
    
    /**
     * Returns the current time on the server.
     * @return 
     */
    public Object getCurrentTime();
    
    /**
     * Get the system's locale settings including timezone.
     * @return 
     */
    public Object getLocaleSettings();
    
    /**
     * Get the amount of unread notifications.
     * @return 
     */
    public Object getUnreadNotifications();
    
    /**
     * Get notifications delivered by the system
     * @return 
     */
    public Object getNotifications();
    
    /**
     * Marks a system notification read.
     * @param id
     * @return 
     */
    public Object markNotificationRead(Number id);
    
    /**
     * Marks all notifications read.
     * @return 
     */
    public Object markAllNotificationsRead();
    
    /**
     * Deletes a single notification.
     * @param id
     * @return 
     */
    public Object deleteNotification(Number id);
    
    /**
     * Deletes all notifications.
     * @return 
     */
    public Object deleteAllNotifications();
    
    /**
     * Stores arbitrary JSON data identified by name.
     * @param name Identifier for storing the data
     * @param data Valid JSON data to be stored.
     * @return true when stored.
     */
    public boolean storeData(String name, Object data) throws PidomeJSONRPCException,SQLException;
    
    /**
     * Restores any arbitrary JSON data identified by name.
     * @param name Identifier for restoring the data.
     * @return Valid JSON data.
     */
    public Object restoreData(String name) throws ParseException,SQLException;
    
    /**
     * Make it possible to do heap dump.
     * This is only available with the appropiate parameter set.
     * @return
     * @throws PidomeJSONRPCException 
     */
    public Object performHeapDump() throws PidomeJSONRPCException;
    
    /**
     * Returns basic data like current day part, date and time, global presence and status.
     * @return Basic serveeer info.
     * @throws PidomeJSONRPCException 
     */
    public Object getBasicServerStatusData() throws PidomeJSONRPCException;
    
    /**
     * Returns basic server information.
     * @return Server information.
     */
    public Object getServerInfo();
    
}