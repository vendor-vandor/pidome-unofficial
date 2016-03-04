/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.plugins.graphdata.RoundRobinDataGraphItem;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.hardware.devices.Devices;

/**
 *
 * @author John
 */
public class GraphServiceJSONWrapper extends AbstractRPCMethodExecutor implements GraphServiceJSONWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getDeviceGraph", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("group", "");}});
                        put(2,new HashMap<String,Object>(){{put("control", "");}});
                        put(3,new HashMap<String,Object>(){{put("range", new ArrayList<String>());}});
                        put(4,new HashMap<String,Object>(){{put("calculation", "");}});
                    }
                });
                put("getUtilityGraph", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("group", "");}});
                        put(2,new HashMap<String,Object>(){{put("control", "");}});
                        put(3,new HashMap<String,Object>(){{put("range", new ArrayList<String>());}});
                        put(4,new HashMap<String,Object>(){{put("calculation", "");}});
                    }
                });
            }
        };
        return mapping;
    }
    
    /**
     * @inheritdoc
     */
    @Override
    public Object getDeviceGraph(Number deviceId, String groupId, String controlId, List<String> range, String calcTypeParam) {
        Map<Object,Object> message = new HashMap<>();
        message.put("message", "");
        Map<String,Object> data = new HashMap<>();
        ///// prep to use field declaration types.
        String calcType = " avg(value) ";
        List<Map<Object,Object>> hourData = new ArrayList<>();
        List<Map<Object,Object>> dayData = new ArrayList<>();
        List<Map<Object,Object>> weekData = new ArrayList<>();
        List<Map<Object,Object>> monthData = new ArrayList<>();
        List<Map<Object,Object>> yearData = new ArrayList<>();
        try {
            String dbFile = SystemConfig.getProperty("system", "database.devicedatalocation") + deviceId + ".data";
            File f = new File(dbFile);
            if (f.exists()) {
                try (Connection conManager = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
                    if(range.contains("hour")){
                        try (PreparedStatement prepHour = conManager.prepareStatement("select \"time\",\"value\" from '"+groupId+"_"+controlId+"_minutes' WHERE value IS NOT NULL");
                                ResultSet rsHour = prepHour.executeQuery()) {
                            while (rsHour.next()) {
                                addNVPToList(hourData, rsHour.getLong("time")*1000, rsHour.getFloat("value"));
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving hour data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("day")){
                        String query = "SELECT time, value "
                                       + "FROM '"+groupId+"_"+controlId+"_hours' ";
                        try (PreparedStatement prepDay = conManager.prepareStatement(query);
                                ResultSet rsDay = prepDay.executeQuery()) {
                            while (rsDay.next()) {
                                addNVPToList(dayData, rsDay.getLong("time")*1000, rsDay.getFloat("value"));
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving day data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("week")){
                        String query = "SELECT time, value " +
                                         "FROM '"+groupId+"_"+controlId+"_days' "
                                      + "WHERE time > strftime('%s',strftime('%Y-%m-%d 00:00:00', datetime('now','-7 days'))) AND value IS NOT NULL ";
                        try (PreparedStatement prepWeek = conManager.prepareStatement(query);
                                ResultSet rsWeek = prepWeek.executeQuery()) {
                            while (rsWeek.next()) {
                                addNVPToList(weekData, rsWeek.getLong("time")*1000, rsWeek.getFloat("value"));
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving week data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("month")){
                        String query = "SELECT time, value " +
                                         "FROM '"+groupId+"_"+controlId+"_days' "
                                      + "WHERE time > strftime('%s',strftime('%Y-%m-%d 00:00:00', datetime('now','-31 days'))) AND value IS NOT NULL ";
                        try (PreparedStatement prepMonth = conManager.prepareStatement(query);
                                ResultSet rsMonth = prepMonth.executeQuery()) {
                            while (rsMonth.next()) {
                                addNVPToList(monthData, rsMonth.getLong("time")*1000, rsMonth.getFloat("value"));
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving month data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("year")){
                        if(calcTypeParam !=null && (calcTypeParam.equals("AVG") || calcTypeParam.equals("SUM"))){
                            calcType = " "+calcType+"(value) ";
                        } else {
                            try {
                                RoundRobinDataGraphItem store = Devices.getStorageList(deviceId.intValue()).getStorageField(groupId, controlId);
                                switch(store.getFieldType()){
                                    case SUM:
                                        calcType = " SUM(value) ";
                                    break;
                                }
                            } catch (Exception ex) {
                                String error = "Could not get device details (Not loaded?), defaulting to average, to overwrite add param calc=SUM for totals or calc=AVG for average";
                                message.put("message", error);
                                LOG.error("Could not get device details (Not loaded?), defaulting to average, to overwrite add param calc=SUM for totals or calc=AVG for average: {}", ex.getMessage());
                            }
                        }
                        String query = "SELECT strftime('%W', datetime(time, 'unixepoch')) as interval, time, "+calcType+" as value "
                                       + "FROM '"+groupId+"_"+controlId+"_days' WHERE time > strftime('%s',strftime('%Y-%m-%d 00:00:00', datetime('now','-1 year'))) AND value IS NOT NULL "
                                      + "GROUP by interval "
                                      + "ORDER by interval";
                        try (PreparedStatement prepYear = conManager.prepareStatement(query);
                                ResultSet rsYear = prepYear.executeQuery()) {
                            while (rsYear.next()) {
                                addNVPToList(yearData, rsYear.getLong("time")*1000, rsYear.getFloat("value"));
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving year data: {}", ex.getMessage());
                        }
                    }
                    
                }
            }
        } catch (ConfigPropertiesException | SQLException ex) {
            LOG.error("Error retrieving data: {}", ex.getMessage());
        }
        data.put("message", message);
        data.put("hour", hourData);
        data.put("day", dayData);
        data.put("week", weekData);
        data.put("month", monthData);
        data.put("year", yearData);
        return data;
    }

    /**
     * @inheritdoc
     */
    @Override
    public Object getUtilityGraph(Number pluginId, String groupId, String controlId, List<String> range, String calcTypeParam){
        DecimalFormat format = new DecimalFormat();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(10);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(false);
        format.setDecimalFormatSymbols(symbols);
        
        Map<String,Object> data = new HashMap<>();
        
        List<Map<Object,Object>> hourData = new ArrayList<>();
        List<Map<Object,Object>> dayData = new ArrayList<>();
        List<Map<Object,Object>> weekData = new ArrayList<>();
        List<Map<Object,Object>> monthData = new ArrayList<>();
        List<Map<Object,Object>> yearData = new ArrayList<>();
        
        try {
            String dbFile = SystemConfig.getProperty("system", "database.plugindatalocation") + pluginId + ".data";
            File f = new File(dbFile);
            if (f.exists()) {
                try (Connection conManager = DriverManager.getConnection("jdbc:sqlite:" + dbFile)) {
                    if(range.contains("hour")){
                        try (PreparedStatement prepHour = conManager.prepareStatement("select \"time\",\"value\" from '"+groupId+"_"+controlId+"_minutes' WHERE value>=0");
                                ResultSet rsHour = prepHour.executeQuery()) {
                            while (rsHour.next()) {
                                addNVPToList(hourData, rsHour.getLong("time")*1000, format.parse(rsHour.getString("value")).floatValue());
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving hour data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("day")){
                        String query = "SELECT time, value "
                                       + "FROM '"+groupId+"_"+controlId+"_hours' "
                                      + "WHERE value>=0";
                        try (PreparedStatement prepDay = conManager.prepareStatement(query);
                             ResultSet rsDay = prepDay.executeQuery()) {
                            while (rsDay.next()) {
                                addNVPToList(dayData, rsDay.getLong("time")*1000, format.parse(rsDay.getString("value")).floatValue());
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving day data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("week")){
                        String query = "SELECT time, value " +
                                         "FROM '"+groupId+"_"+controlId+"_days' "
                                      + "WHERE time > strftime('%s',strftime('%Y-%m-%d 00:00:00', datetime('now','-7 days'))) "
                                        + "AND value>=0 ";
                        try (PreparedStatement prepWeek = conManager.prepareStatement(query);
                             ResultSet rsWeek = prepWeek.executeQuery()) {
                            while (rsWeek.next()) {
                                addNVPToList(weekData, rsWeek.getLong("time")*1000, format.parse(rsWeek.getString("value")).floatValue());
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving week data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("month")){
                        String query = "SELECT time, value " +
                                         "FROM '"+groupId+"_"+controlId+"_days' "
                                      + "WHERE time > strftime('%s',strftime('%Y-%m-%d 00:00:00', datetime('now','-31 days'))) "
                                        + "AND value>=0 ";
                        try (PreparedStatement prepMonth = conManager.prepareStatement(query);
                             ResultSet rsMonth = prepMonth.executeQuery()) {
                            while (rsMonth.next()) {
                                addNVPToList(monthData, rsMonth.getLong("time")*1000, format.parse(rsMonth.getString("value")).floatValue());
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving month data: {}", ex.getMessage());
                        }
                    }
                    
                    if(range.contains("year")){
                        String query = "SELECT ((time / 604800) * 604800) interval, time, value "
                                       + "FROM '"+groupId+"_"+controlId+"_days' WHERE time > strftime('%s',strftime('%Y-%m-%d 00:00:00', datetime('now','-1 year'))) "
                                        + "AND value>=0 "
                                      + "GROUP by interval "
                                      + "ORDER by interval";
                        try (PreparedStatement prepYear = conManager.prepareStatement(query);
                             ResultSet rsYear = prepYear.executeQuery()) {
                            while (rsYear.next()) {
                                addNVPToList(yearData, rsYear.getLong("time")*1000, format.parse(rsYear.getString("value")).floatValue());
                            }
                        } catch (Exception ex){
                            LOG.error("Error retrieving year data: {}", ex.getMessage());
                        }
                    }
                    
                    data.put("hour", hourData);
                    data.put("day", dayData);
                    data.put("week", weekData);
                    data.put("month", monthData);
                    data.put("year", yearData);
                }
            }
        } catch (ConfigPropertiesException | SQLException ex) {
            LOG.error("Error retrieving data: {}", ex.getMessage());
        }
        return data;
    }
    
    /**
     * Returns graphs for plugins.
     * @return 
     */
    @Override
    public Object getPluginGraph() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Creates an nvp set for graphs.
     * Data is set as {"key": asSupplied, "value": asSuplied}
     * @param list The list to modify
     * @param name The name(key) of the item.
     * @param value The value of the item.
     */
    private static void addNVPToList(List list, Object name, Object value){
        list.add(new HashMap<Object,Object>(2){{ put("key",name); put("value",value); }});
    }
    
}