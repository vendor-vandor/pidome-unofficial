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
package org.pidome.server.system.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import org.pidome.server.system.config.SystemConfig;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;

/**
 *
 * @author John Sirach
 */
public class DB {

    private static boolean DBLoaded = false;
    private static HikariDataSource connectionPool;
    private static final Map<String, String> dbLocations = new HashMap<>();
    private static String baseDbLocation;
    public final static String DB_SYSTEM = "DB_SYSTEM";
    final static String fileTypeDb = ".db";
    static Logger LOG = LogManager.getLogger(DB.class);
    
    /**
     * Initializes the db connection and sets up the pooling.
     * @throws ConfigPropertiesException 
     */
    public static synchronized void init() throws ConfigPropertiesException {
        if (DBLoaded == false) {
            LOG.debug("Loading all databases");
            String sDriverName = "org.sqlite.JDBC";
            try {
                Class.forName(sDriverName);

                /// Here we init all the DB's we are going to use
                baseDbLocation = SystemConfig.getProperty("system", "database.privatelocation");
                dbLocations.put(DB_SYSTEM, SystemConfig.getProperty("system", "database.system"));
                
                File dbFile = new File(baseDbLocation + dbLocations.get(DB_SYSTEM) + fileTypeDb);
                if(!dbFile.exists()){
                    createCleanDB(DB_SYSTEM);
                }
                
                // setup the connection pool
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:sqlite:" + baseDbLocation + dbLocations.get(DB_SYSTEM) + fileTypeDb);
                config.setUsername("");
                config.setPassword("");
                config.setAutoCommit(true);
                config.addDataSourceProperty("characterEncoding","utf8");
                config.addDataSourceProperty("useUnicode","true");
                config.setPoolName("DBConnectionPool_PiDomeServer");
                config.setLeakDetectionThreshold(5000);
                connectionPool = new HikariDataSource(config);
                
                LOG.debug("Database driver loaded and config and pooling set");
                
                checkForDBUpdates();
                
                DBLoaded = true;
                
            } catch (ConfigPropertiesException ex) {
                LOG.error("DB configuration error: {}", ex.getMessage(), ex);
                throw new ConfigPropertiesException("Database problem: '"+ex.getMessage()+"', please check the log file");
            } catch (ClassNotFoundException ex) {
                LOG.error("DB driver not found: {}", ex.getMessage(), ex);
                throw new ConfigPropertiesException("Could not load database driver, this ain't gonna work, bailing out, please do a bug report. Message: " + ex.getMessage());
            } catch (SQLException ex) {
                LOG.error("Without the database we can't run: {}", ex.getMessage(), ex);
                throw new RuntimeException("Without the database we can't run: " + ex.getMessage());
            }
        }
    }

    /**
     * Checks if the current db version is correct and if not triggers a update.
     * @throws ConfigPropertiesException 
     */
    private static synchronized void checkForDBUpdates() throws ConfigPropertiesException {
        int dbVersion = 0;
        int curBuild = Integer.parseInt(SystemConfig.getProperty("system", "server.db.version"));
        try (Connection fileDBConnection = connectionPool.getConnection()) {
            try (Statement statementCategories = fileDBConnection.createStatement();
                 ResultSet rsDBVersion = statementCategories.executeQuery("PRAGMA user_version")) {
                while (rsDBVersion.next()) {
                    dbVersion = rsDBVersion.getInt("user_version");
                }
            } catch (SQLException ex){
                LOG.error("Could not get database version: {}", ex.getMessage());
                throw new ConfigPropertiesException("Could not get database version: " + ex.getMessage());
            }
            if(dbVersion<curBuild){
                LOG.info("Current DB verion {} is less then your current setup which is {}, update needed.", dbVersion, curBuild);
                try {
                    updateDB(dbVersion, curBuild, fileDBConnection);
                } catch (ConfigPropertiesException ex){
                    throw new ConfigPropertiesException("Could not update DB: " + ex.getMessage());
                }
            }
            checkFirstRun();
        } catch (SQLException ex){
            LOG.error("Could not get database version: {}", ex.getMessage());
            throw new ConfigPropertiesException("Could not get database version: " + ex.getMessage());
        }
    }
    
    /**
     * Executes a DB update.
     * @param curVersion
     * @param newVersion
     * @param fileDBConnection
     * @throws ConfigPropertiesException 
     */
    private static synchronized void updateDB(int curVersion, int newVersion,Connection fileDBConnection) throws ConfigPropertiesException{
        LOG.info("Runnning db update from {} to {}. DO NOT INTERRUPT!", curVersion, newVersion);
        createBackup();
        File[] updateSet = new File(SystemConfig.getProperty("system", "database.privatelocation")+"update/").listFiles();
        Arrays.sort(updateSet, (File f1, File f2) -> {
            return Integer.parseInt(f1.getName().substring(0, f1.getName().lastIndexOf("."))) - Integer.parseInt(f2.getName().substring(0, f2.getName().lastIndexOf(".")));
        });
        for(File updateFile : updateSet) {
            if(Integer.parseInt(updateFile.getName().substring(0, updateFile.getName().lastIndexOf(".")))>curVersion){
                LOG.info("Trying to update database to version {}", Integer.parseInt(updateFile.getName().substring(0, updateFile.getName().lastIndexOf("."))));
                try {
                    String s;
                    StringBuilder sb = new StringBuilder();  
                    FileReader fr = new FileReader(updateFile);
                    try (BufferedReader br = new BufferedReader(fr)) {
                        while ((s = br.readLine()) != null) {
                            sb.append(s);
                        }
                    }
                    String[] inst = sb.toString().split(";");
                    Statement st = fileDBConnection.createStatement();
                    for (int i = 0; i < inst.length; i++) {
                        if (!inst[i].trim().equals("")) {
                            st.executeUpdate(inst[i]);
                        }
                    }

                } catch (Exception ex) {
                    LOG.error("Could not execute update: {}", ex.getMessage());
                    LOG.info("Aborted database update. Stuck at version {}", Integer.parseInt(updateFile.getName().substring(0, updateFile.getName().lastIndexOf("."))));
                    break;
                }
            }
        }
        LOG.info("Update succesfull");
        removeBackup();
        LOG.info("Done db updating");
    }
    
