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

package org.pidome.server.system.datastorage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;
import org.pidome.misc.utils.MinuteListener;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem.FieldType;
import org.pidome.server.connector.plugins.graphdata.RoundRobinPluginDataInterface;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 *
 * @author John Sirach
 */
public final class RoundRobinDataStorage implements RoundRobinPluginDataInterface,MinuteListener {

    @Override
    public void handleMinuteUpdate(TimeUtils timeutils) {
        curDate = new Date();
        curRoundedTime = (int) (snapToMinuteCount(1).getTime() / 1000);
        if (curDate.getMinutes() == 0) {
            hourlyUpdateRunnable().run();
        }
    }

    public enum Source {
        DEVICE,PLUGIN
    }
    
    boolean dbLoaded = false;
    boolean dtLoaded = false;
    boolean newFile  = false;
    
    Map<String,Map<String,Map<Integer,Double>>> rawData = new HashMap<>();
    
    ArrayList<RoundRobinDataGraphItem> dataTypes = new ArrayList<>();
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(RoundRobinDataStorage.class);
    
    HikariDataSource connectionPool;
    
    Date curDate = new Date();
    int curRoundedTime; /// This var makes sure that even if data would be stored at 13:00:02, it is stored as 13:00:00, because it is meant that way but this way we catch any delays.
    
    Source currentSource;
    
    int currentItemId;
    
    /**
     * Initializes a device data storage database and creates a new one if it does not exist.
     * @param source 
     * @param itemId 
     */
    public RoundRobinDataStorage(Source source, int itemId){
        currentSource = source;
        currentItemId = itemId;
        curDate = new Date();
        curRoundedTime = (int) (snapToMinuteCount(1).getTime() / 1000);
        TimeUtils.addMinuteListener(this);
        try {
            String dbFile;
            switch(source){
                case PLUGIN:
                    dbFile = SystemConfig.getProperty("system", "database.plugindatalocation") + itemId + ".data";
                break;
                default:
                    dbFile = SystemConfig.getProperty("system", "database.devicedatalocation") + itemId + ".data";
                break;
            }
            File f = new File(dbFile);
            if(f.exists()) {
                LOG.debug("Database " + f.getAbsolutePath() + " connected");
                connect(dbFile);
                newFile = false;
            } else {
                LOG.info("Database " + f.getAbsolutePath() + " does not exist, trying to create");
                connect(dbFile);
                newFile = true;
            }
        } catch (ConfigPropertiesException | SQLException ex) {
            LOG.error("Databases not loaded:", ex.getMessage());
        }
    }

