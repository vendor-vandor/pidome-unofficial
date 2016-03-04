package org.pidome.driver.driver.nativeRfDriver;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;


public class NativeRfDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface {

    protected static String myPrefix = "N";
    
    Logger LOG = LogManager.getLogger(NativeRfDriver.class);
    
    public NativeRfDriver() {

    }

    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                String inputLine = oEvent.getStringData();
                try {
                    if (inputLine.equals("ARD_RF_OFF")) {
                        sendData("NARDRFON");
                    } else if (inputLine.equals("ARD_RF_OK")) {
                        switch (lastCommand) {
                            case "ARD_RF_ON":
                                LOG.debug("Native 433 Mhz driver has been enabled");
                                break;
                            case "ARD_RF_OFF":
                                LOG.debug("Native 433 Mhz driver has been disabled");
                                break;
                            default:
                                LOG.debug("{}:STATUS_OK", lastCommand);
                                break;
                        }
                    } else if (inputLine.startsWith("ARD_RF_SEND")) {
                        LOG.debug("{}:SEND_SUCCESS", lastCommand);
                    } else {
                        LOG.debug(inputLine);
                    }
                } catch (Exception e) {
                    LOG.error("Error recieving data: {}", e.getMessage());
                }
            break;
        }
    }
    
    @Override
    public boolean sendData(String data) throws IOException {
        return sendData(data, null);
    }

    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        return write(data, prefix);
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