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

package org.pidome.server.system.hardware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.tools.Platforms;
import org.pidome.server.connector.tools.properties.BooleanPropertyBindingBean;
import org.pidome.server.connector.tools.properties.ReadOnlyBooleanPropertyBindingBean;
import org.pidome.server.services.messengers.ClientMessenger;
import org.pidome.server.system.config.ConfigPropertiesException;
import org.pidome.server.system.hardware.peripherals.i2c.I2CDevices;
import org.pidome.server.system.hardware.peripherals.serial.SerialDevices;
import org.pidome.server.system.hardware.peripherals.server.ServerDevices;
import org.pidome.server.system.hardware.peripherals.usb.USBDevices;
import org.pidome.server.system.hardware.peripherals.emulators.HardwarePluginDeviceEmulators;
import org.pidome.server.system.hardware.peripherals.serial.CustomSerialDevices;
import org.pidome.server.system.packages.PackageProxy;

/**
 * Find out which hardware we have and starts the hardware services.
 * At this moment the only hardware platforms we support are or based on windows
 * or the raspberry pi. Although we talk of ARM support, it explicit comes to LINUX-ARM = Raspberry Pi
 * @author John Sirach
 */
public class Hardware implements HardwareMutationListener, HardwareDiscoveryDoneListener {
    
    static Logger LOG = LogManager.getLogger(Hardware.class);
    
    USBDevices usbDevices;
    SerialDevices serialDevices;
    CustomSerialDevices customSerialDevices;
    I2CDevices I2Cdevices;
    ServerDevices serverDevices;
    HardwarePluginDeviceEmulators pluginDevices;
    
    List _listeners = new ArrayList();
    
    PackageProxy packageLoader = new PackageProxy();
    
    static BooleanPropertyBindingBean active = new BooleanPropertyBindingBean(false);
    
    /**
     * Constructor, extends the packageproxy for package access.
     */
    public Hardware(){
        super();
    }

    /**
     * Initializes all the hardware types and attaches the listeners.
     */
    public final void init(){
        serverDevices = new ServerDevices();
        serverDevices.addHardwareMutationListener(this);
        usbDevices = new USBDevices();
        usbDevices.addHardwareMutationListener(this);
        serialDevices = new SerialDevices();
        serialDevices.addHardwareMutationListener(this);
        I2Cdevices = new I2CDevices();
        I2Cdevices.addHardwareMutationListener(this);
        pluginDevices = HardwarePluginDeviceEmulators.getInstance();
        pluginDevices.addHardwareMutationListener(this);
        customSerialDevices = new CustomSerialDevices();
        customSerialDevices.addHardwareMutationListener(this);
    }
    
    /**
     * Checks if the usb watchdog is running.
     * @return 
     */
    public final boolean USBWatchdogRunning(){
        return (usbDevices!=null)?usbDevices.watchdogRunning():false;
    }
    
    /**
     * Returns the package loader.
     * @return 
     */
    public final PackageProxy getPackageLoader(){
        return packageLoader;
    }
    
    /**
     * Time to start playing with the hardware, this runs the discovery and after discovery it will start the hardware threads.
     */
    public final void start(){
        discover();
        usbDevices.start();
        active.setValue(Boolean.TRUE);
    }
    
    public static ReadOnlyBooleanPropertyBindingBean isRunning(){
        return active.getReadOnlyBooleanPropertyBindingBean();
    }
    
    public final void stop(){
        active.setValue(Boolean.FALSE);
        usbDevices.stop();
        usbDevices.removeHardwareMutationListener(this);
        serialDevices.stop();
        serialDevices.removeHardwareMutationListener(this);
        I2Cdevices.stop();
        I2Cdevices.removeHardwareMutationListener(this);
        serverDevices.stop();
        serverDevices.removeHardwareMutationListener(this);
        pluginDevices.stop();
        pluginDevices.removeHardwareMutationListener(this);
        customSerialDevices.stop();
        customSerialDevices.removeHardwareMutationListener(this);
    }
    
