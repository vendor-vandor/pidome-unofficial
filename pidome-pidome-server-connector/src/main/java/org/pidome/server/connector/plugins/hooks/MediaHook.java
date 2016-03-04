/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static org.pidome.server.connector.plugins.hooks.DeviceHook.LOG;
import org.pidome.server.connector.plugins.media.MediaEvent;

/**
 *
 * @author John
 */
public class MediaHook {
    
    private final static Map<MediaHookListener,Map<Integer,ArrayList<String>>> hookList = new ConcurrentHashMap<>();
    
    /**
     * Handles device data and delivers them to plugins.
     * @param event
     */
    public static void handleMediaEvent(final MediaEvent event){
        LOG.debug("Handling for media {}", event.getSource().getPluginName());
        Runnable runValue = () -> {
            for(MediaHookListener plugin:hookList.keySet()){
                if(hookList.get(plugin).containsKey(0)){
                    plugin.handleMediaEvent(event);
                } else if(hookList.get(plugin).containsKey(event.getSource().getPluginId())){
                    plugin.handleMediaEvent(event);
                }
            }
        };
        runValue.run();
    }
    
    /**
     * So plugins can listen to all media event types.
     * @param plugin
     */
    public synchronized static void addAllMedia(MediaHookListener plugin){
        LOG.debug("Adding all media to {}", plugin.getClass().getName());
        if(hookList.containsKey(plugin)){
            if(!hookList.get(plugin).containsKey(0)){
                hookList.get(plugin).put(0, new ArrayList<>());
            }
        } else {
            Map<Integer,ArrayList<String>> primaryHookSet = new HashMap<>();
            primaryHookSet.put(0, new ArrayList<>());
            hookList.put(plugin, primaryHookSet);
        }
        LOG.debug("Full hook list after: {}", hookList);
    }
    
}
