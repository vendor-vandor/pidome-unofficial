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

package org.pidome.server.system.hardware.peripherals.serial;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCException;
import org.pidome.server.connector.tools.jsonrpc.PidomeJSONRPCUtils;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.config.SystemConfig;
import org.pidome.server.system.hardware.Hardware;
import org.pidome.server.system.hardware.HardwareMutationListener;
import org.pidome.server.system.hardware.HardwarePeripheralEvent;
import org.pidome.server.system.hardware.HardwareRoot;
import org.pidome.server.services.http.rpc.PidomeJSONRPC;

/**
 *
 * @author John Sirach
 */
public class CustomSerialDevices extends HardwareRoot {

    static Logger LOG = LogManager.getLogger(CustomSerialDevices.class);
    
    Map <String, SerialDevice> deviceCollection = new HashMap<>();
    
    @Override
    public final void discover() throws UnsupportedOperationException {
        try {
            List<String> custSerials = new ArrayList<>(Arrays.asList(
                (String[])Files.list(new File(SystemConfig.getProperty("system", "server.conf.hardware.custserial")).toPath()).map(String::valueOf).toArray(size -> new String[size])
            ));
            for(String fileName:(List<String>)custSerials){
                File file = new File(fileName);
                if(file.exists()){
                    try {
                        List<String> lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
                        if(lines.size()==1){
                            try {
                                Map<String,Object> params = new PidomeJSONRPC(lines.get(0).trim(), false).getParsedObject();
                                SerialDevice serialDevice = new SerialDevice();
                                serialDevice.setDeviceKey((String)params.get("userCustomSerialKey"));
                                serialDevice.setDevicePort((String)params.get("hardwarePort"));
                                serialDevice.setVendorId((String)params.get("hardwareVendorId"));
                                serialDevice.setDeviceId((String)params.get("hardwareDeviceId"));
                                serialDevice.setFriendlyName((String)params.get("friendlyName"));
                                
                                deviceCollection.put((String)params.get("userCustomSerialKey"), serialDevice);
                                
                                _fireDeviceEvent(serialDevice, HardwarePeripheralEvent.DEVICE_ADDED);
                            } catch(PidomeJSONRPCException ex){
                                // invalid safe contents
                                file.delete();
                                LOG.error("Could not get parameters from {}", file.getName());
                            } catch (PeripheralHardwareException ex) {
                                LOG.error("Serial device could not be initialized: {} - {}", file.getName(), ex.getMessage(), ex);
                            }
                        } else {
                            /// incorrect amount of lines
                            file.delete();
                            LOG.error("Incorrect parameters save (lines): {}", file.getName());
                        }
                    } catch(IOException e){
                        /// unable to read file
                        file.delete();
                        LOG.error("Could not read settings file: {}", file.getName());
                    }
                }
            }
        } catch (ConfigPropertiesException | IOException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }
    
    final synchronized void _fireDeviceEvent(SerialDevice serialDevice, String eventType) {
        LOG.debug("Event: {}", eventType);
        HardwarePeripheralEvent serialEvent = new HardwarePeripheralEvent(serialDevice, eventType);
        Iterator listeners = getListeners().iterator();
        while( listeners.hasNext() ) {
            ( (HardwareMutationListener) listeners.next() ).deviceMutation( serialEvent );
        }
    }

    public final void createCustomSerialDevice(String port, String friendlyName) throws PeripheralHardwareException, ConfigPropertiesException, IOException {
        Map<String,Object> settingsMap = new HashMap<>();
        settingsMap.put("userCustomSerialKey", UUID.randomUUID().toString());
        settingsMap.put("hardwarePort",        port);
        settingsMap.put("hardwareVendorId",    "PiDome");
        settingsMap.put("hardwareDeviceId",    "UserDefinedSerial");
        settingsMap.put("friendlyName",        friendlyName);

        LOG.info("Trying to save custom serial device: {}", settingsMap);

        try {
            File file = new File(SystemConfig.getProperty("system", "server.conf.hardware.custserial") + port.substring(port.lastIndexOf(File.separator))+1);
            try(FileWriter writer = new FileWriter(file);
                PrintWriter printer = new PrintWriter(writer);) {
                printer.write(PidomeJSONRPCUtils.createNonRPCMethods(settingsMap));
            } catch (IOException ex) {
                LOG.error("Unable to write configuration: {}", ex.getMessage());
            } catch (PidomeJSONRPCException ex) {
                LOG.error("Unable to create JSON properties");
            }
        } catch (ConfigPropertiesException ex) {
            LOG.error("Problem getting custom hardware save configuration setting server.conf.hardware.custserial: {}", ex.getMessage());
        }

        SerialDevice serialDevice = new SerialDevice();
        serialDevice.setDeviceKey((String)settingsMap.get("userCustomSerialKey"));
        serialDevice.setDevicePort((String)settingsMap.get("hardwarePort"));
        serialDevice.setVendorId((String)settingsMap.get("hardwareVendorId"));
        serialDevice.setDeviceId((String)settingsMap.get("hardwareDeviceId"));
        serialDevice.setFriendlyName((String)settingsMap.get("friendlyName"));

        deviceCollection.put((String)settingsMap.get("userCustomSerialKey"), serialDevice);

        _fireDeviceEvent(serialDevice, HardwarePeripheralEvent.DEVICE_ADDED);
    }
    
    @Override
    public void start() throws UnsupportedOperationException {
        /// there is no start, as these devices are custom and loaded on demand so there are no listeners
    }

    @Override
    public void stop() throws UnsupportedOperationException {
        ArrayList<String> stopSet = new ArrayList<>();
        for(String serialKey:deviceCollection.keySet()){
            LOG.info("Removing '" + deviceCollection.get(serialKey).getFriendlyName() + "' device, please wait, removing...");
            _fireDeviceEvent(deviceCollection.get(serialKey), HardwarePeripheralEvent.DEVICE_REMOVED);
            LOG.info("Removed '"+deviceCollection.get(serialKey).getFriendlyName()+"'");
            stopSet.add(serialKey);
        }
        for(String serialKey:stopSet){
            deviceCollection.remove(serialKey);            
        }
    }
    
    
}
