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

package org.pidome.client.entities.remotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;

/**
 *
 * @author John
 */
public class PiDomeRemote {
    
    private String remoteName;
    private String remoteDescription;
    private int remoteId;
    private int sendDevice = 0;
    
    private PCCConnectionInterface connection;
    
    ArrayList<PiDomeRemoteSection> sections = new ArrayList<>();
    
    public PiDomeRemote(PCCConnectionInterface con, Map<String,Object> remote){
        connection = con;
        remoteName = (String)remote.get("name");
        remoteId = ((Long)remote.get("id")).intValue();
        remoteDescription = (String)remote.get("description");
        try {
            setConstruct();
        } catch (PiDomeRemotesException ex) {
            
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
                connection.getJsonHTTPRPC("RemotesService.pressButton", params, "RemotesService.pressButton");
            } catch (PCCEntityDataHandlerException ex) {
                Logger.getLogger(PiDomeRemote.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public final boolean hasSendDevice(){
        return sendDevice!=0;
    }
    
    protected final void destroy(){
        this.connection = null;
    }
    
    private void setConstruct() throws PiDomeRemotesException {
        if(sections.isEmpty()){
            Map<String,Object>params = new HashMap<>();
            params.put("id", remoteId);
            try {
                Map<String,Object> remoteData = connection.getJsonHTTPRPC("RemotesService.getRemote", params, "RemotesService.getRemote").getResult();
                if(remoteData.containsKey("data")){
                    ArrayList<Map<String,Object>> sectionList = (ArrayList<Map<String,Object>>)((Map<String,Object>)((Map<String,Object>)remoteData.get("data")).get("remotevisuals")).get("sections");
                    for(Map<String,Object> sectionData: sectionList){
                        sections.add(new PiDomeRemoteSection(sectionData));
                    }
                } else {
                    throw new PiDomeRemotesException("Remote visual details unavailable");
                }
            } catch (PCCEntityDataHandlerException ex) {
                throw new PiDomeRemotesException("Remote details unavailable");
            }
        }
    }
    
}
