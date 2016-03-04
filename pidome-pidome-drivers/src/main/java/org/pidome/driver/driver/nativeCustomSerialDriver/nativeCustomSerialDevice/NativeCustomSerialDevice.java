/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativeCustomSerialDriver.nativeCustomSerialDevice;

import org.apache.logging.log4j.LogManager;
import org.pidome.server.connector.drivers.devices.BasicDataForDeviceContainer;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;


/**
 *
 * @author John
 */
public class NativeCustomSerialDevice extends Device {

    org.apache.logging.log4j.Logger LOG = LogManager.getLogger(NativeCustomSerialDevice.class);
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest dcr) throws UnsupportedDeviceCommandException {
        this.dispatchToDriver(dcr);
    }

    @Override
    public void handleData(String string, Object o) {
        ///Data send from the driver is encapsulated in a BasicDataForDeviceContainer object
        BasicDataForDeviceContainer workObject = (BasicDataForDeviceContainer)o;
        try {
            DeviceControl control = this.getFullCommandSet().getControlsGroup(workObject.getGroupId()).getDeviceControl(workObject.getControlId());
            Object finalValue;
            switch(control.getDataType()){
                case BOOLEAN:
                    finalValue = Boolean.parseBoolean(workObject.getValue().toString().toLowerCase());
                break;
                case INTEGER:
                    finalValue = Integer.parseInt(workObject.getValue().toString());
                break;
                case FLOAT:
                    finalValue = Float.parseFloat(workObject.getValue().toString());
                break;
                default:    
                    finalValue = workObject.getValue().toString();
                break;
            }
            DeviceNotification not = new DeviceNotification();
            not.addData(workObject.getGroupId(), workObject.getControlId(), finalValue);
            this.dispatchToHost(not);
        } catch (DeviceControlsGroupException | DeviceControlException ex) {
            LOG.error("There is no group and control combination with group id: {}, and control id: {}", workObject.getGroupId(), workObject.getControlId());
        }
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not needed."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not needed."); //To change body of generated methods, choose Tools | Templates.
    }
    
}