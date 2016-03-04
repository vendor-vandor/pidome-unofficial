/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.driver.nativePiDomeIRRecorderDriver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/**
 *
 * @author John
 */
public class NativePiDomeIRRecorderDriver extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface {

    Logger LOG = LogManager.getLogger(NativePiDomeIRRecorderDriver.class);
    
    boolean recording = false;
    
    private final ScheduledExecutorService delayedExecutor = Executors.newSingleThreadScheduledExecutor();
    
    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        return true;
    }

    /**
     * Only send data when the incoming data equals to recording and test or confirm the signal or turn on or of the record mode.
     * @param data
     * @return 
     */
    @Override
    public boolean sendData(String data) throws IOException {
        return true;
    }

    /**
     * Handles device data.
     * @param deviceData
     * @return 
     */
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        if((recording && (deviceData.equals("TST") || deviceData.equals("CNF"))) || deviceData.equals("REC")){
            delayedExecutor.execute(() -> {
                try {
                    writeBytes(deviceData.getBytes("US-ASCII"));
                } catch (IOException ex) {
                    LOG.error("Can not write to device: ", ex.getMessage());
                }
            });
            return true;
        } else {
            delayedExecutor.execute(() -> {
                try {
                    writeBytes(deviceData.getBytes("US-ASCII"));
                    Thread.sleep(250);
                } catch (IOException | InterruptedException ex) {
                    LOG.error("Can not write to device: ", ex.getMessage());
                }
            });
            return true;
        }
    }
    
    /**
     * Handle raw device data.
     * @param oEvent 
     */
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent){
        String data = oEvent.getStringData().trim();
        LOG.trace("Got event from IR device: {} with string data: {}", oEvent.getEventType(), data);
        switch(oEvent.getEventType()){
            case "DATA_RECEIVED":
                switch(data){
                    case "R":
                        recording = true;
                        this.dispatchDataToDevices("RECON", null);
                    break;
                    case "!R":
                        recording = false;
                        this.dispatchDataToDevices("RECOFF", null);
                    break;
                    case "RCVD":
                        this.dispatchDataToDevices("RECOK", null);
                    break;
                    case "CNF":
                        this.dispatchDataToDevices(data, null);
                        recording = false;
                        this.dispatchDataToDevices("RECOFF", null);
                    break;
                    default:
                        this.dispatchDataToDevices(data, null);
                    break;
                }
            break;
        }
    }

    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
