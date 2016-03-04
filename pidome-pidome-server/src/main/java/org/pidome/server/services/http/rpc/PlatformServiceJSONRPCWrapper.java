/*
 * Copyright 2015 John.
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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.http.HTTPConnector;
import org.pidome.server.connector.tools.http.JSONConnector;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.hardware.DeviceServiceException;
import org.pidome.server.system.platform.PlatformException;

/**
 *
 * @author John
 */
public class PlatformServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements PlatformServiceJSONRPCWrapperWrapperInterface {

    static Logger LOG = LogManager.getLogger(PlatformServiceJSONRPCWrapper.class);
    
        /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("searchCustomDevices", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("devicename", "");}});
                        put(1,new HashMap<String,Object>(){{put("driverid", "");}});
                    }
                });
                put("getPersonalCustomDevices", null);
            }
        };
        return mapping;
    }
                
                
    @Override
    public Object searchCustomDevices(String deviceName, String driverId) throws PlatformException {
        ArrayList<Map<String,Object>> resultSet = new ArrayList();
        if((!deviceName.isEmpty() && deviceName.length()>3)&& !driverId.isEmpty()){
            try {
                JSONConnector remote = new JSONConnector("http://platform.pidome.org/platform/devices/search-device.json", false);
                remote.addHeader("PiDome-Req", "PiDome-Req");
                Map<String,Object> params = new HashMap<>();
                params.put("name", deviceName);
                params.put("driverid", driverId);
                try {
                    remote.setHTTPMethod(HTTPConnector.POST);
                    Map<String,Object> remoteData = (Map<String,Object>)remote.postJSON("search.custom", params, "searchcustom").getObjectData();
                    if(remoteData.containsKey("devices")){
                        return (ArrayList<Map<String,Object>>)remoteData.get("devices");
                    }
                } catch (PidomeJSONRPCException ex) {
                    LOG.error("Got malformed data: {}", ex.getMessage());
                     throw new PlatformException("Could not retrieve data, check log file");
                }
            } catch (MalformedURLException ex) {
                LOG.error("Could not retrieve data: {}", ex.getMessage());
                throw new PlatformException("Could not retrieve data, check log file");
            }
        }
        return resultSet;
    }
    
    /**
     * Returns a list of personal custom devices.
     * @return
     * @throws PlatformException 
     */
    @Override
    public Object getPersonalCustomDevices() throws PlatformException {
        try {
            return DeviceService.getPersonalCustomDevices();
        } catch (DeviceServiceException ex) {
            LOG.error("Could not retrieve data: {}", ex.getMessage(), ex);
            throw new PlatformException("Could not retrieve data: " + ex.getMessage());
        }
    }
    
}