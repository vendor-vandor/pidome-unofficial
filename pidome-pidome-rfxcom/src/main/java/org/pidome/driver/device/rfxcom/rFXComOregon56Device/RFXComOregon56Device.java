/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.rFXComOregon56Device;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon56Handler;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class RFXComOregon56Device  extends RFXComDevice {

    RFXComOregon56Handler.PacketSubType subtype = RFXComOregon56Handler.PacketSubType.TYPE1;
    
    @Override
    public Object getPacketSubType() throws PacketProtocolException {
        return subtype;
    }

    @Override
    public RFXComDefinitions.PacketType getPacketType() {
        return RFXComDefinitions.PacketType.OREGON56;
    }

    /**
     * Handles data directly from the RFXCom Oregon driver.
     * @param command 
     */
    @Override
    public void handleData(RFXComCommand command) {
        command.handle(this);
    }

    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void handleData(String data, Object object) {
        /// Not used
    }

    @Override
    public void shutdownDevice() {
        /// not used.
    }

    @Override
    public void startupDevice() {
        switch((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype")){
            case "TYPE1":
                subtype = RFXComOregon56Handler.PacketSubType.TYPE1;
            break;
            case "TYPE2":
                subtype = RFXComOregon56Handler.PacketSubType.TYPE2;
            break;
            case "TYPE3":
                subtype = RFXComOregon56Handler.PacketSubType.TYPE3;
            break;
            case "TYPE4":
                subtype = RFXComOregon56Handler.PacketSubType.TYPE4;
            break;
            case "TYPE5":
                subtype = RFXComOregon56Handler.PacketSubType.TYPE5;
            break;
            case "TYPE6":
                subtype = RFXComOregon56Handler.PacketSubType.TYPE6;
            break;
        }
    }
    
}
