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
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.utilitydata.UtilityData;
import org.pidome.server.connector.plugins.utilitydata.UtilityDataInterface;
import org.pidome.server.services.plugins.UtilityPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_utilitymeasurements extends Webservice_renderer {
    static Logger LOG = LogManager.getLogger(Webclient_utilitymeasurements.class);
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "View utility usages");
        try {
            UtilityData plugin = UtilityPluginService.getInstance().getPlugin(0);
            Map<UtilityDataInterface.Type, Map<String,Map<String,Object>>> currentUsages = plugin.getCurrentTotalUsages();
            pageData.put("utilityMeasurementPluginId", plugin.getPluginId());
            Map<String, Map<String,Map<String,Object>>> newMap = new HashMap<>();
            for (UtilityDataInterface.Type type:currentUsages.keySet()){
                newMap.put(type.toString(), currentUsages.get(type));
            }
            pageData.put("CurrentUsageValues",newMap);
        } catch (PluginException ex) {
            /// Not running
        }
        setData(pageData);
    }
}
