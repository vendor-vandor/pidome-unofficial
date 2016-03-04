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
package org.pidome.server.system.hardware.peripherals.usb.windows;

import org.pidome.server.system.hardware.peripherals.usb.InternalUsbListener;
import org.pidome.server.system.hardware.peripherals.usb.InternalUsbEvent;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.DBT;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jssc.SerialPortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.Peripheral.SubSystem;
import org.pidome.server.system.db.DB;

/**
 * Used for USB events on windows
 * @author John Sirach
 */
public class WindowsUsb implements WinUser.WindowProc {

    WinDef.HWND hWnd;
    
    private final List _listeners = new ArrayList();
    
    static Logger LOG = LogManager.getLogger(WindowsUsb.class);
    
    boolean running = false;
    
    /**
     * Constructor
     */
    public WindowsUsb() {

    }
    
    /**
     * Hooks on windows process for receive USB plugs
     * 
     */
    public void startWinListener(){
        if(running==false){
            running = true;
            // define new window class
            WString windowClass = new WString("MyWindowClass");
            WinDef.HMODULE hInst = Kernel32.INSTANCE.GetModuleHandle("");

            WinUser.WNDCLASSEX wClass = new WinUser.WNDCLASSEX();
            wClass.hInstance = hInst;
            wClass.lpfnWndProc = WindowsUsb.this;
            wClass.lpszClassName = windowClass;

            // register window class
            User32.INSTANCE.RegisterClassEx(wClass);

            // create new window
            hWnd = User32.INSTANCE
                    .CreateWindowEx(
                    User32.WS_EX_TOPMOST,
                    windowClass,
                    "My hidden helper window, used only to catch the windows events",
                    0, 0, 0, 0, 0, WinUser.HWND_MESSAGE, null, hInst, null);
            LOG.debug("Succesfully created USB monitor window: " + hWnd.getPointer().toString());
            /* this filters for all usb device classes */
            DBT.DEV_BROADCAST_DEVICEINTERFACE notificationFilter = new DBT.DEV_BROADCAST_DEVICEINTERFACE();
            notificationFilter.dbcc_devicetype = DBT.DBT_DEVTYP_DEVICEINTERFACE;
            notificationFilter.dbcc_classguid = DBT.GUID_DEVINTERFACE_USB_DEVICE;
            notificationFilter.dbcc_size = notificationFilter.size();


            WinUser.HDEVNOTIFY hDevNotify = User32.INSTANCE.RegisterDeviceNotification(hWnd, notificationFilter, User32.DEVICE_NOTIFY_WINDOW_HANDLE);
            if (hDevNotify != null) {
                LOG.debug("RegisterDeviceNotification was sucessfully");
                WinUser.MSG msg = new WinUser.MSG();
                while (User32.INSTANCE.GetMessage(msg, hWnd, 0, 0) != 0) {
                    User32.INSTANCE.TranslateMessage(msg);
                    User32.INSTANCE.DispatchMessage(msg);
                }
            } else {
                LOG.error("RegisterDeviceNotification has failed!");
            }
        }
    }
    
    /**
     * Returns if the service is accepting usb connections.
     * @return 
     */
    public final boolean watchDogRunning(){
        return hWnd!=null;
    }
    
    /**
     * Callback function for windows process.
     * @param hwnd
     * @param uMsg
     * @param wParam
     * @param lParam
     * @return process result.
     */
    @Override
    public WinDef.LRESULT callback(WinDef.HWND hwnd, int uMsg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
        switch (uMsg) {
            case WinUser.WM_DEVICECHANGE: {
                onDeviceChange(wParam, lParam);
                return new WinDef.LRESULT(0);
            }
            default:
                return User32.INSTANCE.DefWindowProc(hwnd, uMsg, wParam, lParam);
        }
    }

    /**
     * Checks if the broadcasted device change is an device add or removal.
     * @param wParam
     * @param lParam 
     */
    protected void onDeviceChange(WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
        DBT.DEV_BROADCAST_DEVICEINTERFACE bdif = new DBT.DEV_BROADCAST_DEVICEINTERFACE(lParam.longValue());
        switch (wParam.intValue()) {
            case DBT.DBT_DEVICEARRIVAL:
                LOG.debug("DBT_DEVICEARRIVAL (USB device added)");
                setDeviceParameters(bdif.getDbcc_name());
                break;
            case DBT.DBT_DEVICEREMOVECOMPLETE:
                LOG.debug("DBT_DEVICEREMOVECOMPLETE (USB removed)");
                triggerDeviceRemoved(bdif.getDbcc_name());
                break;
            default:
                //// unhandled device change
        }
    }
    
