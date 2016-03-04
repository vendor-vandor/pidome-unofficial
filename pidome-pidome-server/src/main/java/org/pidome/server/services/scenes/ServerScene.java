/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.scenes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.system.hardware.devices.DeviceInterface;

/**
 *
 * @author John
 */
public class ServerScene {

    private final int sceneId;
    private String sceneName = "";
    private String description="";
    private boolean isActive = false;
    
    private List<DeviceSceneInclusion> deviceInclusions = new ArrayList<>();
    private ArrayList dependenciesPlane = new ArrayList<>();
    
    static Logger LOG = LogManager.getLogger(ServerScene.class);
    
    public ServerScene(int sceneId, String sceneName){
        this.sceneId   = sceneId;
        this.sceneName = sceneName;
    }
    
    public final int getSceneId(){
        return this.sceneId;
    }
    
    protected final void setSceneName(String name){
        this.sceneName = name;
    }
    
    public final String getSceneName(){
        return this.sceneName;
    }
    
    protected final void setDescription(String description){
        this.description = description;
    }
    
    public final String getDescription(){
        return this.description;
    }
    
    protected final void setActive(boolean active){
        this.isActive = active;
    }
    
    public final boolean isActive(){
        return isActive;
    }
    
    /**
     * Checks if there is a dependency.
     * @param deviceId
     * @param groupId
     * @param controlId
     * @return 
     */
    public final boolean hasDependency(int deviceId, String groupId, String controlId){
        Iterator<DeviceSceneInclusion> inclusions = this.deviceInclusions.iterator();
        while(inclusions.hasNext()){
            DeviceSceneInclusion incl = inclusions.next();
            if(incl.getDeviceId()==deviceId && incl.getGroupId().equals(groupId) && incl.getControlId().equals(controlId)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if there is a dependency and if there is one add the last known command.
     * @param deviceId
     * @param groupId
     * @param controlId
     * @param command
     * @return 
     */
    public final boolean registerLastKnownCommand(int deviceId, String groupId, String controlId, Runnable command){
        Iterator<DeviceSceneInclusion> inclusions = this.deviceInclusions.iterator();
        while(inclusions.hasNext()){
            DeviceSceneInclusion incl = inclusions.next();
            if(incl.getDeviceId()==deviceId && incl.getGroupId().equals(groupId) && incl.getControlId().equals(controlId)){
                incl.setLastRequest(command);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the inclusions for the given device.
     * @param deviceId
     * @return 
     */
    public final List<DeviceSceneInclusion> getDeviceDependencies(int deviceId){
        List<DeviceSceneInclusion> inclusions = new ArrayList<>();
        Iterator<DeviceSceneInclusion> current = this.deviceInclusions.iterator();
        while(current.hasNext()){
            DeviceSceneInclusion incl = current.next();
            if(incl.getDeviceId()==deviceId){
                inclusions.add(incl);
            }
        }
        return inclusions;
    }
    
    /**
     * Sets the scene's dependencies.
     * @param dependencies the JSON-RPC String.
     */
    protected void setDependencies(ArrayList dependencies){
        this.dependenciesPlane = dependencies;
        deviceInclusions.clear();
        if(dependencies.isEmpty()){
            LOG.info("There are no actions to be added for scene: {}.", this.sceneName);
        } else {
            for(int i = 0; i < dependencies.size(); i++){
                Map<String,Object> exec = (Map<String,Object>)dependencies.get(i);
                switch((String)exec.get("itemtype")){
                    case "device":
                        LOG.debug("Adding device to scene: {}", exec);
                        DeviceSceneInclusion inclu = new DeviceSceneInclusion(this,
                                                                              ((Number)exec.get("deviceid")).intValue(), 
                                                                              (String)exec.get("group"), 
                                                                              (String)exec.get("command"));
                        Map<String,Object> execHandle = new HashMap<>();
                        execHandle.put("value", exec.get("value"));
                        execHandle.put("extra", exec.get("extra"));
                        inclu.createActivateCommand(execHandle);
                        deviceInclusions.add(inclu);
                    break;
                }
            }
        }
    }

    /**
     * Returns the locations as strings from the dependency list.
     * @return 
     */
    public final ArrayList<String> getStringLocationFromDependencies(){
        ArrayList<String> locationSet = new ArrayList<>();
        for(DeviceSceneInclusion inclu:this.deviceInclusions){
            try {
                DeviceInterface device = DeviceService.getDevice(inclu.getDeviceId());
                if(!locationSet.contains(device.getLocationName())){
                    locationSet.add(device.getLocationName());
                }
            } catch (UnknownDeviceException ex) {
                LOG.error("Device not found for location retrieval");
            }
        }
        return locationSet;
    }
    
    /**
     * Returns the location IDs from the dependency list.
     * @return 
     */
    public final ArrayList<Integer> getLocationIdsFromDependencies(){
        ArrayList<Integer> locationSet = new ArrayList<>();
        for(DeviceSceneInclusion inclu:this.deviceInclusions){
            try {
                DeviceInterface device = DeviceService.getDevice(inclu.getDeviceId());
                if(!locationSet.contains(device.getLocationId())){
                    locationSet.add(device.getLocationId());
                }
            } catch (UnknownDeviceException ex) {
                LOG.error("Device not found for location retrieval");
            }
        }
        return locationSet;
    }
    
    /**
     * Returns the scenes dependencies.
     * @return MApping containing the devices included.
     */
    public final List<DeviceSceneInclusion> getDependencies(){
        return deviceInclusions;
    }
    
    /**
     * Returns the scenes dependencies.
     * @return MApping containing the devices included.
     */
    public final ArrayList getPlainDependenciesArrayList(){
        return dependenciesPlane;
    }
    
}