    /**
     * Does discovery on the already plugged in hardware.
     */
    final void discover(){
        LOG.info("Hardware discovery started");
        serverDevices.discover();
        Thread usbThread = new Thread(){
            @Override
            public final void run(){
                setName("USB-discovery");
                LOG.info("Started peripherals discovery on USB");
                usbDevices.discover();
                LOG.info("Done peripherals discovery on USB");
            }
        };
        discoveryThreadRunner(usbThread);
        if(Platforms.ARCH_ARM.equals(Platforms.getReportedArch())){
            Thread serialThread = new Thread(){
                @Override
                public final void run(){
                    setName("Serial-discovery");
                    LOG.info("Started peripherals discovery on Serial GPIO");
                    serialDevices.discover();
                    LOG.info("Done peripherals discovery on Serial GPIO");
                }
            };
            discoveryThreadRunner(serialThread);
            Thread i2cThread = new Thread(){
                @Override
                public final void run(){
                    setName("I2C-discovery");
                    LOG.info("Started peripherals discovery on I2C");
                    I2Cdevices.discover();
                    LOG.info("Done peripherals discovery on I2C");
                }
            };
            discoveryThreadRunner(i2cThread);
        }
        Thread customSerialThread = new Thread(){
            @Override
            public final void run(){
                setName("User-defined-serials-discovery");
                LOG.info("Started user defined serial devices discovery");
                customSerialDevices.discover();
                LOG.info("Done user defined serial devices discovery");
            }
        };
        discoveryThreadRunner(customSerialThread);
    }

    /**
     * Runs a discovery thread.
     * @param thread 
     */
    final void discoveryThreadRunner(Thread thread){
        thread.start();
        //discoveryTimer(thread, thread.getName());
        //synchronized(thread){
        //    try {
        //        thread.wait();
        //    } catch (InterruptedException ex) {
        //        LOG.error("Could not correctly finish the "+thread.getName()+" discovery service");
        //    }
        //}
    }
    
    /**
     * Sets a timer on a discovery thread so a maximum execution time is handled for that thread.
     * @param discoveryThread
     * @param name 
     */
    final void discoveryTimer(Thread discoveryThread, String name){
        Thread discoveryTimerThread = new Thread(){
            @Override
            public final void run(){
                setName(name + "-discovery-timer-60");
                LOG.debug("Started "+name+" watcher (interrupts after 60 seconds)");
                try {
                    Thread.sleep(60000);
                    if(discoveryThread !=null && discoveryThread.isAlive()){
                        discoveryThread.interrupt();
                    }
                } catch (InterruptedException ex) {
                    LOG.error("Hardware discovery is not watched anymore, finish manually");
                }
            }
        };
        discoveryTimerThread.start();
    }
    
    /**
     * Catches changes in attached hardware.
     * @param hardwarePeripheralEvent 
     */
    @Override
    public final void deviceMutation(HardwarePeripheralEvent hardwarePeripheralEvent) {
        Peripheral device = hardwarePeripheralEvent.getSource();
        Map<String,Object> sendObject = new HashMap<>();
        sendObject.put("friendlyname", device.getFriendlyName());
        switch(hardwarePeripheralEvent.getEventType()){
            case HardwarePeripheralEvent.DEVICE_ADDED:
                _fireHardwareEvent(device, HardwareEvent.HARDWARE_ADDED);
                ClientMessenger.send("SystemService","hardwareAdded", 0, sendObject);
            break;
            case HardwarePeripheralEvent.DEVICE_REMOVED:
                _fireHardwareEvent(device, HardwareEvent.HARDWARE_REMOVED);
                ClientMessenger.send("SystemService","hardwareRemoved", 0, sendObject);
            break;
        }
    }
    
    /**
     * Add listeners for changed hardware.
     * @param l 
     */
    protected final synchronized void addHardwareListener( HardwareMutationListener l ) {
        LOG.debug("Added listener: {}", l.getClass().getName());
        _listeners.add( l );
    }
    
    /**
     * Removes listeners for changed hardware.
     * @param l 
     */
    protected final synchronized void removeHardwareListener( HardwareMutationListener l ) {
        LOG.debug("Removed listener: {}", l.getClass().getName());
        _listeners.remove( l );
    }
    
    /**
     * Fires events after a hardware change has been detected.
     * @param device
     * @param eventType 
     */
    protected final synchronized void _fireHardwareEvent(Peripheral device, String eventType) {
        LOG.debug("New hardware event: {}",eventType);
        HardwareEvent event = new HardwareEvent(device, eventType);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (HardwareListener) listeners.next() ).hardwareChange( event );
        }
    }

    @Override
    public void discoveryDoneHandler(HardwareDiscoveryDoneEvent doneEvent) {
        doneEvent.getSource().start();
    }
    
    /**
     * Creates a custom serial device.
     * This creates a custom serial device to be used for server interaction.
     * @param port
     * @param friendlyName
     * @throws PeripheralHardwareException
     * @throws ConfigPropertiesException
     * @throws IOException 
     */
    public void createCustomSerialDevice(String port, String friendlyName) throws PeripheralHardwareException, ConfigPropertiesException, IOException {
        customSerialDevices.createCustomSerialDevice(port, friendlyName);
    }
    
}