/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import static org.pidome.server.services.http.rpc.PidomeJSONRPC.LOG;

/**
 *
 * @author John
 */
public class RPCMethodLibrary {
 
    private static Map<String,AbstractRPCMethodExecutor> wrapperSet;
    
    public static void prepare(){
        LOG.info("Initializing RPC");
        wrapperSet = new HashMap<String, AbstractRPCMethodExecutor>() {
            {
                put("DeviceService", new DeviceServiceJSONRPCWrapper());
                put("LocationService", new LocationServiceJSONRPCWrapper());
                put("CategoryService", new CategoryServiceJSONRPCWrapper());
                put("SystemService", new SystemServiceJSONRPCWrapper());
                put("ClientService", new ClientServiceJSONRPCWrapper());
                put("MacroService", new MacroServiceJSONRPCWrapper());
                put("PluginService", new PluginServiceJSONRPCWrapper());
                put("MediaService", new MediaServiceJSONRPCWrapper());
                put("TriggerService", new TriggerServiceJSONRPCWrapper());
                put("PresenceService", new PresenceServiceJSONRPCWrapper());
                put("DayPartService", new DayPartServiceJSONRPCWrapper());
                put("UserStatusService", new UserStatusServiceJSONRPCWrapper());
                put("UtilityMeasurementService", new UtilityMeasurementServiceJSONWrapper());
                put("MessengerService", new MessengerServiceJSONRPCWrapper());
                put("RemotesService", new RemotesServiceJSONRPCWrapper());
                put("DevicePluginService", new DevicePluginServiceJSONRPCWrapper());
                put("HardwareService", new HardwareServiceJSONRPCWrapper());
                put("AutomationRulesService", new AutomationRulesServiceJSONRPCWrapper());
                put("UserService", new UserServiceJSONRPCWrapper());
                put("WeatherService", new WeatherServiceJSONRPCWrapper());
                put("PlatformService", new PlatformServiceJSONRPCWrapper());
                put("EventService", new EventServiceJSONRPCWrapper());
                put("ScenesService", new ScenesServiceJSONRPCWrapper());
                put("PackageService", new PackageServiceJSONRPCWrapper());
                put("AccessControllerService", new AccessControllerServiceJSONRPCWrapper());
                put("DashboardService", new DashboardServiceJSONRPCWrapper());
                put("DataModifierService", new DataModifierServiceJSONRPCWrapper());
                put("GraphService", new GraphServiceJSONWrapper());
                put("JSONService", null);
            }
        };
        prepareMethodSets();
        LOG.debug("JSON wrapper set.");
        LOG.info("Done Initializing RPC");
    }

    protected static boolean containsNameSpace(String nameSpace){
        return wrapperSet.containsKey(nameSpace);
    }
    
    protected static AbstractRPCMethodExecutor getMethodWrapper(String nameSpace){
        return wrapperSet.get(nameSpace);
    }
    
