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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;

/**
 *
 * @author John Sirach
 */
public class ServerDevice extends Peripheral {

    static Logger LOG = LogManager.getLogger(ServerDevice.class);
    
    public ServerDevice() throws PeripheralHardwareException {
        super(Peripheral.TYPE_SERVER);
        setSubSystem(SubSystem.SERVER);
        LOG.debug("The local server \"device\"");
    }

}
