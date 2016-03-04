/*
 * Copyright 2013 John.
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
package org.pidome.server.services.hardware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pidome.server.connector.drivers.devices.UnknownDeviceException;
import org.pidome.server.system.hardware.devices.DeviceInterface;
import org.pidome.server.system.hardware.devices.DevicesException;

/**
 *
 * @author John
 */
public class DeviceServiceTest {
    
    static DeviceService instance;
    
    public DeviceServiceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws DevicesException {
        instance = new DeviceService();
        instance.start();
    }
    
    @AfterClass
    public static void tearDownClass() {
        instance = null;
    }
    
    /**
     * Test of sendDevice method, of class DeviceService.
     */
    @Test
    public void testSendDevice() {
        Integer deviceId = 0;
        String cmdGroup = "unknown";
        String cmdSet = "unknown";
        Map<String,Object> deviceCommand =  new HashMap<>();
        deviceCommand.put("value", "something");
        boolean expResult = false;
        boolean result = false;
        try {
            result = DeviceService.sendDevice(deviceId, cmdGroup, cmdSet, deviceCommand, true);
        } catch (DeviceServiceException ex) {
            result = false;
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of addBatch method, of class DeviceService.
     */
    @Test
    public void testAddBatch() {
        Integer deviceId = 0;
        String deviceGroup = "anything";
        String deviceSet = "anything";
        String deviceCommand = "anything";
        String batchName = "testbatch";
        DeviceService.addBatch(deviceId, deviceGroup, deviceSet, deviceCommand, "", batchName);
    }

    /**
     * Test of runBatch method, of class DeviceService.
     */
    @Test
    public void testRunBatch() {
        String batchName = "testbatch";
        DeviceService.runBatch(batchName);
    }

    /**
     * Test of getAllEnabledDevices method, of class DeviceService.
     * @throws org.pidome.server.services.hardware.DeviceServiceException
     */
    @Test
    public void testGetAllEnabledDevices() throws DeviceServiceException {
        assertNotNull(DeviceService.getAllEnabledDevices());
    }

    /**
     * Test of getAllDeclaredDevices method, of class DeviceService.
     */
    @Test
    public void testGetAllDeclaredDevices_0args() {
        assertNotNull(DeviceService.getAllDeclaredDevices());
    }

    /**
     * Test of getAllDeclaredDevices method, of class DeviceService.
     */
    @Test
    public void testGetAllDeclaredDevices_String() {
        String device = "";
        List expResult = new ArrayList();
        List result = DeviceService.getAllDeclaredDevices(device);
        assertEquals(expResult, result);
    }

    /**
     * Test of getDeclaredDevice method, of class DeviceService.
     */
    @Test
    public void testGetDeclaredDevice() {
        int deviceId = 0;
        Map expResult = new HashMap<>();
        Map result = DeviceService.getDeclaredDevice(deviceId);
        assertEquals(expResult, result);
    }

    /**
     * Test of deleteDevice method, of class DeviceService.
     */
    @Test
    public void testDeleteDevice() {
        Integer deviceId = 0;
        boolean expResult = true;
        boolean result;
        try {
            result = DeviceService.deleteDevice(deviceId);
        } catch (DeviceServiceException ex) {
            result = false;
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of saveDevice method, of class DeviceService.
     */
    @Test
    public void testSaveDevice() {
        Integer device = 1;
        Integer location = 1;
        String address = "";
        String name = "";
        Integer category = 0;
        boolean favorite = true;
        Map settings = new HashMap<>();
        Boolean expResult = false;
        Boolean result;
        try {
            DeviceService.getAllEnabledDevices(); /// This also reloads the known devices table so a device can be saved.
            result = DeviceService.saveDevice(device, location, address, name, category, favorite, settings, new ArrayList<>());
        } catch (DeviceServiceException ex) {
            result = false;
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of getDeviceInstance method, of class DeviceService.
     */
    @Test (expected=UnknownDeviceException.class)
    public void testGetDeviceInstance() throws Exception {
        String deviceName = "testdevice";
        Integer deviceId = 0;
        DeviceInterface expResult = null;
        DeviceInterface result = DeviceService.getDeviceInstance(deviceId);
        assertEquals(expResult, result);
    }

    /**
     * Test of getOfflineDeviceInstance method, of class DeviceService.
     */
    @Test (expected=UnknownDeviceException.class)
    public void testGetOfflineDeviceInstance() throws Exception {
        Integer deviceId = 0;
        DeviceInterface expResult = null;
        DeviceInterface result = DeviceService.getOfflineDeviceInstance(deviceId);
        assertEquals(expResult, result);
    }

    /**
     * Test of editDevice method, of class DeviceService.
     */
    @Test
    public void testEditDevice() {
        Integer deviceId = 0;
        Integer location = 0;
        String address = "";
        String name = "";
        Integer category = 0;
        boolean favorite = false;
        Map settings = new HashMap<>();
        boolean expResult = true;
        boolean result;
        try {
            result = DeviceService.editDevice(deviceId, location, address, name, category, favorite, settings, new ArrayList<>());
        } catch (DeviceServiceException ex) {
            result = false;
        }
        assertEquals(expResult, result);
    }

    /**
     * Test of getActiveDeviceList method, of class DeviceService.
     */
    @Test
    public void testGetActiveDeviceList() {
        Map expResult = new HashMap<>();
        Map result = DeviceService.getActiveDeviceList();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDevice method, of class DeviceService.
     */
    @Test (expected=UnknownDeviceException.class)
    public void testGetDevice() throws Exception {
        Integer deviceId = 0;
        DeviceInterface expResult = null;
        DeviceInterface result = DeviceService.getDevice(deviceId);
        assertEquals(expResult, result);
    }

    /**
     * Test of getLooseInstance method, of class DeviceService.
     */
    @Test (expected=UnknownDeviceException.class)
    public void testGetLooseInstance() throws Exception {
        Integer device = 0;
        DeviceInterface expResult = null;
        DeviceInterface result = DeviceService.getLooseInstance(device);
        assertEquals(expResult, result);
    }

}