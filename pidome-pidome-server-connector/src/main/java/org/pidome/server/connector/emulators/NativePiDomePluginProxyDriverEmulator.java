package org.pidome.server.connector.emulators;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriver;
import org.pidome.server.connector.drivers.peripherals.software.PeripheralSoftwareDriverInterface;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author John
 */
public class NativePiDomePluginProxyDriverEmulator extends PeripheralSoftwareDriver implements PeripheralSoftwareDriverInterface {

    Logger LOG = LogManager.getLogger(NativePiDomePluginProxyDriverEmulator.class);
    
    @Override
    public boolean sendData(String data, String prefix) throws IOException {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sendData(String data) throws IOException {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean handleDeviceData(Device device, String group, String set, String deviceData, boolean userIntent) throws IOException {
        if(getHardwareDriver() instanceof PluginPeripheral){
            ((PluginPeripheral)getHardwareDriver()).dispatchPluginData(device, group,set,deviceData.getBytes(),userIntent);
        }
        return true;
    }
    
    @Override
    public boolean handleDeviceData(Device device, String group, String set, String deviceData) throws IOException {
        if(getHardwareDriver() instanceof PluginPeripheral){
            ((PluginPeripheral)getHardwareDriver()).dispatchPluginData(device, group,set,deviceData.getBytes(), true);
        }
        return true;
    }
    

    @Override
    public boolean handleDeviceData(Device device, DeviceCommandRequest request) throws IOException {
        if(getHardwareDriver() instanceof PluginPeripheral){
            ((PluginPeripheral)getHardwareDriver()).dispatchPluginData(device, request);
        }
        return true;
    }
    
    @Override
    public void driverBaseDataReceived(PeripheralHardwareDataEvent oEvent) {
        switch(oEvent.getEventType()){
            case PeripheralHardwareDataEvent.DATA_RECEIVED:
                String data = oEvent.getStringData();
                LOG.trace("Data received from device hardware driver: {}", data);
                dispatchDataToDevices(data, oEvent.getByteArrayOutputStream().toByteArray());
            break;
        }
    }
    
    @Override
    public final void deviceLoaded(Device device){
        if(getHardwareDriver() instanceof PluginPeripheral){
            ((PluginPeripheral)getHardwareDriver()).deviceLoaded(device);
        }
    }
    
}