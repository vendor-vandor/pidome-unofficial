/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.macros;

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
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponents;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.DomResourceException;
import org.pidome.client.system.domotics.components.DomComponent;
import org.pidome.client.system.rpc.PidomeJSONRPC;
import org.pidome.client.system.rpc.PidomeJSONRPCException;
import org.pidome.client.system.scenes.components.mainstage.desktop.DesktopIcon;

/**
 *
 * @author John Sirach
 */
public class Macros implements DomComponent,ClientDataConnectionListener {

    static Map<Integer,Macro> macros = new HashMap<>();
    ObservableMap<Integer,Macro> observableMacros = FXCollections.observableMap(macros);
    
    static Map<Integer, DesktopIcon> iconList = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(Macros.class);
    
    static List<MacroStateEventListener> _macroStateChangedListeners = new ArrayList();
    
    DomComponents dom;
    
    public Macros(DomComponents dom, ArrayList<Map<String,Object>> macros){
        this.dom = dom;
        startMacroChangeListener();
        macros.stream().forEach((macro) -> {
            createMacro(macro);
        });
        startMacros();
    }
    
    public final void createMacro(Map<String,Object> macro){
        observableMacros.put(((Long)macro.get("id")).intValue(), new Macro(macro));
    }
    
    public static Map<Integer,Macro> getMacros(){
        return macros;
    }
    
    final boolean isFavorite(int macroId){
        if(observableMacros.containsKey(macroId)){
            return (boolean)observableMacros.get(macroId).isFavorite();
        }
        return false;
    }

    public static Macro getMacro(int id) throws DomComponentsException {
        if(macros.containsKey(id)){
            return macros.get(id);
        } else {
            LOG.error("Macro id {} does not exist", id);
            throw new DomComponentsException("Macro id " + id + " does not exist");
        }
    }
    
    public static String getMacroCommand(final int macroId) throws DomComponentsException {
        if(macros.containsKey(macroId)){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", macroId);
                }
            };
            try {
                return PidomeJSONRPC.createExecMethod("MacroService.runMacro", "MacroService.runMacro", sendObject);
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Could not send data: {}", sendObject);
                throw new DomComponentsException("Could not create macro command for: " + macroId);
            }
        } else {
            LOG.error("Macro id {} does not exist", macroId);
            throw new DomComponentsException("Macro id " + macroId + " does not exist");
        }
    }
    
    public final void startMacros(){
        ClientData.addClientDataConnectionListener(this);
        LOG.debug("Started dispatcher");
    }

    final void startMacroChangeListener(){
        observableMacros.addListener((MapChangeListener.Change<? extends Integer, ? extends Macro> change) -> {
            if (change.wasRemoved() && change.wasAdded()){
                _fireMacroChangeEvent(MacroStateEvent.MACROUPDATED, change.getKey(), change.getValueAdded().getMacroInfoMap());
            } else if (change.wasRemoved()){
                _fireMacroChangeEvent(MacroStateEvent.MACROREMOVED, change.getKey(), change.getValueRemoved().getMacroInfoMap());
                change.getValueRemoved().destroy();
            } else if(change.wasAdded()){
                _fireMacroChangeEvent(MacroStateEvent.MACROADDED, change.getKey(), change.getValueAdded().getMacroInfoMap());
            }
        });
    }
    
    public static synchronized void addMacroListener(MacroStateEventListener l){
        LOG.debug("Added macro event listener {}", l.getClass().getName());
        _macroStateChangedListeners.add(l);
    }

    public static synchronized void removeMacroListener(MacroStateEventListener l){
        LOG.debug("Removed macro event listener {}", l.getClass().getName());
        _macroStateChangedListeners.remove(l);
    }

    private synchronized void _fireMacroChangeEvent(String EVENTTYPE, int id, Map value) {
        LOG.debug("Macro: {}", EVENTTYPE);
        MacroStateEvent event = new MacroStateEvent(this, EVENTTYPE);
        event.setData(value);
        Iterator listeners = _macroStateChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((MacroStateEventListener) listeners.next()).handleMacroStateEvent(event);
        }
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.MCRRECEIVED)){
            LOG.debug("Got data: {}", event.getData());
            Map<String,Object> data = (Map<String,Object>)event.getData();
            int macroId = ((Long)data.get("id")).intValue();
            switch(event.getMethod()){
                case "runMacro":
                    Map<String,Object> activeInfo = new HashMap<>();
                    activeInfo.put("id", macroId);
                    _fireMacroChangeEvent(MacroStateEvent.MACROACTIVE, macroId, activeInfo);
                break;
                case "addMacro":
                    createMacro(data);
                break;
                case "deleteMacro":
                    observableMacros.remove(macroId);
                break;
                case "updateMacro":
                    try {
                        if(observableMacros.containsKey(macroId)){
                            observableMacros.get(macroId).updateInfo((Map<String,Object>)this.dom.getJSONData("MacroService.getMacro", data).getResult().get("data"));
                            if(iconList.containsKey(macroId)){
                                iconList.get(macroId).updateName("Macro: " + observableMacros.get(macroId).getName());
                            }
                        }
                    } catch (DomResourceException ex) {
                        LOG.error("Could not update macro: {}", macroId);
                    }
                break;
            }
        }
    }
    
}
