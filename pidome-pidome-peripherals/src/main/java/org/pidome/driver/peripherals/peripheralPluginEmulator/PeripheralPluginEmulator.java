/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.peripherals.peripheralPluginEmulator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDataEvent;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriver;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareDriverInterface;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;
import org.pidome.server.connector.emulators.PeripheralPlugin;
import org.pidome.server.connector.emulators.PluginPeripheral;

/**
 *
 * @author John
 */
public class PeripheralPluginEmulator extends PeripheralHardwareDriver implements PeripheralHardwareDriverInterface,PluginPeripheral {

    boolean pluginReceiverStarted = false;
    
    Logger LOG = LogManager.getLogger(PeripheralPluginEmulator.class);
    
    PeripheralPlugin _pluginLink;
    
    public PeripheralPluginEmulator() throws PeripheralHardwareException {
        prepare();
    }
    
    @Override
    public void initDriver() throws PeripheralHardwareException,UnsupportedOperationException {
        //// not needed to initialize. But overriding is needed to start.
    }
    
    @Override
    public void startDriver() throws PeripheralHardwareException {
        pluginReceiverStarted = true;
    }

    @Override
    public void stopDriver() {
        pluginReceiverStarted = false;
    }

    @Override
    public String readPort() throws IOException {
        throw new UnsupportedOperationException("Not used. Use dispatchPluginData");
    }

    @Override
    public void writePort(String data) throws IOException {
        throw new UnsupportedOperationException("Not used. Use dispatchPluginData");
    }
    
    @Override
    public void writePort(byte[] data) throws IOException {
        throw new UnsupportedOperationException("Not used. Use dispatchPluginData"); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void releaseDriver() {
        _pluginLink = null;
    }

    @Override
    public void handlePluginData(byte[] data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        bos.write(data, 0, data.length);
        dispatchData(PeripheralHardwareDataEvent.DATA_RECEIVED, bos);
    }

    @Override
    public void dispatchPluginData(Device device, String group, String set, byte[] data, boolean userIntent) {
        if(_pluginLink!=null)_pluginLink.handleDeviceData(device, group, set, data, userIntent);
    }

    @Override
    public void setPluginLink(PeripheralPlugin plugin) {
        _pluginLink = plugin;
    }

    @Override
    public void removePluginLink() {
        _pluginLink = null;
    }

    @Override
    public PeripheralPlugin getPluginLink() {
        return _pluginLink;
    }

    @Override
    public void dispatchPluginData(Device device, DeviceCommandRequest dcr) {
        if(_pluginLink!=null)_pluginLink.handleDeviceData(device, dcr);
    }

    @Override
    public void deviceLoaded(Device device) {
        if(_pluginLink!=null)_pluginLink.deviceLoaded(device);
    }
}