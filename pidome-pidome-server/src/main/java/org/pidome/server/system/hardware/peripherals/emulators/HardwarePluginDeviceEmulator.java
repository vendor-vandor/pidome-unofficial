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

package org.pidome.server.system.hardware.peripherals.emulators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;

/**
 *
 * @author John
 */
public class HardwarePluginDeviceEmulator extends Peripheral {

    static Logger LOG = LogManager.getLogger(HardwarePluginDeviceEmulator.class);
    
    DevicePlugin plugin;
    
    /**
     * Constructor.
     * Sets hardware item type.
     * @throws PeripheralHardwareException 
     */
    public HardwarePluginDeviceEmulator() throws PeripheralHardwareException {
        super(Peripheral.TYPE_PLUGIN);
        setSubSystem(SubSystem.PLUGIN);
        LOG.debug("An emulator layer for plugins");
    }

    
    /**
     * Sets the plugin.
     * @param plugin 
     */
    public final void setPlugin(DevicePlugin plugin){
        this.plugin = plugin;
    }
    
    /**
     * Set's the software id as known from the plugin.
     */
    public final void prepareSoftwareId(){
        LOG.info("setting custom driver id from plugin '{}': {}, {}", plugin.getBaseName(),plugin.getExpectedDriverId(), plugin.getExpectedDriverVersion());
        setSoftwareDriverId(plugin.getExpectedDriverId(), plugin.getExpectedDriverVersion());
    }
    
    /**
     * Overwritten version of getting the software id.
     * This software id is supplied by the plugin.
     * @return
     * @throws PeripheralHardwareException
     * @throws UnsupportedOperationException 
     */
    @Override
    public final PeripheralHardwareDriver.PeripheralVersion getPeripheralSoftwareId() throws PeripheralHardwareException,UnsupportedOperationException {
        return super.getPeripheralSoftwareId();
    }
    
    /**
     * Returns the plugin.
     * @return 
     */
    public final DevicePlugin getPlugin(){
        return plugin;
    }
    
    /**
     * Removes the plugin link.
     */
    public final void unsetPlugin(){
        plugin = null;
    }
    
}
