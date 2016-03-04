/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.plugins.plugindata;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author John
 */
public class PluginDataSet {
    
    Map<String,PluginData> pluginDatas = new HashMap<>();
    Properties propertiesSet;
    
    public PluginDataSet(){
        
    }
    
    public PluginData getDataItem(String dataName) throws PluginDataException {
        if(pluginDatas.containsKey(dataName)){
            return pluginDatas.get(dataName);
        } else {
            throw new PluginDataException("Data item '"+dataName+"' does not exist");
        }
    }
    
    public final void setDataItem(String name, PluginData item){
        switch(item.getSettingDataType()){
            case Boolean:
                propertiesSet.put("B:" + name, item.getSetting());
            break;
            case String:
                propertiesSet.put("S:" + name, item.getSetting());
            break;
            case Integer:
                propertiesSet.put("I:" + name, item.getSetting());
            break;
            case Long:
                propertiesSet.put("L:" + name, item.getSetting());
            break;
            case Double:
                propertiesSet.put("D:" + name, item.getSetting());
            break;
        }
        pluginDatas.put(name, item);
    }
    
    public final void removeDataItem(String name){
        if(pluginDatas.containsKey(name)){
            String dataName = "";
            switch(pluginDatas.get(name).getSettingDataType()){
                case Boolean:
                    dataName = "B:" + name;
                break;
                case String:
                    dataName = "S:" + name;
                break;
                case Integer:
                    dataName = "I:" + name;
                break;
                case Long:
                    dataName = "L:" + name;
                break;
                case Double:
                    dataName = "D:" + name;
                break;
            }
            pluginDatas.remove(name);
            if(propertiesSet.contains(dataName)){
                propertiesSet.remove(dataName);
            }
        }
    }
    
    public Map<String,PluginData> getDataCollection(){
        return pluginDatas;
    }
    
    public void composeByProperties(Properties properties){
        pluginDatas.clear();
        propertiesSet = properties;
        for (Entry<Object, Object> entry : properties.entrySet()) {
            try {
                String[] entryTypeSet = ((String)entry.getKey()).split(":");
                PluginData pluginData = new PluginData();
                switch(entryTypeSet[0]){
                    case "B":
                        pluginData.setSetting(Boolean.valueOf((String)entry.getValue()));
                    break;
                    case "S":
                        pluginData.setSetting((String)entry.getValue());
                    break;
                    case "I":
                        pluginData.setSetting(Integer.valueOf((String)entry.getValue()));
                    break;
                    case "L":
                        pluginData.setSetting(Long.valueOf((String)entry.getValue()));
                    break;
                    case "D":
                        pluginData.setSetting(Double.valueOf((String)entry.getValue()));
                    break;
                }
                pluginDatas.put(entryTypeSet[1], pluginData);
            } catch (PluginDataException ex) {
                Logger.getLogger(PluginDataSet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public Properties getAsProperties(){
        return propertiesSet;
    }
    
}
