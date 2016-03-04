/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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

import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.misc.utils.PiDomeLocaleUtils;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.HeapDumper;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.clients.persons.PersonBaseRole.BaseRole;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.persons.PersonsManagementException;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.audit.Notification;
import org.pidome.server.system.audit.NotificationsReader;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.dayparts.DayPartException;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.db.DB;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

/**
 *
 * @author John Sirach
 */
public class SystemServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements SystemServiceJSONRPCWrapperInterface {

    public SystemServiceJSONRPCWrapper(){
        super();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getClientInitPaths", null);
                put("getServerInfo", null);
                put("performHeapDump", null);
                put("getCurrentTime", null);
                put("getLocaleSettings", null);
                put("setServerSetting", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("setting", "");}});
                        put(1,new HashMap<String,Object>(){{put("values", new Object());}});
                    }
                });
                put("getUnreadNotifications", null);
                put("getNotifications", null);
                put("markNotificationRead", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("markAllNotificationsRead", null);
                put("deleteNotification", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteAllNotifications", null);
                put("storeData", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                        put(1,new HashMap<String,Object>(){{put("data", new Object());}});
                    }
                });
                put("restoreData", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("name", "");}});
                    }
                });
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public final Object getClientInitPaths() throws PidomeJSONRPCException {
        return new HashMap<String,Object>(){{
            put("jsonurl", "/jsonrpc.json");
        }};
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean setServerSetting(String setting, Object value) throws RPCMethodInvalidParamsException {
        switch(setting){
            case "setdebug":
                final Map<String,Boolean> doesDebug = (Map<String,Boolean>)value;
                if(doesDebug.containsKey("value")){
                    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                    Configuration config = ctx.getConfiguration();
                    LoggerConfig loggerConfig = config.getLoggerConfig("RollingFile"); 
                    if((boolean)doesDebug.get("value") == true){
                        loggerConfig.setLevel(Level.DEBUG);
                    } else {
                        loggerConfig.setLevel(Level.INFO);
                    }
                    ctx.updateLoggers();
                    LOG.info("Log level modified to: {}", loggerConfig.getLevel());
                    Map<String,Object> sendObject = new HashMap<String,Object>(){
                        {
                            put("setting", "setdebug");
                            put("value", (boolean)doesDebug.get("value"));
                        }
                    };
                    ClientMessenger.send("SystemService","setServerSetting", 0, sendObject);
                    return true;
                } else {
                    throw new RPCMethodInvalidParamsException("Invalid 'value' parameter");
                }
            case "setclientsauth":
                final Map<String,Boolean> doesAuth = (Map<String,Boolean>)value;
                if(doesAuth.containsKey("value")){
                    if((boolean)doesAuth.get("value") == true){
                        SystemConfig.setProperty("system", "displayclients.auth", "true");
                    } else {
                        SystemConfig.setProperty("system", "displayclients.auth", "false");
                    }
                    try {
                        SystemConfig.store("system", null);
                    } catch (IOException ex) {
                        LOG.error("Clients auth changed, but could not store setting: {}", ex.getMessage());
                    }
                    LOG.info("Client authorization modified to: {}", (boolean)doesAuth.get("value"));
                    Map<String,Object> sendObject = new HashMap<String,Object>(){
                        {
                            put("setting", "setclientsauth");
                            put("value", (boolean)doesAuth.get("value"));
                        }
                    };
                    ClientMessenger.send("SystemService","setServerSetting", 0, sendObject);
                    return true;
                } else {
                    throw new RPCMethodInvalidParamsException("Invalid 'value' parameter");
                }
            case "settimezone":
                final Map<String,String> timezoneValues = (Map<String,String>)value;
                if(timezoneValues.containsKey("timezone") && timezoneValues.containsKey("latitude") && timezoneValues.containsKey("longitude")){
                    TimeUtils.setNewLocalizedTimeZoneData(timezoneValues.get("timezone"), timezoneValues.get("latitude"), timezoneValues.get("longitude"));
                    PiDomeLocaleUtils.setNewLocale(timezoneValues.get("locale"));
                    LOG.info("Time zone modified to: {} - {}, {}", timezoneValues.get("timezone"), timezoneValues.get("latitude"), timezoneValues.get("longitude"));
                    Map<String,Object> sendObject = new HashMap<String,Object>(){
                        {
                            put("setting", "settimezone");
                            put("timezone", timezoneValues.get("timezone"));
                            put("locale", timezoneValues.get("locale"));
                            put("latitude", timezoneValues.get("latitude"));
                            put("longitude", timezoneValues.get("longitude"));
                        }
                    };
                    ClientMessenger.send("SystemService","setServerSetting", 0, sendObject);
                    return true;
                } else {
                    throw new RPCMethodInvalidParamsException("Invalid 'settimezone' parameters");
                }
            default:
                throw new RPCMethodInvalidParamsException("Invalid 'setting'");
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getCurrentTime() {
        return new HashMap<String,Object>(){{
                put("time",TimeUtils.getInstance().get24HoursTime());
                put("date",TimeUtils.getInstance().getDDMMYYYYdate());
                put("shorttext", TimeUtils.getInstance().getShortDateTextRepresentation());
                put("day",TimeUtils.getInstance().getDayOfMonth());
                put("dayname",TimeUtils.getInstance().getDayName());
                put("month",TimeUtils.getInstance().getMonth());
                put("monthname",TimeUtils.getInstance().getMonthName());
                put("year",TimeUtils.getInstance().getYear());
                put("sunrise",TimeUtils.getSunrise());
                put("sunset",TimeUtils.getSunset());
                put("latitude", TimeUtils.getCurrentLatitude());
                put("longitude", TimeUtils.getCurrentLongitude());
            }};
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getLocaleSettings() {
        return new HashMap<String,Object>(){{
                put("locale",Locale.getDefault().toLanguageTag());
                put("timezone",TimeUtils.getCurrentTimeZone());
            }};
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getUnreadNotifications(){
        return new HashMap<String,Object>(){{
                put("notifications",NotificationsReader.getUnreadNotifications());
            }};
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getNotifications(){
        List<Map<String,Object>> nots = new ArrayList<>();
        List<Notification> notList = NotificationsReader.getNotifications();
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMM d, yyyy HH:mm:ss");
        for(Notification not:notList){
            nots.add(new HashMap<String,Object>(){{
                put("id",not.getId());
                put("date",format.format(not.getDate()));
                put("type",not.getType());
                put("subject",not.getSubject());
                put("message",not.getMessage());
                put("read",not.getRead());
            }});
        }
        return new HashMap<String,Object>(){{
                put("notifications",notList.size());
                put("messages",nots);
            }};
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object markNotificationRead(Number id) {
        return NotificationsReader.markNotificationRead(id.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object markAllNotificationsRead() {
        return NotificationsReader.markAllNotificationsRead();
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deleteNotification(Number id) {
        return NotificationsReader.deleteNotification(id.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deleteAllNotifications() {
        return NotificationsReader.deleteAllNotifications();
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean storeData(String name, Object data) throws PidomeJSONRPCException,SQLException {
        String serializedJson = PidomeJSONRPCUtils.getParamCollection(data);
        LOG.debug("Saving arbtotrary data to '{}': '{}'", name, data);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
            PreparedStatement prep = fileDBConnection.prepareStatement("INSERT OR REPLACE INTO arbitrarydata (varname, varcontent) \n" +
"  VALUES (  COALESCE((SELECT varname FROM arbitrarydata WHERE varname = ?), ?), \n" +
"            ?\n" +
"          );");
            prep.setString(1, name);
            prep.setString(2, name);
            prep.setString(3, serializedJson);
            prep.execute();
        } catch (SQLException ex) {
            LOG.error("Could not save arbitrary data: {} ", ex.getMessage(), ex);
            throw new SQLException("Could not save arbitrary data: "+ ex.getMessage());
        }
        LOG.debug("Stored data for: {}", name);
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object restoreData(String name) throws ParseException,SQLException {
        Map<String,Object> data = new HashMap<>();
        data.put("data", new Object());
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prep = fileDBConnection.prepareStatement("SELECT varcontent FROM arbitrarydata WHERE varname=? LIMIT 1")) {
            prep.setString(1, name);
            ResultSet rs = prep.executeQuery();
            if(rs.next()) {
                 data.put("data", new JSONParser().parse(rs.getString("varcontent")));
            }
        } catch (SQLException ex) {
            LOG.error("Could not get arbitrary data: {} ", ex.getMessage(), ex);
            throw new SQLException("Could not get arbitrary data: "+ ex.getMessage());
        }
        return data;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object performHeapDump() throws PidomeJSONRPCException {
        if(System.getProperties().containsKey("enableHeapdumps")){
            SimpleDateFormat name = new SimpleDateFormat("yyyyMMddHHmmss");
            HeapDumper.dumpHeap(name.format(new Date()) + "_heapDump.bin", true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getBasicServerStatusData() throws PidomeJSONRPCException {
        Map<String,Object> data = new HashMap<>();
        data.put("time", getCurrentTime());
        try {
            data.put("presence", PresenceService.current().getName());
        } catch (PresenceException ex) {
            data.put("presence", "Unknown");
        }
        try {
            data.put("userstatus", UserStatusService.current().getName());
        } catch (UserStatusException ex) {
            data.put("userstatus", "Unknown");
        }
        try {
            data.put("daypart", DayPartsService.current().getName());
        } catch (DayPartException ex) {
            data.put("daypart", "Unknown");
        }
        try {
            data.put("loggedinusername", PersonsManagement.getInstance().getPerson(getCaller().getId()).getLoginName());
        } catch (PersonsManagementException ex) {
            data.put("loggedinusername", "Unknown");
        }
        try {
            data.put("loggedinuserisadmin", PersonsManagement.getInstance().getPerson(getCaller().getId()).getRole().role().equals(BaseRole.ADMIN));
        } catch (PersonsManagementException ex) {
            data.put("loggedinuserisadmin", false);
        }
        return data;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getServerInfo(){
        Map<String, Object> serverData = new HashMap<>();
        try {
            serverData.put("version_major", SystemConfig.getProperty("system", "server.major"));
            serverData.put("version_minor", SystemConfig.getProperty("system", "server.minor"));
            serverData.put("version_build", SystemConfig.getProperty("system", "server.build"));
            serverData.put("version_name", SystemConfig.getProperty("system", "server.releasename"));
            serverData.put("version_release", SystemConfig.getProperty("system", "server.releasetype"));
        } catch (ConfigPropertiesException ex){
            serverData.put("version_major", "Unknown");
            serverData.put("version_minor", "Unknown");
            serverData.put("version_build", "Unknown");
            serverData.put("version_name", "Unknown");
            serverData.put("version_release", "Unknown");
        }

        TimeUtils tmUtls = new TimeUtils();

        serverData.put("date_date", tmUtls.getYYYYMMDDDate());
        serverData.put("date_time", tmUtls.getFull24HoursTime());
        
        return serverData;
    }
    
}