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

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentation;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionRequest;
import org.pidome.server.connector.plugins.PluginBase;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.connector.plugins.freeform.FreeformPlugin;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.hardware.DeviceServiceException;
import org.pidome.server.services.plugins.DevicePluginService;
import org.pidome.server.services.plugins.PluginServiceException;

/**
 *
 * @author John
 */
public class DevicePluginServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements DevicePluginServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer,Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getPlugins", null);
                put("deletePlugin", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("pluginFunction", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("params", new HashMap<String,Object>());}});
                    }
                });
                put("getPresentation", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getPluginDevices", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    /**
     * @inheritDoc
     */
    @Override
    public final Object getPlugins() throws PluginException {
        List<Map<String,Object>> pluginList = new ArrayList<>();
        for(Map<String,Object> plugin:DevicePluginService.getInstance().getPlugins().values()){
            LOG.debug("Got plugin list: {}", plugin);
            plugin.remove("pluginObject");
            pluginList.add(plugin);
        }
        return pluginList;
    }


    /**
     * Deletes a device plugin.
     * @param pluginId
     * @return
     * @throws PluginServiceException 
     */
    @Override
    public final Object deletePlugin(Long pluginId) throws PluginServiceException {
        return DevicePluginService.getInstance().deletePlugin(pluginId.intValue());
    }
    
    @Override
    public final Object getPresentation(Long pluginId) throws PluginServiceException {
        for(Entry<Integer,Map<String,Object>> pluginSet:DevicePluginService.getInstance().getPlugins().entrySet()){
            if(pluginSet.getKey()==pluginId.intValue()){
                PluginBase plugin = (PluginBase)(pluginSet.getValue()).get("pluginObject");
                if(plugin instanceof FreeformPlugin){
                    try {
                        ((FreeformPlugin)plugin).prepareWebPresentation();
                    } catch (UnsupportedOperationException ex){
                        //// plugin does not need to prepare.
                    }
                }
                Map<String, Object> details = new HashMap<>();
                List<Object> presentList = new ArrayList<>();
                if (plugin.hasPresentation()) {
                    for (WebPresentationGroup group : plugin.getWebPresentationGroups()) {
                        Map<String, Object> presentationGroup = new HashMap<>();
                        presentationGroup.put("title", group.getTitle());
                        presentationGroup.put("description", group.getDescription());
                        List<Map<String, Object>> collection = new ArrayList<>();
                        for (WebPresentation item : group.getCollection()) {
                            Map<String, Object> presentationItem = new HashMap<>();
                            presentationItem.put("label", item.getLabel());
                            presentationItem.put("type", item.getType().toString());
                            presentationItem.put("content", item.getPresentationValue());
                            collection.add(presentationItem);
                        }
                        presentationGroup.put("content", collection);
                        presentList.add(presentationGroup);
                    }
                }
                details.put("presentation", presentList);
                return details;
            }
        }  
        throw new PluginServiceException("No view available");
    }
    
    /**
     * Returns the devices bound to this plugin.
     * @param pluginId
     * @return
     * @throws PluginServiceException 
     */
    @Override
    public final Object getPluginDevices(Long pluginId) throws PluginServiceException {
        for(Entry<Integer,Map<String,Object>> pluginSet:DevicePluginService.getInstance().getPlugins().entrySet()){
            if(pluginSet.getKey()==pluginId.intValue()){
                DevicePlugin plugin = (DevicePlugin)(pluginSet.getValue()).get("pluginObject");
                LOG.debug("Getting devices for: {}", plugin.getHardwareDevice().getSoftwareDriver().getPackageName());
                return DeviceService.getInstalledDevicesInfoByDriverName(plugin.getHardwareDevice().getSoftwareDriver().getPackageName());
            }
        }
        throw new PluginServiceException("Devices not available for this plugin, is the plugin running?");
    }
    
    /**
     * Executes a device action in a software peripheral.
     * @param peripheralPort
     * @param params
     * @return
     * @throws DeviceServiceException 
     */
    @Override
    public Object pluginFunction(Long pluginId, Map<String,Object> params) throws DeviceServiceException{
        for(Entry<Integer,Map<String,Object>> pluginSet:DevicePluginService.getInstance().getPlugins().entrySet()){
            if(pluginSet.getKey()==pluginId.intValue()){
                PluginBase plugin = (PluginBase)(pluginSet.getValue()).get("pluginObject");
                try {
                    if(params.get("function_id").equals("customFunction") && plugin instanceof WebPresentCustomFunctionInterface){
                        WebPresentCustomFunctionRequest request = new WebPresentCustomFunctionRequest();
                        if(params.containsKey("identifier")){
                            request.setIdentifier((String)params.get("identifier"));
                            params.remove("identifier");
                        }
                        request.setResultParams(params);
                        ((WebPresentCustomFunctionInterface)plugin).handleCustomFunctionRequest(request);
                    }
                } catch (PeripheralDriverDeviceMutationException ex) {
                    throw new DeviceServiceException(ex.getMessage());
                } catch (NullPointerException ex){
                    throw new DeviceServiceException("No function id supplied or other error, can not handle request: " + ex.getMessage());
                } catch (Exception ex) {
                    throw new DeviceServiceException("Could not complete request: " + ex.getMessage());
                }
            }
        }
        return true;
    }
    
}