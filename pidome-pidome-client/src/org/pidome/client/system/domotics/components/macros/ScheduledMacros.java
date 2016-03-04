/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.client.system.domotics.components.macros;

import org.pidome.client.system.domotics.components.system.SysStateEventListener;
import org.pidome.client.system.domotics.components.system.SysStateEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.client.system.client.data.ClientData;
import org.pidome.client.system.client.data.ClientDataConnectionEvent;
import org.pidome.client.system.client.data.ClientDataConnectionListener;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.DomComponent;

/**
 *
 * @author John Sirach
 */
public class ScheduledMacros implements DomComponent,ClientDataConnectionListener {

    static Map<String,Map<String,String>> events = new HashMap<>();
    static Map<String,Map<String,String>> sysEvents = new HashMap<>();
    
    static StringProperty curSysStateId = new SimpleStringProperty("0");
    
    static Logger LOG = LogManager.getLogger(ScheduledMacros.class);
    
    static List _sysStateChangedListeners = new ArrayList();
    
    public ScheduledMacros(){
        curSysStateId.addListener((ObservableValue<? extends String> ov, final String t, final String t1) -> {
            if (!t.equals(t1)) {
                if(sysEvents.containsKey(t)) sysEvents.get(t).put("active", "false");
                if(sysEvents.containsKey(t1)) sysEvents.get(t1).put("active", "true");
                Platform.runLater(() -> {
                    _fireSysStateChangeEvent(SysStateEvent.SYSSTATECHANGED, t1);
                });
            }
        });
    }
    
    public final void createMacro(Map<String,String> info){
        if(info.containsKey("eventtype") && info.get("eventtype").equals("sysstate")){
            createSysMacro(info);
        }
    }

    public final void createSystemMacro(Map<String,String> info){
        createSysMacro(info);
    }
    
    final void createSysMacro(Map<String,String> event){
        LOG.trace("Creating system event id: {}, {}",event.get("id"), event);
        sysEvents.put(event.get("id"), event);
        _fireSysStateChangeEvent(SysStateEvent.SYSSTATELISTADDED, event.get("id"));
        if(event.containsKey("active") && event.get("active").contains("true")){
            curSysStateId.setValue(event.get("id"));
        }
    }
    
    public static Map<String,Map<String,String>> getEvents(){
        return events;
    }
    
    public static String getCurSysState(){
        return curSysStateId.getValue();
    }
    
    public static Map<String,String> getMacro(String id) throws DomComponentsException {
        if(events.containsKey(id)){
            return events.get(id);
        } else {
            LOG.error("Macro id {} does not exist", id);
            throw new DomComponentsException("Macro id " + id + " does not exist");
        }
    }

    public static Map<String,Map<String,String>> getSysEvents(){
        return sysEvents;
    }
    
    public static Map<String,String> getSysMacro(String id) throws DomComponentsException {
        if(sysEvents.containsKey(id)){
            return sysEvents.get(id);
        } else {
            LOG.error("Event id {} does not exist", id);
            throw new DomComponentsException("Event id " + id + " does not exist");
        }
    }
    
    public static String getSysMacroCommand(String eventId) throws DomComponentsException {
        if(sysEvents.containsKey(eventId)){
            return sysEvents.get(eventId).get("cmd");
        } else {
            LOG.error("Event id {} does not exist", eventId);
            throw new DomComponentsException("Event id " + eventId + " does not exist");
        }
    }
    
    public final void startMacros(){
        ClientData.addClientDataConnectionListener(this);
        LOG.debug("Started dispatcher");
    }

    public static synchronized void addSysStateMacroListener(SysStateEventListener l){
        LOG.debug("Added systate event listener {}", l.getClass().getName());
        _sysStateChangedListeners.add(l);
    }

    public static synchronized void removeSysStateMacroListener(SysStateEventListener l){
        LOG.debug("Removed systate event listener {}", l.getClass().getName());
        _sysStateChangedListeners.remove(l);
    }
    
    private synchronized void _fireSysStateChangeEvent(String EVENTTYPE, Object data) {
        LOG.debug("Event: {}", EVENTTYPE);
        SysStateEvent event = new SysStateEvent(this, EVENTTYPE);
        event.setData(data);
        Iterator listeners = _sysStateChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((SysStateEventListener) listeners.next()).handleSysteStateEvent(event);
        }
    }

    private synchronized void _fireMacroChangeEvent(String EVENTTYPE, Object data) {
        LOG.debug("Event: {}", EVENTTYPE);
        SysStateEvent event = new SysStateEvent(this, EVENTTYPE);
        event.setData(data);
        Iterator listeners = _sysStateChangedListeners.iterator();
        while (listeners.hasNext()) {
            ((SysStateEventListener) listeners.next()).handleSysteStateEvent(event);
        }
    }
    
    @Override
    public void handleClientDataConnectionEvent(ClientDataConnectionEvent event) {
        if(event.getEventType().equals(ClientDataConnectionEvent.MCRRECEIVED)){
            LOG.debug("Got data: {}", event.getData());
            String[] dataSet = (String[])event.getData();
            try {
                if(dataSet[0].equals("MCR")){
                    if(dataSet[1].equals("SYSSTATE")){
                        if(!dataSet[2].isEmpty()){
                            if(sysEvents.containsKey(dataSet[2])){
                                LOG.debug("Set system state id: {}", dataSet[2]);
                                curSysStateId.setValue(dataSet[2]);
                            } else {
                                LOG.error("I do not know of SYSSTATE id: {}", dataSet[2]);
                            }
                        } else {
                            LOG.error("SYSSTATE ID can not be empty");
                        }
                    } else {
                        LOG.error("Only SYSSTATE is supported");
                    }
                } else {
                    LOG.error("Not an MCR type");
                }
            } catch (Exception ex){
                LOG.error("Problem parsing data set: {}", dataSet.toString());
            }
        }
    }
    
}
