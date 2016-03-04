/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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
package org.pidome.server.services.macros;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.ServiceEvent;
import org.pidome.server.services.ServiceEventListener;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.config.ConfigException;

/*
 * handles all the occuring events. timed and triggered
 */
public class MacroService implements ServiceInterface {

    static MacroDB db = new MacroDB();
    
    private static MacroService myInstance;

    static Logger LOG = LogManager.getLogger(MacroService.class);
    
    static PropertyChangeSupport propListeners;
    
    static boolean active = false;
    
    static Map<Integer,Macro> macroList = new HashMap<>();
    
    static List<ServiceEventListener> _listeners = new ArrayList<>();
    
    Map<String, Object> eventDetails = new HashMap<>();
    
    public MacroService() throws ConfigException {
        eventDetails.put("serviceName", "MacroService");
        propListeners = new PropertyChangeSupport(this);
        try {
            reloadMacros();
            propListeners.firePropertyChange("macrosAmount", 0, macroList.size());
        } catch (MacroException ex) {
            throw new ConfigException("Check all parameters, macros loading failed: " + ex.getMessage());
        }
    }
    
    /**
     * Fires a device service event.
     * @param EVENTTYPE
     * @param eventDetails 
     */
    private synchronized void _fireServiceEvent(String EVENTTYPE, Map<String,Object>eventDetails) {
        ServiceEvent serviceEvent = new ServiceEvent(this, EVENTTYPE);
        serviceEvent.setDetails(eventDetails);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (ServiceEventListener) listeners.next() ).handleServiceEvent( serviceEvent );
        }
    }
    
    /**
     * (Re)loads all macros.
     */
    final void reloadMacros() throws MacroException {
        macroList.clear();
        try {
            Map<Integer, Map<String, Object>> storedMacros = db.getMacros();
            for(int key:storedMacros.keySet()){
                macroList.put(key, new Macro(key,
                        (String) storedMacros.get(key).get("name"),
                        (String) storedMacros.get(key).get("description"),
                        (boolean) storedMacros.get(key).get("isfavorite"),
                        (ArrayList) storedMacros.get(key).get("executes")));
            }
        } catch (SQLException ex) {
            LOG.error("Could not load macros: {}", ex.getMessage());
            throw new MacroException("Macro's could not be loaded: " + ex.getMessage());
        }
    }
    
    /**
     * Adds a listener.
     * @param l 
     */
    public static void addEventListener(ServiceEventListener l){
        if(!_listeners.contains(l)) _listeners.add(l);
    }
    
    /**
     * Removes a listener.
     * @param l 
     */
    public static void removeEventListener(ServiceEventListener l){
        if(_listeners.contains(l)) _listeners.remove(l);
    }
    
    /**
     * Returns the macro instance.
     * @return 
     */
    public static MacroService getInstance(){
        return myInstance;
    }
    
    /**
     * Add a properties listener
     * @param propName
     * @param l 
     */
    public static void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        propListeners.addPropertyChangeListener(propName, l);
    }

    /**
     * Removes a properties listener
     * @param propName
     * @param l 
     */
    public static void removePropertyChangeListener(String propName, PropertyChangeListener l) {
        propListeners.removePropertyChangeListener(propName, l);
    }
    
    /**
     * Updates a trigger.
     * An update is reflected immediately.
     * @param macroId
     * @param name
     * @param description 
     * @param favorite 
     * @param exec 
     * @return  
     * @throws org.pidome.server.services.macros.MacroException  
     */
    public static boolean saveMacro(final int macroId, String name, String description, boolean favorite, ArrayList exec) throws MacroException {
        try {
            macroList.get(macroId).edit(true);
            db.saveMacro(macroId, name, description, favorite, exec);
            macroList.put(macroId, new Macro(macroId, name, description, favorite, exec));
            macroList.get(macroId).edit(false);
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", macroId);
                }
            };
            ClientMessenger.send("MacroService", "updateMacro", 0, sendObject);
            return true;
        } catch (NullPointerException | SQLException | IOException ex) {
            throw new MacroException("Could not update macro: " + ex.getMessage());
        }
    }

    /**
     * Saves a new macro.
     * A new macro is immediately active.
     * @param name
     * @param description 
     * @param favorite 
     * @param exec 
     * @return 
     * @throws org.pidome.server.services.macros.MacroException 
     */
    public static int saveMacro(String name, String description, boolean favorite, ArrayList exec) throws MacroException {
        try {
            final int macroId = db.saveMacro(0, name, description, favorite, exec);
            macroList.put(macroId, new Macro(macroId, name, description, favorite, exec));
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", macroId);
                }
            };
            ClientMessenger.send("MacroService", "addMacro", 0, sendObject);
            return macroId;
        } catch (SQLException | IOException ex) {
            throw new MacroException("Could not save macro: " + ex.getMessage());
        }
    }
    
    /**
     * Set macro favorite or not.
     * A new trigger is immediately active.
     * @param macroId
     * @param favorite
     * @return 
     * @throws org.pidome.server.services.macros.MacroException 
     */
    public static boolean setFavorite(final int macroId, final boolean favorite) throws MacroException {
        try {
            db.setFavorite(macroId, favorite);
            getMacro(macroId).setFavorite(favorite);
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", macroId);
                    put("favorite", favorite);
                }
            };
            ClientMessenger.send("MacroService", "setFavorite", 0, sendObject);
            return true;
        } catch (SQLException ex) {
            throw new MacroException("Could not save macro: " + ex.getMessage());
        }
    }
    
    /**
     * Deletes a macro from the database and unloads.
     * Triggers are when removed immediately unlinked
     * @param macroId
     * @return 
     * @throws org.pidome.server.services.macros.MacroException 
     */
    public static boolean deleteMacro(final int macroId) throws MacroException {
        try {
            macroList.get(macroId).edit(true);
            db.deleteMacro(macroId);
            macroList.remove(macroId);
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", macroId);
                }
            };
            ClientMessenger.send("MacroService", "deleteMacro", 0, sendObject);
            return true;
        } catch (NullPointerException | SQLException ex) {
            throw new MacroException("Could not delete macro: " + ex.getMessage());
        }
    }
    
    /**
     * Returns a single Macro.
     * @param id
     * @return
     * @throws MacroException 
     */
    public static Macro getMacro(int id) throws MacroException {
        if(macroList.containsKey(id)){
            return macroList.get(id);
        } else {
            throw new MacroException("Macro id "+id+" does not exist");
        }
    }
    
    /**
     * Should only be used from the console, it runs events based on given time
     * or event id
     *
     * @param macroId
     * @return boolean
     */
    public static boolean runMacro(final int macroId) {
        if (active) {
            if (macroList.containsKey(macroId)) {
                LOG.debug("MACROSERVICE: Running macro id '"+macroId);
                Map<String, Object> sendObject = new HashMap<String, Object>() {
                    {
                        put("id", macroId);
                    }
                };
                ClientMessenger.send("MacroService", "runMacro", 0, sendObject);
                macroList.get(macroId).execute();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * Returns a list of macro's
     * @return 
     */
    public static Map<Integer, Macro>getMacrosList(){
        return macroList;
    }
    
    @Override
    public void interrupt() {
        active = false;
        _fireServiceEvent(ServiceEvent.SERVICEUNAVAILABLE, eventDetails);
    }

    @Override
    public void start() {
        active = true;
        _fireServiceEvent(ServiceEvent.SERVICEAVAILABLE, eventDetails);
    }

    @Override
    public boolean isAlive() {
        return active;
    }
    
    @Override
    public String getServiceName() {
        return "Macro service";
    }
    
}
