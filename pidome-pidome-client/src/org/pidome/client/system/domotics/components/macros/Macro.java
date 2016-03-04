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

package org.pidome.client.system.domotics.components.macros;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopBase;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopIcon;
import org.pidome.client.system.scenes.components.mainstage.desktop.DraggableIconInterface;

/**
 *
 * @author John
 */
public final class Macro implements ClientDataConnectionListener,DraggableIconInterface {

    DesktopIcon desktopIcon;
    
    String name = "";
    String description = "";
    boolean isFavorite = false;
    int hasExecutionItems = 0;
    int id = 0;
    
    static Logger LOG = LogManager.getLogger(Macros.class);
    
    public Macro(Map<String,Object> macro){
        updateInfo(macro);
        ClientData.addClientDataConnectionListener(this);
        handleShortCut();
    }
    
    public final void updateInfo(Map<String,Object> macro){
        name = (String)macro.get("name");
        description = (String)macro.get("description");
        isFavorite = (boolean)macro.get("favorite");
        hasExecutionItems = ((Long)macro.get("executions")).intValue();
        id = ((Long)macro.get("id")).intValue();
    }
    
    public final Map<String,Object> getMacroInfoMap(){
        Map<String,Object> info = new HashMap<>();
        info.put("description", description);
        info.put("name", name);
        info.put("executions", hasExecutionItems);
        info.put("favorite", isFavorite);
        info.put("id", id);
        return info;
    }
    
    public final String getName(){
        return this.name;
    }
    
    public final boolean isFavorite(){
        return this.isFavorite;
    }
    
    public final String getDescription(){
        return this.description;
    }
    
    public final int getId(){
        return this.id;
    }
    
    final void handleShortCut(){
        handleShortCut(false);
    }
    
    final void handleShortCut(boolean removed){
        if (!removed && isFavorite() && desktopIcon==null) {
            ArrayList macroData = new ArrayList();
            macroData.add(String.valueOf(id));
            desktopIcon = new DesktopIcon(this, DesktopIcon.MACRO, "Macro: " + name, "org.pidome.client.system.scenes.components.mainstage.desktop.HandleMacroShortcut", macroData);
            DesktopBase.addDesktopIcon(desktopIcon);
        } else if ((!isFavorite() || removed) && desktopIcon!=null) {
            DesktopBase.removeDesktopIcon(desktopIcon);
            desktopIcon = null;
        }
    }
    
    @Override
    public void iconAdded() {
        Map<String,Object> serverParams = new HashMap<>();
        serverParams.put("id", getId());
        serverParams.put("favorite", true);
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("MacroService.setFavorite", "MacroService.setFavorite", serverParams));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send macro favorite:false");
        }
    }
    
    @Override
    public void iconRemoved() {
        Map<String,Object> serverParams = new HashMap<>();
        serverParams.put("id", getId());
        serverParams.put("favorite", false);
        try {
            ClientData.sendData(PidomeJSONRPC.createExecMethod("MacroService.setFavorite", "MacroService.setFavorite", serverParams));
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not send macro favorite:false");
        }
    }

    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.MCRRECEIVED)){
            LOG.debug("Got data: {}", event.getData());
            Map<String,Object> data = (Map<String,Object>)event.getData();
            if(((Long)data.get("id")).intValue() == id){
                switch(event.getMethod()){
                    case "setFavorite":
                        isFavorite = (boolean)data.get("favorite");
                        handleShortCut(false);
                    break;
                }
            }
        }
    }
    
    public final void destroy(){
        handleShortCut(true);
        ClientData.removeClientDataConnectionListener(this);
    }
    
}
