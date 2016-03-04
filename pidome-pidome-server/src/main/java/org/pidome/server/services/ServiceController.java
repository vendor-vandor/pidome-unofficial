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
package org.pidome.server.services;

import java.io.IOException;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.accesscontrollers.AccesControllersService;
import org.pidome.server.services.automations.AutomationRules;
import org.pidome.server.services.http.management.ManagementHttpService;
import org.pidome.server.services.clients.persons.PersonsManagement;
import org.pidome.server.services.clients.socketservice.SocketService;
import org.pidome.server.services.clients.socketservice.SocketServiceException;
import org.pidome.server.services.events.EventService;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.macros.MacroService;
import org.pidome.server.system.config.ConfigException;
import org.pidome.server.services.network.BroadcastService;
import org.pidome.server.services.plugins.DataModifierPluginService;
import org.pidome.server.services.plugins.DevicePluginService;
import org.pidome.server.services.plugins.MediaPluginService;
import org.pidome.server.services.plugins.MessengerPluginService;
import org.pidome.server.services.plugins.PluginService;
import org.pidome.server.services.plugins.RemotesPluginService;
import org.pidome.server.services.plugins.UtilityPluginService;
import org.pidome.server.services.plugins.WeatherPluginService;
import org.pidome.server.services.scenes.ServerScenes;
import org.pidome.server.services.triggerservice.TriggerService;
import org.pidome.server.system.dayparts.DayPartException;
import org.pidome.server.system.dayparts.DayPartsService;
import org.pidome.server.system.hardware.devices.DevicesException;
import org.pidome.server.system.location.BaseLocations;
import org.pidome.server.system.presence.PresenceException;
import org.pidome.server.system.presence.PresenceService;
import org.pidome.server.system.userstatus.UserStatusException;
import org.pidome.server.system.userstatus.UserStatusService;

public final class ServiceController {

    public enum Service {
        BROADCASTSERVER,
        MACROSERVICE,
        HARDWARESERVICE,
        TRIGGERSERVICE,
        PRESENCESERVICE,
        DAYPARTSERVICE,
        USERSTATUSSERVICE,
        LOCATIONSERVICE,

        SOCKETSERVICE,
        SOCKETSSERVICE,
        
        //Client services
        MANAGEMENTSERVICE,
        MOBILESERVICE,
        
        MODIFIERPLUGINSERVICE,
        MEDIAPLUGINSERVICE,
        UTILITYPLUGINSERVICE,
        MESSENGERPLUGINSERVICE,
        REMOTESPLUGINSERVICE,
        DEVICEPLUGINSERVICE,
        WEATHERPLUGINSERVICE,
        MQTTSERVERSERVICE,
        
        EVENTSERVICE,
        
        SCENESSERVICE,
        
        ACCESSCONTROLLERSERVICE,
        
        /**
         * Expirimental.
         */
        AUTOMATIONRULES
    }
    
    private static final HashMap<Service, ServiceInterface> ServiceMap = new HashMap<>();

    static Logger LOG = LogManager.getLogger(ServiceController.class);
    
    public static void initialize(){
        registerServices();
    }
    
    public static synchronized void stopAllServers() {
        for (Service service: ServiceMap.keySet()) {
            stopService(service);
        }
        unRegisterServices();
    }

    private static synchronized void stopService(Service service) {
        if (ServiceMap.containsKey(service) && isServiceRunning(service)) {
            LOG.debug("Attempting to stop service {}", ServiceMap.get(service).getServiceName());
            ServiceMap.get(service).interrupt();
        }
    }

    private static synchronized void startService(Service service) {
        if (ServiceMap.containsKey(service) && !isServiceRunning(service)) {
            LOG.debug("Attempting to start service {}", ServiceMap.get(service).getServiceName());
            ServiceMap.get(service).start();
        }
    }
    
    public static boolean isServiceRunning(Service service) {
        return ServiceMap.get(service).isAlive();
    }

    public static ServiceInterface getService(Service service){
        return ServiceMap.get(service);
    }
    
