/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.devices;

/**
 *
 * @author John
 */
public interface PluginDeviceMutationInterface {
    public void removeDeviceByPlugin(int deviceId) throws PluginDeviceMutationException;
    public void addFromExistingDeviceByPlugin(int installedId, String name, String address, int location, int category) throws PluginDeviceMutationException;
    public void addDeviceByPlugin(int installedId, String name, String location, int category) throws PluginDeviceMutationException;
}
