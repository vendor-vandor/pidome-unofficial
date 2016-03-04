/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeServerDriver;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;



/**
 * This class is now here just for consistency purposes.
 * It will change to the interface between the server device and the server peripheral driver
 * @author John
 */
public class NativeServerDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface {

    static Logger LOG = LogManager.getLogger(NativeServerDriver.class);
    
    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        /// I'm just an empty driver at the moment
        return false;
    }

    @Override
    public boolean sendData(String data) throws IOException {
        return false;
    }

    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        return sendData(deviceData);
    }
    
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        LOG.trace("Got from hardware driver: {} - {} bytes",oEvent.getStringData(), oEvent.getByteArrayOutputStream().size());
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                String data = oEvent.getStringData();
                dispatchDataToDevices(data, data);
            break;
        }
    }

    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
