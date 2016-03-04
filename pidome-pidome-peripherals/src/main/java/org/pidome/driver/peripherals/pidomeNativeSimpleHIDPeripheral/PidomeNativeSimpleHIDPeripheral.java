/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.peripherals.pidomeNativeSimpleHIDPeripheral;

import com.codeminders.hidapi.HIDDevice;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;

/**
 *
 * @author John Sirach
 */
public class PidomeNativeSimpleHIDPeripheral extends PeripheralHardwareDriver implements PeripheralHardwareDriverInterface {

    static HIDManager manager;
    HIDDevice hidDevice;
    
    int managerCount = 0;
    
    private static final int BUFSIZE = 2048;
    
    private static final long READ_UPDATE_DELAY_MS = 50L;
    
    static Logger LOG = LogManager.getLogger(PidomeNativeSimpleHIDPeripheral.class);
    
    public PidomeNativeSimpleHIDPeripheral() throws PeripheralHardwareException {
        prepare();
    }
    
    @Override
    public void initDriver() throws PeripheralHardwareException {
        if(manager==null){
            try {
                com.codeminders.hidapi.ClassPathLibraryLoader.loadNativeHIDLibrary();
                manager = HIDManager.getInstance();
            } catch (IOException ex) {
                LOG.error("Manager could not be loaded: {}", ex.getMessage(), ex);
            }
        }
        managerCount++;
    }
    
    @Override
    public void startDriver() throws PeripheralHardwareException {
        try {
            String combinedPort = this.getPort();
            LOG.debug("got port: {}", combinedPort);
            HIDDeviceInfo[] devs = manager.listDevices();
            String devicePath = "";
            for (HIDDeviceInfo device : devs) {
                if (device.getProduct_id() == Integer.parseInt(getProductID(), 16) && device.getVendor_id() == Integer.parseInt(getVendorID(), 16)) {
                    devicePath = device.getPath();
                }
            }
            if (!devicePath.equals("")){
                LOG.trace("Using path: {} to open hid device", devicePath);
                hidDevice = manager.openByPath(devicePath);
                LOG.trace("Manufacturer: {}", hidDevice.getManufacturerString());
                LOG.trace("Product: {}", hidDevice.getProductString());
                LOG.trace("Serial Number: {}", hidDevice.getSerialNumberString());
                LOG.debug("Peripheral started attempting to start reader.");
            } else {
                throw new PeripheralHardwareException("Device not found");
            }
        } catch (Exception ex) {
            LOG.error("Could not get list of attached usb devices, or device open failed: {} "+ ex.getMessage(), ex);
            throw new PeripheralHardwareException("Could not get list of attached usb devices, or device open failed: "+ ex.getMessage());
        }
        
    }

    @Override
    public PeripheralVersion getSoftwareId() throws PeripheralHardwareException {
        createPeripheralVersion("NATIVE_PIDOME_USBHID_PASSTHROUGH_0.0.1");
        return getVersion();
    }
    
    @Override
    public void stopDriver() {
        try {
            hidDevice.close();
            LOG.debug("Closed HID device");
        } catch (IOException ex) {
            LOG.error("Could not close HID device, or port is already closed: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public String readPort() throws IOException {
        throw new UnsupportedOperationException("Internal reader used.");
    }

    @Override
    public final void writePort(String command) throws IOException {
        LOG.trace("Have to send: {}",command);
        if (command.length() > 32) {
            command = command.substring(0, 32);
        }
        char[] charArray = command.toCharArray();
        byte[] dataCmd = new byte[33];
        dataCmd[0] = (byte) 0;
        for (int i = 0; i < command.length(); i++) {
            dataCmd[i + 1] = (byte) charArray[i];
        }
        LOG.debug("Written data length {}", hidDevice.write(dataCmd));
    }
    
    @Override
    public void releaseDriver() {
        managerCount--;
        if(managerCount==0){
            manager.release();
            LOG.debug("Unloaded manager");
        }
    }

}
