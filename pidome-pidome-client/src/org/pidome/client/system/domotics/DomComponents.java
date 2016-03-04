/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.domotics.components.devices.Devices;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.domotics.components.macros.Macros;
import org.pidome.client.system.domotics.components.locations.Locations;
import org.pidome.client.system.domotics.components.Server;
import org.pidome.client.system.domotics.components.ServerEvent;
import org.pidome.client.system.domotics.components.dayparts.DayParts;
import org.pidome.client.system.domotics.components.plugins.PidomeMediaPlugin;
import org.pidome.client.system.domotics.components.messaging.ClientMessaging;
import org.pidome.client.system.domotics.components.notifications.Notifications;
import org.pidome.client.system.domotics.components.remotes.PiDomeRemotes;
import org.pidome.client.system.domotics.components.userpresence.UserPresences;
import org.pidome.client.system.domotics.components.utilitymeasurement.UtilityMeasurements;
import org.pidome.client.system.rpc.PidomeJSONRPC;

/**
 *
 * @author John Sirach
 */
public class DomComponents extends DomResources {

    static Map<String,Map<String, DomComponent>> componentCollection = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(DomComponents.class);

    static DomComponents self;
    
    public DomComponents(){ self = this; }
    
    public static DomComponents getInstance(){
        return self;
    }
    
    public final void createComponentsFromInit(Document xml) throws DomComponentsException {
        componentCollection.put("categories", createCategories());
        componentCollection.put("locations", createLocations());
        componentCollection.put("devices", createDevices());
        componentCollection.put("userpresences", createUserPresences());
        componentCollection.put("dayparts", createDayParts());
        componentCollection.put("macros", createMacros());
        componentCollection.put("remotes", createRemotes());
        componentCollection.put("utilitymeasurment", createUtilityMeasurment());
        try {
            LOG.debug("Starting initialization");
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            Node initNode = (Node)xpath.compile("/init").evaluate(xml, XPathConstants.NODE);
            NodeList initList = initNode.getChildNodes();
            if(initList.getLength()<5){
                throw new DomComponentsException("Server initialization XML not correct");
            }
            for(int i = 0; i < initList.getLength(); i++){
                switch(initList.item(i).getNodeName()){
                    case "server":
                        componentCollection.put("server", createServer(initList.item(i)));
                        ((Server)componentCollection.get("server").get("server")).dispatchServerEvent(ServerEvent.SERVERVERSIONINFOCHANGED);
                    break;
                    case "clients":
                        componentCollection.put("clients", createClients(initList.item(i)));
                    break;
                    case "plugins":
                        componentCollection.put("plugins", createPlugins(initList.item(i)));
                    break;
                }
            }
            componentCollection.put("messaging", new HashMap<String, DomComponent>(){{ put ("ClientMessaging",new ClientMessaging());}});
            componentCollection.put("notifications", new HashMap<String, DomComponent>(){{ put ("Notifications",new Notifications());}});
            _fireDomoticsEvent(DomoticsEvent.INITDATARECEIVED);
        } catch (XPathExpressionException ex) {
            LOG.error("Could not initialize base component collection from init xml: {}", ex.getMessage());
        }
    }
    
    public final void updateDeviceFromJSON(PidomeJSONRPC JSONData, String subUpdate) throws DomComponentsException {
        Map<String, Object> dataResult = JSONData.getResult();
        switch (subUpdate) {
            case "UPDATEDEVICE":
                LOG.debug("To update a device");
                updateDevice((Map<String, Object>) dataResult.get("data"));
                break;
            case "ADDEDDEVICE":
                LOG.debug("To add a device");
                createSingleDevice((Map<String, Object>) dataResult.get("data"));
                break;
        }
    }
    
    public final void updateComponentsFromXml(Document xml, String subUpdate) throws DomComponentsException {
        try {
            LOG.debug("Starting updating");
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            Node initNode = (Node)xpath.compile("/data").evaluate(xml, XPathConstants.NODE);
            NodeList initList = initNode.getChildNodes();
            if(initList.getLength()<2){
                throw new DomComponentsException("Server data XML not correct");
            }
            for(int i = 0; i < initList.getLength(); i++){
                //LOG.debug("Update Node name: {}", initList.item(i).getNodeName());
                switch(initList.item(i).getNodeName()){
                    case "plugins":
                        updatePlugins(initList.item(i));
                    break;
                }
            }
        } catch (XPathExpressionException ex) {
            LOG.error("Could not initialize base component collection from data xml: {}", ex.getMessage());
        }
    }
    
