/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.piRemoteishDriver;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 *
 * @author John
 */
public final class PiRemoteishDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface {

    static Logger LOG = LogManager.getLogger(PiRemoteishDriver.class);
    
    /**
     * To send data to the peripheral driver.
     * @param data
     * @return
     * @throws IOException 
     * @deprecated 
     */
    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        /// not used.
        return true;
    }

    /**
     * To send data to the peripheral driver.
     * @param data
     * @return
     * @throws IOException 
     * @deprecated 
     */
    @Override
    public boolean sendData(String data) throws IOException {
        /// not used
        return true;
    }

    /**
     * Data coming from devices.
     * @param device
     * @param group
     * @param set
     * @param deviceData
     * @return
     * @throws IOException 
     */
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        return writeBytes(deviceData.getBytes());
    }
    
    /**
     * Data received from an peripheral driver.
     * @param oEvent 
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        LOG.trace("Got from hardware driver: {} - {} bytes", oEvent.getStringData(), oEvent.getByteArrayOutputStream().size());
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                String data = oEvent.getStringData();
                if(getRunningDevices().iterator().hasNext()){
                    getRunningDevices().iterator().next().handleData(data, oEvent);
                }
            break;
        }
    }
    
}
