/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.devices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.categories.Categories;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopBase;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopIcon;
import org.pidome.client.system.scenes.components.mainstage.desktop.DraggableIconInterface;

/**
 *
 * @author John Sirach
 */
public final class Device implements DraggableIconInterface {

    Map<String,Object> primaryDetails = new HashMap<>();
    Map<String,CommandGroup> commandGroup = new HashMap<>();
    
    List<DeviceValueChangeListener> commonListeners = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(Device.class);
    
    DesktopIcon desktopIcon;
    
    public Device(Map<String,Object> deviceDetails) throws DomComponentsException {
        LOG.trace("New device {} at location {}", deviceDetails.get("name"), deviceDetails.get("locationname"));
        updateDetails(deviceDetails);
        createCommandSet();
    }

    public final void updateFavorite(boolean favorite){
        primaryDetails.put("favorite", favorite);
        handleShortCut();
    }
    
    final void handleShortCut(){
        handleShortCut(false);
    }
    
    final void handleShortCut(boolean removed){
        if(!removed && isFavorite() && getId()!=1 && desktopIcon==null){
            ArrayList deviceData = new ArrayList();
            deviceData.add(String.valueOf(getId()));
            desktopIcon = new DesktopIcon(this,DesktopIcon.DEVICE, getName(), "org.pidome.client.system.scenes.components.mainstage.displays.DeviceDisplay", deviceData);
            desktopIcon.setIcon(Categories.getCategoryConstant(this.getCategory()));
            DesktopBase.addDesktopIcon(desktopIcon);
        } else if ((!isFavorite() || removed) && desktopIcon!=null){
            DesktopBase.removeDesktopIcon(desktopIcon);
            desktopIcon = null;
        }
    }
    
