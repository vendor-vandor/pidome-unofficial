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

package org.pidome.server.system.extras;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;

/**
 *
 * @author John
 */
public class SystemExtras {
    
    static Logger LOG = LogManager.getLogger(SystemExtras.class);
    
    private final static DataNotificationLeds notifications = new DataNotificationLeds();
    
    public SystemExtras(){}
    
    public final void initializeExtras(){
        LOG.info("Initializing system extras");
        setLedNotifications();
        LOG.info("Done initializing extras");
        
    }
    
    /**
     * Setting LED notifications.
     * Based on the setting in the system config.
     */
    private static void setLedNotifications(){
        try {
            LOG.info("LED notifications setting: {}", SystemConfig.getProperty("system", "server.datalednotifications"));
            if(SystemConfig.getProperty("system", "server.datalednotifications").equals("true")){
                notifications.setDataNotificationLeds(true);
                notifications.notifyLedSnd();
                notifications.notifyLedRcv();
                notifications.notifyLedSnd();
                notifications.notifyLedRcv();
            } else {
                notifications.setDataNotificationLeds(false);
            }
        } catch (ConfigPropertiesException ex) {
            /// No problem here, non vital feature.
            LOG.info("No visual rcv/snd: {}", ex.getMessage());
        }
    }
    
}
