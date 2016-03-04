/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeNativeKlikAanKlikUitLearningMotionSensor;

import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class PidomeNativeKlikAanKlikUitLearningMotionSensor extends Device {

    public PidomeNativeKlikAanKlikUitLearningMotionSensor(){}
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        throw new UnsupportedDeviceCommandException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        DeviceNotification notification = new DeviceNotification();
        notification.addData("devicereadings", "devicenoticed", false,false);
        dispatchToHost(notification);
    }

    @Override
    public void handleData(String data, Object object) {
        DeviceNotification notification = new DeviceNotification();
        notification.addData("devicereadings", "devicenoticed", data.equals("1"),false);
        dispatchToHost(notification);
    }
    
}
