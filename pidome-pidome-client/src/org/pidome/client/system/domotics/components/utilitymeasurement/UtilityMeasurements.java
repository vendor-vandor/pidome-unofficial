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

package org.pidome.client.system.domotics.components.utilitymeasurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.DomResourceException;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 *
 * @author John
 */
public class UtilityMeasurements implements DomComponent,ClientDataConnectionListener {

    static Logger LOG = LogManager.getLogger(UtilityMeasurements.class);
    
    DomComponents domResource;
    static Map<Integer, UtilityMeasurement> pluginsList = new HashMap<>();
    
    /**
     * Constructor.
     * @param domResource
     * @param locationData 
     */
    public UtilityMeasurements(DomComponents domResource, ArrayList<Map<String,Object>> locationData){
        this.domResource = domResource;
        locationData.stream().forEach((location) -> {
            createInstance(location);
        });
        ClientData.addClientDataConnectionListener(this);
    }
    
    /**
     * Creates a single instance.
     * @param info 
     */
    final void createInstance(Map<String,Object> info){
        int pluginId = ((Long)info.get("id")).intValue();
        pluginsList.put(pluginId, new UtilityMeasurement(pluginId, (String)info.get("name")));
        Map<String, Object> params = new HashMap<>();
        params.put("id", pluginId);
        params.put("filter", null);
        try {
            Map<String,Object> initialData = domResource.getJSONData("UtilityMeasurementService.getCurrentTotalUsage", params).getResult();
            if(initialData.containsKey("data")){
                Map<String,Map<String,Map<String,Object>>> dataValues = (Map<String,Map<String,Map<String,Object>>>)initialData.get("data");
                for(Entry<String,Map<String,Map<String,Object>>> entryset: dataValues.entrySet()){
                    LOG.debug("Got measurments data for {}: {}",entryset.getKey(), entryset.getValue());
                    switch(entryset.getKey()){
                        case "POWER":
                            pluginsList.get(pluginId).updatePower((double)entryset.getValue().get("unitcurrent").get("value"), 
                                                                  (double)entryset.getValue().get("current").get("value"), 
                                                                  (double)entryset.getValue().get("today").get("value"));
                            pluginsList.get(pluginId).setMappedName("WATT", (String)entryset.getValue().get("unitcurrent").get("name"));
                            pluginsList.get(pluginId).setMappedName("KWH", (String)entryset.getValue().get("current").get("name"));
                            pluginsList.get(pluginId).setThreshold("WATT", Math.round((1000*(double)entryset.getValue().get("today").get("threshold"))/24));
                            pluginsList.get(pluginId).setThreshold("KWH", (double)entryset.getValue().get("today").get("threshold"));
                        break;
                        case "WATER":
                            pluginsList.get(pluginId).updateWater((double)entryset.getValue().get("current").get("value"), 
                                                                  (double)entryset.getValue().get("today").get("value"));
                            pluginsList.get(pluginId).setMappedName("WATER", (String)entryset.getValue().get("current").get("name"));
                            pluginsList.get(pluginId).setThreshold("WATER", (double)entryset.getValue().get("today").get("threshold"));
                        break;
                        case "GAS":
                            pluginsList.get(pluginId).updateGas((double)entryset.getValue().get("current").get("value"), 
                                                                (double)entryset.getValue().get("today").get("value"));
                            pluginsList.get(pluginId).setMappedName("GAS", (String)entryset.getValue().get("current").get("name"));
                            pluginsList.get(pluginId).setThreshold("GAS", (double)entryset.getValue().get("today").get("threshold"));
                        break;
                    }
                }
            }
        } catch (DomResourceException ex) {
            LOG.error("Could no create initial data: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     * Returns the measurments plugin.
     * @return
     * @throws UtilityMeasurementsException 
     */
    public static UtilityMeasurement getMeasurementsPlugin() throws UtilityMeasurementsException{
        if(!pluginsList.isEmpty()){
            return pluginsList.values().iterator().next();
        } else {
            throw new UtilityMeasurementsException("There is no plugin active");
        }
    }
    
    /**
     * Handles plugin value updates.
     * @param event 
     */
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.UTILITYMEASURERECEIVED)){
            Map<String,Object> dataSet = (Map<String,Object>)event.getData();
            LOG.debug("Got data: {}", dataSet);
            switch(event.getMethod()){
                case "getCurrentUsage":
                    try {
                        int pluginId = ((Long)dataSet.get("id")).intValue();
                        String type = ((String)dataSet.get("type"));
                        if(pluginsList.containsKey(pluginId)){
                            UtilityMeasurement plugin = pluginsList.get(pluginId);
                            Map<String,Map<String,Object>> powerValues = ((Map<String,Map<String,Object>>)dataSet.get("values"));
                            switch(type){
                                case "POWER":
                                    plugin.updatePower((double)powerValues.get("unitcurrent").get("value"), 
                                                       (double)powerValues.get("current").get("value"), 
                                                       (double)powerValues.get("today").get("value"));
                                break;
                                case "WATER":
                                    plugin.updateWater((double)powerValues.get("current").get("value"), 
                                                       (double)powerValues.get("today").get("value"));
                                break;
                                case "GAS":
                                    plugin.updateGas((double)powerValues.get("current").get("value"), 
                                                     (double)powerValues.get("today").get("value"));
                                break;
                            }
                        }
                    } catch (NullPointerException e){
                        LOG.error("Faulty utility measurement data: {}", event.getData(), e);
                    }
                break;
            }
        }
    }
    
}
