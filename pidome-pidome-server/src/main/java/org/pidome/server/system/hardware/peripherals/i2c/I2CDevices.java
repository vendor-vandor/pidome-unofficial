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

package org.pidome.server.system.hardware.peripherals.i2c;

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
public class I2CDevices extends HardwareRoot {

    static Logger LOG = LogManager.getLogger(I2CDevices.class);
    
    I2CDevice I2Cdevice;
    
    @Override
    public final void discover() throws UnsupportedOperationException {
        try {
            I2Cdevice = new I2CDevice();
            I2Cdevice.setDeviceKey("InternalGPIOI2C");
            I2Cdevice.setDevicePort("{Auto discovery by driver}");
            I2Cdevice.setVendorId("PiDome");
            I2Cdevice.setDeviceId("GPIOI2C");
            I2Cdevice.setFriendlyName("Raspberry GPIO I2C");
            _fireDiscoveryDoneEvent();
        } catch (PeripheralHardwareException ex) {
            LOG.error("Nothing on I2C: {}", ex.getMessage());
        }
    }
    
    final synchronized void _fireDeviceEvent(I2CDevice i2cDevice, String eventType) {
        LOG.debug("Event: {}", eventType);
        HardwarePeripheralEvent gpioI2CEvent = new HardwarePeripheralEvent(i2cDevice, eventType);
        Iterator listeners = getListeners().iterator();
        while( listeners.hasNext() ) {
            ( (HardwareMutationListener) listeners.next() ).deviceMutation( gpioI2CEvent );
        }
    }

    @Override
    public void start() throws UnsupportedOperationException {
        _fireDeviceEvent(I2Cdevice, HardwarePeripheralEvent.DEVICE_ADDED);
    }

    @Override
    public void stop() throws UnsupportedOperationException {
        _fireDeviceEvent(I2Cdevice, HardwarePeripheralEvent.DEVICE_REMOVED);
    }
    
    
}
