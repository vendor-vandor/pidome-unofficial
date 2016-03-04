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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerDevice;
import org.pidome.server.connector.drivers.devices.specials.presence.AccessControllerProxyInterface;
import org.pidome.server.services.accesscontrollers.AccesControllersService;
import org.pidome.server.services.accesscontrollers.AccessControllerDeviceWrapper;
import org.pidome.server.services.accesscontrollers.AccessControllerServiceException;
import org.pidome.server.services.accesscontrollers.AccessControllerToken;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.system.hardware.devices.DeviceStruct;

/**
 *
 * @author John
 */
public final class AccessControllerServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements AccessControllerServiceJSONRPCWrapperInterface {

    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getAccessControllers", null);
                put("getAccessControllerCandidates", null);
                put("getControllerAccessTokens", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getPersonAccessTokens", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteController", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteToken", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("deleteTokenFromAccessController", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("tokenid", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("controllerid", 0L);}});
                    }
                });
                put("createController", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("deviceid", 0L);}});
                    }
                });
                put("getAccessTokens", null);
                put("registerUserTokenByController", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("controllerid", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("userid", 0L);}});
                    }
                });
            }
        };
        return mapping;
    }

    @Override
    public final Object getAccessControllers() {
        List<Map<String,Object>> controllersSet = new ArrayList<>();
        for(AccessControllerDeviceWrapper controller:AccesControllersService.getAccessControllers()){
            try {
                DeviceStruct device = (DeviceStruct)DeviceService.getDevice(controller.getWrappedDeviceId());
                Map<String,Object>ControllerData = new HashMap<>();
                ControllerData.put("id", controller.getControllerId());
                ControllerData.put("wrappeddeviceid", device.getId());
                ControllerData.put("wrappeddevicename", device.getDevice().getDeviceName());
                ControllerData.put("wrappeddevicelocation", device.getLocationName());
                List<Map<String,String>> capabs = new ArrayList<>();
                for(AccessControllerProxyInterface.Capabilities cap:controller.getCapabilities()){
                    Map<String,String> capab = new HashMap<>();
                    capab.put("id", cap.toString());
                    capab.put("name", cap.getName());
                    capab.put("description", cap.getDescription());
                    capabs.add(capab);
                }
                ControllerData.put("capabilities", capabs);
                controllersSet.add(ControllerData);
            } catch (UnknownDeviceException ex) {
                Logger.getLogger(AccessControllerServiceJSONRPCWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return controllersSet;
    }

    @Override
    public Object getAccessControllerCandidates() {
        List<Map<String,Object>> controllersSet = new ArrayList<>();
        for(AccessControllerDevice device:AccesControllersService.getAccessControllerCandidates()){
            Map<String,Object>ControllerData = new HashMap<>();
            ControllerData.put("id", device.getId());
            ControllerData.put("name", device.getDeviceName());
            controllersSet.add(ControllerData);
        }
        return controllersSet;
    }
    
    @Override
    public final Object getControllerAccessTokens(Long accessController) throws AccessControllerServiceException {
        AccessControllerDeviceWrapper controller = AccesControllersService.getAccessController(accessController.intValue());
        List<Map<String,Object>> tokenSet = new ArrayList<>();
        try {
            for(AccessControllerToken token:controller.getAccessTokens()){
                Map<String,Object> tokenInfo = new HashMap<>();
                tokenInfo.put("id", token.getTokenId());
                tokenInfo.put("type", token.getTokenType());
                tokenInfo.put("controllerid", accessController);
                tokenInfo.put("personid", token.getPersonId());
                tokenInfo.put("personname", token.getPersonName());
                tokenSet.add(tokenInfo);
            }
        } catch (UnknownDeviceException ex) {
            throw new AccessControllerServiceException("Unknown device bound to access controller ("+ex.getMessage()+")");
        }
        return tokenSet;
    }
    
    @Override
    public final Object getAccessTokens() throws AccessControllerServiceException {
        List<Map<String,Object>> tokenSet = new ArrayList<>();
        for(AccessControllerToken token:AccesControllersService.getAccessTokens()){
            Map<String,Object> tokenInfo = new HashMap<>();
            tokenInfo.put("id", token.getTokenId());
            tokenInfo.put("type", token.getTokenType());
            tokenInfo.put("personid", token.getPersonId());
            tokenInfo.put("personname", token.getPersonName());
            tokenSet.add(tokenInfo);
        }
        return tokenSet;
    }
    
    @Override
    public final Object getPersonAccessTokens(Long personId) throws AccessControllerServiceException {
        List<Map<String,Object>> tokenSet = new ArrayList<>();
        for(AccessControllerToken token:AccesControllersService.getAccessTokensForPerson(personId.intValue())){
            Map<String,Object> tokenInfo = new HashMap<>();
            tokenInfo.put("id", token.getTokenId());
            tokenInfo.put("type", token.getTokenType());
            tokenInfo.put("personid", token.getPersonId());
            tokenInfo.put("personname", token.getPersonName());
            tokenInfo.put("wrappeddeviceid", token.getWrappedDeviceId());
            tokenInfo.put("wrappeddevicename", token.getWrappedDeviceName());
            tokenSet.add(tokenInfo);
        }
        return tokenSet;
    }

    @Override
    public boolean deleteController(Long controllerId) throws AccessControllerServiceException {
        return AccesControllersService.detachAccessController(controllerId.intValue());
    }

    @Override
    public boolean createController(Long wrappedDeviceId) throws AccessControllerServiceException {
        return AccesControllersService.bindAccessController(wrappedDeviceId.intValue());
    }

    @Override
    public boolean registerUserTokenByController(Long controllerId, Long userId) throws AccessControllerServiceException {
        return AccesControllersService.registerUserTokenByController(controllerId.intValue(), userId.intValue());
    }
    
    @Override
    public boolean deleteToken(Long tokenId) throws AccessControllerServiceException {
        return AccesControllersService.deleteToken(tokenId.intValue());
    }

    @Override
    public boolean deleteTokenFromAccessController(Long tokenId, Long acessControllerId) throws AccessControllerServiceException {
        return AccesControllersService.deleteToken(tokenId.intValue(), acessControllerId.intValue());
    }
    
}