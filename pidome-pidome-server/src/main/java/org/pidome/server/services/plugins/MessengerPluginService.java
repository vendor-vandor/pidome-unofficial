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

package org.pidome.server.services.plugins;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.messengers.MessengerException;
import org.pidome.server.connector.plugins.messengers.sms.SMSMessengerBase;
import org.pidome.server.system.plugins.PluginsDB;

/**
 *
 * @author John
 */
public class MessengerPluginService extends PluginService {

    static MessengerPluginService me;
    
    int definedPluginId = 4;
    int definedPluginTypeId = 3;
    
    /**
     * Constructor.
     */
    protected MessengerPluginService(){
        if(me!=null){
            me = this;
        }
    }
    
    /**
     * Returns instance.
     * @return 
     */
    public static MessengerPluginService getInstance(){
        if(me==null){
            me = new MessengerPluginService();
        }
        return me;
    }
    
    @Override
    public int getInstalledId() {
        return definedPluginId;
    }
    
    @Override
    public int getPluginTypeId() {
        return definedPluginTypeId;
    }

    /**
     * Returns the SMS plugin.
     * @throws org.pidome.server.connector.plugins.PluginException
     */
    @Override
    public final SMSMessengerBase getPlugin(int pluginId) throws PluginException {
        if(!pluginsList.isEmpty()){
            return (SMSMessengerBase)pluginsList.values().iterator().next();
        } else {
            throw new PluginException("Plugin not loaded");
        }
    }
    
    /**
     * Returns only the active media plugins known.
     * When an plugin is active and loaded it will be included in an extra field named pluginObject
     * @return 
     */
    public Map<Integer,Map<String,Object>> getActivePlugins(){
        Map<Integer,Map<String,Object>> pluginCollection = PluginsDB.getPlugins(getPluginTypeId());
        for(int key: pluginCollection.keySet()){
            if(pluginsList.containsKey(key) && pluginsList.get(key).getRunning()){
                pluginCollection.get(key).put("active", pluginsList.get(key).getRunning());
                pluginCollection.get(key).put("pluginObject", (SMSMessengerBase)pluginsList.get(key));
            } else {
                pluginCollection.remove(key);
            }
        }
        return pluginCollection;
    }
    
    @Override
    void startPluginHandlers(int pluginId) {
        //// not used
    }

    @Override
    void stopHandlers(int pluginId) {
        //// not used
    }

    @Override
    public String getServiceName() {
        return "Messenger plugin service";
    }

    /**
     * Sends the sms message
     * @param message
     * @throws MessengerException 
     */
    public final void sendSmsMessage(String message) throws MessengerException {
        if(!pluginsList.isEmpty()){
            try {
                ((SMSMessengerBase)pluginsList.values().iterator().next()).sendSmsMessage(message);
            } catch (MessengerException ex) {
                Logger.getLogger(MessengerPluginService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Sends the pushbullet message
     * @param message
     * @throws MessengerException 
     */
    public final void sendPushbulletMessage(String message) throws MessengerException {
        if(!pluginsList.isEmpty()){
            try {
                ((SMSMessengerBase)pluginsList.values().iterator().next()).sendPushBulletMessage(message);
            } catch (MessengerException ex) {
                Logger.getLogger(MessengerPluginService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}