/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.joda.time.DateTime;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public class CustomEvent {
    
    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(CustomEvent.class);
    
    private final int id;
    private final String identifier;
    private String name = "";
    private String description = "";
    
    private String lastOccurrence;
    private String lastOccurrenceDescription;
    
    protected CustomEvent(int id, String identifier){
        this.id = id;
        this.identifier = identifier;
    }
    
    public final int getId(){
        return this.id;
    }
    
    public final String getIdentifier(){
        return this.identifier;
    }
    
    protected final void setName(String name){
        this.name = name;
    }
    
    public final String getName(){
        return this.name;
    }
    
    protected final void setDescription(String description){
        this.description = description;
    }
    
    public final String getDescription(){
        return this.description;
    }
    
    protected final void setLastOccurrence(String date, String description){
        this.lastOccurrence = date;
        this.description = description;
    }
    
    public final String getLastOccurrence(){
        return this.lastOccurrence;
    }
    
    public final String getLastOccurrenceDescription(){
        return this.lastOccurrenceDescription;
    }
    
    protected final void occur(String reason){
        DateTime date = new DateTime();
        this.lastOccurrence = date.toLocalDateTime().toString();
        this.lastOccurrenceDescription = reason;
        LOG.info("Occurrence of {} at {} because of {}",this.getIdentifier(), this.lastOccurrence, this.lastOccurrenceDescription);
        Map<String, Object> sendObject = new HashMap<>();
        sendObject.put("id", id);
        sendObject.put("identifier", identifier);
        sendObject.put("datetime",   lastOccurrence);
        sendObject.put("reason", reason);
        Runnable run = () -> {
            ClientMessenger.send("EventService", "eventOccurred", 0, sendObject);
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM) ){
                try (PreparedStatement prep = fileDBConnection.prepareStatement("UPDATE customevents SET 'last_occurrence'=datetime('now'),'last_occurrence_remark'=? where id=?")) {
                    prep.setString(1, reason);
                    prep.setInt(2, id);
                    prep.executeUpdate();
                } catch (Exception ex){
                    LOG.error("Problem updating custom event occurrence {}", ex.getMessage());
                }
            } catch (SQLException ex) {
                LOG.error("Problem updating custom event occurrence {}", ex.getMessage());
            }
        };
        new Thread() { @Override public void run() { run.run(); } }.start();
    }
    
}
