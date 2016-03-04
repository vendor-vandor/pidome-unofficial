/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.rFXComOregon55Device;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon55Handler;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class RFXComOregon55Device  extends RFXComDevice {

    RFXComOregon55Handler.PacketSubType subtype = RFXComOregon55Handler.PacketSubType.TYPE1;
    
    @Override
    public Object getPacketSubType() throws PacketProtocolException {
        return subtype;
    }

    @Override
    public RFXComDefinitions.PacketType getPacketType() {
        return RFXComDefinitions.PacketType.OREGON55;
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
                subtype = RFXComOregon55Handler.PacketSubType.TYPE1;
            break;
            case "TYPE2":
                subtype = RFXComOregon55Handler.PacketSubType.TYPE2;
            break;
            case "TYPE3":
                subtype = RFXComOregon55Handler.PacketSubType.TYPE3;
            break;
            case "TYPE4":
                subtype = RFXComOregon55Handler.PacketSubType.TYPE4;
            break;
            case "TYPE5":
                subtype = RFXComOregon55Handler.PacketSubType.TYPE5;
            break;
        }
    }
    
}
