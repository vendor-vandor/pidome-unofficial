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
package org.pidome.server.services.http.rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareException;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentation;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentationGroup;
import org.pidome.server.services.hardware.DeviceService;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.hardware.peripherals.PeripheralController;

/**
 *
 * @author John
 */
public class HardwareServiceJSONRPCWrapper extends AbstractRPCMethodExecutor implements HardwareServiceJSONRPCWrapperInterface {

    public HardwareServiceJSONRPCWrapper() {
        super();
    }

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Map<Integer, Map<String, Object>>> createFunctionalMapping() {
        Map<String, Map<Integer, Map<String, Object>>> mapping = new HashMap<String, Map<Integer, Map<String, Object>>>() {
            {
                put("getConnectedHardware", null);
                put("disconnectPeripheral", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                    }
                });
                put("getPeripheralConnectSettings", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                    }
                });
                put("getWaitingPeripheralConnectSettings", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                    }
                });
                put("setPeripheralConnectSettings", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                        put(1, new HashMap<String, Object>() {{put("parameters", new HashMap<>());}});
                    }
                });
                put("getPeripheralSoftwareDrivers", null);
                put("getScriptedSoftwareDrivers", null);
                put("getPeripheralSoftwareDriversForCustomDevices", null);
                put("getDiscoveredDevices", null);
                put("getLocalDeviceEntries", null);
                put("getSoftwareDriverPresentation", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                    }
                });
                put("getConnectedPeripheralInfo", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                    }
                });
                put("createCustomSerialDevice", new TreeMap<Integer, Map<String, Object>>() {
                    {
                        put(0, new HashMap<String, Object>() {{put("port", "");}});
                        put(1, new HashMap<String, Object>() {{put("friendlyname", "");}});
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
    public Object getConnectedHardware() {
        List<Map<String, Object>> peripheralsList = new ArrayList<>();

        Map<String, PeripheralController> waiters = DeviceService.getWaitingHardwarePeripherals();
        Map<String, PeripheralController> runners = DeviceService.getRunningHardwarePeripherals();
        Map<String, PeripheralController> unsupported = DeviceService.getAttachedUnsupportedPeripherals();

        waiters.entrySet().stream().forEach((e) -> {
            Map<String, Object> peripheralDetail = new HashMap<>();
            PeripheralController peripheral = e.getValue();
            peripheralDetail.put("key", peripheral.getPeripheral().getDeviceKey());
            peripheralDetail.put("port", peripheral.getPeripheral().getDevicePort());
            peripheralDetail.put("type", peripheral.getPeripheral().getDeviceType());
            peripheralDetail.put("friendlyname", peripheral.getPeripheral().getFriendlyName());
            peripheralDetail.put("vid", peripheral.getPeripheral().getVendorId());
            peripheralDetail.put("pid", peripheral.getPeripheral().getDeviceId());
            peripheralDetail.put("subtypetext", peripheral.getPeripheral().getSubSystem().getDescription());
            peripheralDetail.put("subtype", peripheral.getPeripheral().getSubSystem().toString());
            peripheralDetail.put("serial", peripheral.getPeripheral().getSerial());
            peripheralDetail.put("softwaredriver", "");
            peripheralDetail.put("softwaredriverversion", "");
            peripheralDetail.put("haspresentation", false);
            peripheralDetail.put("lastknownerror", peripheral.getPeripheral().getError());
            peripheralDetail.put("status", "warning");
            try {
                peripheralDetail.put("hardwaredriver", peripheral.getPeripheralHardwareDriver().getFriendlyName());
            } catch (PeripheralHardwareException ex) {
                peripheralDetail.put("hardwaredriver", "");
                peripheralDetail.put("status", "warning");
            }
            peripheralsList.add(peripheralDetail);
        });

        runners.entrySet().stream().forEach((e) -> {
            Map<String, Object> peripheralDetail = new HashMap<>();
            PeripheralController peripheral = e.getValue();
            peripheralDetail.put("key", peripheral.getPeripheral().getDeviceKey());
            peripheralDetail.put("port", peripheral.getPeripheral().getDevicePort());
            peripheralDetail.put("type", peripheral.getPeripheral().getDeviceType());
            peripheralDetail.put("friendlyname", peripheral.getPeripheral().getFriendlyName());
            peripheralDetail.put("vid", peripheral.getPeripheral().getVendorId());
            peripheralDetail.put("pid", peripheral.getPeripheral().getDeviceId());
            peripheralDetail.put("subtypetext", peripheral.getPeripheral().getSubSystem().getDescription());
            peripheralDetail.put("subtype", peripheral.getPeripheral().getSubSystem().toString());
            peripheralDetail.put("serial", peripheral.getPeripheral().getSerial());
            peripheralDetail.put("status", "success");
            peripheralDetail.put("haspresentation", false);
            peripheralDetail.put("lastknownerror", peripheral.getPeripheral().getError());
            try {
                peripheralDetail.put("softwaredriver", peripheral.getSoftwareDriver().getFriendlyName());
                peripheralDetail.put("softwaredriverversion", peripheral.getPeripheralSoftwareId().getVersion());
                peripheralDetail.put("haspresentation", peripheral.getSoftwareDriver().hasPresentation());
            } catch (Exception ex) {
                peripheralDetail.put("softwaredriver", "");
                peripheralDetail.put("softwaredriverversion", "");
                peripheralDetail.put("status", "warning");
            }
            try {
                peripheralDetail.put("hardwaredriver", peripheral.getPeripheralHardwareDriver().getFriendlyName());
            } catch (PeripheralHardwareException ex) {
                peripheralDetail.put("hardwaredriver", "Unknown");
                peripheralDetail.put("status", "error");
            }
            peripheralsList.add(peripheralDetail);
        });

        unsupported.entrySet().stream().forEach((e) -> {
            Map<String, Object> peripheralDetail = new HashMap<>();
            PeripheralController peripheral = e.getValue();
            try {
                peripheralDetail.put("key", peripheral.getPeripheral().getDeviceKey());
                peripheralDetail.put("port", peripheral.getPeripheral().getDevicePort());
                peripheralDetail.put("type", peripheral.getPeripheral().getDeviceType());
                peripheralDetail.put("friendlyname", peripheral.getPeripheral().getFriendlyName());
                peripheralDetail.put("vid", peripheral.getPeripheral().getVendorId());
                peripheralDetail.put("pid", peripheral.getPeripheral().getDeviceId());
                peripheralDetail.put("subtypetext", peripheral.getPeripheral().getSubSystem().getDescription());
                peripheralDetail.put("subtype", peripheral.getPeripheral().getSubSystem().toString());
                peripheralDetail.put("serial", peripheral.getPeripheral().getSerial());
                peripheralDetail.put("haspresentation", false);
                peripheralDetail.put("softwaredriver", "");
                peripheralDetail.put("softwaredriverversion", "");
                peripheralDetail.put("status", "error");
                peripheralDetail.put("hardwaredriver", "Unsupported");
                peripheralDetail.put("lastknownerror", peripheral.getPeripheral().getError());
                peripheralsList.add(peripheralDetail);
            } catch (Exception ex){
                ///LOG.error("Problem getting data for device: {}", peripheral.toString(), ex);
            }
        });
        
        return peripheralsList;

    }

    @Override
    public Object getSoftwareDriverPresentation(String peripheralPort) throws PeripheralSoftwareException {
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                Map<String, Object> details = new HashMap<>();
                details.put("hardware", hardware.getPeripheral().getFriendlyName());
                details.put("port", hardware.getPeripheral().getDevicePort());
                details.put("driver", hardware.getSoftwareDriver().getFriendlyName());
                List<Object> presentList = new ArrayList<>();
                if (hardware.getSoftwareDriver().hasPresentation()) {
                    for (WebPresentationGroup group : hardware.getSoftwareDriver().getWebPresentationGroups()) {
                        Map<String, Object> presentationGroup = new HashMap<>();
                        presentationGroup.put("title", group.getTitle());
                        presentationGroup.put("description", group.getDescription());
                        List<Map<String, Object>> collection = new ArrayList<>();
                        for (WebPresentation item : group.getCollection()) {
                            Map<String, Object> presentationItem = new HashMap<>();
                            presentationItem.put("label", item.getLabel());
                            presentationItem.put("type", item.getType().toString());
                            presentationItem.put("content", item.getPresentationValue());
                            collection.add(presentationItem);
                        }
                        presentationGroup.put("content", collection);
                        presentList.add(presentationGroup);
                    }
                }
                details.put("presentation", presentList);
                return details;
            }
        }
        throw new PeripheralSoftwareException("Unknown port used for peripheral");
    }

    @Override
    public Object getPeripheralSoftwareDrivers() {
        ArrayList<Map<String,Object>> set = new ArrayList<>();
        for(PeripheralSoftwareDriverInterface driver:DeviceService.getPeripheralSoftwareDrivers().values()){
            Map<String,Object> item = new HashMap<>();
            item.put("id", driver.getId());
            item.put("driverid", driver.getNamedId());
            item.put("hascustom", driver.hasCustom());
            item.put("name", driver.getFriendlyName());
            set.add(item);
        }
        return set;
    }

    @Override
    public Object getPeripheralConnectSettings(String peripheralPort) throws PeripheralSoftwareException, PeripheralHardwareException {
        Map<String, Object> options = new HashMap<>();
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                Map<String, PeripheralHardwareDriver.PeripheralOption> optionSet = hardware.getPeripheralHardwareDriver().getPeripheralOptions();
                for(PeripheralHardwareDriver.PeripheralOption option:optionSet.values()){
                    options.put(option.getOptionName(), option.getSelectedValue());
                }
            }
        }
        return options;
    }
    
    @Override
    public Object getConnectedPeripheralInfo(String peripheralPort) throws PeripheralSoftwareException, PeripheralHardwareException {
        Map<String, Object> options = new HashMap<>();
        Map<String, Object> deviceInfo = new HashMap<>();
        Map<String, Object> collection = new HashMap<>();
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort)) {
                deviceInfo.put("Registration", hardware.getPeripheral().getDeviceKey());
                deviceInfo.put("Port", hardware.getPeripheral().getDevicePort());
                deviceInfo.put("Type", hardware.getPeripheral().getDeviceType());
                deviceInfo.put("Name", hardware.getPeripheral().getFriendlyName());
                deviceInfo.put("Vendor id", hardware.getPeripheral().getVendorId());
                deviceInfo.put("Product id", hardware.getPeripheral().getDeviceId());
                deviceInfo.put("Serial", hardware.getPeripheral().getSerial());
                deviceInfo.put("Interface", hardware.getPeripheral().getSubSystem().getDescription());
                deviceInfo.put("Software driver", hardware.getSoftwareDriver().getFriendlyName());
                deviceInfo.put("Hardware driver", hardware.getPeripheralHardwareDriver().getFriendlyName());
                Map<String, PeripheralHardwareDriver.PeripheralOption> optionSet = hardware.getPeripheralHardwareDriver().getPeripheralOptions();
                for(PeripheralHardwareDriver.PeripheralOption option:optionSet.values()){
                    options.put(option.getOptionName(), option.getSelectedValue());
                }
            }
        }
        collection.put("connection", options);
        collection.put("device", deviceInfo);
        return collection;
    }
    
    /**
     * Returns a JSON list of drivers which are scripted.
     * @return 
     */
    @Override
    public Object getScriptedSoftwareDrivers(){
        return DeviceService.getScriptedDrivers();
    }
    
    @Override
    public Object getWaitingPeripheralConnectSettings(String peripheralPort) throws PeripheralSoftwareException, PeripheralHardwareException {
        Map<String, Object> options = new HashMap<>();
        PeripheralController controller = DeviceService.getWaitingHardwarePeripheral(peripheralPort);
        Map<String,PeripheralHardwareDriver.PeripheralOption> deviceOptions = controller.getPeripheralHardwareDriver().getPeripheralOptions();
        ArrayList<Map<String,Object>> connectSettingsSet = new ArrayList<>();
        for(String optionId:deviceOptions.keySet()){
            Map<String,Object> connectSettings = new HashMap<>();
            connectSettings.put("id", optionId);
            connectSettings.put("label", deviceOptions.get(optionId).getOptionName());
            ArrayList<Map<String,Object>> connectOptionSet = new ArrayList<>();
            for(int option:deviceOptions.get(optionId).getSelectOptions().keySet()){
                Map<String,Object> optionSetting = new HashMap<>();
                optionSetting.put("id", option);
                optionSetting.put("label", deviceOptions.get(optionId).getSelectOptions().get(option).get("name"));
                connectOptionSet.add(optionSetting);
            }
            connectSettings.put("items", connectOptionSet);
            connectSettingsSet.add(connectSettings);
        }
        options.put("settings", connectSettingsSet);
        ArrayList<Map<String,Object>> driverSettingsSet = new ArrayList<>();
        for(Map<String,String> setItem:DeviceService.getPossiblePeripheralSoftwareDrivers(controller.getPeripheralHardwareDriver().getNamedId(), controller.getPeripheral().getVendorId(), controller.getPeripheral().getDeviceId())){
            Map<String,Object> driverOptions = new HashMap<>();
            driverOptions.put("id", setItem.get("driverid"));
            driverOptions.put("version", setItem.get("version"));
            driverOptions.put("prefered", setItem.get("prefered"));
            driverOptions.put("name", setItem.get("friendlyname"));
            driverSettingsSet.add(driverOptions);
        }
        options.put("drivers", driverSettingsSet);
        return options;
    }
    
    @Override 
    public boolean setPeripheralConnectSettings(String peripheralPort, Map<String,String> parameters) throws PeripheralHardwareException {
        if(parameters.containsKey("peripheral_driver")){
            String driver = (String)parameters.get("peripheral_driver");
            String peripheralDriver = driver.substring(0,driver.lastIndexOf("_"));
            String version = driver.substring(driver.lastIndexOf("_")+1);
            parameters.remove("peripheral_driver");
            DeviceService.getWaitingHardwarePeripheral(peripheralPort).getPeripheralHardwareDriver().setPeripheralOptions(parameters);
            DeviceService.loadPeripheralWithDriverId(peripheralPort, peripheralDriver, version, true);
            return true;
        } else {
            throw new PeripheralHardwareException("Missing driver, can not start hardware");
        }
    }
    
    @Override
    public Object getPeripheralSoftwareDriversForCustomDevices() {
        ArrayList<Map<String,Object>> set = new ArrayList<>();
        for(PeripheralSoftwareDriverInterface driver:DeviceService.getPeripheralSoftwareDrivers().values()){
            if(driver.hasCustom()){
                Map<String,Object> item = new HashMap<>();
                item.put("id", driver.getId());
                item.put("driverid", driver.getNamedId());
                item.put("hascustom", driver.hasCustom());
                item.put("name", driver.getFriendlyName());
                set.add(item);
            }
        }
        return set;
    }
    
    @Override
    public Object disconnectPeripheral(String peripheralPort) throws PeripheralSoftwareException {
        for (PeripheralController hardware : DeviceService.getRunningHardwarePeripherals().values()) {
            if (hardware.getPeripheral().getDevicePort().equals(peripheralPort) && (hardware.getPeripheral().getDeviceType().equals("TYPE_USB") || 
                hardware.getPeripheral().getDeviceType().equals("TYPE_SERIAL"))) {
                DeviceService.stopRunningPeripheral(peripheralPort);
                return true;
            }
        }
        return false;
    }

    @Override
    public Object getLocalDeviceEntries() throws ConfigPropertiesException,IOException {
        return DeviceService.getFilteredCustomDeviceSet();
    }

    @Override
    public Object createCustomSerialDevice(String port, String friendlyName) throws PeripheralHardwareException, ConfigPropertiesException, IOException {
        DeviceService.createCustomSerialDevice(port,friendlyName);
        return true;
    }
 
}
