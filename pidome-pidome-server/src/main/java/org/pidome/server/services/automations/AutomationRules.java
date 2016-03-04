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

package org.pidome.server.services.automations;

import org.pidome.server.services.automations.rule.AutomationRule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.services.ServiceInterface;
import org.pidome.server.system.db.DB;

/**
 *
 * @author John
 */
public final class AutomationRules implements ServiceInterface {
    
    static Logger LOG = LogManager.getLogger(AutomationRules.class);
    
    static List<AutomationRule> ruleSet = new ArrayList<>();
    
    private static AutomationRules me;
    
    private static boolean running = false;
    
    public static AutomationRules getInstance(){
        if(me==null){
            me = new AutomationRules();
        }
        return me;
    }
    
    private AutomationRules(){
        
    }
    
    /**
     * Returns the automation rules list.
     * @return 
     */
    public static ArrayList<Map<String,Object>> getRules(){
        ArrayList<Map<String,Object>> results = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                Statement statementEvents = fileDBConnection.createStatement();
                ResultSet rs = statementEvents.executeQuery("SELECT a.id, "
                        + "a.name, "
                        + "a.description, "
                        + "a.active "
                        + "FROM automationrules a")) {
            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("name", rs.getString("name"));
                data.put("description",rs.getString("description"));
                data.put("active",rs.getBoolean("active"));
                results.add(data);
            }
        } catch (SQLException ex){
            LOG.error("Could not get rules: {}", ex.getMessage());
        }
        LOG.debug("Found {} automation rules", results.size());
        return results;
    }
    
    /**
     * Returns the automation rules list.
     * @param ruleId
     * @return 
     */
    public static Map<String,Object> getRule(int ruleId){
        Map<String,Object> result = new HashMap<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement statementEvents = fileDBConnection.prepareStatement("SELECT a.id, "
                    + "a.name, "
                    + "a.description, "
                    + "a.active, "
                    + "a.rule "
                    + "FROM automationrules a WHERE id=? LIMIT 1;")) {
            statementEvents.setInt(1, ruleId);
            try (ResultSet rs = statementEvents.executeQuery()){
                while (rs.next()) {
                    result.put("id", rs.getInt("id"));
                    result.put("name", rs.getString("name"));
                    result.put("description",rs.getString("description"));
                    result.put("active",rs.getBoolean("active"));
                    result.put("rule",rs.getString("rule"));
                }
            }
        } catch (SQLException ex){
            LOG.error("Could not get rule: {}", ex.getMessage());
        }
        LOG.trace("Found automation rule: {}", result);
        return result;
    }
    
    /**
     * Adds a rule to the database.
     * @param ruleId
     * @param name
     * @param description
     * @param active
     * @param rule 
     * @return  
     */
    public static int saveRule(int ruleId, String name, String description, boolean active, String rule){
        int insertId = 0;
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)) {
            if (ruleId == 0) {
                try (PreparedStatement prep = fileDBConnection.prepareStatement("insert into automationrules ('name','description','active','rule') values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    LOG.debug("Saving new automation rule: {}", name);
                    prep.setString(1, name);
                    prep.setString(2, description);
                    prep.setBoolean(3, active);
                    prep.setString(4, rule);
                    prep.executeUpdate();
                    try (ResultSet rs = prep.getGeneratedKeys()) {
                        if (rs.next()) {
                            insertId = rs.getInt(1);
                        }
                    }
                }
            } else {
                try (PreparedStatement prep = fileDBConnection.prepareStatement("update automationrules set 'name'=? ,'description'=?,'active'=?,'rule'=? ,modified=datetime('now') where id=?")) {
                    LOG.debug("Updating automation rule: {}", name);
                    prep.setString(1, name);
                    prep.setString(2, description);
                    prep.setBoolean(3, active);
                    prep.setString(4, rule);
                    prep.setInt(5, ruleId);
                    prep.executeUpdate();
                }
                insertId = ruleId;
            }
            loadRule(insertId);
        } catch (SQLException ex){
            LOG.error("Could not save rule in database: {}", ex.getMessage());
        }
        return insertId;
    }
    
    /**
     * Deletes a rule from the database
     *
     * @param ruleId
     * @throws SQLException
     */
    public static void deleteRule(int ruleId) throws SQLException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                PreparedStatement prep = fileDBConnection.prepareStatement("DELETE FROM automationrules WHERE id=?")) {
            prep.setInt(1, ruleId);
            prep.executeUpdate();
            unloadRule(ruleId);
        }
    }
    
    /**
     * Activates or deactivates a rule.
     * @param ruleId
     * @param active
     * @return 
     */
    public static boolean setRuleActive(int ruleId, boolean active){
        if(!active){
            unloadRule(ruleId);
        } else {
            loadRule(ruleId);
        }
        return true;
    }
    
    private static void loadInitialRules(){
        for(Map<String,Object> initRule:getRules()){
            Map<String,Object> rule = getRule((int)initRule.get("id"));
            try {
                AutomationRule loadRule = new AutomationRule((int)rule.get("id"),
                        (String)rule.get("name"),
                        (String)rule.get("description"),
                        (boolean)rule.get("active"),
                        (String)rule.get("rule"));
                ruleSet.add(loadRule);
                LOG.info("Loaded rule: {}", loadRule.getName());
            } catch (Exception ex) {
                LOG.error("Can not create ruleset for: {}, {}", (String)rule.get("name"), ex.getMessage());
            }
        }
        running = true;
    }

    private static void unloadRule(int ruleId){
        int index = -1;
        for(AutomationRule rule:ruleSet){
            if(rule.getId()==ruleId){
                LOG.info("Unloaded rule: {}", rule.getName());
                rule.destroy();
            }
            index++;
        }
        if(index!=-1){
            ruleSet.remove(index);
        }
    }
    
    private static void loadRule(int ruleId){
        unloadRule(ruleId);
        Map<String,Object> rule = getRule(ruleId);
        if(!rule.isEmpty()){
            try {
                AutomationRule newRule = new AutomationRule((int)rule.get("id"),
                        (String)rule.get("name"),
                        (String)rule.get("description"),
                        (boolean)rule.get("active"),
                        (String)rule.get("rule"));
                ruleSet.add(newRule);
                LOG.info("Loaded rule: {}", newRule.getName());
            } catch (AutomationRulesException ex) {
                LOG.error("Could not load rule: {}", ex.getMessage(), ex);
            }
        }
    }
    
    @Override
    public void interrupt() {
        for(AutomationRule rule:ruleSet){
            rule.destroy();
        }
    }

    @Override
    public void start() {
        loadInitialRules();
    }

    @Override
    public boolean isAlive() {
        return running;
    }

    @Override
    public String getServiceName() {
        return "Automation rules service";
    }
    
}