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

package org.pidome.server.services.http.rpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceDataControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceSelectControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceSliderControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceToggleControl;
import org.pidome.server.connector.drivers.devices.devicestructure.NotAFloatDataTypeException;
import org.pidome.server.connector.drivers.devices.devicestructure.NotAnIntDataTypeException;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryBaseInterface;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryInterface;
import org.pidome.server.connector.drivers.peripherals.software.DeviceDiscoveryScanInterface;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDevice;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredDeviceNotFoundException;
import org.pidome.server.connector.drivers.peripherals.software.DiscoveredItemsCollection;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralDriverDeviceMutationException;
import org.pidome.server.connector.drivers.peripherals.software.TimedDiscoveryException;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentAddExistingDeviceRequest;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionInterface;
import org.pidome.server.connector.interfaces.web.presentation.webfunctions.WebPresentCustomFunctionRequest;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.services.hardware.DeviceServiceException;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.system.hardware.devices.DevicesException;
import org.pidome.server.system.hardware.peripherals.PeripheralController;

/**
 *
 * @author John Sirach
 */
final class DeviceServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements DeviceServiceJSONRPCWrapperInterface {

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer,Map<String, Object>>> createFunctionalMapping() {
        Map<String,Map<Integer,Map<String, Object>>> mapping = new HashMap<String, Map<Integer,Map<String, Object>>>(){
            {
                put("getDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("setFavorite", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("favorite", new Boolean(false));}});
                    }
                });
                put("sendDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("group", "");}});
                        put(2,new HashMap<String,Object>(){{put("control", "");}});
                        put(3,new HashMap<String,Object>(){{put("action", new HashMap<String,Object>());}});
                    }
                });
                put("startDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("editDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("location", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("address", "");}});
                        put(3,new HashMap<String,Object>(){{put("name", "");}});
                        put(4,new HashMap<String,Object>(){{put("category", 0L);}});
                        put(5,new HashMap<String,Object>(){{put("favorite", 0L);}});
                        put(6,new HashMap<String,Object>(){{put("settings", new HashMap<>());}});
                        put(7,new HashMap<String,Object>(){{put("modifiers", new ArrayList<Map<String,Object>>());}});
                    }
                });
                put("addDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("location", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("address", "");}});
                        put(3,new HashMap<String,Object>(){{put("name", "");}});
                        put(4,new HashMap<String,Object>(){{put("category", 0L);}});
                        put(5,new HashMap<String,Object>(){{put("favorite", 0L);}});
                        put(6,new HashMap<String,Object>(){{put("settings", new HashMap<>());}});
                        put(7,new HashMap<String,Object>(){{put("modifiers", new ArrayList<Map<String,Object>>());}});
                    }
                });
                put("deleteDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getDeviceActionGroups", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("filter", new Object());}});
                    }
                });
                put("getDeviceActionGroupCommands", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("groupid", "");}});
                        put(2,new HashMap<String,Object>(){{put("filter", new Object());}});
                    }
                });
                put("getDeviceCommand", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("groupid", "");}});
                        put(2,new HashMap<String,Object>(){{put("commandid", "");}});
                    }
                });
                put("getInstalledDevices", null);
                put("getDeclaredDevices", null);
                put("getDeclaredDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("peripheralDeviceFunction", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("peripheralport", "");}});
                        put(1,new HashMap<String,Object>(){{put("params", new HashMap<String,Object>());}});
                    }
                });
                put("getActiveDevices", null);
                put("setVisualDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("x", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("y", 0L);}});
                        put(3,new HashMap<String,Object>(){{put("old", 0L);}});
                    }
                });
                put("updateVisualDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                        put(1,new HashMap<String,Object>(){{put("x", 0L);}});
                        put(2,new HashMap<String,Object>(){{put("y", 0L);}});
                    }
                });
                put("removeVisualDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getVisualDevices", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("floorid", 0L);}});
                    }
                });
                put("getCustomDevices", null);
                put("getCleanCustomDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", "");}});
                    }
                });
                put("getDeclaredCustomDevices", null);
                put("getDeclaredDevicesWithFullDetails", null);
                put("getPeripheralDeclaredDevices", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("peripheralport", "");}});
                    }
                });
                put("getDevicesByPeripheralSoftwareDriver", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0L);}});
                    }
                });
                put("getFavoriteDevices", null);
                put("enableDeviceDiscovery", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("peripheralport", "");}});
                        put(1,new HashMap<String,Object>(){{put("period", 0L);}});
                    }
                });
                put("disableDeviceDiscovery", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("peripheralport", "");}});
                    }
                });
                put("getDiscoveredDevices", null);
                put("getDiscoveryEnabledDrivers", null);
                put("getDiscoveredDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("peripheralport", "");}});
                        put(1,new HashMap<String,Object>(){{put("address", "");}});
                    }
                });
                put("removeDiscoveredDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("peripheralport", "");}});
                        put(1,new HashMap<String,Object>(){{put("address", "");}});
                    }
                });
                put("getDeviceSettings", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0l);}});
                    }
                });
                put("getInstalledDeviceSettings", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0l);}});
                    }
                });
                put("getDeviceSkeleton", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0l);}});
                    }
                });
                put("addCustomDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("identifier",   "");}});
                        put(1,new HashMap<String,Object>(){{put("friendlyname", "");}});
                        put(2,new HashMap<String,Object>(){{put("devicedriver", "");}});
                        put(3,new HashMap<String,Object>(){{put("struct",       new HashMap<>());}});
                        put(4,new HashMap<String,Object>(){{put("driverid",     0l);}});
                        put(5,new HashMap<String,Object>(){{put("packageid",    0l);}});
                    }
                });
                put("updateCustomDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id",           0l);}});
                        put(1,new HashMap<String,Object>(){{put("friendlyname", "");}});
                        put(2,new HashMap<String,Object>(){{put("identifier",   "");}});
                        put(3,new HashMap<String,Object>(){{put("struct",       new HashMap<>());}});
                    }
                });
                put("deleteCustomDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0l);}});
                    }
                });
                put("assignCustomDevice", new TreeMap<Integer,Map<String, Object>>(){
                    {
                        put(0,new HashMap<String,Object>(){{put("id", 0l);}});
                        put(1,new HashMap<String,Object>(){{put("driver", 0l);}});
                    }
                });
            }
        };
        return mapping;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object sendDevice(Long deviceId, String cmdGroup, String cmdControl, Map<String,Object> action) throws DeviceServiceException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(DeviceService.getDevice(deviceId.intValue()).getLocationId())){
                return DeviceService.sendDevice(deviceId.intValue(), cmdGroup, cmdControl, action, true);
            } else {
                throw new DeviceServiceException("Not allowed");
            }
        } catch (Exception ex) {
            throw new DeviceServiceException(ex);
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object startDevice(Long deviceId) throws DeviceServiceException {
        return DeviceService.startDevice(deviceId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object editDevice(Long deviceId, Long location, String address, String name, Long category, boolean favorite, Map settings,List<Map<String,Object>> modifiers) throws DeviceServiceException {
        return DeviceService.editDevice(deviceId.intValue(), location.intValue(), address, name, category.intValue(), favorite, settings, modifiers);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object addDevice(Long device, Long location, String address, String name, Long category, boolean favorite, Map settings, List<Map<String,Object>> modifiers) throws DeviceServiceException {
        return DeviceService.saveDevice(device.intValue(), location.intValue(), address, name, category.intValue(), favorite, settings, modifiers);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object deleteDevice(Long deviceId) throws DeviceServiceException {
        return DeviceService.deleteDevice(deviceId.intValue());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getInstalledDevices() throws DeviceServiceException {
        final Map<Integer,Map<String,String>> devices = DeviceService.getAllEnabledDevices();
        List<Map<String,Object>> returnDevices = new ArrayList();
        for (final int key:devices.keySet()){
            if(devices.get(key).get("selectable").equals("1")){
                returnDevices.add(new HashMap<String,Object>(){
                    {
                        put("friendlyname", devices.get(key).get("friendlyname"));
                        put("driver", devices.get(key).get("driver"));
                        put("drivername", devices.get(key).get("drivername"));
                        put("id", key);
                    }
                });
            }
        }
        return returnDevices;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getDeclaredDevices() throws DeviceServiceException {
        List<Map<String,Object>> devices = new ArrayList<>();
        List<Map<String,Object>> base = DeviceService.getAllDeclaredDevices();
        try {
            for(int i=0;i < base.size();i++){
                if(this.getCaller().getRole().hasLocationAccess((int)base.get(i).get("location"))){
                    base.get(i).remove("xml");
                    base.get(i).remove("settings");
                    base.get(i).remove("driver");
                    base.get(i).remove("type");
                    base.get(i).remove("devicebaseid");
                    devices.add(base.get(i));
                }
            }
        } catch (Exception ex) {
            throw new DeviceServiceException(ex.getMessage());
        }
        return devices;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getDeclaredDevice(Long id) throws DeviceServiceException {
        List<Map<String,Object>> devices = DeviceService.getAllDeclaredDevices();
        Map<String,Object> returnDevice = new HashMap<>();
        for (Map<String, Object> device : devices) {
            if (id.intValue() == (int) device.get("id")) {
                returnDevice.putAll(device);
                returnDevice.remove("xml");
                returnDevice.remove("settings");
                returnDevice.remove("driver");
                returnDevice.remove("type");
                returnDevice.remove("devicebaseid");
            }
        }
        return returnDevice;
    }
    
    /**
     * Returns a list of installed device based on an active (running) hardware peripheral port.
     * @param peripheralPort
     * @return 
     */
    @Override
    public Object getPeripheralDeclaredDevices(String peripheralPort) throws DeviceServiceException {
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                return DeviceService.getInstalledDevicesInfoByDriverName(hardware.getSoftwareDriver().getPackageName());
            }
        }
        throw new DeviceServiceException("Driver not available(peripheral running?), devices can not be retreived");
    }
    
    /**
     * Executes a device action in a software peripheral.
     * @param peripheralPort
     * @param params
     * @return
     * @throws DeviceServiceException 
     */
    @Override
    public Object peripheralDeviceFunction(String peripheralPort, Map<String,Object> params) throws DeviceServiceException{
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                try {
                    if(params.get("function_id").equals("FUNCTION_ADD_DEVICE") && hardware.getSoftwareDriver() instanceof WebPresentAddExistingDeviceInterface){
                        WebPresentAddExistingDeviceRequest request = new WebPresentAddExistingDeviceRequest(DiscoveredDevice.FUNCTION_TYPE.FUNCTION_ADD_DEVICE);
                        request.setResultParams(params);
                        ((WebPresentAddExistingDeviceInterface)(hardware.getSoftwareDriver())).handleNewDeviceRequest(request);
                    } else if(params.get("function_id").equals("FUNCTION_REQUEST_ADDRESS") && hardware.getSoftwareDriver() instanceof WebPresentAddExistingDeviceInterface){
                        WebPresentAddExistingDeviceRequest request = new WebPresentAddExistingDeviceRequest(DiscoveredDevice.FUNCTION_TYPE.FUNCTION_REQUEST_ADDRESS);
                        request.setResultParams(params);
                        ((WebPresentAddExistingDeviceInterface)(hardware.getSoftwareDriver())).handleNewDeviceRequest(request);
                    } else if(params.get("function_id").equals("customFunction") && hardware.getSoftwareDriver() instanceof WebPresentCustomFunctionInterface){
                        WebPresentCustomFunctionRequest request = new WebPresentCustomFunctionRequest();
                        if(params.containsKey("identifier")){
                            request.setIdentifier((String)params.get("identifier"));
                            params.remove("identifier");
                        }
                        request.setResultParams(params);
                        ((WebPresentCustomFunctionInterface)(hardware.getSoftwareDriver())).handleCustomFunctionRequest(request);
                    } else {
                        LOG.error("Invalid request made to {} parameter set: {}", peripheralPort, params);
                        throw new DeviceServiceException("Invalid request made to " + peripheralPort + " refer to log file");
                    }
                } catch (PeripheralDriverDeviceMutationException ex) {
                    LOG.error("Device mutation exception: {}", ex.getMessage(), ex);
                    throw new DeviceServiceException(ex.getMessage());
                } catch (NullPointerException ex){
                    LOG.error("Invalid data supplied: {}", ex.getMessage(), ex);
                    throw new DeviceServiceException("No function id supplied or other error, can not handle request: " + ex.getMessage());
                } catch (Exception ex) {
                    LOG.error("Uncatched exception: {}", ex.getMessage(), ex);
                    throw new DeviceServiceException("Could not complete request: " + ex.getMessage());
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getActiveDevices() throws DeviceServiceException {
        final List<DeviceInterface> devices = DeviceService.getActiveDevices();
        List<Map<String,Object>> returnList = new ArrayList();
        for(int i=0;i<devices.size();i++){
            final DeviceInterface device = devices.get(i);
            try {
                if(this.getCaller().getRole().hasLocationAccess(device.getLocationId())){
                    Map<String,Object> details = new HashMap<String,Object>(){{
                        put("location",device.getLocationId());
                        put("locationname",device.getLocationName());
                        put("favorite",device.getIsFavorite());
                        put("id",device.getId());
                        put("category",device.getCategoryId());
                        put("categoryname",device.getCategoryName());
                        put("categoryconstant",device.getCategoryConstant());
                        put("friendlyname",device.getFriendlyName());
                        put("address",device.getAddress());
                        put("name",device.getDeviceName());
                        put("device",device.getDeviceDriver());
                        put("active", device.getisActive());
                        put("lastsend", device.getLastSendTime());
                        put("lastreceive", device.getLastReceiveTime());
                        put("status", device.getDevice().getDeviceStatus().toString().toLowerCase());
                        put("statustext", device.getDevice().getDeviceStatusText());
                    }};
                    List<Map<String,Object>> groups = new ArrayList();

                    for(DeviceControlsGroup group: device.getFullCommandSet().getControlsGroups().values()){
                        Map<String,Object> groupsBase = new HashMap<>();
                        if(!group.isHidden()){
                            groupsBase.put("id", group.getGroupId());
                            groupsBase.put("name", group.getGroupLabel());
                            ArrayList commands = new ArrayList();
                            try {
                                for (DeviceControl control : device.getFullCommandSet().getControlsGroup(group.getGroupId()).getGroupControls().values()) {
                                    if(!control.isHidden()){
                                        Map<String,Object> cmdDetails = new HashMap<>();
                                        cmdDetails.put("commandtype", control.getControlType().toString().toLowerCase());
                                        cmdDetails.put("currentvalue", control.getValue());
                                        cmdDetails.put("lastdatachange", control.getLastDataChangeAsString());
                                        cmdDetails.put("status", control.getControlStatus().toString().toLowerCase());
                                        cmdDetails.put("typedetails", getTypeDetails(control));
                                        commands.add(cmdDetails);
                                    }
                                }
                            } catch (DeviceControlsGroupException ex) {
                                LOG.error("Device {} reported group: {} does not to seem exist", device.getFriendlyName(), group.getGroupId());
                            }
                            groupsBase.put("commands", commands);
                            groups.add(groupsBase);
                        }
                    }
                    details.put("commandgroups", groups);
                    returnList.add(details);
                }
            } catch (Exception ex) {
                throw new DeviceServiceException(ex.getMessage());
            }
        }
        return returnList;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getDeviceActionGroups(Long deviceId, ArrayList<Object> filter) throws DeviceServiceException,UnknownDeviceException {
        DeviceInterface device = DeviceService.getDevice(deviceId.intValue());
        List<String> denyList    = new ArrayList<>();
        List<String> includeList = new ArrayList<>();
        List<Map<String,Object>> groups = new ArrayList();
        for (Object s : filter){
            if(s!=null){
                if(String.valueOf(s).startsWith("!")){
                    denyList.add(String.valueOf(s).substring(1));
                } else {
                    includeList.add(String.valueOf(s));
                }
            }
        }
        for(DeviceControlsGroup group: device.getFullCommandSet().getControlsGroups().values()){
            if(!group.isHidden()){
                for(DeviceControl control:group.getGroupControls().values()){
                    if(!control.isHidden()){
                        if((denyList.isEmpty() || !denyList.contains(control.getControlType().toString().toLowerCase()))){
                            if((includeList.isEmpty() || includeList.contains(control.getControlType().toString().toLowerCase()))){
                                boolean present = false;
                                for(Map<String,Object> groupItem: groups){
                                    if(groupItem.get("id").equals(group.getGroupId())){
                                        present = true;
                                        break;
                                    }
                                }
                                if(!present){
                                    Map<String,Object> groupsBase = new HashMap<>();
                                    groupsBase.put("id", group.getGroupId());
                                    groupsBase.put("name", group.getGroupLabel());
                                    groupsBase.put("controls", getDeviceActionGroupCommands(deviceId, group.getGroupId(), filter));
                                    groups.add(groupsBase);
                                }
                            }
                        }
                    }
                }
            }
        }
        return groups;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object getDeviceActionGroupCommands(Long deviceId, String group, ArrayList<Object> filter) throws DeviceServiceException, UnknownDeviceException {
        DeviceInterface device = DeviceService.getDevice(deviceId.intValue());
        List<String> denyList = new ArrayList<>();
        List<String> includeList = new ArrayList<>();
        List<Map<String,Object>> commands = new ArrayList();
        for (Object s : filter) {
            if (s != null) {
                if (String.valueOf(s).startsWith("!")) {
                    denyList.add(String.valueOf(s).substring(1));
                } else {
                    includeList.add(String.valueOf(s));
                }
            }
        }
        try {
            if(!device.getFullCommandSet().getControlsGroup(group).isHidden()){
                for (DeviceControl control : device.getFullCommandSet().getControlsGroup(group).getGroupControls().values()) {
                    if(!control.isHidden()){
                        if ((denyList.isEmpty() || !denyList.contains(control.getControlType().toString().toLowerCase()))) {
                            if ((includeList.isEmpty() || includeList.contains(control.getControlType().toString().toLowerCase()))) {
                                Map<String,Object> cmdDetails = new HashMap<>();
                                cmdDetails.put("commandtype", control.getControlType().toString().toLowerCase());
                                cmdDetails.put("currentvalue", control.getValue());
                                cmdDetails.put("lastdatachange", control.getLastDataChangeAsString());
                                cmdDetails.put("status", control.getControlStatus().toString().toLowerCase());
                                cmdDetails.put("typedetails", getTypeDetails(control));
                                commands.add(cmdDetails);
                            }
                        }
                    }
                }
            }
        } catch (DeviceControlsGroupException ex) {
            Logger.getLogger(DeviceServiceJSONRPCWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return commands;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getDeviceCommand(Long deviceId, String group, String controlId) throws DeviceServiceException, UnknownDeviceException {
        DeviceInterface device = DeviceService.getDevice(deviceId.intValue());
        Map<String,Object> command = new HashMap<>();
        try {
            if(!device.getFullCommandSet().getControlsGroup(group).isHidden()){
                if(!device.getFullCommandSet().getControlsGroup(group).getDeviceControl(controlId).isHidden()){
                    DeviceControl control = device.getFullCommandSet().getControlsGroup(group).getDeviceControl(controlId);
                    command.put("commandtype", control.getControlType().toString().toLowerCase());
                    command.put("currentvalue", control.getValue());
                    command.put("lastdatachange", control.getLastDataChangeAsString());
                    command.put("status", control.getControlStatus().toString().toLowerCase());
                    command.put("typedetails", getTypeDetails(control));
                }
            }
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("Could not get device control: {}", controlId);
            throw new DeviceServiceException("Could not get device control: " + controlId);
        }
        return command;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getDevice(Long deviceId) throws UnknownDeviceException,DeviceServiceException {
        final DeviceInterface device = DeviceService.getDevice(deviceId.intValue());
        try {
            if(this.getCaller().getRole().hasLocationAccess(device.getLocationId())){
                Map<String,Object> details = new HashMap<String,Object>(){{
                    put("location",device.getLocationId());
                    put("locationname",device.getLocationName());
                    put("favorite",device.getIsFavorite());
                    put("id",device.getId());
                    put("category",device.getCategoryId());
                    put("categoryname",device.getCategoryName());
                    put("categoryconstant",device.getCategoryConstant());
                    put("friendlyname",device.getFriendlyName());
                    put("address",device.getAddress());
                    put("name",device.getDeviceName());
                    put("device",device.getDriverName());
                    put("driver",device.getDeviceDriver());
                    put("active", device.getisActive());
                    put("lastsend", device.getLastSendTime());
                    put("lastreceive", device.getLastReceiveTime());
                    put("defsequence", device.getDefinitionSequence());
                    put("status", device.getDevice().getDeviceStatus().toString().toLowerCase());
                    put("statustext", device.getDevice().getDeviceStatusText());
                }};
                List<Map<String,Object>> groups = new ArrayList();
                
                for(DeviceControlsGroup group: device.getFullCommandSet().getControlsGroups().values()){
                    Map<String,Object> groupsBase = new HashMap<>();
                    if(!group.isHidden()){
                        groupsBase.put("id", group.getGroupId());
                        groupsBase.put("name", group.getGroupLabel());
                        ArrayList commands = new ArrayList();
                        try {
                            for (DeviceControl control : device.getFullCommandSet().getControlsGroup(group.getGroupId()).getGroupControls().values()) {
                                if(!control.isHidden()){
                                    Map<String,Object> cmdDetails = new HashMap<>();
                                    cmdDetails.put("commandtype", control.getControlType().toString().toLowerCase());
                                    cmdDetails.put("currentvalue", control.getValue());
                                    cmdDetails.put("lastdatachange", control.getLastDataChangeAsString());
                                    cmdDetails.put("status", control.getControlStatus().toString().toLowerCase());
                                    cmdDetails.put("typedetails", getTypeDetails(control));
                                    commands.add(cmdDetails);
                                }
                            }
                        } catch (DeviceControlsGroupException ex) {
                            LOG.error("Device {} reported group: {} does not to seem exist", device.getFriendlyName(), group.getGroupId());
                        }
                        groupsBase.put("commands", commands);
                        groups.add(groupsBase);
                    }
                }
                details.put("commandgroups", groups);
                return details;
            } else {
                throw new DeviceServiceException("Not allowed");
            }
        } catch (Exception ex) {
            LOG.error("Problem constructing device response: ", ex.getMessage(), ex);
            throw new DeviceServiceException(ex.getMessage());
        }
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Object getDeclaredDevicesWithFullDetails() throws DeviceServiceException {
        List<Map<String,Object>> devices = new ArrayList<>();
        
        for(Map<String,Object> devicesSet:DeviceService.getAllDeclaredDevices()){
            DeviceInterface device = null;
            try {
                device = DeviceService.getDevice((int)devicesSet.get("id"));
            } catch (Exception ex){
                try {
                    device = DeviceService.getOfflineDeviceInstance((int)devicesSet.get("id"));
                } catch (UnknownDeviceException ex1) {
                    LOG.error("Could not create any instance of device: {}", devicesSet, ex1);
                }
            }
            if(device!=null){
                try {
                    if(this.getCaller().getRole().hasLocationAccess(device.getLocationId())){
                        Map<String,Object> details = new HashMap<String,Object>();
                        details.put("location",device.getLocationId());
                        details.put("locationname",device.getLocationName());
                        details.put("favorite",device.getIsFavorite());
                        details.put("id",device.getId());
                        details.put("category",device.getCategoryId());
                        details.put("categoryname",device.getCategoryName());
                        details.put("categoryconstant",device.getCategoryConstant());
                        details.put("friendlyname",device.getFriendlyName());
                        details.put("address",device.getAddress());
                        details.put("name",device.getDeviceName());
                        details.put("device",device.getDriverName());
                        details.put("driver",device.getDeviceDriver());
                        details.put("active", device.getisActive());
                        details.put("defsequence", device.getDefinitionSequence());
                        details.put("status", device.getDevice().getDeviceStatus().toString().toLowerCase());
                        details.put("statustext", device.getDevice().getDeviceStatusText());
                        List<Map<String,Object>> groups = new ArrayList();

                        for(DeviceControlsGroup group: device.getFullCommandSet().getControlsGroups().values()){
                            Map<String,Object> groupsBase = new HashMap<>();
                            if(!group.isHidden()){
                                groupsBase.put("id", group.getGroupId());
                                groupsBase.put("name", group.getGroupLabel());
                                ArrayList commands = new ArrayList();
                                try {
                                    for (DeviceControl control : device.getFullCommandSet().getControlsGroup(group.getGroupId()).getGroupControls().values()) {
                                        if(!control.isHidden()){
                                            Map<String,Object> cmdDetails = new HashMap<>();
                                            cmdDetails.put("commandtype", control.getControlType().toString().toLowerCase());
                                            cmdDetails.put("currentvalue", control.getValue());
                                            cmdDetails.put("lastdatachange", control.getLastDataChangeAsString());
                                            cmdDetails.put("status", control.getControlStatus().toString().toLowerCase());
                                            cmdDetails.put("typedetails", getTypeDetails(control));
                                            commands.add(cmdDetails);
                                        }
                                    }
                                } catch (DeviceControlsGroupException ex) {
                                    LOG.error("Device {} reported group: {} does not to seem exist", device.getFriendlyName(), group.getGroupId());
                                }
                                groupsBase.put("commands", commands);
                                groups.add(groupsBase);
                            }
                        }
                        details.put("commandgroups", groups);
                        devices.add(details);
                    }
                } catch (Exception ex) {
                    throw new DeviceServiceException(ex.getMessage());
                }
            }
        }
        return devices;
    }
    
    
    /**
     * Returns the type details.
     * @param control
     * @return 
     */
    private Map<String,Object> getTypeDetails(DeviceControl control){
        Map<String,Object> typeDetails = new HashMap<>();
        typeDetails.put("datatype"          ,control.getDataType().toString().toLowerCase());
        typeDetails.put("id"                ,control.getControlId());
        typeDetails.put("label"             ,control.getDescription());
        typeDetails.put("deviceCommandValue",control.getValue());
        if(control.hasShortCut()){
            typeDetails.put("shortcut", control.getShortCutPosition());
        }
        typeDetails.put("commandset"        ,new ArrayList<>());
        switch(control.getControlType()){
            case DATA:
                typeDetails.put("prefix"   , ((DeviceDataControl)control).getPrefix());
                typeDetails.put("suffix"   , ((DeviceDataControl)control).getSuffix());
                typeDetails.put("graph"    , ((DeviceDataControl)control).hasGraph());
                typeDetails.put("readonly" , ((DeviceDataControl)control).isReadOnly());
                
                typeDetails.put("boolvis" , ((DeviceDataControl)control).getBoolVisualType().toString().toLowerCase());
                typeDetails.put("truetext" , ((DeviceDataControl)control).getTrueText());
                typeDetails.put("falsetext" , ((DeviceDataControl)control).getFalseText());
                
                try {
                    typeDetails.put("minvalue", ((DeviceDataControl)control).getMinValueInt());
                    typeDetails.put("maxvalue", ((DeviceDataControl)control).getMaxValueInt());
                    typeDetails.put("warnvalue", ((DeviceDataControl)control).getWarnValueInt());
                    typeDetails.put("highvalue", ((DeviceDataControl)control).getHighValueInt());
                } catch (NotAnIntDataTypeException ex) {
                    try {
                        typeDetails.put("minvalue", ((DeviceDataControl)control).getMinValueFloat());
                        typeDetails.put("maxvalue", ((DeviceDataControl)control).getMaxValueFloat());
                        typeDetails.put("warnvalue", ((DeviceDataControl)control).getWarnValueFloat());
                        typeDetails.put("highvalue", ((DeviceDataControl)control).getHighValueFloat());
                    } catch (NotAFloatDataTypeException ex1) {
                        typeDetails.put("minvalue", "");
                        typeDetails.put("maxvalue", "");
                        typeDetails.put("warnvalue", "");
                        typeDetails.put("highvalue", "");
                    }
                }
                if(((DeviceDataControl)control).hasGraph()){
                    typeDetails.put("graphtype", ((DeviceDataControl)control).getGraph());
                } else {
                    typeDetails.put("graphtype", "");
                }
                typeDetails.put("visual"    , ((DeviceDataControl)control).hasVisual());
                if(((DeviceDataControl)control).hasVisual()){
                    typeDetails.put("visualtype", ((DeviceDataControl)control).getVisual());
                } else {
                    typeDetails.put("visualtype", "");
                }
            break;
            case SELECT:
                typeDetails.put("commandset", ((DeviceSelectControl)control).getFullSelectList());
            break;
            case BUTTON:
                //// all data is already there.
            break;
            case TOGGLE:
                typeDetails.put("commandset", ((DeviceToggleControl)control).getFullToggleMap());
            break;
            case COLORPICKER:
                typeDetails.put("commandset", ((DeviceColorPickerControl)control).getFullButtonsList());
                typeDetails.put("color"     , ((DeviceColorPickerControl)control).getFullColorMap());
                typeDetails.put("mode"      , ((DeviceColorPickerControl)control).getMode());
            break;
            case SLIDER:
                typeDetails.put("min", ((DeviceSliderControl)control).getMin());
                typeDetails.put("max", ((DeviceSliderControl)control).getMax());
            break;
        }
        return typeDetails;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Object setFavorite(Long deviceId, Boolean favorite) throws DeviceServiceException, UnknownDeviceException {
        try {
            if(this.getCaller().getRole().hasLocationAccess(DeviceService.getDevice(deviceId.intValue()).getLocationId())){
                 return DeviceService.setAsFavorite(deviceId.intValue(), favorite);
            } else {
                throw new DeviceServiceException("Not allowed");
            }
        } catch (Exception ex) {
            throw new DeviceServiceException(ex.getMessage());
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public final boolean setVisualDevice(Long deviceId, Long x, Long y, Long old) throws DeviceServiceException, UnknownDeviceException {
        if(old.intValue()!=0){
            DeviceService.removeVisualDimenions(old.intValue());
        }
        return DeviceService.setVisualDimenions(deviceId.intValue(), x.intValue(), y.intValue());
    }

    @Override
    public final boolean updateVisualDevice(Long deviceId, Long x, Long y) throws DeviceServiceException, UnknownDeviceException {
        return DeviceService.setVisualDimenions(deviceId.intValue(), x.intValue(), y.intValue());
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final boolean removeVisualDevice(Long deviceId) throws DeviceServiceException, UnknownDeviceException {
        return DeviceService.removeVisualDimenions(deviceId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public final Object getVisualDevices(Long floorId) throws DeviceServiceException {
        return DeviceService.getVisualDevices(floorId.intValue());
    }

    /**
     * @inheritDoc
     */
    @Override
    public final Object getCustomDevices() throws DeviceServiceException {
        Map<Integer,Map<String,String>> devices = DeviceService.getAllCustomDevices();
        ArrayList<Map<String,String>> deviceList = new ArrayList<>();
        for(Map<String,String> singleDevice:devices.values()){
            deviceList.add(singleDevice);
        }
        return deviceList;
    }

    /**
     * Returns a custom device as json without xml data.
     * @param deviceId
     * @return 
     */
    @Override
    public final Object getCleanCustomDevice(String deviceId){
        Map<String,Object> device = DeviceService.getCustomDevice(deviceId);
        return device;
    }
    
    /**
     * Returns only the active favorite devices
     * @return
     * @throws DeviceServiceException 
     */
    @Override
    public final Object getFavoriteDevices() throws DeviceServiceException {
        final List<DeviceInterface> devices = DeviceService.getActiveDevices();
        List<Map<String,Object>> returnList = new ArrayList();
        for(int i=0;i<devices.size();i++){
            final DeviceInterface device = devices.get(i);
            try {
                if(this.getCaller().getRole().hasLocationAccess(device.getLocationId())){
                    if(device.getIsFavorite()){
                        Map<String,Object> details = new HashMap<>();
                        details.put("id",device.getId());
                        details.put("category",device.getCategoryId());
                        details.put("categoryname",device.getCategoryName());
                        details.put("friendlyname",device.getDeviceName());
                        details.put("location",device.getLocationId());
                        details.put("locationname",device.getLocationName());
                        details.put("categoryconstant",device.getCategoryConstant());
                        details.put("defsequence", device.getDefinitionSequence());
                        details.put("status", device.getDevice().getDeviceStatus().toString().toLowerCase());
                        details.put("statustext", device.getDevice().getDeviceStatusText());
                        List<Map<String,Object>> groups = new ArrayList();
                        for(DeviceControlsGroup group: device.getFullCommandSet().getControlsGroups().values()){
                            Map<String,Object> groupsBase = new HashMap<>();
                            if(!group.isHidden()){
                                groupsBase.put("id", group.getGroupId());
                                groupsBase.put("name", group.getGroupLabel());
                                ArrayList commands = new ArrayList();
                                try {
                                    for (DeviceControl control : device.getFullCommandSet().getControlsGroup(group.getGroupId()).getGroupControls().values()) {
                                        if(!control.isHidden()){
                                            Map<String,Object> cmdDetails = new HashMap<>();
                                            cmdDetails.put("commandtype", control.getControlType().toString().toLowerCase());
                                            cmdDetails.put("currentvalue", control.getValue());
                                            cmdDetails.put("lastdatachange", control.getLastDataChangeAsString());
                                            cmdDetails.put("status", control.getControlStatus().toString().toLowerCase());
                                            cmdDetails.put("typedetails", getTypeDetails(control));
                                            commands.add(cmdDetails);
                                        }
                                    }
                                } catch (DeviceControlsGroupException ex) {
                                    LOG.error("Device {} reported group: {} does not to seem exist", device.getFriendlyName(), group.getGroupId());
                                }
                                groupsBase.put("commands", commands);
                                groups.add(groupsBase);
                            }
                        }
                        details.put("commandgroups", groups);
                        returnList.add(details);
                    }
                }
            } catch (Exception ex) {
                throw new DeviceServiceException(ex.getMessage());
            }
        }
        return returnList;
    }

    /**
     * Return a list of devices identified by the given software driver.
     * @param id
     * @return 
     */
    @Override
    public Object getDevicesByPeripheralSoftwareDriver(Long id) {
        return DeviceService.getInstalledDevicesByPeripheralSoftwareDriver(id.intValue());
    }

    /**
     * Returns a list of possible all custom devices.
     * @return 
     */
    @Override
    public Object getDeclaredCustomDevices() {
        return DeviceService.getDeclaredCustomDevices();
    }
    
    /**
     * Returns a list of discovered devices.
     * @return 
     */
    @Override
    public List<Map<String,Object>> getDiscoveredDevices(){
        List<Map<String,Object>> items = new ArrayList<>();
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if(hardware.getSoftwareDriver() instanceof DeviceDiscoveryBaseInterface){
                Map<String,Object> driverDetails = new HashMap<>();
                driverDetails.put("name", hardware.getSoftwareDriver().getFriendlyName());
                driverDetails.put("port", hardware.getPeripheral().getDevicePort());
                driverDetails.put("devices", new ArrayList<>());
                try {
                    for(DiscoveredDevice device:DiscoveredItemsCollection.getDiscoveredDevices((DeviceDiscoveryBaseInterface)hardware.getSoftwareDriver())){
                        Map<String,Object> found = new HashMap<>();
                        found.put("name",        device.getName());
                        found.put("address",     device.getAddress());
                        found.put("time",        device.getDiscoveryDateTime());
                        found.put("type",        device.getFunctionType());
                        if(device.getDeviceDriver()!=null){
                            found.put("knowndevice", DeviceService.getInstalledDeviceByDriverName(device.getDeviceDriver(), hardware.getSoftwareDriver().getId()));
                        } else {
                            found.put("knowndevice", new HashMap<>());
                        }
                        ((List)driverDetails.get("devices")).add(found);
                    }
                    items.add(driverDetails);
                } catch (DiscoveredDeviceNotFoundException ex){
                    //// No discovered devices available.
                } catch (Exception ex) {
                    //// No devices found
                    LOG.error("[Discard this message, it is development debugging] Driver is member of device mutations but does not implement discovery correctly: {} (initial details: {})", ex.getMessage(), driverDetails,ex);
                }
            }
        }
        return items;
    }   

    /**
     * Returns a single found device's information.
     * @param peripheralPort
     * @param deviceAddress
     * @return
     * @throws DiscoveredDeviceNotFoundException 
     */
    @Override
    public Map<String,Object> getDiscoveredDevice(String peripheralPort, String deviceAddress) throws DiscoveredDeviceNotFoundException {
        Map<String,Object> info = new HashMap<>();
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                DiscoveredDevice device = DiscoveredItemsCollection.getDiscoveredDevice((DeviceDiscoveryBaseInterface)hardware.getSoftwareDriver(), deviceAddress);
                info.put("name",            device.getName());
                info.put("address",         device.getAddress());
                info.put("time",            device.getDiscoveryDateTime());
                info.put("type",            device.getFunctionType());
                info.put("description",     device.getDescription());
                info.put("peripheralport",  hardware.getPeripheral().getDevicePort());
                info.put("parameters",      device.getParameterValues());
                info.put("information",     device.getVisualInformation());
                info.put("newaddress",      device.getNewAddress());
                info.put("hasbuildskeleton",device.isAutoCreate());
                if(device.getDeviceDriver()!=null){
                    info.put("knowndevice", DeviceService.getInstalledDeviceByDriverName(device.getDeviceDriver(), hardware.getSoftwareDriver().getId()));
                } else {
                    info.put("knowndevice", new HashMap<>());
                }
            }
        }
        return info;
    }
    
    /**
     * Returns a single found device's information.
     * @param peripheralPort
     * @param deviceAddress
     * @return
     * @throws DiscoveredDeviceNotFoundException 
     */
    @Override
    public boolean removeDiscoveredDevice(String peripheralPort, String deviceAddress) throws DiscoveredDeviceNotFoundException {
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                if(hardware.getSoftwareDriver() instanceof DeviceDiscoveryBaseInterface){
                    DiscoveredItemsCollection.removeDiscoveredDevice((DeviceDiscoveryInterface)hardware.getSoftwareDriver(), deviceAddress);
                    try {
                        String port = hardware.getPeripheralHardwareDriver().getPort();
                        Runnable run = () -> {
                            LOG.info("Removed device. Driver: {}, Device info - name: {}, address: {}, visual: {}, parameters: {}", ((DeviceDiscoveryInterface) hardware.getSoftwareDriver()).getName(), deviceAddress);
                            Map<String, Object> sendObject = new HashMap<String, Object>() {
                                {
                                    put("port", port);
                                    put("deviceaddress", deviceAddress);
                                }
                            };
                            ClientMessenger.send("DeviceService", "removedDiscoveredDevice", 0, sendObject);
                        };
                        run.run();
                    } catch (PeripheralHardwareException ex) {
                        Logger.getLogger(DeviceServiceJSONRPCWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Enable device discovery.
     * @param peripheralPort
     * @param period
     * @return
     * @throws DiscoveredDeviceNotFoundException 
     */
    @Override
    public Object enableDeviceDiscovery(String peripheralPort, Long period) throws TimedDiscoveryException {
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                if(hardware.getSoftwareDriver() instanceof DeviceDiscoveryBaseInterface){
                    ((DeviceDiscoveryInterface)hardware.getSoftwareDriver()).enableDiscovery(period.intValue());
                } else {
                    LOG.warn("Selected driver {} does not do auto discovery", hardware.getSoftwareDriver().getFriendlyName());
                }
            }
        }
        return true;
    }
    
    /**
     * Enable device discovery.
     * @param peripheralPort
     * @param period
     * @return
     * @throws DiscoveredDeviceNotFoundException 
     */
    @Override
    public Object disableDeviceDiscovery(String peripheralPort) throws TimedDiscoveryException {
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                if(hardware.getSoftwareDriver() instanceof DeviceDiscoveryBaseInterface){
                    ((DeviceDiscoveryInterface)hardware.getSoftwareDriver()).disableDiscovery();
                } else {
                    LOG.warn("Selected driver {} does not do auto discovery", hardware.getSoftwareDriver().getFriendlyName());
                }
            }
        }
        return true;
    }
    
    /**
     * Enable device discovery.
     * @param peripheralPort
     * @param period
     * @return
     * @throws DiscoveredDeviceNotFoundException 
     */
    @Override
    public Object getDiscoveryEnabledDrivers() {
        List<Map<String,Object>> driverList = new ArrayList<>();
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if(hardware.getSoftwareDriver() instanceof DeviceDiscoveryBaseInterface){
                Map<String,Object> info = new HashMap<>();
                info.put("name", hardware.getSoftwareDriver().getFriendlyName());
                info.put("port", hardware.getPeripheral().getDevicePort());
                try {
                    info.put("timer", ((DeviceDiscoveryInterface)hardware.getSoftwareDriver()).getDiscoveryTime());
                } catch (TimedDiscoveryException ex) {
                    LOG.error("Unable to retreive the timer set on the discovery of {}. Reason: {}",hardware.getSoftwareDriver().getFriendlyName(),ex.getMessage());
                    info.put("timer", 0);
                }
                info.put("found", ((DeviceDiscoveryInterface)hardware.getSoftwareDriver()).getDiscoveredAmount());
                info.put("active", ((DeviceDiscoveryInterface)hardware.getSoftwareDriver()).discoveryIsEnabled());
                Map<String,Object> discoverySupportTypes = new HashMap<>();
                discoverySupportTypes.put("discovery", hardware.getSoftwareDriver() instanceof DeviceDiscoveryInterface);
                discoverySupportTypes.put("scan", hardware.getSoftwareDriver() instanceof DeviceDiscoveryScanInterface);
                info.put("discovertypes", discoverySupportTypes);
                driverList.add(info);
            }
        }
        return driverList;
    }
    
    public final Object getDeviceSkeleton(Number installedDeviceId){
        return DeviceService.getCustomDevice(installedDeviceId.intValue());
    }

    @Override
    public Object getDeviceStructure(Number installedId) throws DeviceServiceException {
        return DeviceService.getDeviceStruct(installedId.intValue());
    }

    @Override
    public Object getDeviceSettings(Number deviceId) throws DeviceServiceException,UnknownDeviceException {
        return DeviceService.getDeviceSettings(deviceId.intValue());
    }

    @Override
    public Object getInstalledDeviceSettings(Number installedDeviceId) throws DeviceServiceException, UnknownDeviceException, DevicesException {
        return DeviceService.getInstalledDeviceSettings(installedDeviceId.intValue());
    }

    @Override
    public boolean deleteCustomDevice(Number customDeviceId) {
        return DeviceService.deleteCustomDevice(customDeviceId.intValue());
    }
    
    @Override
    public int addCustomDevice(String identifier, String friendlyname, String deviceDriver, Map<String,Object> struct, Number driverId, Number packageId) throws UnsupportedDeviceException {
        return DeviceService.createCustomDevice(identifier, friendlyname, deviceDriver, struct, driverId.intValue(), packageId.intValue());
    }
 
    @Override
    public boolean updateCustomDevice(Number customDeviceId, String friendlyName, String name, Map<String,Object> struct) throws UnsupportedDeviceException {
        return DeviceService.updateCustomDevice(customDeviceId.intValue(), friendlyName, name, struct);
    }
    
    @Override
    public boolean assignCustomDevice(Number customDriverId, Number customDeviceId) throws DeviceServiceException {
        return DeviceService.assignCustomDevice(customDriverId.intValue(), customDeviceId.intValue());
    }
    
}