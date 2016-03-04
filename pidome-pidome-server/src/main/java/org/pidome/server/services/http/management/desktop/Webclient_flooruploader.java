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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_flooruploader extends Webservice_renderer {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(Webclient_flooruploader.class);
    
    @Override
    public String render() throws Exception {
        File path = new File("resources/floorplan/");
        path.mkdirs();
        String content = "";
        for(Map.Entry<String,byte[]> file:fileDataMap.entrySet()){
            try (DataOutputStream fos = new DataOutputStream(new FileOutputStream("resources/floorplan/" + file.getKey()))) {
                fos.write(file.getValue(), 0, file.getValue().length);
                fos.flush();
                content = "{\"success\": true, \"filename\":\"/floorplan/" + file.getKey()+"\"}";
            } catch (Exception ex) {
                LOG.error("Could not write file: {}. Reason: {}", file.getKey(), ex.getMessage());
                content = "{\"success\": false, \"message\":\"Could not write file: "+file.getKey()+". Reason: " + ex.getMessage()+"\"}";
            }
        }
        return getCustomRender(content);
    }
    
}
