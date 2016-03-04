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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.server.system.location.BaseLocations;
import org.pidome.server.system.location.LocationServiceException;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public final class Webclient_settingslocations extends Webservice_renderer {
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Locations");
        try {
            pageData.put("locations", BaseLocations.getLocations());
        } catch (LocationServiceException ex) {
            Logger.getLogger(Webclient_settingslocations.class.getName()).log(Level.SEVERE, null, ex);
            pageData.put("locations", new ArrayList());
        }
        setData(pageData);
    }
}
