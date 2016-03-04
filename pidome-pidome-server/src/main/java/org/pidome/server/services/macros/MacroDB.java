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
import org.pidome.server.system.db.DB;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;

/**
 *
 * @author John Sirach
 */
public class MacroDB {

    static Logger LOG = LogManager.getLogger(MacroDB.class);

    /**
     * Returns a map containing macro's
     *
     * @return
     * @throws SQLException
     */
    public final Map<Integer, Map<String, Object>> getMacros() throws SQLException {
        Map<Integer, Map<String, Object>> results = new HashMap<>();
        JSONParser jsonParser = new JSONParser();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                Statement statementEvents = fileDBConnection.createStatement();
                ResultSet rs = statementEvents.executeQuery("SELECT m.id, "
                        + "m.name, "
                        + "m.description, "
                        + "m.isfavorite, "
                        + "m.executes "
                        + "FROM macros m")) {
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("name", rs.getString("name"));
                data.put("description",rs.getString("description"));
                data.put("isfavorite",rs.getBoolean("isfavorite"));
                if(rs.getString("executes")!=null && !rs.getString("executes").equals("")){
                    try {
                        data.put("executes", jsonParser.parse(rs.getString("executes")));
                    } catch(ParseException ex){
                        LOG.error("Could not parse for macro {}: {}. Macro data invalid", rs.getString("name"), ex.getMessage(),ex);
                        data.put("executes", new ArrayList());
                    }
                } else {
                    /// Add the possibility of empty macro's
                    data.put("executes", new ArrayList());
                }
                results.put((int)rs.getInt("id"), data);
            }
        }
        LOG.debug("Found {} macros", results.size());
        return results;
    }

    /**
     * Returns a single macro from the database
     *
     * @param triggerId
     * @return
     * @throws SQLException
     */
    public final Map<String, Object> getMacro(int triggerId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        LOG.debug("Retrieving macro id {} from database", triggerId);
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT m.id, "
                        + "m.name, "
                        + "m.description, "
                        + "m.isfavorite, "
                        + "m.executes, "
                        + "FROM macros m WHERE m.id=?")) {
            prep.setInt(1, triggerId);
            try (ResultSet rs = prep.executeQuery()) {
                while (rs.next()) {
                    try {
                        JSONParser jsonParser = new JSONParser();
                        result.put("name", rs.getString("name"));
                        result.put("description", rs.getString("description"));
                        result.put("isfavorite", rs.getBoolean("occurrence"));
                        if(rs.getString("executes")!=null && !rs.getString("executes").equals("")){
                            result.put("executes", jsonParser.parse(rs.getString("executes")));
                        } else {
                            /// Add the possibility of empty macro's
                            result.put("executes", new ArrayList());
                        }
                    } catch(ParseException ex){
                        LOG.error("Could not parse for macro {}: {}", rs.getString("name"), ex.getMessage());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Saves a macro in the database with executes
     *
     * @param macroId
     * @param name
     * @param description
     * @param favorite
     * @param execs
     * @return
     * @throws org.pidome.server.services.macros.MacroException
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     */
    public final int saveMacro(int macroId, String name, String description, boolean favorite, ArrayList execs) throws MacroException, SQLException, IOException {
        try {
            int insertId = 0;
            String executes = PidomeJSONRPCUtils.getParamCollection(execs);
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
                if (macroId == 0) {
                    try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into macros ('name','description','isfavorite','executes') values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        LOG.debug("Saving new macro: {}", name);
                        prep.setString(1, name);
                        prep.setString(2, description);
                        prep.setBoolean(3, favorite);
                        prep.setString(4, executes);
                        prep.executeUpdate();
                        try (ResultSet rs = prep.getGeneratedKeys()) {
                            if (rs.next()) {
                                insertId = rs.getInt(1);
                            }
                        }
                    }
                } else {
                    try (PreparedStatement prep = fileDBConnection.prepareStatement("update macros set 'name'=? ,'description'=?,'isfavorite'=?,'executes'=? ,modified=datetime('now') where id=?")) {
                        LOG.debug("Updating macro: {}", name);
                        prep.setString(1, name);
                        prep.setString(2, description);
                        prep.setBoolean(3, favorite);
                        prep.setString(4, executes);
                        prep.setInt(5, macroId);
                        prep.executeUpdate();
                    }
                    insertId = macroId;
                }
            }
            return insertId;
        } catch (PidomeJSONRPCException ex) {
            throw new MacroException(ex.getMessage());
        }
    }

    /**
     * Set a macro favorite.
     * @param macroId
     * @param favorite
     * @return 
     */
    public final boolean setFavorite(final int macroId, final boolean favorite) throws SQLException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            try (PreparedStatement prep = fileDBConnection.prepareStatement("update macros set 'isfavorite'=?, modified=datetime('now') where id=?")) {
                LOG.debug("Updating macro as favorite: {}, {}", macroId, favorite);
                prep.setBoolean(1, favorite);
                prep.setInt(2, macroId);
                prep.executeUpdate();
            }
        }
        return true;
    }
    
    /**
     * Deletes a macro from the database
     *
     * @param macroId
     * @throws SQLException
     */
    public final void deleteMacro(int macroId) throws SQLException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM macros WHERE id=?")) {
            prep.setInt(1, macroId);
            prep.executeUpdate();
        }
    }
    
}
