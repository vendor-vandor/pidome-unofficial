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

package org.pidome.client.system.domotics.components.remotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.DomResourceException;
import org.pidome.client.system.domotics.components.devices.Devices;

/**
 *
 * @author John
 */
public class PiDomeRemote {
    
    private String remoteName;
    private String remoteDescription;
    private int remoteId;
    private int sendDevice = 0;
    
    ArrayList<PiDomeRemoteSection> sections = new ArrayList<>();
    
    DomComponents dom;
    
    static Logger LOG = LogManager.getLogger(PiDomeRemote.class);
    
    public PiDomeRemote(DomComponents dom, Map<String,Object> remote){
        this.dom = dom;
        remoteName = (String)remote.get("name");
        remoteId = ((Long)remote.get("id")).intValue();
        remoteDescription = (String)remote.get("description");
        try {
            setConstruct();
        } catch (PiDomeRemotesException ex) {
            LOG.error("Could not get remote details: {}", ex.getMessage());
        }
    }
    
    public final String getName(){
        return this.remoteName;
    }
    
    public final String getDescription(){
        return this.remoteDescription;
    }
    
    public final int getId(){
        return this.remoteId;
    }
    
    public final ArrayList<PiDomeRemoteSection> getConstruct(){
        return this.sections;
    }
    
    public final void sendRemoteSignal(String id){
        if(hasSendDevice()){
            Map<String,Object>params = new HashMap<>();
            params.put("id", remoteId);
            params.put("button", id);
            try {
                dom.getJSONData("RemotesService.pressButton", params).getResult();
            } catch (DomResourceException ex) {
                LOG.error("Could not press button {} of remote: {}", id, remoteId);
            }
        }
    }
    
    public final boolean hasSendDevice(){
        return sendDevice!=0;
    }
    
    private void setConstruct() throws PiDomeRemotesException {
        if(sections.isEmpty()){
            Map<String,Object>params = new HashMap<>();
            params.put("id", remoteId);
            try {
                Map<String,Object> remoteData = dom.getJSONData("RemotesService.getRemote", params).getResult();
                if(remoteData.containsKey("data")){
                    int proposedDevice = ((Long)((Map<String,Object>)remoteData.get("data")).get("sendtestdevice")).intValue();
                    try {
                        Devices.getDeviceById(proposedDevice);
                        sendDevice = proposedDevice;
                    } catch (DomComponentsException ex) {
                        LOG.warn("Remote id {} has no active send device", remoteId);
                    }
                    ArrayList<Map<String,Object>> sectionList = (ArrayList<Map<String,Object>>)((Map<String,Object>)((Map<String,Object>)remoteData.get("data")).get("remotevisuals")).get("sections");
                    sectionList.stream().forEach((sectionData) -> {
                        sections.add(new PiDomeRemoteSection(sectionData));
                    });
                } else {
                    throw new PiDomeRemotesException("Remote visual details unavailable");
                }
            } catch (DomResourceException ex) {
                throw new PiDomeRemotesException("Remote details unavailable");
            }
        }
    }
    
}
