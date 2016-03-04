/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.rFXComCustomDevice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting1.RFXComLighting1Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting2.RFXComLighting2Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting5.RFXComLighting5Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon50Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon51Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon52Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon54Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon55Handler;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComOregon.RFXComOregon56Handler;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class RFXComCustomDevice extends RFXComDevice {

    Logger LOG = LogManager.getLogger(RFXComCustomDevice.class);
    
    RFXComDefinitions.PacketType packetType;
    int subPacketType;
    
    /**
     * Returns the correct sub packet type based on the packet type config.
     * @return
     * @throws PacketProtocolException 
     */
    @Override
    public Object getPacketSubType() throws PacketProtocolException {
        switch(packetType){
            case LIGHTING1:
                return RFXComLighting1Handler.getSubPacketTypes().get(subPacketType);
            case LIGHTING2:
                return RFXComLighting2Handler.getSubPacketTypes().get(subPacketType);
            case LIGHTING5:
                return RFXComLighting5Handler.getSubPacketTypes().get(subPacketType);
            case OREGON50:
                return RFXComOregon50Handler.getSubPacketTypes().get(subPacketType);
            case OREGON51:
                return RFXComOregon51Handler.getSubPacketTypes().get(subPacketType);
            case OREGON52:
                return RFXComOregon52Handler.getSubPacketTypes().get(subPacketType);
            case OREGON54:
                return RFXComOregon54Handler.getSubPacketTypes().get(subPacketType);
            case OREGON55:
                return RFXComOregon55Handler.getSubPacketTypes().get(subPacketType);
            case OREGON56:
                return RFXComOregon56Handler.getSubPacketTypes().get(subPacketType);
        }
        throw new PacketProtocolException("Unsupported protocol");
    }

    /**
     * Returns the packet type as defined in the device's config.
     * @return 
     */
    @Override
    public RFXComDefinitions.PacketType getPacketType() {
        return packetType;
    }

    /**
     * Handles a command request from the outside.
     * @param command
     * @throws UnsupportedDeviceCommandException 
     */
    @Override
    public void handleCommandRequest(DeviceCommandRequest command) throws UnsupportedDeviceCommandException {
        this.dispatchToDriver(command);
    }

    /**
     * Handles data transfered by the driver.
     * Not used in this case.
     * @param data
     * @param object 
     */
    @Override
    public void handleData(String data, Object object) {
        //// not used.
    }
    
    /**
     * Handles data directly from the RFXCom driver.
     * @param command 
     */
    @Override
    public void handleData(RFXComCommand command){
        command.handle(this);
    }

    /**
     * Shuts down any routines.
     */
    @Override
    public void shutdownDevice() {
        /// Not used.
    }

    /**
     * Starts any routines and sets options from the device options.
     */
    @Override
    public void startupDevice() {
        LOG.info("Having option data: {}", this.getDeviceOptions());
        switch((String)this.getDeviceOptions().getSimpleSettingsMap().get("type")){
            case "LIGHTING1":
                packetType = RFXComDefinitions.PacketType.LIGHTING1;
                subPacketType = RFXComLighting1Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "LIGHTING2":
                packetType = RFXComDefinitions.PacketType.LIGHTING2;
                subPacketType = RFXComLighting2Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "LIGHTING5":
                packetType = RFXComDefinitions.PacketType.LIGHTING5;
                subPacketType = RFXComLighting5Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "OREGON50":
                packetType = RFXComDefinitions.PacketType.OREGON50;
                subPacketType = RFXComOregon50Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "OREGON51":
                packetType = RFXComDefinitions.PacketType.OREGON51;
                subPacketType = RFXComOregon51Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "OREGON52":
                packetType = RFXComDefinitions.PacketType.OREGON52;
                subPacketType = RFXComOregon52Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "OREGON54":
                packetType = RFXComDefinitions.PacketType.OREGON54;
                subPacketType = RFXComOregon54Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "OREGON55":
                packetType = RFXComDefinitions.PacketType.OREGON52;
                subPacketType = RFXComOregon55Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
            case "OREGON56":
                packetType = RFXComDefinitions.PacketType.OREGON54;
                subPacketType = RFXComOregon56Handler.getSubPacketTypeIntByStringId((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype"));
            break;
        }
    }
    
}
