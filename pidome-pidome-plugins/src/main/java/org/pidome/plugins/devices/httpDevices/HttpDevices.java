/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.plugins.devices.httpDevices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.PluginDeviceMutationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfiguration;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationException;
import org.pidome.server.connector.interfaces.web.configuration.WebConfigurationOptionSet;
import org.pidome.server.connector.plugins.PluginException;
import org.pidome.server.connector.plugins.emulators.DevicePlugin;

/**
 *
 * @author John
 */
public final class HttpDevices extends DevicePlugin {

    Map<String, String> configuration;
    static Logger LOG = LogManager.getLogger(HttpDevices.class);
    
    boolean pluginStarted = false;
    
    public HttpDevices(){
        WebConfiguration conf = new WebConfiguration();
        WebConfigurationOptionSet optionSet = new WebConfigurationOptionSet("Options");
        optionSet.setConfigurationSetDescription("This plugin does not have any additional options. Refer to the documentation about http enabled devices.");
        conf.addOptionSet(optionSet);
        setConfiguration(conf);
    }
    
    @Override
    public void setConfigurationValues(Map<String, String> config) throws WebConfigurationException {

    }

    @Override
    public void startPlugin() throws PluginException {
        pluginStarted = true;
        setRunning(pluginStarted);
    }

    @Override
    public void stopPlugin() {
        pluginStarted = false;
        setRunning(pluginStarted);
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getExpectedDriverId() {
        return "NATIVE_PIDOMEHTTPDEVICES_DRIVER";
    }

    /**
     * @inheritDoc
     */
    @Override
    public String getExpectedDriverVersion() {
        return "0.0.1";
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleDeviceData(Device device, String group, String set, byte[] data, boolean userIntent) {
        if (pluginStarted) {
            handleDeviceCommand(device, group, set, data);
        }
    }

    /**
     * Handles the device command.
     * It sorts out what kind of request and how it's made.
     */
    private void handleDeviceCommand(Device device, String group, String set, byte[] data){
        Runnable exec = () -> {
            String workData = new StringBuilder(device.getAddress()).append(data).toString();
            BufferedReader rd;
            try {
               LOG.debug("Trying url: {}", workData);
               HttpURLConnection conn = (HttpURLConnection) new URL(workData).openConnection();
               conn.setConnectTimeout(2000);
               conn.setReadTimeout(2000);
               conn.setRequestMethod("GET");
               rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
               rd.close();
            } catch (IOException ex) {
               LOG.error("Could not open connection to {}: {} ()", workData, ex.getMessage(), device.getName());
            } catch (Exception ex) {
               LOG.error("An error occured during request to: {} ({}, )", workData, ex.getMessage(), ex.getCause().getMessage(), device.getName());
            }
        };
        exec.run();
    }
    
    @Override
    public void prepareDelete() {
        List<Device> devices = getHardwareDevice().getSoftwareDriver().getRunningDevices();
        List<Integer> deviceIds= new ArrayList<>();
        for(Device device: devices){
            deviceIds.add(device.getId());
        }
        for(Integer id:deviceIds){
            try {
                this.deleteDevice(id);
            } catch (PluginDeviceMutationException ex) {
                LOG.error("Could not delete device {}: {}", id,ex.getMessage());
            }
        }
    }

    @Override
    public void deviceRemoved(Device device) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deviceAdded(Device device) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepareWebPresentation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasGraphData() {
        return false;
    }

    @Override
    public void handleDeviceData(Device device, DeviceCommandRequest dcr) {
        /// not used yet.
    }
    
}
