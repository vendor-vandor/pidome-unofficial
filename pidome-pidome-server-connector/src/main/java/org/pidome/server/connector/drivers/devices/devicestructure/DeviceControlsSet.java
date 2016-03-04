/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices.devicestructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;

/**
 * A collection of device controls.
 * @author John
 */
public final class DeviceControlsSet {

    static Logger LOG = LogManager.getLogger(DeviceControlsSet.class);
    List<IntervalCommand> receiverCollection = new ArrayList();
    Map<String,DeviceControlsGroup> groupSet = new HashMap<>();
    
    private final String canonicalBaseName;
    
    /**
     * Constructor.
     * By giving it the correct command set xml node it can create the complete device controls structure.
     * @param canonicalBaseName
     * @param groups
     * @throws UnsupportedDeviceException 
     */
    public DeviceControlsSet(String canonicalBaseName, List<Map<String,Object>> groups) throws UnsupportedDeviceException {
        this.canonicalBaseName = canonicalBaseName;
        setCollectionControlsSet(groups);
    }

    /**
     * Returns a string representing the canonical and basename joined together.
     * @return 
     */
    public final String getCanonicalBaseName(){
        return this.canonicalBaseName;
    }
    
    /**
     * Sets the command collections for the device.
     *
     * @param xml
     * @throws UnsupportedDeviceException
     */
    void setCollectionControlsSet(List<Map<String,Object>> groups) throws UnsupportedDeviceException {
        try {
            for(Map<String,Object> group: groups){
                if(group.containsKey("id") && group.containsKey("label") && group.containsKey("controls")){
                    DeviceControlsGroup createdGroupSet = new DeviceControlsGroup((String)group.get("id"),(String)group.get("label"));
                    if(group.containsKey("hidden") && (boolean)group.get("hidden")==true){
                        createdGroupSet.setHidden();
                    }
                    createdGroupSet.setGroupContents((List<Map<String,Object>>)group.get("controls"));
                    this.groupSet.put(createdGroupSet.getGroupId(), createdGroupSet);
                }
            }
        } catch (Exception ex) {
            LOG.error("This should not happen", ex);
            throw new UnsupportedDeviceException(ex);
        }
    }

    /**
     * Links groups to devices.
     * @param device 
     */
    public final void setDevice(Device device){
        for(DeviceControlsGroup group:getControlsGroups().values()){
            group.setDevice(device);
        }
    }
    
    /**
     * Returns the group by group id.
     * @param groupId
     * @return
     * @throws DeviceControlsGroupException 
     */
    public final DeviceControlsGroup getControlsGroup(String groupId) throws DeviceControlsGroupException {
        if(this.groupSet.containsKey(groupId)){
            return this.groupSet.get(groupId);
        } else {
            throw new DeviceControlsGroupException("Group '"+groupId+"' does not exist");
        }
    }
    
    /**
     * Returns all the device control groups.
     * @return 
     */
    public final Map<String,DeviceControlsGroup> getControlsGroups(){
        return this.groupSet;
    }
    
    /**
     * Returns all the device control groups.
     * @return 
     */
    public final DeviceControlsGroup[] getControlsGroupsAsList(){
        List<DeviceControlsGroup> list = new ArrayList<>(getControlsGroups().values());
        return list.toArray(new DeviceControlsGroup[list.size()]);
    }
    
    /**
     * Returns a list of data receiver sets and commands.
     * @return 
     */
    public final List<IntervalCommand> getReceiverSet(){
        return receiverCollection;
    }
    
    /**
     * Class used for interval commands.
     */
    public class IntervalCommand {
        
        private final String groupName;
        private final String setName;
        private final String command;
        private final int interval;
        private final String dataType;
        
        public IntervalCommand(String groupName, String setName, String command, String dataType, int interval){
            this.groupName = groupName;
            this.setName   = setName;
            this.command   = command;
            this.interval  = interval;
            this.dataType  = dataType;
        }
        
        public String getGroupName(){
            return this.groupName;
        }
        
        public String getSetName(){
            return this.setName;
        }
        
        public String getCommand(){
            return this.command;
        }
        
        public int getInterval(){
            return this.interval;
        }
        
        public String getDataType(){
            return this.dataType;
        }
        
    }
    
    
}
