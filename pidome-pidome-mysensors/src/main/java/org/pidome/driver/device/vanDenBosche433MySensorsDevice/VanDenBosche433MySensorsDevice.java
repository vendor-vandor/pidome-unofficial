/*
 * Copyright 2014 John Sirach <john.sirach@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pidome.driver.device.vanDenBosche433MySensorsDevice;

import org.pidome.mysensors.PidomeNativeMySensorsDeviceResources14;
import org.pidome.server.connector.drivers.devices.Device;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 * A custom MySensors device handling data for a 433 MySensors proxy device.
 * @author John/Eric van den Bosche
 */
public class VanDenBosche433MySensorsDevice extends Device {

    /**
     * MySensors resource mapping.
     */
    private final PidomeNativeMySensorsDeviceResources14 resources = new PidomeNativeMySensorsDeviceResources14();
    /**
     * The protocol to send.
     */
    private String protocol   = "";
    /**
     * The house code to send.
     */
    private String housecode  = "";
    /**
     * The device code to send.
     */
    private String devicecode = "";
    
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        switch(command.getControlId()){
            case "switchdevice":
                String var1Command = new StringBuilder(protocol).append(housecode).append(devicecode).append(command.getCommandValueData()).toString();
                dispatchToDriver(command.getGroupId(), String.valueOf(resources.getIntByVar(command.getControlId())), var1Command);
            break;
            default:
                throw new UnsupportedDeviceCommandException("Unsupported control used");
        }
    }

    @Override
    public void handleData(String data, Object object) {
        /// Not implemented.
    }

    @Override
    public void shutdownDevice() {
        //// Not used.
    }

    @Override
    public void startupDevice() {
        this.protocol   = (String)this.getDeviceOptions().getSimpleSettingsMap().get("optionProtocol");
        this.housecode  = (String)this.getDeviceOptions().getSimpleSettingsMap().get("optionHouseCode");
        this.devicecode = (String)this.getDeviceOptions().getSimpleSettingsMap().get("optionDeviceCode");
    }
    
}