/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeNativeKlikAanKlikUitLearningSwitch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.DeviceNotification;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class PidomeNativeKlikAanKlikUitLearningSwitch extends Device {
    
    static Logger LOG = LogManager.getLogger(PidomeNativeKlikAanKlikUitLearningSwitch.class);
    
    public PidomeNativeKlikAanKlikUitLearningSwitch(){}

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        LOG.trace("Handling incomming value: {}", command.getCommandValue());
        switch(String.valueOf(command.getCommandValue())){
            case "true":
                dispatchToDriver(command.getGroupId(), command.getControl().getControlId(), new StringBuilder((String)command.getCommandValueData()).append(":1:").append(this.getDeviceOptions().getSimpleSettingsMap().get("signaltime")).toString());                
            break;
            case "false":
                dispatchToDriver(command.getGroupId(), command.getControl().getControlId(), new StringBuilder((String)command.getCommandValueData()).append(":0:").append(this.getDeviceOptions().getSimpleSettingsMap().get("signaltime")).toString());
            break;
        }
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleData(String data, Object object) {
        DeviceNotification notification = new DeviceNotification();
        switch(data){
            case "1":
                notification.addData(data, data, object);
            break;
            default:
                notification.addData("deviceactions", "deviceswitch", false, false);
            break;
        }
        dispatchToHost(notification);
    }
    
}