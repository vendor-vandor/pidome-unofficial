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
package org.pidome.server.system.hardware.peripherals.usb.linux;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral.SubSystem;
import org.pidome.server.system.hardware.peripherals.serial.SerialUtils;
import org.pidome.server.system.hardware.peripherals.usb.InternalUsbEvent;
import org.pidome.server.system.hardware.peripherals.usb.InternalUsbListener;
import org.pidome.server.system.hardware.peripherals.usb.linux.CFuncImpl.LinuxCLib.FDSet;
import org.pidome.server.system.hardware.peripherals.usb.linux.CFuncImpl.TimeVal;

/**
 *
 * @author John Sirach
 */
public class RaspUsb {

    static Logger LOG = LogManager.getLogger(RaspUsb.class);

    private final List _listeners = new ArrayList();

    static Thread udevThread;

    /**
     * Link to the java udev library interface
     */
    UdevLib udevLib;
    /**
     * The udev pointer to the new_udev instance
     */
    UdevLib.udev udevPointer;
    /**
     * The udev_monitor pointer to udev_monitor_new_from_netlink
     */
    UdevLib.udev_monitor mon;

    /**
     * Will hold the file descriptor int
     */
    int fd;

    /**
     * Adds an event listener for USB device changes.
     *
     * @param l
     */
    public synchronized void addEventListener(InternalUsbListener l) {
        _listeners.add(l);
        LOG.debug("Added listener: {}", l.getClass().getName());
    }

    /**
     * Removes a listener for USB device changes.
     *
     * @param l
     */
    public synchronized void removeEventListener(InternalUsbListener l) {
        _listeners.remove(l);
        LOG.debug("Removed listener: {}", l.getClass().getName());
    }

    /**
     * Prediscover usb ports.
     */
    public void discover() {
        SerialUtils.discoverPorts();
        try {
            udevLib = UdevLib.INSTANCE;
            udevPointer = udevLib.udev_new();

            if (udevPointer == null) {
                LOG.error("Can't create udev link, no real time events possible");
            } else {
                mon = udevLib.udev_monitor_new_from_netlink(udevPointer, "udev");
                ///udevLib.udev_monitor_filter_add_match_subsystem_devtype(mon, "usb", "usb_device");

                UdevLib.udev_enumerate enumerate = udevLib.udev_enumerate_new(udevPointer);
                udevLib.udev_enumerate_scan_devices(enumerate);

                UdevLib.udev_list_entry devices = udevLib.udev_enumerate_get_list_entry(enumerate);

                String devicePath;
                UdevLib.udev_device enumDev;

                while (devices != null) {
                    devicePath = udevLib.udev_list_entry_get_name(devices);
                    enumDev = udevLib.udev_device_new_from_syspath(udevPointer, devicePath);
                    try {
                        handleUsbDeviceAction(enumDev, true);
                    } catch (NullPointerException ex) {
                        //// Device has no sybsystem
                    }
                    udevLib.udev_device_unref(enumDev);
                    devices = udevLib.udev_list_entry_get_next(devices);
                }
                udevLib.udev_enumerate_unref(enumerate);
            }
        } catch (UnsatisfiedLinkError e) {
            LOG.error("Udev library not found, no automatic attach/detach functions possible");
        }
    }

