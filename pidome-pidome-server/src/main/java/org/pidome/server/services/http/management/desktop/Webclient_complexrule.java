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

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.automations.AutomationRules;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_complexrule extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_complexrule.class);

    @Override
    public final void collect() {
        Map<String,Object> pageData = new HashMap<>();
        if(getDataMap.containsKey("rule")){
            pageData.put("page_title", "Edit Automation rule");
            pageData.put("rule", AutomationRules.getRule(Integer.parseInt(getDataMap.get("rule"))));
        } else {
            pageData.put("page_title", "New Automation rule");
        }
        setData(pageData);
    }
}
