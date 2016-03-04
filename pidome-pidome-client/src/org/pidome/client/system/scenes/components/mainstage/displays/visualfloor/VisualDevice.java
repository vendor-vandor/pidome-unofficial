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
import javafx.scene.image.Image;
import org.pidome.client.system.domotics.DomComponentsException;
import org.pidome.client.system.domotics.components.devices.Device;
import org.pidome.client.system.domotics.components.devices.Devices;

/**
 *
 * @author John
 */
public class VisualDevice extends Image {
    
    Device device;
    
    protected VisualDevice(int deviceId) throws DomComponentsException, IOException {
        super(VisualFloorUtils.loadSmallDeviceImage(Devices.getDeviceById(deviceId)));
        this.device = Devices.getDeviceById(deviceId);
    }
    
    public final int getDeviceId(){
        return this.device.getId();
    }
    
}