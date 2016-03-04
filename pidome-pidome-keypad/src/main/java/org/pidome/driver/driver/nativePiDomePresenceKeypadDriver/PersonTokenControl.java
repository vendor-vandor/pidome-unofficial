/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.driver.nativePiDomePresenceKeypadDriver;

import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlException;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlType;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroup;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlsGroupException;

/**
 *
 * @author John
 */
public class PersonTokenControl extends DeviceControl {

    public PersonTokenControl() throws DeviceControlException, DeviceControlsGroupException {
        super(new DeviceControlsGroup("custom", "Custom"), DeviceControlType.CUSTOM, "custompersontokencontrol");
    }

    @Override
    public Object getValueData() {
        return this.getValueProperty().get();
    }
    
}