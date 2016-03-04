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

package org.pidome.server.system.hardware.peripherals.usb;

import java.util.ArrayList;
import org.pidome.server.system.hardware.HardwarePeripheralEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral.SubSystem;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.system.hardware.HardwareMutationListener;
import org.pidome.server.system.hardware.peripherals.usb.windows.WindowsUsb;
import org.pidome.server.system.hardware.peripherals.usb.linux.RaspUsb;
import org.pidome.server.connector.tools.Platforms;
import org.pidome.server.system.hardware.HardwareRoot;

/**
 *
 * @author John Sirach
 */
public final class USBDevices extends HardwareRoot implements InternalUsbListener {
    
    WindowsUsb winUsb;
    RaspUsb raspUsb;
    
    Map <String, USBDevice> deviceCollection = new HashMap<>();
    
    static Logger LOG = LogManager.getLogger(USBDevices.class);
    
    /**
     * Constructor.
     */
    public USBDevices(){
        prepare();
    }
    
    /**
     * Prepares the USB implementation.
     * @throws UnsupportedOperationException 
     */
    public final void prepare() throws UnsupportedOperationException {
        LOG.debug("prepare");
        switch(Platforms.isOs()){
            case Platforms.OS_WINDOWS:
                switch(Platforms.isArch()){
                    case Platforms.ARCH_64:
                    case Platforms.ARCH_86:
                        winUsb = new WindowsUsb();
                        winUsb.addEventListener(this);
                        LOG.debug("Using Windows USB");
                    break;
                    default:
                        throw new UnsupportedOperationException(Platforms.getReportedOs() + " on "+Platforms.getReportedArch()+" is unsupported for realtime USB connection status");
            }
            break;
            case Platforms.OS_LINUX:
                switch(Platforms.isArch()){
                    case Platforms.ARCH_ARM:
                        raspUsb = new RaspUsb();
                        raspUsb.addEventListener(this);
                        LOG.debug("Using linux USB");
                    break;
                    default:
                        throw new UnsupportedOperationException(Platforms.getReportedOs() + " on "+Platforms.getReportedArch()+" is unsupported for realtime USB connection status");
                }
            break;
            default:
                throw new UnsupportedOperationException(Platforms.getReportedOs() + " is unsupported for realtime USB connection status");
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void deviceMutation(SubSystem subSystem, String serial, String deviceName, String vendorId, String deviceId, String usbKey, String devicePort) {
        switch(InternalUsbEvent.getEventType()){
            case InternalUsbEvent.DEVICE_ADDED:
                try {
                    USBDevice usbDevice = new USBDevice();
                    usbDevice.setSerial(serial);
                    usbDevice.setSubSystem(subSystem);
                    usbDevice.setDeviceKey(usbKey);
                    usbDevice.setDevicePort(devicePort);
                    usbDevice.setVendorId(vendorId);
                    usbDevice.setDeviceId(deviceId);
                    usbDevice.setFriendlyName(deviceName);
                    deviceCollection.put(usbKey, usbDevice);
                    LOG.info("New '"+deviceName+"' device drivers going to load, please wait...");
                    _fireDeviceEvent(usbDevice, HardwarePeripheralEvent.DEVICE_ADDED);
                } catch (PeripheralHardwareException ex){
                    
                }
            break;
            case InternalUsbEvent.DEVICE_REMOVED:
                if(deviceCollection.containsKey(usbKey)){
                    LOG.info("Removing '"+deviceCollection.get(usbKey).getFriendlyName()+"' device, please wait, removing...");
                    _fireDeviceEvent(deviceCollection.get(usbKey), HardwarePeripheralEvent.DEVICE_REMOVED);
                    LOG.info("Removed '"+deviceCollection.get(usbKey).getFriendlyName()+"'");
                    deviceCollection.remove(usbKey);
                }
            break;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public final void discover() throws UnsupportedOperationException {
        switch(Platforms.isOs()){
            case Platforms.OS_WINDOWS:
                winUsb.discover();
            break;
            case Platforms.OS_LINUX:
                raspUsb.discover();
            break;
            default:
                throw new UnsupportedOperationException("Discovery of USB devices is unsupported");
        }
        _fireDiscoveryDoneEvent();
    }
    
    /**
     * Returns if the usb watchdog is running for real time connects.
     * @return 
     */
    public final boolean watchdogRunning(){
        switch(Platforms.isOs()){
            case Platforms.OS_WINDOWS:
                return winUsb.watchDogRunning();
            case Platforms.OS_LINUX:
                return raspUsb.watchDogRunning();
        }
        return false;
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public final void start(){
        switch(Platforms.isOs()){
            case Platforms.OS_WINDOWS:
                Thread windowsUsb = new Thread() {
                    @Override
                    public void run() {
                        winUsb.startWinListener();
                    }
                };
                windowsUsb.setName("Windows USB Monitor");
                windowsUsb.start();
                LOG.info("USB listener started");
            break;
            case Platforms.OS_LINUX:
                Thread raspberryUsb = new Thread() {
                    @Override
                    public void run() {
                        raspUsb.startRaspListener();
                    }
                };
                raspberryUsb.setName("Rasp USB Monitor");
                raspberryUsb.start();
                LOG.info("USB listener started");
            break;
            default:
                throw new UnsupportedOperationException("Discovery of USB devices is unsupported");
        }
    }
    
    /**
     * Fires a usb device event.
     * @param usbDevice
     * @param eventType 
     */
    final synchronized void _fireDeviceEvent(USBDevice usbDevice, String eventType) {
        LOG.debug("Event: {}", eventType);
        HardwarePeripheralEvent UsbEvent = new HardwarePeripheralEvent(usbDevice, eventType);
        Iterator listeners = getListeners().iterator();
        while( listeners.hasNext() ) {
            ( (HardwareMutationListener) listeners.next() ).deviceMutation( UsbEvent );
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void stop() throws UnsupportedOperationException {
        ArrayList<String> stopSet = new ArrayList<>();
        for(String usbKey:deviceCollection.keySet()){
            LOG.info("Removing '" + deviceCollection.get(usbKey).getFriendlyName() + "' device, please wait, removing...");
            _fireDeviceEvent(deviceCollection.get(usbKey), HardwarePeripheralEvent.DEVICE_REMOVED);
            LOG.info("Removed '"+deviceCollection.get(usbKey).getFriendlyName()+"'");
            stopSet.add(usbKey);
        }
        for(String usbKey:stopSet){
            deviceCollection.remove(usbKey);            
        }
    }
}
