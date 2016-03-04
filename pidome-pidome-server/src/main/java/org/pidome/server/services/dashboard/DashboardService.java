/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.dashboard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.system.db.DB;

/**
 * Dashboard management.
 * Dashboards are based on fixed clients or persons. Next to this Persons can have 
 * customized dashboards.
 * By default dashboards are identified by connected device types where there is 
 * an exception for dashboards available for clients based on user preferences. These are HYBRID.
 * @author John
 */
public class DashboardService {
 
    static Logger LOG = LogManager.getLogger(DashboardService.class);
    
    /**
     * Returns a dashboard object.
     * To be used for RPC.
     * The id is or a fixed client or a person id. The device type determines the id type. MOBILE and WEB are bound to users and DISPLAY is bound to a fixed client.
     * @param idFor The id this dashboard is for.
     * @param clientId The client id this dashboard is for (only used with mobile).
     * @param deviceType the device type requesting it.
     * @return 
     * @throws org.pidome.server.services.dashboard.DashboardServiceException 
     */
    public static Dashboard getDashboard(int idFor, int clientId, RemoteClient.DeviceType deviceType) throws DashboardServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prepDash = fileDBConnection.prepareStatement("SELECT d.*,p.firstname as personname,c.clientname,lc.deviceinfo "
                                                                            + "FROM dashboards d "
                                                                       + "LEFT JOIN persons p ON d.personid=p.id "
                                                                       + "LEFT JOIN fixedclients c ON c.id=d.clientid "
                                                                       + "LEFT JOIN clients_linked lc ON lc.id=d.clientid "
                                                                           + "WHERE d.clienttype=? AND ( "
                                                                                 + "CASE "
                                                                                     + "WHEN d.clienttype='MOBILE' THEN lc.id=? "
                                                                                     + "ELSE d.clientid=? "
                                                                                 + "END ) "
                                                                             + "AND d.personid=? LIMIT 1")){
            prepDash.setString(1, deviceType.toString());
            switch(deviceType){
                case DISPLAY:
                    prepDash.setInt(2, 0);
                    prepDash.setInt(3, idFor);
                    prepDash.setInt(4, 0);
                break;
                case MOBILE:
                    prepDash.setInt(2, clientId);
                    prepDash.setInt(3, 0);
                    prepDash.setInt(4, idFor);
                break;
                default:
                    prepDash.setInt(2, 0);
                    prepDash.setInt(3, 0);
                    prepDash.setInt(4, idFor);
                break;
            }
            Dashboard dash;
            try (ResultSet rsDash = prepDash.executeQuery()) {
                if (rsDash.next()) {
                    dash = createDashboard(rsDash, true);
                } else {
                    LOG.warn("No dashboard found for idFor: '{}' and clientId: '{}' with type: '{}'", idFor, clientId, deviceType);
                    dash = createDashboard(null, false);
                }
            }
            switch(deviceType){
                case DISPLAY:
                    dash.setClientId(idFor);
                break;
                case MOBILE:
                    dash.setClientId(clientId);
                    dash.setPersonId(idFor);
                break;
                default:
                    dash.setPersonId(idFor);
                break;
            }
            return dash;
        } catch (SQLException ex) {
            LOG.error("Problem loading dashboard {}", ex.getMessage(), ex);
            throw new DashboardServiceException("Could not load dashboard: " + ex.getMessage());
        }
    }
    
    public static Dashboard getDashboardById(int dashId) throws DashboardServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prepDash = fileDBConnection.prepareStatement("SELECT d.*,p.firstname as personname,c.clientname,lc.deviceinfo "
                                                                            + "FROM dashboards d "
                                                                       + "LEFT JOIN persons p ON d.personid=p.id "
                                                                       + "LEFT JOIN fixedclients c ON c.id=d.clientid "
                                                                       + "LEFT JOIN clients_linked lc ON lc.id=d.clientid "
                                                                           + "WHERE d.id=? LIMIT 1")){
            prepDash.setInt(1, dashId);
            ResultSet rsDash = prepDash.executeQuery();
            if (rsDash.next()) {
                return createDashboard(rsDash, true);
            } else {
                throw new DashboardServiceException("There is no dashboard with id " + dashId);
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading persons list {}", ex.getMessage());
            throw new DashboardServiceException("Could not load dashboard: " + ex.getMessage());
        }
    }
    
    public static Dashboard getDashboardById(int dashId, int uid) throws DashboardServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prepDash = fileDBConnection.prepareStatement("SELECT d.*,p.firstname as personname,c.clientname,lc.deviceinfo "
                                                                            + "FROM dashboards d "
                                                                       + "LEFT JOIN persons p ON d.personid=p.id "
                                                                       + "LEFT JOIN fixedclients c ON c.id=d.clientid "
                                                                       + "LEFT JOIN clients_linked lc ON lc.id=d.clientid "
                                                                           + "WHERE d.id=? AND p.id=? LIMIT 1")){
            prepDash.setInt(1, dashId);
            prepDash.setInt(1, uid);
            ResultSet rsDash = prepDash.executeQuery();
            if (rsDash.next()) {
                return createDashboard(rsDash, true);
            } else {
                throw new DashboardServiceException("There is no dashboard with id " + dashId);
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading persons list {}", ex.getMessage());
            throw new DashboardServiceException("Could not load dashboard: " + ex.getMessage());
        }
    }
    
    /**
     * Delete a dashboard.
     * @param dashId
     * @return
     * @throws DashboardServiceException 
     */
    public static boolean deleteDashboardById(int dashId) throws DashboardServiceException {
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prepDash = fileDBConnection.prepareStatement("DELETE FROM dashboards WHERE id=?")){
            prepDash.setInt(1, dashId);
            prepDash.execute();
            return true;
        } catch (SQLException ex) {
            LOG.error("Problem loading persons list {}", ex.getMessage());
            throw new DashboardServiceException("Could not load dashboard: " + ex.getMessage());
        }
    }
    
    /**
     * Returns a dashboard object.
     * To be used for RPC.
     * The id is or a fixed client or a person id. The device type determines the id type. MOBILE and WEB are bound to users and DISPLAY is bound to a fixed client.
     * @return 
     * @throws org.pidome.server.services.dashboard.DashboardServiceException 
     */
    public static List<Dashboard> getDashboards() throws DashboardServiceException {
        List<Dashboard> dashList = new ArrayList<>();
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prepDash = fileDBConnection.prepareStatement("SELECT d.*,p.firstname as personname,c.clientname,lc.deviceinfo "
                                                                            + "FROM dashboards d "
                                                                       + "LEFT JOIN persons p ON d.personid=p.id "
                                                                       + "LEFT JOIN fixedclients c ON c.id=d.clientid "
                                                                       + "LEFT JOIN clients_linked lc ON lc.id=d.clientid "); 
             ResultSet rsDash = prepDash.executeQuery()){
            while (rsDash.next()) {
                dashList.add(createDashboard(rsDash, false));
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading persons list {}", ex.getMessage());
            throw new DashboardServiceException("Could not load dashboard: " + ex.getMessage());
        }
        return dashList;
    }
    
    /**
     * Get a dashboard to be shown on a client identified by an user.
     * @param clientId
     * @param idFor
     * @return
     * @throws DashboardServiceException 
     */
    public static Dashboard getUserDashboardByClient(int clientId, int idFor) throws DashboardServiceException{
        try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
             PreparedStatement prepDash = fileDBConnection.prepareStatement("SELECT * FROM dashboards d "
                                                                       + "INNER JOIN persons p ON d.personid=p.id "
                                                                       + "INNER JOIN fixedclients c ON c.id=d.clientid "
                                                                            + "WHERE d.clientid=? AND d.personid=? AND d.clienttype='HYBRID' LIMIT 1")){
            prepDash.setInt(1, clientId);
            prepDash.setInt(2, idFor);
            try (ResultSet rsDash = prepDash.executeQuery()) {
                if (rsDash.next()) {
                    return createDashboard(rsDash, true);
                } else {
                    throw new DashboardServiceException("No dashboard found for " + idFor + " with bound to client " + clientId);
                }
            }
        } catch (SQLException ex) {
            LOG.error("Problem loading persons list {}", ex.getMessage());
            throw new DashboardServiceException("Could not load dashboard: " + ex.getMessage());
        }
    }
    
    /**
     * Saves/updates a dashboard to/in the database.
     * @param dashId The id to be updated
     * @param name
     * @param personId
     * @param clientid
     * @param type
     * @param content
     * @return
     * @throws DashboardServiceException 
     */
    public static boolean saveDashboard(int dashId, String name, int personId, int clientid, String type, Object content) throws DashboardServiceException {
        try {
            String serializedJson = PidomeJSONRPCUtils.getParamCollection(content);
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM);
                    PreparedStatement prepDash = fileDBConnection.prepareStatement("INSERT OR REPLACE INTO dashboards (id, name, clienttype, clientid, personid, construct) " +
                            " VALUES (COALESCE((SELECT id FROM dashboards WHERE id = ?), null), ?, ?, ?, ?, ?);")){
                prepDash.setInt(1, dashId);
                prepDash.setString(2, name);
                prepDash.setString(3, type);
                prepDash.setInt(4, clientid);
                prepDash.setInt(5, personId);
                prepDash.setString(6, serializedJson);
                prepDash.execute();
            } catch (SQLException ex) {
                LOG.error("Problem saving dashboard: {}", ex.getMessage(), ex);
                throw new DashboardServiceException("Could not save dashboard: " + ex.getMessage());
            }
        } catch (PidomeJSONRPCException ex) {
            LOG.error("Could not compose serialized dashboard data: {}", ex.getMessage(), ex);
            throw new DashboardServiceException("Could not save dashboard");
        }
        return true;
    }
    
    /**
     * Return a constructed dashboard.
     * @param rsDash
     * @return
     * @throws SQLException 
     */
    private static Dashboard createDashboard(ResultSet rsDash, boolean construct) throws SQLException {
        Dashboard dash = new Dashboard();
        if(rsDash!=null){
            dash.setId(rsDash.getInt("id"));
            dash.setName(rsDash.getString("name"));
            dash.setClientId(rsDash.getInt("clientid"));
            if(rsDash.getString("clienttype").equals("MOBILE")){
                dash.setClientName(rsDash.getString("deviceinfo"));
            } else {
                dash.setClientName(rsDash.getString("clientname"));
            }
            dash.setPersonId(rsDash.getInt("personid"));
            dash.setPersonName(rsDash.getString("personname"));
            dash.setClientType(rsDash.getString("clienttype"));
            if(construct){
                dash.setConstruct(rsDash.getString("construct"));
            } else {
                dash.setConstruct("[]");
            }
        } else {
            dash.setId(0);
            dash.setName("");
            dash.setClientId(0);
            dash.setClientName("");
            dash.setPersonId(0);
            dash.setPersonName("");
            dash.setClientType("NONE");
            dash.setConstruct("[]");
        }
        return dash;
    }
    
}