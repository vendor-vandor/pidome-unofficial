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

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.plugins.media.Media;
import org.pidome.server.connector.plugins.media.MediaPlugin;
import org.pidome.server.connector.plugins.media.MediaPlugin.ItemType;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.http.Webservice_XSLTransformer;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public final class Webclient_mediarenderer extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_mediarenderer.class);

    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        if (getDataMap.containsKey("mediaid") && getDataMap.get("mediaid").length() > 0) {
            Webservice_XSLTransformer mediaRender = new Webservice_XSLTransformer();
            mediaRender.setRenderFile("media");
            int mediaId = Integer.parseInt(getDataMap.get("mediaid"));
            pageData.put("mediaid", getDataMap.get("mediaid"));
            Media plugin = MediaPluginService.getInstance().getPlugin(mediaId);
            if(plugin!=null){
                mediaRender.setRenderParameter("playerid", mediaId);
                mediaRender.setRenderParameter("name", plugin.getPluginName());
                mediaRender.setRenderParameter("location", plugin.getPluginLocation());
                
                pageData.put("playername", plugin.getPluginName());
                pageData.put("playerlocation", plugin.getPluginLocation());
                
                mediaRender.setRenderParameter("currentItemType", "");
                mediaRender.setRenderParameter("currentALBUM", "");
                mediaRender.setRenderParameter("currentALBUM_ARTIST", "");
                mediaRender.setRenderParameter("currentTITLE", "No item playing");
                mediaRender.setRenderParameter("currentTITLE_ARTIST", "");
                mediaRender.setRenderParameter("currentDURATION", "");
                
                Map<String,Object> currentFile = plugin.getNowPlayingData();
                if(currentFile!=null && !currentFile.isEmpty()){
                    mediaRender.setRenderParameter("currentItemType", ((ItemType)currentFile.get("ItemType")).toString());
                    mediaRender.setRenderParameter("currentDURATION", currentFile.get(MediaPlugin.AudioDetails.DURATION.toString()));
                    mediaRender.setRenderParameter("currentTITLE", currentFile.get(MediaPlugin.AudioDetails.TITLE.toString()));
                    switch((ItemType)currentFile.get("ItemType")){
                        case AUDIO:
                            mediaRender.setRenderParameter("currentALBUM", currentFile.get(MediaPlugin.AudioDetails.ALBUM.toString()));
                            mediaRender.setRenderParameter("currentALBUM_ARTIST", currentFile.get(MediaPlugin.AudioDetails.ALBUM_ARTIST.toString()));
                            mediaRender.setRenderParameter("currentTITLE_ARTIST", currentFile.get(MediaPlugin.AudioDetails.TITLE_ARTIST.toString()));
                        break;
                    }
                }
                Map<Integer,Map<MediaPlugin.PlayListItem,Object>> playlist = plugin.getPlayList();
                ValueComparator vc = new ValueComparator();
                Map<String,Map<String,Object>> newlist = new TreeMap<>(vc);
                for(int key:playlist.keySet()){
                    Map<String,Object> innerList = new HashMap<>();
                    for(MediaPlugin.PlayListItem type:playlist.get(key).keySet()){
                        innerList.put(type.toString(), playlist.get(key).get(type));
                    }
                    newlist.put(String.valueOf(key), innerList);
                }
                pageData.put("playlist", newlist);
                try {
                    pageData.put("pluginControl", mediaRender.render((String)plugin.getXml()).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
                } catch (IOException ex) {
                    LOG.error("Could not create device control");
                }
            } 
        }
        setData(pageData);
    }

    static class ValueComparator implements Comparator<String> {

        @Override
        public int compare(String a, String b) {
            return Integer.parseInt(a)-Integer.parseInt(b);
        }
    }
    
}
