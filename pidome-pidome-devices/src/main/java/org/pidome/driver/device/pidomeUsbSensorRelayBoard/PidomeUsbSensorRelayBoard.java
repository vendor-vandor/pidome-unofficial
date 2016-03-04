/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pidome.driver.device.pidomeUsbSensorRelayBoard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John Sirach
 */
public class PidomeUsbSensorRelayBoard extends Device {

    static Logger LOG = LogManager.getLogger(PidomeUsbSensorRelayBoard.class);

    public PidomeUsbSensorRelayBoard(){}
    
    @Override
    public void handleData(String data, Object object) {
        LOG.debug("Got handleData: {} - {}", data, object);
    }

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        dispatchToDriver(command.getGroupId(), command.getControlId(), (String)command.getCommandValueData());
    }

    @Override
    public void shutdownDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void startupDevice() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
