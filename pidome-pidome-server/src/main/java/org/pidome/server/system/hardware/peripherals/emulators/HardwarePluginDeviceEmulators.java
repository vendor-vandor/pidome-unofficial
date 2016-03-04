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

import java.util.Iterator;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;
import org.pidome.server.system.hardware.HardwareMutationListener;
import org.pidome.server.system.hardware.HardwarePeripheralEvent;
import org.pidome.server.system.hardware.HardwareRoot;

/**
 *
 * @author John
 */
public class HardwarePluginDeviceEmulators extends HardwareRoot {

    static Logger LOG = LogManager.getLogger(HardwarePluginDeviceEmulators.class);
    
    private static HardwarePluginDeviceEmulators me;
    
    public HardwarePluginDeviceEmulators(){
        super();
    }
    
    public static HardwarePluginDeviceEmulators getInstance(){
        if(me==null){
            me = new HardwarePluginDeviceEmulators();
        }
        return me;
    }
    
    /**
     * Creates a new emulator device.
     * @param plugin
     * @throws PeripheralHardwareException 
     */
    public static void createDevice(DevicePlugin plugin) throws PeripheralHardwareException{
        String randKey = UUID.randomUUID().toString();
        HardwarePluginDeviceEmulator emu = new HardwarePluginDeviceEmulator();
        emu.setDevicePort(randKey);
        emu.setDeviceId("HwPluginEmu");
        emu.setVendorId("PiDome");
        emu.setFriendlyName("Emulator for: " + plugin.getBaseName());
        emu.setDeviceKey(randKey);
        emu.setPlugin(plugin);
        plugin.setHardwareDevice(emu);
        getInstance()._fireDeviceEvent(emu, HardwarePeripheralEvent.DEVICE_ADDED);
    }
    
    /**
     * Creates a new emulator device.
     * @param plugin
     * @throws PeripheralHardwareException 
     */
    public static void removeDevice(DevicePlugin plugin) throws PeripheralHardwareException{
        getInstance()._fireDeviceEvent((HardwarePluginDeviceEmulator)plugin.getHardwareDevice(), HardwarePeripheralEvent.DEVICE_REMOVED);
    }
    
    /**
     * Fires an emulated hardare device event.
     * @param device
     * @param eventType 
     */
    private synchronized void _fireDeviceEvent(HardwarePluginDeviceEmulator device, String eventType) {
        LOG.debug("Event: {}", eventType);
        HardwarePeripheralEvent event = new HardwarePeripheralEvent(device, eventType);
        Iterator listeners = getListeners().iterator();
        while( listeners.hasNext() ) {
            ( (HardwareMutationListener) listeners.next() ).deviceMutation( event );
        }
    }
    
    /**
     * No need to discover, not used.
     * @throws UnsupportedOperationException 
     */
    @Override
    public void discover() throws UnsupportedOperationException {
        /// Not needed because a plugin can not be discovered outside the plugin service.
    }

    /**
     * No need to specific start, not used.
     * @throws UnsupportedOperationException 
     */
    @Override
    public void start() throws UnsupportedOperationException {
        /// Not needed for emulation
    }

    /**
     * No need for specific stop, not used. A plugin stopping initiates a device unload.
     * @throws UnsupportedOperationException 
     */
    @Override
    public void stop() throws UnsupportedOperationException {
        /// not needed for amulation
    }
    
}
