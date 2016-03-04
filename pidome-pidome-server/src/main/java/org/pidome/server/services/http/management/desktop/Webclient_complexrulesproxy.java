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

package org.pidome.server.services.http.management.desktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.AutomationRules;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_complexrulesproxy extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_complexrulesproxy.class);
    
    @Override
    public String render() throws Exception {
        if(postDataMap.containsKey("name") && postDataMap.containsKey("description") && postDataMap.containsKey("active") && postDataMap.containsKey("rule")){
            if(postDataMap.containsKey("id")){
                if(Integer.parseInt(postDataMap.get("id"))>0){
                    AutomationRules.saveRule(Integer.parseInt(postDataMap.get("id")), postDataMap.get("name"), postDataMap.get("description"), postDataMap.get("active").equals("true"), postDataMap.get("rule"));        
                } else {
                    return "{\"result\":false, \"message\":\"Incorrect save id\"}";
                }
            } else {
                AutomationRules.saveRule(0, postDataMap.get("name"), postDataMap.get("description"), postDataMap.get("active").equals("true"), postDataMap.get("rule"));
            }
        }
        return "{\"result\":true}";
    }
    
}
