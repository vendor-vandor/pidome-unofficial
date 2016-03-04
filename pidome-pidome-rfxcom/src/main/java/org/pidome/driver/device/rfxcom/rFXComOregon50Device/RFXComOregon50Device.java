/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.rFXComOregon50Device;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon50Handler;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class RFXComOregon50Device  extends RFXComDevice {

    RFXComOregon50Handler.PacketSubType subtype = RFXComOregon50Handler.PacketSubType.TYPE1;
    
    @Override
    public Object getPacketSubType() throws PacketProtocolException {
        return subtype;
    }

    @Override
    public RFXComDefinitions.PacketType getPacketType() {
        return RFXComDefinitions.PacketType.OREGON50;
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
                subtype = RFXComOregon50Handler.PacketSubType.TYPE1;
            break;
            case "TYPE2":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE2;
            break;
            case "TYPE3":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE3;
            break;
            case "TYPE4":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE4;
            break;
            case "TYPE5":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE5;
            break;
            case "TYPE6":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE6;
            break;
            case "TYPE7":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE7;
            break;
            case "TYPE8":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE8;
            break;
            case "TYPE9":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE9;
            break;
            case "TYPE10":
                subtype = RFXComOregon50Handler.PacketSubType.TYPE10;
            break;
        }
    }
    
}
