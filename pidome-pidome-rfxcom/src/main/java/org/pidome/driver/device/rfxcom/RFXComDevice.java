/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pidome.driver.device.rfxcom;

import org.pidome.driver.device.rfxcom.definitions.RFXComDefinitions.PacketType;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.PacketProtocolException;
import org.pidome.driver.driver.nativeRFXComDriver.PacketProtos.RFXComCommand;
import org.pidome.server.connector.drivers.devices.Device;

/**
 *
 * @author John
 */
public abstract class RFXComDevice extends Device {
    
    public abstract Object getPacketSubType() throws PacketProtocolException;
    
    public abstract PacketType getPacketType();
    
    public abstract void handleData(RFXComCommand command);
    
}
