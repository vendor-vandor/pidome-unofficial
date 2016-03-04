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

package org.pidome.client.scenes.floormap;

import java.io.IOException;
import org.pidome.client.entities.devices.Device;
import org.pidome.client.entities.devices.DevicePulseListener;

/**
 *
 * @author John
 */
public class MeshedDevice extends MeshedItem implements DevicePulseListener {
    
    Device device;
    
    VisualDevice visualDevice;
    
    protected MeshedDevice(VisualDevice visualDevice, double x, double y) throws IOException {
        super(VisualFloorUtils.createSimpleOnePlanedMesh((float)visualDevice.getWidth(), (float)visualDevice.getHeight()), visualDevice, x, y);
        device = visualDevice.getDevice();
        addPulseHandler();
    }
    
    protected final Device getDevice(){
        return this.device;
    }

    @Override
    final void addPulseHandler(){
        device.addDevicePulseListener(this);
    }
    
    @Override
    final void removePulseHandler(){
        device.removeDevicePulseListener(this);
    }
    
    @Override
    public void pulse() {
        pulse(150);
    }
    
}
