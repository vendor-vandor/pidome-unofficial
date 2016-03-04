/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.plugins.mqtt.pidomeMQTTBroker;

import java.util.List;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.interfaces.web.presentation.WebPresentSimpleNVP;
import org.pidome.server.connector.plugins.PluginException;

/**
 * interface for the broker plugin runtypes.
 * @author John
 */
public interface PluginRunner {
    
    /**
     * Start method.
     * @throws org.pidome.server.connector.plugins.PluginException
     */
    public void start() throws PluginException;
    
    /**
     * Stop method.
     */
    public void stop();
    
    /**
     * Pass on device data including user intent.
     * When the user intent is false the command is created by an internal process, you should consider if you want to publish.
     * @param device
     * @param group
     * @param control
     * @param data 
     * @param userIntent 
     */
    public void handleDeviceData(Device device, String group, String control, byte[] data, boolean userIntent);
    
    /**
     * Adds link so resources are available.
     * @param parent 
     */
    public void setParent(PidomeMQTTBroker parent);
    
    /**
     * Removes the link.
     */
    public void unsetParent();
    
}