    /**
     * Sets up pooling for this single device database connection instance.
     * @param dbFile
     * @throws SQLException 
     */
    final void setupPool(final String dbFile) throws SQLException {
        if(connectionPool==null){
            // setup the connection pool
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbFile);
            config.setUsername("");
            config.setPassword("");
            config.setAutoCommit(true);
            config.addDataSourceProperty("characterEncoding","utf8");
            config.addDataSourceProperty("useUnicode","true");
            config.setLeakDetectionThreshold(10000);
            config.setMaximumPoolSize(5);
            config.setPoolName("DBConnectionPool_" + this.currentSource.toString() + "_" + this.currentItemId);
            connectionPool = new HikariDataSource(config);
        }
    }
    
    /**
     * Connects to the database file.
     * @param dbFile
     * @throws SQLException 
     */
    final void connect(String dbFile) throws SQLException {
        setupPool(dbFile);
    }
    
    /**
     * Registers data to be stored in the database for this device.
     * @param dataTypes 
     */
    @Override
    public final void registerDataTypes(ArrayList<RoundRobinDataGraphItem> dataTypes){
        this.dataTypes = dataTypes;
        LOG.debug("New datatype set for storing: {}", this.dataTypes);
        if(newFile == true){
            createInitialDBTables(dataTypes);
            newFile = false;
        }
        for(RoundRobinDataGraphItem field: dataTypes){
            field.resetData();
        }
    }
    
    /**
     * Creates the initial tables for every single storage item.
     * creates hourly, daily, weekly, monthly and yearly fields
     * @param dataTypes 
     */
    final void createInitialDBTables(ArrayList<RoundRobinDataGraphItem> dataTypes){
        try {
            try (Connection connection = connectionPool.getConnection(); Statement stmt = connection.createStatement()) {
                connection.setAutoCommit(true);
                for(RoundRobinDataGraphItem field: dataTypes){
                    stmt.addBatch("CREATE TABLE '"+field.getFieldName()+"_minutes' (\"time\" INTEGER, \"value\" REAL)");
                    stmt.addBatch("CREATE TABLE '"+field.getFieldName()+"_hours' (\"time\" INTEGER, \"value\" REAL)");
                    stmt.addBatch("CREATE TABLE '"+field.getFieldName()+"_days' (\"time\" INTEGER, \"value\" REAL)");
                }
                stmt.executeBatch();
                stmt.close();
                connection.close();
            }
        } catch (SQLException ex) {
            LOG.error("Could not create storage DB:{}", ex.getMessage());
        }
    }
    
    /**
     * Stores a specific value.
     * @param dataGroup
     * @param dataName
     * @param data 
     */
    @Override
    public final void store(String dataGroup, String dataName, double data){
        String fieldNameCheck = dataGroup + "_" + dataName;
        for(RoundRobinDataGraphItem field: dataTypes){
            if(field.getFieldName().equals(fieldNameCheck)){
                field.handleData(data);
                saveData(field);
            }
        }
    }

    /**
     * Returns a specific field.
     * @param dataGroup
     * @param dataName
     * @return 
     * @throws java.lang.Exception 
     */
    @Override
    public final RoundRobinDataGraphItem getStorageField(String dataGroup, String dataName) throws Exception {
        String fieldNameCheck = dataGroup + "_" + dataName;
        for(RoundRobinDataGraphItem field: dataTypes){
            if(field.getFieldName().equals(fieldNameCheck)){
                return field;
            }
        }
        throw new Exception ("field not found");
    }
    
    /**
     * Saves the data to the database based on the current date/time setting and resets the specific round robin of the data stored.
     * @param dataName 
     */
    final void saveData(final RoundRobinDataGraphItem field){
        storeData(field.getFieldName(), field.getCurData());
    }
    

    /**
     * Creates a runnable for hourly updates in the tables;
     * @return 
     */
    private Runnable hourlyUpdateRunnable(){
        Runnable run = () -> {
            curDate = new Date();
            curRoundedTime = (int) (snapToMinuteCount(1).getTime() / 1000);
            for(RoundRobinDataGraphItem field: dataTypes){
                LOG.debug("Hourly update of field: {}", field.getFieldName());
                if (curDate.getMinutes() == 0) {
                    updateDayData(field);
                }
                if (((curDate.getHours()) == 0 && curDate.getMinutes() == 0)) {
                    updateYearData(field);
                }
            }
        };
        return run;
    }
    
    /**
     * Stores the data.
     * @param dataName
     * @param dataType
     * @param date
     * @param data 
     */
    final void storeData(String dataName, Double data){
        int boundary = curRoundedTime - 3599; /// only keep the last hour, but loose the first minute (00 is the end of the previous hour)
        if(boundary > 0){
            try (Connection connection = connectionPool.getConnection()) {
                try (PreparedStatement prepDel = connection.prepareStatement("delete from '"+dataName+"_minutes' where time<?;")) {
                    prepDel.setInt(1, boundary);
                    prepDel.execute();
                    prepDel.close();
                } catch (Exception ex){
                    try (Statement stmt = connection.createStatement()) {
                        connection.setAutoCommit(true);
                        stmt.addBatch("CREATE TABLE IF NOT EXISTS '"+dataName+"_minutes' (\"time\" INTEGER, \"value\" REAL)");
                        stmt.addBatch("CREATE TABLE IF NOT EXISTS '"+dataName+"_hours' (\"time\" INTEGER, \"value\" REAL)");
                        stmt.addBatch("CREATE TABLE IF NOT EXISTS '"+dataName+"_days' (\"time\" INTEGER, \"value\" REAL)");
                        stmt.executeBatch();
                        stmt.close();
                    }
                }
                try (PreparedStatement prepIns = connection.prepareStatement("insert into '"+dataName+"_minutes' values(?,?);")) {
                    prepIns.setInt(1, curRoundedTime);
                    prepIns.setDouble(2, data);
                    prepIns.execute();
                    prepIns.close();
                }
                connection.close();
            } catch (SQLException ex) {
                LOG.error("Problem executing DB data actions: ", ex.getMessage(), ex);
            }
        } else {
            LOG.debug("Illegal storage time frame: {} - {}", boundary, curRoundedTime);
        }
    }
    
    /**
     * Sets the data of the last hour.
     * only keep the last day, but loose the start of the day at 00 because it is used as an end mark for the day before.
     * @param field 
     */
    final void updateDayData(final RoundRobinDataGraphItem field){
        int boundary = curRoundedTime - 86599;
        if(boundary > 0){
            try (Connection connection = connectionPool.getConnection()) {
                try (PreparedStatement prepDel = connection.prepareStatement("delete from '"+field.getFieldName()+"_hours' where time<?;")) {
                    prepDel.setInt(1, boundary);
                    prepDel.execute();
                    prepDel.close();
                } catch (Exception ex){
                    LOG.error("Could not clear hour history for field {}", field.getFieldName(), ex);
                }
                String getFieldTotal;
                switch(field.getFieldType()){
                    case SUM:
                        getFieldTotal = "SELECT sum(value) FROM '"+field.getFieldName()+"_minutes'";
                    break;
                    case ABSOLUTE:
                        getFieldTotal = "SELECT value FROM '"+field.getFieldName()+"_minutes' ORDER BY time ASC LIMIT 1";
                    break;
                    default:
                        getFieldTotal = "SELECT avg(value) FROM '"+field.getFieldName()+"_minutes'";
                    break;
                }
                try (PreparedStatement prepIns = connection.prepareStatement("insert into '"+field.getFieldName()+"_hours' values(?,("+getFieldTotal+"));")) {
                    prepIns.setInt(1, curRoundedTime);
                    prepIns.execute();
                    prepIns.close();
                } catch (Exception ex){
                    LOG.error("Could not update hourly data table for: {}", field.getFieldName());
                }
                connection.close();
            } catch (SQLException ex) {
                LOG.error("Problem executing DB data actions: ", ex.getMessage(), ex);
            }
        } else {
            LOG.debug("Illegal storage time frame: {} - {}", boundary, curRoundedTime);
        }
    }

    /**
     * Inserts the database table which holds the days data.
     * This data is held until someone chooses to clean it up.
     * @param field 
     */
    final void updateYearData(final RoundRobinDataGraphItem field){
        String getFieldTotal;
        switch(field.getFieldType()){
            case SUM:
                getFieldTotal = "SELECT sum(value) FROM '"+field.getFieldName()+"_hours'";
            break;
            case ABSOLUTE:
                getFieldTotal = "SELECT value FROM '"+field.getFieldName()+"_hours' ORDER BY time ASC LIMIT 1";
            break;
            default:
                getFieldTotal = "SELECT avg(value) FROM '"+field.getFieldName()+"_hours'";
            break;
        }
        try (Connection connection = connectionPool.getConnection()) {
            try (PreparedStatement prepIns = connection.prepareStatement("insert into '"+field.getFieldName()+"_days' values(?,("+getFieldTotal+"));")) {
                prepIns.setInt(1, (int)(snapToOneMinuteBeforeLastDayEnd().getTime()/1000));
                prepIns.execute();
                prepIns.close();
            } catch (Exception ex){
                LOG.error("Could not update daily data table for: {}", field.getFieldName());
            }
            connection.close();
        } catch (SQLException ex) {
            LOG.error("Problem executing DB data actions: ", ex.getMessage(), ex);
        }
    }
    
    /**
     * Returns the total amount of a field based on the 00:00 time value on the day this function is called.
     * @param field
     * @return 
     */
    public final double getTodayTotal(String dataGroup, String dataName){
        String fieldNameCheck = dataGroup + "_" + dataName;
        int todayAtMidnight = (int)(new DateTime().withTimeAtStartOfDay().toDate().getTime()/1000);
        try (Connection connection = connectionPool.getConnection()) {
            for(RoundRobinDataGraphItem field: dataTypes){
                if(field.getFieldName().equals(fieldNameCheck)){
                    if(field.getFieldType().equals(FieldType.ABSOLUTE)){
                        try (PreparedStatement prep = connection.prepareStatement("SELECT value FROM '"+fieldNameCheck+"_minutes' WHERE time>? ORDER BY time ASC LIMIT 1")) {
                            prep.setInt(1,todayAtMidnight);
                            try(ResultSet rsTotal = prep.executeQuery()) {
                                if (rsTotal.next()) {
                                    double value = rsTotal.getDouble("value");
                                    rsTotal.close();
                                    return value;
                                }
                            }
                        }
                    }
                }
            }
            try (PreparedStatement prep = connection.prepareStatement("SELECT ((SELECT total(value) FROM '"+fieldNameCheck+"_hours' WHERE time>?)+(SELECT total(value) FROM '"+dataGroup + "_" + dataName+"_minutes' WHERE time>?)) as total")) {
                prep.setInt(1,todayAtMidnight);
                prep.setInt(2,todayAtMidnight);
                try(ResultSet rsTotal = prep.executeQuery()) {
                    if (rsTotal.next()) {
                        double value = rsTotal.getDouble("value");
                        rsTotal.close();
                        connection.close();
                        return value;
                    }
                }
            }
            connection.close();
        } catch (SQLException ex) {
            LOG.error("Problem retrieving today data: ", ex.getMessage(), ex);
        }
        return 0D;
    }
    
    /**
     * Rounds time to the lower bound of a minute.
     * 12:30:59 = 12:30:00
     * @param origDate
     * @param minute 0 would be at :00/:10/:20 etc.., one is at every minute, two would be every two minutes etc...
     * @return 
     */
    final Date snapToMinuteCount(int minute){
        return new Date((long)Math.floor((curDate.getTime() / (1000.0 * 60 * minute))) * (1000 * 60 * minute));
    }
    
    /**
     * Makes sure that when day data storage is being done it is done on the day itself.
     * Did not know a better name for this one, but when data is saved on a new day while it is for the day before, this function makes sure it happens that way.
     * This function only works when it is being called within an hour after a new day has started.
     * @return 
     */
    final Date snapToOneMinuteBeforeLastDayEnd() {
        return new Date(((long) Math.floor((curDate.getTime() / (1000.0 * 3600))) * (1000 * 3600))-1000L);
    }
    
    /**
     * Stops the data saving service.
     */
    public final void stop(){
        TimeUtils.removeMinuteListener(this);
        if(this.connectionPool!=null){
            this.connectionPool.shutdown();
        }
    }
    
}
