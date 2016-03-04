/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.system.db.DB;

/**
 * Service for dispatching texts of events. Currently this service is being used
 * for custom created events which can be passed to the rpc service.
 *
 * @author John
 */
public class EventService implements ServiceInterface {

    /**
     * Logger.
     */
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(EventService.class);

    /**
     * List of events listeners.
     */
    private List<EventServiceListener> _listeners = new ArrayList<>();

    /**
     * List of custom events.
     */
    private List<CustomEvent> customEvents = new ArrayList<>();

    /**
     * If running boolean.
     */
    boolean isRunning = false;

    private static EventService me;
    
    private EventService(){}
    
    public static EventService getInstance(){
        if(me==null){
            me = new EventService();
        }
        return me;
    }
    
    /**
     * Interrupts/Stops the service.
     */
    @Override
    public void interrupt() {
        isRunning = false;
        _listeners.clear();
    }

    /**
     * Starts the service.
     */
    @Override
    public void start() {
        try {
            loadCustomEvents();
            isRunning = true;
        } catch (EventServiceException ex) {
            Logger.getLogger(EventService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if the service is alive.
     *
     * @return true when the service is running.
     */
    @Override
    public boolean isAlive() {
        return isRunning;
    }

    /**
     * Returns the service name
     *
     * @return returns EventService name.
     */
    @Override
    public String getServiceName() {
        return "EventService";
    }

    /**
     * Preloads custom events.
     *
     * @throws EventServiceException When events can not be loaded.
     */
    public final void loadCustomEvents() throws EventServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try (Statement statementEvents = fileDBConnection.createStatement()) {
                try (ResultSet rsEvents = statementEvents.executeQuery("SELECT * FROM customevents")) {
                    while (rsEvents.next()) {
                        CustomEvent event = new CustomEvent(rsEvents.getInt("id"), rsEvents.getString("identifier"));
                        event.setName(rsEvents.getString("name"));
                        event.setDescription(rsEvents.getString("description"));
                        event.setLastOccurrence(rsEvents.getString("last_occurrence"), rsEvents.getString("last_occurrence_remark"));
                        customEvents.add(event);
                    }
                } catch (Exception ex) {
                    LOG.error("Problem loading custom events list {}", ex.getMessage());
                    throw new EventServiceException("Could not load custom events: " + ex.getMessage());
                }
            } catch (Exception ex) {
                LOG.error("Problem loading custom events list {}", ex.getMessage());
                throw new EventServiceException("Could not load custom events: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading custom events list {}", ex.getMessage());
            throw new EventServiceException("Could not load custom events: " + ex.getMessage());
        }
    }

    /**
     * Updates a custom event.
     *
     * @param id the id of the custom event
     * @param name the name of the custom event
     * @param description the description of the custom event.
     * @return true if updating was successful.
     * @throws org.pidome.server.services.events.EventServiceException can not
     * be modified.
     */
    public final boolean updateCustomEvent(int id, String name, String description) throws EventServiceException {
        if(isAlive()){
            boolean result = false;
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE customevents SET 'name'=?,'description'=?, 'modified'=datetime('now') where id=?")) {
                    prep.setString(1, name);
                    prep.setString(2, description);
                    prep.setInt(3, id);
                    prep.executeUpdate();
                    CustomEvent event = getCustomEvent(id);
                    event.setName(name);
                    event.setDescription(description);
                    result = true;
                } catch (Exception ex) {
                    LOG.error("Problem updating custom event {}", ex.getMessage());
                    throw new EventServiceException("Could not update custom event: " + ex.getMessage());
                }
            } catch (SQLException ex) {
                LOG.error("Problem updating custom event {}", ex.getMessage());
                throw new EventServiceException("Could not update custom event: " + ex.getMessage());
            }
            return result;
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Updates a custom event.
     *
     * @param identifier the identifier of the custom event
     * @param name the name of the custom event
     * @param description the description of the custom event.
     * @return true if adding was successful.
     * @throws org.pidome.server.services.events.EventServiceException When an
     * event can not be added.
     */
    public final boolean addCustomEvent(String identifier, String name, String description) throws EventServiceException {
        if(isAlive()){
            boolean result = false;
            for (CustomEvent event : this.customEvents) {
                if (event.getIdentifier().equals(identifier)) {
                    throw new EventServiceException("Event identifier already exists, please use another one or delete the original one first");
                }
            }
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
                try (PreparedStatement prep = fileDBConnection.prepareStatement("INSERT INTO customevents (`identifier`,`name`,`description`) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    prep.setString(1, identifier);
                    prep.setString(2, name);
                    prep.setString(3, description);
                    prep.executeUpdate();
                    try (ResultSet rs = prep.getGeneratedKeys()) {
                        if (rs.next()) {
                            int insertId = rs.getInt(1);
                            CustomEvent event = new CustomEvent(insertId, identifier);
                            event.setName(name);
                            event.setDescription(description);
                            customEvents.add(event);
                        }
                    }
                    result = true;
                } catch (Exception ex) {
                    LOG.error("Problem updating custom event {}", ex.getMessage());
                    throw new EventServiceException("Could not update custom event: " + ex.getMessage());
                }
            } catch (SQLException ex) {
                LOG.error("Problem updating custom event {}", ex.getMessage());
                throw new EventServiceException("Could not update custom event: " + ex.getMessage());
            }
            return result;
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Deletes a custom event.
     * @param id
     * @return True when deletion is successful.
     * @throws org.pidome.server.services.events.EventServiceException When deletion fails.
     */
    public final boolean deleteCustomEvent(int id) throws EventServiceException {
        if(isAlive()){
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                    PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM customevents WHERE id=?")) {
                prep.setInt(1, id);
                prep.executeUpdate();
            } catch (SQLException ex) {
                LOG.error("Problem deleting custom event {}", ex.getMessage());
                throw new EventServiceException("Could not delete custom event: " + ex.getMessage());
            }
            CustomEvent delete = null;
            for (CustomEvent event : this.customEvents) {
                if (event.getId() == id) {
                    delete = event;
                }
            }
            if(delete!=null){
                this.customEvents.remove(delete);
                return true;
            }
            return false;
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Returns a single custom event.
     *
     * @param id the id of the requested custom event.
     * @return the Custom Event.
     * @throws EventServiceException when a custom event is not found.
     */
    public final CustomEvent getCustomEvent(int id) throws EventServiceException {
        if(isAlive()){
            for (CustomEvent event : this.customEvents) {
                if (event.getId() == id) {
                    return event;
                }
            }
            throw new EventServiceException("Custom event with id" + id + " is not found");
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Returns a list of custom events.
     * @return List of custom events.
     * @throws org.pidome.server.services.events.EventServiceException When the event service is not running
     */
    public final List<CustomEvent> getCustomEvents() throws EventServiceException {
        if(isAlive()){
            return this.customEvents;
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Let an event occur.
     *
     * @param id the id of the event.
     * @param reason The reason the event occurs.
     * @return true when the event has been triggered.
     * @throws org.pidome.server.services.events.EventServiceException When the event service is not running
     */
    public final boolean occur(int id, String reason) throws EventServiceException {
        if(isAlive()){
            for (CustomEvent event : this.customEvents) {
                if (event.getId() == id) {
                    event.occur(reason);
                    broadcastEventOccurred(event);
                    return true;
                }
            }
            return false;
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Let an event occur.
     *
     * @param identifier the identifier of the event.
     * @param reason The reason the event occurs.
     * @return true when the event has been triggered.
     * @throws org.pidome.server.services.events.EventServiceException When the event service is not running
     */
    public final boolean occur(String identifier, String reason) throws EventServiceException {
        if(isAlive()){
            for (CustomEvent event : this.customEvents) {
                if (event.getIdentifier().equals(identifier)) {
                    event.occur(reason);
                    broadcastEventOccurred(event);
                    return true;
                }
            }
            return false;
        } else {
            throw new EventServiceException("Event service not running");
        }
    }

    /**
     * Adds an event listener.
     * @param listener The listener to add.
     */
    public final void addListener(EventServiceListener listener){
        if(!this._listeners.contains(listener)){
            this._listeners.add(listener);
        }
    }
    
    /**
     * Removes an event listener.
     * @param listener the listener to be removed.
     */
    public final void removeListener(EventServiceListener listener){
        this._listeners.remove(listener);
    }
    
    /**
     * Broadcast that the an event occurred to the listeners.
     * @param event The event that occurred
     */
    private void broadcastEventOccurred(CustomEvent event){
        Runnable run = () -> {
            Iterator<EventServiceListener> listeners = _listeners.iterator();
            while( listeners.hasNext() ) {
                LOG.trace("Broadcasting new custom event: " + event.getName());
                listeners.next().handleCustomEvent(event);
            };
        };
        new Thread() { @Override public final void run() { run.run(); } }.start();
    }    
    
}