    /**
     * Used for initial discovery of attached devices during startup.
     */
    public final void discover(){
        LOG.info("started USB discover");
        List<String> knownPorts = getRegisteredComports();
        String[] regKeys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Enum\\USB\\");
        for (String regKey : regKeys) {
            if (isKnownHardwareDevice(regKey)) {
                LOG.debug("Supported USB device: {}", regKey);
                String[] knownDevices = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Enum\\USB\\" + regKey + "\\");
                for (String knownDevice : knownDevices) {
                    String hasPort = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Enum\\USB\\" + regKey + "\\" + knownDevice + "\\Device Parameters", "PortName");
                    if (knownPorts.contains(hasPort)) {
                        LOG.debug("USB device {} has port {}", regKey, hasPort);
                        setDeviceParameters(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Enum\\USB\\" + regKey + "\\" + knownDevice + "\\Device Parameters", "SymbolicName"));
                    }
                }
            }
        }
    }
    
    /**
     * Checks if a specific windows connected vid and pid is included.
     * @param vidpid
     * @return 
     */
    private boolean isKnownHardwareDevice(String vidpid){
        Matcher m = Pattern.compile("VID_([0-9a-z]+)&PID_([0-9a-z]+)").matcher(vidpid.trim());
        if(m.matches()){
            try (Connection fileDBConnection = DB.getConnection(DB.DB_SYSTEM)){
                PreparedStatement prep = fileDBConnection.prepareStatement("SELECT ip.id FROM installed_peripherals ip WHERE ip.vid=? and ip.pid=? LIMIT 1");
                prep.setString(1, m.group(1));
                prep.setString(2, m.group(2));
                try (ResultSet rsDevices = prep.executeQuery()) {
                    if (rsDevices.next()) {
                        return true;
                    }
                }
                prep.close();
            } catch (SQLException ex) {
                LOG.error("Could not load installed devices from database: {}", ex.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Retrieves a list of known com ports
     * @return List<String> List of com ports
     */
    private List<String> getRegisteredComports(){
        List<String> knownPorts = new ArrayList();
        knownPorts.addAll(Arrays.asList(SerialPortList.getPortNames()));
        LOG.debug("Available com ports -> {}", knownPorts);
        return knownPorts;
    }
    
    /**
     * Checks device parameters in the windows registry.
     * @param deviceName Full device name as reported in windows
     */
    void setDeviceParameters(String deviceName){
        //// \\?\USB#Vid_2341&Pid_0043#749343030303514061C1#{a5dcbf10-6530-11d2-901f-00c04fb951ed}
        deviceName = deviceName.toLowerCase();
        Matcher m = Pattern.compile("(.+)#vid_([0-9a-z]+)&pid_([0-9a-z]+)#([0-9a-z]+)#(.+)").matcher(deviceName.trim());
        //// Check for a basic match
        if(m.matches()){
            /// We only support devices wich have devicesettings with portname in the registry at this moment.
            String deviceDesc = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "System\\CurrentControlSet\\Enum\\USB\\Vid_"+m.group(2)+"&Pid_"+m.group(3) + "\\" + m.group(4),
                    "DeviceDesc");
            String devicePort = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                    "System\\CurrentControlSet\\Enum\\USB\\Vid_"+m.group(2)+"&Pid_"+m.group(3) + "\\" + m.group(4) + "\\Device Parameters",
                    "PortName");
            LOG.debug("Got device '{}' on port '{}' (Windows name, will try to fix naming)", deviceDesc,devicePort);
            if(deviceDesc.contains(";")){
                deviceDesc = deviceDesc.split(";")[1];
            }
            LOG.info("Found device '{}' on port '{}'", deviceDesc,devicePort);
            _fireDeviceEvent(InternalUsbEvent.DEVICE_ADDED,
                             deviceDesc,
                             m.group(2),
                             m.group(3),
                             m.group(4),
                             devicePort);
        } else {
            LOG.info("Unsupported device: {}",deviceName);
        }
    }
    
    /**
     * Event for when a device is removed.
     * @param deviceName 
     */
    public void triggerDeviceRemoved(String deviceName){
        deviceName = deviceName.toLowerCase();
        Matcher m = Pattern.compile("(.+)#vid_([0-9a-z]+)&pid_([0-9a-z]+)#([0-9a-z]+)#(.+)").matcher(deviceName.trim());
        if(m.matches()){
             LOG.info("Peripheral {} has been removed", m.group(4));
            _fireDeviceEvent(InternalUsbEvent.DEVICE_REMOVED,m.group(4));
        }
    }
    
    /**
     * Adds an event listener for USB device changes.
     * @param l 
     */
    public synchronized void addEventListener( InternalUsbListener l ) {
        _listeners.add( l );
        LOG.debug("Added listener: {}", l.getClass().getName());
    }
    
    /**
     * Removes a listener for USB device changes.
     * @param l 
     */
    public synchronized void removeEventListener( InternalUsbListener l ) {
        _listeners.remove( l );
        LOG.debug("Removed listener: {}", l.getClass().getName());
    }
    
    /**
     * This usb device event is used when a device disconnects
     * @param EVENTTYPE
     * @param usbKey 
     */
    private synchronized void _fireDeviceEvent(String EVENTTYPE, String usbKey) {
        LOG.debug("Event: {}, {}", EVENTTYPE, usbKey);
        InternalUsbEvent internalUsbEvent = new InternalUsbEvent(this, EVENTTYPE);
        Iterator listeners = _listeners.iterator();
        while( listeners.hasNext() ) {
            ( (InternalUsbListener) listeners.next() ).deviceMutation(SubSystem.UNKNOWN, null, null, null, null, usbKey, null );
        }
    }
    
    /**
     * Used for when a device is added.
     * @param EVENTTYPE
     * @param DeviceName
     * @param vendorId
     * @param deviceId
     * @param usbKey
     * @param devicePort 
     * @see #_fireDeviceEvent(java.lang.String, java.lang.String) 
     */
    private synchronized void _fireDeviceEvent(String EVENTTYPE, String DeviceName, String vendorId, String deviceId, String usbKey, String devicePort) {
        LOG.debug("Event: {}, {}", EVENTTYPE, usbKey);
        Iterator listeners = _listeners.iterator();
        InternalUsbEvent internalUsbEvent = new InternalUsbEvent(this, EVENTTYPE);
        while( listeners.hasNext() ) {
            ( (InternalUsbListener) listeners.next() ).deviceMutation( SubSystem.SERIAL, "Unknown", DeviceName, vendorId, deviceId, usbKey, devicePort );
        }
    }
    
    
    /**
     * Gets the last error.
     *
     * @return the last error
     */
    public int getLastError() {
        int rc = Kernel32.INSTANCE.GetLastError();
        if (rc != 0) {
            LOG.error(rc);
        }
        return rc;
    }
    
}
