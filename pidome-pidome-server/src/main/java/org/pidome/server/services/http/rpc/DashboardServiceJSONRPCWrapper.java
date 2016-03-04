/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pidome.server.services.clients.remoteclient.RemoteClient;
import org.pidome.server.services.clients.remoteclient.RemoteClient.DeviceType;
import org.pidome.server.services.dashboard.Dashboard;
import org.pidome.server.services.dashboard.DashboardService;
import org.pidome.server.services.dashboard.DashboardServiceException;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.services.clients.persons.PersonBaseRole;
import org.pidome.server.services.clients.persons.PersonBaseRole.BaseRole;

/**
 *
 * @author John
 */
public class DashboardServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements DashboardServiceJSONRPCWrapperInterface {

    static org.apache.logging.log4j.Logger LOG = LogManager.getLogger(DashboardServiceJSONRPCWrapper.class);
    
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getDashboard", null);
                put("getDashboardById", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                    }
                });
                put("deleteDashboard", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                    }
                });
                put("getDashboards", null);
                put("getUserDashboardByClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("personid", 0);}});
                    }
                });
                put("getClientDashboard", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("clientid", 0);}});
                    }
                });
                put("getUserDashboard", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("personid" , "");}});
                    }
                });
                put("saveUserDashboard", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("personid", 0);}});
                        put(3,new HashMap<String,Object>(){{put("type", "");}});
                        put(4,new HashMap<String,Object>(){{put("construct", new Object());}});
                    }
                });
                put("saveUserMobileDashboard", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("personid", 0);}});
                        put(3,new HashMap<String,Object>(){{put("clientid", 0);}});
                        put(4,new HashMap<String,Object>(){{put("construct", new Object());}});
                    }
                });
                put("saveUserDashboardForClient", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("personid", 0);}});
                        put(3,new HashMap<String,Object>(){{put("clientid", 0);}});
                        put(4,new HashMap<String,Object>(){{put("construct", new Object());}});
                    }
                });
                put("saveClientDashboard", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0);}});
                        put(1,new HashMap<String,Object>(){{put("name", "");}});
                        put(2,new HashMap<String,Object>(){{put("clientid", 0);}});
                        put(3,new HashMap<String,Object>(){{put("construct", new Object());}});
                    }
                });
            }
        };
        return mapping;
    }
    
    private Map<String,Object> constructDashBoardData(Dashboard dashboard) throws DashboardServiceException{
        try {
            Map<String,Object> returnData = new HashMap<>();
            returnData.put("id",         dashboard.getId());
            returnData.put("data",       new JSONParser().parse(dashboard.getConstruct()));
            returnData.put("name",       dashboard.getName());
            returnData.put("clienttype", dashboard.getClientType());
            returnData.put("clientid",   dashboard.getClientId());
            returnData.put("clientname", dashboard.getClientName());
            returnData.put("personid",   dashboard.getPersonId());
            returnData.put("personname", dashboard.getPersonName());
            return returnData;
        } catch (ParseException ex) {
            LOG.error("Dashboard data composing error: {}", ex.getMessage());
            throw new DashboardServiceException("Could not create dashboard data");
        }
    }
    
    @Override
    public Object getDashboard() throws DashboardServiceException {
        if(this.getCallerResource().getDeviceType() == DeviceType.MOBILE){
            return constructDashBoardData(DashboardService.getDashboard(this.getCaller().getId(), this.getCallerResource().getResourceId(), this.getCallerResource().getDeviceType()));
        } else {
            return constructDashBoardData(DashboardService.getDashboard(this.getCaller().getId(), 0, this.getCallerResource().getDeviceType()));
        }
    }

    @Override
    public Object getDashboardById(Long dashId) throws DashboardServiceException {
        try {
            if(this.getCaller().getRole().role()==PersonBaseRole.BaseRole.ADMIN){
                return constructDashBoardData(DashboardService.getDashboardById(dashId.intValue()));
            } else {
                return constructDashBoardData(DashboardService.getDashboardById(dashId.intValue(), this.getCaller().getId()));
            }
        } catch (Exception ex) {
            LOG.error("Unable to get dashboard: {}", ex.getMessage(), ex);
            throw new DashboardServiceException("Unable to get dashboard: " + ex.getMessage());
        }
    }
    
    @Override
    public Object getDashboards() throws DashboardServiceException {
        List<Map<String,Object>> list = new ArrayList<>();
        for(Dashboard dash:DashboardService.getDashboards()){
            Map<String,Object> dashy = new HashMap<>();
            dashy.put("id", dash.getId());
            dashy.put("name", dash.getName());
            dashy.put("clienttype", dash.getClientType());
            dashy.put("clientid", dash.getClientId());
            dashy.put("clientname", dash.getClientName());
            dashy.put("personid", dash.getPersonId());
            dashy.put("personname", dash.getPersonName());
            list.add(dashy);
        }
        return list;
    }
    
    @Override
    public Object getUserDashboardByClient(Long personId) throws DashboardServiceException {
        return constructDashBoardData(DashboardService.getUserDashboardByClient(this.getCaller().getId(), personId.intValue()));
    }

    @Override
    public Object getClientDashboard(Long clientId) throws DashboardServiceException {
        return constructDashBoardData(DashboardService.getDashboard(clientId.intValue(), 0, RemoteClient.DeviceType.DISPLAY));
    }

    @Override
    public Object getUserDashboard(Long personId, String type) throws DashboardServiceException {
        switch(type){
            case "MOBILE":
                if(this.getCallerResource().getDeviceType()==DeviceType.MOBILE){
                    return constructDashBoardData(DashboardService.getDashboard(personId.intValue(), this.getCallerResource().getResourceId(), RemoteClient.DeviceType.MOBILE));
                } else {
                    throw new DashboardServiceException("Only MOBILE profiles can request mobile type dashboards.");
                }
            default:
                return constructDashBoardData(DashboardService.getDashboard(personId.intValue(), 0, RemoteClient.DeviceType.WEB));
        }
    }

    @Override
    public boolean saveUserMobileDashboard(Long dashId, String name, Long personId, Long clientId, Object content) throws DashboardServiceException {
        boolean result = false;
        try {
            if(this.getCaller().getRole().role().equals(BaseRole.ADMIN)){
                result = DashboardService.saveDashboard(dashId.intValue(), name, personId.intValue(), clientId.intValue(), "MOBILE", content);
            } else if (this.getCaller().getRole().role().equals(BaseRole.USER) && this.getCaller().getId() == personId.intValue()){
                result = DashboardService.saveDashboard(dashId.intValue(), name, personId.intValue(), clientId.intValue(), "MOBILE", content);
            } else {
                throw new DashboardServiceException("Not allowed to save this dashboard");
            }
        } catch (Exception ex) {
            LOG.error("Error checking valid user data: {}", ex.getMessage(), ex);
            throw new DashboardServiceException("Could not possitively identify if user is allowed to save this desktop");
        }
        if(result){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", dashId);
                    put("personid", personId);
                    put("clientid", clientId);
                }
            };
            ClientMessenger.send("DashboardService", "saveUserMobileDashboard", 0, sendObject);
        }
        return result;
    }
    
    @Override
    public boolean saveUserDashboard(Long dashId, String name, Long personId, String type, Object content) throws DashboardServiceException {
        boolean result = false;
        try {
            if(this.getCaller().getRole().role().equals(BaseRole.ADMIN)){
                result = DashboardService.saveDashboard(dashId.intValue(), name, personId.intValue(), 0, type, content);
            } else if (this.getCaller().getRole().role().equals(BaseRole.USER) && this.getCaller().getId() == personId.intValue()){
                result = DashboardService.saveDashboard(dashId.intValue(), name, personId.intValue(), 0, type, content);
            } else {
                throw new DashboardServiceException("Not allowed to save this dashboard");
            }
        } catch (Exception ex) {
            LOG.error("Error checking valid user data: {}", ex.getMessage(), ex);
            throw new DashboardServiceException("Could not possitively identify if user is allowed to save this desktop");
        }
        if(result){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", dashId);
                    put("personid", personId);
                }
            };
            ClientMessenger.send("DashboardService", "saveUserDashboard", 0, sendObject);
        }
        return result;
    }
    
    @Override
    public boolean saveUserDashboardForClient(Long dashId, String name, Long personId, Long clientId, Object content) throws DashboardServiceException {
        boolean result = false;
        try {
            if(this.getCaller().getRole().role().equals(BaseRole.ADMIN)){
                result = DashboardService.saveDashboard(dashId.intValue(), name, personId.intValue(), clientId.intValue(), "HYBRID", content);
            } else if (this.getCaller().getRole().role().equals(BaseRole.USER) && this.getCaller().getId() == personId.intValue()){
                result = DashboardService.saveDashboard(dashId.intValue(), name, personId.intValue(), clientId.intValue(), "HYBRID", content);
            } else {
                throw new DashboardServiceException("Not allowed to save this dashboard");
            }
        } catch (Exception ex) {
            LOG.error("Error checking valid user data: {}", ex.getMessage(), ex);
            throw new DashboardServiceException("Could not possitively identify if user is allowed to save this desktop");
        }
        if(result){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", dashId);
                    put("clientid", clientId);
                    put("personid", personId);
                }
            };
            ClientMessenger.send("DashboardService", "saveUserDashboardForClient", 0, sendObject);
        }
        return result;
    }

    @Override
    public boolean saveClientDashboard(Long dashId, String name, Long clientId, Object content) throws DashboardServiceException {
        boolean result = DashboardService.saveDashboard(dashId.intValue(), name, 0, clientId.intValue(), "DISPLAY", content);
        if(result){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", dashId);
                    put("clientid", clientId);
                }
            };
            ClientMessenger.send("DashboardService", "saveClientDashboard", 0, sendObject);
        }
        return result;
    }

    @Override
    public boolean deleteDashboard(Long dashId) throws DashboardServiceException {
        boolean result = DashboardService.deleteDashboardById(dashId.intValue());
        if(result){
            Map<String, Object> sendObject = new HashMap<String, Object>() {
                {
                    put("id", dashId);
                }
            };
            ClientMessenger.send("DashboardService", "deleteDashboard", 0, sendObject);
        }
        return result;
    }

}