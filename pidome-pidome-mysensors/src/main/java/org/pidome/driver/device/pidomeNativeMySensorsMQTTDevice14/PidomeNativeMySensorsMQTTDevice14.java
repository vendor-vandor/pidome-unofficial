/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeNativeMySensorsMQTTDevice14;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.pidomeNativeMySensorsDevice14.PidomeNativeMySensorsDevice14;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceColorPickerControlColorData;

/**
 *
 * @author John
 */
public class PidomeNativeMySensorsMQTTDevice14 extends PidomeNativeMySensorsDevice14 {

    static Logger LOG = LogManager.getLogger(PidomeNativeMySensorsMQTTDevice14.class);
    
    public PidomeNativeMySensorsMQTTDevice14(){
        super();
    }

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getDataType()){
            case BOOLEAN:
                dispatchToDriver(command.getGroupId(), command.getControlId(), ((boolean)command.getCommandValue()==true)?"1":"0");
            break;
            case COLOR:
                DeviceColorPickerControlColorData colorData = new DeviceColorPickerControlColorData(command.getCommandValue());
                dispatchToDriver(command.getGroupId(), command.getControlId(), colorData.getHex().replace("#", ""));
            break;
            default:
                dispatchToDriver(command.getGroupId(), command.getControlId(), command.getCommandValueData().toString());
            break;
        }
    }

    @Override
    public void handleData(String data, Object object) {
        /// Not used handled in plugin.
    }    
    
}