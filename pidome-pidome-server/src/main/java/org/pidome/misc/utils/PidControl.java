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

package org.pidome.misc.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.Platforms;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 *
 * @author John
 */
public class PidControl {
    
    static Logger LOG = LogManager.getLogger(PidControl.class);
    private static String currentPid = "";
    private static String pidLocation= "";
        
    public static void createPid() throws RuntimeException {
        switch(Platforms.isOs()){
            case Platforms.OS_WINDOWS:
                /// No pid control yet for windows
            break;
            default:
                try {
                    pidLocation = SystemConfig.getProperty("system", "server.pidlocation");
                    LOG.debug("PID location: {}", pidLocation);
                } catch (ConfigPropertiesException ex) {
                    throw new RuntimeException("Could not determine pid location, start aborted");
                }
                setPid();
                File pidFile = new File(pidLocation);
                try {
                    FileOutputStream stream = new FileOutputStream(pidFile, false);
                    stream.write(currentPid.getBytes());
                } catch (FileNotFoundException ex) {
                    LOG.error("Could not create pid file, aborting");
                    throw new RuntimeException("Could not create pid file, aborting");
                } catch (IOException ex) {
                    LOG.error("Could not write to pid file, aborting");
                    throw new RuntimeException("Could not write to pid file, aborting");
                }
                LOG.debug("Running under PID: {}",currentPid);
            break;
        }
    }
    
    private static void setPid(){
        File proc_self = new File("/proc/self");
        if(proc_self.exists()) {
            try {
                currentPid = proc_self.getCanonicalFile().getName();
            } catch(Exception e) {
                throw new RuntimeException("Could not determine current pid, will not continue");
            }
        } else {
            throw new RuntimeException("Could not determine current pid, no proc file present, will not continue");
        }
    }
    
    
    
    public static void shutDown(){
        File pidFile = new File(pidLocation);
        if(pidFile.exists()){
            pidFile.delete();
        }
    }
    
}
