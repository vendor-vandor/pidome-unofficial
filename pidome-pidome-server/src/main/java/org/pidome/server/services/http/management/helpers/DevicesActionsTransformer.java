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

package org.pidome.server.services.http.management.helpers;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.services.http.Webservice_XSLTransformer;

/**
 *
 * @author John Sirach
 */
public final class DevicesActionsTransformer {
    
    static Logger LOG = LogManager.getLogger(DevicesActionsTransformer.class);
    
    String cmdSet = "";
    int deviceId = 0;
    String deviceName = "Unknown";

    Map<String, Object> cmds = new HashMap<>();
    
    public DevicesActionsTransformer() {}
        
    public final void setCurrentCmdSet(Map<String, Object> cmds){
        this.cmds = cmds;
    }
    
    public final String getTransformed(){
        try {
            
            Webservice_XSLTransformer deviceRender = new Webservice_XSLTransformer();
            deviceRender.setRenderFile("deviceActions");
            deviceName = (String)cmds.get("device");
            deviceId = (int)cmds.get("device_id");
            
            DeviceInterface device = DeviceService.getDevice(deviceId);
            deviceRender.setRenderParameter("devicedbname", device.getName());
            for(String key:cmds.keySet()){
                deviceRender.setRenderParameter(key, cmds.get(key));
            }
            cmdSet = deviceRender.render(device).replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
        } catch (UnknownDeviceException ex) {
            LOG.debug("Could not get device, it is unknown: {}", ex.getMessage());
        } catch (Exception ex) {
            LOG.debug("Could not render for device {}, message: {}", deviceId, ex.getMessage(), ex);
        }
        return cmdSet;
    }
    
}