    private static boolean prepareMethodSets(){
        Map<String,Method[]> methodSet = new HashMap<>();
        Map<String,Map<String,Map<Integer,Map<String, Object>>>> namedMapper = new HashMap<>();
        for(String key:wrapperSet.keySet()){
            switch(key){
                case "DeviceService":
                    methodSet.put("DeviceService", DeviceServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("DeviceService", wrapperSet.get("DeviceService").createFunctionalMapping());
                break;
                case "LocationService":
                    methodSet.put("LocationService", LocationServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("LocationService", wrapperSet.get("LocationService").createFunctionalMapping());
                break;
                case "CategoryService":
                    methodSet.put("CategoryService", CategoryServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("CategoryService", wrapperSet.get("CategoryService").createFunctionalMapping());
                break;
                case "SystemService":
                    methodSet.put("SystemService", SystemServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("SystemService", wrapperSet.get("SystemService").createFunctionalMapping());
                break;
                case "ClientService":
                    methodSet.put("ClientService", ClientServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("ClientService", wrapperSet.get("ClientService").createFunctionalMapping());
                break;
                case "MacroService":
                    methodSet.put("MacroService", MacroServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("MacroService", wrapperSet.get("MacroService").createFunctionalMapping());
                break;
                case "PluginService":
                    methodSet.put("PluginService", PluginServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("PluginService", wrapperSet.get("PluginService").createFunctionalMapping());
                break;
                case "MediaService":
                    methodSet.put("MediaService", MediaServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("MediaService", wrapperSet.get("MediaService").createFunctionalMapping());
                break;
                case "TriggerService":
                    methodSet.put("TriggerService", TriggerServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("TriggerService", wrapperSet.get("TriggerService").createFunctionalMapping());
                break;
                case "PresenceService":
                    methodSet.put("PresenceService", PresenceServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("PresenceService", wrapperSet.get("PresenceService").createFunctionalMapping());
                break;
                case "DayPartService":
                    methodSet.put("DayPartService", DayPartServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("DayPartService", wrapperSet.get("DayPartService").createFunctionalMapping());
                break;
                case "UserStatusService":
                    methodSet.put("UserStatusService", UserStatusServiceJSONRPCWrapperInterface.class.getDeclaredMethods());
                    namedMapper.put("UserStatusService", wrapperSet.get("UserStatusService").createFunctionalMapping());
                break;
                case "UtilityMeasurementService":
                    methodSet.put("UtilityMeasurementService", UtilityMeasurementServiceJSONWrapper.class.getDeclaredMethods());
                    namedMapper.put("UtilityMeasurementService", wrapperSet.get("UtilityMeasurementService").createFunctionalMapping());
                break;
                case "MessengerService":
                    methodSet.put("MessengerService", MessengerServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("MessengerService", wrapperSet.get("MessengerService").createFunctionalMapping());
                break;
                case "RemotesService":
                    methodSet.put("RemotesService", RemotesServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("RemotesService", wrapperSet.get("RemotesService").createFunctionalMapping());
                break;
                case "DevicePluginService":
                    methodSet.put("DevicePluginService", DevicePluginServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("DevicePluginService", wrapperSet.get("DevicePluginService").createFunctionalMapping());
                break;         
                case "HardwareService":
                    methodSet.put("HardwareService", HardwareServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("HardwareService", wrapperSet.get("HardwareService").createFunctionalMapping());
                break;
                case "AutomationRulesService":
                    methodSet.put("AutomationRulesService", AutomationRulesServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("AutomationRulesService", wrapperSet.get("AutomationRulesService").createFunctionalMapping());
                break;
                case "UserService":
                    methodSet.put("UserService", UserServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("UserService", wrapperSet.get("UserService").createFunctionalMapping());
                break;
                case "WeatherService":
                    methodSet.put("WeatherService", WeatherServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("WeatherService", wrapperSet.get("WeatherService").createFunctionalMapping());
                break;
                case "PlatformService":
                    methodSet.put("PlatformService", PlatformServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("PlatformService", wrapperSet.get("PlatformService").createFunctionalMapping());
                break;
                case "EventService":
                    methodSet.put("EventService", EventServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("EventService", wrapperSet.get("EventService").createFunctionalMapping());
                break;
                case "ScenesService":
                    methodSet.put("ScenesService", ScenesServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("ScenesService", wrapperSet.get("ScenesService").createFunctionalMapping());
                break;
                case "PackageService":    
                    methodSet.put("PackageService", PackageServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("PackageService", wrapperSet.get("PackageService").createFunctionalMapping());
                break;
                case "AccessControllerService":    
                    methodSet.put("AccessControllerService", AccessControllerServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("AccessControllerService", wrapperSet.get("AccessControllerService").createFunctionalMapping());
                break;
                case "DashboardService":
                    methodSet.put("DashboardService", DashboardServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("DashboardService", wrapperSet.get("DashboardService").createFunctionalMapping());
                break;
                case "DataModifierService":
                    methodSet.put("DataModifierService", DataModifierServiceJSONRPCWrapper.class.getDeclaredMethods());
                    namedMapper.put("DataModifierService", wrapperSet.get("DataModifierService").createFunctionalMapping());
                break;
                case "GraphService":
                    methodSet.put("GraphService", GraphServiceJSONWrapper.class.getDeclaredMethods());
                    namedMapper.put("GraphService", wrapperSet.get("GraphService").createFunctionalMapping());
                break;
                default:
                    LOG.error("RPC namespace '{}' has been misconfigured!", key);
                break;
            }
            LOG.info("Done initializing RPC namespace: {}", key);
        }
        AbstractRPCMethodExecutor.setDeclaredMethods(methodSet, namedMapper);
        return true;
    }
    
}