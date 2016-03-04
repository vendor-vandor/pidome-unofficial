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

package org.pidome.server.services.http.management.xmlapi;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public class Webclient_plugin  extends Webservice_renderer {
    
    public Webclient_plugin(){
        super();
    }
    
    @Override
    public void collect() {
        Map<String, Map<String, Object>> xmlStaticData = new HashMap<>();
        /// first some static version data
        if (postDataMap.containsKey("id") && postDataMap.get("id").length() > 0) {
            xmlStaticData.put("mediaplugins", getMediaPlugin(Integer.valueOf(postDataMap.get("id"))));
        } else {
            xmlStaticData.put("mediaplugins", new HashMap<String, Object>());
        }
        setData(xmlStaticData);
    }
    
    
    Map getMediaPlugin(int pluginId){
        Map<String,Map<String,Object>> returnMap = new HashMap<>();
        Map<Integer,Map<String,Object>> plugins = MediaPluginService.getInstance().getPlugins();
        for(int id:plugins.keySet()){
            if(pluginId == id){
                returnMap.put(String.valueOf(id), plugins.get(id));
                Map<Integer,Map<MediaPlugin.PlayListItem,Object>> playlist = ((Media)plugins.get(id).get("pluginObject")).getPlayList();
                Map<String,Map<String,Object>> newlist = new TreeMap<>();
                for(int key:playlist.keySet()){
                    Map<String,Object> innerList = new HashMap<>();
                    for(MediaPlugin.PlayListItem type:playlist.get(key).keySet()){
                        innerList.put(type.toString(), playlist.get(key).get(type));
                    }
                    newlist.put(String.valueOf(key), innerList);
                }
                plugins.get(id).put("mediaPlaylist", newlist);
            }
        }
        return returnMap;
    }
    
}
