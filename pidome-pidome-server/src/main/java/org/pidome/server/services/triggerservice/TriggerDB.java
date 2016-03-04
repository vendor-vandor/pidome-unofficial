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
package org.pidome.server.services.triggerservice;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.services.triggerservice.TriggerEvent.Occurrence;
import org.pidome.server.system.db.DB;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

/**
 *
 * @author John Sirach
 */
public class TriggerDB {

    static Logger LOG = LogManager.getLogger(TriggerDB.class);

    /**
     * REturns a map containing triggers
     *
     * @return
     * @throws SQLException
     */
    public final Map<Integer, Map<String, Object>> getTriggers() throws SQLException {
        Map<Integer, Map<String, Object>> results = new HashMap<>();
        JSONParser jsonParser = new JSONParser();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                Statement statementEvents = fileDBConnection.createStatement();
                ResultSet rs = statementEvents.executeQuery("SELECT t.id, "
                        + "t.name, "
                        + "t.description, "
                        + "t.matchtype, "
                        + "t.rules, "
                        + "t.executes, "
                        + "t.occurrence "
                        + "FROM eventtriggers t")) {
            while (rs.next()) {
                try {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", rs.getString("name"));
                    data.put("description",rs.getString("description"));
                    data.put("matchtype",rs.getString("matchtype"));
                    data.put("rules", jsonParser.parse(rs.getString("rules")));
                    data.put("executes", jsonParser.parse(rs.getString("executes")));
                    data.put("occurrence", rs.getString("occurrence"));
                    results.put((int)rs.getInt("id"), data);
                } catch(ParseException ex){
                    LOG.error("Could not parse for trigger {}: {}", rs.getString("name"), ex.getMessage());
                }
            }
        }
        LOG.debug("Found {} triggers", results.size());
        return results;
    }

    /**
     * Returns a single trigger from the database
     *
     * @param triggerId
     * @return
     * @throws SQLException
     */
    public final Map<String, Object> getTrigger(int triggerId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        LOG.debug("Retrieving trigger id {} from database", triggerId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT t.id, "
                        + "t.name, "
                        + "t.description, "
                        + "t.matchtype, "
                        + "t.rules, "
                        + "t.executes, "
                        + "t.occurrence "
                        + "FROM eventtriggers t WHERE t.id=?")) {
            prep.setInt(1, triggerId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    try {
                        JSONParser jsonParser = new JSONParser();
                        result.put("name", rs.getString("name"));
                        result.put("description", rs.getString("description"));
                        result.put("matchtype",rs.getString("matchtype"));
                        result.put("rules", jsonParser.parse(rs.getString("rules")));
                        result.put("executes", jsonParser.parse(rs.getString("executes")));
                        result.put("occurrence", rs.getString("occurrence"));
                    } catch(ParseException ex){
                        LOG.error("Could not parse for trigger {}: {}", rs.getString("name"), ex.getMessage());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Saves a trigger in the database with rules and executes
     *
     * @param triggerId
     * @param name
     * @param description
     * @param ruleset
     * @param matchType
     * @param occurrence
     * @param execs
     * @return
     * @throws org.pidome.server.services.triggerservice.TriggerException
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public final int saveTrigger(int triggerId, String name, String description, Occurrence occurrence, String matchType, ArrayList ruleset, ArrayList execs) throws TriggerException, SQLException, IOException {
        try {
            int insertId = 0;
            String rules = PidomeJSONRPCUtils.getParamCollection(ruleset);
            String executes = PidomeJSONRPCUtils.getParamCollection(execs);
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
                if (triggerId == 0) {
                    try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into eventtriggers ('name','description','occurrence','matchtype','rules','executes') values(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        LOG.debug("Saving new trigger: {}", name);
                        prep.setString(1, name);
                        prep.setString(2, description);
                        prep.setString(3, occurrence.toString());
                        prep.setString(4, matchType);
                        prep.setString(5, rules);
                        prep.setString(6, executes);
                        prep.executeUpdate();
                        try (ResultSet rs = prep.getGeneratedKeys()) {
                            if (rs.next()) {
                                insertId = rs.getInt(1);
                            }
                        }
                    }
                } else {
                    try (PreparedStatement prep = fileDBConnection.prepareStatement("update eventtriggers set 'name'=? ,'description'=?,'occurrence'=?,'matchtype'=?,'rules'=?,'executes'=? ,modified=datetime('now') where id=?")) {
                        LOG.debug("Updating trigger: {}", name);
                        prep.setString(1, name);
                        prep.setString(2, description);
                        prep.setString(3, occurrence.toString());
                        prep.setString(4, matchType);
                        prep.setString(5, rules);
                        prep.setString(6, executes);
                        prep.setInt(7, triggerId);
                        prep.executeUpdate();
                    }
                    insertId = triggerId;
                }
            }
            return insertId;
        } catch (PidomeJSONRPCException ex) {
            throw new TriggerException(ex.getMessage());
        }
    }

    /**
     * Deletes a trigger from the database
     *
     * @param triggerId
     * @throws SQLException
     */
    public final void deleteTrigger(int triggerId) throws SQLException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM eventtriggers WHERE id=?")) {
            prep.setInt(1, triggerId);
            prep.executeUpdate();
        }
    }
    
}
