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

package org.pidome.server.services.http.management.desktop;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.pidome.misc.utils.TimeUtils;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public final class Webclient_settings  extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_settings.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Server settings");
        
        /// set log level info
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("X"); 
        pageData.put("log_level", loggerConfig.getLevel());
        try {
            pageData.put("client_auth", SystemConfig.getProperty("system", "displayclients.auth"));
        } catch (ConfigPropertiesException ex) {
            pageData.put("client_auth", "unknown");
        }
        /// Set timezone data
        pageData.put("timezones", TimeZone.getAvailableIDs());
        pageData.put("timezone", TimeUtils.getCurrentTimeZone());
        pageData.put("latitude", TimeUtils.getCurrentLatitude());
        pageData.put("longitude", TimeUtils.getCurrentLongitude());
        
        pageData.put("locales", Locale.getAvailableLocales());
        pageData.put("locale", Locale.getDefault());
        
        setData(pageData);
    }
    
    @Override
    public String render() throws Exception {
        if(getDataMap.containsKey("saveTimezone")){
            TimeUtils.setNewLocalizedTimeZoneData(getDataMap.get("timezone"), getDataMap.get("latitude"), getDataMap.get("longitude"));
            TriggerService.updateVariableTimedTriggers();
            LOG.info("Time zone modified to: {} - {}, {}", getDataMap.get("timezone"), getDataMap.get("latitude"), getDataMap.get("longitude"));
            return "{ \"result\" : [ { \"exec\":true } ] }";
        } else if (getDataMap.containsKey("setShutdown") && getDataMap.get("setShutdown").equals("true")){
            shutdown();
            System.gc();	// so that HPROF reports only live objects.
            System.exit(0);
            return "{ \"result\" : [ { \"exec\":true } ] }";
        } else {
            return super.render();
        }
    }
    
    private static void shutdown(){
        Runtime.getRuntime().exit(0);
    }
    
}
