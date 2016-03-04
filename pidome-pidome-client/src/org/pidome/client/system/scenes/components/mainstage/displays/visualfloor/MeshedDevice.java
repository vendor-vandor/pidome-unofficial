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

package org.pidome.client.system.scenes.components.mainstage.displays.visualfloor;

import java.io.IOException;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeEvent;
import org.pidome.client.system.domotics.components.devices.DeviceValueChangeListener;
import org.pidome.client.system.domotics.components.devices.Devices;

/**
 *
 * @author John
 */
public class MeshedDevice extends MeshedItem implements DeviceValueChangeListener {
    
    Device device;
    
    VisualDevice visualDevice;
    
    protected MeshedDevice(VisualDevice visualDevice, double x, double y) throws DomComponentsException, IOException {
        super(VisualFloorUtils.createSimpleOnePlanedMesh((float)visualDevice.getWidth(), (float)visualDevice.getHeight()), visualDevice, x, y);
        device = Devices.getDeviceById(visualDevice.getDeviceId());
        addPulseHandler();
    }
    
    protected final Device getDevice(){
        return this.device;
    }

    @Override
    final void addPulseHandler(){
        device.addDeviceValueEventListener(this);
    }
    
    @Override
    final void removePulseHandler(){
        device.removeDeviceValueEventListener(this);
    }
    
    @Override
    public void handleDeviceValueChange(DeviceValueChangeEvent event) {
        pulse(150);
    }
    
}
