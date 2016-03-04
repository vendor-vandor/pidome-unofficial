/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.rFXComLighting5Device;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting5.RFXComLighting5Handler;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class RFXComLighting5Device extends RFXComDevice {

    RFXComLighting5Handler.PacketSubType subtype = RFXComLighting5Handler.PacketSubType.LIGHTWAVERF;
    
    /**
     * Returns the packet sub type.
     * @return 
     */
    @Override
    public RFXComLighting5Handler.PacketSubType getPacketSubType() {
        return subtype;
    }

    /**
     * Returns the packet type.
     * @return 
     */
    @Override
    public RFXComDefinitions.PacketType getPacketType() {
        return RFXComDefinitions.PacketType.LIGHTING5;
    }
    
    /**
     * Handles a command request received from RPC.
     * @param command
     * @throws UnsupportedDeviceCommandException 
     */
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        this.dispatchToDriver(command);
    }

    /**
     * Handles data received from a driver.
     * @param data
     * @param object 
     */
    @Override
    public void handleData(String data, Object object) {
        /// not used.
    }

    /**
     * Handles data directly from the RFXCom Lighting 5 driver.
     * @param command 
     */
    @Override
    public void handleData(RFXComCommand command){
        command.handle(this);
    }
    
    /**
     * If there are any tasks or what so ever used, use this to stop them.
     */
    @Override
    public void shutdownDevice() {
        ///Not used.
    }

    /**
     * If there are any automated tasks or what so ever use this to start them.
     */
    @Override
    public void startupDevice() {
        switch((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype")){
            case "LIGHTWAVERF":
                subtype = RFXComLighting5Handler.PacketSubType.LIGHTWAVERF;
            break;
        }
    }

}