    Map<String, DomComponent> createPlugins(Node pluginsNode) throws DomComponentsException {
        Map<String, DomComponent> collection = new HashMap<>();
        if(pluginsNode.getNodeName().equals("plugins")){
            PidomeMediaPlugin media = new PidomeMediaPlugin();
            NodeList pluginsNodes = pluginsNode.getChildNodes();
            for(int i=0; i< pluginsNodes.getLength();i++){
                if(pluginsNodes.item(i).getNodeType()==Node.ELEMENT_NODE)
                switch(pluginsNodes.item(i).getNodeName()){
                    case "mediaplugins":
                        NodeList mediaNodes = pluginsNodes.item(i).getChildNodes();
                        for(int j=0; j< mediaNodes.getLength();j++){
                            switch(mediaNodes.item(j).getNodeName()){
                                case "mediaplugin":
                                    media.createMedia(mediaNodes.item(i));
                                break;
                            }
                        }
                    break;
                }
            }
            collection.put("media", media);
        }
        return collection;
    }
    
    void updatePlugins(Node pluginsNode){
        if(pluginsNode.getNodeName().equals("plugins")){
            NodeList pluginsNodes = pluginsNode.getChildNodes();
            for(int i=0; i< pluginsNodes.getLength();i++){
                if(pluginsNodes.item(i).getNodeType()==Node.ELEMENT_NODE)
                switch(pluginsNodes.item(i).getNodeName()){
                    case "mediaplugins":
                        NodeList mediaNodes = pluginsNodes.item(i).getChildNodes();
                        for(int j=0; j< mediaNodes.getLength();j++){
                            switch(mediaNodes.item(j).getNodeName()){
                                case "mediaplugin":
                                    ((PidomeMediaPlugin)componentCollection.get("plugins").get("media")).createMedia(mediaNodes.item(i));
                                break;
                            }
                        }
                    break;
                }
            }
        }
    }
    
    public final void removePlugin(int pluginId){
        ((PidomeMediaPlugin)componentCollection.get("plugins").get("media")).deleteMedia(pluginId);
    }
    
    Map<String, DomComponent> createClients(Node clientsNode) throws DomComponentsException {
        Map<String, DomComponent> collection = new HashMap<>();
        if(clientsNode.getNodeName().equals("clients")){
            PidomeClients clients = new PidomeClients();
            NodeList clientNodes = clientsNode.getChildNodes();
            for(int i=0; i< clientNodes.getLength();i++){
                switch(clientNodes.item(i).getNodeName()){
                    case "client":
                        clients.addClient(DomoticsHttpComponentParser.getNodeAttributes(clientNodes.item(i)).get("id"));
                    break;
                }
            }
            collection.put("clients", clients);
        }
        return collection;
    }
    
    Map<String, DomComponent> createServer(Node serverNode) throws DomComponentsException {
        Map<String, DomComponent> collection = new HashMap<>();
        if(serverNode.getNodeName().equals("server")){
            Server serverComponent = new Server();
            serverComponent.setServerInfo(DomoticsHttpComponentParser.getNodeAttributes(serverNode));
            NodeList serverNodes = serverNode.getChildNodes();
            for(int i=0; i< serverNodes.getLength();i++){
                switch(serverNodes.item(i).getNodeName()){
                    case "version":
                        serverComponent.setServerVersionInfo(DomoticsHttpComponentParser.getNodeAttributes(serverNodes.item(i)));
                    break;
                    case "date":
                        serverComponent.setServerInitInfo(DomoticsHttpComponentParser.getNodeAttributes(serverNodes.item(i)));
                    break;
                }
            }
            collection.put("server", serverComponent);
        } else {
            throw new DomComponentsException("Could not create server components");
        }
        LOG.debug("Server initialized");
        return collection;
    }
    
