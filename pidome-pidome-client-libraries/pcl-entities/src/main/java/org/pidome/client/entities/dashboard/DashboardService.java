/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.client.entities.dashboard;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.client.entities.Entity;
import org.pidome.client.entities.EntityNotAvailableException;
import org.pidome.client.entities.macros.MacroService;
import org.pidome.client.entities.users.UserServiceException;
import org.pidome.client.system.PCCConnectionInterface;
import org.pidome.client.system.PCCConnectionNameSpaceRPCListener;
import org.pidome.pcl.data.parser.PCCEntityDataHandler;
import org.pidome.pcl.data.parser.PCCEntityDataHandlerException;
import org.pidome.pcl.utilities.properties.IntegerPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ObservableArrayListBean;
import org.pidome.pcl.utilities.properties.ReadOnlyIntegerPropertyBindingBean;
import org.pidome.pcl.utilities.properties.ReadOnlyObservableArrayListBean;

/**
 * Service which holds a set of dashboards.
 * Depending on the connection profile this service will also hold user bound dashboards.
 * When the connection profile FIXED is used which identifies this client as a fixed client there will be user
 * bound and client bound dashboards available if these are created on the server. All other connection profiles will only contain 
 * the dashboard assigned to the user.
 * @author John
 */
public class DashboardService extends Entity implements PCCConnectionNameSpaceRPCListener {

    static {
        Logger.getLogger(DashboardService.class.getName()).setLevel(Level.ALL);
    }

    /**
     * List of known dashboards.
     */
    private final ObservableArrayListBean<Dashboard> dashList = new ObservableArrayListBean<>();
    
    /**
     * A read only wrapper for the dashboards list.
     */
    private final ReadOnlyObservableArrayListBean<Dashboard> readOnlyDashboardsList = new ReadOnlyObservableArrayListBean<>(dashList);
    
    /**
     * Property holding the amount of dashboards available.
     * -1: Unknown
     *  0: No dashboards
     *  1+: One or more dashboards.
     */
    private final IntegerPropertyBindingBean dashAmount = new IntegerPropertyBindingBean(-1);
    
    /**
     * Connection interface.
     */
    private PCCConnectionInterface connection;
    
    /**
     * Constructor.
     * @param connection The server connection.
     */
    public DashboardService(PCCConnectionInterface connection){
        this.connection = connection;
    }
    
    /**
     * Returns the amount of dashboards available.
     * This is the preferred method to check if there are dashboards available. with this amount you can show 
     * an user there are no, 1 or more dashboards available so an user can select one to use.
     * @return Observable int property of the amount of dashbords.
     */
    public final ReadOnlyIntegerPropertyBindingBean getDashboardAmount(){
        return dashAmount.getReadOnlyBooleanPropertyBindingBean();
    }
    
    /**
     * Returns true if one or more dashboards are available.
     * @return boolean true when dashboards are there.
     * Use the getDashboardsList() to get the dashboards.
     */
    public final boolean hasDashboard(){
        return !dashList.isEmpty();
    }
    
    /**
     * Initializes the dashboard service and starts listeners.
     */
    @Override
    protected void initilialize() {
        this.connection.addPCCConnectionNameSpaceListener("DashboardService", this);
    }

    /**
     * Releases all dashboard service data and listeners.
     */
    @Override
    protected void release() {
        this.connection.removePCCConnectionNameSpaceListener("DashboardService", this);
        this.connection = null;
    }

    /**
     * Preloads dashboard service dashboards.
     * @throws EntityNotAvailableException When preloading fails.
     */
    @Override
    public void preload() throws EntityNotAvailableException {
        if(!loaded){
            loaded = true;
            try {
                loadInitialDashboardList();
            } catch (DashboardServiceException ex) {
                throw new EntityNotAvailableException("Could not preload dashboards list", ex);
            }
        }
    }

    /**
     * Reloads all known dashboards.
     * @throws EntityNotAvailableException When reloading fails.
     */
    @Override
    public void reload() throws EntityNotAvailableException {
        loaded = false;
        dashList.clear();
        preload();
    }

