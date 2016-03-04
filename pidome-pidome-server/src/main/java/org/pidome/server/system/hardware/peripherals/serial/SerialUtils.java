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

package org.pidome.server.system.hardware.peripherals.serial;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 *
 * @author John Sirach
 */
public class SerialUtils {
    
    static Logger LOG = LogManager.getLogger(SerialUtils.class);
    
    /**
     * Librxtx sux sometimes.
     * When we have ttyACM[0-9]+ ports they are not recognized, now every time when there is a new device we create a new known ports environment setting.
     * We automatic include ttyAMA0 because of the serial GPIO usage
     * @return List<String> Linux port names
     */
    public static List<String> discoverPorts(){
        List<String> ports = new ArrayList();
        try { 
            String path = SystemConfig.getProperty("system", "server.linuxdevlocation"); 
            String fileName;
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            String newPortsList = "";
            for (int i = 0; i < listOfFiles.length; i++) {
                fileName = listOfFiles[i].getName();
                if (fileName.contains("ttyACM") || fileName.contains("ttyUSB") || fileName.contains("ttyAMA0")) {
                    newPortsList += path + fileName + ":";
                    ports.add(path + fileName);
                }
            }
            if(newPortsList.length()>0){
                System.setProperty("gnu.io.rxtx.SerialPorts", newPortsList.substring(0, newPortsList.length()-1));
                LOG.debug("Setting new known ports list: {}", newPortsList.substring(0, newPortsList.length()-1));
            }
        } catch (ConfigPropertiesException ex) {
            LOG.error("Can not retreive comports location settings from system.xml config");
        }
        return ports;
    }
}
