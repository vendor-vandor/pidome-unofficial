/*
 * Copyright 2013 John Sirach <john.sirach@gmail.com>.
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

package org.pidome.server.system.hardware.peripherals.usb;

import java.util.HashMap;
import java.util.Map;
import org.pidome.server.connector.drivers.peripherals.hardware.PeripheralHardwareException;

/**
 *
 * @author John Sirach
 */
public abstract class BasePeripheralUSBDevice {

    Map<String, String> usbDetails = new HashMap<>();
    
    String perepheralId;
    
    public String getConnectedPeripheralId(){
        return usbDetails.get("deviceKey");
    }
    
    public String getRegisteredPortName(){
        return usbDetails.get("deviceVendor") + "#" + usbDetails.get("deviceKey");
    }
    
    public void setConnectionDetails(Map<String, String> device){
        usbDetails = device;
    }
    
    public String getPeripheralName(){
        return usbDetails.get("deviceFriendLyName");
    }
    
    public void setPerepheralId(String id){
        perepheralId = id;
    }
    
    public String getPerepheralId() throws PeripheralHardwareException {
        if(perepheralId==null){
            throw new PeripheralHardwareException("Driver does not store device id");
        } else {
            return perepheralId;
        }
    }
    
}