    /**
     * Enumerates device information and adds or removes a device.
     * @param dev The usb device we are talking about.
     * @param action set to true to add a device, false to remove.
     */
    private void handleUsbDeviceAction(UdevLib.udev_device dev, boolean action){
        UdevLib.udev_device parentDevice = udevLib.udev_device_get_parent_with_subsystem_devtype(
               dev,
               "usb",
               "usb_device");
        if (parentDevice!=null) {
            String DeviceName = udevLib.udev_device_get_sysattr_value(parentDevice,"product");     
            String vendorId   = udevLib.udev_device_get_sysattr_value(parentDevice,"idVendor");
            String deviceId   = udevLib.udev_device_get_sysattr_value(parentDevice, "idProduct");
            String devicePort = udevLib.udev_device_get_devnode(dev);
            String vendorName = udevLib.udev_device_get_sysattr_value(parentDevice,"manufacturer");
            String subClass   = udevLib.udev_device_get_subsystem(dev);
            String path       = udevLib.udev_device_get_devpath(dev);
            String serial     = udevLib.udev_device_get_sysattr_value(parentDevice,"serial");

            if(serial == null){
                serial = "unknown";
            }
            if(vendorName == null){
                vendorName = "Unknown";
            }
            
            switch (subClass) {
                case "tty":
                case "hidraw":
                case "bluetooth":
                    LOG.info("USB device {}, please wait", ((action==true)?"added":"removed"));
                    LOG.debug("Path: {}", path);
                    LOG.debug("Device sub class: {}", subClass);
                    LOG.debug("Device serial: {}", serial);
                    LOG.debug("Device name: {}", DeviceName);
                    LOG.debug("Device id: {}", deviceId);
                    LOG.debug("Device port: {}", devicePort);
                    LOG.debug("Device vendor id: {}", vendorId);
                    LOG.debug("Device vendor name: {}", vendorName);
                    if(action){
                        SubSystem subSys;
                        switch(subClass){
                            case "tty":
                                subSys = SubSystem.SERIAL;
                            break;
                            case "hidraw":
                                subSys = SubSystem.HID;
                            break;
                            case "bluetooth":
                                subSys = SubSystem.BLUETOOTH;
                            break;
                            default:
                                subSys = SubSystem.UNKNOWN;
                            break;
                        }
                        _fireDeviceEvent(InternalUsbEvent.DEVICE_ADDED, subSys, serial, vendorName + ", " + DeviceName, vendorId, deviceId, udevLib.udev_device_get_devpath(dev), devicePort);
                    } else {
                        _fireDeviceEvent(InternalUsbEvent.DEVICE_REMOVED, udevLib.udev_device_get_devpath(dev));
                    }
                break;
                default:
                    LOG.debug("Subclass {} of usb device {} is unsupported.", subClass, DeviceName);
                break;
            }                
        }
    }
    
    /**
     * The internal usb listener. The listener starter is integrated with the
     * "preload discovery"
     */
    public void startRaspListener() {
        if (udevLib != null) {
            udevLib.udev_monitor_enable_receiving(mon);
            fd = udevLib.udev_monitor_get_fd(mon);

            FDSet fds = CFuncImpl.newFDSet();
            TimeVal tv = new TimeVal();

            LOG.info("Usb listener started");

            while (true) {
                CFuncImpl.FD_ZERO(fds);
                CFuncImpl.FD_SET(fd, fds);
                tv.tv_sec = 0;
                tv.tv_usec = 0;

                int ret = CFuncImpl.select(fd + 1, fds, null, null, tv);

                if (ret > 0 && CFuncImpl.FD_ISSET(fd, fds)) {
                    UdevLib.udev_device dev = udevLib.udev_monitor_receive_device(mon);
                    if (dev != null) {
                        LOG.trace("USB event subsystem: {}", udevLib.udev_device_get_subsystem(dev));
                        handleUsbDeviceAction(dev, udevLib.udev_device_get_action(dev).equals("add"));
                        udevLib.udev_device_unref(dev);
                    } else {
                        LOG.error("No Device from receive_device(). An error occured.\n");
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    if (mon != null) {
                        udevLib.udev_monitor_unref(mon);
                    }
                    udevLib.udev_unref(udevPointer);
                    LOG.error("Udev listener thread exited, no automatic attach/detach operations possible, server restart needed to fix.");
                }
            }
        }
    }

    /**
     * Returns if the service is accepting usb connections.
     * @return 
     */
    public final boolean watchDogRunning(){
        return mon!=null;
    }

    /**
     * This usb device event is used when a device disconnects
     *
     * @param EVENTTYPE
     * @param usbKey
     */
    private synchronized void _fireDeviceEvent(String EVENTTYPE, String usbKey) {
        Runnable run = () -> {
            LOG.debug("Event: {}, {}", EVENTTYPE, usbKey);
            InternalUsbEvent internalUsbEvent = new InternalUsbEvent(this, EVENTTYPE);
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                ((InternalUsbListener) listeners.next()).deviceMutation(SubSystem.UNKNOWN, null, null, null, null, usbKey, null);
            }
        };
        run.run();
    }

    /**
     * Used for when a device is added.
     *
     * @param EVENTTYPE
     * @param DeviceName
     * @param vendorId
     * @param deviceId
     * @param usbKey
     * @param devicePort
     * @see #_fireDeviceEvent(java.lang.String, java.lang.String)
     */
    private synchronized void _fireDeviceEvent(String EVENTTYPE, SubSystem subSystem, String serial, String DeviceName, String vendorId, String deviceId, String usbKey, String devicePort) {
        Runnable run = () -> {
            LOG.debug("Event: {}, {}", EVENTTYPE, usbKey);
            InternalUsbEvent internalUsbEvent = new InternalUsbEvent(this, EVENTTYPE);
            Iterator listeners = _listeners.iterator();
            while (listeners.hasNext()) {
                ((InternalUsbListener) listeners.next()).deviceMutation(subSystem, serial, DeviceName, vendorId, deviceId, usbKey, devicePort);
            }
        };
        run.run();
    }

}