    private static void checkFirstRun(){
        try {
            if(SystemConfig.getProperty("system", "server.veryfirstrun").equals("true")){
                LOG.info("Running first run initial user data set");
                try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + baseDbLocation + "system.default.db");
                        PreparedStatement prep = connection.prepareStatement("SELECT clientpass,roleset FROM persons WHERE clientname='admin'");
                        ResultSet result = prep.executeQuery()){
                    if(result.next()){
                        try (PreparedStatement update = connectionPool.getConnection().prepareStatement("UPDATE persons SET clientpass=?, roleset=? WHERE clientname='admin'")){
                            update.setString(1, result.getString("clientpass"));
                            update.setString(2, result.getString("roleset"));
                            update.executeUpdate();
                            SystemConfig.setProperty("system", "server.veryfirstrun", "false");
                            try {
                                SystemConfig.store("system", "initial run setting");
                            } catch (IOException ex) {
                                LOG.error("Could not store initial run setting: ", ex.getMessage());
                            }
                        } catch (SQLException ex) {
                            LOG.error("Could not set initial user data: {}", ex.getMessage(), ex);
                        }
                    }
                } catch (SQLException ex) {
                    LOG.error("Could not get initial user data to set: {}", ex.getMessage(), ex);
                }
                LOG.info("Done running first run initial user data set");
            }
        } catch (ConfigPropertiesException ex) {
            LOG.error("Could not check for initial run setting: {}", ex.getMessage());
        }
    }
    
    /**
     * Creates a backup of the DB.
     * This should only be done when there are no actions busy in the db. Currently only used
     * within the update process.
     * @throws ConfigPropertiesException 
     */
    private static void createBackup() throws ConfigPropertiesException {
        String dbFile = SystemConfig.getProperty("system", "database.privatelocation") + "system.db";
        String dbBackupFile = SystemConfig.getProperty("system", "database.privatelocation") + "system.db.backup";
        LOG.info("Creating backup");
        File db = new File(dbFile);
        File dbBck = new File(dbBackupFile);
        if(db.exists() && db.isFile()){
            try {
                Files.copy(db.toPath(), dbBck.toPath());
            } catch (IOException ex) {
                throw new ConfigPropertiesException("Could not create backup: " + ex.getMessage());
            }
        } else {
            throw new ConfigPropertiesException("Given db file " + dbFile + " is not a file");
        }
    }
    
    private static void removeBackup() throws ConfigPropertiesException {
        LOG.info("removing backup");
        File dbBackupFile = new File(SystemConfig.getProperty("system", "database.privatelocation") + "system.db.backup");
        if(dbBackupFile.exists()){
            dbBackupFile.delete();
        }
    }
    
    /**
     * Sets a connection to a database, returns an open connections if one exists.
     *
     * @param dbType
     * @return The SQL connection.
     * @throws SQLException
     */
    public static synchronized Connection getConnection(String dbType) throws SQLException {
        if(DBLoaded==false) throw new SQLException("Database pooling not setup yet");
        if (connectionPool!=null) {
            return connectionPool.getConnection();
        } else {
            File f = new File(baseDbLocation + dbLocations.get(dbType) + fileTypeDb);
            if(!f.exists()) {
                throw new RuntimeException("Could not open database, but does exist. File a bug report!");
            } else {
                throw new RuntimeException("Database not initialized, file a bug report!");
            }
        }
    }
    
    /**
     * Removes a database (Not the default database).
     * @param dbType 
     */
    public static void removeDB(String dbType){
        String file = baseDbLocation + dbLocations.get(dbType) + fileTypeDb;
        File db = new File(file);
        if(connectionPool!=null){
            connectionPool.shutdown();
        }
        if(db.exists()){
            db.delete();
        }
    }
    
    /**
     * Creates a new clean database with the defaults.
     * @param dbType
     * @throws SQLException 
     */
    private static void createCleanDB(String dbType) throws SQLException {
        String fileDefault = baseDbLocation + dbLocations.get(dbType) + ".default" + fileTypeDb;
        String file = baseDbLocation + dbLocations.get(dbType) + fileTypeDb;
        File from = new File(fileDefault);
        File to   = new File(file);
        try {
            Files.copy( from.toPath(), to.toPath() );
        } catch (IOException ex) {
            throw new SQLException("Could not create clean db: " + ex.getMessage());
        }
    }
    
    /**
     * Closes a database connection.
     * @param dbType
     */
    private static void release(String dbType) {
        if(connectionPool!=null)connectionPool.close();
    }

    /**
     * Closes all databases if open.
     */
    public static synchronized void releaseAll() {
        release(DB_SYSTEM);
    }
}
