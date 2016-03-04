/*
 * Copyright 2014 John.
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

package org.pidome.server.services.automations.variables;

import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControl;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceControlDataType;
import org.pidome.server.connector.drivers.devices.devicestructure.DeviceToggleControl;
import org.pidome.server.services.automations.rule.AutomationRulesVarProxy;
import org.pidome.server.services.hardware.DeviceService;

/**
 *
 * @author John
 */
public class DeviceAutomationVariable extends AutomationVariable {

    DeviceControlDataType dataType;
    
    public DeviceAutomationVariable(int deviceId, String group, String control) throws Exception {
        super(AutomationRulesVarProxy.getDeviceBindListener(new StringBuilder(String.valueOf(deviceId)).append(group).append(control).toString()), "DeviceVar_" + deviceId);
        DeviceControl deviceControl;
        try {
            deviceControl = DeviceService.getDevice(deviceId).getFullCommandSet().getControlsGroup(group).getDeviceControl(control);
        } catch (Exception ex){
            //// If a device is not yet loaded, get an offline instance to retreive the datatype;
            deviceControl = DeviceService.getOfflineDeviceInstance(deviceId).getFullCommandSet().getControlsGroup(group).getDeviceControl(control);
        }
        if(deviceControl instanceof DeviceToggleControl){
            this.dataType = DeviceControlDataType.BOOLEAN;
        } else {
            this.dataType = deviceControl.getDataType();
        }
        try {
            this.set(deviceControl.getValueData());
        } catch (Exception ex){
            //// When this is reached the device is not loaded.
        }
    }

    public final DeviceControlDataType getDeviceDataType(){
        return this.dataType;
    }
    
    @Override
    public void destroy() {
        this.unlink();
    }
    
}
