/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom.rFXComLighting1Device;

import org.pidome.driver.device.rfxcom.RFXComDevice;
import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComLighting1.RFXComLighting1Handler;
import org.pidome.server.connector.drivers.devices.DeviceCommandRequest;
import org.pidome.server.connector.drivers.devices.UnsupportedDeviceCommandException;

/**
 *
 * @author John
 */
public class RFXComLighting1Device extends RFXComDevice {

    RFXComLighting1Handler.PacketSubType subtype;
    
    /**
     * Returns the packet sub type.
     * @return 
     */
    @Override
    public RFXComLighting1Handler.PacketSubType getPacketSubType() {
        return subtype;
    }

    /**
     * Returns the packet type.
     * @return 
     */
    @Override
    public RFXComDefinitions.PacketType getPacketType() {
        return RFXComDefinitions.PacketType.LIGHTING1;
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
     * Handles data directly from the RFXCom Lighting 2 driver.
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
        /// not used.
    }

    /**
     * If there are any automated tasks or what so ever use this to start them.
     */
    @Override
    public void startupDevice() {
        switch((String)this.getDeviceOptions().getSimpleSettingsMap().get("subtype")){
            case "X10":
                subtype = RFXComLighting1Handler.PacketSubType.X10;
            break;
            case "ARC":
                subtype = RFXComLighting1Handler.PacketSubType.ARC;
            break;
            case "ELRO_AB400D":
                subtype = RFXComLighting1Handler.PacketSubType.ELRO_AB400D;
            break;
            case "WAVEMAN":
                subtype = RFXComLighting1Handler.PacketSubType.WAVEMAN;
            break;
            case "EMW200":
                subtype = RFXComLighting1Handler.PacketSubType.EMW200;
            break;
            case "IMPULS":
                subtype = RFXComLighting1Handler.PacketSubType.IMPULS;
            break;
            case "RISING_SUN":
                subtype = RFXComLighting1Handler.PacketSubType.RISING_SUN;
            break;
            case "PHILIPS_SBC":
                subtype = RFXComLighting1Handler.PacketSubType.PHILIPS_SBC;
            break;
            case "ENERGENIE":
                subtype = RFXComLighting1Handler.PacketSubType.ENERGENIE;
            break;
            case "ENERGENIE5":
                subtype = RFXComLighting1Handler.PacketSubType.ENERGENIE5;
            break;
            case "COCO_GDR2":
                subtype = RFXComLighting1Handler.PacketSubType.COCO_GDR2;
            break;
        }
    }

}