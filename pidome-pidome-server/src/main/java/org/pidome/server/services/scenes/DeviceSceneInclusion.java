/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.scenes;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.system.hardware.devices.DeviceStruct;

/**
 *
 * @author John
 */
public final class DeviceSceneInclusion {
    
    private int deviceId;
    private final String group;
    private final String control;
 
    Runnable lastRequest;
    Runnable sceneActivation;
    
    private ServerScene scene;
    
    private Map<String,Object> action;
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DeviceSceneInclusion.class);
    
    /**
     * Initializes scene inclusion for a single control.
     * @param scene
     * @param deviceId
     * @param groupId
     * @param controlId 
     */
    protected DeviceSceneInclusion(ServerScene scene, int deviceId, String groupId, String controlId){
        this.deviceId = deviceId;
        this.group    = groupId;
        this.control  = controlId;
        this.scene    = scene;
    }
    
    /**
     * Returns the active scene id this registration belongs to.
     * @return 
     */
    public final int getDeviceId(){
        return this.deviceId;
    }
    
    /**
     * Checks if a control is part of a scene.
     * @param groupId
     * @param controlId
     * @return 
     */
    public final boolean isPartOfaScene(String groupId, String controlId){
        return (groupId.equals(this.group) && controlId.equals(this.control));
    }
    
    /**
     * Returns the group id.
     * @return 
     */
    public final String getGroupId(){
        return this.group;
    }
    
    /**
     * Returns the control id.
     * @return 
     */
    public final String getControlId(){
        return this.control;
    }
    
    /**
     * Registers the last command.
     * @param lastRequest the last known command request for the group and control id combo.
     */
    protected final void setLastRequest(Runnable lastRequest){
        this.lastRequest = lastRequest;
    }
    
    /**
     * Executes the last known command
     */
    protected final void runInclusionDeActivate(){
        try {
            ((DeviceStruct)DeviceService.getDevice(deviceId)).setScene(null);
        } catch (UnknownDeviceException ex) {
            LOG.error("Could not unset the scene from device '{}'", deviceId);
        }
        if(this.lastRequest!=null) this.lastRequest.run();
    }
    
    /**
     * Creates the command to be ran when this inclusion is added.
     * @param action 
     */
    protected final void createActivateCommand(Map<String,Object> action){
        this.action = action;
    }
    
    /**
     * Runs the command when this inclusion is active.
     */
    protected final void runInclusionActivate(){
        if(action!=null){
            try {
                DeviceStruct device = ((DeviceStruct)DeviceService.getDevice(deviceId));
                device.setScene(this.scene);
                try {
                    this.lastRequest = device.getLastDeviceCommand(this.group, this.control);
                } catch (UnsupportedDeviceCommandException ex) {
                    LOG.error("Could not set the last known command for '{}': {}", device.getDeviceName(), ex.getMessage(), ex);
                }
            } catch (UnknownDeviceException ex) {
                LOG.error("Unknown device requested: {}", ex.getMessage(), ex);
            }
            if(sceneActivation == null){
                try {
                    DeviceCommandRequest activateSceneCommand = ((DeviceStruct)DeviceService.getDevice(deviceId)).createDeviceExecCommand(group, control, action);
                    sceneActivation = ((DeviceStruct)DeviceService.getDevice(deviceId)).getDeviceCommandExecution(activateSceneCommand);
                } catch (UnknownDeviceException | DeviceControlsGroupException | DeviceControlException ex) {
                    LOG.error("Could not compose action for scene activation: {}", ex.getMessage(), ex);
                }
            }
            if(sceneActivation!=null){
                sceneActivation.run();
            }
        }
    }
    
}