    /**
     * Unloads only data.
     * You must call reload after this if you want the data to be available again.
     * @throws EntityNotAvailableException When unloading fails.
     */
    @Override
    public void unloadContent() throws EntityNotAvailableException {
        dashList.clear();
    }

    /**
     * Loads the initial macro list.
     * @throws UserServiceException 
     */
    private void loadInitialDashboardList() throws DashboardServiceException {
        if(dashList.isEmpty()){
            try {
                handleRPCCommandByResult(this.connection.getJsonHTTPRPC("DashboardService.getDashboard", null, "DashboardService.getDashboard"));
            } catch (PCCEntityDataHandlerException ex) {
                throw new DashboardServiceException("Problem getting dashboard", ex);
            }
        }
    }
    
    /**
     * Returns a observable read only list of macros.
     * @return Returns a bindable list of macros.
     */
    public final ReadOnlyObservableArrayListBean<Dashboard> getDashboardsList() {
        return readOnlyDashboardsList;
    }
    
    /**
     * Handles dashboard bound data when publicized.
     * @param rpcDataHandler Dashboard data from RPC as PCCEntityDataHandler
     */
    @Override
    public void handleRPCCommandByBroadcast(PCCEntityDataHandler rpcDataHandler) {
        Map<String,Object> data = (Map<String,Object>)rpcDataHandler.getParameters();
        switch(rpcDataHandler.getMethod()){
            case "saveClientDashboard":
                for(Dashboard dash:dashList){
                    if(dash.getClientId()==((Number)data.get("clientid")).intValue()){
                        try {
                            dash.reBuild((List<Map<String, Object>>)((Map<String, Object>)this.connection.getJsonHTTPRPC("DashboardService.getDashboard", null, "DashboardService.getDashboard").getResult().get("data")).get("data"));
                        } catch (PCCEntityDataHandlerException ex) {
                            Logger.getLogger(DashboardService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            break;
            case "saveUserMobileDashboard":
                for(Dashboard dash:dashList){
                    if(dash.getClientId()==((Number)data.get("clientid")).intValue() && dash.getPersonId()==((Number)data.get("personid")).intValue()){
                        try {
                            dash.reBuild((List<Map<String, Object>>)((Map<String, Object>)this.connection.getJsonHTTPRPC("DashboardService.getDashboard", null, "DashboardService.getDashboard").getResult().get("data")).get("data"));
                        } catch (PCCEntityDataHandlerException ex) {
                            Logger.getLogger(DashboardService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            break;
            case "deleteDashboard":
                for(Dashboard dash:dashList){
                    if(dash.getId()==((Number)data.get("id")).intValue()){
                        try {
                            dash.reBuild((List<Map<String, Object>>)((Map<String, Object>)this.connection.getJsonHTTPRPC("DashboardService.getDashboard", null, "DashboardService.getDashboard").getResult().get("data")).get("data"));
                        } catch (PCCEntityDataHandlerException ex) {
                            Logger.getLogger(DashboardService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            break;
        }
    }

    /**
     * Handles dashboard data fetched by a command.
     * @param rpcDataHandler Dashboard data from RPC as PCCEntityDataHandler
     */
    @Override
    public void handleRPCCommandByResult(PCCEntityDataHandler rpcDataHandler) {
       Map<String,Object> data = (Map<String,Object>)rpcDataHandler.getResult().get("data");
        Runnable run = () -> {
            try {
                Dashboard dash = new Dashboard(((Number)data.get("id")).intValue(), (String)data.get("name"));
                dash.setClientId(((Number)data.get("clientid")).intValue());
                dash.setPersonId(((Number)data.get("personid")).intValue());
                dash.setUid(((Number)data.get("personid")).intValue());
                dash.setDashboardType((String)data.get("clienttype"));
                dash.build((List<Map<String, Object>>)data.get("data"));
                dashList.add(dash);
            } catch (Exception ex){
                Logger.getLogger(MacroService.class.getName()).log(Level.SEVERE, "Problem creating scenes list", ex);
            }
        };
        run.run();
    }
}