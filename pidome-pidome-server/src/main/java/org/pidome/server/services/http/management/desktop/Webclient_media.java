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
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public class Webclient_media extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_media.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Media");
        Map<Integer,Map<String,Object>> origData = MediaPluginService.getInstance().getActivePlugins();
        Map<String,Map<String,Object>> mediaData = new HashMap<>();
        for(int key:origData.keySet()){
            mediaData.put(String.valueOf(key), origData.get(key));
        }
        pageData.put("devicelisting", mediaData);
        setData(pageData);
    }

}
