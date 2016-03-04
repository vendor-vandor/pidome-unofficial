/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.server.connector.drivers.peripherals.software;

import java.util.Map;

/**
 *
 * @author John
 */
public interface PeripheralDriverDeviceMutationInterface {
    public void removeDeviceByDriver(int deviceId) throws PeripheralDriverDeviceMutationException;
    public void addDeviceByDriver(int installedId, String name, String address, int location, int category) throws PeripheralDriverDeviceMutationException;
    public void addDeviceByDriver(int installedId, String name, String address, int location, int category, Map<String,Object> settingsXml) throws PeripheralDriverDeviceMutationException;
    public void createCustomDeviceByDriver(String identifier, String friendlyname, String deviceName, Map<String,Object> struct, int driverId, int packageId) throws PeripheralDriverDeviceMutationException;
    public int createDeviceSkeletonByDriver(PeripheralSoftwareDriver driver, DiscoveredDevice deviceStructureCreator) throws PeripheralDriverDeviceMutationException;
}