    public static void startAllServers(){
        PersonsManagement.getInstance();
        if (!ServiceMap.containsKey(Service.MANAGEMENTSERVICE)) {
            try {
                ManagementHttpService managementService = new ManagementHttpService();
                ServiceMap.put(Service.MANAGEMENTSERVICE, managementService);
                managementService.start();
            } catch (Exception ex) {
                LOG.error("Management http service could not be initialized: {}", ex.getMessage(), ex);
            }
        }
        if (!ServiceMap.containsKey(Service.LOCATIONSERVICE)) {
                ServiceInterface service = new BaseLocations();
                ServiceMap.put(Service.LOCATIONSERVICE, service);
                service.start();
        }
        if (!ServiceMap.containsKey(Service.MACROSERVICE)) {
            try {
                ServiceInterface service = new MacroService();
                ServiceMap.put(Service.MACROSERVICE, service);
                service.start();
            } catch (ConfigException ex) {
                LOG.error("Macro service could not be initialized: {} ", ex.getMessage());
            }
        }
        if (!ServiceMap.containsKey(Service.EVENTSERVICE)) {
            try {
                EventService service = EventService.getInstance();
                ServiceMap.put(Service.EVENTSERVICE, service);
                service.start();
            } catch (Exception ex) {
                LOG.error("Event service could not be initialized: {}", ex.getMessage());
            }
        }
        if (!ServiceMap.containsKey(Service.ACCESSCONTROLLERSERVICE)) {
            AccesControllersService accessControllerService = new AccesControllersService();
            ServiceMap.put(Service.ACCESSCONTROLLERSERVICE, accessControllerService);
            accessControllerService.start();
        }
        if (!ServiceMap.containsKey(Service.HARDWARESERVICE)) {
            try {
                ServiceInterface service = new DeviceService();
                ServiceMap.put(Service.HARDWARESERVICE, service);
                service.start();
            } catch (DevicesException ex) {
                LOG.error("Device service could not be initialized: {}", ex.getMessage());
            }
        }
        if (!ServiceMap.containsKey(Service.USERSTATUSSERVICE)) {
            try {
                ServiceInterface service = new UserStatusService();
                ServiceMap.put(Service.USERSTATUSSERVICE, service);
                service.start();
            } catch (UserStatusException ex) {
                LOG.error("User status service could not be initialized: {}", ex.getMessage());
            }
        }
        if (!ServiceMap.containsKey(Service.DAYPARTSERVICE)) {
            try {
                ServiceMap.put(Service.DAYPARTSERVICE, new DayPartsService());
            } catch (DayPartException ex) {
                LOG.error("Day part service could not be initialized: {}", ex.getMessage());
            }
        }
        if (!ServiceMap.containsKey(Service.PRESENCESERVICE)) {
            try {
                ServiceInterface service = new PresenceService();
                ServiceMap.put(Service.PRESENCESERVICE, service);
                service.start();
            } catch (PresenceException ex) {
                LOG.error("Presence service could not be initialized: {}", ex.getMessage());
            }
        }
        if (!ServiceMap.containsKey(Service.SOCKETSERVICE)) {
            try {
                ServiceInterface service = new SocketService();
                ServiceMap.put(Service.SOCKETSERVICE, service);
                service.start();
            } catch (SocketServiceException ex) {
                LOG.error("Client display service could not be initialized: {}", ex.getMessage());
            }
        }
        if(!ServiceMap.containsKey(Service.MODIFIERPLUGINSERVICE)){
            PluginService pluginService = DataModifierPluginService.getInstance();
            ServiceMap.put(Service.MODIFIERPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.MEDIAPLUGINSERVICE)) {
            PluginService pluginService = MediaPluginService.getInstance();
            ServiceMap.put(Service.MEDIAPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.UTILITYPLUGINSERVICE)) {
            PluginService pluginService = UtilityPluginService.getInstance();
            ServiceMap.put(Service.UTILITYPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.MESSENGERPLUGINSERVICE)) {
            MessengerPluginService pluginService = MessengerPluginService.getInstance();
            ServiceMap.put(Service.MESSENGERPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.REMOTESPLUGINSERVICE)) {
            RemotesPluginService pluginService = RemotesPluginService.getInstance();
            ServiceMap.put(Service.REMOTESPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.DEVICEPLUGINSERVICE)) {
            DevicePluginService pluginService = DevicePluginService.getInstance();
            ServiceMap.put(Service.DEVICEPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.WEATHERPLUGINSERVICE)) {
            WeatherPluginService pluginService = WeatherPluginService.getInstance();
            ServiceMap.put(Service.WEATHERPLUGINSERVICE, pluginService);
            pluginService.start();
        }
        if (!ServiceMap.containsKey(Service.TRIGGERSERVICE)) {
            TriggerService triggerService = new TriggerService();
            ServiceMap.put(Service.TRIGGERSERVICE, triggerService);
            triggerService.start();
        }
        if (!ServiceMap.containsKey(Service.AUTOMATIONRULES)) {
            AutomationRules rules = AutomationRules.getInstance();
            ServiceMap.put(Service.AUTOMATIONRULES, rules);
            Runnable task = () -> { rules.start(); };
            new Thread(task).start();
        }
        if (!ServiceMap.containsKey(Service.SCENESSERVICE)) {
            ServerScenes scenes = ServerScenes.getInstance();
            ServiceMap.put(Service.SCENESSERVICE, scenes);
            Runnable task = () -> { scenes.start(); };
            new Thread(task).start();
        }
        if (!ServiceMap.containsKey(Service.BROADCASTSERVER)) {
            try {
                ServiceInterface service = new BroadcastService();
                ServiceMap.put(Service.BROADCASTSERVER, service);
                service.start();
            } catch (IOException ex) {
                LOG.error("Broadcast service could not be initialized: {}", ex.getMessage());
            }
        }
    }
    
    private static void registerServices() {

    }

    private static void unRegisterServices() {
        ServiceMap.clear();
    }
}