    @Override
    public void iconRemoved() {
        Map<String,Object> serverParams = new HashMap<>();
        serverParams.put("id", getId());
        serverParams.put("favorite", false);
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("DeviceService.setFavorite", "DeviceService.setFavorite", serverParams));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send device favorite:false");
        }
    }
    
    @Override
    public void iconAdded() {
        Map<String,Object> serverParams = new HashMap<>();
        serverParams.put("id", getId());
        serverParams.put("favorite", true);
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("DeviceService.setFavorite", "DeviceService.setFavorite", serverParams));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send device favorite:false");
        }
    }
    
    public final void updateDetails(Map<String,Object> deviceDetails){
        primaryDetails.clear();
        primaryDetails.putAll(deviceDetails);
        handleShortCut();
        if(desktopIcon!=null){
            desktopIcon.updateName(getName());
        }
    }
    
    public final boolean isFavorite(){
        if(primaryDetails.containsKey("favorite")){
            return (boolean)primaryDetails.get("favorite");
        } else {
            return false;
        }
    }
    
    public final int getCategory(){
        if(primaryDetails.containsKey("category")){
            return ((Long)primaryDetails.get("category")).intValue();
        } else {
            return 0;
        }
    }
    
    public final boolean hasShortCut(){
        return primaryDetails.containsKey("shortcut");
    }
    
    public final int getShortCut(){
        return ((Long)primaryDetails.get("shortcut")).intValue();
    }
    
    public final int getLocation(){
        if(primaryDetails.containsKey("location")){
            return ((Long)primaryDetails.get("location")).intValue();
        } else {
            return 0;
        }
    }
    
    public final String getName(){
        if(primaryDetails.containsKey("name")){
            return (String)primaryDetails.get("name");
        } else {
            return "Unknown";
        }
    }
    
    public final int getId(){
        if(primaryDetails.containsKey("id")){
            return ((Long)primaryDetails.get("id")).intValue();
        } else {
            return 0;
        }
    }
    
    public final Object getLastCmd(String group, String set){
        if(commandGroup.containsKey(group)){
            return commandGroup.get(group).getLastCmd(set);
        } else {
            return "";
        }
    }
    
    public final void setServerCmd(String group, String set, Object value){
        notifyCommonListeners();
        if(group!=null && set != null && value!=null){
            if(commandGroup.containsKey(group)){
                commandGroup.get(group).updateValuesByServer(set, value);
            }
        }
    }
    
    public final Map<String,CommandGroup> getCommandGroups(){
        return commandGroup;
    }
    
    public final Map<String,Object> getDeviceDetails(){
        return primaryDetails;
    }
    
    final void createCommandSet(){
        if(primaryDetails.containsKey("commandgroups")){
            ArrayList cmdGroups = (ArrayList)primaryDetails.get("commandgroups");
            cmdGroups.stream().forEach((cmdGroup) -> {
                createCommandGroup((Map<String,Object>)cmdGroup);
            });
        }
    }
    
    final void createCommandGroup(Map<String,Object> commandGroups){
        commandGroup.put((String)commandGroups.get("id"), new CommandGroup(this,commandGroups));
    }
    
    final void setLastKnownCmds(NodeList lastCmds){
        for(int i=0; i< lastCmds.getLength();i++){
            Map<String,String>cmdDetails = new HashMap<>(getNodeAttributes(lastCmds.item(i)));
            if(!cmdDetails.isEmpty() && commandGroup.containsKey(cmdDetails.get("group"))){
                commandGroup.get(cmdDetails.get("group")).updateValues(cmdDetails.get("set"), cmdDetails.get("cmd"));
            }
        }
    }
    
    public final void sendCommand(String group, String control, Object value, String extra){
        Map<String, Object> valueSend = new HashMap<>();
        valueSend.put("value", value);
        valueSend.put("extra", extra);
        Map<String, Object> sendObject = new HashMap<String, Object>() {
            {
                put("id", ((Long)primaryDetails.get("id")).intValue());
                put("group", group);
                put("control", control);
                put("action", valueSend);
            }
        };
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("DeviceService.sendDevice", "DeviceService.sendDevice", sendObject));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send data: {}", sendObject);
        }
        
    }
    
    /**
     * Convenience function to retrieve all the node attributes contained in an element.
     * @param node
     * @return HashMap of all the attribute name value pairs of a node
     * @todo move to a XML convenience package
     */
    public final Map<String,String> getNodeAttributes(Node node){
        Map<String,String> nodeAttribMap = new HashMap<>();
        NamedNodeMap nodeAttr = node.getAttributes();
        if(nodeAttr!=null){
            for (int i = 0; i < nodeAttr.getLength(); ++i){
                Node attr = nodeAttr.item(i);
                nodeAttribMap.put(attr.getNodeName(),attr.getNodeValue());
            }
        }
        return nodeAttribMap;
    }
    
    
    public final synchronized void addDeviceValueEventListener(DeviceValueChangeListener l, String group, String setName) {
        if(commandGroup.containsKey(group)){
            commandGroup.get(group).addListener(l, setName);
        }
    }

    public final synchronized void removeDeviceValueEventListener(DeviceValueChangeListener l, String group, String setName) {
        if(commandGroup.containsKey(group)){
            commandGroup.get(group).removeListener(l, setName);
        }
    }
    
    public final synchronized void addDeviceValueEventListener(DeviceValueChangeListener l) {
        if(!commonListeners.contains(l)){
            commonListeners.add(l);
        }
    }

    public final synchronized void removeDeviceValueEventListener(DeviceValueChangeListener l) {
        if(commonListeners.contains(l)){
            commonListeners.remove(l);
        }
        for(Device.CommandGroup group : commandGroup.values()){
            group.removeListener(l);
        }
    }
    
    public final void notifyCommonListeners(){
        DeviceValueChangeEvent event = new DeviceValueChangeEvent(DeviceValueChangeEvent.GLOBALCHANGE);
        Iterator listeners = commonListeners.iterator();
        while (listeners.hasNext()) {
            ((DeviceValueChangeListener) listeners.next()).handleDeviceValueChange(event);
        }
    }

    public final class CommandGroup {
    
        Logger LOG = LogManager.getLogger(CommandGroup.class);
        
        Map<String,Object> groupDetails = new HashMap<>();
        String groupType;
        
        Map<String,Map<String,Object>> setDetails = new HashMap<>();
        Map<String,Map<String,Map<String,Object>>> cmdDetails = new HashMap<>();
        Map<String, Object> lastCmd = new HashMap<>();
        
        ObservableMap<String,Object> observableCmds = FXCollections.observableMap(lastCmd);
        
        Map<String, List<DeviceValueChangeListener>> _listeners = new HashMap<>();
        
        Map<String, Boolean> dataHistory = new HashMap<>();
        
        String deviceId = "0";
        
        Device device;
        
        public CommandGroup(Device device, final Map<String,Object>groupDetails){
            this.device = device;
            LOG.debug("Groupdetails: {}", groupDetails);
            this.groupDetails.putAll(groupDetails);
            createCommandSet();
            
            observableCmds.addListener((MapChangeListener.Change<? extends String, ? extends Object> change) -> {
                if(change.wasAdded()){
                    _fireDeviceValueChangeEvent((String)groupDetails.get("id"), change.getKey(), change.getValueAdded());
                }
            });   
        }
    
        public synchronized void removeListener(DeviceValueChangeListener l){
            for(String setName:_listeners.keySet()){
                if(_listeners.get(setName).contains(l)){
                    _listeners.get(setName).remove(l);
                }
            }
        }
        
        public synchronized void addListener(DeviceValueChangeListener l, String setName){
            if(!_listeners.containsKey(setName)){
                _listeners.put(setName, new ArrayList());
            }
            _listeners.get(setName).add(l);
            LOG.debug("Added device value event listener {}", l.getClass().getName());
        }
        
        public synchronized void removeListener(DeviceValueChangeListener l, String setName){
            if(_listeners.containsKey(setName)){
                _listeners.get(setName).remove(l);
            }
            LOG.debug("Removed device value event listener {}", l.getClass().getName());
        }
        
        void _fireDeviceValueChangeEvent(String groupName, String setName, Object value){
            LOG.debug("Event: {}, listeners: {}", DeviceValueChangeEvent.VALUECHANGED, _listeners.size());
            if(_listeners.containsKey(setName)){
                DeviceValueChangeEvent event = new DeviceValueChangeEvent(this.device, DeviceValueChangeEvent.VALUECHANGED);
                event.setValues(groupName, setName, value);
                Iterator listeners = _listeners.get(setName).iterator();
                while (listeners.hasNext()) {
                    ((DeviceValueChangeListener) listeners.next()).handleDeviceValueChange(event);
                }
            }
            LOG.debug("Got data for data history: {} - {}", setName, value);
        }
        
        public final Map<String,Object> getGroupDetails(){
            return groupDetails;
        }
        
        public final Map<String,Map<String,Object>> getFullSetList(){
            return setDetails;
        }
        
        public final Map<String,Object> getSetDetails(String setName){
            return setDetails.get(setName);
        }
        
        public final Map<String,Map<String,Object>> getCommandSet(String setName){
            return cmdDetails.get(setName);
        }
        
        public final boolean hasDataHistory(String set){
            return dataHistory.containsKey(set);
        }
        
        public final void updateValues(String set, String cmd){
            lastCmd.put(set, cmd);
        }
        
        public final void updateValuesByServer(String set, Object cmd){
            observableCmds.put(set, cmd);
        }
        
        public final void updateValuesByServer(String set, double cmd){
            observableCmds.put(set, String.valueOf(cmd));
        }
        
        public final Object getLastCmd(String setName){
            if(lastCmd.containsKey(setName)){
                return lastCmd.get(setName);
            } else {
                return "";
            }
        }
        
        final void createCommandSet(){
            if(this.groupDetails.containsKey("commands")){
                ArrayList cmds = (ArrayList) this.groupDetails.get("commands");
                for (int i = 0; i < cmds.size(); ++i){
                    Map<String,Object> cmd = (Map<String,Object>)cmds.get(i);
                    if(cmd.containsKey("typedetails")){
                        Map<String,Object> collectionDetails = (Map<String,Object>)cmd.get("typedetails");
                        groupType = (String)(cmd.get("commandtype"));
                        LOG.debug("Creating command type: {} for {}", groupType, collectionDetails);
                        switch((String)(cmd.get("commandtype"))){
                            case "select":
                                collectionDetails.put("type", groupType);
                                cmdDetails.put((String)collectionDetails.get("id"), composeSelectOptionsSet((ArrayList<Map<String,Object>>)collectionDetails.get("commandset")));
                            break;
                            case "toggle":
                                collectionDetails.put("type", groupType);
                                cmdDetails.put((String)collectionDetails.get("id"), composeCommandToggle((Map<String,Map<String,Object>>)collectionDetails.get("commandset")));
                            break;
                            case "data":
                                collectionDetails.put("datatype", collectionDetails.get("datatype"));
                                collectionDetails.put("type", groupType);
                                if(collectionDetails.containsKey("graph") && (boolean)collectionDetails.get("graph")==true){
                                    collectionDetails.put("graph", "true");
                                } else {
                                    collectionDetails.put("graph", "false");
                                }
                                cmdDetails.put((String)collectionDetails.get("id"), composeDataSet((String)collectionDetails.get("id"),collectionDetails));
                            break;
                            case "button":
                                collectionDetails.put("type", groupType);
                                cmdDetails.put((String)collectionDetails.get("id"), composeButtonSet(collectionDetails));
                            break;
                            case "slider":
                                collectionDetails.put("type", groupType);
                                cmdDetails.put((String)collectionDetails.get("id"), composeSliderSet(collectionDetails));
                            break;
                            case "colorpicker":
                                collectionDetails.put("type", groupType);
                                cmdDetails.put((String)collectionDetails.get("id"), composeColorpickerSet((ArrayList<Map<String,Object>>)collectionDetails.get("commandset")));
                            break;
                        }
                        LOG.debug("Having set details for device: {}", collectionDetails);
                        setDetails.put((String)collectionDetails.get("id"), collectionDetails);
                        if(cmd.containsKey("currentvalue") && cmd.get("currentvalue")!=null){
                            lastCmd.put((String)collectionDetails.get("id"), cmd.get("currentvalue"));
                        }
                    }
                }
            }
        }
        
        final Map<String,Map<String,Object>> composeColorpickerSet(ArrayList<Map<String,Object>> options){
            Map<String,Map<String,Object>> cmdSet = new HashMap<>();
            options.stream().forEach((option) -> {
                Map<String,Object> commandDetails = new HashMap<>();
                commandDetails.put("type", "option");
                commandDetails.put("label", option.get("label"));
                commandDetails.put("value", option.get("value"));
                cmdSet.put((String)option.get("value"), commandDetails);
            });
            return cmdSet;
        }        
        
        final Map<String,Map<String,Object>> composeButtonSet(Map<String,Object> details){
            Map<String,Map<String,Object>> dataSet = new HashMap<>();
            Map<String,Object> commandDetails = new HashMap<>();
            
            commandDetails.put("type", details.get("type"));
            commandDetails.put("label", details.get("label"));
            commandDetails.put("value", details.get("deviceCommandValue"));
            
            dataSet.put((String)details.get("label"), commandDetails);
            
            return dataSet;
        }
        
        final Map<String,Map<String,Object>> composeSliderSet(Map<String,Object> details){
            Map<String,Map<String,Object>> dataSet = new HashMap<>();
            Map<String,Object> commandDetails = new HashMap<>();
            
            commandDetails.put("type", details.get("type"));
            commandDetails.put("label", details.get("label"));
            commandDetails.put("value", details.get("value"));
            commandDetails.put("min", details.get("min"));
            commandDetails.put("max", details.get("max"));
            
            dataSet.put((String)details.get("label"), commandDetails);
            
            return dataSet;            
        }
        
        final Map<String,Map<String,Object>> composeDataSet(String id, Map<String,Object> details){
            Map<String,Map<String,Object>> dataSet = new HashMap<>();
            Map<String,Object> commandDetails = new HashMap<>();
            
            commandDetails.put("type", details.get("type"));
            commandDetails.put("label", details.get("label"));
            commandDetails.put("value", 0);
            if(((String)details.get("graph")).equals("true")){
                dataHistory.put(id, true);
            }
            if(details.containsKey("visual") && (boolean)details.get("visual")==true && details.containsKey("visualtype")){
                commandDetails.put("visual", details.get("visualtype"));
            }
            dataSet.put((String)details.get("label"), commandDetails);
            return dataSet;
        }
        
        final Map<String,Map<String,Object>> composeSelectOptionsSet(ArrayList<Map<String,Object>> options){
            Map<String,Map<String,Object>> cmdSet = new HashMap<>();
            options.stream().forEach((option) -> {
                Map<String,Object> commandDetails = new HashMap<>();
                commandDetails.put("type", "option");
                commandDetails.put("label", option.get("name"));
                commandDetails.put("value", option.get("value"));
                cmdSet.put((String)option.get("name"), commandDetails);
            });
            return cmdSet;
        }        
        
        final Map<String,Map<String,Object>> composeCommandToggle(Map<String,Map<String,Object>> options){
            Map<String,Map<String,Object>> cmdSet = new HashMap<>();
            for(String key: options.keySet()){
                Map<String,Object> commandDetails = new HashMap<>();
                switch(key){
                    case "on":
                        commandDetails.put("type", "on");
                        commandDetails.put("label","On");
                        commandDetails.put("value",options.get(key).get("value"));
                        cmdSet.put("On", commandDetails);
                    break;
                    case "off":
                        commandDetails.put("type", "off");
                        commandDetails.put("label","Off");
                        commandDetails.put("value",options.get(key).get("value"));
                        cmdSet.put("Off", commandDetails);
                    break;
                }
            }
            return cmdSet;
        }
        
    }
    
    
}
