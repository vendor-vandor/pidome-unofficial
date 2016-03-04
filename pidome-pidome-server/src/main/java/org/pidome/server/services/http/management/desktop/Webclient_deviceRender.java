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

import org.pidome.server.services.http.management.helpers.DeviceValuesParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.services.http.Webservice_XSLTransformer;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John Sirach
 */
public class Webclient_deviceRender extends Webservice_renderer {

    static Logger LOG = LogManager.getLogger(Webclient_deviceRender.class);

    @Override
    public String render() {
        try {
            if ((postDataMap.containsKey("device") && postDataMap.get("device").length() > 0
                    && postDataMap.containsKey("deviceid") && postDataMap.get("deviceid").length() > 0) ||
                (getDataMap.containsKey("device") && getDataMap.get("device").length() > 0
                    && getDataMap.containsKey("deviceid") && getDataMap.get("deviceid").length() > 0)) {
                Webservice_XSLTransformer deviceRender = new Webservice_XSLTransformer();
                deviceRender.setRenderFile("device");
                String deviceId = (postDataMap.get("deviceid")!=null)?postDataMap.get("deviceid"):getDataMap.get("deviceid");

                DeviceInterface device;
                try {
                    device = DeviceService.getDevice(Integer.parseInt(deviceId));

                    deviceRender.setRenderParameter("deviceId", deviceId);
                    deviceRender.setRenderParameter("deviceActionName", (postDataMap.get("device")!=null)?postDataMap.get("device"):getDataMap.get("device"));
                    deviceRender.setRenderParameter("deviceLocationName", device.getLocationName());
                    deviceRender.setRenderParameter("deviceFriendlyName", device.getDeviceName());
                    deviceRender.setRenderParameter("deviceValues", new DeviceValuesParser(device.getStoredCmdSet()));
                    
                    deviceRender.setRenderParameter("deviceCategoryName", device.getCategoryName());
                    deviceRender.setRenderParameter("deviceIsFavorite", device.getIsFavorite()==true?"Yes":"No");
                    
                    deviceRender.setRenderParameter("serverHost", this.serverData.get("hostname"));
                    deviceRender.setRenderParameter("serverPort", this.serverData.get("hostport"));
                    
                    if(getDataMap.containsKey("ajax")){
                        deviceRender.setRenderParameter("_ajaxRequest", "true");
                    } else {
                        deviceRender.setRenderParameter("_ajaxRequest", "false");
                    }
                    
                    return getCustomRender(deviceRender.render(device));
                } catch (UnknownDeviceException ex) {
                    LOG.error("Unknown device: {}", ex.getMessage());
                    return ex.getMessage();
                } catch (Exception ex) {
                    LOG.error("Problem parsing: {}", ex.getMessage());
                    return "Problem parsing: " + ex.getMessage();
                }

            }
        } catch (UnsupportedOperationException ex) {
            LOG.error(ex.getMessage());
            return ex.getMessage();
        }
        LOG.error("faulty Post/Get data (?): {}, {}", postDataMap,getDataMap);
        return "No data";
    }
    
}
