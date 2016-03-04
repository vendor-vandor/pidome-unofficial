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
package org.pidome.server.system.config;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import org.pidome.server.logging.LoggerStream;

public final class SystemConfig {

    final static String baseLoc = System.getProperty("user.dir");
    static boolean loaded = false;

    static Logger LOG = LogManager.getLogger(SystemConfig.class);
    
    static Map<String,SystemProperties> propList = new HashMap<>();
    
    private static List<URL> librariesURLs = new ArrayList<>();
    
    /**
     * Initializes the configuration
     * @throws ConfigException 
     */
    public static void initialize() throws ConfigException {
        if(loaded == false){
            try {
                redirectOutputToLog();
                setInterfaceLibs();
                setSharedLibs();
                loaded = true;
                propList.put("system", new SystemProperties("config/","system", new SystemProperties("config/","system.default")));
            } catch (Exception ex) {
                throw new ConfigException("Problem initializing configuration: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Sets a list of paths to be used by plugins and drivers.
     */
    private static void setSharedLibs(){
        Path connectorPath = new File("libs/shared/").toPath();
        try {
            Files.walkFileTree(connectorPath, new urlPaths());
            LOG.debug("Shared libraries file paths: {}", librariesURLs);
        } catch (IOException ex) {
            LOG.error("Could not collect shared libary paths: {}", ex.getMessage());
        }
    }
    
    /**
     * Sets the interfaces libraries.
     */
    private static void setInterfaceLibs(){
        Path connectorPath = new File("libs/interfaces/").toPath();
        try {
            Files.walkFileTree(connectorPath, new urlPaths());
            LOG.debug("Interface libraries file paths: {}", librariesURLs);
        } catch (IOException ex) {
            LOG.error("Could not collect interface libary paths: {}", ex.getMessage());
        }
    }
    
    /**
     * Returns the library paths.
     * @return 
     */
    public static List<URL> getLibraryPaths(){
        return librariesURLs;
    }
    
    /**
     * Class to be used to set the paths with a walkFileTree.
     */
    private static class urlPaths extends SimpleFileVisitor<Path> {
        
        private List<URL> librariesList = new ArrayList<>();
        
        /**
         * Executed when a file is found.
         * It gets the url from a file to be added to the library paths.
         * @param file
         * @param attr
         * @return 
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
            try {
                librariesList.add(file.toUri().toURL());
            } catch (MalformedURLException ex) {
                LOG.error("Could not add {} to libraries path", file.getFileName());
            }
            return CONTINUE;
        }
        
        /**
         * Executed when a file failed.
         * @param file
         * @param exc
         * @return 
         */
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                LOG.error("cycle detected while adding libraries: " + file);
            }
            return CONTINUE;
        }
        
        /**
         * Executed when all done.
         * @param file
         * @param exc
         * @return 
         */
        @Override
        public FileVisitResult postVisitDirectory(Path file, IOException exc) {
            for(int i=0;i<librariesList.size();i++){
                librariesURLs.add(librariesList.get(i));
            }
            return CONTINUE;
        }
        
    }
    
    /**
     * Returns a configuration property
     * @param type
     * @param propName
     * @return
     * @throws ConfigPropertiesException 
     */
    public static String getProperty(String type, String propName) throws ConfigPropertiesException {
        if(loaded == false){
            try {
                initialize();
            } catch (ConfigException ex) {
                throw new ConfigPropertiesException(ex.getMessage());
            }
        }
        if(propList.containsKey(type)){
            String propValue = propList.get(type).getProperty(propName);
            if(propValue==null){
                throw new ConfigPropertiesException("Property '"+propName+"' in '"+type+"' does not exist");
            } else {
                return propValue;
            }
        } else {
            throw new ConfigPropertiesException("Property type '"+type+"' does not exist");
        }
    }
    
    /**
     * Returns a complete configuration type set.
     * @param type
     * @return
     * @throws ConfigPropertiesException 
     */
    public static Set<Map.Entry<Object,Object>>getPropertiesNVP(String type) throws ConfigPropertiesException {
        if(propList.containsKey(type)){
            return propList.get(type).entrySet();
        } else {
            throw new ConfigPropertiesException("Property type '"+type+"' does not exist");
        }
    }
    
    /**
     * Sets a configuration value.
     * @param type
     * @param propName
     * @param propValue 
     */
    public static void setProperty(String type, String propName, String propValue){
        if(propList.containsKey(type)) propList.get(type).setProperty(propName, propValue);
    }
    
    /**
     * Stores a configuration set.
     * @param type
     * @param comment
     * @throws IOException 
     */
    public static void store(String type, String comment) throws IOException {
        if(propList.containsKey(type)) propList.get(type).store(comment);
    }
    
    
    /**
     * Changes the log level used.
     * @param LogLevel 
     */
    public static void setLogLevel(Level LogLevel){
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration logConfig = ctx.getConfiguration();
        LoggerConfig loggerConfig = logConfig.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(LogLevel);
        ctx.updateLoggers();
    }
    
    /**
     * Redirects system.out and system.err to the log file.
     */
    static void redirectOutputToLog(){
        setOutToLog();
        setErrToLog();
    }
    
    /**
     * Redirects system.out to the log file.
     */
    static void setOutToLog(){
        System.out.println("Setting system out to log file appLog.txt. logging continues there unless the log file could not be accessed where this file will then be the fallback");
        System.setOut(new PrintStream(new LoggerStream(LogManager.getLogger("out"), Level.DEBUG, System.out)));
    }

    /**
     * Redirects system.err to the log file.
     */
    static void setErrToLog(){
        System.setErr(new PrintStream(new LoggerStream(LogManager.getLogger("err"), Level.ERROR, System.err)));
    }
    
}