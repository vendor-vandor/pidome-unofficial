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

package org.pidome.server.services.http.management.desktop;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.ServiceController;
import org.pidome.server.system.network.http.HttpServer;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.network.BroadcastService;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.plugins.PluginService;
import org.pidome.server.services.clients.socketservice.SocketService;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.hardware.Hardware;
import org.pidome.server.system.hardware.devices.Devices;
import org.pidome.server.system.network.Network;
import org.pidome.server.services.http.Webservice_renderer;

/**
 *
 * @author John
 */
public final class Webclient_serverstatus extends Webservice_renderer {
    
    static Logger LOG = LogManager.getLogger(Webclient_serverstatus.class);
    
    @Override
    public void collect() {
        Map<String,Object> pageData = new HashMap<>();
        pageData.put("page_title", "Server status");
        
        Map<String, String> serviceDetails = new HashMap<>();
        
        serviceDetails.put("hardwareOnline", String.valueOf(Hardware.isRunning().get()));
        serviceDetails.put("devicesOnline", String.valueOf(ServiceController.getService(ServiceController.Service.HARDWARESERVICE).isAlive()));
        serviceDetails.put("activeDevicesCount", String.valueOf(Devices.getActiveDevices().size()));
        serviceDetails.put("peripheralsCount", String.valueOf(DeviceService.getRunningHardwarePeripherals().size() + DeviceService.getWaitingHardwarePeripherals().size()));
        
        serviceDetails.put("BroadcastService", ((BroadcastService)ServiceController.getService(ServiceController.Service.BROADCASTSERVER)).getCombinedAddress());
        serviceDetails.put("BroadcastServiceAvailable", String.valueOf(ServiceController.getService(ServiceController.Service.BROADCASTSERVER).isAlive()));
        
        serviceDetails.put("ClientDisplayService", ((SocketService)ServiceController.getService(ServiceController.Service.SOCKETSERVICE)).getCombinedAddress());
        serviceDetails.put("ClientDisplayServiceAvailable", String.valueOf(ServiceController.getService(ServiceController.Service.SOCKETSERVICE).isAlive()));
        serviceDetails.put("ClientDisplayServiceSSL", ((SocketService)ServiceController.getService(ServiceController.Service.SOCKETSERVICE)).getCombinedSSLAddress());
        
        serviceDetails.put("WebHttpService", ((HttpServer)ServiceController.getService(ServiceController.Service.MANAGEMENTSERVICE)).getCombinedAddress());
        serviceDetails.put("WebHttpServiceAvailable", String.valueOf(ServiceController.getService(ServiceController.Service.MANAGEMENTSERVICE).isAlive()));
        
        serviceDetails.put("WebHttpServiceSSL", "Unknown");
        
        serviceDetails.put("WebHttpServiceSSL", "See http");
        serviceDetails.put("WebHttpServiceSSLAvailable", String.valueOf(((HttpServer)ServiceController.getService(ServiceController.Service.MANAGEMENTSERVICE)).SSLAvailable()));
        
        serviceDetails.put("WebHttpWSService", "See http");
        serviceDetails.put("WebHttpWSServiceAvailable", String.valueOf(ServiceController.getService(ServiceController.Service.MOBILESERVICE).isAlive()));
        try {
            serviceDetails.put("WebHttpWSServiceSSL", "See http");
        } catch (NullPointerException ex) {
            serviceDetails.put("WebHttpWSServiceSSL", "");
        }
        serviceDetails.put("WebHttpWSServiceAvailableSSL", String.valueOf(ServiceController.getService(ServiceController.Service.MOBILESERVICE).isAlive()));
        
        serviceDetails.put("TriggerServiceAvail", String.valueOf(((TriggerService)ServiceController.getService(ServiceController.Service.TRIGGERSERVICE)).isAlive()));
        serviceDetails.put("TriggerServiceAmount", String.valueOf(((TriggerService)ServiceController.getService(ServiceController.Service.TRIGGERSERVICE)).getTriggersAmount()));
        
        serviceDetails.put("MediaServiceAvail", String.valueOf(((MediaPluginService)ServiceController.getService(ServiceController.Service.MEDIAPLUGINSERVICE)).isAlive()));
        serviceDetails.put("MediaServiceAmount", String.valueOf(((MediaPluginService)ServiceController.getService(ServiceController.Service.MEDIAPLUGINSERVICE)).getPluginsCount()));
        
        serviceDetails.put("UtilityServiceAvail", String.valueOf(((PluginService)ServiceController.getService(ServiceController.Service.UTILITYPLUGINSERVICE)).isAlive()));
        
        serviceDetails.put("HardwareUSBWatchdog", String.valueOf(DeviceService.USBWatchdogRunning()));
        
        try {
            serviceDetails.put("cpuLoad", DeviceService.getDevice(1).getStoredCmdSet().get("values").get("cpuusage") + "% ");
            serviceDetails.put("memLoad", DeviceService.getDevice(1).getStoredCmdSet().get("values").get("memusage") + "MB ");
            serviceDetails.put("sizeLoad", DeviceService.getDevice(1).getStoredCmdSet().get("values").get("diskspace") + "MB ");
            serviceDetails.put("cpuHeatLoad", DeviceService.getDevice(1).getStoredCmdSet().get("values").get("procheat") + "'C ");
        } catch (Exception ex) {
            serviceDetails.put("cpuLoad", "Unknown");
            serviceDetails.put("memLoad", "Unknown");
            serviceDetails.put("sizeLoad", "Unknown");
            serviceDetails.put("cpuHeatLoad", "Unknown");
        }
        
        try {
            serviceDetails.put("release", SystemConfig.getProperty("system", "server.major") +"." + SystemConfig.getProperty("system", "server.minor") + " (" + SystemConfig.getProperty("system", "server.releasename") + ")");
        } catch (ConfigPropertiesException ex) {
            serviceDetails.put("release", "Unknown");
        }
        try {
            serviceDetails.put("build", SystemConfig.getProperty("system", "server.build"));
        } catch (ConfigPropertiesException ex) {
            serviceDetails.put("build", "Unknown");
        }
        
        try {
            serviceDetails.put("dataLeds", SystemConfig.getProperty("system", "server.datalednotifications"));
        } catch (ConfigPropertiesException ex) {
            serviceDetails.put("dataLeds", "Unknown");
        }
        
        try {
            serviceDetails.put("ipAddress",Network.getIpAddressProperty().get().getHostAddress());
        } catch (UnknownHostException ex) {
            serviceDetails.put("ipAddress","Error");
        }
        
        pageData.put("serviceDetails", serviceDetails);
        setData(pageData);
    }
    
}