    Map<String,DomComponent> createUtilityMeasurment(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("UtilityMeasurementService.getPlugins", null).getResult();
            if(result.containsKey("data")){
                UtilityMeasurements utilityComponent = new UtilityMeasurements(this, (ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", utilityComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("Utility measurment initialized");
        return collection;
    }
    
    
    Map<String,DomComponent> createUserPresences(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("PresenceService.getPresences", null).getResult();
            if(result.containsKey("data")){
                UserPresences precenseComponent = new UserPresences((ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", precenseComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("User presences initialized");
        return collection;
    }
    
    Map<String,DomComponent> createUserStatuses(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("UserStatusService.getUserStatuses", null).getResult();
            if(result.containsKey("data")){
                UserPresences precenseComponent = new UserPresences((ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", precenseComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("User presences initialized");
        return collection;
    }
    
    Map<String,DomComponent> createDayParts(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("DayPartService.getDayParts", null).getResult();
            if(result.containsKey("data")){
                DayParts daypartsComponent = new DayParts((ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", daypartsComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("User presences initialized");
        return collection;
    }
    
    Map<String, DomComponent> createLocations(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("LocationService.getLocations", null).getResult();
            if(result.containsKey("data")){
                Locations locationsComponent = new Locations((ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", locationsComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("Locations initialized");
        return collection;
    }
    
    /**
     * Creates the initial Categories.
     * @return 
     */
    Map<String, DomComponent> createCategories(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("CategoryService.getFullCategoryList", null).getResult();
            if(result.containsKey("data")){
                Categories categoriesComponent = new Categories((ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", categoriesComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("Categories initialized");
        return collection;
    }
    
    /**
     * Create macro's
     * @return 
     */
    Map<String, DomComponent> createMacros(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("MacroService.getMacros", null).getResult();
            if(result.containsKey("data")){
                Macros macrosComponent = new Macros(this,(ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", macrosComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("Macro's initialized");
        return collection;
    }
    
    /**
     * Create macro's
     * @return 
     */
    Map<String, DomComponent> createRemotes(){
        Map<String, DomComponent> collection = new HashMap<>();
        try {
            Map<String,Object> result = getJSONData("RemotesService.getRemotes", null).getResult();
            if(result.containsKey("data")){
                PiDomeRemotes remotesComponent = new PiDomeRemotes(this,(ArrayList<Map<String,Object>>)result.get("data"));
                collection.put("component", remotesComponent);
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        LOG.debug("Remotes initialized");
        return collection;
    }
    
    /**
     * Creates the initial devices.
     * @return 
     */
    Map<String, DomComponent> createDevices(){
        Map<String, DomComponent> collection = new HashMap<>();
        Devices deviceComponent = new Devices();
        collection.put("component", deviceComponent);
        try {
            Map<String,Object> result = getJSONData("DeviceService.getActiveDevices", null).getResult();
            if(result.containsKey("data")){
                ArrayList<Map<String,Object>> devices = (ArrayList<Map<String,Object>>)result.get("data");
                for( Map<String,Object> device: devices){
                    try {
                        Map<String,Object> params = new HashMap<>();
                        params.put("id", device.get("id"));
                        deviceComponent.createDevice((Map<String, Object>)getJSONData("DeviceService.getDevice", params).getResult().get("data"));
                    } catch (DomComponentsException ex) {
                        LOG.error("Could not create device: {}", device);
                    }
                }
            }
        } catch(DomResourceException ex){
            LOG.error("Could not retrieve data: {}", ex.getMessage());
        }
        deviceComponent.startEvents();
        LOG.debug("Devices initialized");
        return collection;
    }
    
    
    //// System updates
    void createSingleDevice(Map<String,Object> deviceDetails){
        Devices deviceComponent = ((Devices)componentCollection.get("devices").get("component"));
        try {
            deviceComponent.createDevice(deviceDetails);
        } catch (DomComponentsException ex) {
            LOG.error("could not create device from device node: {}", ex.getMessage());
        }
        LOG.debug("Device created");
    }
    
    public final void removeDevice(int deviceId){
        Devices deviceComponent = ((Devices)componentCollection.get("devices").get("component"));
        deviceComponent.removeDeviceById(deviceId);
    }
    

    /**
     * Updates a device.
     * @param deviceData 
     */
    void updateDevice(Map<String,Object> deviceData){
        Devices deviceComponent = ((Devices)componentCollection.get("devices").get("component"));
        deviceComponent.updateDevice(deviceData);
        LOG.debug("Device updated");
    }
    
}
