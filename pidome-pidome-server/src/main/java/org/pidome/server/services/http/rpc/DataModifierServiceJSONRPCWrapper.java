/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.interfaces.web.configuration.WebOption;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.datamodifiers.DataModifierPlugin;
import org.pidome.server.services.plugins.DataModifierPluginService;
import org.pidome.server.services.plugins.PluginServiceException;
import static org.pidome.server.services.http.rpc.AbstractRPCMethodExecutor.LOG;

/**
 *
 * @author John
 */
public class DataModifierServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements DataModifierServiceJSONRPCWrapperInterface {

    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getPlugins", null);
                put("getInstalledPlugins", null);
                put("getInstalledPluginOptions", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("installid", 0L);}});
                    }
                });
                put("getPlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("savePlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("installid", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("options", new HashMap<String, String>());}});
                    }
                });
                put("updatePlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("description", "");}});
                        put(3,new HashMap<String,Object>(){{put("options", new HashMap<String, String>());}});
                    }
                });
            }
        };
        return mapping;
    }
    
    @Override
    public Object getPlugins() {
        List<Map<String,Object>> pluginList = new ArrayList<>();
        for(DataModifierPlugin plugin:DataModifierPluginService.getInstance().getPlugins()){
            Map<String,Object> item = new HashMap<>();
            item.put("id", plugin.getPluginId());
            item.put("basename", plugin.getBaseName());
            item.put("description", plugin.getPluginDescription());
            item.put("direction", plugin.getDirection().toString());
            item.put("name", plugin.getPluginName());
            item.put("active", plugin.getRunning());
            item.put("currentvalue", plugin.getCurrentValue());
            item.put("attached", plugin.getAttachedControlsAmount());
            pluginList.add(item);
        }
        return pluginList;
    }

    @Override
    public Object getInstalledPlugins() {
        List<Map<String,Object>> pluginList = new ArrayList<>();
        Map<Integer, Map<String, Object>> origData = DataModifierPluginService.getInstance().getInstalledPlugins();
        for (int key : origData.keySet()) {
            origData.get(key).put("id", key);
            pluginList.add(origData.get(key));
        }
        return pluginList;
    }

    @Override
    public Object getInstalledPluginOptions(Number installedId) throws PluginException {
        Map<String,Object> pluginBase = new HashMap<>();
        DataModifierPlugin plugin = DataModifierPluginService.getInstance().getBareboneDevicePluginInstance(installedId.intValue());
        pluginBase.put("basename", plugin.getBaseName());
        pluginBase.put("name", plugin.getPluginName());
        pluginBase.put("description", plugin.getPluginDescription());
        pluginBase.put("configuration", new ArrayList<>());
        try {
            List<WebConfigurationOptionSet> confList = plugin.getConfiguration().getOptions();
            for(WebConfigurationOptionSet optionSet:confList){
                Map<String,Object> parentConf = new HashMap<>();
                parentConf.put("title", optionSet.getConfigurationSetTitle());
                parentConf.put("description", optionSet.getConfigurationSetDescription());
                parentConf.put("optionslist", new ArrayList<>());
                for(WebOption option:optionSet.getOptions()){
                    Map<String,Object> singleOption = new HashMap<>();
                    singleOption.put("id", option.getId());
                    singleOption.put("name", option.getOptionName());
                    singleOption.put("description", option.getOptionDescription());
                    singleOption.put("defaultvalue", option.getDefaultValue());
                    singleOption.put("value", option.getValue());
                    singleOption.put("optionvalues", option.getSet());
                    singleOption.put("optionfieldtype", option.getFieldType().toString());
                    ((List)parentConf.get("optionslist")).add(singleOption);
                }
                ((List)pluginBase.get("configuration")).add(parentConf);
            }
        } catch (WebConfigurationException ex) {
            LOG.warn("No configuration available for installed for {}: {}", plugin.getBaseName(), ex.getMessage(), ex);
        }
        return pluginBase;
    }


    @Override
    public Object getPlugin(Number pluginId) throws PluginException {
        Map<String,Object> pluginBase = new HashMap<>();
        DataModifierPlugin plugin = DataModifierPluginService.getInstance().getPlugin(pluginId.intValue());
        pluginBase.put("basename", plugin.getBaseName());
        pluginBase.put("name", plugin.getPluginName());
        pluginBase.put("description", plugin.getPluginDescription());
        pluginBase.put("configuration", new ArrayList<>());
        pluginBase.put("id", plugin.getPluginId());
        try {
            List<WebConfigurationOptionSet> confList = plugin.getConfiguration().getOptions();
            for(WebConfigurationOptionSet optionSet:confList){
                Map<String,Object> parentConf = new HashMap<>();
                parentConf.put("title", optionSet.getConfigurationSetTitle());
                parentConf.put("description", optionSet.getConfigurationSetDescription());
                parentConf.put("optionslist", new ArrayList<>());
                for(WebOption option:optionSet.getOptions()){
                    Map<String,Object> singleOption = new HashMap<>();
                    singleOption.put("id", option.getId());
                    singleOption.put("name", option.getOptionName());
                    singleOption.put("description", option.getOptionDescription());
                    singleOption.put("defaultvalue", option.getDefaultValue());
                    singleOption.put("value", option.getValue());
                    singleOption.put("optionvalues", option.getSet());
                    singleOption.put("optionfieldtype", option.getFieldType().toString());
                    ((List)parentConf.get("optionslist")).add(singleOption);
                }
                ((List)pluginBase.get("configuration")).add(parentConf);
            }
        } catch (WebConfigurationException ex) {
            LOG.warn("No configuration available for installed for {}: {}", plugin.getBaseName(), ex.getMessage(), ex);
        }
        return pluginBase;
    }
    
    @Override
    public boolean savePlugin(Number installedId, String name, String description, Map<String, String> optionsSet) throws PluginServiceException {
        return DataModifierPluginService.getInstance().savePlugin(name, description, 0, false, optionsSet, installedId.intValue());
    }

    @Override
    public boolean updatePlugin(Number pluginId, String name, String description, Map<String, String> optionsSet) throws PluginServiceException {
        return DataModifierPluginService.getInstance().updatePlugin(pluginId.intValue(), name, description, 0, false, optionsSet);
    }

    @Override
    public boolean deletePlugin(Number pluginId) throws PluginServiceException {
        return DataModifierPluginService.getInstance().deletePlugin(pluginId.intValue());
    }
    
}