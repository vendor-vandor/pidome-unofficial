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

package org.pidome.server.system.packages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 *
 * @author John Sirach
 */
public class PackageConfigLoader implements AutoCloseable {

    static Logger LOG = LogManager.getLogger(PackageConfigLoader.class);
    
    public final static String PACKAGES = "PACKAGES";
    public final static String PACKAGE  = "PACKAGE";
    
    protected static String configFileContent;

    String file;
    
    /**
     * Loads the specified configuration file.
     * @param config
     * @throws org.pidome.server.system.config.ConfigPropertiesException 
     */
    public PackageConfigLoader(String config) throws ConfigPropertiesException {
        this(config, null);
    }

    /**
     * Loads a package config file.
     * @param config
     * @param configFile
     * @throws ConfigPropertiesException 
     */
    public PackageConfigLoader(String config, String configFile) throws ConfigPropertiesException {
        switch(config){
            case PACKAGES:
                file = SystemConfig.getProperty("system", "packages.config");
            break;
            case PACKAGE:
                if(configFile!=null){
                    file = SystemConfig.getProperty("system", "packages.location") + configFile + ".xml";
                } else {
                    throw new ConfigPropertiesException("Need package file to load");
                }
            break;
            default:
                throw new ConfigPropertiesException("Unsupported config file");
        }
        try (FileInputStream stream = new FileInputStream(new File(file))) {
            LOG.debug("Loading: " + file);
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            configFileContent = Charset.forName("UTF-8").decode(bb).toString();
        } catch (FileNotFoundException e) {
            LOG.error(file + " does not exist.");
            throw new ConfigPropertiesException(file + " does not exist.");
        } catch (IOException e) {
            LOG.error("Problem reading " + file + " file a bug report.");
            throw new ConfigPropertiesException("Problem reading " + file + " file a bug report.");
        }
    }
    
    /**
     * Returns the contents of a package file.
     * @return 
     * @throws org.pidome.server.system.config.ConfigPropertiesException 
     */
    public String getContents() throws ConfigPropertiesException {
        if(configFileContent==null){
            throw new ConfigPropertiesException("There is no content");
        } else {
            return configFileContent;
        }
    }
    
    /**
     * Autoclosable.
     */
    @Override
    public void close() {
        /// Already closed
    }
    
}
