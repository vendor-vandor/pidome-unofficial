/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.connector.drivers.devices;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating a device notification to be send out to be passed to the server.
 * @author John
 */
public final class DeviceNotification {
    
    private final List<GroupData> messageStruct = new ArrayList<>();
    
    public final void addData(String groupId, String controlId, Object value){
        addData(groupId, controlId, value, false);
    }
    
    /**
     * Adds data to the notification.
     * @param groupId
     * @param controlId
     * @param value 
     * @param forcedSend 
     */
    public final void addData(String groupId, String controlId, Object value, boolean forcedSend){
        boolean found = false;
        ControlData controlData = new ControlData(controlId, value, forcedSend);
        for (GroupData messageStructSet : messageStruct) {
            if (messageStructSet.getId().equals(groupId)) {
                found = true;
                messageStructSet.addControlData(controlData);
            }
        }
        if(!found){
            GroupData group = new GroupData(groupId);
            group.addControlData(controlData);
            messageStruct.add(group);
        }
    }
    
    /**
     * Returns the structure.
     * @return 
     */
    public final List<GroupData> getStruct(){
        return messageStruct;
    }
    
    /**
     * Class for group data.
     */
    public final class GroupData {
        
        /**
         * Group Id.
         */
        private final String id;
        
        /**
         * Control data.
         */
        List<ControlData> controlData = new ArrayList<>();
        
        /**
         * Constructor.
         * @param id 
         */
        private GroupData(String id){
            this.id = id;
        }

        /**
         * Adds control data.
         * @param control 
         */
        private void addControlData(ControlData control){
            boolean put = true;
            for(int i = 0; i < controlData.size(); i++){
                if(controlData.get(i).getId().equals(control.getId())){
                    put = false;
                    break;
                }
            }
            if(put){
                controlData.add(control);
            }
        }
        
        /**
         * Returns the group id.
         * @return 
         */
        public final String getId(){
            return this.id;
        }
        
        /**
         * Returns all the controls data.
         * @return 
         */
        public final List<ControlData> getControlData(){
            return controlData;
        }
        
    }
    
    /**
     * Holds control data.
     */
    public final class ControlData {
        
        private final String controlId;
        private final Object value;
        private final boolean forced;
        
        public ControlData(String id, Object value, boolean forced){
            this.controlId = id;
            this.value = value;
            this.forced = forced;
        }
        
        /**
         * Returns the id.
         * @return 
         */
        public final String getId(){
            return this.controlId;
        }
        
        /**
         * Returns the value.
         * @return 
         */
        public final Object getValue(){
            return this.value;
        }
        
        /**
         * Returns if the value should be forced to be send.
         * @return 
         */
        public final boolean getForced(){
            return this.forced;
        }
        
    }
    
}