/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.system.hardware.peripherals.usb.linux;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

/**
 *
 * @author John Sirach
 */
/**
 *
 * @author John Sirach <john.sirach@gmail.com>
 */
interface UdevLib extends Library {
    UdevLib INSTANCE = (UdevLib) Native.loadLibrary("udev", UdevLib.class);
    int getpid();
    int getppid();
    long time(long buf[]);
    
    /**
     * Creating a new udev instance
     * @return 
     */
    UdevLib.udev udev_new();
    
    /**
     * Udev events monitor
     * @param udev
     * @param name
     * @return 
     */
    UdevLib.udev_monitor udev_monitor_new_from_netlink(UdevLib.udev udev, String name);
    
    /**
     * Set the filter for the udev events monitor
     * @param udev_monitor
     * @param subsystem
     * @param devtype
     * @return 
     */
    int udev_monitor_filter_add_match_subsystem_devtype(UdevLib.udev_monitor udev_monitor, String subsystem, String devtype);
        
    /**
     * Retrieves the device reported by the monitor
     * @param udev_monitor
     * @return 
     */
    UdevLib.udev_device udev_monitor_receive_device(UdevLib.udev_monitor udev_monitor);
    
    /**
     * Returns usb device information representation.
     * @param udev_device The USB device.
     * @param entryName The field to retrieve. This can be vendor_id,product_id etc..
     * @return 
     */
    String udev_device_get_sysattr_value(UdevLib.udev_device udev_device,String entryName);
    
    /**
     * Returns the device's node
     * @param udev_device
     * @return 
     */
    String udev_device_get_devnode(UdevLib.udev_device udev_device);
    
    /**
     * Returns the device's subsystem
     * @param udev_device
     * @return 
     */
    String udev_device_get_subsystem(UdevLib.udev_device udev_device);
    
    /**
     * Returns the device's dev type
     * @param udev_device
     * @return 
     */
    String udev_device_get_devtype(UdevLib.udev_device udev_device);
    
    /**
     * Retrieves the dev path
     * @param udev_device
     * @return 
     */
    String udev_device_get_devpath(UdevLib.udev_device udev_device);
    
    /**
     * Returns the action for this device
     * @param udev_device
     * @return 
     */
    String udev_device_get_action(UdevLib.udev_device udev_device);
    
    /**
     * Removes reference
     * @param udev_device 
     */
    void udev_device_unref(UdevLib.udev_device udev_device);
    
    /**
     * Retrieves the file descriptor of the monitor
     * @param udev_monitor
     * @return 
     */
    int udev_monitor_get_fd(UdevLib.udev_monitor udev_monitor);
    
    /**
     * Enables receiving.
     * @param udev_monitor
     * @return 
     */
    int udev_monitor_enable_receiving(UdevLib.udev_monitor udev_monitor);
    
    /**
     * Release th monitor
     * @param udev_monitor 
     */
    void udev_monitor_unref(UdevLib.udev_monitor udev_monitor);    
    
    /**
     * Release self reference
     * @param udev 
     */
    void udev_unref(UdevLib.udev udev);
    
    /**
     * enumerate reference.
     * @param udev_enumerate
     * @return 
     */
    UdevLib.udev_enumerate udev_enumerate_ref(UdevLib.udev_enumerate udev_enumerate);
    
    /**
     * Scan already connected devices
     * @param udev_enumerate
     * @return 
     */
    int udev_enumerate_scan_devices(UdevLib.udev_enumerate udev_enumerate);
    
    /**
     * New enumerator for devices
     * @param udev
     * @return 
     */
    UdevLib.udev_enumerate udev_enumerate_new(UdevLib.udev udev);
    
    /**
     * Enum list entries
     * @param udev_enumerate
     * @return 
     */
    UdevLib.udev_list_entry udev_enumerate_get_list_entry(UdevLib.udev_enumerate udev_enumerate);
    
    /**
     * Go to the next entry in the enumeration
     * @param list_entry
     * @return 
     */
    UdevLib.udev_list_entry udev_list_entry_get_next(UdevLib.udev_list_entry list_entry);
    
    /**
     * Finds the parent with the given subsystem.
     * @param udev_device
     * @param subsystem
     * @param devtype
     * @return 
     */
    UdevLib.udev_device udev_device_get_parent_with_subsystem_devtype(udev_device udev_device, String subsystem, String devtype);
    
    /**
     * Get the filename from the entry
     * @param list_entry
     * @return 
     */
    String udev_list_entry_get_name(UdevLib.udev_list_entry list_entry);
    
    /**
     * Create a device from a filename
     * @param udev
     * @param syspath
     * @return 
     */
    UdevLib.udev_device udev_device_new_from_syspath(UdevLib.udev udev, String syspath);
    
    /**
     * release reference to enum
     * @param udev_enumerate 
     */
    void udev_enumerate_unref(UdevLib.udev_enumerate udev_enumerate);
    
    /**
     * Udev pointer
     */
    public static class udev extends PointerType {

        public udev(Pointer address) {
            super(address);
        }

        public udev() {
            super();
        }
    };
    
    /**
     * Pointer to the udev monitor
     */
    public static class udev_monitor extends PointerType {

        public udev_monitor(Pointer address) {
            super(address);
        }

        public udev_monitor() {
            super();
        }
    };
    
    /**
     * The pointer to the device reported by the monitor
     */
    public static class udev_device extends PointerType {

        public udev_device(Pointer address) {
            super(address);
        }

        public udev_device() {
            super();
        }
    };
    
    /**
     * udev devices enumerator
     */
    public static class udev_enumerate extends PointerType {

        public udev_enumerate(Pointer address) {
            super(address);
        }

        public udev_enumerate() {
            super();
        }
    };
    
    /**
     * get list entry
     */
    public static class udev_list_entry extends PointerType {

        public udev_list_entry(Pointer address) {
            super(address);
        }

        public udev_list_entry() {
            super();
        }
    };
    
}