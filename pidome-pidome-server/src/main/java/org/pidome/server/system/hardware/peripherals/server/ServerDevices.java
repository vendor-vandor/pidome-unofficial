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

package org.pidome.server.system.hardware.peripherals.server;

import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.system.hardware.HardwareMutationListener;
import org.pidome.server.system.hardware.HardwarePeripheralEvent;
import org.pidome.server.system.hardware.HardwareRoot;

/**
 *
 * @author John Sirach
 */
public class ServerDevices extends HardwareRoot {

    static Logger LOG = LogManager.getLogger(ServerDevices.class);
    
    ServerDevice serverDevice;
    
    @Override
    public final void discover() throws UnsupportedOperationException {
        try {
            serverDevice = new ServerDevice();
            serverDevice.setDeviceKey("InternalServerDevice");
            serverDevice.setDevicePort("{Internally used device map}");
            serverDevice.setVendorId("PiDome");
            serverDevice.setDeviceId("ServerDevicePeripheral");
            serverDevice.setFriendlyName("Server information layer");
            _fireDiscoveryDoneEvent();
        } catch (PeripheralHardwareException ex) {
            LOG.error("Nothing on serial: {}", ex.getMessage());
        }
    }
    
    final synchronized void _fireDeviceEvent(ServerDevice serverDevice, String eventType) {
        LOG.debug("Event: {}", eventType);
        HardwarePeripheralEvent serverDeviceEvent = new HardwarePeripheralEvent(serverDevice, eventType);
        Iterator listeners = getListeners().iterator();
        while( listeners.hasNext() ) {
            ( (HardwareMutationListener) listeners.next() ).deviceMutation( serverDeviceEvent );
        }
    }

    @Override
    public void start() throws UnsupportedOperationException {
        _fireDeviceEvent(serverDevice, HardwarePeripheralEvent.DEVICE_ADDED);
    }

    @Override
    public void stop() throws UnsupportedOperationException {
        _fireDeviceEvent(serverDevice, HardwarePeripheralEvent.DEVICE_REMOVED);
    }
    
    
}
