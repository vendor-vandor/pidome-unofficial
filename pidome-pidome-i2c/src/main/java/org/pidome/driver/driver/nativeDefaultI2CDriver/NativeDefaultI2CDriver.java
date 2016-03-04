/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.driver.nativeDefaultI2CDriver;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;


/**
 *
 * @author John Sirach
 */
public class NativeDefaultI2CDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface {

    Logger LOG = LogManager.getLogger(NativeDefaultI2CDriver.class);
    
    public NativeDefaultI2CDriver() {}

    @Override
    public boolean sendData(String data) throws IOException {
        return sendData(data, null);
    }

    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        return write(data, prefix);
    }

    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                String data = oEvent.getStringData();
                dispatchDataToDevices(data, oEvent.getByteArrayOutputStream().toByteArray());
            break;
        }
    }
    
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        return sendData(deviceData);
    }

    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}